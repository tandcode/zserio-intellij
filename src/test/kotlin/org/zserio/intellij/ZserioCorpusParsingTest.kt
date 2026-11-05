package org.zserio.intellij

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import java.io.File

/**
 * Parses every `*.zs` file below the directories given in the system property `zserio.corpus` (path separated list,
 * pass it with `./gradlew test -Pzserio.corpus=/path/to/zserio/benchmarks`) and expects no syntax errors.
 *
 * Without the property the test does nothing. Directories with intentionally broken schemas can be excluded with the
 * system property `zserio.corpus.skip` (a regular expression matched against the file path).
 */
class ZserioCorpusParsingTest : BasePlatformTestCase() {
    fun testCorpusParsesWithoutErrors() {
        val roots = System.getProperty("zserio.corpus")?.split(File.pathSeparator)?.filter { it.isNotBlank() }
        if (roots.isNullOrEmpty()) {
            return
        }
        val skip = System.getProperty("zserio.corpus.skip")?.let { Regex(it) }
        val files = roots.flatMap { root -> File(root).walkTopDown().filter { it.isFile && it.extension == "zs" }.toList() }
            .filter { skip == null || !skip.containsMatchIn(it.path) }
            .sortedBy { it.path }
        assertNotEmpty(files)

        val failures = mutableListOf<String>()
        for (file in files) {
            val psi = myFixture.configureByText("corpus.zs", file.readText())
            val errors = PsiTreeUtil.findChildrenOfType(psi, PsiErrorElement::class.java)
            if (errors.isNotEmpty()) {
                val first = errors.first()
                val line = psi.text.substring(0, first.textOffset).count { it == '\n' } + 1
                failures += "${file.path}:$line: ${first.errorDescription}"
            }
        }
        println("Parsed ${files.size} files, ${failures.size} with errors")
        assertEmpty(failures)
    }
}
