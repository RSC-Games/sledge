package common.parser.ops;

import java.util.ArrayList;
import common.parser.Token;
import common.parser.TokenID;

public abstract class Operation {
    public final Opcode op;
    protected ArrayList<Argument> args;
    protected int lineNo;

    protected Operation(Opcode op, ArrayList<Argument> args) {
        this.op = op;
        this.args = args;

        if (args.size() > 0)
            this.lineNo = args.get(0).getLineNo();
    }

    public Argument getArgument(int i) {
        return this.args.get(i);
    }

    /**
     * Perform whatever is stored in this operation.
     */
    public abstract void execute();

    /**
     * Allow different operations on branches.
     */
    public abstract boolean isBranch();

    /**
     * Set inner (only for branches)
     */
    public abstract void setInner(ArrayList<Operation> ops);

    public String toString() {
        return "lno: " + lineNo + " " + op + " args: " + args;
    }
    
    /**
     * Analyze a given code line and attempt to guess what operation it is performing.
     * In the event an operation cannot be guessed, that's an error.
     * 
     * @param line Line tokens
     */
    public static Operation staticAnalyze(ArrayList<Token> line) {        
        return getOperationType(line);
    }

    /**
     * Helper. Guesses the operation performed by a line.
     * 
     * @param line Provided line of tokens.
     * @return Detected operation.
     */
    private static Operation getOperationType(ArrayList<Token> line) {
        /**
         * Parsing requirements for each operation (sorted by test order)
         * OP_TYPE_TARGET: tokens required: <target name> {
         * OP_TYPE_COND: tokens required: if (<cond>) {
         * OP_TYPE_VARAPP: tokens required: <varname> := <value>
         * OP_TYPE_VARSET: tokens required: <varname> = <value>
         * OP_TYPE_UNIT_INTERNAL: tokens required: %<unit name>
         * OP_TYPE_CMD: tokens required: @<cmd>
         */
        ArrayList<Argument> out = new ArrayList<Argument>();
        
        if (isTarget(line, out))
            return new Target(Opcode.OP_TYPE_TARGET, out);
        else if (isCondition(line, out))
            return new Condition(Opcode.OP_TYPE_COND, out);
        else if (isVarApp(line, out))
            return new VariableAppend(Opcode.OP_TYPE_VARAPP, out);
        else if (isVarSet(line, out))
            return new VariableSet(Opcode.OP_TYPE_VARSET, out);
        else if (isUnit(line, out))
            return new UnitExecutor(Opcode.OP_TYPE_UNIT_INTERNAL, out);
        else if (isCmd(line, out))
            return new ExternalCmd(Opcode.OP_TYPE_CMD, out);

        throw new RuntimeException("Failed to parse line. Tokens: " + line);
    }

    /**
     * Helper. Detects if the input line follows the pattern "<name> {"
     */
    private static boolean isTarget(ArrayList<Token> line, ArrayList<Argument> out) {
        if (line.size() == 2 && line.get(0).tok == TokenID.TOK_NAME && line.get(1).tok == TokenID.TOK_CURLY_OPEN) {
            out.add(new Argument(line.get(0)));
            return true;
        }
        return false;
    }

    /**
     * Helper. Detects if the input line follows the pattern "if (<cond>) {"
     */
    private static boolean isCondition(ArrayList<Token> line, ArrayList<Argument> out) {
        Token keyword = line.get(0);
        // line arg 1 should be a (. If it's not, then that's invalid syntax.
        ArrayList<Token> arg = new ArrayList<Token>();
        Token term = line.get(line.size() - 1);

        if (keyword.tok == TokenID.TOK_NAME && keyword.val.equals("if") && line.get(1).tok == TokenID.TOK_PAREN_OPEN
            && line.get(line.size() - 2).tok == TokenID.TOK_PAREN_CLOSE && term.tok == TokenID.TOK_CURLY_OPEN) {
            
            ArrayList<Token> condition = parseArg(line, 2, line.size() - 2);
            out.add(new Argument(condition));
            return true;
        }
        return false;
    }

    /**
     * Helper. Detects if the input line follows the pattern "<varname> := <val>"
     */
    private static boolean isVarApp(ArrayList<Token> line, ArrayList<Argument> out) {
        Token name = line.get(0);
        Token op = line.get(1);

        if (line.size() >= 4 && op.tok == TokenID.TOK_APPEND && line.get(2).tok == TokenID.TOK_EQUALS) {
            ArrayList<Token> vardata = parseArg(line, 3, line.size());
            out.add(new Argument(name));  // Variable name
            out.add(new Argument(vardata));  // Data to join (joined by " ")
            return true;
        }
        return false;
    }

    /**
     * Helper. Detects if the input line follows the pattern "<varname> = <val>"
     */
    private static boolean isVarSet(ArrayList<Token> line, ArrayList<Argument> out) {
        Token name = line.get(0);
        Token op = line.get(1);

        if (line.size() >= 3 && op.tok == TokenID.TOK_EQUALS) {
            ArrayList<Token> vardata = parseArg(line, 2, line.size());
            out.add(new Argument(name));
            out.add(new Argument(vardata));
            return true;
        }
        return false;
    }

    /**
     * Helper. Detects if the input line follows the pattern "%<unitname> <args>"
     */
    private static boolean isUnit(ArrayList<Token> line, ArrayList<Argument> out) {        
        if (line.size() >= 2 && line.get(0).tok == TokenID.TOK_INTERNAL_UNIT && line.get(1).tok == TokenID.TOK_NAME) {
            ArrayList<Token> args = parseArg(line, 2, line.size());
            out.add(new Argument(line.get(1)));  // Unit name.
            out.add(new Argument(args));
            return true;
        }
        return false;
    }

    /**
     * Helper. Detects if the input line follows the pattern "@<cmd> <args>"
     */
    private static boolean isCmd(ArrayList<Token> line, ArrayList<Argument> out) {
        if (line.size() >= 2 && line.get(0).tok == TokenID.TOK_UNIT && line.get(1).tok == TokenID.TOK_NAME) {
            ArrayList<Token> args = parseArg(line, 2, line.size());
            out.add(new Argument(line.get(0))); // Cmd name.
            out.add(new Argument(args));
            return true;
        }
        return false;
    }

    /**
     * Parse an arg from given array bounds.
     */
    private static ArrayList<Token> parseArg(ArrayList<Token> line, int start, int end) {
        ArrayList<Token> arg = new ArrayList<Token>();

        for (int i = start; i < end; i++) {
            arg.add(line.get(i));
        }
        return arg;
    }
}
