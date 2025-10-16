package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;

import com.rsc_games.sledge.env.BuilderVars;
import com.rsc_games.sledge.parser.CodeLine;

class Condition extends Operation {
    ArrayList<Operation> conditionalElements;
    Condition connectedBranch;

    /**
     * Represents a target operation. A target contains a list of inner operations
     * and runs them when {@code execute} is called. A Target must never be nested.
     */
    public Condition(Opcode op, CodeLine previous, ArrayList<Argument> args) {
        super(op, args);

        if (previous != null && previous.isBranch)
            connectedBranch = (Condition)previous.getOp();
    }

    /**
     * Allow different operations on branches.
     */
    public boolean isBranch() {
        return true;
    }

    /**
     * Set inner elements for operation.
     */
    public void setInner(ArrayList<Operation> conditionalElements) {
        this.conditionalElements = conditionalElements;
    }

    /**
     * Execute all operations further down in the tree.
     * Provided args in the list:
     * args[0]: Conditional type.
     * args[1 to n]: The conditional. 
     * 
     * @param vars (unused)
     */
    // TODO: Change execution behavior with if/elif/else chain.
    // TODO: failed test case test_missing_paren_open
    public void execute(BuilderVars vars) {
        assert this.conditionalElements != null: "Inner operations never set for target!";

        System.out.println(args + " has previous branch " + this.connectedBranch);

        if (!args.get(1).evaluate(vars))
            return;

        for (Operation op : conditionalElements)
            op.execute(vars);
    }
}