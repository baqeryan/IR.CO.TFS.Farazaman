package Ir.co.tfs.farazaman.architecture

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class DomainPurityTest {
    @Test
    fun cleanDomainSourcesDoNotImportPlatformOrTransportTypes() {
        val root = ArchitectureTestSupport.repositoryRoot()
        val roots = buildList {
            add(File(root, "core/common/src/main"))
            add(File(root, "core/model/src/main"))
            File(root, "feature").listFiles().orEmpty().forEach { feature ->
                add(File(feature, "src/main/java/Ir/co/tfs/farazaman/feature/${feature.name}/domain"))
                add(File(feature, "src/main/kotlin/Ir/co/tfs/farazaman/feature/${feature.name}/domain"))
            }
        }
        val forbiddenImports = listOf(
            "import android.",
            "import androidx.",
            "import retrofit2.",
            "import okhttp3.",
            "import com.google.gson.",
        )

        val violations = roots.asSequence()
            .flatMap(ArchitectureTestSupport::sourceFiles)
            .flatMap { file ->
                file.readLines().withIndex().asSequence()
                    .filter { (_, line) -> forbiddenImports.any(line::contains) }
                    .map { (index, line) -> "${file.relativeTo(root)}:${index + 1}: ${line.trim()}" }
            }
            .toList()

        assertTrue("Domain purity violations:\n${violations.joinToString("\n")}", violations.isEmpty())
    }
}
