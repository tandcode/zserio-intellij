package org.zserio.intellij.psi

import com.intellij.icons.AllIcons
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import javax.swing.Icon

/** What a named element declares; used for icons, find usages and semantic highlighting. */
enum class ZserioKind(val description: String, val icon: Icon) {
    STRUCT("structure", AllIcons.Nodes.Class),
    CHOICE("choice", AllIcons.Nodes.Interface),
    UNION("union", AllIcons.Nodes.AbstractClass),
    ENUM("enumeration", AllIcons.Nodes.Enum),
    BITMASK("bitmask", AllIcons.Nodes.Enum),
    SUBTYPE("subtype", AllIcons.Nodes.Class),
    INSTANTIATION("template instantiation", AllIcons.Nodes.Class),
    SQL_TABLE("SQL table", AllIcons.Nodes.Class),
    SQL_DATABASE("SQL database", AllIcons.Nodes.Class),
    SERVICE("service", AllIcons.Nodes.Interface),
    PUBSUB("pubsub", AllIcons.Nodes.Interface),
    TEMPLATE_PARAMETER("template parameter", AllIcons.Nodes.Parameter),
    CONSTANT("constant", AllIcons.Nodes.Constant),
    RULE_GROUP("rule group", AllIcons.Nodes.Package),
    FIELD("field", AllIcons.Nodes.Field),
    PARAMETER("parameter", AllIcons.Nodes.Parameter),
    FUNCTION("function", AllIcons.Nodes.Method),
    ENUM_ITEM("enumeration item", AllIcons.Nodes.Field),
    BITMASK_VALUE("bitmask value", AllIcons.Nodes.Field),
    COLUMN("column", AllIcons.Nodes.Field),
    TABLE("table", AllIcons.Nodes.Field),
    METHOD("method", AllIcons.Nodes.Method),
    MESSAGE("message", AllIcons.Nodes.Field),
    UNKNOWN("element", AllIcons.Nodes.Unknown);

    /** True for kinds which can be used wherever a type is expected. */
    val isType: Boolean
        get() = when (this) {
            STRUCT, CHOICE, UNION, ENUM, BITMASK, SUBTYPE, INSTANTIATION, SQL_TABLE, SQL_DATABASE,
            SERVICE, PUBSUB, TEMPLATE_PARAMETER -> true
            else -> false
        }
}

val ZserioNamedElement.kind: ZserioKind
    get() = when (this) {
        is ZserioStructDeclaration -> ZserioKind.STRUCT
        is ZserioChoiceDeclaration -> ZserioKind.CHOICE
        is ZserioUnionDeclaration -> ZserioKind.UNION
        is ZserioEnumDeclaration -> ZserioKind.ENUM
        is ZserioBitmaskDeclaration -> ZserioKind.BITMASK
        is ZserioSubtypeDeclaration -> ZserioKind.SUBTYPE
        is ZserioInstantiateDeclaration -> ZserioKind.INSTANTIATION
        is ZserioSqlTableDeclaration -> ZserioKind.SQL_TABLE
        is ZserioSqlDatabaseDeclaration -> ZserioKind.SQL_DATABASE
        is ZserioServiceDefinition -> ZserioKind.SERVICE
        is ZserioPubsubDefinition -> ZserioKind.PUBSUB
        is ZserioTemplateParameter -> ZserioKind.TEMPLATE_PARAMETER
        is ZserioConstDefinition -> ZserioKind.CONSTANT
        is ZserioRuleGroupDefinition -> ZserioKind.RULE_GROUP
        is ZserioFieldTypeId -> ZserioKind.FIELD
        is ZserioParameterDefinition -> ZserioKind.PARAMETER
        is ZserioFunctionDefinition -> ZserioKind.FUNCTION
        is ZserioEnumItem -> ZserioKind.ENUM_ITEM
        is ZserioBitmaskValue -> ZserioKind.BITMASK_VALUE
        is ZserioSqlTableFieldDefinition -> ZserioKind.COLUMN
        is ZserioSqlDatabaseFieldDefinition -> ZserioKind.TABLE
        is ZserioServiceMethodDefinition -> ZserioKind.METHOD
        is ZserioPubsubMessageDefinition -> ZserioKind.MESSAGE
        else -> ZserioKind.UNKNOWN
    }

/** Members which can be accessed through a type or a value of a type. */
object ZserioMembers {
    fun fields(compound: PsiElement): Collection<ZserioFieldTypeId> =
        PsiTreeUtil.findChildrenOfType(compound, ZserioFieldTypeId::class.java)

    fun parameters(compound: PsiElement): Collection<ZserioParameterDefinition> =
        PsiTreeUtil.findChildrenOfType(compound, ZserioParameterDefinition::class.java)

    fun functions(compound: PsiElement): Collection<ZserioFunctionDefinition> =
        PsiTreeUtil.findChildrenOfType(compound, ZserioFunctionDefinition::class.java)

    fun templateParameters(declaration: PsiElement): Collection<ZserioTemplateParameter> =
        PsiTreeUtil.findChildrenOfType(declaration, ZserioTemplateParameter::class.java)

    fun enumItems(enumeration: ZserioEnumDeclaration): Collection<ZserioEnumItem> =
        PsiTreeUtil.findChildrenOfType(enumeration, ZserioEnumItem::class.java)

    fun bitmaskValues(bitmask: ZserioBitmaskDeclaration): Collection<ZserioBitmaskValue> =
        PsiTreeUtil.findChildrenOfType(bitmask, ZserioBitmaskValue::class.java)

    /** Members visible from the outside, e.g. in `value.member` or `Enum.ITEM`. */
    fun accessible(container: PsiElement): List<ZserioNamedElement> = when (container) {
        is ZserioStructDeclaration, is ZserioChoiceDeclaration, is ZserioUnionDeclaration ->
            fields(container) + functions(container)
        is ZserioEnumDeclaration -> enumItems(container).toList()
        is ZserioBitmaskDeclaration -> bitmaskValues(container).toList()
        else -> emptyList()
    }
}
