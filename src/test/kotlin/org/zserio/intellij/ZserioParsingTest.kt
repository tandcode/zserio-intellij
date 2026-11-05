package org.zserio.intellij

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.zserio.intellij.psi.ZserioNamedElement
import org.zserio.intellij.psi.ZserioStructDeclaration

class ZserioParsingTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData"

    private fun errorsOf(file: PsiFile): List<String> =
        PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java)
            .map { "${it.errorDescription} at offset ${it.textOffset}: '${it.text}'" }

    private fun parse(text: String): PsiFile = myFixture.configureByText("test.zs", text)

    private fun assertParses(text: String) {
        val errors = errorsOf(parse(text))
        assertTrue("Unexpected parse errors: $errors", errors.isEmpty())
    }

    fun testSampleFileHasNoErrors() {
        val file = myFixture.configureByFile("parser/Sample.zs")
        assertEmpty(errorsOf(file))
    }

    fun testPackageAndImportsAreExposed() {
        val file = myFixture.configureByFile("parser/Sample.zs") as ZserioFile
        assertEquals("test.sample", file.packageName)
        assertEquals(2, file.imports.size)
        assertTrue(file.topLevelDeclarations.any { it.name == "Features" })
    }

    fun testNestedTemplateArgumentsAreNotAShift() {
        assertParses("struct S { Pair<Pair<uint8, uint8>, string> p; };")
    }

    fun testShiftAndComparisonInExpressions() {
        assertParses("const uint32 A = 1 << 2 >> 1; const bool B = 1 > 2; const bool C = 4 >= 2;")
    }

    fun testDynamicBitFieldLengthIsNotARelationalExpression() {
        assertParses("struct S { uint8 n; bit<n> a; bit<n + 1> b; int<n * 2> c; uint8 d : d > 0; };")
    }

    fun testGenericFieldWithConstraintIsNotAnOffset() {
        assertParses("struct S { Pair<A, B> field : field > 0; };")
    }

    fun testOffsetExpression() {
        assertParses("struct S { uint32 offset; offset: uint8 data; };")
    }

    fun testEmptyDeclarations() {
        assertParses("struct E { }; enum uint8 En { }; sql_database D { };")
    }

    fun testTrailingCommentWithoutNewline() {
        assertParses("struct E { }; // comment")
    }

    fun testUnterminatedBlockCommentDoesNotFailTheLexer() {
        val file = parse("struct E { }; /* never closed")
        assertEquals(1, PsiTreeUtil.findChildrenOfType(file, ZserioStructDeclaration::class.java).size)
    }

    fun testErrorsAreReportedForBrokenInput() {
        assertNotEmpty(errorsOf(parse("struct S { uint8 a }")))
    }

    fun testBrokenMemberDoesNotHideFollowingDeclarations() {
        val file = parse("struct A { uint8 a uint8 b; }; struct B { uint8 c; };")
        assertNotEmpty(errorsOf(file))
        val names = PsiTreeUtil.findChildrenOfType(file, ZserioStructDeclaration::class.java).map { it.name }
        assertEquals(listOf("A", "B"), names)
        val fieldNames = PsiTreeUtil.findChildrenOfType(file, ZserioNamedElement::class.java).map { it.name }
        assertTrue(fieldNames.containsAll(listOf("a", "b", "c")))
    }

    fun testMissingClosingBraceDoesNotSwallowNextDeclaration() {
        val file = parse("struct A { uint8 a;\nstruct B { uint8 b; };")
        assertNotEmpty(errorsOf(file))
        val names = PsiTreeUtil.findChildrenOfType(file, ZserioStructDeclaration::class.java).map { it.name }
        assertEquals(listOf("A", "B"), names)
    }
}
