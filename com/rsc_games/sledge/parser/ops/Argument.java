package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;

import com.rsc_games.sledge.env.BuilderVars;
import com.rsc_games.sledge.parser.Token;

/**
 * Fundamental AST component. Represents an argument for a conditional branch
 * (such as an if statement).
 */
public class Argument {
    /**
     * Tokens list for eventual compilation and evaluation.
     */
    ArrayList<Token> tokens;

    /**
     * Prepares an argument for processing.
     * 
     * @param tokens Some fragment of the token stream.
     */
    public Argument(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public Argument(Token token) {
        this.tokens = new ArrayList<Token>();
        this.tokens.add(token);
    }
    
    /**
     * Identify what line this argument is.
     * 
     * @return The line number (all of them should be the same)
     */
    public int getLineNo() {
        return this.tokens.get(0).lno;
    }

    /**
     * Builds an immediately executable tree for a conditional statement
     * to evaluate. Does not perform any checks to ensure the supplied
     * argument should be translated.
     * 
     * @return The compiled and immediately executable conditional from
     *  this argument.
     */
    public ConditionLiteral compileCondition() {
        return new ConditionLiteral(this.tokens);
    }

    /**
     * Variable dereferencing works. Just takes the current values and converts them
     * to an output string.
     */
    // TODO: Resolving variables is clunky and badly implemented. It should not concatenate
    // everything into a long string.
    // Variable resolution should occur in this file (it's easy; just parse through the string
    // for names and replace those names with their equivalent values. Also return a string array)
    @Deprecated
    public String stringVal(BuilderVars vars) {
        // TODO: implement
        return "resolve not implemented";
    }

    /**
     * Return the raw reconstructed string value of this arguments list.
     * Does not perform any variable resolution, so it's useful for pre-execution
     * analysis of the token stream.
     * 
     * @return The raw reconstructed string value.
     */
    public String stringValNoResolve() {
        ArrayList<String> textRepr = new ArrayList<String>();
        this.tokens.forEach((x) -> { textRepr.add(x.val); });
        return String.join(" ", textRepr);
    }
    
    public String toString() {
        return stringValNoResolve();
    }
}
