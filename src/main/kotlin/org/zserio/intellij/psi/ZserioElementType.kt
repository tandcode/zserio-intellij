package org.zserio.intellij.psi

import com.intellij.psi.tree.IElementType
import org.jetbrains.annotations.NonNls
import org.zserio.intellij.ZserioLanguage

class ZserioElementType(@NonNls debugName: String) : IElementType(debugName, ZserioLanguage)
