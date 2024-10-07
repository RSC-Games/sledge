package common.parser;

import java.util.ArrayList;

public class BuildConfig {
    ExecTree tree;

    public BuildConfig(String path) {
        Tokenizer lexer = new Tokenizer(path);

        // Take the input file stream and tokenize it.
        ArrayList<Token> tokens = lexer.getAllTokens();

        // Build a CST representing the input file structure.
        CSTGenerator cst = new CSTGenerator(tokens);
        Tree builtCST = cst.buildCST();

        // Parse the CST down into an AST.
        ASTGenerator ast = new ASTGenerator(builtCST);
        ast.clean();  // Step 1: Purge whitespace and comments.
        //ast.printTree();
        ast.analyze();  // Step 2: Translate the CST tree into a hierarchical tree of operations.
        ExecTree execTree = ast.buildExec();  // Step 3: Extract operations from the AST and build the interpretable script.
        this.tree = execTree;
    }

    public ExecTree getExecutableTree() {
        return this.tree;
    }
    
    public void printTarget(String target) {
        this.tree.printTree(target);
    }
}
