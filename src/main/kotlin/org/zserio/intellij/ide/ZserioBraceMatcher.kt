package org.zserio.intellij.ide

import com.intellij.lang.BracePair
import com.intellij.lang.PairedBraceMatcher
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import org.zserio.intellij.psi.ZserioTypes

class ZserioBraceMatcher : PairedBraceMatcher {
    override fun getPairs(): Array<BracePair> = PAIRS

    override fun isPairedBracesAllowedBeforeType(lbraceType: IElementType, contextType: IElementType?): Boolean = true

    override fun getCodeConstructStart(file: PsiFile?, openingBraceOffset: Int): Int = openingBraceOffset

    companion object {
        private val PAIRS = arrayOf(
            BracePair(ZserioTypes.LBRACE, ZserioTypes.RBRACE, true),
            BracePair(ZserioTypes.LPAREN, ZserioTypes.RPAREN, false),
            BracePair(ZserioTypes.LBRACKET, ZserioTypes.RBRACKET, false),
        )
    }
}
