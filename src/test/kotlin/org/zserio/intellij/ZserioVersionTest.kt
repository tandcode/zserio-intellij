package org.zserio.intellij

import com.intellij.psi.PsiErrorElement
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import org.zserio.intellij.version.ZserioSupport
import org.zserio.intellij.version.ZserioVersion

class ZserioVersionTest : BasePlatformTestCase() {
    private fun assertParses(text: String) {
        val file = myFixture.configureByText("test.zs", text)
        val errors = PsiTreeUtil.findChildrenOfType(file, PsiErrorElement::class.java).map { it.errorDescription }
        assertTrue("Unexpected parse errors: $errors", errors.isEmpty())
    }

    fun testVersionParsing() {
        assertEquals(ZserioVersion(2, 4, 2), ZserioVersion.parse("2.4.2"))
        assertEquals(ZserioVersion(2, 9, 0), ZserioVersion.parse(" 2.9 "))
        assertEquals(ZserioVersion(2, 9, 0), ZserioVersion.parse("2.9.0-pre1"))
        assertNull(ZserioVersion.parse(""))
        assertNull(ZserioVersion.parse("2"))
        assertNull(ZserioVersion.parse("v2.9.0"))
        assertNull(ZserioVersion.parse("2.x.0"))
        assertNull(ZserioVersion.parse("99999999999.0.0"))
    }

    fun testVersionOrdering() {
        assertTrue(ZserioVersion(2, 10) > ZserioVersion(2, 9, 5))
        assertTrue(ZserioVersion(2, 4, 2) < ZserioVersion(2, 5))
        assertEquals("2.9", ZserioVersion(2, 9).toShortString())
        assertEquals("2.4.2", ZserioVersion(2, 4, 2).toShortString())
    }

    fun testSupportedRange() {
        assertTrue(ZserioSupport.isSupported(ZserioVersion(2, 0, 0)))
        assertTrue(ZserioSupport.isSupported(ZserioVersion(2, 13, 0)))
        assertTrue(ZserioSupport.isSupported(ZserioSupport.LATEST))
        assertFalse(ZserioSupport.isSupported(ZserioVersion(1, 4, 0)))
        assertFalse(ZserioSupport.isSupported(ZserioVersion(2, 21, 0)))
        assertEquals("2.0 to 2.20", ZserioSupport.RANGE_TEXT)
    }

    // ---- syntax of each release is accepted ----

    fun testSyntaxOf2_0() = assertParses(
        """
        package v20;
        bitmask uint8 Flags { A, B = 2 };
        pubsub Bus { publish topic("a/b") Msg out; subscribe topic("c") Msg in; topic("d") Msg any; };
        service Svc { Msg call(Msg); };
        sql_table T { int32 id sql "PRIMARY KEY NOT NULL"; sql_virtual string name; sql_without_rowid; };
        struct Msg { varsize n; align(8): uint8 data[n]; string s = "tab\t\u00e4\x41\012"; };
        """.trimIndent()
    )

    fun testSyntaxOf2_1() = assertParses("/*! # Markdown !*/\nstruct S { uint8 a; };")

    fun testSyntaxOf2_4() = assertParses(
        """
        rule_group G { rule "r1"; };
        struct S { packed uint8 a[]; };
        """.trimIndent()
    )

    fun testSyntaxOf2_5() = assertParses("zserio_compatibility_version(\"2.5.0\");\npackage p;\nstruct S { };")

    fun testSyntaxOf2_8() = assertParses("struct S { uint8 a; optional uint8 b if isset(a, Flags.A); };")

    fun testSyntaxOf2_9() = assertParses("struct S { bytes data; };")

    fun testSyntaxOf2_12() = assertParses(
        """
        enum uint8 E { @deprecated A, @removed B, C };
        struct S { uint8 a; extend uint8 b; };
        """.trimIndent()
    )

    fun testOlderSyntaxStillAcceptedAfterNewerOnesWereAdded() = assertParses(
        """
        struct Pair<A, B> { A a; B b; };
        instantiate Pair<uint8, string> P;
        struct S(uint8 n) { bit<n> d[]; extern e; uint32 off; off: uint8 x; int<4> y; };
        """.trimIndent()
    )

    // ---- warnings for features newer than the declared compatibility version ----

    fun testFeatureNewerThanDeclaredVersionIsReported() {
        myFixture.configureByText(
            "a.zs",
            """
            zserio_compatibility_version("2.4.0");
            package a;
            struct S { <warning descr="The 'bytes' type requires Zserio 2.9, but this file declares compatibility version 2.4">bytes</warning> data; };
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testSeveralFeaturesAreReportedIndividually() {
        myFixture.configureByText(
            "a.zs",
            """
            zserio_compatibility_version("2.4.0");
            enum uint8 E { <warning descr="'@deprecated' requires Zserio 2.12, but this file declares compatibility version 2.4">@deprecated</warning> A };
            struct S { <warning descr="'extend' requires Zserio 2.12, but this file declares compatibility version 2.4">extend</warning> uint8 a; packed uint8 b[]; };
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testMarkdownCommentNewerThanDeclaredVersionIsReported() {
        myFixture.configureByText(
            "a.zs",
            """
            zserio_compatibility_version("2.0.0");
            <warning descr="Markdown documentation comments requires Zserio 2.1, but this file declares compatibility version 2.0">/*! text !*/</warning>
            struct S { };
            """.trimIndent()
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testCompatibilityDirectiveItselfIsNeverReportedAsTooNew() {
        myFixture.configureByText("a.zs", "zserio_compatibility_version(\"2.4.2\");\nstruct S { };")
        myFixture.checkHighlighting(true, false, false)
    }

    fun testFeaturesAreNotReportedWithoutDeclaredVersion() {
        myFixture.configureByText("a.zs", "struct S { bytes data; extend uint8 a; };")
        myFixture.checkHighlighting(true, false, false)
    }

    fun testFeaturesAreNotReportedWhenDeclaredVersionIsNewEnough() {
        myFixture.configureByText(
            "a.zs",
            "zserio_compatibility_version(\"2.12.0\");\nstruct S { bytes data; extend uint8 a; };"
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testDeclaredVersionOutsideOfSupportedRangeIsReported() {
        myFixture.configureByText(
            "a.zs",
            "zserio_compatibility_version(<warning descr=\"Zserio 3.0 is outside of the versions supported by the plugin " +
                "(2.0 to 2.20), the syntax may not be recognized correctly\">\"3.0.0\"</warning>);\nstruct S { };"
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testInvalidDeclaredVersionIsReported() {
        myFixture.configureByText(
            "a.zs",
            "zserio_compatibility_version(<warning descr=\"Not a Zserio version number, expected a version such as 2.9.0\">\"abc\"</warning>);\nstruct S { };"
        )
        myFixture.checkHighlighting(true, false, false)
    }

    fun testKeywordCompletionShowsTheIntroducingVersion() {
        myFixture.configureByText("a.zs", "struct S { <caret> };")
        val lookups = myFixture.completeBasic()
        val bytes = lookups.first { it.lookupString == "extend" }
        val presentation = com.intellij.codeInsight.lookup.LookupElementPresentation()
        bytes.renderElement(presentation)
        assertEquals("  since 2.12", presentation.tailText)
    }
}
