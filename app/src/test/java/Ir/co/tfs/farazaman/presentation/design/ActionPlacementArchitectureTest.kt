package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.architecture.ArchitectureTestSupport
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ActionPlacementArchitectureTest {
    private val migratedScreens = setOf(
        "TrackMapScreen.kt",
        "ViolationMapScreen.kt",
        "RoleSelectionScreen.kt",
        "SupervisorHomeScreen.kt",
        "SupervisorTodayScreen.kt",
        "SupervisorHistoryScreen.kt",
        "SupervisorWorkAreaScreen.kt",
    )

    @Test
    fun `migrated screens use semantic Fz actions instead of raw material buttons`() {
        val composeRoot = File(
            ArchitectureTestSupport.repositoryRoot(),
            "app/src/main/java/Ir/co/tfs/farazaman/presentation/compose",
        )
        val forbiddenImports = listOf(
            "androidx.compose.material3.Button",
            "androidx.compose.material3.OutlinedButton",
            "androidx.compose.material3.TextButton",
            "androidx.compose.material3.IconButton",
        )
        val violations = composeRoot.walkTopDown()
            .filter { it.name in migratedScreens }
            .flatMap { file ->
                file.readLines().filter { line -> forbiddenImports.any(line::contains) }
                    .map { "${file.name}: $it" }
            }.toList()

        assertTrue("Raw Material actions found: $violations", violations.isEmpty())
    }

    @Test
    fun `map screens use shared navigation controls and bottom action bar`() {
        val root = File(
            ArchitectureTestSupport.repositoryRoot(),
            "app/src/main/java/Ir/co/tfs/farazaman/presentation/compose",
        )
        val violations = listOf("TrackMapScreen.kt", "ViolationMapScreen.kt").flatMap { name ->
            val source = File(root, name).readText()
            buildList {
                if ("FzBackButton(" !in source) add("$name: missing FzBackButton")
                if ("FzMapControls(" !in source) add("$name: missing FzMapControls")
                if ("FzBottomActionBar(" !in source) add("$name: missing FzBottomActionBar")
                if (Regex("FzButton\\(\\s*\"بازگشت\"").containsMatchIn(source)) add("$name: textual back action")
            }
        }

        assertTrue("Action placement drift found: $violations", violations.isEmpty())
    }

    @Test
    fun `track layer panel does not participate in map controls layout`() {
        val source = File(
            ArchitectureTestSupport.repositoryRoot(),
            "app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/TrackMapScreen.kt",
        ).readText()
        assertTrue(
            "Layer panel must be anchored by the shared map control popup",
            "layersExpanded = state.layersExpanded && state.hasLayers" in source && "layerContent = layers" in source,
        )
        assertTrue("Map controls must keep a fixed CenterEnd anchor", "modifier = Modifier.align(Alignment.CenterEnd)" in source)
    }

    @Test
    fun `supervisor chrome uses icon actions and the history page template`() {
        val root = File(
            ArchitectureTestSupport.repositoryRoot(),
            "app/src/main/java/Ir/co/tfs/farazaman/presentation/compose",
        )
        val screenNames = listOf(
            "SupervisorHomeScreen.kt",
            "SupervisorTodayScreen.kt",
            "SupervisorHistoryScreen.kt",
        )
        val violations = screenNames.flatMap { name ->
            val source = File(root, name).readText()
            buildList {
                if ("SupervisorNotificationAction(" !in source) add("$name: missing notification icon")
                if ("SupervisorProfileAction(" !in source) add("$name: missing profile icon")
                if (Regex("FzButton\\(\\s*\"(اعلان‌ها|پروفایل)\"").containsMatchIn(source)) {
                    add("$name: textual header action")
                }
                if (name == "SupervisorHistoryScreen.kt" && "FzPageTemplate(" !in source) {
                    add("$name: missing shared page template")
                }
            }
        }
        assertTrue("Supervisor chrome drift found: $violations", violations.isEmpty())
    }

    @Test
    fun `active visit gesture has a topmost touch target`() {
        val source = File(
            ArchitectureTestSupport.repositoryRoot(),
            "app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorHomeScreen.kt",
        ).readText()
        assertTrue(
            "The visit button must overlay its gesture target so Button does not consume stop taps",
            "Modifier.matchParentSize().pointerInput(tracking)" in source,
        )
    }

    @Test
    fun `supervisor navigation shows labels only for the active icon`() {
        val source = File(
            ArchitectureTestSupport.repositoryRoot(),
            "app/src/main/java/Ir/co/tfs/farazaman/presentation/compose/SupervisorScaffold.kt",
        ).readText()
        assertTrue("Selected navigation item must show its label", "if (selected)" in source && "text = label" in source)
        assertTrue("Inactive navigation item must remain icon-only", "FzIconButton(" in source && "contentDescription = label" in source)
        assertTrue(
            "Every supervisor destination must provide an icon",
            listOf("ic_supervisor_home", "ic_supervisor_today", "ic_supervisor_history").all(source::contains),
        )
    }
}
