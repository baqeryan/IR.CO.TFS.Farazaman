package Ir.co.tfs.farazaman.presentation.compose

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class ComposeScreenArchitectureTest {
    @Test fun `compose screens do not import infrastructure or direct Material2`() {
        val root = File("src/main/java/Ir/co/tfs/farazaman/presentation/compose")
        val forbidden = listOf("retrofit2.", "SharedPreferences", "android.content.Intent", "android.location.", "android.hardware.", "androidx.compose.material.")
        val violations = root.walkTopDown().filter { it.extension == "kt" }.flatMap { file ->
            file.readLines().filter { line -> forbidden.any(line::contains) }.map { "${file.name}: $it" }
        }.toList()
        assertTrue("Compose architecture violations: $violations", violations.isEmpty())
    }

    @Test fun `production screens do not depend on XML layouts or generated bindings`() {
        val sourceRoot = File("src/main/java")
        val layoutRoot = File("src/main/res/layout")
        val forbidden = listOf("R.layout.", "ViewBinding", "DataBindingUtil", "Binding.inflate")
        val violations = sourceRoot.walkTopDown().filter { it.extension in setOf("kt", "java") }.flatMap { file ->
            file.readLines().filter { line -> forbidden.any(line::contains) }.map { "${file.name}: $it" }
        }.toList()
        val layouts = layoutRoot.listFiles()?.filter { it.extension == "xml" }.orEmpty()
        assertTrue("Production layout XML remains: ${layouts.map(File::getName)}", layouts.isEmpty())
        assertTrue("Production XML/binding references remain: $violations", violations.isEmpty())
    }
}
