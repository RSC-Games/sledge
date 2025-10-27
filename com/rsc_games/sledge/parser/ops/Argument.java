package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;

import com.rsc_games.sledge.env.BuilderVars;
import com.rsc_games.sledge.parser.ProcessingException;
import com.rsc_games.sledge.parser.Token;
import com.rsc_games.sledge.parser.TokenID;

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
     * Processes the argument stream during execution and returns a processed 
     * result.
     * 
     * @param vars Current variable state.
     * @return Whether the condition is true at the time it's called.
     */
    // TODO: Clunky evaluation. Replace with a functional system.
    @Deprecated
    public boolean evaluate(BuilderVars vars) {
        // Expand all string variables for evaluation.
        this.parseVars(vars);

        // Single item argument evaluation (like if (1) or if ($(VARIABLE))... no other elements).
        if (this.tokens.size() == 1) {
            Token t = this.tokens.get(0);

            //if (t.tok != TokenID.TOK_STRING)
            //    throw new ProcessingException(t.lno, "expected string value"); 

            System.out.println("condition value " + t.val);
            System.out.println("Condition eval: " + !t.val.equals(""));
            return !t.val.equals("");
        }

        // For longer conditionals a comparison of both sides will be necessary.
        else if (this.tokens.size() == 4) {
            Token lval = this.tokens.get(0);
            
            if (this.tokens.get(1).tok != TokenID.TOK_EQUALS || this.tokens.get(2).tok != TokenID.TOK_EQUALS)
                throw new RuntimeException("Error: Unrecognized conditional. Expected \"==\"");

            Token rval = this.tokens.get(3);
            System.out.println("Condition eval: " + lval.val.equals(rval.val));
            return lval.val.equals(rval.val);
        }

        System.out.println("(parser) Cannot evaluate longer conditions; not supported.");
        System.out.println("tokens list " + tokens);
        return false;
    }

    /**
     * Replaces variables with their values at the time or evaluation.
     * Forces the $() syntax for variables.
     */
    // Possibly one of the worst and ugliest classes I've ever written in my short life.
    @Deprecated
    public void parseVars(BuilderVars vars) {
        boolean deref = false;
        boolean parenOpen = false;

        for (int i = 0; i < this.tokens.size(); i++) {
            Token tok = this.tokens.get(i);

            if (tok.tok == TokenID.TOK_DEREF) {
                deref = true;
            }
            else if (tok.tok == TokenID.TOK_PAREN_OPEN) {
                if (!deref) 
                    throw new ProcessingException(tok.lno, "can't interpret token stream after (: " + tokens);
                
                parenOpen = true;
            }
            else if (deref && tok.tok == TokenID.TOK_NAME) {
                if (!parenOpen) 
                    throw new ProcessingException(tok.lno, "expected name in $(), got " + tokens);

                Token newTok = new Token(TokenID.TOK_STRING, vars.get(tok.val), tok.lno);
                this.tokens.set(i, newTok);
            }
            else if (tok.tok == TokenID.TOK_PAREN_CLOSE) {
                if (!deref && !parenOpen) 
                    throw new ProcessingException(tok.lno, "missing closing parenthesis in var resolution: " + tokens);
                
                deref = false;
                parenOpen = false;
            }
        }

        // Clean out the remaining string reference tokens.
        ArrayList<Token> tmpTokens = new ArrayList<Token>();

        for (Token tok : this.tokens) {
            if (tok.tok != TokenID.TOK_DEREF && tok.tok != TokenID.TOK_PAREN_OPEN
                && tok.tok != TokenID.TOK_PAREN_CLOSE)
                tmpTokens.add(tok);
        }

        this.tokens = tmpTokens;
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
        String out = "";
        boolean deref = false;
        boolean parenOpen = false;

        for (Token tok : tokens) {
            if (tok.tok == TokenID.TOK_DEREF) {
                deref = true;
            }
            else if (tok.tok == TokenID.TOK_PAREN_OPEN) {
                if (!deref) 
                    throw new ProcessingException(tok.lno, "missing \"$\" in var resolution: (todo: process var name)");
                
                parenOpen = true;
            }
            else if (deref && tok.tok == TokenID.TOK_NAME) {
                if (!parenOpen) 
                    throw new ProcessingException(tok.lno, "missing parenthesis around var resolution");

                out += (out.equals("") ? "" : " ") + vars.get(tok.val);
            }
            else if (tok.tok == TokenID.TOK_PAREN_CLOSE) {
                if (!deref && !parenOpen) 
                    throw new ProcessingException(tok.lno, "expected name within $(), got (todo: process var line)");
                
                deref = false;
                parenOpen = false;
            }
            else {
                if (deref || parenOpen) 
                    throw new ProcessingException(tok.lno, "missing closing parenthesis around var resolution");
                
                out += (out.equals("") ? "" : " ") + tok.val;
            }
        }

        return out;
    }

    @Deprecated
    public String stringVal__NoVarReplacement() {
        String out = "";
        boolean deref = false;
        boolean parenOpen = false;

        for (Token tok : tokens) {
            if (tok.tok == TokenID.TOK_DEREF) {
                deref = true;
            }
            else if (tok.tok == TokenID.TOK_PAREN_OPEN) {
                if (!deref)
                    System.out.println("shut the helly up nobody asked if there was a missing $"); 
                    //throw new ProcessingException(tok.lno, "missing \"$\" in var resolution: (todo: process var name)");
                
                parenOpen = true;
            }
            else if (deref && tok.tok == TokenID.TOK_NAME) {
                if (!parenOpen)
                    System.out.println("parser found name but is crying because no ( was found before.");
                    //throw new ProcessingException(tok.lno, "missing parenthesis around var resolution");

                out += (out.equals("") ? "" : " ") + tok.val;
            }
            else if (tok.tok == TokenID.TOK_PAREN_CLOSE) {
                if (!deref && !parenOpen)
                    System.out.println("parser thinks there's no name within the so called var resolution");
                    //throw new ProcessingException(tok.lno, "expected name within $(), got (todo: process var line)");
                
                deref = false;
                parenOpen = false;
            }
            else {
                if (deref || parenOpen)
                    System.out.println("some weird bug case occurred in broken, deprecated code? no waaay"); 
                    //throw new ProcessingException(tok.lno, "missing closing parenthesis around var resolution");
                
                out += (out.equals("") ? "" : " ") + tok.val;
            }
        }

        return out;
    }
    
    public String toString() {
        return stringVal__NoVarReplacement();
    }
}
