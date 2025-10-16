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
    TOK_EQUALS,  // =, mergeable
    TOK_COLON, // :, mergeable
    TOK_INTERNAL_UNIT,  // %
    TOK_UNIT,  // @
    TOK_PIPE,  // |, mergeable
    TOK_AMPERSAND,  // &, mergeable

    // Composite tokens
    TOK_APPEND,  // :=

    // Conditionals
    TOK_COND_LESS_THAN,  // <, mergeable
    TOK_COND_LESS_THAN_OR_EQUAL,  // <=
    TOK_COND_GREATER_THAN,  // >, mergeable
    TOK_COND_GREATER_THAN_OR_EQUAL,  // >=
    TOK_COND_EQUIVALENT,  // ==
    TOK_COND_NOT_EQUIVALENT,  // !=
    TOK_COND_OR,  // ||
    TOK_COND_AND,  // &&
    TOK_COND_NOT,  // !, mergeable

    TOK_CHAR
}
