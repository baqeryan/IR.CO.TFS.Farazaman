package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.security.DeveloperOptionsGuard

/**
 * Activity that displays a warning when Developer Options is enabled
 * and prevents the user from accessing the app until it's disabled.
 */
class DeveloperOptionsWarningActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developer_options_warning)

        val titleText = findViewById<TextView>(R.id.warning_title)
        val messageText = findViewById<TextView>(R.id.warning_message)
        val settingsButton = findViewById<Button>(R.id.btn_open_settings)
        val recheckButton = findViewById<Button>(R.id.btn_recheck)

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = Unit
            },
        )

        titleText.text = "هشدار امنیتی"
        messageText.text = "برای استفاده از این برنامه، لطفاً گزینه‌های توسعه‌دهنده (Developer Options) را خاموش کنید.\n\nفعال بودن این گزینه می‌تواند امنیت اطلاعات شما را به خطر بیندازد."

        settingsButton.setOnClickListener {
            // Open Developer Options settings
            try {
                val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                startActivity(intent)
            } catch (e: Exception) {
                // If direct access fails, open general settings
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
        }

        recheckButton.setOnClickListener {
            // Recheck if Developer Options is still enabled
            if (!DeveloperOptionsGuard.shouldBlockCurrentBuild(this)) {
                // Developer Options is now disabled; reveal the activity that was previously open.
                finish()
            } else {
                // Still enabled, show a toast or update message
                messageText.text = "گزینه‌های توسعه‌دهنده هنوز فعال است. لطفاً آن را خاموش کنید و مجدداً امتحان کنید."
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!DeveloperOptionsGuard.shouldBlockCurrentBuild(this)) {
            finish()
        }
    }
}

