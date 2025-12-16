package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;
import java.util.List;

import com.rsc_games.sledge.env.BuilderVars;
import com.rsc_games.sledge.parser.Grammar;
import com.rsc_games.sledge.parser.ProcessingException;
import com.rsc_games.sledge.parser.Token;
import com.rsc_games.sledge.parser.TokenID;

class ConditionLiteral {
    private ConditionFragment cond;

    public ConditionLiteral(ArrayList<Token> tokenStream) {
        System.out.println("processing tokens into condition");
        System.out.println("got args " + tokenStream);

        // Ensure the literal parser doesn't detect a parenthesis imbalance when no such
        // case exists.
        Token leadIn = tokenStream.get(0);

        if (leadIn.tok != TokenID.TOK_PAREN_OPEN)
            throw new ProcessingException(leadIn.lno, "processing error: missing \"(\" in conditional");

        System.out.println("\n************************** PARSING LINE *********************");
        ConditionFragment fragment = fullParseLiteral(tokenStream, "", 1, tokenStream.size() - 1);
        System.out.println("************************ END LINE PARSE *********************\n");

        System.out.println("top level parse: " + fragment);

        // TODO: Parse down a binary tree of boolean operators, such as &&/||/==, with operator
        // precedence.
        this.cond = fragment;
    }

    /**
     * Literal parsing process (recursive)
     * 
     * 1. Find the loosest associative conditional in the token stream.
     *  - Since this algorithm is recursive, any groups of parenthesis are skipped entirely.
     * 2. Split the token stream into low and high halves, and recursively process them.
     * 3. If this is a bottom case conditional (comparison, reflexive, or inverse case),
     *  then parse it into a usable form and return (base case).
     * 
     * @param tokenStream The original input stream for parsing (not modified).
     * @param indents (debug parameter)
     * @param lowIndex Lowest index for processing (bound inclusive)
     * @param highIndex Highest index for processing (bound exclusive)
     * @return The nested tree of parsed conditionals
     */
    ConditionFragment fullParseLiteral(ArrayList<Token> tokenStream, String indents, int lowIndex,
                                         int highIndex) {
        // Empty range. Don't scan.
        if (lowIndex == highIndex) {
            System.out.println("found empty range");
            return null; // true null here.
        }

        // Small range (probably just a literal)
        if (lowIndex == highIndex - 1) {
            System.out.println("hit base case reflexive");
            return new ConditionFragment(tokenStream.get(lowIndex), null, null);
        }

        System.out.printf("in range: %d to %d\n", lowIndex, highIndex);

        // Parenthesis reduction (ignore outermost parenthesis).
        if (tokenStream.get(lowIndex).tok == TokenID.TOK_PAREN_OPEN 
            && tokenStream.get(highIndex - 1).tok == TokenID.TOK_PAREN_CLOSE) {
            lowIndex++;
            highIndex--;

            System.out.println("performed range reduction on full paren group");
        }

        System.out.println("line " + reconstructSource(tokenStream.subList(lowIndex, highIndex)));


        // Get the operator with the highest precedence for this conditional and its ranges.
        OperatorTokenRanges area = getLoosestOperator(tokenStream, lowIndex, highIndex);
        System.out.println("operator for this sequence: " + area);

        System.out.println("got range0: " + reconstructSource(tokenStream.subList(area.lowRangelidx, area.lowRangehidx)));
        System.out.println("got range1: " + reconstructSource(tokenStream.subList(area.highRangelidx, area.highRangehidx)));

        // Parse given ranges recursively
        ConditionFragment lowRange = fullParseLiteral(tokenStream, indents + "\t", area.lowRangelidx, area.lowRangehidx);
        ConditionFragment highRange = fullParseLiteral(tokenStream, indents + "\t", area.highRangelidx, area.highRangehidx);

        System.out.println("low range text " + lowRange);
        System.out.println("high range text " + highRange);

        return new ConditionFragment(area.operator, lowRange, highRange);
    }

    /**
     * Trampoline to the inner evaluation function.
     * 
     * @param vars Full list of variables to resolve.
     * @return The evaluated condition.
     */
    public boolean evaluate(BuilderVars vars) {
        return this.cond.evaluate(vars);
    }

    @Deprecated
    private String reconstructSource(List<Token> in) {
        ArrayList<String> textRepr = new ArrayList<String>();
        in.forEach((x) -> { textRepr.add(x.val); });
        return String.join(" ", textRepr);
    }

