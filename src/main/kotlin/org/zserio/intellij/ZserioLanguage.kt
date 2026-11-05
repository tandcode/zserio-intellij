package org.zserio.intellij

import com.intellij.lang.Language

object ZserioLanguage : Language("Zserio") {
    private fun readResolve(): Any = ZserioLanguage

    override fun getDisplayName(): String = "Zserio"

    override fun isCaseSensitive(): Boolean = true
}
