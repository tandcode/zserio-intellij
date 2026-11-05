package org.zserio.intellij.reference

import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.FileTypeIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.intellij.psi.util.PsiModificationTracker
import com.intellij.psi.util.PsiTreeUtil
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.ZserioFileType
import org.zserio.intellij.psi.ZserioBitmaskDeclaration
import org.zserio.intellij.psi.ZserioChoiceCase
import org.zserio.intellij.psi.ZserioChoiceDeclaration
import org.zserio.intellij.psi.ZserioDotExpression
import org.zserio.intellij.psi.ZserioEnumDeclaration
import org.zserio.intellij.psi.ZserioId
import org.zserio.intellij.psi.ZserioIdentifierExpression
import org.zserio.intellij.psi.ZserioImportDeclaration
import org.zserio.intellij.psi.ZserioMembers
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.ZserioQualifiedName
import org.zserio.intellij.psi.ZserioSqlTableDeclaration
import org.zserio.intellij.psi.ZserioStructDeclaration
import org.zserio.intellij.psi.ZserioTypeArgument
import org.zserio.intellij.psi.ZserioTypes
import org.zserio.intellij.psi.ZserioUnionDeclaration
import org.zserio.intellij.psi.kind

/**
 * Name resolution for Zserio.
 *
 * - Types are visible if they are declared in a file with the same package name (the files of a package are not
 *   restricted to a directory), or imported with `import pkg.Type;` / `import pkg.*;`, or written fully qualified.
 * - Identifiers in expressions additionally see the members and parameters of the enclosing compound type, the
 *   items of the enclosing enumeration / bitmask and, in `case` labels, the items of the choice selector's type.
 */
object ZserioResolver {
    fun resolve(id: ZserioId): ZserioNamedElement? {
        val name = id.text
        return candidates(id).firstOrNull { it.name == name }
    }

    fun variants(id: ZserioId): List<ZserioNamedElement> = candidates(id).distinctBy { it.name }.toList()

    /** Everything which could be referenced by [id], ordered by priority (the first element with a name wins). */
    fun candidates(id: ZserioId): Sequence<ZserioNamedElement> = when (val parent = id.parent) {
        is ZserioQualifiedName -> typeCandidates(id, parent)
        is ZserioImportDeclaration -> importCandidates(id, parent)
        is ZserioIdentifierExpression, is ZserioTypeArgument -> expressionCandidates(id)
        is ZserioDotExpression -> dotCandidates(parent)
        else -> emptySequence()
    }

    /** Types which can be written at [context] without qualification. Used for keyword completion. */
    fun typesVisibleFrom(context: PsiElement): Sequence<ZserioNamedElement> {
        val file = context.containingFile as? ZserioFile ?: return emptySequence()
        return (templateParametersInScope(context) + visibleTopLevel(file)).filter { it.kind.isType }
    }

    private fun typeCandidates(id: ZserioId, name: ZserioQualifiedName): Sequence<ZserioNamedElement> {
        val ids = name.idList
        if (ids.lastOrNull() !== id) {
            return emptySequence() // a package segment of a qualified name
        }
        val file = id.containingFile as? ZserioFile ?: return emptySequence()
        val qualifier = ids.dropLast(1)
        return if (qualifier.isEmpty()) {
            (templateParametersInScope(id) + visibleTopLevel(file)).filter { it.kind.isType }
        } else {
            topLevelIn(file.project, qualifier.joinToString(".") { it.text }).filter { it.kind.isType }
        }
    }

    private fun importCandidates(id: ZserioId, import: ZserioImportDeclaration): Sequence<ZserioNamedElement> {
        val ids = import.idList
        val hasWildcard = import.node.findChildByType(ZserioTypes.MULTIPLY) != null
        if (hasWildcard || ids.lastOrNull() !== id) {
            return emptySequence()
        }
        val file = id.containingFile as? ZserioFile ?: return emptySequence()
        return topLevelIn(file.project, ids.dropLast(1).joinToString(".") { it.text })
    }

