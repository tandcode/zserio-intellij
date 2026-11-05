package org.zserio.intellij.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.ZserioLanguage
import org.zserio.intellij.lexer.ZserioLexerAdapter
import org.zserio.intellij.psi.ZserioTokenSets
import org.zserio.intellij.psi.ZserioTypes

class ZserioParserDefinition : ParserDefinition {
    override fun createLexer(project: Project?): Lexer = ZserioLexerAdapter()

    override fun createParser(project: Project?): PsiParser = ZserioParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = ZserioTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = ZserioTokenSets.STRINGS

    override fun createElement(node: ASTNode): PsiElement = ZserioTypes.Factory.createElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = ZserioFile(viewProvider)

    companion object {
        val FILE = IFileElementType(ZserioLanguage)
    }
}
