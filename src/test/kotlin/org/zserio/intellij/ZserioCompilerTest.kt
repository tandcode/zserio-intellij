package org.zserio.intellij

import junit.framework.TestCase
import org.zserio.intellij.compiler.ZserioCompiler
import org.zserio.intellij.compiler.ZserioCompilerOutput
import org.zserio.intellij.compiler.ZserioSeverity
import org.zserio.intellij.compiler.ZserioSourceOverlay
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

class ZserioCompilerTest : TestCase() {
    private lateinit var directory: Path

    override fun setUp() {
        super.setUp()
        directory = Files.createTempDirectory("zserio-test")
    }

    override fun tearDown() {
        ZserioSourceOverlay.delete(directory)
        super.tearDown()
    }

    private fun write(relative: String, text: String): Path {
        val file = directory.resolve(relative)
        Files.createDirectories(file.parent)
        Files.writeString(file, text)
        return file
    }

    fun testParseOutput() {
        val diagnostics = ZserioCompilerOutput.parse(
            """
            Parsing src/a/b.zs
            [ERROR] src/a/b.zs:4:13: 'align' is a reserved keyword!
            [WARNING] C:\work\a.zs:10:2: Unused type 'Foo'
            [ERROR] src/a/b.zs: No such file!
            [INFO] x.zs:1:1: info
            """.trimIndent()
        )
        assertEquals(3, diagnostics.size)
        assertEquals(ZserioSeverity.ERROR, diagnostics[0].severity)
        assertEquals("src/a/b.zs", diagnostics[0].path)
        assertEquals(4, diagnostics[0].line)
        assertEquals(13, diagnostics[0].column)
        assertEquals("'align' is a reserved keyword!", diagnostics[0].message)
        assertEquals("C:\\work\\a.zs", diagnostics[1].path)
        assertEquals(ZserioSeverity.WARNING, diagnostics[1].severity)
        assertEquals(ZserioSeverity.INFO, diagnostics[2].severity)
    }

    fun testLocateUsesThePackageToFindTheSourceRoot() {
        val (root, relative) = ZserioCompiler.locate(Paths.get("/work/schemas/a/b/c.zs"), "a.b.c")
        assertEquals(Paths.get("/work/schemas"), root)
        assertEquals("a/b/c.zs", relative)
    }

    fun testLocateFallsBackToTheDirectoryOfTheFile() {
        val (root, relative) = ZserioCompiler.locate(Paths.get("/work/schemas/other.zs"), "a.b.c")
        assertEquals(Paths.get("/work/schemas"), root)
        assertEquals("other.zs", relative)

        val (rootOfDefault, relativeOfDefault) = ZserioCompiler.locate(Paths.get("/work/schemas/default.zs"), "")
        assertEquals(Paths.get("/work/schemas"), rootOfDefault)
        assertEquals("default.zs", relativeOfDefault)
    }

    fun testOverlayReplacesOnlyTheEditedFile() {
        val realRoot = directory.resolve("real")
        write("real/a/b.zs", "saved")
        write("real/a/other.zs", "other")
        write("real/c.zs", "c")
        val overlay = Files.createDirectories(directory.resolve("overlay"))

        ZserioSourceOverlay.create(realRoot, "a/b.zs", "edited", overlay)

        assertEquals("edited", Files.readString(overlay.resolve("a/b.zs")))
        assertEquals("other", Files.readString(overlay.resolve("a/other.zs")))
        assertEquals("c", Files.readString(overlay.resolve("c.zs")))
        assertEquals("saved", Files.readString(realRoot.resolve("a/b.zs")))

        ZserioSourceOverlay.delete(overlay)
        assertFalse(Files.exists(overlay))
        assertEquals("other", Files.readString(realRoot.resolve("a/other.zs"))) // links are not followed
    }

    /** Runs the real compiler; only when a jar is given with -Pzserio.jar=... */
    fun testRealCompiler() {
        val jar = System.getProperty("zserio.jar") ?: return
        val java = Paths.get(System.getProperty("java.home"), "bin", "java").toString()
        write("main.zs", "package main;\nimport q.Other;\nstruct S\n{\n    Other o;\n    uint8 x : x > unknown;\n};\n")
        write("q.zs", "package q;\nstruct Other\n{\n    uint8 v;\n};\n")
        val compiler = ZserioCompiler(java, jar)

        val diagnostics = compiler.check(directory, "main.zs", null)
        assertEquals(diagnostics.toString(), 1, diagnostics.size)
        assertEquals(ZserioSeverity.ERROR, diagnostics[0].severity)
        assertEquals(6, diagnostics[0].line)
        assertEquals(19, diagnostics[0].column)

        // unsaved text: the file on disk is wrong, the text from the editor is fine
        val fixed = "package main;\nimport q.Other;\nstruct S\n{\n    Other o;\n    uint8 x : x > 0;\n};\n"
        assertEmpty(compiler.check(directory, "main.zs", fixed))
        // the file on disk was not modified
        assertTrue(Files.readString(directory.resolve("main.zs")).contains("unknown"))
        // messages of imported files are not attributed to the checked file
        write("q.zs", "package q;\nstruct Other\n{\n    uint8 v : v > nothing;\n};\n")
        assertEmpty(compiler.check(directory, "main.zs", fixed))
    }

    private fun assertEmpty(collection: Collection<*>) = assertTrue(collection.toString(), collection.isEmpty())
}
