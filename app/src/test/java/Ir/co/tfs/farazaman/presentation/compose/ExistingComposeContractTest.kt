package Ir.co.tfs.farazaman.presentation.compose

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ExistingComposeContractTest {
    @Test fun `existing screens expose state or typed events`() {
        val files = listOf("RoleSelectionScreen.kt", "AppUpdateUi.kt", "SplashScreen.kt").map {
            File("src/main/java/Ir/co/tfs/farazaman/presentation/compose/$it")
        }
        files.forEach { file ->
            val source = file.readText()
            assertTrue("${file.name} must contain a composable", source.contains("@Composable"))
        }
    }
}
