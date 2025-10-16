package com.rsc_games.sledge.parser;

import java.util.HashMap;

import com.rsc_games.sledge.env.BuilderVars;

import java.util.ArrayList;

/**
 * Final tree representation. This is the only directly executable tree.
 */
public class ExecTree {
    /**
     * Full list of targets
     */
    HashMap<String, ExecTreeNode> targets;

    /**
     * Convert the codeline + treenode mess to a cleaner, usable tree.
     * 
     * @param tree The tree to convert.
     */
    public ExecTree(Tree tree) {
        this.targets = new HashMap<String, ExecTreeNode>();

        // Build the final executable tree.
        TreeNode root = tree.getRoot();
        ArrayList<TreeNode> availTargets = root.getSubTrees();
        ArrayList<ExecTreeNode> eNodes = buildExecTree(availTargets);

        // Allow target execution.
        cpTreeToTargets(eNodes);
    }

    /**
     * Interanl helper. Recursively converts the tree to an executable format.
     * 
     * @param nodes The inner trees of the CST tree subsystem.
     * @return The generated executable tree.
     */
    private ArrayList<ExecTreeNode> buildExecTree(ArrayList<TreeNode> nodes) {
        ArrayList<ExecTreeNode> execNodes = new ArrayList<ExecTreeNode>();

        for (TreeNode node : nodes)
            execNodes.add(node.toExecTree());

        return execNodes;
    }

    /**
     * Register each target to enable lookups.
     * 
     * @param nodes The internal executable tree nodes.
     */
    private void cpTreeToTargets(ArrayList<ExecTreeNode> nodes) {
        for (ExecTreeNode node : nodes) {
            targets.put(node.name, node);
        }
    }

    public void execTarget(BuilderVars vars, String target) {
        targets.get(target).execute(vars);
    }

    public void printTree(String target) {
        System.out.println("\n***************** EXECUTABLE TREE ******************");
        targets.get(target).printTree();
        System.out.println("******************** END TREE **********************\n");
    }
}
