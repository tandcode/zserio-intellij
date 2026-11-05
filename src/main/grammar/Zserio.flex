package org.zserio.intellij.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static org.zserio.intellij.psi.ZserioTypes.*;

%%

%{
  public _ZserioLexer() {
    this((java.io.Reader) null);
  }
%}

%public
%class _ZserioLexer
%implements FlexLexer
%function advance
%type IElementType
%unicode

WHITE_SPACE=[ \t\r\n\f]+

// Block comments: "/* ... */". The body must not contain "*/". The same expression with an optional closing
// is used for unterminated comments so that the lexer keeps working while the user is typing.
BLOCK_BODY=([^*]|"*"+[^*/])*
DOC_COMMENT="/**"("*"*[^*/]{BLOCK_BODY})?"*"+"/"
MARKDOWN_COMMENT="/*!"{BLOCK_BODY}"*"+"/"
BLOCK_COMMENT="/*"{BLOCK_BODY}"*"+"/"
UNTERMINATED_COMMENT="/*"{BLOCK_BODY}"*"*
LINE_COMMENT="//"[^\r\n\f]*

EXPONENT=[eE][+\-]?[0-9]+
BINARY_LITERAL=[01]+[bB]
OCTAL_LITERAL=0[0-7]+
HEXADECIMAL_LITERAL=0[xX][0-9a-fA-F]+
DOUBLE_LITERAL=([0-9]+{EXPONENT}|[0-9]+"."[0-9]*{EXPONENT}?|"."[0-9]+{EXPONENT}?)
FLOAT_LITERAL={DOUBLE_LITERAL}[fF]
DECIMAL_LITERAL=([1-9][0-9]*|0)
STRING_LITERAL=\"([^\"\\\r\n\f]|\\.)*\"
UNTERMINATED_STRING=\"([^\"\\\r\n\f]|\\.)*
IDENTIFIER=[a-zA-Z_][a-zA-Z_0-9]*

%%

<YYINITIAL> {
  {WHITE_SPACE}            { return TokenType.WHITE_SPACE; }

  {DOC_COMMENT}            { return DOC_COMMENT; }
  {MARKDOWN_COMMENT}       { return DOC_COMMENT; }
  {BLOCK_COMMENT}          { return BLOCK_COMMENT; }
  {UNTERMINATED_COMMENT}   { return BLOCK_COMMENT; }
  {LINE_COMMENT}           { return LINE_COMMENT; }

  "true"|"false"           { return BOOL_LITERAL; }

  "=="        { return EQ; }
  ">="        { return GE; }
  "<="        { return LE; }
  "&&"        { return LOGICAL_AND; }
  "||"        { return LOGICAL_OR; }
  "<<"        { return LSHIFT; }
  "!="        { return NE; }
  "&"         { return AND; }
  "="         { return ASSIGN; }
  "!"         { return BANG; }
  ":"         { return COLON; }
  ","         { return COMMA; }
  "/"         { return DIVIDE; }
  "."         { return DOT; }
  ">"         { return GT; }
  "{"         { return LBRACE; }
  "["         { return LBRACKET; }
  "("         { return LPAREN; }
  "<"         { return LT; }
  "-"         { return MINUS; }
  "%"         { return MODULO; }
  "*"         { return MULTIPLY; }
  "|"         { return OR; }
  "+"         { return PLUS; }
  "?"         { return QUESTIONMARK; }
  "}"         { return RBRACE; }
  "]"         { return RBRACKET; }
  ")"         { return RPAREN; }
  ";"         { return SEMICOLON; }
  "~"         { return TILDE; }
  "^"         { return XOR; }

  "align"                             { return ALIGN; }
  "bit"                               { return BIT_FIELD; }
  "bool"                              { return BOOL; }
  "bitmask"                           { return BITMASK; }
  "bytes"                             { return BYTES; }
  "case"                              { return CASE; }
  "choice"                            { return CHOICE; }
  "const"                             { return CONST; }
  "default"                           { return DEFAULT; }
  "@deprecated"                       { return DEPRECATED; }
  "enum"                              { return ENUM; }
  "explicit"                          { return EXPLICIT; }
  "extend"                            { return EXTEND; }
  "extern"                            { return EXTERN; }
  "float16"                           { return FLOAT16; }
  "float32"                           { return FLOAT32; }
  "float64"                           { return FLOAT64; }
  "function"                          { return FUNCTION; }
  "if"                                { return IF; }
  "implicit"                          { return IMPLICIT; }
  "import"                            { return IMPORT; }
  "@index"                            { return INDEX; }
  "instantiate"                       { return INSTANTIATE; }
  "int"                               { return INT_FIELD; }
  "int16"                             { return INT16; }
  "int32"                             { return INT32; }
  "int64"                             { return INT64; }
  "int8"                              { return INT8; }
  "isset"                             { return ISSET; }
  "lengthof"                          { return LENGTHOF; }
  "numbits"                           { return NUMBITS; }
  "on"                                { return ON; }
  "optional"                          { return OPTIONAL; }
  "package"                           { return PACKAGE; }
  "packed"                            { return PACKED; }
  "pubsub"                            { return PUBSUB; }
  "publish"                           { return PUBLISH; }
  "@removed"                          { return REMOVED; }
  "return"                            { return RETURN; }
  "rule"                              { return RULE; }
  "rule_group"                        { return RULE_GROUP; }
  "service"                           { return SERVICE; }
  "sql"                               { return SQL; }
  "sql_database"                      { return SQL_DATABASE; }
  "sql_table"                         { return SQL_TABLE; }
  "sql_virtual"                       { return SQL_VIRTUAL; }
  "sql_without_rowid"                 { return SQL_WITHOUT_ROWID; }
  "string"                            { return STRING; }
  "struct"                            { return STRUCTURE; }
  "subscribe"                         { return SUBSCRIBE; }
  "subtype"                           { return SUBTYPE; }
  "topic"                             { return TOPIC; }
  "uint16"                            { return UINT16; }
  "uint32"                            { return UINT32; }
  "uint64"                            { return UINT64; }
  "uint8"                             { return UINT8; }
  "union"                             { return UNION; }
  "using"                             { return USING; }
  "valueof"                           { return VALUEOF; }
  "varint"                            { return VARINT; }
  "varint16"                          { return VARINT16; }
  "varint32"                          { return VARINT32; }
  "varint64"                          { return VARINT64; }
  "varsize"                           { return VARSIZE; }
  "varuint"                           { return VARUINT; }
  "varuint16"                         { return VARUINT16; }
  "varuint32"                         { return VARUINT32; }
  "varuint64"                         { return VARUINT64; }
  "zserio_compatibility_version"      { return COMPAT_VERSION; }

  {STRING_LITERAL}         { return STRING_LITERAL; }
  {FLOAT_LITERAL}          { return FLOAT_LITERAL; }
  {DOUBLE_LITERAL}         { return DOUBLE_LITERAL; }
  {HEXADECIMAL_LITERAL}    { return HEXADECIMAL_LITERAL; }
  {BINARY_LITERAL}         { return BINARY_LITERAL; }
  {OCTAL_LITERAL}          { return OCTAL_LITERAL; }
  {DECIMAL_LITERAL}        { return DECIMAL_LITERAL; }

  {IDENTIFIER}             { return IDENTIFIER; }

  {UNTERMINATED_STRING}    { return TokenType.BAD_CHARACTER; }
}

[^] { return TokenType.BAD_CHARACTER; }
