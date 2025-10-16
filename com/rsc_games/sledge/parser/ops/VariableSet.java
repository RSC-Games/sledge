package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;

import com.rsc_games.sledge.env.BuilderVars;

class VariableSet extends Operation {
    /**
     * Represents a target operation. A target contains a list of inner operations
     * and runs them when {@code execute} is called. A Target must never be nested.
     */
    public VariableSet(Opcode op, ArrayList<Argument> args) {
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
     * 
     * @param vars Current variable state (to add a new variable to)
     */
    public void execute(BuilderVars vars) {
        //System.out.println("Executing operation " + lineNo);
        vars.set(args.get(0).stringVal(vars), args.get(1).stringVal(vars));
    }
}