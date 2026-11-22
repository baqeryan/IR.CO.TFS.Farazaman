package Ir.co.tfs.farazaman.presentation.design

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeDesignDriftTest {
    @Test fun `public shared composables use Fz naming`() {
        val root = File("../core/ui/src/main/java/Ir/co/tfs/farazaman/presentation/design/components")
        val function = Regex("^fun\\s+(?:<[^>]+>\\s+)?(?:[A-Za-z0-9_.<>]+\\.)?([A-Za-z0-9_]+)\\s*\\(")
        val violations = root.walkTopDown()
            .filter { it.extension == "kt" }
            .flatMap { file ->
                val lines = file.readLines()
                lines.mapIndexedNotNull { index, line ->
                    val isComposable = lines.take(index).asReversed().firstOrNull { it.isNotBlank() }?.trim() == "@Composable"
                    if (!isComposable) return@mapIndexedNotNull null
                    function.find(line.trim())?.groupValues?.get(1)
                        ?.takeUnless { it.startsWith("Fz") || it.startsWith("fz") }
                        ?.let { "${file.name}: $it" }
                }
            }.toList()
        assertTrue("Shared component naming drift detected: $violations", violations.isEmpty())
    }
}