    private fun expressionCandidates(id: ZserioId): Sequence<ZserioNamedElement> = sequence {
        var element: PsiElement? = id.parent
        while (element != null && element !is ZserioFile) {
            when (element) {
                is ZserioStructDeclaration, is ZserioChoiceDeclaration, is ZserioUnionDeclaration -> {
                    yieldAll(ZserioMembers.parameters(element))
                    yieldAll(ZserioMembers.fields(element))
                    yieldAll(ZserioMembers.functions(element))
                }
                is ZserioEnumDeclaration -> yieldAll(ZserioMembers.enumItems(element))
                is ZserioBitmaskDeclaration -> yieldAll(ZserioMembers.bitmaskValues(element))
            }
            element = element.parent
        }
        yieldAll(caseLabelItems(id))
        (id.containingFile as? ZserioFile)?.let { yieldAll(visibleTopLevel(it)) }
    }

    /** In `case ITEM:` the unqualified items of the selector's enumeration / bitmask are visible. */
    private fun caseLabelItems(id: ZserioId): List<ZserioNamedElement> {
        if (PsiTreeUtil.getParentOfType(id, ZserioChoiceCase::class.java) == null) {
            return emptyList()
        }
        val choice = PsiTreeUtil.getParentOfType(id, ZserioChoiceDeclaration::class.java) ?: return emptyList()
        val selector = ZserioTypeInference.resolveExpression(choice.expression)
        return ZserioTypeInference.containerOf(selector)?.let { ZserioMembers.accessible(it) } ?: emptyList()
    }

    private fun dotCandidates(dot: ZserioDotExpression): Sequence<ZserioNamedElement> = sequence {
        val left = dot.expression
        val container = ZserioTypeInference.containerOf(ZserioTypeInference.resolveExpression(left))
        if (container != null) {
            yieldAll(ZserioMembers.accessible(container))
        } else {
            // not a value or a type, but maybe a (partial) package name: "pkg.sub.Type"
            val path = ZserioTypeInference.packagePath(left)
            if (path != null) {
                yieldAll(topLevelIn(dot.project, path))
            }
        }
    }

    private fun templateParametersInScope(context: PsiElement): Sequence<ZserioNamedElement> = sequence {
        var element: PsiElement? = context
        while (element != null && element !is ZserioFile) {
            if (element is ZserioStructDeclaration || element is ZserioChoiceDeclaration ||
                element is ZserioUnionDeclaration || element is ZserioSqlTableDeclaration
            ) {
                yieldAll(ZserioMembers.templateParameters(element))
            }
            element = element.parent
        }
    }

    /** Top level declarations visible from [file] without qualification. */
    private fun visibleTopLevel(file: ZserioFile): Sequence<ZserioNamedElement> = sequence {
        yieldAll(file.topLevelDeclarations)
        yieldAll(topLevelIn(file.project, file.packageName))
        for (import in file.imports) {
            val ids = import.idList.map { it.text }
            if (ids.isEmpty()) {
                continue
            }
            if (import.node.findChildByType(ZserioTypes.MULTIPLY) != null) {
                yieldAll(topLevelIn(file.project, ids.joinToString(".")))
            } else {
                val name = ids.last()
                yieldAll(topLevelIn(file.project, ids.dropLast(1).joinToString(".")).filter { it.name == name })
            }
        }
    }

    /** Top level declarations of all files of the package [packageName] (empty string is the default package). */
    fun topLevelIn(project: Project, packageName: String): Sequence<ZserioNamedElement> =
        filesByPackage(project)[packageName].orEmpty().asSequence().flatMap { it.topLevelDeclarations }

    private fun filesByPackage(project: Project): Map<String, List<ZserioFile>> {
        if (DumbService.isDumb(project)) {
            return emptyMap()
        }
        return CachedValuesManager.getManager(project).getCachedValue(project) {
            val psiManager = PsiManager.getInstance(project)
            val files = FileTypeIndex.getFiles(ZserioFileType, GlobalSearchScope.allScope(project))
                .mapNotNull { psiManager.findFile(it) as? ZserioFile }
            CachedValueProvider.Result.create(files.groupBy { it.packageName }, PsiModificationTracker.MODIFICATION_COUNT)
        }
    }
}
