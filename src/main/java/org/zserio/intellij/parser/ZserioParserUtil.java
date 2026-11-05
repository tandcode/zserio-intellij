package org.zserio.intellij.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.parser.GeneratedParserUtilBase;
import com.intellij.openapi.util.Key;

/** Helpers referenced from Zserio.bnf. */
public class ZserioParserUtil extends GeneratedParserUtilBase {
    private static final Key<Boolean> NO_GT = Key.create("zserio.parser.noGt");

    /** Consumes a single unexpected token and marks it as an error. Fails at the end of the file. */
    public static boolean errorToken(PsiBuilder builder, int level) {
        if (builder.eof()) {
            return false;
        }
        PsiBuilder.Marker marker = builder.mark();
        builder.advanceLexer();
        marker.error("Unexpected token");
        return true;
    }

    /** True unless parsing the length of a dynamic bit field, e.g. "bit&lt;length&gt;", where '&gt;' closes the length. */
    public static boolean gtAllowed(PsiBuilder builder, int level) {
        return !Boolean.TRUE.equals(builder.getUserData(NO_GT));
    }

    /** Parses with '&gt;' disallowed as an operator. */
    public static boolean noGt(PsiBuilder builder, int level, Parser parser) {
        Boolean previous = builder.getUserData(NO_GT);
        builder.putUserData(NO_GT, Boolean.TRUE);
        try {
            return parser.parse(builder, level);
        } finally {
            builder.putUserData(NO_GT, previous);
        }
    }
}
