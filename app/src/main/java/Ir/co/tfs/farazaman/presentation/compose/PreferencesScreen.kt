package Ir.co.tfs.farazaman.presentation.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import Ir.co.tfs.farazaman.OSMTracker
import Ir.co.tfs.farazaman.R
import Ir.co.tfs.farazaman.presentation.design.components.FzButton
import Ir.co.tfs.farazaman.presentation.design.components.FzButtonVariant
import Ir.co.tfs.farazaman.presentation.design.components.FzCard
import Ir.co.tfs.farazaman.presentation.design.components.FzHeaderSize
import Ir.co.tfs.farazaman.presentation.design.components.FzPageTemplate
import Ir.co.tfs.farazaman.presentation.design.components.FzTextField
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanTheme
import Ir.co.tfs.farazaman.presentation.design.theme.FaraZamanThemeTokens

data class PreferencesUiState(
    val voiceDuration: String = "2", val soundEnabled: Boolean = true, val useBarometer: Boolean = false,
    val checkGpsAtStartup: Boolean = true, val ignoreGpsClock: Boolean = false, val gpsInterval: String = "3",
    val gpsMinDistance: String = "5", val storageDirectory: String = "/osmtracker", val directoryPerTrack: Boolean = true,
    val filenamePattern: String = "name_date", val filenameLabel: String = "", val accuracyMode: String = "none",
    val approximateHdop: Boolean = false, val compassMode: String = "none", val pictureSource: String = "camera",
    val keepScreenOn: Boolean = true, val showOsmTrack: Boolean = false, val orientation: String = "none",
    val canClearOsmToken: Boolean = false,
)

sealed interface PreferencesEvent : UiEvent {
    data object Back : PreferencesEvent
    data object Logout : PreferencesEvent
    data object OpenLocationSettings : PreferencesEvent
    data object ClearOsmToken : PreferencesEvent
    data class SetBoolean(val key: String, val value: Boolean) : PreferencesEvent
    data class SetString(val key: String, val value: String) : PreferencesEvent
}

private data class Choice(val value: String, val label: String)

