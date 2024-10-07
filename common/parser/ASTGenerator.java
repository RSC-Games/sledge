package common.parser;

public class ASTGenerator {
    Tree builtTree;

    public ASTGenerator(Tree tree) {
        this.builtTree = tree;
    }

    /**
     * Purge all whitespace entries in the given tree, and prepare it for
     * initial line-oriented AST parsing.
     */
    public void clean() {
        TreeNode root = builtTree.getRoot();
        root.clean();
    }

    /**
     * Analyze the currently trimmed tree and create a new tree of generic operations.
     */
    public void analyze() {
        TreeNode root = builtTree.getRoot();
        root.analyze();
    }
    
    public ExecTree buildExec() {
        return new ExecTree(builtTree);
    }
    
    /**
     * Show the current AST tree on screen.
     */
    public void printTree() {
        System.out.println("################## PRINTING AST TREE ####################");
        System.out.println(builtTree.getRoot());
        System.out.println("%%%%%%%%%%%%%%%%%%%%% END AST TREE %%%%%%%%%%%%%%%%%%%%%%");
    }
}
