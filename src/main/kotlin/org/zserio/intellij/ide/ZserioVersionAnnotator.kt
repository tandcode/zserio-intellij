package org.zserio.intellij.ide

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.psi.ZserioCompatibilityVersionDirective
import org.zserio.intellij.version.ZserioSupport
import org.zserio.intellij.version.ZserioVersion

/**
 * Version checks driven by the `zserio_compatibility_version("x.y.z");` directive of a file. Nothing is reported for
 * files without the directive: the plugin then accepts the syntax of every supported version, and the compiler
 * (when configured) tells what its own version does not understand.
 */
class ZserioVersionAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element is ZserioCompatibilityVersionDirective) {
            checkDeclaredVersion(element, holder)
            return
        }
        if (element.firstChild != null) {
            return
        }
        val feature = ZserioSupport.featureOf(element.node.elementType, element.text) ?: return
        val declared = declaredVersion(element) ?: return
        if (declared < feature.since) {
            holder.newAnnotation(
                HighlightSeverity.WARNING,
                "${feature.description.replaceFirstChar { it.uppercase() }} requires Zserio ${feature.since.toShortString()}, " +
                    "but this file declares compatibility version ${declared.toShortString()}"
            ).range(element).create()
        }
    }

    private fun checkDeclaredVersion(directive: ZserioCompatibilityVersionDirective, holder: AnnotationHolder) {
        val literal = directive.stringLiteral ?: return
        val version = parseLiteral(literal.text)
        val message = when {
            version == null -> "Not a Zserio version number, expected a version such as 2.9.0"
            !ZserioSupport.isSupported(version) ->
                "Zserio ${version.toShortString()} is outside of the versions supported by the plugin " +
                    "(${ZserioSupport.RANGE_TEXT}), the syntax may not be recognized correctly"
            else -> return
        }
        holder.newAnnotation(HighlightSeverity.WARNING, message).range(literal).create()
    }

    private fun declaredVersion(element: PsiElement): ZserioVersion? {
        val file = element.containingFile as? ZserioFile ?: return null
        val directive = PsiTreeUtil.getChildOfType(file, ZserioCompatibilityVersionDirective::class.java) ?: return null
        return parseLiteral(directive.stringLiteral?.text ?: return null)
    }

    private fun parseLiteral(literal: String): ZserioVersion? = ZserioVersion.parse(literal.removeSurrounding("\""))
}
