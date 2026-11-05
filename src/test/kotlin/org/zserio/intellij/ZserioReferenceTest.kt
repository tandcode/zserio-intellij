package org.zserio.intellij

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiNamedElement
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.zserio.intellij.psi.ZserioBitmaskValue
import org.zserio.intellij.psi.ZserioEnumItem
import org.zserio.intellij.psi.ZserioFieldTypeId
import org.zserio.intellij.psi.ZserioFunctionDefinition
import org.zserio.intellij.psi.ZserioParameterDefinition
import org.zserio.intellij.psi.ZserioStructDeclaration
import org.zserio.intellij.psi.ZserioTemplateParameter

class ZserioReferenceTest : BasePlatformTestCase() {
    private fun resolveAtCaret(text: String): PsiElement? {
        myFixture.configureByText("main.zs", text)
        return myFixture.file.findReferenceAt(myFixture.caretOffset)?.resolve()
    }

    private fun nameOf(element: PsiElement?): String? = (element as? PsiNamedElement)?.name

    // ---- types -------------------------------------------------------------------------------------------------

    fun testTypeInSameFile() {
        val target = resolveAtCaret("struct A { }; struct B { <caret>A a; };")
        assertInstanceOf(target, ZserioStructDeclaration::class.java)
        assertEquals("A", nameOf(target))
    }

    fun testTypeInOtherFileOfSamePackage() {
        myFixture.addFileToProject("a.zs", "package p; struct A { };")
        val target = resolveAtCaret("package p; struct B { <caret>A a; };")
        assertInstanceOf(target, ZserioStructDeclaration::class.java)
        assertEquals("a.zs", target?.containingFile?.name)
    }

    fun testTypeInOtherPackageNeedsImport() {
        myFixture.addFileToProject("a.zs", "package q; struct A { };")
        assertNull(resolveAtCaret("package p; struct B { <caret>A a; };"))
    }

    fun testSingleTypeImport() {
        myFixture.addFileToProject("a.zs", "package q; struct A { }; struct Other { };")
        assertEquals("A", nameOf(resolveAtCaret("package p; import q.A; struct B { <caret>A a; };")))
        assertNull(resolveAtCaret("package p; import q.A; struct B { <caret>Other a; };"))
    }

    fun testWildcardImport() {
        myFixture.addFileToProject("a.zs", "package q.sub; struct A { }; struct Other { };")
        assertEquals("Other", nameOf(resolveAtCaret("package p; import q.sub.*; struct B { <caret>Other a; };")))
    }

    fun testImportStatementItselfResolves() {
        myFixture.addFileToProject("a.zs", "package q; struct A { };")
        assertEquals("A", nameOf(resolveAtCaret("package p; import q.<caret>A; struct B { };")))
    }

    fun testQualifiedType() {
        myFixture.addFileToProject("a.zs", "package q; struct A { };")
        assertEquals("A", nameOf(resolveAtCaret("package p; struct B { q.<caret>A a; };")))
    }

    fun testDefaultPackageIsShared() {
        myFixture.addFileToProject("a.zs", "struct A { };")
        assertEquals("A", nameOf(resolveAtCaret("struct B { <caret>A a; };")))
    }

    fun testTemplateParameter() {
        val target = resolveAtCaret("struct Box<T> { <caret>T value; };")
        assertInstanceOf(target, ZserioTemplateParameter::class.java)
    }

    fun testTemplateArgument() {
        assertEquals("A", nameOf(resolveAtCaret("struct A { }; struct Box<T> { T v; }; struct S { Box<<caret>A> box; };")))
    }

    fun testSubtypeAndConstArePartOfTheSamePackage() {
        assertEquals("Byte", nameOf(resolveAtCaret("subtype uint8 Byte; struct S { <caret>Byte b; };")))
    }

    // ---- expressions -------------------------------------------------------------------------------------------

    fun testFieldInExpression() {
        val target = resolveAtCaret("struct S { uint8 len; uint8 d : <caret>len > 0; };")
        assertInstanceOf(target, ZserioFieldTypeId::class.java)
        assertEquals("len", nameOf(target))
    }

    fun testParameterInExpression() {
        val target = resolveAtCaret("struct S(uint8 version) { uint8 f if <caret>version > 1; };")
        assertInstanceOf(target, ZserioParameterDefinition::class.java)
    }

    fun testFunctionInExpression() {
        val target = resolveAtCaret("struct S { uint8 a; function uint8 get() { return a; } uint8 b : <caret>get() > 1; };")
        assertInstanceOf(target, ZserioFunctionDefinition::class.java)
    }

    fun testConstantInExpression() {
        assertEquals("LIMIT", nameOf(resolveAtCaret("const uint8 LIMIT = 5; struct S { uint8 a : a < <caret>LIMIT; };")))
    }

    fun testConstantFromImportedPackage() {
        myFixture.addFileToProject("c.zs", "package q; const uint8 LIMIT = 5;")
        assertEquals("LIMIT", nameOf(resolveAtCaret("package p; import q.*; struct S { uint8 a : a < <caret>LIMIT; };")))
    }

    fun testEnumItemThroughType() {
        val target = resolveAtCaret(
            "enum uint8 Color { RED, GREEN }; struct S { Color c; uint8 x if c == Color.<caret>GREEN; };"
        )
        assertInstanceOf(target, ZserioEnumItem::class.java)
        assertEquals("GREEN", nameOf(target))
    }

