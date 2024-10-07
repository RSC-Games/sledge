package common.parser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import util.TextFile;

public class Tokenizer {
    static HashMap<String, TokenID> tokTable = new HashMap<String, TokenID>();
    static boolean populated = false;
    TextFile f;
    int lno = 1;

    TokenBuilder cToken;
    boolean read = true;
    String c = "";

    /**
     * Create a tokenizer to parse a given input file.
     * 
     * @param f Provided text file to parse.
     */
    public Tokenizer(String fpath) {
        try {
            this.f = new TextFile(fpath, "r");
        }
        catch (IOException ie) {
            throw new RuntimeException("Build failed! No build config (missing ./hammer)");
        }
        populateTable();
    }

    static void populateTable() {
        if (populated) return;

        tokTable.put("#", TokenID.TOK_COMMENT);
        tokTable.put("\n", TokenID.TOK_LINE_TERM);
        tokTable.put(" ", TokenID.TOK_SPACE);
        tokTable.put("", TokenID.TOK_EOF);
        // Word token not recognized by this table.
        tokTable.put("(", TokenID.TOK_PAREN_OPEN);
        tokTable.put(")", TokenID.TOK_PAREN_CLOSE);
        tokTable.put("{", TokenID.TOK_CURLY_OPEN);
        tokTable.put("}", TokenID.TOK_CURLY_CLOSE);
        tokTable.put("\"", TokenID.TOK_QUOTE);
        tokTable.put("$", TokenID.TOK_DEREF);
        tokTable.put(":", TokenID.TOK_APPEND);
        tokTable.put("=", TokenID.TOK_EQUALS);
        tokTable.put("%", TokenID.TOK_INTERNAL_UNIT);
        tokTable.put("@", TokenID.TOK_UNIT);

        populated = true;
    }

    public boolean available() {
        return this.f != null;
    }
    
    public ArrayList<Token> getAllTokens() {
        ArrayList<Token> tokens = new ArrayList<Token>();
        
        while (available()) {
            Token t = getNextToken();
            tokens.add(t);
            //System.out.println("Got tok " + t);
        }
        
        return tokens;
    }

    // Relies on external global state. Not re-entrant.
    public Token getNextToken() {
        Token t = null;

        while (t == null) {
            if (this.f == null)
                return null;

            if (read) c = readSrc(1);
            t = getToken(c);
        }
        
        return t;
    }

    private Token getToken(String c) {
        // Carriage return unsupported.
        if (c.equals("\r"))
            return null;
        
        TokenID tokType = getTokType(c);

        // No recognized token. Try to read a new one.
        if (tokType == null) {
            this.read = true;
            return null;
        }

        // String detection and reading.
        if (isCurrentToken(TokenID.TOK_STRING)) {
            if (tokType == TokenID.TOK_QUOTE)
                return endToken();

            this.cToken.append(c);
            return null;
        }

        // Comment trimming.
        else if (isCurrentToken(TokenID.TOK_COMMENT)) {
            if (tokType == TokenID.TOK_LINE_TERM) {
                this.read = false;
                return endToken();
            }

            this.cToken.append(c);
            return null;
        }

        // Word reading
        else if (isCurrentToken(TokenID.TOK_NAME)) {
            if (tokType != TokenID.TOK_CHAR) {
                this.read = false;
                return endToken();
            }

            this.cToken.append(c);
            return null;
        }

        this.read = true;

        // Minimal token processing needs to be done, so only one step will yield a result.
        switch (tokType) {
            case TOK_QUOTE: {
                startNewToken(TokenID.TOK_STRING);
                break;
            }
            case TOK_COMMENT: {
                startNewToken(TokenID.TOK_COMMENT);
                break;
            }
            case TOK_CHAR: {
                startNewToken(TokenID.TOK_NAME);
                this.cToken.append(c);
                break;
            }
            case TOK_EOF: {
                this.f.close();
                this.f = null;
                throwIfTokenDefined();
                return new Token(tokType, "EOF", this.lno);
            }
            // Fallthrough intended.
            case TOK_LINE_TERM: {
                this.lno++;
            }
            default: {
                // TOK_LINE_TERM
                // TOK_SPACE
                // TOK_PAREN_OPEN
                // TOK_PAREN_CLOSE
                // TOK_CURLY_OPEN
                // TOK_CURLY_CLOSE
                // TOK_QUOTE
                // TOK_DEREF
                // TOK_INTERNAL_UNIT
                // TOK_UNIT
                throwIfTokenDefined();
                return new Token(tokType, c, this.lno);
            }
        }

        return null;
    }

    private TokenID getTokType(String c) {
        TokenID t = tokTable.get(c);

        // Probably just a number or character.
        if (t != null)
            return t;

        // Unrecognized word.
        if (!isCurrentToken(TokenID.TOK_COMMENT) && !isCurrentToken(TokenID.TOK_STRING) &&
            !c.matches("[A-Za-z0-9]+") && !c.equals("_"))
            throw new BadParserTokenException("Got bad char: \"" + c + "\" (lno " + lno + ")");
        return TokenID.TOK_CHAR;
    }

    private void startNewToken(TokenID tokType) {
        throwIfTokenDefined();
        this.cToken = new TokenBuilder(tokType);
    }

    private Token endToken() {
        Token t = new Token(this.cToken.id, this.cToken.data, this.lno);
        this.cToken = null;
        return t;
    }

    private void throwIfTokenDefined() {
        if (this.cToken != null)
            throw new IllegalStateException("New directive found before last one terminated!");
    }

    private boolean isCurrentToken(TokenID tok) {
        return (this.cToken != null && this.cToken.id == tok);
    }

    private String readSrc(int l) {
        try {
            return f.read(l);
        }
        catch (IOException ie) {
            return "";
        }
    }
}

class BadParserTokenException extends RuntimeException {
    public BadParserTokenException(String message) {
        super(message);
    }
}
