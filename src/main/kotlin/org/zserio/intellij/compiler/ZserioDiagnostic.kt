package org.zserio.intellij.compiler

enum class ZserioSeverity { ERROR, WARNING, INFO }

/** A message of the Zserio compiler. [line] and [column] are 1-based. */
data class ZserioDiagnostic(
    val severity: ZserioSeverity,
    val path: String,
    val line: Int,
    val column: Int,
    val message: String
)

object ZserioCompilerOutput {
    // "[ERROR] path/to/file.zs:12:5: message"; the path may contain colons (Windows drive letters)
    private val DIAGNOSTIC = Regex("""^\[(ERROR|WARNING|INFO)] (.+?):(\d+):(\d+): (.*)$""")

    fun parse(output: String): List<ZserioDiagnostic> = output.lineSequence().mapNotNull { line ->
        DIAGNOSTIC.matchEntire(line.trimEnd())?.let { match ->
            val (severity, path, lineNumber, column, message) = match.destructured
            ZserioDiagnostic(ZserioSeverity.valueOf(severity), path, lineNumber.toInt(), column.toInt(), message)
        }
    }.toList()
}
