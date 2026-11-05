package org.zserio.intellij

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class ZserioEditorTest : BasePlatformTestCase() {
    override fun getTestDataPath(): String = "src/test/testData/editor"

    fun testFolding() {
        myFixture.testFolding("$testDataPath/Folding.zs")
    }

    fun testSemanticHighlighting() {
        myFixture.configureByFile("Semantic.zs")
        myFixture.checkHighlighting(false, true, false)
    }

    fun testSyntaxErrorsAreHighlighted() {
        myFixture.configureByText(
            "a.zs",
            "struct S { uint8 a<error descr=\"':', ';', '=', '[' or if expected, got '}'\"> </error>};"
        )
        myFixture.checkHighlighting()
    }

    fun testBraceMatching() {
        myFixture.configureByText("a.zs", "struct S { uint8 a : (a > 0); <caret>};")
        assertEquals(9, com.intellij.codeInsight.highlighting.BraceMatchingUtil.getMatchedBraceOffset(
            myFixture.editor, false, myFixture.file))
    }

    fun testCommentByLineAction() {
        myFixture.configureByText("a.zs", "struct S {<caret> };")
        myFixture.performEditorAction("CommentByLineComment")
        myFixture.checkResult("//struct S { };")
    }

    fun testCommentByBlockAction() {
        myFixture.configureByText("a.zs", "<selection>struct S { }</selection>;")
        myFixture.performEditorAction("CommentByBlockComment")
        myFixture.checkResult("/*struct S { }*/;")
    }
}
