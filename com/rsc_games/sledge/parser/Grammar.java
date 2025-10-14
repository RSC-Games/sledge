package com.rsc_games.sledge.parser;

import java.util.ArrayList;

/**
 * Helper functions for validating the language grammar in the CST.
 * This enables better, more accurate error reporting and less internal
 * confusion while parsing.
 */
class Grammar {
    public static final ArrayList<String> reservedWords;
    public static final ArrayList<String> conditionals;

    static {
        reservedWords = new ArrayList<String>();
        conditionals = new ArrayList<String>();

        reservedWords.add("if");
        reservedWords.add("elif");
        reservedWords.add("else");

        conditionals.addAll(reservedWords);
    }

    // TODO: Have a private class variable contain a detailed error state from the below
    // functions if they return false, and allow callers to query this.

    /**
     * Determine if the given token list is a legal line.
     * This particular helper analyzes sequences with a TOK_NAME initial
     * token (like targets or variable ops).
     * Not all lines passed in must be legal, but any that breaks the rules
     * will trigger an exception.
     * 
     * @param tokens Token stream for this line.
     * @return Whether this is a valid line.
     * @throws ProcessingException If the grammar is permanently broken.
     */
    public static boolean isValidLine_TYPE_NAME(ArrayList<Token> tokens) {
        // The second token determines how we continue processing the line.
        Token firstToken = tokens.get(0);
        Token secondToken = tokens.get(1);

        // Guaranteed a target definition. ABSOLUTELY NO MORE TOKENS MUST BE PRESENT
        // OR THIS IS AN INVALID LINE!!!!!
        if (secondToken.tok == TokenID.TOK_CURLY_OPEN) {
            if (tokens.size() > 2)
                throw new ProcessingException(secondToken.lno, "target cannot take arguments");

            return true;
        }
        // Variable declaration. Must only have an lvalue, equal sign, and an rvalue.
        else if (secondToken.tok == TokenID.TOK_EQUALS) {
            // Doesn't have any operand. Technically not legal, but it's not a showstopper.
            if (tokens.size() < 3)
                return false;

            return validateRvalue(tokens, 2);
        }
        // Fundamentally similar to above, but due to different parsing must be handled in
        // its own case. This is a variable append operation, which parses its arguments
        // identically to the above.
        else if (secondToken.tok == TokenID.TOK_APPEND) {
            // Can't really do anything yet (can't finish the syntax checks)
            if (tokens.size() < 3)
                return false;

            // Variable append must be :=
            if (tokens.get(2).tok != TokenID.TOK_EQUALS)
                throw new ProcessingException(secondToken.lno, "illegal character in var append; expected :=");

            // No operands yet.
            if (tokens.size() < 4)
                return false;

            return validateRvalue(tokens, 3);
        }
        // Special case: Probably a missing semicolon.
        else if (secondToken.tok == TokenID.TOK_NAME || secondToken.tok == TokenID.TOK_UNIT 
                 || secondToken.tok == TokenID.TOK_INTERNAL_UNIT) {
            throw new ProcessingException(firstToken.lno, "missing semicolon at end of line");
        }
        // All other cases cannot become legal.
        else {
            throw new ProcessingException(firstToken.lno, "line not valid target/variable op");
        }
    }

    /**
     * Determine if the given token list is a legal line.
     * This particular helper analyzes sequences with a TOK_UNIT initial
     * token (any command/internal unit execution).
     * Not all lines passed in must be legal, but any that breaks the rules
     * will trigger an exception.
     * 
     * @param tokens Token stream for this line.
     * @return Whether this is a valid line.
     * @throws ProcessingException If the grammar is permanently broken.
     */
    public static boolean isValidLine_TYPE_UNIT(ArrayList<Token> tokens) {
        Token secondToken = tokens.get(1);

        // All of the units have the same syntax, so no special logic is required.

        // A unit name cannot be a literal or any non-name string.
        if (secondToken.tok != TokenID.TOK_NAME)
            throw new ProcessingException(tokens.get(0).lno, "expected unit name");

        // No arguments to scan.
        if (tokens.size() < 3)
            return true;

        return validateRvalue(tokens, 2);
    }

