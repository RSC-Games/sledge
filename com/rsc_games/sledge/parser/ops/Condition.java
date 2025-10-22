package com.rsc_games.sledge.parser.ops;

import java.util.ArrayList;

import com.rsc_games.sledge.env.BuilderVars;
import com.rsc_games.sledge.parser.CodeLine;
import com.rsc_games.sledge.parser.ProcessingException;

class Condition extends Operation {
    ArrayList<Operation> conditionalElements;
    Condition connectedBranch;

    /** 
     * Any preceeding case is guaranteed to have been executed. Therefore it's
     * going to definitively have evaluated true or false.
     */
    boolean conditionMet = false;

    /**
     * Represents a target operation. A target contains a list of inner operations
     * and runs them when {@code execute} is called. A Target must never be nested.
     */
    public Condition(Opcode op, CodeLine previous, ArrayList<Argument> args) {
        super(op, args);
        String conditionalType = args.get(0).stringVal__NoVarReplacement();

        // Determine if a previous case can be linked to this.
        if (!conditionalType.equals("if") && previous != null && previous.isBranch)
            connectedBranch = (Condition)previous.getOp();

        // Ensure only legal combinations of if/else chains are permitted.
        if (!conditionalType.equals("if") && connectedBranch == null)
            throw new ProcessingException(lineNo,
                String.format("processing exception: %s without if", conditionalType));
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

        String condType = this.args.get(0).stringVal__NoVarReplacement();

        // Else only executes if the above case evaluated false.
        // TODO: Ensure else without if/elif is assessed at parse time.
        if (condType.equals("else") && conditionMet)
            return;

        if (!condType.equals("else") && !args.get(1).evaluate(vars))
            return;

        for (Operation op : conditionalElements)
            op.execute(vars);
    }
}