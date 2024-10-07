package common.parser.ops;

import common.VariableState;
import modules.Modules;
import java.util.ArrayList;

class UnitExecutor extends Operation {
    String unitName;

    /**
     * Represents a target operation. A target contains a list of inner operations
     * and runs them when {@code execute} is called. A Target must never be nested.
     */
    public UnitExecutor(Opcode op, ArrayList<Argument> args) {
        super(op, args);
        this.unitName = args.get(0).stringVal();
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
     * args[0]: Operation
     * args[1]: Arguments to pass in.
     */
    public void execute() {
        String cmdline = args.get(1).stringVal();
        //System.out.println("Executing operation " + lineNo);
        int retcode = Modules.tryRun(unitName, cmdline);

        if (retcode != 0)
            throw new RuntimeException("Unit " + unitName + ", cmdline \"" + cmdline + "\" (lno " + lineNo + ") failed to execute.");
    }
}