    /**
     * Variable set/append rvalues must be strings or concatenatable literals. Existing
     * variables are allowed too. Any other symbols are not.
     * 
     * @param tokens Input token list.
     * @param startIndex Starting offset to begin scanning (index 2 on varset, index 3
     *  on varappend)
     * @return Whether the rvalue is legal. There's no real case in which a currently
     *  illegal rvalue can become legal.
     */
    private static boolean validateRvalue(ArrayList<Token> tokens, int startIndex) {
        for (int i = startIndex; i < tokens.size(); ++i) {
            Token next = tokens.get(i);

            // Almost guaranteed missing semicolon (keywords are almost always the start of 
            // a new line)
            if (next.tok == TokenID.TOK_KEYWORD || next.tok == TokenID.TOK_UNIT 
                || next.tok == TokenID.TOK_INTERNAL_UNIT)
                throw new ProcessingException(tokens.get(i - 1).lno, "missing semicolon at end of line");

            if (next.tok != TokenID.TOK_NAME && next.tok != TokenID.TOK_STRING)
                throw new ProcessingException(next.lno, "illegal type in rvalue: " + next.tok);
        }

        return true;
    }

    /**
     * Determine if the given token list is a legal line.
     * This particular helper analyzes sequences with a TOK_KEYWORD initial
     * token (like control structures).
     * Not all lines passed in must be legal, but any that breaks the rules
     * will trigger an exception.
     * 
     * @param tokens Token stream for this line.
     * @return Whether this is a valid line.
     * @throws ProcessingException If the grammar is permanently broken.
     */
    public static boolean isValidLine_TYPE_KEYWORD(ArrayList<Token> tokens) {
        Token firstToken = tokens.get(0);
        //Token secondToken = tokens.get(1);

        if (conditionals.contains(firstToken.val)) {
            return validateConditional(firstToken.val, tokens);
        }
        else {
            // No other reserved words are currently supported.
            throw new ProcessingException(firstToken.lno, "processing error: unimplemented/unsupported keyword");
        }
    }

    /**
     * Ensure a conditional statement matches the format expected by the later
     * parser stages.
     * 
     * @param conditional The conditional type (if/elif/else)
     * @param tokens The tokens for the given line.
     * @return Whether the conditional properly matches the requirements.
     */
    private static boolean validateConditional(String conditional, ArrayList<Token> tokens) {
        Token secondToken = tokens.get(1);

        // Else has the smallest requirements list, and the easiest to validate.
        if (conditional.equals("else")) {
            if (tokens.size() > 2)
                throw new ProcessingException(secondToken.lno, "too many arguments for else statement");

            if (secondToken.tok != TokenID.TOK_CURLY_OPEN)
                throw new ProcessingException(tokens.get(0).lno, "missing \"{\" on conditional");

            return true;
        }

        // Not enough tokens for this to possibly be a full line of code.
        // The shortest requires if () {, which is 4 tokens.
        if (tokens.size() < 4)
            return false;

        // If/elif are identical. They must have parenthesis and the opening bracket.
        Token closeParenthesis = tokens.get(tokens.size() - 2);
        Token termBracket = tokens.get(tokens.size() - 1);

        if (closeParenthesis.tok != TokenID.TOK_PAREN_CLOSE)
            //throw new ProcessingException(secondToken.lno, "expected \")\" on conditional");
            return false;

        if (termBracket.tok != TokenID.TOK_CURLY_OPEN)
            //throw new ProcessingException(secondToken.lno, "missing \"{\" on conditional");
            return false;
        
        // TODO: still need to test actual internal condition to ensure its legal.
        return true;
    }
}
