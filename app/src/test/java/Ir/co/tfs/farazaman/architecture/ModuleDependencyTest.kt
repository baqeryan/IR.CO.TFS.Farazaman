package Ir.co.tfs.farazaman.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class ModuleDependencyTest {
    @Test
    fun featureModulesDoNotDependOnAppOrOtherFeatures() {
        val root = ArchitectureTestSupport.repositoryRoot()
        val featureBuildFiles = File(root, "feature").listFiles().orEmpty()
            .map { File(it, "build.gradle") }
            .filter(File::isFile)
        val forbiddenProjectDependencies = Regex("project\\(['\"]:(?:app|feature:[^'\"]+)['\"]\\)")

        val violations = featureBuildFiles.flatMap { file ->
            file.readLines().withIndex()
                .filter { (_, line) -> forbiddenProjectDependencies.containsMatchIn(line) }
                .map { (index, line) -> "${file.relativeTo(root)}:${index + 1}: ${line.trim()}" }
        }

        assertTrue("Forbidden module dependencies:\n${violations.joinToString("\n")}", violations.isEmpty())
    }
}
