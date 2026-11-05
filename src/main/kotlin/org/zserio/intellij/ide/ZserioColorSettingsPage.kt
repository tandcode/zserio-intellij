package org.zserio.intellij.ide

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import org.zserio.intellij.ZserioIcons
import javax.swing.Icon

class ZserioColorSettingsPage : ColorSettingsPage {
    override fun getIcon(): Icon = ZserioIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = ZserioSyntaxHighlighter()

    override fun getDemoText(): String = """
        package demo.shop;

        import demo.common.*;

        /** Documentation comment. */
        const uint8 MAX_ITEMS = 0x20;

        enum uint8 Category
        {
            @deprecated BOOKS,
            FOOD = 2,
            TOYS
        };

        // Line comment
        struct Box<T>
        {
            T value;
        };

        struct Order(uint16 version)
        {
            string                  customer;
            Category                category;
            align(8):
            bit<4>                  quantity : quantity > 0;
            Item                    items[];
            optional float32        discount if version >= 2;

            function bool isFood()
            {
                return category == Category.FOOD;
            }
        };
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey>? = null

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "Zserio"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Keyword", ZserioSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Built-in type", ZserioSyntaxHighlighter.BUILTIN_TYPE),
            AttributesDescriptor("Annotation", ZserioSyntaxHighlighter.ANNOTATION),
            AttributesDescriptor("Number", ZserioSyntaxHighlighter.NUMBER),
            AttributesDescriptor("String", ZserioSyntaxHighlighter.STRING),
            AttributesDescriptor("Identifier", ZserioSyntaxHighlighter.IDENTIFIER),
            AttributesDescriptor("Operator", ZserioSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Braces", ZserioSyntaxHighlighter.BRACES),
            AttributesDescriptor("Brackets", ZserioSyntaxHighlighter.BRACKETS),
            AttributesDescriptor("Parentheses", ZserioSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Comma", ZserioSyntaxHighlighter.COMMA),
            AttributesDescriptor("Semicolon", ZserioSyntaxHighlighter.SEMICOLON),
            AttributesDescriptor("Dot", ZserioSyntaxHighlighter.DOT),
            AttributesDescriptor("Comments//Line comment", ZserioSyntaxHighlighter.LINE_COMMENT),
            AttributesDescriptor("Comments//Block comment", ZserioSyntaxHighlighter.BLOCK_COMMENT),
            AttributesDescriptor("Comments//Documentation comment", ZserioSyntaxHighlighter.DOC_COMMENT),
            AttributesDescriptor("Bad character", ZserioSyntaxHighlighter.BAD_CHARACTER),
            AttributesDescriptor("Semantic//Type name", ZserioSyntaxHighlighter.TYPE_NAME),
            AttributesDescriptor("Semantic//Choice, service and pubsub name", ZserioSyntaxHighlighter.CHOICE_NAME),
            AttributesDescriptor("Semantic//Enumeration and bitmask name", ZserioSyntaxHighlighter.ENUM_NAME),
            AttributesDescriptor("Semantic//Template parameter", ZserioSyntaxHighlighter.TEMPLATE_PARAMETER),
            AttributesDescriptor("Semantic//Field", ZserioSyntaxHighlighter.FIELD),
            AttributesDescriptor("Semantic//Parameter", ZserioSyntaxHighlighter.PARAMETER),
            AttributesDescriptor("Semantic//Function", ZserioSyntaxHighlighter.FUNCTION),
            AttributesDescriptor("Semantic//Constant", ZserioSyntaxHighlighter.CONSTANT),
            AttributesDescriptor("Semantic//Enumeration item", ZserioSyntaxHighlighter.ENUM_ITEM),
        )
    }
}
