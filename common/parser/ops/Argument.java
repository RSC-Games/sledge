package common.parser.ops;

import common.VariableState;
import common.parser.Token;
import common.parser.TokenID;
import java.util.ArrayList;

public class Argument {
    ArrayList<Token> tokens;

    public Argument(ArrayList<Token> tokens) {
        this.tokens = tokens;
    }

    public Argument(Token token) {
        this.tokens = new ArrayList<Token>();
        this.tokens.add(token);
    }
    
    public int getLineNo() {
        return this.tokens.get(0).lno;
    }

    /**
     * Need to implement for control structures to work.
     */
    public boolean evaluate() {
        // Expand all string variables for evaluation.
        this.parseVars();

        // For one-length token conditionals, we want a non-null value.
        if (this.tokens.size() == 1) {
            Token t = this.tokens.get(0);
            if (t.tok != TokenID.TOK_STRING) throw new RuntimeException("Error (lno " + t.lno 
                + "): Expected string value");

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

        System.out.println("Cannot evaluate longer conditions; not supported.");
        System.out.println(tokens);
        return false;
    }

    /**
     * Replace string dereferences with their values.
     */
    public void parseVars() {
        boolean deref = false;
        boolean parenOpen = false;

        for (int i = 0; i < this.tokens.size(); i++) {
            Token tok = this.tokens.get(i);

            if (tok.tok == TokenID.TOK_DEREF) {
                deref = true;
            }
            else if (tok.tok == TokenID.TOK_PAREN_OPEN) {
                if (!deref) throw new RuntimeException("Bad argument: " + tokens);
                parenOpen = true;
            }
            else if (deref && tok.tok == TokenID.TOK_NAME) {
                if (!parenOpen) throw new RuntimeException("Bad argument: " + tokens);
                Token newTok = new Token(TokenID.TOK_STRING, VariableState.get(tok.val), tok.lno);
                this.tokens.set(i, newTok);
            }
            else if (tok.tok == TokenID.TOK_PAREN_CLOSE) {
                if (!deref && !parenOpen) throw new RuntimeException("Bad argument: " + tokens);
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
    public String stringVal() {
        String out = "";
        boolean deref = false;
        boolean parenOpen = false;

        for (Token tok : tokens) {
            if (tok.tok == TokenID.TOK_DEREF) {
                deref = true;
            }
            else if (tok.tok == TokenID.TOK_PAREN_OPEN) {
                if (!deref) throw new RuntimeException("Error (lno " + tok.lno + "): Expected \"$\"");
                parenOpen = true;
            }
            else if (deref && tok.tok == TokenID.TOK_NAME) {
                if (!parenOpen) throw new RuntimeException("Error (lno " + tok.lno + "): Expected \"(\".");
                out += (out.equals("") ? VariableState.get(tok.val) : " " + VariableState.get(tok.val));
            }
            else if (tok.tok == TokenID.TOK_PAREN_CLOSE) {
                if (!deref && !parenOpen) throw new RuntimeException("Error (lno " + tok.lno + "): Missing name.");
                deref = false;
                parenOpen = false;
            }
            else {
                if (deref || parenOpen) throw new RuntimeException("Error (lno " + tok.lno + "): Expected variable terminator.");
                out += (out.equals("") ? tok.val : " " + tok.val);
            }
        }

        return out;
    }
    
    public String toString() {
        return stringVal();
    }
}
