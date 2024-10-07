package common.parser;

public enum TokenID {
    // Names and whitespace
    TOK_NAME,
    TOK_COMMENT,  // Whitespace
    TOK_STRING,
    TOK_LINE_TERM,  // Whitespace
    TOK_SPACE,  // Whitespace
    TOK_EOF,

    // Special characters
    TOK_PAREN_OPEN,
    TOK_PAREN_CLOSE,
    TOK_CURLY_OPEN,
    TOK_CURLY_CLOSE,
    TOK_QUOTE,  // "
    TOK_DEREF,  // $
    TOK_EQUALS,  // =
    TOK_APPEND, // :  not implemented
    TOK_INTERNAL_UNIT,  // %
    TOK_UNIT,  // @
    TOK_CHAR
}
