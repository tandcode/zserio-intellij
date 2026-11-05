package org.zserio.intellij

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object ZserioFileType : LanguageFileType(ZserioLanguage) {
    private fun readResolve(): Any = ZserioFileType

    override fun getName(): String = "Zserio"

    override fun getDescription(): String = "Zserio schema file"

    override fun getDefaultExtension(): String = "zs"

    override fun getIcon(): Icon = ZserioIcons.FILE
}
