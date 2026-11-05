package org.zserio.intellij.compiler

import java.io.IOException
import java.nio.file.FileVisitResult
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.SimpleFileVisitor
import java.nio.file.attribute.BasicFileAttributes

/**
 * Lets the compiler check the unsaved text of a file without touching the project: a temporary source root which
 * contains symbolic links to everything in the real source root, except for the edited file which is a real file with
 * the text from the editor.
 */
object ZserioSourceOverlay {
    /** Creates the overlay in the existing empty directory [overlayRoot]. */
    @Throws(IOException::class)
    fun create(realRoot: Path, relativePath: String, text: String, overlayRoot: Path) {
        val parts = relativePath.split('/')
        var real = realRoot
        var overlay = overlayRoot
        for ((index, part) in parts.withIndex()) {
            if (Files.isDirectory(real)) {
                Files.list(real).use { entries ->
                    for (entry in entries) {
                        if (entry.fileName.toString() != part) {
                            Files.createSymbolicLink(overlay.resolve(entry.fileName.toString()), entry)
                        }
                    }
                }
            }
            if (index == parts.lastIndex) {
                Files.writeString(overlay.resolve(part), text)
            } else {
                real = real.resolve(part)
                overlay = overlay.resolve(part)
                Files.createDirectories(overlay)
            }
        }
    }

    /** Deletes [directory] recursively without following symbolic links. */
    fun delete(directory: Path) {
        try {
            Files.walkFileTree(directory, object : SimpleFileVisitor<Path>() {
                override fun visitFile(file: Path, attrs: BasicFileAttributes): FileVisitResult {
                    Files.deleteIfExists(file)
                    return FileVisitResult.CONTINUE
                }

                override fun postVisitDirectory(dir: Path, exc: IOException?): FileVisitResult {
                    Files.deleteIfExists(dir)
                    return FileVisitResult.CONTINUE
                }
            })
        } catch (ignored: IOException) {
            // best effort: the overlay lives in the temporary directory anyway
        }
    }
}
