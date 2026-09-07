package Ir.co.tfs.farazaman.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.activity.SplashActivity
import javax.inject.Inject

@AndroidEntryPoint
class FarazamanFirebaseMessagingService : FirebaseMessagingService() {

    @Inject lateinit var tokenRegistrar: FirebaseDeviceTokenRegistrar
    @Inject lateinit var appNotificationSync: AppNotificationSync

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        tokenRegistrar.register(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val title = message.notification?.title ?: message.data[DATA_TITLE].orEmpty()
        val body = message.notification?.body ?: message.data[DATA_BODY].orEmpty()
        val receivedAt = message.sentTime.takeIf { it > 0L } ?: System.currentTimeMillis()
        val recordId = message.data["notificationId"]
            ?: message.messageId
            ?: "$receivedAt-${message.data.hashCode()}"
        FirebaseNotificationInboxStore.upsert(
            this,
            FirebaseNotificationRecord(
                id = recordId,
                title = title,
                body = body,
                receivedAtMs = receivedAt,
                data = message.data,
            ),
        )
        appNotificationSync.refreshUnreadCount(force = true)
        showNotification(
            message,
            title.ifBlank { getString(R.string.fcm_notification_default_title) },
            body,
        )
    }

    private fun showNotification(message: RemoteMessage, title: String, body: String) {
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) return

        createNotificationChannel()
        val intent = Intent(this, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            message.data.forEach { (key, value) -> putExtra(key, value) }
        }
        val notificationId = message.messageId?.hashCode() ?: System.currentTimeMillis().hashCode()
        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notifications)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()
        NotificationManagerCompat.from(this).notify(notificationId, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.fcm_notification_channel_name),
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = getString(R.string.fcm_notification_channel_description)
        }
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        const val CHANNEL_ID = "farazaman_notifications"
        private const val DATA_TITLE = "title"
        private const val DATA_BODY = "body"
    }
}
