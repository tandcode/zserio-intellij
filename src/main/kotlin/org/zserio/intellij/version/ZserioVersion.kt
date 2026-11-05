package org.zserio.intellij.version

/** A Zserio release number such as `2.9.0`. Pre-release suffixes (`2.9.0-pre1`) are ignored. */
data class ZserioVersion(val major: Int, val minor: Int, val patch: Int = 0) : Comparable<ZserioVersion> {
    override fun compareTo(other: ZserioVersion): Int =
        compareValuesBy(this, other, ZserioVersion::major, ZserioVersion::minor, ZserioVersion::patch)

    override fun toString(): String = "$major.$minor.$patch"

    /** Short form used in messages, e.g. `2.9` for `2.9.0`. */
    fun toShortString(): String = if (patch == 0) "$major.$minor" else toString()

    companion object {
        private val PATTERN = Regex("""(\d+)\.(\d+)(?:\.(\d+))?(?:-\w+)?""")

        /** Returns null when [text] is not a version number. Surrounding whitespace is ignored. */
        fun parse(text: String): ZserioVersion? {
            val match = PATTERN.matchEntire(text.trim()) ?: return null
            val (major, minor, patch) = match.destructured
            return try {
                ZserioVersion(major.toInt(), minor.toInt(), patch.ifEmpty { "0" }.toInt())
            } catch (e: NumberFormatException) {
                null
            }
        }
    }
}
