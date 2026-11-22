package Ir.co.tfs.farazaman.presentation.design

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class DesignSystemArchitectureTest {
    @Test
    fun `design package does not depend on business layers`() {
        val root = File("src/main/java/Ir/co/tfs/farazaman/presentation/design")
        val forbidden = listOf(".activity.", ".supervisor.", ".data.", ".domain.", ".contractor.")
        val violations = root.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            file.readLines().filter { it.startsWith("import Ir.co.tfs.farazaman") }
                .filter { line -> forbidden.any(line::contains) }
                .map { "${file.name}: $it" }
        }.toList()
        assertTrue("Design System has forbidden imports: $violations", violations.isEmpty())
    }
}
