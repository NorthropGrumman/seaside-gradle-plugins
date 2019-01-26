/**
 * UNCLASSIFIED
 * Northrop Grumman Proprietary
 * ____________________________
 *
 * Copyright (C) 2019, Northrop Grumman Systems Corporation
 * All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains the property of
 * Northrop Grumman Systems Corporation. The intellectual and technical concepts
 * contained herein are proprietary to Northrop Grumman Systems Corporation and
 * may be covered by U.S. and Foreign Patents or patents in process, and are
 * protected by trade secret or copyright law. Dissemination of this information
 * or reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Northrop Grumman.
 */
package com.ngc.seaside.gradle.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * This class describes the path of an element in the Tree.
 * 
 * @author justan.provence@ngc.com
 */
public class TreePath {

    public static final String DELIMITER = "|";
    
    private List<String> path = new ArrayList<>();

    private boolean root = false;
    private String name = "";
    
    /**
     * Constructor.
     * @param path the path. This should have at least 1 leaf or it will throw an IllegalArgumentException.
     */
    public TreePath(String... path) {
        this(Arrays.asList(path));
    }
    
    /**
     * Constructor.
     * @param parent the parent path.
     * @param leaf   1 to many leafs. This can also be a copy constructor if left blank.
     */
    public TreePath(TreePath parent, String... leaf) {
        List<String> path = parent.getPath(); //returns copy
        if(leaf != null && leaf.length > 0) {
            path.addAll(Arrays.asList(leaf));
        }
        setPath(path);
    }
    
    /**
     * Constructor.
     * @param path the path.
     */
    public TreePath(List<String> path) {
        setPath(path);
    }
    
    /**
     * Method for creating a TreePath from a String based on the TreePath.DELIMITER
     * AA|AB|AC would produce a path of size 3, whose root is AA and name is AC
     * @param pathAsString the String representation of the path.
     * @return a new TreePath object.
     */
    public static TreePath fromString(String pathAsString) {
        String[] asArray = pathAsString.split("\\"+DELIMITER);
        return new TreePath(asArray);
    }

    /**
     * Determine if this is the root. If the path size is 1, it can be assumed it is the root.
     * @return true if the path is the root path.
     */
    public boolean isRoot() {
        return root;
    }
    
    /**
     * Get the name.
     * @return the name of the path. This is the last leaf of the path.
     */
    public String getName() {
        return name;
    }

    /**
     * Get the path.
     * @return the path.
     */
    public List<String> getPath() {
        return new ArrayList<>(path);
    }
    
    /**
     * Determine if this path is a leaf of the given path.<br>
     * This path: AA|AB, test path = AA will return true.<br>
     * This path: AA|AB|AC, path = AA will return false (AA|AB is the direct ancestor).<br>
     * @param path the path to test.
     * @return true if the given path is the direct ancestor of this path.
     */
    public boolean isLeafOf(TreePath path) {
        List<String> current = getPath();
        List<String> test = path.getPath();
        if(current.size() <= test.size()) {
            return false;
        }
        current.removeAll(test);
        return current.size() == 1;
    }
    
    /**
     * Determine if the given path is an ancestor of this path.<br>
     * This = "AA|AB|AC", path = "AA" would return true. 
     * @param path the path to test.
     * @return true if the given path is an ancestor of this path.
     */
    public boolean isDescendantOf(TreePath path) {
        String temp = path.toString();
        String current = toString();
        return current.startsWith(temp);
    }
     
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for(int i=0; i<path.size(); i++) {
            builder.append(path.get(i));
            if(i < path.size() -1) {
                builder.append(DELIMITER);
            }
        }
        return builder.toString();
    }
   
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        TreePath other = (TreePath) obj;
        
        return Objects.equals(path, other.path);
    }
    
    @Override
    public int hashCode() {
      return Objects.hash(path);
    }
    
    /**
     * Set the path, must be at least 1 in size.
     * If the size is 1, it is assumed to be the root.
     * @param path the path
     * @throws IllegalArgumentException if the path size is 0.
     */
    private void setPath(List<String> path) {
        if(path != null && !path.isEmpty()) {
            this.path = path;
            this.name = this.path.get(this.path.size()-1);
            if(path.size() == 1) {
                this.root = true;
            }
        }
        else {
            throw new IllegalArgumentException("");
        }
    }

}
