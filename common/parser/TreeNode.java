package common.parser;

import java.util.ArrayList;
import common.parser.ops.Operation;

class TreeNode {
    // Chain these calls to create codelines and a tree of a bunch of
    // found elements.
    static int indent = -1;
    CodeLine line;
    ArrayList<CodeLine> childLines = new ArrayList<CodeLine>();

    public TreeNode(CodeLine parentLine) {
        this.line = parentLine;
    }

    public void addLine(CodeLine line) {
        this.childLines.add(line);
    }

    public ArrayList<TreeNode> getSubTrees() {
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();

        for (CodeLine line : this.childLines) {
            TreeNode node = line.getTreeNode();
            if (node != null) nodes.add(node);
        }

        return nodes;
    }

    /**
     * Convert this current node to a recursive executable tree.
     */
    public ExecTreeNode toExecTree() {
        ExecTreeNode node = new ExecTreeNode(line.extractOp());

        for (int i = 0; i < this.childLines.size(); i++) {
            this.childLines.get(i).generateInner();
            node.addOp(this.childLines.get(i).extractOp());
        }
        
        return node;
    }

    /**
     * Generally the target directive will be the first line of the
     * root trees.
     */
    public CodeLine getTarget() {
        return this.childLines.get(0);
    }

    /**
     * Generate the inner values for operation.
     */
    public ArrayList<Operation> generateInner() {
        ArrayList<Operation> inner = new ArrayList<Operation>();

        for (CodeLine cline : childLines) {
            Operation op = cline.generateInner();
            inner.add(op);
        }
        
        return inner;
    }

    /**
     * Purge all whitespace on this current node and subnodes.
     */
    public void clean() { 
        ArrayList<CodeLine> lines = this.childLines;
        this.childLines = new ArrayList<CodeLine>();

        // Clean and erase empty lines.       
        for (CodeLine cline : lines) {
            cline.clean();
            if (!cline.isEmpty())
                this.childLines.add(cline);
        }
    }

    public void analyze() {
        // Tell each codeline to analyze itself.
        for (CodeLine cline : this.childLines) {
            cline.analyze();
        }
    }

    public String toString() {
        String s = "";
        indent++;
        for (CodeLine cline : childLines) {
            s += "\n" + cline.toString(indentStr(indent));
        }
        indent--;

        return s;
    }

    String indentStr(int indent) {
        String s = "";
        for (int i = 0; i < indent; i++) s += "\t";
        return s;
    }
}
