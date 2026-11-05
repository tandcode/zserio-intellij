package org.zserio.intellij.psi

import com.intellij.psi.tree.TokenSet

object ZserioTokenSets {
    val COMMENTS: TokenSet = TokenSet.create(ZserioTypes.LINE_COMMENT, ZserioTypes.BLOCK_COMMENT, ZserioTypes.DOC_COMMENT)

    val STRINGS: TokenSet = TokenSet.create(ZserioTypes.STRING_LITERAL)

    val NUMBERS: TokenSet = TokenSet.create(
        ZserioTypes.BINARY_LITERAL,
        ZserioTypes.OCTAL_LITERAL,
        ZserioTypes.DECIMAL_LITERAL,
        ZserioTypes.HEXADECIMAL_LITERAL,
        ZserioTypes.FLOAT_LITERAL,
        ZserioTypes.DOUBLE_LITERAL
    )

    /** Tokens of the built-in types (uint8, string, bit, ...). */
    val BUILTIN_TYPES: TokenSet = TokenSet.create(
        ZserioTypes.BIT_FIELD,
        ZserioTypes.INT_FIELD,
        ZserioTypes.INT8,
        ZserioTypes.INT16,
        ZserioTypes.INT32,
        ZserioTypes.INT64,
        ZserioTypes.UINT8,
        ZserioTypes.UINT16,
        ZserioTypes.UINT32,
        ZserioTypes.UINT64,
        ZserioTypes.VARINT,
        ZserioTypes.VARINT16,
        ZserioTypes.VARINT32,
        ZserioTypes.VARINT64,
        ZserioTypes.VARSIZE,
        ZserioTypes.VARUINT,
        ZserioTypes.VARUINT16,
        ZserioTypes.VARUINT32,
        ZserioTypes.VARUINT64,
        ZserioTypes.BOOL,
        ZserioTypes.STRING,
        ZserioTypes.FLOAT16,
        ZserioTypes.FLOAT32,
        ZserioTypes.FLOAT64,
        ZserioTypes.EXTERN,
        ZserioTypes.BYTES
    )

    /** Declaration, member and expression keywords (everything except built-in types). */
    val KEYWORDS: TokenSet = TokenSet.create(
        ZserioTypes.BOOL_LITERAL,
        ZserioTypes.ALIGN,
        ZserioTypes.BITMASK,
        ZserioTypes.CASE,
        ZserioTypes.CHOICE,
        ZserioTypes.CONST,
        ZserioTypes.DEFAULT,
        ZserioTypes.ENUM,
        ZserioTypes.EXPLICIT,
        ZserioTypes.EXTEND,
        ZserioTypes.FUNCTION,
        ZserioTypes.IF,
        ZserioTypes.IMPLICIT,
        ZserioTypes.IMPORT,
        ZserioTypes.INSTANTIATE,
        ZserioTypes.ISSET,
        ZserioTypes.LENGTHOF,
        ZserioTypes.NUMBITS,
        ZserioTypes.ON,
        ZserioTypes.OPTIONAL,
        ZserioTypes.PACKAGE,
        ZserioTypes.PACKED,
        ZserioTypes.PUBSUB,
        ZserioTypes.PUBLISH,
        ZserioTypes.RETURN,
        ZserioTypes.RULE,
        ZserioTypes.RULE_GROUP,
        ZserioTypes.SERVICE,
        ZserioTypes.SQL,
        ZserioTypes.SQL_DATABASE,
        ZserioTypes.SQL_TABLE,
        ZserioTypes.SQL_VIRTUAL,
        ZserioTypes.SQL_WITHOUT_ROWID,
        ZserioTypes.STRUCTURE,
        ZserioTypes.SUBSCRIBE,
        ZserioTypes.SUBTYPE,
        ZserioTypes.TOPIC,
        ZserioTypes.UNION,
        ZserioTypes.USING,
        ZserioTypes.VALUEOF,
        ZserioTypes.COMPAT_VERSION
    )

    /** "@deprecated", "@removed" and "@index". */
    val ANNOTATIONS: TokenSet = TokenSet.create(
        ZserioTypes.DEPRECATED,
        ZserioTypes.REMOVED,
        ZserioTypes.INDEX
    )
}
