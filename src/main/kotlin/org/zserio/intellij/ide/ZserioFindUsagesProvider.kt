package org.zserio.intellij.ide

import com.intellij.lang.cacheBuilder.DefaultWordsScanner
import com.intellij.lang.cacheBuilder.WordsScanner
import com.intellij.lang.findUsages.FindUsagesProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.psi.tree.TokenSet
import org.zserio.intellij.lexer.ZserioLexerAdapter
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.ZserioTokenSets
import org.zserio.intellij.psi.ZserioTypes
import org.zserio.intellij.psi.kind

class ZserioFindUsagesProvider : FindUsagesProvider {
    override fun getWordsScanner(): WordsScanner = DefaultWordsScanner(
        ZserioLexerAdapter(),
        TokenSet.create(ZserioTypes.IDENTIFIER),
        ZserioTokenSets.COMMENTS,
        ZserioTokenSets.STRINGS
    )

    override fun canFindUsagesFor(psiElement: PsiElement): Boolean = psiElement is PsiNamedElement

    override fun getHelpId(psiElement: PsiElement): String? = null

    override fun getType(element: PsiElement): String = (element as? ZserioNamedElement)?.kind?.description ?: ""

    override fun getDescriptiveName(element: PsiElement): String = (element as? PsiNamedElement)?.name.orEmpty()

    override fun getNodeText(element: PsiElement, useFullName: Boolean): String = getDescriptiveName(element)
}
