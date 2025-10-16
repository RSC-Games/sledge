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
        tokTable.put(":", TokenID.TOK_COLON);
        tokTable.put("=", TokenID.TOK_EQUALS);
        tokTable.put("%", TokenID.TOK_INTERNAL_UNIT);
        tokTable.put("@", TokenID.TOK_UNIT);

        tokTable.put("<", TokenID.TOK_COND_LESS_THAN);
        tokTable.put(">", TokenID.TOK_COND_GREATER_THAN);
        tokTable.put("|", TokenID.TOK_PIPE);
        tokTable.put("&", TokenID.TOK_AMPERSAND);
        tokTable.put("!", TokenID.TOK_COND_NOT);
    }

    private ArrayList<Token> tokenStream;
    private FileReader fileReader;

    // Internal token processing state. Should be moved to a function.
    private TokenBuilder curToken;
    private boolean readNextChar = true;
    private boolean escapeEscaped = false;
    private String currentChar = "";
    private int lno = 1;

    /**
     * Create a tokenizer to parse a given input file.
     * 
     * @param f Provided text file to parse.
     */
    public Tokenizer(String fpath) throws IOException {
        this.fileReader = new FileReader(new File(fpath));
        this.tokenStream = new ArrayList<Token>();
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
     * used, other than for simplifying the token stream.
     */
    public void tokenizeFile() {
        ArrayList<Token> tokens = new ArrayList<Token>();
        
        while (available()) {
            Token tok = getNextToken();
            tokens.add(tok);
        }

        this.tokenStream.addAll(tokens);
    }

    /**
     * Second pass. Go through the previously generated token stream and perform
     * "token fusion". This reduces the complexity of parsing the token stream
     * down the line.
     * 
     * @return The list of processed tokens.
     */
    public ArrayList<Token> processTokens() {
        ArrayList<Token> processedTokens = new ArrayList<Token>();

        // Longest mergeable sequence is 2 tokens. Look ahead one token
        // and attempt to merge if possible. Otherwise, slide the window
        // ahead one token and keep trying.
        for (int i = 0; i < tokenStream.size(); i++) {
            Token nextToken = tokenStream.get(i);

            // Don't attempt to merge a non-mergeable token (most of them).
            if (!Grammar.mergeables.contains(nextToken.tok)) {
                processedTokens.add(nextToken);
                continue;
            }

            // Boolean conjunctions (like ||, &&) are handled separately.
            boolean isConjunction = nextToken.tok == TokenID.TOK_PIPE || nextToken.tok == TokenID.TOK_AMPERSAND;

            // Locate the next mergeable candidate.
            Token candidate = getNextViableToken(i);

            // No merge to be done.
            if (candidate == null) {
                processedTokens.add(nextToken);
                continue;
            }

            int candidateIndex = tokenStream.indexOf(candidate); 
            TokenID mergeType = determineMergeType(nextToken);

            // Each token can only bind to a very specific set of characters.
            // However, all mergeables can bind to an equal sign.
            if (!isConjunction && candidate.tok == TokenID.TOK_EQUALS) {
                Token merged = new Token(mergeType, nextToken.val + candidate.val, nextToken.lno);
                processedTokens.add(merged);
                i = candidateIndex;
                continue;
            }

            // Conjunctions can only bind to another of the same type as them.
            if (isConjunction && nextToken.tok == candidate.tok) {
                Token merged = new Token(mergeType, nextToken.val + candidate.val, candidateIndex);
                processedTokens.add(merged);
                i = candidateIndex;
                continue;
            }

            // Can't merge this token as it doesn't adhere to the specified rules.
            throw new ProcessingException(nextToken.lno, "parser error: invalid syntax");
        }
        
        return processedTokens;
    }

    /**
     * Find the next viable token after the given index for a merge. This function
     * implicitly ignores whitespace and comments. If a line terminator is found,
     * immediately bail out (since that's illegal).
     * 
     * @param offset Current token index.
     * @return The next viable token for a potential merge.
     */
    private Token getNextViableToken(int offset) {
        for (int i = offset + 1; i < tokenStream.size(); i++) {
            Token candidate = tokenStream.get(i);

            // Skip whitespace and non-code tokens
            if (Grammar.whitespace.contains(candidate.tok))
                continue;

            // If we hit a literal that's probably another term. Likely isn't any merge
            // to be done.
            if (candidate.tok == TokenID.TOK_STRING || candidate.tok == TokenID.TOK_NAME)
                return null;

            // Only a few character types are mergeable.
            if (!Grammar.mergeables.contains(candidate.tok))
                throw new ProcessingException(tokenStream.get(offset).lno, "parser error: invalid syntax");

            return candidate;
        }

        throw new ProcessingException(tokenStream.get(offset).lno, "parser error: reached EOF while parsing");
    }

    /**
     * Determine the merge type of the given input tokens. 
     * 
     * @param nextToken The token to (soon) be merged
     * @return The new, merged, type of the token.
     */
    private TokenID determineMergeType(Token nextToken) {
        // NOTE: Despite this solution being ugly, there's a reason I opted for a switch
        // statement, rather than using a LUT.
        switch (nextToken.tok) {
            case TOK_EQUALS:
                return TokenID.TOK_COND_EQUIVALENT;
            case TOK_COLON:
                return TokenID.TOK_APPEND;
            case TOK_PIPE:
                return TokenID.TOK_COND_OR;
            case TOK_AMPERSAND:
                return TokenID.TOK_COND_AND;
            case TOK_COND_LESS_THAN:
                return TokenID.TOK_COND_LESS_THAN_OR_EQUAL;
            case TOK_COND_GREATER_THAN:
                return TokenID.TOK_COND_GREATER_THAN_OR_EQUAL;
            case TOK_COND_NOT:
                return TokenID.TOK_COND_NOT_EQUIVALENT;
            default:
                throw new RuntimeException("internal error: unexpected mergeable token " + nextToken);
        }
    }

    /**
     * Read the next token from the input file.
     * @implNote This function modifies internal state, so it is inherently 
     *  NOT thread-safe!
     * 
     * @return The next token-data pair in the file.
     */
    private Token getNextToken() {
        Token nextToken = null;

        while (nextToken == null) {
            if (!available())
                return null;

            if (readNextChar) 
                currentChar = readNextChar();

            nextToken = processCharsToToken(currentChar);
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
        if (isCurrentToken(TokenID.TOK_STRING)) {
            String lastChar = curToken.getLastChar();

            if (tokType == TokenID.TOK_QUOTE) {
                if (!lastChar.equals("\\") || escapeEscaped)
                    return endToken();

                // Escape isn't actually part of the output.
                this.curToken.replaceLastChar(nextChar);
                return null;
            }

            // Strings are not allowed to continue past the end of a new line generally.
            if (tokType == TokenID.TOK_NEWLINE) {
                if (!lastChar.equals("\\") || escapeEscaped)
                    throw new ProcessingException(lno, "parser error: unterminated string literal");

                lno++; // Fix broken line number tracking.
                this.curToken.replaceLastChar(nextChar);
                return null;
            }

            // Prevent weird edge case where a \" is escaped properly but there are mismatched quotes.
            if (tokType == TokenID.TOK_EOF)
                throw new ProcessingException(lno, "parser error: reached EOF while parsing string");

            // Handle escaping escape characters. Determined below the escape case to reduce
            // logic complexity.
            escapeEscaped = false;
            if (currentChar.equals("\\") && lastChar.equals("\\")) {
                escapeEscaped = true;
                return null;
            }

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
