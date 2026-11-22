package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint

/** Compatibility entry point retained for existing intents and shortcuts. */
@AndroidEntryPoint
class TrackManager : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(
            Intent(this, TrackManagerComposeActivity::class.java),
        )
        finish()
    }
}
