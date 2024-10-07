package common.parser;

import common.parser.ops.Operation;
import java.util.ArrayList;

class CodeLine {
    ArrayList<Token> tokens = new ArrayList<Token>();
    TreeNode attachedNode;
    Operation op;  // Set and initialized at AST 

    public boolean isBranch = false;
    public boolean isStart = false;  // ignored unless {@code isBranch} is {@code true}.

    public CodeLine(ArrayList<Token> tokenStream) {
        this.tokens = parseLine(tokenStream);
    }

    public void addTreeNode(TreeNode node) {
        this.attachedNode = node;
    }

    public TreeNode getTreeNode() {
        return this.attachedNode;
    }

    public Operation extractOp() {
        return this.op;
    }

    public Operation generateInner() {
        ArrayList<Operation> inner = new ArrayList<Operation>();

        if (attachedNode != null) 
            inner = attachedNode.generateInner();
        
        if (op.isBranch())
            op.setInner(inner);

        return op;
    }

    private ArrayList<Token> parseLine(ArrayList<Token> tokenStream) {
        ArrayList<Token> tokList = new ArrayList<Token>();
        Token t;
        
        do {
            if (tokenStream.size() == 0) return tokList;
        
            t = tokenStream.remove(0);  // Get the first available token.
            //System.out.println("loaded " + t);
            
            // Allow traversing up and down the stack.
            if (t.tok == TokenID.TOK_CURLY_OPEN) {
                //System.out.println("Found {");
                isBranch = true;
                isStart = true;
            }
            else if (t.tok == TokenID.TOK_CURLY_CLOSE) {
                //System.out.println("Found }");
                isBranch = true;
                isStart = false;
            }

            tokList.add(t);
        }
        while (t.tok != TokenID.TOK_LINE_TERM);

        return tokList;
    }

    /**
     * Analyze this line's components and convert
     */
    public void analyze() {
        if (attachedNode != null)
            attachedNode.analyze();

        this.op = Operation.staticAnalyze(this.tokens);
    }

    /**
     * Clean this line's whitespace.
     */
    public void clean() {
        if (attachedNode != null)
            attachedNode.clean();
            
        ArrayList<Token> newTokens = new ArrayList<Token>();

        for (Token t : this.tokens) {
            if (t.tok != TokenID.TOK_LINE_TERM && t.tok != TokenID.TOK_COMMENT 
                && t.tok != TokenID.TOK_SPACE)
                newTokens.add(t);
        }

        this.tokens = newTokens;
    }

    /**
     * Empty lines are not necessary to retain.
     */
    public boolean isEmpty() {
        return this.tokens.size() == 0;
    }

    public String toString() {
        String s = "" + tokens;
        s += "\n\t" + attachedNode;
        return s;
    }

    public String toString(String indentStr) {
        String s = indentStr + tokens;
        
        if (attachedNode != null)
            s += indentStr + "\t" + attachedNode;
        
        return s;
    }
}
