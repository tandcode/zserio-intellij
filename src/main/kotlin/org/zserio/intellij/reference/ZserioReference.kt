package org.zserio.intellij.reference

import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceBase
import org.zserio.intellij.psi.ZserioElementFactory
import org.zserio.intellij.psi.ZserioId
import org.zserio.intellij.psi.kind

class ZserioReference(id: ZserioId) : PsiReferenceBase<ZserioId>(id, TextRange(0, id.textLength)) {
    override fun resolve(): PsiElement? = ZserioResolver.resolve(element)

    override fun getVariants(): Array<Any> =
        ZserioResolver.variants(element).map { target ->
            LookupElementBuilder.create(target, target.name.orEmpty())
                .withIcon(target.kind.icon)
                .withTypeText(target.kind.description, true)
        }.toTypedArray()

    override fun handleElementRename(newName: String): PsiElement =
        element.replace(ZserioElementFactory.createId(element.project, newName))
}
