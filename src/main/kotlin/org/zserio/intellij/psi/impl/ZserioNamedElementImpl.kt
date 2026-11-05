package org.zserio.intellij.psi.impl

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.navigation.ItemPresentation
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.zserio.intellij.psi.ZserioElementFactory
import org.zserio.intellij.psi.ZserioId
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.kind
import javax.swing.Icon

/**
 * Base class of all PSI elements which declare a name. The declared name is the first [ZserioId] which is a direct
 * child of the element (identifiers nested in type references are references, not declarations).
 */
abstract class ZserioNamedElementImpl(node: ASTNode) : ASTWrapperPsiElement(node), ZserioNamedElement {
    override fun getNameIdentifier(): PsiElement? = PsiTreeUtil.getChildOfType(this, ZserioId::class.java)

    override fun getName(): String? = nameIdentifier?.text

    override fun setName(name: String): PsiElement {
        nameIdentifier?.replace(ZserioElementFactory.createId(project, name))
        return this
    }

    override fun getTextOffset(): Int = nameIdentifier?.textOffset ?: super.getTextOffset()

    override fun getIcon(flags: Int): Icon = kind.icon

    override fun getPresentation(): ItemPresentation = object : ItemPresentation {
        override fun getPresentableText(): String? = name

        override fun getLocationString(): String? = containingFile?.name

        override fun getIcon(unused: Boolean): Icon = kind.icon
    }
}
