package Ir.co.tfs.farazaman.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class PresentationBoundaryTest {
    @Test
    fun migratedPresentationDoesNotAccessTransportOrStorageImplementations() {
        val root = ArchitectureTestSupport.repositoryRoot()
        val presentationRoots = File(root, "feature").listFiles().orEmpty().flatMap { feature ->
            listOf(
                File(feature, "src/main/java/Ir/co/tfs/farazaman/feature/${feature.name}/presentation"),
                File(feature, "src/main/kotlin/Ir/co/tfs/farazaman/feature/${feature.name}/presentation"),
            )
        }
        val forbiddenTokens = listOf(
            "retrofit2.",
            "okhttp3.",
            "android.content.SharedPreferences",
            "android.content.ContentResolver",
            "android.database.Cursor",
        )

        val violations = presentationRoots.asSequence()
            .flatMap(ArchitectureTestSupport::sourceFiles)
            .flatMap { file ->
                file.readLines().withIndex().asSequence()
                    .filter { (_, line) -> forbiddenTokens.any(line::contains) }
                    .map { (index, line) -> "${file.relativeTo(root)}:${index + 1}: ${line.trim()}" }
            }
            .toList()

        assertTrue("Presentation boundary violations:\n${violations.joinToString("\n")}", violations.isEmpty())
    }
}
