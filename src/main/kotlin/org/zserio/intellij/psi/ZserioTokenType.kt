package org.zserio.intellij.psi

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.zserio.intellij.ZserioLanguage

class ZserioTokenType(@NonNls debugName: String) : IElementType(debugName, ZserioLanguage) {
    // The debug name of a token is its text; parse error messages use it ("';' expected").
    override fun toString(): String = super.toString()
}
