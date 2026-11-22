package Ir.co.tfs.farazaman.presentation.design

import Ir.co.tfs.farazaman.architecture.ArchitectureTestSupport
import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

class TypographyArchitectureTest {
    @Test
    fun `production UI references IRANYekan instead of IranSans`() {
        val root = ArchitectureTestSupport.repositoryRoot()
        val searchRoots = listOf(
            File(root, "core/ui/src/main"),
            File(root, "app/src/main/java"),
            File(root, "app/src/main/res/values"),
        )
        val violations = searchRoots.asSequence().flatMap { it.walkTopDown() }
            .filter { it.isFile && it.extension in setOf("kt", "java", "xml") }
            .filter { "iran_sans" in it.readText() || "IranSans" in it.readText() }
            .map { it.relativeTo(root).path }
            .toList()
        assertTrue("Legacy IranSans references found: $violations", violations.isEmpty())
    }

    @Test
    fun `IRANYekan provides regular medium and bold weights`() {
        val fontRoot = File(ArchitectureTestSupport.repositoryRoot(), "core/ui/src/main/res/font")
        listOf("iran_yekan_regular.ttf", "iran_yekan_medium.ttf", "iran_yekan_bold.ttf").forEach { name ->
            assertTrue("Missing font weight: $name", File(fontRoot, name).length() > 0L)
        }
    }

    @Test
    fun `history date model receives Persian weekday labels`() {
        val source = File(
            ArchitectureTestSupport.repositoryRoot(),
            "app/src/main/java/Ir/co/tfs/farazaman/activity/NewSupervisorActivity.kt",
        ).readText()
        assertTrue("History weekday labels are not populated", "weekdays[it.dayOfWeek().coerceIn(0,6)]" in source)
    }
}
