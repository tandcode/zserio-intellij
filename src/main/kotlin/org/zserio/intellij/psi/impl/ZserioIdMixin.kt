package org.zserio.intellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.psi.PsiReference
import org.zserio.intellij.psi.ZserioDotExpression
import org.zserio.intellij.psi.ZserioId
import org.zserio.intellij.psi.ZserioIdentifierExpression
import org.zserio.intellij.psi.ZserioImportDeclaration
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.ZserioQualifiedName
import org.zserio.intellij.psi.ZserioTypeArgument
import org.zserio.intellij.reference.ZserioReference

abstract class ZserioIdMixin(node: ASTNode) : ASTWrapperPsiElement(node) {
    /** Identifiers in declarations have no reference; identifiers in type names and expressions do. */
    override fun getReference(): PsiReference? {
        val parent = parent
        if (parent is ZserioNamedElement && parent.nameIdentifier === this) {
            return null
        }
        return when (parent) {
            is ZserioQualifiedName,
            is ZserioImportDeclaration,
            is ZserioIdentifierExpression,
            is ZserioDotExpression,
            is ZserioTypeArgument -> ZserioReference(this as ZserioId)
            else -> null
        }
    }
}
