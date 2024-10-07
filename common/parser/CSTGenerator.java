package common.parser;

import java.util.ArrayList;

class CSTGenerator {
    ArrayList<Token> tokenStream;

    public CSTGenerator(ArrayList<Token> tokenStream) {
        this.tokenStream = tokenStream;
    }

    /**
     * Take an input token stream and convert it into a hierarchical concrete
     * syntax tree.
     */
    public Tree buildCST() {
        TreeNode base = new TreeNode(null);
        buildCST0(base);
        Tree cstTree = new Tree(base);
        
        return cstTree;
    }

    void buildCST0(TreeNode node) {
        while (tokenStream.size() > 0) {
            CodeLine line = new CodeLine(tokenStream);
            //System.out.println("line " + line);

            // Just get another line.
            if (!line.isBranch) {
                node.addLine(line);
                continue;
            }

            // Go up or down the stack.
            if (line.isStart) {
                TreeNode newNode = new TreeNode(line);
                line.addTreeNode(newNode);
                buildCST0(newNode);
                node.addLine(line);
            }
            else return;
        }
    }
}