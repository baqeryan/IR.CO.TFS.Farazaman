package Ir.co.tfs.farazaman.notification

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import Ir.co.tfs.farazaman.data.repository.AppNotificationRepository
import Ir.co.tfs.farazaman.util.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationUnreadCountStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: SharedPreferences,
) {
    fun current(): Int = read(context)

    fun update(value: Int) {
        val count = value.coerceAtLeast(0)
        preferences.edit().putInt(KEY_UNREAD_COUNT, count).apply()
        context.sendBroadcast(
            Intent(ACTION_UNREAD_COUNT_CHANGED)
                .setPackage(context.packageName)
                .putExtra(EXTRA_UNREAD_COUNT, count),
        )
    }

    companion object {
        private const val KEY_UNREAD_COUNT = "app_notification_unread_count"
        const val ACTION_UNREAD_COUNT_CHANGED =
            "Ir.co.tfs.farazaman.notification.UNREAD_COUNT_CHANGED"
        const val EXTRA_UNREAD_COUNT = "unread_count"

        fun read(context: Context): Int =
            android.preference.PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
                .getInt(KEY_UNREAD_COUNT, 0)
                .coerceAtLeast(0)
    }
}

@Singleton
class AppNotificationSync @Inject constructor(
    private val repository: AppNotificationRepository,
    private val store: NotificationUnreadCountStore,
    private val tokenManager: TokenManager,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)
    @Volatile private var lastAttemptAt = 0L

    fun refreshUnreadCount(force: Boolean = false) {
        if (!tokenManager.isLoggedIn()) return
        val now = System.currentTimeMillis()
        if (!force && now - lastAttemptAt < MIN_REFRESH_INTERVAL_MS) return
        if (!running.compareAndSet(false, true)) return
        lastAttemptAt = now
        scope.launch {
            try {
                store.update(repository.unreadCount())
            } catch (_: Exception) {
                // The last confirmed count remains visible; foreground refresh can retry later.
            } finally {
                running.set(false)
            }
        }
    }

    companion object { private const val MIN_REFRESH_INTERVAL_MS = 30_000L }
}
