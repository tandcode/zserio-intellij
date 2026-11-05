package org.zserio.intellij.ide

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import org.zserio.intellij.psi.ZserioId
import org.zserio.intellij.psi.ZserioKind
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.kind

/** Colors declarations and resolved references by what they declare / refer to. */
class ZserioSemanticAnnotator : Annotator {
    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element !is ZserioId) {
            return
        }
        val parent = element.parent
        val target = if (parent is ZserioNamedElement && parent.nameIdentifier === element) {
            parent
        } else {
            element.reference?.resolve() as? ZserioNamedElement
        }
        val key = target?.let { keyFor(it.kind) } ?: return
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION).range(element).textAttributes(key).create()
    }

    private fun keyFor(kind: ZserioKind): TextAttributesKey? = when (kind) {
        ZserioKind.STRUCT, ZserioKind.UNION, ZserioKind.SUBTYPE, ZserioKind.INSTANTIATION, ZserioKind.SQL_TABLE,
        ZserioKind.SQL_DATABASE -> ZserioSyntaxHighlighter.TYPE_NAME
        ZserioKind.CHOICE, ZserioKind.SERVICE, ZserioKind.PUBSUB -> ZserioSyntaxHighlighter.CHOICE_NAME
        ZserioKind.ENUM, ZserioKind.BITMASK -> ZserioSyntaxHighlighter.ENUM_NAME
        ZserioKind.TEMPLATE_PARAMETER -> ZserioSyntaxHighlighter.TEMPLATE_PARAMETER
        ZserioKind.FIELD, ZserioKind.COLUMN, ZserioKind.TABLE, ZserioKind.MESSAGE -> ZserioSyntaxHighlighter.FIELD
        ZserioKind.PARAMETER -> ZserioSyntaxHighlighter.PARAMETER
        ZserioKind.FUNCTION, ZserioKind.METHOD -> ZserioSyntaxHighlighter.FUNCTION
        ZserioKind.CONSTANT -> ZserioSyntaxHighlighter.CONSTANT
        ZserioKind.ENUM_ITEM, ZserioKind.BITMASK_VALUE -> ZserioSyntaxHighlighter.ENUM_ITEM
        ZserioKind.RULE_GROUP, ZserioKind.UNKNOWN -> null
    }
}