    fun testEnumItemThroughQualifiedPackage() {
        myFixture.addFileToProject("c.zs", "package q; enum uint8 Color { RED, GREEN };")
        val target = resolveAtCaret("package p; struct S { uint8 x if x == q.Color.<caret>RED; };")
        assertInstanceOf(target, ZserioEnumItem::class.java)
    }

    fun testBitmaskValue() {
        val target = resolveAtCaret(
            "bitmask uint8 Flags { READ, WRITE }; struct S { Flags f; bool b if isset(f, Flags.<caret>WRITE); };"
        )
        assertInstanceOf(target, ZserioBitmaskValue::class.java)
    }

    fun testMemberOfFieldType() {
        val target = resolveAtCaret(
            "struct Inner { uint8 value; }; struct Outer { Inner inner; uint8 x : inner.<caret>value > 0; };"
        )
        assertInstanceOf(target, ZserioFieldTypeId::class.java)
        assertEquals("value", nameOf(target))
        assertEquals("Inner", (target?.parent?.parent as? ZserioStructDeclaration)?.name)
    }

    fun testMemberThroughSubtypeAndArray() {
        val target = resolveAtCaret(
            "struct Inner { uint8 value; }; subtype Inner Alias; " +
                "struct Outer { Alias items[]; uint8 x : items[0].<caret>value > 0; };"
        )
        assertEquals("value", nameOf(target))
    }

    fun testMemberThroughFunctionResult() {
        val target = resolveAtCaret(
            "struct Inner { uint8 value; }; struct Outer { Inner inner; function Inner get() { return inner; } " +
                "uint8 x : get().<caret>value > 0; };"
        )
        assertEquals("value", nameOf(target))
    }

    fun testCaseLabelUsesSelectorEnumeration() {
        val target = resolveAtCaret(
            "enum uint8 Color { RED, GREEN }; choice C(Color color) on color { case <caret>RED: uint8 r; default: ; };"
        )
        assertInstanceOf(target, ZserioEnumItem::class.java)
    }

    fun testEnumItemSeesPreviousItems() {
        val target = resolveAtCaret("enum uint8 E { A, B = <caret>A + 1 };")
        assertInstanceOf(target, ZserioEnumItem::class.java)
    }

    fun testUnknownNameDoesNotResolve() {
        assertNull(resolveAtCaret("struct S { uint8 a : <caret>unknown > 0; };"))
    }

    // ---- refactorings and navigation -------------------------------------------------------------------------

    fun testRenameTypeUpdatesReferencesInAllFiles() {
        val other = myFixture.addFileToProject("b.zs", "package p; struct B { A a; A other; };")
        myFixture.configureByText("a.zs", "package p; struct <caret>A { };")
        myFixture.renameElementAtCaret("Renamed")
        myFixture.checkResult("package p; struct Renamed { };")
        assertEquals("package p; struct B { Renamed a; Renamed other; };", other.text)
    }

    fun testRenameFieldUpdatesExpressions() {
        myFixture.configureByText("a.zs", "struct S { uint8 <caret>len; uint8 d : len > 0; };")
        myFixture.renameElementAtCaret("count")
        myFixture.checkResult("struct S { uint8 count; uint8 d : count > 0; };")
    }

    fun testRenameFromUsage() {
        myFixture.configureByText("a.zs", "enum uint8 Color { RED }; struct S { Color c; uint8 x if c == Color.<caret>RED; };")
        myFixture.renameElementAtCaret("CRIMSON")
        myFixture.checkResult("enum uint8 Color { CRIMSON }; struct S { Color c; uint8 x if c == Color.CRIMSON; };")
    }

    fun testFindUsages() {
        myFixture.addFileToProject("b.zs", "package p; struct B { A a; };")
        myFixture.configureByText("a.zs", "package p; struct <caret>A { }; struct C { A a; A b; };")
        val usages = myFixture.testFindUsagesUsingAction()
        assertEquals(3, usages.size)
    }

    // ---- completion ------------------------------------------------------------------------------------------

    private fun completionsAt(text: String): List<String> {
        myFixture.configureByText("main.zs", text)
        myFixture.completeBasic()
        return myFixture.lookupElementStrings.orEmpty()
    }

    fun testCompletionOfMembersInExpression() {
        val variants = completionsAt("struct S { uint8 length; uint8 count; uint8 d : <caret> };")
        assertContainsElements(variants, "length", "count", "S")
    }

    fun testCompletionOfEnumItemsAfterDot() {
        val variants = completionsAt("enum uint8 Color { RED, GREEN }; struct S { uint8 x if x == Color.<caret> };")
        assertSameElements(variants, "RED", "GREEN")
    }

    fun testCompletionOfTypesAndKeywordsInStructBody() {
        val variants = completionsAt("struct Other { }; struct S { <caret> };")
        assertContainsElements(variants, "Other", "uint8", "string", "optional", "align", "function")
    }

    fun testCompletionOfTopLevelKeywords() {
        val variants = completionsAt("<caret>")
        assertContainsElements(variants, "package", "import", "struct", "enum", "const")
    }

    fun testCompletionOfTypesFromImportedPackage() {
        myFixture.addFileToProject("c.zs", "package q; struct Imported { };")
        val variants = completionsAt("package p; import q.*; struct S { <caret> };")
        assertContainsElements(variants, "Imported")
    }
}
