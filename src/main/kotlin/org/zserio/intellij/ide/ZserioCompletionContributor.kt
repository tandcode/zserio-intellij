package org.zserio.intellij.ide

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.patterns.PlatformPatterns.psiElement
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.ProcessingContext
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.psi.ZserioBitmaskDeclaration
import org.zserio.intellij.psi.ZserioChoiceDeclaration
import org.zserio.intellij.psi.ZserioDotExpression
import org.zserio.intellij.psi.ZserioEnumDeclaration
import org.zserio.intellij.psi.ZserioId
import org.zserio.intellij.psi.ZserioIdentifierExpression
import org.zserio.intellij.psi.ZserioImportDeclaration
import org.zserio.intellij.psi.ZserioKeywords
import org.zserio.intellij.psi.ZserioPubsubDefinition
import org.zserio.intellij.psi.ZserioSqlTableDeclaration
import org.zserio.intellij.psi.ZserioStructDeclaration
import org.zserio.intellij.psi.ZserioTypeArgument
import org.zserio.intellij.psi.ZserioTypes
import org.zserio.intellij.psi.ZserioUnionDeclaration
import org.zserio.intellij.psi.kind
import org.zserio.intellij.reference.ZserioResolver
import org.zserio.intellij.version.ZserioSupport

/**
 * Keyword completion (references provide their own variants) plus type names where the parser cannot yet tell that
 * a type is being written, e.g. the first word of a field declaration.
 */
class ZserioCompletionContributor : CompletionContributor() {
    init {
        extend(CompletionType.BASIC, psiElement(ZserioTypes.IDENTIFIER), object : CompletionProvider<CompletionParameters>() {
            override fun addCompletions(parameters: CompletionParameters, context: ProcessingContext, result: CompletionResultSet) {
                val position = parameters.position
                val id = position.parent as? ZserioId
                when (id?.parent) {
                    is ZserioIdentifierExpression, is ZserioTypeArgument -> {
                        addKeywords(result, EXPRESSION_KEYWORDS)
                        return
                    }
                    is ZserioDotExpression, is ZserioImportDeclaration -> return
                    else -> Unit
                }
                addKeywords(result, keywordsAt(position))
                if (PsiTreeUtil.getParentOfType(position, ZserioFile::class.java) != null && isInsideBody(position)) {
                    for (type in ZserioResolver.typesVisibleFrom(position).distinctBy { it.name }) {
                        result.addElement(
                            LookupElementBuilder.create(type, type.name.orEmpty())
                                .withIcon(type.kind.icon)
                                .withTypeText(type.kind.description, true)
                        )
                    }
                }
            }
        })
    }

    private fun addKeywords(result: CompletionResultSet, keywords: Collection<String>) {
        for (keyword in keywords) {
            var element = LookupElementBuilder.create(keyword).bold()
            ZserioSupport.sinceOfKeyword(keyword)?.let { element = element.withTailText("  since ${it.toShortString()}", true) }
            result.addElement(element)
        }
    }

    private fun isInsideBody(position: PsiElement): Boolean = enclosingDeclaration(position) != null

    /** The declaration whose `{ ... }` body contains [position], if any. */
    private fun enclosingDeclaration(position: PsiElement): PsiElement? {
        val declaration = PsiTreeUtil.getParentOfType(
            position,
            ZserioStructDeclaration::class.java, ZserioChoiceDeclaration::class.java,
            ZserioUnionDeclaration::class.java, ZserioEnumDeclaration::class.java,
            ZserioBitmaskDeclaration::class.java, ZserioSqlTableDeclaration::class.java,
            ZserioPubsubDefinition::class.java
        ) ?: return null
        val open = declaration.node.findChildByType(ZserioTypes.LBRACE) ?: return null
        return if (position.textOffset > open.startOffset) declaration else null
    }

    private fun keywordsAt(position: PsiElement): Collection<String> = when (enclosingDeclaration(position)) {
        null -> TOP_LEVEL_KEYWORDS
        is ZserioStructDeclaration -> STRUCT_KEYWORDS + ZserioKeywords.BUILTIN_TYPES
        is ZserioChoiceDeclaration -> CHOICE_KEYWORDS + ZserioKeywords.BUILTIN_TYPES
        is ZserioUnionDeclaration -> UNION_KEYWORDS + ZserioKeywords.BUILTIN_TYPES
        is ZserioSqlTableDeclaration -> listOf("sql_virtual", "sql", "sql_without_rowid") + ZserioKeywords.BUILTIN_TYPES
        is ZserioPubsubDefinition -> listOf("publish", "subscribe", "topic")
        else -> emptyList() // enumerations and bitmasks only contain their items
    }

    private companion object {
        val TOP_LEVEL_KEYWORDS = listOf("package", "import") + ZserioKeywords.TOP_LEVEL
        val STRUCT_KEYWORDS = listOf("align", "extend", "optional", "packed", "implicit", "if", "function")
        val CHOICE_KEYWORDS = listOf("case", "default", "packed", "implicit", "function")
        val UNION_KEYWORDS = listOf("packed", "implicit", "function")
        val EXPRESSION_KEYWORDS = listOf("isset", "lengthof", "valueof", "numbits", "true", "false")
    }
}
