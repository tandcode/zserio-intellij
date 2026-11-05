package org.zserio.intellij.ide

import com.intellij.navigation.ChooseByNameContributorEx
import com.intellij.navigation.GotoClassContributor
import com.intellij.navigation.NavigationItem
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.Processor
import com.intellij.util.indexing.FindSymbolParameters
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.IdFilter
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.psi.ZserioNamedElement

private const val SEPARATOR = "."

/** Looks up declarations by name through a [ZserioNameIndex]. */
abstract class ZserioNameContributor(
    private val indexId: ID<String, Void>,
    private val accepts: (ZserioNamedElement) -> Boolean,
) : ChooseByNameContributorEx, GotoClassContributor, DumbAware {

    override fun processNames(processor: Processor<in String>, scope: GlobalSearchScope, filter: IdFilter?) {
        FileBasedIndex.getInstance().processAllKeys(indexId, processor, scope, filter)
    }

    override fun processElementsWithName(
        name: String,
        processor: Processor<in NavigationItem>,
        parameters: FindSymbolParameters,
    ) {
        val project = parameters.project
        val files = FileBasedIndex.getInstance().getContainingFiles(indexId, name, parameters.searchScope)
        val psiManager = PsiManager.getInstance(project)
        for (virtualFile in files) {
            val file = psiManager.findFile(virtualFile) as? ZserioFile ?: continue
            for (element in PsiTreeUtil.findChildrenOfType(file, ZserioNamedElement::class.java)) {
                val item = element as? NavigationItem ?: continue
                if (element.name == name && accepts(element) && !processor.process(item)) {
                    return
                }
            }
        }
    }

    override fun getQualifiedName(item: NavigationItem): String? {
        val element = item as? ZserioNamedElement ?: return item.name
        val names = generateSequence<com.intellij.psi.PsiElement>(element) { it.parent }
            .takeWhile { it !is ZserioFile }
            .filterIsInstance<ZserioNamedElement>()
            .mapNotNull { it.name }
            .toList()
            .asReversed()
        val packageName = (element.containingFile as? ZserioFile)?.packageName.orEmpty()
        return (listOfNotNull(packageName.ifEmpty { null }) + names).joinToString(SEPARATOR).ifEmpty { null }
    }

    override fun getQualifiedNameSeparator(): String = SEPARATOR
}

/** "Go to Class" (Cmd+O): top-level types. */
class ZserioClassContributor :
    ZserioNameContributor(ZserioTypeNameIndex.NAME, { ZserioTypeNameIndex.isTypeDeclaration(it) })

/** "Go to Symbol" (Cmd+Alt+O): types, constants, fields, functions and enumeration items. */
class ZserioSymbolContributor :
    ZserioNameContributor(ZserioSymbolNameIndex.NAME, { ZserioSymbolNameIndex.isSymbolDeclaration(it) })
