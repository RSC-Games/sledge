package com.rsc_games.sledge.parser;

import java.util.ArrayList;

import com.rsc_games.sledge.parser.ops.Operation;

/**
 * Evil twin of the codeline system. Recursively holds child codelines, which can
 * have their own children, etc.
 */
class TreeNode {
    /**
     * Current indentation level of the tree (probably for debugging purposes)
     */
    static int indent = -1;

    /**
     * Codeline directly associated with this tree node. Necessary
     * for code generation.
     */
    CodeLine line;

    /**
     * All of the aforementioned child nodes.
     */
    ArrayList<CodeLine> childLines = new ArrayList<CodeLine>();

    /**
     * Prepare this node for CST generation.
     * 
     * @param parentLine Technically the associated line (not much of a parent)
     */
    public TreeNode(CodeLine parentLine) {
        this.line = parentLine;
    }

    /**
     * CST operation. Register another child line.
     * @param line
     */
    public void addLine(CodeLine line) {
        this.childLines.add(line);
    }

    /**
     * Get a list of this tree's direct children (via the codeline system)
     * @implNote This function does not recurse over the child trees.
     * 
     * @return A list of this tree's children.
     */
    public ArrayList<TreeNode> getSubTrees() {
        ArrayList<TreeNode> nodes = new ArrayList<TreeNode>();

        for (CodeLine line : this.childLines) {
            TreeNode node = line.getTreeNode();

            if (node != null) 
                nodes.add(node);
        }

        return nodes;
    }

    /**
     * AST operation. Recursively compile executable operations and build
     * a finally "executable" tree.
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
     * AST operation. First line of a direct subtree contains the target name.
     * Should never be called on any indirect child.
     */
    public CodeLine getTarget() {
        return this.childLines.get(0);
    }

    /**
     * AST operation. Purge all whitespace on this current node and subnodes.
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

    /**
     * AST operation. The bulk of the code generation work occurs here.
     */
    public void analyze() {
        // Tell each codeline to analyze itself.
        for (CodeLine cline : this.childLines) {
            cline.analyze();
        }
    }
    
    /**
     * AST operation. Generate the inner values for operation.
     */
    public ArrayList<Operation> generateInner() {
        ArrayList<Operation> inner = new ArrayList<Operation>();

        for (CodeLine cline : childLines) {
            Operation op = cline.generateInner();
            inner.add(op);
        }
        
        return inner;
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
