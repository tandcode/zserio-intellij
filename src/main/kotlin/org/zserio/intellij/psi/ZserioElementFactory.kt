package org.zserio.intellij.psi

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.util.PsiTreeUtil
import org.zserio.intellij.ZserioFile
import org.zserio.intellij.ZserioFileType

object ZserioElementFactory {
    fun createFile(project: Project, text: String): ZserioFile =
        PsiFileFactory.getInstance(project).createFileFromText("dummy.zs", ZserioFileType, text) as ZserioFile

    /** Creates a detached identifier element, e.g. to replace a name when renaming. */
    fun createId(project: Project, name: String): ZserioId {
        val file = createFile(project, "const uint8 $name = 0;")
        return PsiTreeUtil.findChildOfType(file, ZserioId::class.java)
            ?: throw IllegalArgumentException("'$name' is not a valid Zserio identifier")
    }
}
