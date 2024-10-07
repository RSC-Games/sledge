package common.parser;

import common.parser.ops.Operation;
import common.parser.ops.Opcode;
import java.util.ArrayList;

public class ExecTreeNode {
    ArrayList<Operation> operations;
    public final String name;

    public ExecTreeNode(Operation base) {
        if (base.op != Opcode.OP_TYPE_TARGET)
            throw new RuntimeException("Treenode op must be a target!");

        name = base.getArgument(0).stringVal();
        operations = new ArrayList<Operation>();
    }
    
    public void addOp(Operation op) {
        operations.add(op);
    }

    public void printTree() {
        for (Operation op : operations) {
            System.out.println(op);
        }
    }

    public void execute() {
        for (Operation op : operations) {
            op.execute();
        }
    }
}
