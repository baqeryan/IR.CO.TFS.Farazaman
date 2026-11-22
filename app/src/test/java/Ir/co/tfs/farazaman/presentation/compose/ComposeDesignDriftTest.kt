package Ir.co.tfs.farazaman.presentation.compose

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeDesignDriftTest {
    @Test fun `migrated compose package has no obvious reusable styling literals`() {
        val root = File("src/main/java/Ir/co/tfs/farazaman/presentation/compose")
        val allowlist = setOf("FaraZamanTheme.kt", "RoleSelectionScreen.kt", "AppUpdateUi.kt", "SplashScreen.kt")
        val violations = root.walkTopDown().filter { it.extension == "kt" && it.name !in allowlist }.flatMap { file ->
            file.readLines().filter { it.contains("Color(0x") || it.matches(Regex(".*\\b\\d+\\.(dp|sp).*")) }.map { "${file.name}: $it" }
        }.toList()
        assertTrue("Design drift detected: $violations", violations.isEmpty())
    }
}
