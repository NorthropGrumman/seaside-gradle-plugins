package com.ngc.seaside.gradle.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The TreeNode provides a way to query the tree and add new elements.
 * The root node can add any leaf based on it's path assuming that the full path 
 * to their parents already exist.
 * 
 * @author justan.provence@ngc.com
 */
public class TreeNode implements Comparable<TreeNode> {
    private TreeNode parent;
    private TreePath path;
    private TreeMap<String, TreeNode> children;
    private Comparator<TreeNode> comparator;
    private String nodeDescription;
    
    /**
     * Constructor
     * @param path the path defines the node's name and it's root flag.
     */
    public TreeNode(TreePath path) {
        this(path, new NameComparator());
    }
    
    /**
     * Constructor
     * @param path the path defines the node's name and it's root flag.
     */
    public TreeNode(TreePath path, Comparator<TreeNode> comparator) {
        this.path = path;
        children = new TreeMap<String, TreeNode>();
        this.comparator = comparator;
    }

    /**
     * Constructor
     * @param path the path defines the node's name and it's root flag.
     * @param nodeDescription a description string for describing the node's represented object.
     */
    public TreeNode(TreePath path, String nodeDescription, Comparator<TreeNode> comparator) {
        this.path = path;
        this.nodeDescription = nodeDescription;
        children = new TreeMap<String, TreeNode>();
        this.comparator = comparator;
    }
    
    /**
     * Get the name of the node, this is the same as calling
     * TreeNode.getPath().getName();
     * @return the name of the node.
     */
    public String getName() {
        return path.getName();
    }

    /**
     * Get the description of the node's represented object.
     * @return the node's description.
     */
    public String getDescription() {
        return nodeDescription;
    }

    /**
     * Get the parent node.
     * @return the parent node.
     */
    public TreeNode getParent() {
        return parent;
    }
    
    /**
     * Set the parent node.
     * @param parent the parent node.
     */
    public TreeNode setParent(TreeNode parent) {
        this.parent = parent;
        return this;
    }
    
    /**
     * Get the path associated with this node.
     * @return the path.
     */
    public TreePath getPath() {
        return path;
    }
    
    /**
     * Determine if this is the root node. Is equal to 
     * TreeNode.getPath().isRoot().
     * @return the root node.
     */
    public boolean isRoot() {
        return path.isRoot();
    }

    /**
     * Get the children.
     * @return the children or empty list if it's a leaf.
     */
    public List<TreeNode> getChildren() {
        if(children.isEmpty()) {
            return Collections.emptyList();
        }
        List<TreeNode> list = new ArrayList<>(children.values());
        Collections.sort(list);
        return list;
    }
    
    /**
     * Add a child to the node. This is a recursive method that will check all of 
     * the children and it's children's children and so on to see if it can add the child 
     * based on it's path.
     * @param node the node to add.
     * @return true if added, false if it was unable to add.
     */
    public boolean addChild(TreeNode node) {
        if(node.getPath().isLeafOf(path)) {
            node.setParent(this);
            children.put(node.getName(), node);
            return true;
        }
        else if(node.getPath().isDescendantOf(path)) {
            for(TreeNode n: children.values()) {
                if(n.addChild(node)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Remove the child. This is a recursive method that will check all of the 
     * children and it's children's children and so on to see if it can find the 
     * correct TreeNode to remove.
     * @param node the tree node.
     * @return true if the child was removed.
     */
    public boolean removeChild(TreeNode node) {
        if(children.containsKey(node.getName())) {
            node.setParent(null);
            children.remove(node.getName());
            return true;
        }
        else if(node.getPath().isDescendantOf(path)) {
            for(TreeNode n: children.values()) {
                if(n.removeChild(node)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * Remove all the children from this node.
     */
    public void removeAll() {
        for(TreeNode child : children.values()) {
            child.removeAll();
        }
        children.clear();
    }
    
    /**
     * Get the leaf nodes for this node and the height at which they are.
     * @return the leaf nodes.
     */
    public Map<TreeNode, Integer> getLeafNodes() {
        return getLeafNodes(this, 1);
    }
    
    /**
     * Get the height of the node.
     * @return the height of the tree.
     */
    public int getHeight() {
        return getHeight(1);
    }
    
    /**
     * Get the index of the child assuming it is a child of this node.
     * @param name the name of the child.
     * @return the index of the child, or -1 if the child isn't found.
     */
    public int indexOfChild(String name) {
        if(children.containsKey(name)) {
            String[] keys = new String[children.size()];
            children.keySet().toArray(keys);
            return Arrays.binarySearch(keys, name);
        }
        return -1;
    }
    
    /**
     * Get the child based on it's name.
     * @param name the name.
     * @return null if not a child of this node.
     */
    public TreeNode getChild(String name) {
        return children.get(name);
    }
    
    /**
     * Determine if this node contains the given node.
     * @param name the name of the node.
     * @return true if the child exist.
     */
    public boolean contains(String name) {
        return children.containsKey(name);
    }
    
    /**
     * Default is to compare on the name. Set the default comparator
     * in the constructor.
     */
    @Override
    public int compareTo(TreeNode other) {
        return comparator.compare(this, other);
    }
    
    /**
     * Use the static method toString(TreeNode, String) for a more detailed representation of the node.
     */
    @Override
    public String toString() {
        return String.format("TreeNode: [name: %s] [children size: %s]", getName(), children.size());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) {
            return true;
        }
        if(!(obj instanceof TreeNode)) {
            return false;
        }
        TreeNode other = (TreeNode) obj;
        return Objects.equals(path, other.path);
    }
    
    /**
     * Static method for producing a string of the given node with some offset to denote children.
     * @param n      the node.
     * @param offset the offset (to start it's usually "")
     * @return the String representation of the tree, using space and newlines to help visualize the node.
     */
    public static String toString(TreeNode n, String offset) {
        String val = "";
        val += offset + n.getName() + System.getProperty("line.separator");
        offset += " ";
        for(TreeNode child: n.getChildren()) {
            val += toString(child, offset);
        }
        return val;
    }

    /**
     * Get all the leaf nodes by their height.
     * @param node the node.
     * @param height the current height.
     * @return map of leaf nodes to their height.
     */
    private Map<TreeNode, Integer> getLeafNodes(TreeNode node, int height) {
        Map<TreeNode, Integer> nodes = new LinkedHashMap<>();
        
        for(TreeNode child : node.getChildren()) {
            if(!child.getChildren().isEmpty()) {
                nodes.putAll(getLeafNodes(child, height+1));
            }
            else {
                nodes.put(child, height+1);
            }
        }
        
        return nodes;
    }

    /**
     * Get the height based on a start point.
     * @param start the start.
     * @return the height.
     */
    private int getHeight(int start) {
        if(children.size() == 0) {
            return start;
        }
        
        List<Integer> heights = new ArrayList<>();
        for(TreeNode node: children.values()) {
            heights.add(node.getHeight(start+1));
        }
        return Collections.max(heights);
    }

    /**
     * Default comparator using the name.
     */
    public static class NameComparator implements Comparator<TreeNode> {
        @Override
        public int compare(TreeNode node1, TreeNode node2) {
            return node1.getName().compareTo(node2.getName());
        }
    }
}
