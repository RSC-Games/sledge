package com.rsc_games.sledge.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;

import com.rsc_games.sledge.env.BuildEnvironment;
import com.rsc_games.sledge.lib.LogModule;

/**
 * Mostly a small convenience wrapper around the raw executable tree
 * output by the ExecTree parser. The parser was written in early 2024
 * and contains numerous bad design choices that may warrant a rewrite.
 */
public class TargetTree {
    private ExecTree tree;

    /**
     * Run the entire file processor from start to finish.
     * 
     * @param path The location of the config file to process.
     */
    public TargetTree(String path) throws IOException {
        Tokenizer lexer = new Tokenizer(path);

        lexer.tokenizeFile();
        ArrayList<Token> tokens = lexer.processTokens();

        // Turn the token stream into something resembling logical structure.
        CSTGenerator cst = new CSTGenerator(tokens);

        // Ensure all TOK_NAMEs that correspond to keywords are converted properly.
        cst.markKeywords();
        Tree builtCST = cst.buildCST();

        // NOTE: This doesn't actually generate a *true* AST but it is abstract
        // enough for our purposes. It requires a 3 step process to generate
        // a usable config.
        ASTGenerator ast = new ASTGenerator(builtCST);

        // Step 1: Purge whitespace and comments.
        ast.clean();
        //ast.printTree();

        // Step 2: Translate the CST tree into a hierarchical tree of operations.
        ast.analyze();

        // Step 3: Extract operations from the AST and build the interpretable script.
        this.tree = ast.buildExec();
    }

    public void execTarget(BuildEnvironment environment, String target) {
        this.tree.execTarget(environment.getVars(), target);
    }
    
    public void printTarget(String target) {
        this.tree.printTree(target);
    }

    /**
     * Print out the list of identified targets.
     */
    public void listTargets() {
        LogModule.warn("sledge", "Registered targets:");

        for (String target : this.tree.targets.keySet())
            LogModule.warn("sledge", "\t" + target);
    }

    /**
     * Attempt to extract the actual code at a given line within a file.
     * Assumes the file already exists when processing.
     * 
     * @param filename The file to extract from (should be ./hammer)
     * @param lineNumber The line index of the file (starts at 1)
     * @return The line of code in question.
     */
    public static String getCodeAtLine(String filename, int lineNumber) {
        FileReader reader;
        
        // NOTE: Can allocate up to a maximum of a 4 GB buffer (should never be an issue here).
        CharBuffer codebuf = CharBuffer.allocate((int)new File(filename).length());

        try {
            reader = new FileReader(filename);
            reader.read(codebuf);
            reader.close();
        }
        catch (IOException ie) {
            // Well... we tried.
            LogModule.critical("sledge", "i/o error during error reporting!");
            return "";
        }

        codebuf.position(0);
        String[] lines = codebuf.toString().split("\n");
        int lineIndex = (lineNumber == -1 ? lines.length: lineNumber) - 1;

        if (lineIndex > lines.length) {
            LogModule.warn("sledge", "unable to locate line " + lineNumber + " (index out of range)");
            return "";
        }

        return lines[lineIndex].stripLeading();
    }
}
