package org.zserio.intellij

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.intellij.psi.util.PsiTreeUtil
import org.zserio.intellij.psi.ZserioImportDeclaration
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.ZserioPackageNameDefinition

class ZserioFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ZserioLanguage) {
    override fun getFileType(): FileType = ZserioFileType

    override fun toString(): String = "Zserio File"

    /** Dotted name from the `package` statement; empty for the default package. */
    val packageName: String
        get() = PsiTreeUtil.getChildOfType(this, ZserioPackageNameDefinition::class.java)
            ?.idList?.joinToString(".") { it.text } ?: ""

    val imports: List<ZserioImportDeclaration>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, ZserioImportDeclaration::class.java)

    /** Types, constants and rule groups declared at the top level of this file. */
    val topLevelDeclarations: List<ZserioNamedElement>
        get() = PsiTreeUtil.getChildrenOfTypeAsList(this, ZserioNamedElement::class.java)
}