@Composable
fun PreferencesScreen(state: PreferencesUiState, onEvent: (PreferencesEvent) -> Unit, modifier: Modifier = Modifier) {
    val spacing = FaraZamanThemeTokens.spacing
    var edit by remember { mutableStateOf<Triple<String, String, String>?>(null) }
    var choices by remember { mutableStateOf<Pair<String, List<Choice>>?>(null) }
    var confirmLogout by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }
    val durations = stringArrayResource(R.array.prefs_voicerec_durations).map { Choice(it, "$it ${stringResource(R.string.prefs_voicerec_duration_seconds)}") }
    val filenameChoices = stringArrayResource(R.array.prefs_output_filename_values).zip(stringArrayResource(R.array.prefs_output_filename_keys)).map { Choice(it.first, it.second) }
    val accuracyChoices = stringArrayResource(R.array.prefs_output_accuracy_values).zip(stringArrayResource(R.array.prefs_output_accuracy_keys)).map { Choice(it.first, it.second) }
    val compassChoices = stringArrayResource(R.array.prefs_compass_heading_values).zip(stringArrayResource(R.array.prefs_compass_heading_keys)).map { Choice(it.first, it.second) }
    val orientationChoices = stringArrayResource(R.array.prefs_ui_orientation_options_values).zip(stringArrayResource(R.array.prefs_ui_orientation_options_keys)).map { Choice(it.first, it.second) }
    val gpsIntervalTitle = stringResource(R.string.prefs_gps_logging_interval)
    val gpsDistanceTitle = stringResource(R.string.prefs_gps_logging_min_distance)
    val storageTitle = stringResource(R.string.prefs_storage_dir)
    val filenameLabelTitle = stringResource(R.string.prefs_output_filename_label)

    FzPageTemplate(title = "تنظیمات", headerSize = FzHeaderSize.Compact, onBack = { onEvent(PreferencesEvent.Back) }, modifier = modifier) { padding ->
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(padding), verticalArrangement = Arrangement.spacedBy(spacing.sm)) {
            PreferenceSection(stringResource(R.string.prefs_gps))
            ActionPreference(stringResource(R.string.prefs_gps_os_settings), stringResource(R.string.prefs_gps_os_settings_summary)) { onEvent(PreferencesEvent.OpenLocationSettings) }
            SwitchPreference(stringResource(R.string.prefs_check_gps_startup), stringResource(R.string.prefs_check_gps_startup_summary), state.checkGpsAtStartup) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_GPS_CHECKSTARTUP, it)) }
            SwitchPreference(stringResource(R.string.prefs_gps_ignore_clock), stringResource(R.string.prefs_gps_ignore_clock_summary), state.ignoreGpsClock) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_GPS_IGNORE_CLOCK, it)) }
            ValuePreference(gpsIntervalTitle, state.gpsInterval) { edit = Triple(OSMTracker.Preferences.KEY_GPS_LOGGING_INTERVAL, gpsIntervalTitle, state.gpsInterval) }
            ValuePreference(gpsDistanceTitle, state.gpsMinDistance) { edit = Triple(OSMTracker.Preferences.KEY_GPS_LOGGING_MIN_DISTANCE, gpsDistanceTitle, state.gpsMinDistance) }

            PreferenceSection(stringResource(R.string.prefs_output))
            ValuePreference(storageTitle, state.storageDirectory) { edit = Triple(OSMTracker.Preferences.KEY_STORAGE_DIR, storageTitle, state.storageDirectory) }
            SwitchPreference(stringResource(R.string.prefs_output_one_dir_per_track), stringResource(R.string.prefs_output_one_dir_per_track_summary), state.directoryPerTrack) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_OUTPUT_DIR_PER_TRACK, it)) }
            ChoicePreference(stringResource(R.string.prefs_output_filename), state.filenamePattern) { choices = OSMTracker.Preferences.KEY_OUTPUT_FILENAME to filenameChoices }
            ValuePreference(filenameLabelTitle, state.filenameLabel) { edit = Triple(OSMTracker.Preferences.KEY_OUTPUT_FILENAME_LABEL, filenameLabelTitle, state.filenameLabel) }
            ChoicePreference(stringResource(R.string.prefs_output_accuracy), state.accuracyMode) { choices = OSMTracker.Preferences.KEY_OUTPUT_ACCURACY to accuracyChoices }
            SwitchPreference(stringResource(R.string.prefs_output_gpx_hdop_approximation), stringResource(R.string.prefs_output_gpx_hdop_approximation_summary), state.approximateHdop) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_OUTPUT_GPX_HDOP_APPROXIMATION, it)) }
            ChoicePreference(stringResource(R.string.prefs_compass_heading), state.compassMode) { choices = OSMTracker.Preferences.KEY_OUTPUT_COMPASS to compassChoices }

            PreferenceSection(stringResource(R.string.prefs_ui))
            ChoicePreference(stringResource(R.string.prefs_voicerec_duration), state.voiceDuration) { choices = OSMTracker.Preferences.KEY_VOICEREC_DURATION to durations }
            SwitchPreference(stringResource(R.string.prefs_sound_enabled), stringResource(R.string.prefs_sound_enabled_summary), state.soundEnabled) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_SOUND_ENABLED, it)) }
            SwitchPreference(stringResource(R.string.prefs_use_barometer), stringResource(R.string.prefs_use_barometer_summary), state.useBarometer) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_USE_BAROMETER, it)) }
            SwitchPreference(stringResource(R.string.prefs_display_always_on), stringResource(R.string.prefs_display_always_on_summary), state.keepScreenOn) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_UI_DISPLAY_KEEP_ON, it)) }
            SwitchPreference(stringResource(R.string.prefs_displaytrack_osm), stringResource(R.string.prefs_displaytrack_osm_summary), state.showOsmTrack) { onEvent(PreferencesEvent.SetBoolean(OSMTracker.Preferences.KEY_UI_DISPLAYTRACK_OSM, it)) }
            ChoicePreference(stringResource(R.string.prefs_ui_orientation), state.orientation) { choices = OSMTracker.Preferences.KEY_UI_ORIENTATION to orientationChoices }
            if (state.canClearOsmToken) ActionPreference(stringResource(R.string.prefs_osm_clear_oauth_data), stringResource(R.string.prefs_osm_clear_oauth_data_summary)) { confirmClear = true }
            FzButton(text = stringResource(R.string.prefs_logout), variant = FzButtonVariant.Danger, onClick = { confirmLogout = true }, modifier = Modifier.fillMaxWidth())
        }
    }

    edit?.let { current -> EditValueDialog(current.second, current.third, numeric = current.first.contains("gps.logging"), onDismiss = { edit = null }) { value ->
        val normalized = if (current.first == OSMTracker.Preferences.KEY_STORAGE_DIR && !value.startsWith('/')) "/$value" else value
        onEvent(PreferencesEvent.SetString(current.first, normalized)); edit = null
    } }
    choices?.let { current -> ChoiceDialog(current.second, onDismiss = { choices = null }) { onEvent(PreferencesEvent.SetString(current.first, it)); choices = null } }
    if (confirmLogout) ConfirmDialog(stringResource(R.string.prefs_logout_dialog_title), stringResource(R.string.prefs_logout_dialog_message), { confirmLogout = false }) { onEvent(PreferencesEvent.Logout) }
    if (confirmClear) ConfirmDialog(stringResource(R.string.prefs_osm_clear_oauth_data), stringResource(R.string.prefs_osm_clear_oauth_data_dialog), { confirmClear = false }) { onEvent(PreferencesEvent.ClearOsmToken); confirmClear = false }
}

