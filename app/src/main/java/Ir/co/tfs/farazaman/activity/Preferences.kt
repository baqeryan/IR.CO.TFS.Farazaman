package Ir.co.tfs.farazaman.activity

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.preference.PreferenceManager
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import Ir.co.tfs.farazaman.OSMTracker
import Ir.co.tfs.farazaman.presentation.compose.PreferencesEvent
import Ir.co.tfs.farazaman.presentation.compose.PreferencesScreen
import Ir.co.tfs.farazaman.presentation.compose.PreferencesUiState
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.login.LoginActivity
import Ir.co.tfs.farazaman.util.LogoutCacheCleaner

class Preferences : AppCompatActivity() {
    companion object {
        const val LAYOUTS_SUBDIR = "layouts"
        const val LAYOUT_FILE_EXTENSION = ".xml"
        const val ICONS_DIR_SUFFIX = "_icons"
    }

    private val prefs by lazy { PreferenceManager.getDefaultSharedPreferences(this) }
    private var uiState by mutableStateOf(PreferencesUiState())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        uiState = loadState()
        setContent { FaraZamanTheme { PreferencesScreen(uiState, ::handleEvent) } }
    }

    private fun loadState() = PreferencesUiState(
        voiceDuration = prefs.getString(OSMTracker.Preferences.KEY_VOICEREC_DURATION, OSMTracker.Preferences.VAL_VOICEREC_DURATION).orEmpty(),
        soundEnabled = prefs.getBoolean(OSMTracker.Preferences.KEY_SOUND_ENABLED, OSMTracker.Preferences.VAL_SOUND_ENABLED),
        useBarometer = prefs.getBoolean(OSMTracker.Preferences.KEY_USE_BAROMETER, OSMTracker.Preferences.VAL_USE_BAROMETER),
        checkGpsAtStartup = prefs.getBoolean(OSMTracker.Preferences.KEY_GPS_CHECKSTARTUP, OSMTracker.Preferences.VAL_GPS_CHECKSTARTUP),
        ignoreGpsClock = prefs.getBoolean(OSMTracker.Preferences.KEY_GPS_IGNORE_CLOCK, OSMTracker.Preferences.VAL_GPS_IGNORE_CLOCK),
        gpsInterval = prefs.getString(OSMTracker.Preferences.KEY_GPS_LOGGING_INTERVAL, OSMTracker.Preferences.VAL_GPS_LOGGING_INTERVAL).orEmpty(),
        gpsMinDistance = prefs.getString(OSMTracker.Preferences.KEY_GPS_LOGGING_MIN_DISTANCE, OSMTracker.Preferences.VAL_GPS_LOGGING_MIN_DISTANCE).orEmpty(),
        storageDirectory = prefs.getString(OSMTracker.Preferences.KEY_STORAGE_DIR, OSMTracker.Preferences.VAL_STORAGE_DIR).orEmpty(),
        directoryPerTrack = prefs.getBoolean(OSMTracker.Preferences.KEY_OUTPUT_DIR_PER_TRACK, OSMTracker.Preferences.VAL_OUTPUT_GPX_OUTPUT_DIR_PER_TRACK),
        filenamePattern = prefs.getString(OSMTracker.Preferences.KEY_OUTPUT_FILENAME, OSMTracker.Preferences.VAL_OUTPUT_FILENAME).orEmpty(),
        filenameLabel = prefs.getString(OSMTracker.Preferences.KEY_OUTPUT_FILENAME_LABEL, OSMTracker.Preferences.VAL_OUTPUT_FILENAME_LABEL).orEmpty(),
        accuracyMode = prefs.getString(OSMTracker.Preferences.KEY_OUTPUT_ACCURACY, OSMTracker.Preferences.VAL_OUTPUT_ACCURACY).orEmpty(),
        approximateHdop = prefs.getBoolean(OSMTracker.Preferences.KEY_OUTPUT_GPX_HDOP_APPROXIMATION, OSMTracker.Preferences.VAL_OUTPUT_GPX_HDOP_APPROXIMATION),
        compassMode = prefs.getString(OSMTracker.Preferences.KEY_OUTPUT_COMPASS, OSMTracker.Preferences.VAL_OUTPUT_COMPASS).orEmpty(),
        pictureSource = prefs.getString(OSMTracker.Preferences.KEY_UI_PICTURE_SOURCE, OSMTracker.Preferences.VAL_UI_PICTURE_SOURCE).orEmpty(),
        keepScreenOn = prefs.getBoolean(OSMTracker.Preferences.KEY_UI_DISPLAY_KEEP_ON, OSMTracker.Preferences.VAL_UI_DISPLAY_KEEP_ON),
        showOsmTrack = prefs.getBoolean(OSMTracker.Preferences.KEY_UI_DISPLAYTRACK_OSM, OSMTracker.Preferences.VAL_UI_DISPLAYTRACK_OSM),
        orientation = prefs.getString(OSMTracker.Preferences.KEY_UI_ORIENTATION, OSMTracker.Preferences.VAL_UI_ORIENTATION).orEmpty(),
        canClearOsmToken = prefs.contains(OSMTracker.Preferences.KEY_OSM_OAUTH2_ACCESSTOKEN),
    )

    private fun handleEvent(event: PreferencesEvent) {
        when (event) {
            PreferencesEvent.Back -> finish()
            PreferencesEvent.OpenLocationSettings -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            PreferencesEvent.Logout -> LogoutCacheCleaner.clearAndThen(this, Runnable {
                startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
                finish()
            })
            PreferencesEvent.ClearOsmToken -> update { remove(OSMTracker.Preferences.KEY_OSM_OAUTH2_ACCESSTOKEN) }
            is PreferencesEvent.SetBoolean -> update { putBoolean(event.key, event.value) }
            is PreferencesEvent.SetString -> update { putString(event.key, event.value) }
        }
    }

    private fun update(block: android.content.SharedPreferences.Editor.() -> Unit) {
        prefs.edit().apply(block).apply()
        uiState = loadState()
    }
}
