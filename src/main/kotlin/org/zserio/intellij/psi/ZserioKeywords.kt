package org.zserio.intellij.psi

/** Reserved words of the Zserio language (identifiers must not clash with them). */
object ZserioKeywords {
    val ALL: Set<String> = setOf(
        "align",
        "bit",
        "bool",
        "bitmask",
        "bytes",
        "case",
        "choice",
        "const",
        "default",
        "enum",
        "explicit",
        "extend",
        "extern",
        "float16",
        "float32",
        "float64",
        "function",
        "if",
        "implicit",
        "import",
        "instantiate",
        "int",
        "int16",
        "int32",
        "int64",
        "int8",
        "isset",
        "lengthof",
        "numbits",
        "on",
        "optional",
        "package",
        "packed",
        "pubsub",
        "publish",
        "return",
        "rule",
        "rule_group",
        "service",
        "sql",
        "sql_database",
        "sql_table",
        "sql_virtual",
        "sql_without_rowid",
        "string",
        "struct",
        "subscribe",
        "subtype",
        "topic",
        "uint16",
        "uint32",
        "uint64",
        "uint8",
        "union",
        "using",
        "valueof",
        "varint",
        "varint16",
        "varint32",
        "varint64",
        "varsize",
        "varuint",
        "varuint16",
        "varuint32",
        "varuint64",
        "zserio_compatibility_version",
        "true",
        "false"
    )

    /** Keywords which can start a top level declaration. */
    val TOP_LEVEL: List<String> = listOf(
        "const", "rule_group", "subtype", "struct", "choice", "union", "enum", "bitmask",
        "sql_table", "sql_database", "service", "pubsub", "instantiate"
    )

    val BUILTIN_TYPES: List<String> = listOf(
        "bit", "int", "int8", "int16", "int32", "int64", "uint8", "uint16", "uint32", "uint64",
        "varint", "varint16", "varint32", "varint64", "varsize", "varuint", "varuint16", "varuint32", "varuint64",
        "bool", "string", "float16", "float32", "float64", "extern", "bytes"
    )
}
