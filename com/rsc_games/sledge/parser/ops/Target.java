package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;

class Target extends Operation {
    ArrayList<Operation> inner;
    String name;

    /**
     * Represents a target operation. A target contains a list of inner operations
     * and runs them when {@code execute} is called. A Target must never be nested.
     */
    public Target(Opcode op, ArrayList<Argument> args) {
        super(op, args);
        this.name = args.get(0).stringVal();
    }
    
    /**
     * Allow different operations on branches.
     */
    public boolean isBranch() {
        return true;
    }

    /**
     * Set the inner operations of this operation.
     */
    public void setInner(ArrayList<Operation> inner) {
        this.inner = inner;
    }

    /**
     * Execute all operations further down in the tree.
     */
    public void execute() {
        assert this.inner != null: "Inner operations never set for target!";

        for (Operation op : inner) {
            op.execute();
        }
    }
}