package com.rsc_games.sledge.parser;

import java.util.ArrayList;

/**
 * Concrete syntax tree builder. I think when I wrote these I was probably
 * just slinging jargon around because I'm not sure where any "concrete"
 * tree exists.
 */
class CSTGenerator {
    /**
     * Fully processed token stream from the tokenizer.
     */
    ArrayList<Token> tokenStream;

    /**
     * Allows detection of mismatched curly brackets.
     */
    int branchDepth = 0;

    /**
     * Not sure why there's an entire constructor just to do this but...
     * okay...?
     * 
     * @param tokenStream Finalized stream from the tokenizer
     */
    public CSTGenerator(ArrayList<Token> tokenStream) {
        this.tokenStream = tokenStream;
    }

    /**
     * Find all keywords and mark them as TOK_KEYWORD instead
     * of TOK_NAME.
     */
    public void markKeywords() {
        for (int i = 0; i < tokenStream.size(); i++) {
            Token token = tokenStream.get(i);

            if (token.tok == TokenID.TOK_NAME)
                tokenStream.set(i, markTokenIfKeyword(token));
        }
    }

    /**
     * Determine if the provided token is a reserved word, or keyword.
     * If it is, since tokens are immutable, it must be remade with the
     * reserved word flag.
     * 
     * @param token Token of the NAME type.
     * @return A new token if it is a keyword, otherwise the original.
     */
    private Token markTokenIfKeyword(Token token) {
        if (!Grammar.reservedWords.contains(token.val))
            return token;

        return new Token(TokenID.TOK_KEYWORD, token.val, token.lno);
    }

    /**
     * Take an input token stream and convert it into a hierarchical concrete
     * syntax tree.
     */
    public Tree buildCST() {
        Tree cstTree = new Tree();
        buildCST0(cstTree.getRoot());

        // Too many opening curlies.
        if (this.branchDepth > 0)
            throw new ProcessingException(-1, "reached EOF while parsing (not enough close braces)");
        else if (this.branchDepth < 0)
            throw new ProcessingException(tokenStream.get(0).lno, "too many closing brackets");
        
        return cstTree;
    }

    /**
     * Build the entire CST with a provided root node.
     * 
     * @param node Tree root.
     */
    void buildCST0(TreeNode node) {
        while (tokenStream.size() > 0 && branchDepth >= 0) {
            CodeLine line = new CodeLine(node.line ,tokenStream);

            // Each branch of the tree is any code block that encapsulates
            // other code. If we haven't encountered one, keep processing
            // lines and adding them.
            if (!line.isBranch) {
                node.addLine(line);
                continue;
            }

            // Starting branch nodes encapsulate new entries.
            // Ending nodes require us to return to the caller (no more nodes
            // to add here).
            if (line.isStart) {
                if (line.isTarget() && this.branchDepth != 0)
                    throw new ProcessingException(line.getStartingLineNumber(), 
                            String.format("targets can only be declared at root (current nesting: %d)", this.branchDepth));

                TreeNode newNode = new TreeNode(line);
                line.addTreeNode(newNode);

                this.branchDepth++;

                buildCST0(newNode);
                node.addLine(line);
            }
            else {
                this.branchDepth--;
                return;
            }
        }
    }
}