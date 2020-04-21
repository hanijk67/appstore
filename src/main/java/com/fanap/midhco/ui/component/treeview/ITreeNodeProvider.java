package com.fanap.midhco.ui.component.treeview;

import java.util.Set;

/**
 * Created by admin123 on 8/17/2015.
 */
public interface ITreeNodeProvider<G> {
    public Set<TreeNode<G>> getNodes(TreeNode<G> parentNode);
}
