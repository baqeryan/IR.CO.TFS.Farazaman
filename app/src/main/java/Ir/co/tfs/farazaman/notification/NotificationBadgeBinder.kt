package Ir.co.tfs.farazaman.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.view.View
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import Ir.co.tfs.farazaman.R

object NotificationBadgeBinder {
    fun bind(anchor: View, owner: LifecycleOwner) {
        val badge = BadgeDrawable.create(anchor.context).apply {
            backgroundColor = ContextCompat.getColor(anchor.context, R.color.app_error)
            badgeTextColor = ContextCompat.getColor(anchor.context, R.color.white)
            maxCharacterCount = 3
        }
        fun render(count: Int) {
            badge.isVisible = count > 0
            badge.number = count.coerceAtMost(999)
        }
        render(NotificationUnreadCountStore.read(anchor.context))
        BadgeUtils.attachBadgeDrawable(badge, anchor)

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                render(intent?.getIntExtra(NotificationUnreadCountStore.EXTRA_UNREAD_COUNT, 0) ?: 0)
            }
        }
        owner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            private var registered = false
            override fun onStart(owner: LifecycleOwner) {
                if (registered) return
                ContextCompat.registerReceiver(
                    anchor.context,
                    receiver,
                    IntentFilter(NotificationUnreadCountStore.ACTION_UNREAD_COUNT_CHANGED),
                    ContextCompat.RECEIVER_NOT_EXPORTED,
                )
                registered = true
                render(NotificationUnreadCountStore.read(anchor.context))
            }

            override fun onStop(owner: LifecycleOwner) {
                if (registered) {
                    anchor.context.unregisterReceiver(receiver)
                    registered = false
                }
            }

            override fun onDestroy(owner: LifecycleOwner) {
                if (registered) anchor.context.unregisterReceiver(receiver)
                registered = false
                BadgeUtils.detachBadgeDrawable(badge, anchor)
            }
        })
    }
}
