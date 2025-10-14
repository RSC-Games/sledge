package com.rsc_games.sledge.parser;

public class Token {
    public final TokenID tok;
    public final String val;
    public final int lno;

    public Token(TokenID id, String s, int lno) {
        this.tok = id;
        this.val = s;
        this.lno = lno;
    }

    public String toString() {
        return tok + (tok != TokenID.TOK_NEWLINE ? " of value " + val : "") 
            + " on lno " + lno;
    }
}
