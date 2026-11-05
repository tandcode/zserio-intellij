package org.zserio.intellij.ide

import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.indexing.DataIndexer
import com.intellij.util.indexing.DefaultFileTypeSpecificInputFilter
import com.intellij.util.indexing.FileBasedIndex
import com.intellij.util.indexing.FileContent
import com.intellij.util.indexing.ID
import com.intellij.util.indexing.ScalarIndexExtension
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.ZserioFileType
import org.zserio.intellij.psi.ZserioKind
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.kind

/** Maps names of declared elements to the Zserio files which declare them; feeds "Go to Class" and "Go to Symbol". */
abstract class ZserioNameIndex : ScalarIndexExtension<String>() {
    /** True if an element with this name has to be stored in the index. */
    protected abstract fun accepts(element: ZserioNamedElement): Boolean

    override fun getIndexer(): DataIndexer<String, Void, FileContent> = DataIndexer { content ->
        val result = HashMap<String, Void?>()
        val file = content.psiFile
        if (file is ZserioFile) {
            for (element in PsiTreeUtil.findChildrenOfType(file, ZserioNamedElement::class.java)) {
                val name = element.name
                if (!name.isNullOrEmpty() && accepts(element)) {
                    result[name] = null
                }
            }
        }
        result
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> = EnumeratorStringDescriptor.INSTANCE

    override fun getVersion(): Int = 1

    override fun getInputFilter(): FileBasedIndex.InputFilter = DefaultFileTypeSpecificInputFilter(ZserioFileType)

    override fun dependsOnFileContent(): Boolean = true
}

/** Top-level types (structures, enumerations, subtypes, ...). */
class ZserioTypeNameIndex : ZserioNameIndex() {
    override fun getName(): ID<String, Void> = NAME

    override fun accepts(element: ZserioNamedElement): Boolean = isTypeDeclaration(element)

    companion object {
        val NAME: ID<String, Void> = ID.create("org.zserio.intellij.typeNames")

        fun isTypeDeclaration(element: ZserioNamedElement): Boolean =
            element.parent is ZserioFile && element.kind.isType
    }
}

/** Everything which has a name except parameters: types, constants, fields, functions, enumeration items, ... */
class ZserioSymbolNameIndex : ZserioNameIndex() {
    override fun getName(): ID<String, Void> = NAME

    override fun accepts(element: ZserioNamedElement): Boolean = isSymbolDeclaration(element)

    companion object {
        val NAME: ID<String, Void> = ID.create("org.zserio.intellij.symbolNames")

        fun isSymbolDeclaration(element: ZserioNamedElement): Boolean = when (element.kind) {
            ZserioKind.TEMPLATE_PARAMETER, ZserioKind.PARAMETER, ZserioKind.UNKNOWN -> false
            else -> true
        }
    }
}
