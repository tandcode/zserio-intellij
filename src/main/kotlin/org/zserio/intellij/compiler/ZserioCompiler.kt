package org.zserio.intellij.compiler

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.execution.util.ExecUtil
import com.intellij.util.execution.ParametersListUtil
import java.nio.file.Files
import java.nio.file.InvalidPathException
import java.nio.file.Path

/** Runs the Zserio compiler (without any generator) to check a schema. */
class ZserioCompiler(
    private val javaExecutable: String,
    private val compilerJar: String,
    private val extraArguments: String = ""
) {
    /**
     * Checks the schema file [relativePath] (relative to [sourceRoot] and using '/') and returns the compiler's
     * messages for that file. If [text] is given, it is used instead of the content of the file on disk.
     */
    fun check(sourceRoot: Path, relativePath: String, text: String?, timeoutMillis: Int = 60_000): List<ZserioDiagnostic> {
        var overlay: Path? = null
        try {
            var effectiveRoot = sourceRoot
            if (text != null) {
                overlay = Files.createTempDirectory("zserio-overlay")
                try {
                    ZserioSourceOverlay.create(sourceRoot, relativePath, text, overlay)
                    effectiveRoot = overlay
                } catch (e: Exception) {
                    // symbolic links are not available: check what is saved on disk
                    ZserioSourceOverlay.delete(overlay)
                    overlay = null
                }
            }
            val commandLine = GeneralCommandLine(javaExecutable, "-jar", compilerJar)
                .withParameters("-src", effectiveRoot.toString())
                .withParameters(ParametersListUtil.parse(extraArguments))
                .withParameters(relativePath)
                .withWorkingDirectory(effectiveRoot)
                .withRedirectErrorStream(true)
            val output = ExecUtil.execAndGetOutput(commandLine, timeoutMillis)
            val expected = effectiveRoot.resolve(relativePath).toAbsolutePath().normalize()
            return ZserioCompilerOutput.parse(output.stdout).filter { refersTo(it.path, effectiveRoot, expected) }
        } finally {
            overlay?.let { ZserioSourceOverlay.delete(it) }
        }
    }

    /** The compiler prints the paths of all files it reads (imports as well), only those of [expected] are wanted. */
    private fun refersTo(diagnosticPath: String, workingDirectory: Path, expected: Path): Boolean = try {
        workingDirectory.resolve(diagnosticPath).toAbsolutePath().normalize() == expected
    } catch (e: InvalidPathException) {
        false
    }

    companion object {
        /**
         * Splits the path of a schema file into its source root and the path relative to it. The location of a file
         * has to follow the package: `package a.b.c;` is expected in `<root>/a/b/c.zs`. Files which do not follow this
         * are checked as if their directory was the source root.
         */
        fun locate(file: Path, packageName: String): Pair<Path, String> {
            val fileName = file.fileName.toString()
            if (packageName.isNotEmpty()) {
                val segments = packageName.split('.')
                val expected = segments.dropLast(1) + (segments.last() + ".zs")
                val parts = (0 until file.nameCount).map { file.getName(it).toString() }
                if (parts.size > expected.size && parts.takeLast(expected.size) == expected) {
                    var root: Path = file
                    repeat(expected.size) { root = root.parent }
                    return root to expected.joinToString("/")
                }
            }
            return file.parent to fileName
        }
    }
}
