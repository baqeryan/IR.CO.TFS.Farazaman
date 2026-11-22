package Ir.co.tfs.farazaman.architecture

import java.io.File

internal object ArchitectureTestSupport {
    fun repositoryRoot(): File {
        var current = File(checkNotNull(System.getProperty("user.dir"))).canonicalFile
        repeat(6) {
            if (File(current, "settings.gradle").isFile) return current
            current = current.parentFile ?: return@repeat
        }
        error("Could not locate repository root from ${System.getProperty("user.dir")}")
    }

    fun sourceFiles(root: File): Sequence<File> =
        if (!root.exists()) emptySequence()
        else root.walkTopDown().filter { it.isFile && it.extension in setOf("kt", "java") }
}
