package common.parser;

import java.util.HashMap;
import java.util.ArrayList;

public class ExecTree {
    HashMap<String, ExecTreeNode> targets;

    public ExecTree(Tree tree) {
        this.targets = new HashMap<String, ExecTreeNode>();

        // Build the final executable tree.
        TreeNode root = tree.getRoot();
        ArrayList<TreeNode> availTargets = root.getSubTrees();
        ArrayList<ExecTreeNode> eNodes = buildExecTree(availTargets);

        // Allow target execution.
        cpTreeToTargets(eNodes);
    }

    private ArrayList<ExecTreeNode> buildExecTree(ArrayList<TreeNode> nodes) {
        ArrayList<ExecTreeNode> execNodes = new ArrayList<ExecTreeNode>();

        for (TreeNode node : nodes)
            execNodes.add(node.toExecTree());

        return execNodes;
    }

    private void cpTreeToTargets(ArrayList<ExecTreeNode> nodes) {
        for (ExecTreeNode node : nodes) {
            targets.put(node.name, node);
        }
    }

    public void execTarget(String target) {
        targets.get(target).execute();
    }

    public void printTree(String target) {
        System.out.println("\n***************** EXECUTABLE TREE ******************");
        targets.get(target).printTree();
        System.out.println("******************** END TREE **********************\n");
    }
}
