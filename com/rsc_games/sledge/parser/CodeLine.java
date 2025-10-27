package com.rsc_games.sledge.parser;

import java.util.ArrayList;

import com.rsc_games.sledge.parser.ops.Operation;

/**
 * Internal representation of a "codeline"- a fancy name for a line of executable
 * code in the build config.
 */
public class CodeLine {
    /**
     * All of the encapsulated tokens within this "codeline". (Why tokens?)
     */
    ArrayList<Token> tokens = new ArrayList<Token>();

    /**
     * Recursively stores nodes and their respective codelines in an easy
     *  to walk tree.
     */
    TreeNode attachedNode;

    /**
     * Enable walking up the tree (useful for branch connections).
     */
    CodeLine parentLine;

    /**
     * AST bookkeeping. Represents how this codeline is meant to be executed
     * at build time.
     */
    Operation op; 

    /**
     * Marks this codeline as a branch, which encapsulates other nodes.
     * There's no reason to recurse if this node doesn't contain others.
     */
    public boolean isBranch = false;

    /**
     * Part of the above bookkeeping system. Marks whether this node
     * is the starting node for encapsulated child nodes.
     */
    public boolean isStart = false;

    /**
     * Supply this "codeline" with the operation it must execute.
     * 
     * @param tokenStream What remains of the full token stream.
     */
    public CodeLine(CodeLine parent, ArrayList<Token> tokenStream) {
        this.tokens = parseLine(tokenStream);
        this.parentLine = parent;
    }

    /**
     * CST building operation. Attach a tree node to this "codeline".
     * The tree node contains the child nodes of this codeline.
     * 
     * @param node The pre-filled tree node.
     */
    public void addTreeNode(TreeNode node) {
        this.attachedNode = node;
    }

    /**
     * CST operation. Access the attached tree node (if this is a 
     * branch).
     * 
     * @return The tree node.
     */
    public TreeNode getTreeNode() {
        return this.attachedNode;
    }

    public Operation getOp() {
        return this.op;
    }

    /**
     * Convenience wrapper over the getPreviousLine function below.
     * Hides internal complexity such as which line to pass to the
     * parent function.
     * 
     * @return The previous line of code, if any.
     */
    public CodeLine getPreviousLine() {
        return parentLine.getPreviousLine(this);
    }

    /**
     * Get the line of code directly preceding this one, if any.
     * 
     * @param currentLine Current code line for finding a back pointer
     * @return The previous line, if any, otherwise null.
     */
    private CodeLine getPreviousLine(CodeLine currentLine) {
        int index = this.attachedNode.childLines.indexOf(currentLine) - 1;
        return index > 0 ? this.attachedNode.childLines.get(index) : null;
    }

    public boolean isTarget() {
        return this.tokens.size() == 2 && this.tokens.get(0).tok == TokenID.TOK_NAME
                && this.tokens.get(1).tok == TokenID.TOK_CURLY_OPEN;
    }

    /**
     * Identify which physical line this "codeline" starts on.
     * 
     * @return The physical line.
     */
    public int getStartingLineNumber() {
        return this.tokens.size() > 0 ? this.tokens.get(0).lno : -1;
    }

    /**
     * CST operation. Parse the supplied token stream into a single logical
     * operation.
     * 
     * @param tokenStream The full token stream.
     * 
     * @return The logical operation for this code line.
     */
    private ArrayList<Token> parseLine(ArrayList<Token> tokenStream) {
        ArrayList<Token> tokList = new ArrayList<Token>();
        Token t;
        
        do {
            // Nothing left to process.
            if (tokenStream.size() == 0) 
                return tokList;
        
            t = tokenStream.remove(0);
            
            // Update the branch bookkeeping information for this object.
            if (t.tok == TokenID.TOK_CURLY_OPEN) {
                isBranch = true;
                isStart = true;
            }
            else if (t.tok == TokenID.TOK_CURLY_CLOSE) {
                isBranch = true;
                isStart = false;
            }

            // Early discarding of whitespace characters (Make processing faster)
            if (t.tok == TokenID.TOK_NEWLINE || t.tok == TokenID.TOK_SPACE || t.tok == TokenID.TOK_COMMENT)
                continue;

            // Semicolons are only required for parsing. Don't retain them.
            if (t.tok != TokenID.TOK_SEMICOLON)
                tokList.add(t);

            validateTokenList(tokList);
        }
        // A logical line is only terminated with a curly or a semicolon.
        while (t.tok != TokenID.TOK_SEMICOLON && t.tok != TokenID.TOK_CURLY_OPEN && t.tok != TokenID.TOK_CURLY_CLOSE);

        // Line processing is done. Partial lines are not permitted.
        if (!validateTokenList(tokList))
            throw new ProcessingException(tokList.get(0).lno, "not a statement");

        return tokList;
    }

