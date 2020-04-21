package com.fanap.midhco.ui.component.treeview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by admin123 on 3/22/15.
 */
public class TreeNode<T> implements Serializable, Comparable<TreeNode> {
    String title;
    Long id;
    TreeNode parent;
    List<TreeNode<T>> children = new ArrayList<TreeNode<T>>();
    boolean selected;
    T self;

    public TreeNode() {}

    public TreeNode(T t) {
        this.self = t;
    }

    public void setChildren(List<TreeNode<T>> children) {
        this.children = children;
    }

    public T getSelf() {
        return self;
    }

    public void setSelf(T self) {
        this.self = self;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public TreeNode getParent() {
        return parent;
    }

    public void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public Set<TreeNode> getChildren() {
        return new HashSet<TreeNode>(children);
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public void addChild(TreeNode node) {
        children.add(node);
        node.setParent(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TreeNode treeNode = (TreeNode) o;

        if(id != null)
            if (id != null ? !id.equals(treeNode.id) : treeNode.id != null) return false;
        if(self != null)
            if (self != null ? !self.equals(treeNode.self) : treeNode.self != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    public int compareTo(TreeNode o) {
        if(id < o.getId())
            return -1;
        if(id == o.id)
            return 0;
        return 1;
    }

    @Override
    public String toString() {
        return title.toString();
    }



}
