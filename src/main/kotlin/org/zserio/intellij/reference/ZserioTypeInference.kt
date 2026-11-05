package org.zserio.intellij.reference

import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.zserio.intellij.psi.ZserioArrayExpression
import org.zserio.intellij.psi.ZserioBitmaskDeclaration
import org.zserio.intellij.psi.ZserioBitmaskValue
import org.zserio.intellij.psi.ZserioChoiceDeclaration
import org.zserio.intellij.psi.ZserioConstDefinition
import org.zserio.intellij.psi.ZserioDotExpression
import org.zserio.intellij.psi.ZserioEnumDeclaration
import org.zserio.intellij.psi.ZserioEnumItem
import org.zserio.intellij.psi.ZserioExpression
import org.zserio.intellij.psi.ZserioFieldTypeId
import org.zserio.intellij.psi.ZserioFunctionCallExpression
import org.zserio.intellij.psi.ZserioFunctionDefinition
import org.zserio.intellij.psi.ZserioIdentifierExpression
import org.zserio.intellij.psi.ZserioInstantiateDeclaration
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.ZserioParameterDefinition
import org.zserio.intellij.psi.ZserioParenthesizedExpression
import org.zserio.intellij.psi.ZserioStructDeclaration
import org.zserio.intellij.psi.ZserioSubtypeDeclaration
import org.zserio.intellij.psi.ZserioTypeReference
import org.zserio.intellij.psi.ZserioUnionDeclaration

/**
 * Minimal static "type" inference which is just enough to resolve `a.b.c`, `Enum.ITEM` and the values of
 * `case` labels: an expression is resolved to the element it denotes and that element to the compound / enum
 * declaration whose members can be accessed through it.
 */
object ZserioTypeInference {
    private const val MAX_DEPTH = 16

    /** The declaration a (simple, dotted, indexed or called) expression refers to, if any. */
    fun resolveExpression(expression: ZserioExpression?): PsiElement? = when (expression) {
        is ZserioIdentifierExpression -> ZserioResolver.resolve(expression.id)
        is ZserioDotExpression -> ZserioResolver.resolve(expression.id)
        is ZserioParenthesizedExpression -> resolveExpression(expression.expression)
        is ZserioArrayExpression -> resolveExpression(expression.expressionList.firstOrNull())
        is ZserioFunctionCallExpression -> resolveExpression(expression.expression)
        else -> null
    }

    /**
     * The structure, choice, union, enumeration or bitmask whose members are reachable through [target], which is
     * either such a declaration itself or something typed by one (field, parameter, function, constant, subtype).
     */
    fun containerOf(target: PsiElement?, depth: Int = 0): ZserioNamedElement? {
        if (target == null || depth > MAX_DEPTH) {
            return null
        }
        return when (target) {
            is ZserioStructDeclaration, is ZserioChoiceDeclaration, is ZserioUnionDeclaration,
            is ZserioEnumDeclaration, is ZserioBitmaskDeclaration -> target
            is ZserioSubtypeDeclaration -> containerOf(resolveTypeReference(target.typeReference), depth + 1)
            is ZserioInstantiateDeclaration -> containerOf(resolveTypeReference(target.typeReference), depth + 1)
            is ZserioFieldTypeId ->
                containerOf(resolveTypeReference(target.typeInstantiation.typeReference), depth + 1)
            is ZserioConstDefinition ->
                containerOf(resolveTypeReference(target.typeInstantiation?.typeReference), depth + 1)
            is ZserioParameterDefinition -> containerOf(resolveTypeReference(target.typeReference), depth + 1)
            is ZserioFunctionDefinition -> containerOf(resolveTypeReference(target.typeReference), depth + 1)
            is ZserioEnumItem, is ZserioBitmaskValue ->
                PsiTreeUtil.getParentOfType(target, ZserioEnumDeclaration::class.java, ZserioBitmaskDeclaration::class.java)
            else -> null
        }
    }

    fun resolveTypeReference(reference: ZserioTypeReference?): PsiElement? =
        reference?.qualifiedName?.idList?.lastOrNull()?.let { ZserioResolver.resolve(it) }

    /** `a.b.c` if the expression consists only of dotted identifiers (a possible package name), otherwise null. */
    fun packagePath(expression: ZserioExpression?): String? = when (expression) {
        is ZserioIdentifierExpression -> expression.id.text
        is ZserioDotExpression -> packagePath(expression.expression)?.let { "$it.${expression.id.text}" }
        else -> null
    }
}