    /**
     * Process the token list and determine whether a given line of code is legal.
     * 
     * @param tokList
     * @return True if the given line is legal, false if it's not yet legal, and throws
     *  an exception if rules are violated.
     */
    private boolean validateTokenList(ArrayList<Token> tokList) {
        // There are a few different types of possible "codelines"
        // that must be validated here.
        //
        // The easiest is the target. It must have at most 2 tokens of the form
        //      <target name> {
        //
        // Next is a varset/varappend. It must have at least 3 tokens of the form
        //      <var name> = <value>;
        //      <var name> := <value>; (for var append)
        //
        // A frequent case is reserved words. Those are handled very differently
        // for each one, so that is delegated to the grammar subsystem.
        // Examples include:
        //      if (conditional) {
        //      elif (conditional) {
        //      else {
        //
        // Units/commands are parsed similarly to vars, but have a leading
        // character distingushing them.
        //      %internal_unit <arg> <arg>
        //      @external_unit <arg> <arg>
        
        // Attempt to guess the line type.
        Token firstToken = tokList.get(0);

        // Not sure there's many cases where a curly would be invalid.
        if (firstToken.tok == TokenID.TOK_CURLY_CLOSE)
            return true;

        // If the token list is too short there's not enough information to guess
        // a line type. Try again later. We can't rule it out as invalid though.
        if (tokList.size() < 2)
            return false;

        switch (firstToken.tok) {
            case TOK_NAME: {
                return Grammar.isValidLine_TYPE_NAME(tokList);
            }
            case TOK_KEYWORD: {
                return Grammar.isValidLine_TYPE_KEYWORD(tokList);
            }
            case TOK_INTERNAL_UNIT:
            case TOK_UNIT: {
                return Grammar.isValidLine_TYPE_UNIT(tokList);
            }
            // NOTE: curly tested above.
            case TOK_CURLY_CLOSE:
                break;
            default:
                throw new ProcessingException(firstToken.lno, "illegal start of line");
        }

        return false;
    }

    /**
     * AST operation. Recover the generated executable operation
     * from this bloated data structure.
     * 
     * @return The finalized operation.
     */
    public Operation extractOp() {
        return this.op;
    }

    /**
     * AST operation. Recursively generate executable operations in-place
     * from this tree.
     * 
     * @return The executable tree-ified operations.
     */
    public Operation generateInner() {
        ArrayList<Operation> inner = new ArrayList<Operation>();

        if (attachedNode != null) 
            inner = attachedNode.generateInner();
        
        if (op.isBranch())
            op.setInner(inner);

        return op;
    }

    /**
     * AST operation. Clean out whitespace lines to reduce processing time.
     */
    public void clean() {
        if (attachedNode != null)
            attachedNode.clean();
            
        ArrayList<Token> newTokens = new ArrayList<Token>();

        for (Token t : this.tokens) {
            if (t.tok != TokenID.TOK_NEWLINE && t.tok != TokenID.TOK_COMMENT 
                && t.tok != TokenID.TOK_SPACE)
                newTokens.add(t);
        }

        this.tokens = newTokens;
    }

    public boolean isEmpty() {
        return this.tokens.size() == 0;
    }

    /**
     * AST operation. Analyze this line's components and convert it from a
     * concrete token stream to something executable.
     */
    public void analyze() {
        if (attachedNode != null)
            attachedNode.analyze();

        this.op = Operation.staticAnalyze(this.tokens, this.attachedNode);
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
