package org.zserio.intellij

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import junit.framework.TestCase
import org.zserio.intellij.lexer.ZserioLexerAdapter
import org.zserio.intellij.psi.ZserioTypes

class ZserioLexerTest : TestCase() {
    private fun tokens(text: String): List<Pair<IElementType, String>> {
        val lexer = ZserioLexerAdapter()
        lexer.start(text)
        val result = mutableListOf<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            val type = lexer.tokenType!!
            if (type != TokenType.WHITE_SPACE) {
                result += type to text.substring(lexer.tokenStart, lexer.tokenEnd)
            }
            lexer.advance()
        }
        return result
    }

    private fun types(text: String): List<IElementType> = tokens(text).map { it.first }

    fun testOperators() {
        assertEquals(
            listOf(
                ZserioTypes.DECIMAL_LITERAL, ZserioTypes.LSHIFT, ZserioTypes.DECIMAL_LITERAL,
                ZserioTypes.GT, ZserioTypes.GT, ZserioTypes.GE, ZserioTypes.LE, ZserioTypes.LT,
                ZserioTypes.LOGICAL_AND, ZserioTypes.AND, ZserioTypes.LOGICAL_OR, ZserioTypes.OR,
                ZserioTypes.EQ, ZserioTypes.ASSIGN, ZserioTypes.NE, ZserioTypes.BANG
            ),
            types("1 << 2 > > >= <= < && & || | == = != !")
        )
        assertEquals(listOf(ZserioTypes.GT, ZserioTypes.GT), types(">>"))
    }

    fun testKeywordsAndIdentifiers() {
        assertEquals(
            listOf(ZserioTypes.STRUCTURE, ZserioTypes.IDENTIFIER, ZserioTypes.IDENTIFIER, ZserioTypes.UINT8),
            types("struct structure bits uint8")
        )
        assertEquals(
            listOf(ZserioTypes.DEPRECATED, ZserioTypes.REMOVED, ZserioTypes.INDEX),
            types("@deprecated @removed @index")
        )
        assertEquals(listOf(ZserioTypes.BOOL_LITERAL, ZserioTypes.BOOL_LITERAL, ZserioTypes.IDENTIFIER), types("true false trueish"))
    }

    fun testLiterals() {
        assertEquals(
            listOf(
                ZserioTypes.DECIMAL_LITERAL, ZserioTypes.DECIMAL_LITERAL, ZserioTypes.OCTAL_LITERAL,
                ZserioTypes.HEXADECIMAL_LITERAL, ZserioTypes.BINARY_LITERAL, ZserioTypes.DOUBLE_LITERAL,
                ZserioTypes.DOUBLE_LITERAL, ZserioTypes.FLOAT_LITERAL, ZserioTypes.DOUBLE_LITERAL,
                ZserioTypes.STRING_LITERAL
            ),
            types("0 42 017 0xFf 101b 1.5 2e3 1.5f .5 \"a\\\"b\"")
        )
    }

    fun testComments() {
        assertEquals(
            listOf(
                ZserioTypes.LINE_COMMENT, ZserioTypes.BLOCK_COMMENT, ZserioTypes.BLOCK_COMMENT,
                ZserioTypes.DOC_COMMENT, ZserioTypes.DOC_COMMENT, ZserioTypes.DOC_COMMENT
            ),
            types("// line\n/* block */ /**/ /** doc */ /*! markdown !*/ /***/")
        )
    }

    fun testBrokenInputProducesBadCharactersOnly() {
        assertEquals(listOf(TokenType.BAD_CHARACTER), types("\"unterminated"))
        assertEquals(listOf(TokenType.BAD_CHARACTER), types("#"))
        assertEquals(listOf(ZserioTypes.BLOCK_COMMENT), types("/* unterminated"))
    }
}
