package com.rsc_games.sledge.parser;

/**
 * Thin wrapper around directly managing the concrete tree.
 */
public class Tree {
    TreeNode root;

    /**
     * Implicitly creates a root node for the CST generator.
     */
    public Tree() {
        this.root = new TreeNode(null);
    }

    /**
     * Allow us to directly interact with the tree root.
     * 
     * @return The generated root node.
     */
    public TreeNode getRoot() {
        return this.root;
    }
}
