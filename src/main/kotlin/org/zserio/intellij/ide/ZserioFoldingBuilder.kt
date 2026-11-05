package org.zserio.intellij.ide

import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.zserio.intellij.psi.ZserioImportDeclaration
import org.zserio.intellij.psi.ZserioTypes

/** Folds `{ ... }` bodies, multi-line comments and runs of imports. */
class ZserioFoldingBuilder : FoldingBuilderEx(), DumbAware {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        val descriptors = mutableListOf<FoldingDescriptor>()

        fun addRange(node: ASTNode, range: TextRange, placeholder: String, collapsed: Boolean = false) {
            if (range.length > 0 && document.getLineNumber(range.startOffset) != document.getLineNumber(range.endOffset)) {
                descriptors += FoldingDescriptor(node, range, FoldingGroup.newGroup("zserio"), placeholder, collapsed, emptySet())
            }
        }

        // bodies of declarations and functions
        PsiTreeUtil.processElements(root) { element ->
            val open = element.node.findChildByType(ZserioTypes.LBRACE)
            val close = element.node.findChildByType(ZserioTypes.RBRACE)
            if (open != null && close != null && open.startOffset < close.startOffset) {
                addRange(element.node, TextRange(open.startOffset, close.startOffset + 1), "{...}")
            }
            true
        }

        // comments
        for (comment in PsiTreeUtil.findChildrenOfType(root, PsiComment::class.java)) {
            if (comment.node.elementType != ZserioTypes.LINE_COMMENT) {
                val placeholder = if (comment.node.elementType == ZserioTypes.DOC_COMMENT) "/**...*/" else "/*...*/"
                addRange(comment.node, comment.textRange, placeholder)
            }
        }

        // a run of imports
        val imports = PsiTreeUtil.getChildrenOfTypeAsList(root, ZserioImportDeclaration::class.java)
        if (imports.size > 1) {
            val first = imports.first()
            val range = TextRange(first.textRange.startOffset + "import ".length, imports.last().textRange.endOffset)
            addRange(first.node, range, "...", collapsed = true)
        }
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String = "..."

    override fun isCollapsedByDefault(node: ASTNode): Boolean = false
}
