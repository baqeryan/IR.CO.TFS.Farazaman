package Ir.co.tfs.farazaman

import android.app.Application
import android.app.Activity
import android.content.Context
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import dagger.hilt.android.HiltAndroidApp
import Ir.co.tfs.farazaman.security.DeveloperOptionsGuard
import Ir.co.tfs.farazaman.notification.FirebaseDeviceTokenRegistrar
import Ir.co.tfs.farazaman.notification.NotificationPermissionHelper
import Ir.co.tfs.farazaman.notification.AppNotificationSync
import Ir.co.tfs.farazaman.realtime.MobileSupervisorHubClient
import Ir.co.tfs.farazaman.util.TokenManager
import Ir.co.tfs.farazaman.util.AuthStateManager
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application() {
    @Inject lateinit var tokenManager: TokenManager
    @Inject lateinit var firebaseDeviceTokenRegistrar: FirebaseDeviceTokenRegistrar
    @Inject lateinit var mobileSupervisorHubClient: MobileSupervisorHubClient
    @Inject lateinit var authStateManager: AuthStateManager
    @Inject lateinit var appNotificationSync: AppNotificationSync

    private var startedActivityCount = 0

    override fun onCreate() {
        super.onCreate()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags("fa"))
        registerActivityLifecycleCallbacks(applicationActivityCallbacks)
        authStateManager.registerAuthStateReceiver(authStateReceiver)
    }

    private val authStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.getBooleanExtra(AuthStateManager.EXTRA_IS_LOGGED_IN, false) == true) {
                mobileSupervisorHubClient.onAuthenticationAvailable()
            } else {
                mobileSupervisorHubClient.stop()
            }
        }
    }

    private val applicationActivityCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            activity.window.decorView.apply {
                layoutDirection = View.LAYOUT_DIRECTION_RTL
                textDirection = View.TEXT_DIRECTION_RTL
            }
        }

        override fun onActivityStarted(activity: Activity) {
            startedActivityCount++
            if (startedActivityCount == 1) {
                mobileSupervisorHubClient.enterForeground()
                appNotificationSync.refreshUnreadCount()
            }
        }
        override fun onActivityResumed(activity: Activity) {
            DeveloperOptionsGuard.enforce(activity)
            if (tokenManager.isLoggedIn()) {
                NotificationPermissionHelper.requestIfNeeded(activity)
                firebaseDeviceTokenRegistrar.syncCurrentToken()
                mobileSupervisorHubClient.onAuthenticationAvailable()
                appNotificationSync.refreshUnreadCount()
            }
        }
        override fun onActivityPaused(activity: Activity) = Unit
        override fun onActivityStopped(activity: Activity) {
            startedActivityCount = (startedActivityCount - 1).coerceAtLeast(0)
            if (startedActivityCount == 0 && !activity.isChangingConfigurations) {
                mobileSupervisorHubClient.leaveForeground()
            }
        }
        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit
        override fun onActivityDestroyed(activity: Activity) = Unit
    }
}
