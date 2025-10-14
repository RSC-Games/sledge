package com.rsc_games.sledge.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Tokenizer {
    /**
     * Contains the entire recognized tokenizer language. It's a stretch
     * to call it a "language" but it's enough to get a recognizeable
     * token stream.
     */
    private static final HashMap<String, TokenID> tokTable;

    static {
       tokTable = new HashMap<String, TokenID>();

        tokTable.put("#", TokenID.TOK_COMMENT);
        tokTable.put("\n", TokenID.TOK_NEWLINE);
        tokTable.put(";", TokenID.TOK_SEMICOLON);
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
        // TODO: Parse &&, ||, !=, !
        // TODO: Join := into a single token.
    }

    FileReader fileReader;
    int lno = 1;

    TokenBuilder curToken;
    boolean readNextChar = true;
    String curTokData = "";

    /**
     * Create a tokenizer to parse a given input file.
     * 
     * @param f Provided text file to parse.
     */
    public Tokenizer(String fpath) throws IOException {
        this.fileReader = new FileReader(new File(fpath));
    }

    /**
     * Internal. Is there any text in the file to tokenize?
     * 
     * @return Whether any such text remains.
     */
    private boolean available() {
        return this.fileReader != null;
    }
    
    /**
     * Process all tokens in the entire table at once. Once this is called,
     * the tokenizer object is no longer useful and should no longer be
     * used.
     * 
     * @return All token-data pairs from the current location in the file
     *  to the end.
     */
    public ArrayList<Token> getAllTokens() {
        ArrayList<Token> tokens = new ArrayList<Token>();
        
        while (available()) {
            Token tok = getNextToken();
            tokens.add(tok);
        }
        
        return tokens;
    }

    /**
     * Read the next token from the input file.
     * @implNote This function modifies internal state, so it is inherently 
     *  NOT thread-safe!
     * 
     * @return The next token-data pair in the file.
     */
    public Token getNextToken() {
        Token nextToken = null;

        while (nextToken == null) {
            if (!available())
                return null;

            if (readNextChar) 
                curTokData = readNextChar();

            nextToken = processCharsToToken(curTokData);
        }
        
        return nextToken;
    }

    /**
     * Process the next character. Once a token has been fully processed,
     * it will be returned from this function. I should eventually remove
     * the reliance on object-global state.
     * 
     * @param nextChar The latest character read from the file.
     * @return A token, if we've finished processing one on this iteration.
     */
    private Token processCharsToToken(String nextChar) {
        // Trim CR (doesn't affect parsing other than causing issues) and
        // process the next character.
        if (nextChar.equals("\r"))
            return null;
        
        TokenID tokType = getTokType(nextChar);

        // This isn't a valid token. (Should this error?)
        // Just read another.
        if (tokType == null) {
            this.readNextChar = true;
            return null;
        }

        // String processing and content recovery.
        // TODO: Handle escape characters.
        if (isCurrentToken(TokenID.TOK_STRING)) {
            if (tokType == TokenID.TOK_QUOTE)
                return endToken();

            this.curToken.append(nextChar);
            return null;
        }

        // Discard comments (they don't serve any useful purpose for us)
        else if (isCurrentToken(TokenID.TOK_COMMENT)) {
            if (tokType == TokenID.TOK_NEWLINE) {
                this.readNextChar = false;
                return endToken();
            }

            this.curToken.append(nextChar);
            return null;
        }

        // Process a variable/symbol name/command. It's the parser's responsibility
        // to determine whether it's valid.
        else if (isCurrentToken(TokenID.TOK_NAME)) {
            if (tokType != TokenID.TOK_CHAR) {
                this.readNextChar = false;
                return endToken();
            }

            this.curToken.append(nextChar);
            return null;
        }

        // Above cases lose characters in certain edge scenarios. The below don't.
        this.readNextChar = true;

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
                this.curToken.append(nextChar);
                break;
            }
            case TOK_EOF: {
                throwIfTokenDefined();
                return new Token(tokType, "EOF", this.lno);
            }
            // Fallthrough intended.
            case TOK_NEWLINE: {
                this.lno++; // Allow easier error recovery and reporting.
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
                return new Token(tokType, nextChar, this.lno);
            }
        }

        return null;
    }

    /**
     * Determine the token type, if applicable.
     * 
     * @param nextChar The last character read from the file
     * @return The token type (if a valid alphanumeric character)
     */
    private TokenID getTokType(String nextChar) {
        TokenID type = tokTable.get(nextChar);

        // Probably just a number or character.
        if (type != null)
            return type;

        // Non-alphanumeric (probably doesn't make sense)
        if (!isCurrentToken(TokenID.TOK_COMMENT) && !isCurrentToken(TokenID.TOK_STRING) 
            && !nextChar.matches("[A-Za-z0-9]+") && !nextChar.equals("_")) {
            
            throw new ProcessingException(this.lno,
                String.format("parser error: unrecognized char \"%s\"", nextChar)
            );
        }

        return TokenID.TOK_CHAR;
    }

    /**
     * Prepare for processing and capturing new token data. Note that
     * one cannot already be being processed.
     * 
     * @param tokType The token type of the new token to process.
     */
    private void startNewToken(TokenID tokType) {
        throwIfTokenDefined();
        this.curToken = new TokenBuilder(tokType);
    }

    /**
     * Finish processing a token and mark the pipeline as available.
     * 
     * @return The finished token.
     */
    private Token endToken() {
        Token t = new Token(this.curToken.id, this.curToken.data, this.lno);
        this.curToken = null;
        return t;
    }

    /**
     * Prevent the processing of any new tokens if a previous one was still
     * being processed.
     */
    private void throwIfTokenDefined() {
        if (this.curToken != null) {
            throw new ProcessingException(this.lno,
                String.format("parser error: unterminated literal %s", 
                              this.curToken.data)
            );
        }
    }

    /**
     * Identify if the discovered token type is the same one as the actively
     * processed token.
     * 
     * @param tok Latest discovered token type.
     * @return Whether it's the same type as the current.
     */
    private boolean isCurrentToken(TokenID tok) {
        return (this.curToken != null && this.curToken.id == tok);
    }

    /**
     * Read the next character from the file, and close it if no more exist.
     * 
     * @return The last character read, or an empty string if the file is empty.
     */
    private String readNextChar() {
        try {
            char[] charBuf = new char[1];
            int ret = fileReader.read(charBuf);

            // File is empty.
            if (ret == -1) {
                fileReader.close();
                fileReader = null;
                return "";
            }

            return String.format("%c", charBuf[0]);
        }
        catch (IOException ie) {
            throw new RuntimeException("parser error: lost stream");
        }
    }
}
