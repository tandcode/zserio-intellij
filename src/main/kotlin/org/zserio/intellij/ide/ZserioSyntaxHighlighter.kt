package org.zserio.intellij.ide

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import org.zserio.intellij.lexer.ZserioLexerAdapter
import org.zserio.intellij.psi.ZserioTokenSets
import org.zserio.intellij.psi.ZserioTypes

class ZserioSyntaxHighlighter : SyntaxHighlighterBase() {
    override fun getHighlightingLexer(): Lexer = ZserioLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType?): Array<TextAttributesKey> = pack(keyFor(tokenType))

    private fun keyFor(tokenType: IElementType?): TextAttributesKey? = when {
        tokenType == null -> null
        tokenType == TokenType.BAD_CHARACTER -> BAD_CHARACTER
        tokenType == ZserioTypes.LINE_COMMENT -> LINE_COMMENT
        tokenType == ZserioTypes.BLOCK_COMMENT -> BLOCK_COMMENT
        tokenType == ZserioTypes.DOC_COMMENT -> DOC_COMMENT
        ZserioTokenSets.KEYWORDS.contains(tokenType) -> KEYWORD
        ZserioTokenSets.BUILTIN_TYPES.contains(tokenType) -> BUILTIN_TYPE
        ZserioTokenSets.ANNOTATIONS.contains(tokenType) -> ANNOTATION
        ZserioTokenSets.NUMBERS.contains(tokenType) -> NUMBER
        ZserioTokenSets.STRINGS.contains(tokenType) -> STRING
        tokenType == ZserioTypes.IDENTIFIER -> IDENTIFIER
        tokenType == ZserioTypes.LBRACE || tokenType == ZserioTypes.RBRACE -> BRACES
        tokenType == ZserioTypes.LBRACKET || tokenType == ZserioTypes.RBRACKET -> BRACKETS
        tokenType == ZserioTypes.LPAREN || tokenType == ZserioTypes.RPAREN -> PARENTHESES
        tokenType == ZserioTypes.COMMA -> COMMA
        tokenType == ZserioTypes.SEMICOLON -> SEMICOLON
        tokenType == ZserioTypes.DOT -> DOT
        OPERATORS.contains(tokenType) -> OPERATOR
        else -> null
    }

    companion object {
        val KEYWORD = createTextAttributesKey("ZSERIO_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val BUILTIN_TYPE = createTextAttributesKey("ZSERIO_BUILTIN_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
        val ANNOTATION = createTextAttributesKey("ZSERIO_ANNOTATION", DefaultLanguageHighlighterColors.METADATA)
        val NUMBER = createTextAttributesKey("ZSERIO_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val STRING = createTextAttributesKey("ZSERIO_STRING", DefaultLanguageHighlighterColors.STRING)
        val IDENTIFIER = createTextAttributesKey("ZSERIO_IDENTIFIER", DefaultLanguageHighlighterColors.IDENTIFIER)
        val OPERATOR = createTextAttributesKey("ZSERIO_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val BRACES = createTextAttributesKey("ZSERIO_BRACES", DefaultLanguageHighlighterColors.BRACES)
        val BRACKETS = createTextAttributesKey("ZSERIO_BRACKETS", DefaultLanguageHighlighterColors.BRACKETS)
        val PARENTHESES = createTextAttributesKey("ZSERIO_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val COMMA = createTextAttributesKey("ZSERIO_COMMA", DefaultLanguageHighlighterColors.COMMA)
        val SEMICOLON = createTextAttributesKey("ZSERIO_SEMICOLON", DefaultLanguageHighlighterColors.SEMICOLON)
        val DOT = createTextAttributesKey("ZSERIO_DOT", DefaultLanguageHighlighterColors.DOT)
        val LINE_COMMENT = createTextAttributesKey("ZSERIO_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val BLOCK_COMMENT = createTextAttributesKey("ZSERIO_BLOCK_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val DOC_COMMENT = createTextAttributesKey("ZSERIO_DOC_COMMENT", DefaultLanguageHighlighterColors.DOC_COMMENT)
        val BAD_CHARACTER = createTextAttributesKey("ZSERIO_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        // Semantic highlighting of declarations and references (see ZserioSemanticAnnotator).
        val TYPE_NAME = createTextAttributesKey("ZSERIO_TYPE_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
        val CHOICE_NAME = createTextAttributesKey("ZSERIO_CHOICE_NAME", DefaultLanguageHighlighterColors.INTERFACE_NAME)
        val ENUM_NAME = createTextAttributesKey("ZSERIO_ENUM_NAME", DefaultLanguageHighlighterColors.CLASS_NAME)
        val TEMPLATE_PARAMETER =
            createTextAttributesKey("ZSERIO_TEMPLATE_PARAMETER", DefaultLanguageHighlighterColors.CLASS_NAME)
        val FIELD = createTextAttributesKey("ZSERIO_FIELD", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val PARAMETER = createTextAttributesKey("ZSERIO_PARAMETER", DefaultLanguageHighlighterColors.PARAMETER)
        val FUNCTION = createTextAttributesKey("ZSERIO_FUNCTION", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val CONSTANT = createTextAttributesKey("ZSERIO_CONSTANT", DefaultLanguageHighlighterColors.CONSTANT)
        val ENUM_ITEM = createTextAttributesKey("ZSERIO_ENUM_ITEM", DefaultLanguageHighlighterColors.STATIC_FIELD)

        private val OPERATORS = com.intellij.psi.tree.TokenSet.create(
            ZserioTypes.AND, ZserioTypes.ASSIGN, ZserioTypes.BANG, ZserioTypes.COLON, ZserioTypes.DIVIDE,
            ZserioTypes.EQ, ZserioTypes.GE, ZserioTypes.GT, ZserioTypes.LE, ZserioTypes.LOGICAL_AND,
            ZserioTypes.LOGICAL_OR, ZserioTypes.LSHIFT, ZserioTypes.LT, ZserioTypes.MINUS, ZserioTypes.MODULO,
            ZserioTypes.MULTIPLY, ZserioTypes.NE, ZserioTypes.OR, ZserioTypes.PLUS, ZserioTypes.QUESTIONMARK,
            ZserioTypes.TILDE, ZserioTypes.XOR
        )
    }
}
