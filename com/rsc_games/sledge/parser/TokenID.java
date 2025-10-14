package com.rsc_games.sledge.parser;

public enum TokenID {
    // Names and whitespace
    TOK_NAME,
    TOK_KEYWORD,
    TOK_COMMENT,  // Whitespace
    TOK_STRING,
    TOK_NEWLINE,  // Whitespace
    TOK_SEMICOLON,
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
    TOK_APPEND, // :
    TOK_INTERNAL_UNIT,  // %
    TOK_UNIT,  // @
    TOK_CHAR
}
