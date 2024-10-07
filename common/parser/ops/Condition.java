package common.parser.ops;

import java.util.ArrayList;

class Condition extends Operation {
    ArrayList<Operation> conditionalElements;

    /**
     * Represents a target operation. A target contains a list of inner operations
     * and runs them when {@code execute} is called. A Target must never be nested.
     */
    public Condition(Opcode op, ArrayList<Argument> args) {
        super(op, args);
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
     * args[0]: The conditional. 
     */
    public void execute() {
        assert this.conditionalElements != null: "Inner operations never set for target!";
        //System.out.println("Executing operation " + lineNo);

        if (!args.get(0).evaluate()) return;

        for (Operation op : conditionalElements) {
            op.execute();
        }
    }
}