package com.rsc_games.sledge.parser;

import java.util.ArrayList;

import com.rsc_games.sledge.env.BuilderVars;
import com.rsc_games.sledge.parser.ops.Opcode;
import com.rsc_games.sledge.parser.ops.Operation;

public class ExecTreeNode {
    ArrayList<Operation> operations;
    public final String name;

    public ExecTreeNode(Operation base) {
        if (base.op != Opcode.OP_TYPE_TARGET)
            throw new ProcessingException(base.getLineNumber(), "processing error: expected target type for tree root, got " + base.op);

        name = base.getArgument(0).stringVal__NoVarReplacement();
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

    public void execute(BuilderVars vars) {
        for (Operation op : operations) {
            op.execute(vars);
        }
    }
}
