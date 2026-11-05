package org.zserio.intellij.version

import com.intellij.psi.tree.IElementType
import org.zserio.intellij.psi.ZserioTypes

/** A language element which is not available in every supported version of Zserio. */
class ZserioFeature(val description: String, val since: ZserioVersion, val keyword: String? = null)

/**
 * The versions of Zserio understood by the plugin and the language features added after the oldest of them.
 *
 * The parser accepts the syntax of the newest supported version. Every release only added to the language of the
 * previous one (the grammar of the compiler is identical from 2.12.0 to [LATEST]), so schemas of the older supported
 * versions parse as they are. Features newer than a schema's declared compatibility version are reported by
 * [org.zserio.intellij.ide.ZserioVersionAnnotator].
 */
object ZserioSupport {
    /** Oldest supported release. Zserio 1.x (with `rpc` and `stream`) is not supported. */
    val OLDEST = ZserioVersion(2, 0, 0)

    /** Newest release whose language the plugin has been checked against. */
    val LATEST = ZserioVersion(2, 20, 0)

    /** Human readable range, e.g. for messages and the settings page. */
    val RANGE_TEXT: String = "${OLDEST.toShortString()} to ${LATEST.toShortString()}"

    fun isSupported(version: ZserioVersion): Boolean = version in OLDEST..LATEST

    /** Added in 2.1.0: the `/*! ... !*/` markdown documentation comment. */
    val MARKDOWN_COMMENT = ZserioFeature("markdown documentation comments", ZserioVersion(2, 1))

    /** Features which are a single token, by the type of that token. */
    val TOKEN_FEATURES: Map<IElementType, ZserioFeature> = mapOf(
        ZserioTypes.PACKED to ZserioFeature("'packed' fields", ZserioVersion(2, 4), "packed"),
        ZserioTypes.RULE_GROUP to ZserioFeature("'rule_group'", ZserioVersion(2, 4), "rule_group"),
        ZserioTypes.ISSET to ZserioFeature("'isset'", ZserioVersion(2, 8), "isset"),
        ZserioTypes.BYTES to ZserioFeature("the 'bytes' type", ZserioVersion(2, 9), "bytes"),
        ZserioTypes.EXTEND to ZserioFeature("'extend'", ZserioVersion(2, 12), "extend"),
        ZserioTypes.DEPRECATED to ZserioFeature("'@deprecated'", ZserioVersion(2, 12)),
        ZserioTypes.REMOVED to ZserioFeature("'@removed'", ZserioVersion(2, 12))
    )

    /**
     * Added in 2.5.0. Not part of [TOKEN_FEATURES]: the directive states the version a schema was written for, so a
     * value below 2.5 (as in the language documentation's own example) is normal and must not be reported.
     */
    private val COMPATIBILITY_DIRECTIVE = ZserioFeature(
        "'zserio_compatibility_version'", ZserioVersion(2, 5), "zserio_compatibility_version"
    )

    private val KEYWORD_FEATURES: Map<String, ZserioFeature> =
        (TOKEN_FEATURES.values + COMPATIBILITY_DIRECTIVE).mapNotNull { feature -> feature.keyword?.let { it to feature } }.toMap()

    /** The release which introduced [keyword], or null when it exists in every supported version. */
    fun sinceOfKeyword(keyword: String): ZserioVersion? = KEYWORD_FEATURES[keyword]?.since

    /** The feature which [tokenType] with the given [text] represents, if it is not available since 2.0.0. */
    fun featureOf(tokenType: IElementType, text: CharSequence): ZserioFeature? = when {
        tokenType == ZserioTypes.DOC_COMMENT -> if (text.startsWith("/*!")) MARKDOWN_COMMENT else null
        else -> TOKEN_FEATURES[tokenType]
    }
}
