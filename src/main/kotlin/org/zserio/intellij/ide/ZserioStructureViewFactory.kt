package org.zserio.intellij.ide

import com.intellij.ide.structureView.StructureViewBuilder
import com.intellij.ide.structureView.StructureViewModel
import com.intellij.ide.structureView.StructureViewModelBase
import com.intellij.ide.structureView.StructureViewTreeElement
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder
import com.intellij.ide.util.treeView.smartTree.Sorter
import com.intellij.lang.PsiStructureViewFactory
import com.intellij.openapi.editor.Editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.ide.structureView.impl.common.PsiTreeElementBase
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.psi.ZserioBitmaskDeclaration
import org.zserio.intellij.psi.ZserioChoiceDeclaration
import org.zserio.intellij.psi.ZserioEnumDeclaration
import org.zserio.intellij.psi.ZserioMembers
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.ZserioStructDeclaration
import org.zserio.intellij.psi.ZserioUnionDeclaration

class ZserioStructureViewFactory : PsiStructureViewFactory {
    override fun getStructureViewBuilder(psiFile: PsiFile): StructureViewBuilder? =
        object : TreeBasedStructureViewBuilder() {
            override fun createStructureViewModel(editor: Editor?): StructureViewModel =
                ZserioStructureViewModel(psiFile, editor)
        }
}

private class ZserioStructureViewModel(file: PsiFile, editor: Editor?) :
    StructureViewModelBase(file, editor, ZserioStructureElement(file)), StructureViewModel.ElementInfoProvider {

    init {
        withSorters(Sorter.ALPHA_SORTER)
    }

    override fun isAlwaysShowsPlus(element: StructureViewTreeElement): Boolean = false

    override fun isAlwaysLeaf(element: StructureViewTreeElement): Boolean = element.value !is ZserioFile &&
        childrenOf(element.value as PsiElement).isEmpty()
}

private fun childrenOf(element: PsiElement): List<ZserioNamedElement> = when (element) {
    is ZserioFile -> element.topLevelDeclarations
    is ZserioStructDeclaration, is ZserioChoiceDeclaration, is ZserioUnionDeclaration ->
        ZserioMembers.fields(element).toList() + ZserioMembers.functions(element)
    is ZserioEnumDeclaration -> ZserioMembers.enumItems(element).toList()
    is ZserioBitmaskDeclaration -> ZserioMembers.bitmaskValues(element).toList()
    else -> emptyList()
}

private class ZserioStructureElement(element: PsiElement) : PsiTreeElementBase<PsiElement>(element) {
    override fun getChildrenBase(): Collection<StructureViewTreeElement> =
        element?.let { childrenOf(it).map(::ZserioStructureElement) }.orEmpty()

    override fun getPresentableText(): String? = (element as? ZserioNamedElement)?.name ?: element?.containingFile?.name
}
