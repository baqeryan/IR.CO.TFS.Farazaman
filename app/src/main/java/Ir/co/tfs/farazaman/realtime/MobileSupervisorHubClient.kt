package Ir.co.tfs.farazaman.realtime

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.HubConnectionState
import com.microsoft.signalr.TypeReference
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import Ir.co.tfs.farazaman.AppConstants
import Ir.co.tfs.farazaman.util.TokenManager
import Ir.co.tfs.farazaman.util.RolesManager
import java.util.UUID
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Owns the ASP.NET Core SignalR connection while the application is in foreground. */
@Singleton
class MobileSupervisorHubClient @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferences,
    private val tokenManager: TokenManager,
    private val rolesManager: RolesManager,
    private val gson: Gson,
) {
    private val lock = Any()
    private val reconnectExecutor = Executors.newSingleThreadScheduledExecutor { runnable ->
        Thread(runnable, "supervisor-signalr-reconnect").apply { isDaemon = true }
    }

    private var connection: HubConnection? = null
    private var connectionUrl: String? = null
    private var startDisposable: Disposable? = null
    private var stopDisposable: Disposable? = null
    private var reconnectFuture: ScheduledFuture<*>? = null
    private var foreground = false
    private var reconnectAttempt = 0
    private var generation = 0L

    fun enterForeground() {
        synchronized(lock) {
            foreground = true
            generation++
        }
        startIfEligible()
    }

    fun leaveForeground() {
        synchronized(lock) {
            foreground = false
            generation++
            cancelReconnectLocked()
        }
        stop()
    }

    fun onAuthenticationAvailable() = startIfEligible()

    fun stop() {
        val current = synchronized(lock) {
            generation++
            cancelReconnectLocked()
            startDisposable?.dispose()
            startDisposable = null
            reconnectAttempt = 0
            connection.also {
                connection = null
                connectionUrl = null
            }
        } ?: return

        stopDisposable?.dispose()
        stopDisposable = current.stop()
            .subscribeOn(Schedulers.io())
            .subscribe(
                { Log.d(TAG, "SignalR stopped") },
                { Log.w(TAG, "SignalR stop failed: ${it.javaClass.simpleName}") },
            )
    }

    fun sendChatMessage(
        recipientUserName: String,
        text: String,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    ) {
        val message = MobileChatMessage().apply {
            this.recipientUserName = recipientUserName
            this.text = text
        }
        invokeVoid("SendChatMessage", message, onSuccess = onSuccess, onError = onError)
    }

    fun submitLocation(
        response: LocationResponse,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    ) = invokeVoid("SubmitLocation", response, onSuccess = onSuccess, onError = onError)

    fun rejectLocation(
        requestId: UUID,
        reason: String,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {},
    ) = invokeVoid("RejectLocation", requestId, reason, onSuccess = onSuccess, onError = onError)

    fun getOnlineUsers(
        onResult: (List<OnlineMobileUser>) -> Unit,
        onError: (Throwable) -> Unit = {},
    ) {
        val current = connectedConnection() ?: return onError(notConnectedError())
        val type = object : TypeReference<List<OnlineMobileUser>>() {}.type
        current.invoke<List<OnlineMobileUser>>(type, "GetOnlineUsers")
            .subscribeOn(Schedulers.io())
            .subscribe(onResult, onError)
    }

    private fun startIfEligible() {
        val url = selectedHubUrl()
        val currentGeneration: Long
        val current: HubConnection
        synchronized(lock) {
            if (!foreground || !tokenManager.isLoggedIn() || !rolesManager.hasSupervisorRole()) return
            if (connectionUrl != null && connectionUrl != url) {
                connection?.stop()?.subscribeOn(Schedulers.io())?.subscribe({}, {})
                connection = null
                connectionUrl = null
            }
            current = connection ?: createConnection(url).also {
                connection = it
                connectionUrl = url
            }
            if (current.connectionState != HubConnectionState.DISCONNECTED ||
                startDisposable?.isDisposed == false
            ) return
            currentGeneration = generation
        }

        startDisposable = current.start()
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    synchronized(lock) {
                        if (currentGeneration != generation || !foreground) {
                            current.stop().subscribeOn(Schedulers.io()).subscribe({}, {})
                            return@subscribe
                        }
                        reconnectAttempt = 0
                        startDisposable = null
                    }
                    Log.d(TAG, "SignalR connected")
                    broadcast(ACTION_CONNECTION_CHANGED) {
                        putExtra(EXTRA_CONNECTED, true)
                    }
                },
                { error ->
                    synchronized(lock) { startDisposable = null }
                    Log.w(TAG, "SignalR connection failed: ${error.javaClass.simpleName}")
                    scheduleReconnect()
                },
            )
    }

    private fun createConnection(url: String): HubConnection {
        return HubConnectionBuilder.create(url)
            .withAccessTokenProvider(
                Single.defer {
                    tokenManager.getAccessToken()
                        ?.takeIf(String::isNotBlank)
                        ?.let { Single.just(it) }
                        ?: Single.error<String>(IllegalStateException("Access token is unavailable"))
                },
            )
            .withHandshakeResponseTimeout(HANDSHAKE_TIMEOUT_MS)
            .build()
            .apply {
                serverTimeout = SERVER_TIMEOUT_MS
                registerEvents(this)
                onClosed {
                    broadcast(ACTION_CONNECTION_CHANGED) {
                        putExtra(EXTRA_CONNECTED, false)
                    }
                    scheduleReconnect()
                }
            }
    }

    private fun registerEvents(hub: HubConnection) {
        hub.on("ReceiveChatMessage", { value: MobileChatMessage ->
            broadcastJson(ACTION_CHAT_MESSAGE, value)
        }, MobileChatMessage::class.java)
        hub.on("LocationRequested", { value: LocationRequest ->
            broadcastJson(ACTION_LOCATION_REQUESTED, value)
        }, LocationRequest::class.java)
        hub.on("LocationReceived", { value: LocationResponse ->
            broadcastJson(ACTION_LOCATION_RECEIVED, value)
        }, LocationResponse::class.java)
        hub.on("LocationRejected", { value: LocationRejected ->
            broadcastJson(ACTION_LOCATION_REJECTED, value)
        }, LocationRejected::class.java)

        val onlineUsersType = object : TypeReference<List<OnlineMobileUser>>() {}.type
        hub.on("OnlineUsersChanged", { users: List<OnlineMobileUser> ->
            broadcastJson(ACTION_ONLINE_USERS_CHANGED, users)
        }, onlineUsersType)
    }

    private fun scheduleReconnect() {
        val delay: Long
        val scheduledGeneration: Long
        synchronized(lock) {
            if (!foreground || !tokenManager.isLoggedIn() ||
                !rolesManager.hasSupervisorRole() || reconnectFuture != null
            ) return
            delay = RECONNECT_DELAYS_SECONDS[
                reconnectAttempt.coerceAtMost(RECONNECT_DELAYS_SECONDS.lastIndex)
            ]
            reconnectAttempt++
            scheduledGeneration = generation
            reconnectFuture = reconnectExecutor.schedule({
                synchronized(lock) {
                    reconnectFuture = null
                    if (scheduledGeneration != generation || !foreground) return@schedule
                }
                startIfEligible()
            }, delay, TimeUnit.SECONDS)
        }
    }

    private fun cancelReconnectLocked() {
        reconnectFuture?.cancel(false)
        reconnectFuture = null
    }

    private fun connectedConnection(): HubConnection? = synchronized(lock) {
        connection?.takeIf { it.connectionState == HubConnectionState.CONNECTED }
    }

    private fun invokeVoid(
        method: String,
        vararg args: Any,
        onSuccess: () -> Unit,
        onError: (Throwable) -> Unit,
    ) {
        val current = connectedConnection() ?: return onError(notConnectedError())
        current.invoke(Void::class.java, method, *args)
            .subscribeOn(Schedulers.io())
            .subscribe({ onSuccess() }, onError)
    }

    private fun selectedHubUrl(): String {
        val base = preferences.getString("BASE_URL", AppConstants.BASE_URL)
            ?.trim()?.trimEnd('/').orEmpty().ifBlank { AppConstants.BASE_URL.trimEnd('/') }
        return "$base/hubs/mobile?clientType=supervisor"
    }

    private fun broadcastJson(action: String, value: Any) {
        broadcast(action) { putExtra(EXTRA_PAYLOAD_JSON, gson.toJson(value)) }
    }

    private inline fun broadcast(action: String, configure: Intent.() -> Unit) {
        context.sendBroadcast(Intent(action).setPackage(context.packageName).apply(configure))
    }

    private fun notConnectedError() = IllegalStateException("SignalR is not connected")

    companion object {
        private const val TAG = "MobileSupervisorHub"
        private const val HANDSHAKE_TIMEOUT_MS = 15_000L
        private const val SERVER_TIMEOUT_MS = 30_000L
        private val RECONNECT_DELAYS_SECONDS = longArrayOf(2, 5, 10, 30, 60)

        const val EXTRA_PAYLOAD_JSON = "payload_json"
        const val EXTRA_CONNECTED = "connected"
        const val ACTION_CONNECTION_CHANGED = "Ir.co.tfs.farazaman.realtime.CONNECTION_CHANGED"
        const val ACTION_CHAT_MESSAGE = "Ir.co.tfs.farazaman.realtime.CHAT_MESSAGE"
        const val ACTION_ONLINE_USERS_CHANGED = "Ir.co.tfs.farazaman.realtime.ONLINE_USERS_CHANGED"
        const val ACTION_LOCATION_REQUESTED = "Ir.co.tfs.farazaman.realtime.LOCATION_REQUESTED"
        const val ACTION_LOCATION_RECEIVED = "Ir.co.tfs.farazaman.realtime.LOCATION_RECEIVED"
        const val ACTION_LOCATION_REJECTED = "Ir.co.tfs.farazaman.realtime.LOCATION_REJECTED"
    }
}
