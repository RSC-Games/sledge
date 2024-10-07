package common.parser;

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
        return tok + (tok != TokenID.TOK_LINE_TERM ? " of value " + val : "") 
            + " on lno " + lno;
    }
}
