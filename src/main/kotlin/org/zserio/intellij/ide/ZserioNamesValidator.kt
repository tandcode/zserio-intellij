package org.zserio.intellij.ide

import com.intellij.lang.refactoring.NamesValidator
import com.intellij.openapi.project.Project
import org.zserio.intellij.psi.ZserioKeywords

class ZserioNamesValidator : NamesValidator {
    override fun isKeyword(name: String, project: Project?): Boolean = name in ZserioKeywords.ALL

    override fun isIdentifier(name: String, project: Project?): Boolean = IDENTIFIER.matches(name) && !isKeyword(name, project)

    companion object {
        private val IDENTIFIER = Regex("[a-zA-Z_][a-zA-Z_0-9]*")
    }
}