    /**
     * Find the tightest associated operator for the given range, with ! being
     * the tightest binding, and &&/|| are the loosest binding.
     * This function will determine the closest binding operator within the given
     * conditional level, and return it. This function does not modify the given
     * array.
     * 
     * @param tokens The full token stream for this conditional.
     * @param lowRange Low range for the search (bound inclusive).
     * @param highRange High range for the search (bound exclusive).
     * @return The parsed operator and both ranges with their offsets.
     */
    private OperatorTokenRanges getLoosestOperator(ArrayList<Token> tokens, int lowRange, int highRange) {
        Token operator = null;

        int parenNesting = 0;

        for (int i = lowRange; i < highRange; i++) {
            Token token = tokens.get(i);

            // Skip anything within parenthesis when searching for least associative.
            if (token.tok == TokenID.TOK_PAREN_OPEN)
                parenNesting++;

            else if (token.tok == TokenID.TOK_PAREN_CLOSE)
                parenNesting--;

            // Nothing within parenthesis is loosely bound. Skip everything.
            // TODO: parenNesting < 0 is an instant error
            if (parenNesting > 0)
                continue;

            // Find loosest associative conditional (slowly relax from tightest to loosest
            // if a looser one is found, and assume left to right associativity)

            // This probably is a literal/string/var so ignore.
            if (!Grammar.conditionalLiterals.contains(token.tok)) {
                // TODO: If this isn't a name or string either, it's an error.
                //System.out.println("found illegal character in ")

                continue; // No found conditional.
            }

            // Move from tightest to closest binding - loosest is an instant capture due to the left
            // to right evaluation order.

            // Tightest binding and therefore least important here (also only binds to the right).
            if (operator == null && token.tok == TokenID.TOK_COND_NOT)
                operator = token;

            // Comparison binds both sides but is closer binding than the boolean conjunctions.
            // (is a member of <=, >=, ==, !=, <, >)
            else if (Grammar.comparisonLiterals.contains(token.tok))
                operator = token;

            // ||/&& are the loosest binding, and are an automatic return, since nothing binds looser.
            else if (Grammar.conditionalLiterals.contains(token.tok) && !Grammar.comparisonLiterals.contains(token.tok)
                     && token.tok != TokenID.TOK_COND_NOT) {
                int lowRangeHidx = i;
                int highRangeLidx = i+1;
                return new OperatorTokenRanges(token, lowRange, lowRangeHidx, highRangeLidx, highRange);
            }
        }

        // Probably a reflexive operation. Nothing left to search for.
        if (operator == null)
            return new OperatorTokenRanges(operator, lowRange, highRange, 0, 0);

        int opIndex = tokens.indexOf(operator);
        return new OperatorTokenRanges(operator, lowRange, opIndex, opIndex + 1, highRange);
    }

    /**
     * Intermediate parsing of conditionals to make them evaluatable.
     */
    private class ConditionFragment {
        Token operator;
        ConditionFragment lvalue;
        ConditionFragment rvalue;

        ConditionFragment(Token op, ConditionFragment lvalue, ConditionFragment rvalue) {
            this.operator = op;
            this.lvalue = lvalue;
            this.rvalue = rvalue;
        }

        public boolean evaluate(BuilderVars vars) {
            System.out.println("attempting to evaluate conditional " + operator);

            // For evaulations like (if (TOKEN_NAME)), their values don't matter.
            if (operator.tok == TokenID.TOK_NAME) {
                boolean defined = vars.exists(operator.val);
                System.out.println("var is defined: " + defined);

                return true;
            }

            // TODO: Proper recursive operator evaluation.
            if (lvalue != null)
                lvalue.evaluate(vars);
            if (rvalue != null)
                rvalue.evaluate(vars);

            return false;
        }

        public String toString() {
            return toString("");
        }

        String toString(String indent) {
            String indents = "op %s\n" + indent + "\tlval %s\n" + indent + "\trval %s\n"; 
            return String.format(indents, 
                operator, 
                lvalue != null ? lvalue.toString(indent + "\t") : "null", 
                rvalue != null ? rvalue.toString(indent + "\t") : "null"
            );
        }
    }

    private class OperatorTokenRanges {
        // Interval: [lowRange, highRange)
        public final int lowRangelidx;
        public final int lowRangehidx;
        public final int highRangelidx;
        public final int highRangehidx;
        public final Token operator;

        public OperatorTokenRanges(Token operator, int lrLidx, int lrHidx, int hrLidx, int hrHidx) {
            this.lowRangelidx = lrLidx;
            this.lowRangehidx = lrHidx;
            this.highRangelidx = hrLidx;
            this.highRangehidx = hrHidx;
            this.operator = operator;

            assert (this.lowRangelidx <= this.lowRangehidx): String.format("illegal lr: lidx %d hidx %d", lowRangelidx, lowRangehidx);
            assert (this.highRangelidx <= this.highRangehidx): String.format("illegal hr: lidx %d hidx %d", highRangelidx, highRangehidx);
            //assert (this.lowRangehidx <= this.highRangelidx): String.format("lr must be <= hr");
        }

        public String toString() {
            return String.format("lr_lidx %d lr_hidx %d hr_lidx %d hr_hidx %d op %s", 
                lowRangelidx, lowRangehidx, highRangelidx, highRangehidx, operator);
        }
    }
}
