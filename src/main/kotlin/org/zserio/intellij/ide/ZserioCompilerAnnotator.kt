package org.zserio.intellij.ide

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.ExternalAnnotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiWhiteSpace
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.compiler.ZserioCompiler
import org.zserio.intellij.compiler.ZserioDiagnostic
import org.zserio.intellij.compiler.ZserioSeverity
import org.zserio.intellij.settings.ZserioSettings
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Reports the errors and warnings of the Zserio compiler. Does nothing as long as no compiler is configured in
 * Settings | Tools | Zserio. Files with syntax errors are skipped, the parser already reports those.
 */
class ZserioCompilerAnnotator : ExternalAnnotator<ZserioCompilerAnnotator.Input, List<ZserioDiagnostic>>() {
    class Input(
        val compiler: ZserioCompiler,
        val sourceRoot: Path,
        val relativePath: String,
        /** Text from the editor if it differs from the file on disk, otherwise null. */
        val unsavedText: String?
    )

    override fun collectInformation(file: PsiFile, editor: Editor, hasErrors: Boolean): Input? {
        if (hasErrors || file !is ZserioFile) {
            return null
        }
        val settings = ZserioSettings.getInstance().state
        if (settings.compilerJar.isBlank()) {
            return null
        }
        val virtualFile: VirtualFile = file.virtualFile ?: return null
        if (!virtualFile.isInLocalFileSystem) {
            return null
        }
        val (root, relativePath) = ZserioCompiler.locate(Paths.get(virtualFile.path), file.packageName)
        val unsaved = FileDocumentManager.getInstance().isFileModified(virtualFile)
        val java = settings.javaExecutable.ifBlank { Paths.get(System.getProperty("java.home"), "bin", "java").toString() }
        return Input(
            ZserioCompiler(java, settings.compilerJar, settings.extraArguments),
            root,
            relativePath,
            if (unsaved) file.text else null
        )
    }

    override fun doAnnotate(input: Input?): List<ZserioDiagnostic>? {
        if (input == null) {
            return null
        }
        return try {
            input.compiler.check(input.sourceRoot, input.relativePath, input.unsavedText)
        } catch (e: Exception) {
            null // the compiler cannot be started; the settings page is the place to find out why
        }
    }

    override fun apply(file: PsiFile, problems: List<ZserioDiagnostic>?, holder: AnnotationHolder) {
        val document = file.viewProvider.document ?: return
        for (problem in problems.orEmpty()) {
            val line = problem.line - 1
            if (line < 0 || line >= document.lineCount) {
                continue
            }
            val lineStart = document.getLineStartOffset(line)
            val lineEnd = document.getLineEndOffset(line)
            val offset = (lineStart + problem.column - 1).coerceIn(lineStart, lineEnd)
            val element = file.findElementAt(offset)
            val range = if (element != null && element !is PsiWhiteSpace && element.textRange.startOffset == offset) {
                element.textRange
            } else {
                TextRange(offset, maxOf(offset, lineEnd).coerceAtMost(maxOf(offset + 1, lineEnd)))
            }
            val severity = when (problem.severity) {
                ZserioSeverity.ERROR -> HighlightSeverity.ERROR
                ZserioSeverity.WARNING -> HighlightSeverity.WARNING
                ZserioSeverity.INFO -> HighlightSeverity.WEAK_WARNING
            }
            holder.newAnnotation(severity, problem.message).range(range).create()
        }
    }
}
