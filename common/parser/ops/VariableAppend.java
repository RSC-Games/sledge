package common.parser.ops;

import common.VariableState;
import java.util.ArrayList;

class VariableAppend extends Operation {
    /**
     * Represents a target operation. A target contains a list of inner operations
     * and runs them when {@code execute} is called. A Target must never be nested.
     */
    public VariableAppend(Opcode op, ArrayList<Argument> args) {
        super(op, args);
    }

    /**
     * Allow different operations on branches.
     */
    public boolean isBranch() {
        return false;
    }

    /**
     * Set the inner operations of this operation.
     */
    public void setInner(ArrayList<Operation> inner) {}

    /**
     * Execute all operations further down in the tree.
     * Provided args in the list:
     * args[0]: The variable name
     * args[1]: The variable data to append.
     */
    public void execute() {
        //System.out.println("Executing operation " + lineNo);
        VariableState.append(args.get(0).stringVal(), args.get(1).stringVal());
    }
}