@Composable private fun PreferenceSection(title: String) { Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) }

@Composable private fun ActionPreference(title: String, summary: String, onClick: () -> Unit) = FzCard(Modifier.fillMaxWidth().clickable(onClick = onClick)) { Column(Modifier.padding(FaraZamanThemeTokens.spacing.md)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(summary, style = MaterialTheme.typography.bodySmall) } }

@Composable private fun ValuePreference(title: String, value: String, onClick: () -> Unit) = ActionPreference(title, value, onClick)

@Composable private fun ChoicePreference(title: String, value: String, onClick: () -> Unit) = ActionPreference(title, value, onClick)

@Composable private fun SwitchPreference(title: String, summary: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    FzCard(Modifier.fillMaxWidth().clickable { onChecked(!checked) }) { Row(Modifier.fillMaxWidth().padding(FaraZamanThemeTokens.spacing.md), verticalAlignment = Alignment.CenterVertically) { Column(Modifier.weight(1f)) { Text(title, style = MaterialTheme.typography.titleSmall); Text(summary, style = MaterialTheme.typography.bodySmall) }; Switch(checked, onChecked) } }
}

@Composable private fun EditValueDialog(title: String, initial: String, numeric: Boolean, onDismiss: () -> Unit, onSave: (String) -> Unit) {
    var value by remember(initial) { mutableStateOf(initial) }
    AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { FzTextField(value = value, onValueChange = { if (!numeric || it.all(Char::isDigit)) value = it }, label = title) }, confirmButton = { TextButton(enabled = value.isNotBlank(), onClick = { onSave(value) }) { Text("ذخیره") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } })
}

@Composable private fun ChoiceDialog(items: List<Choice>, onDismiss: () -> Unit, onChoose: (String) -> Unit) {
    AlertDialog(onDismissRequest = onDismiss, title = { Text("انتخاب مقدار") }, text = { Column { items.forEach { item -> Row(Modifier.fillMaxWidth().clickable { onChoose(item.value) }.padding(FaraZamanThemeTokens.spacing.sm), verticalAlignment = Alignment.CenterVertically) { RadioButton(false, onClick = { onChoose(item.value) }); Text(item.label) } } } }, confirmButton = {}, dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } })
}

@Composable private fun ConfirmDialog(title: String, message: String, onDismiss: () -> Unit, onConfirm: () -> Unit) = AlertDialog(onDismissRequest = onDismiss, title = { Text(title) }, text = { Text(message) }, confirmButton = { TextButton(onClick = onConfirm) { Text("تأیید") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("لغو") } })

@androidx.compose.ui.tooling.preview.Preview(showBackground = true, locale = "fa")
@Composable private fun PreferencesScreenPreview() { FaraZamanTheme { PreferencesScreen(PreferencesUiState(), {}) } }
