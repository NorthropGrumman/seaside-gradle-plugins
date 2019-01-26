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

import org.junit.Test;

import static org.junit.Assert.*;

public class TreePathTest {

    
    @Test
    public void testIsLeafOf() {
        TreePath AA = new TreePath("AA");
        
        TreePath AC = new TreePath("AA", "AC");
        
        TreePath CA = new TreePath("AA", "AC", "CA");
        TreePath CB = new TreePath("AA", "AC", "CB");  
        
        assertTrue(AC.isLeafOf(AA));
        assertFalse(CA.isLeafOf(AA));
        assertFalse(CB.isLeafOf(CA));
        assertFalse(AA.isLeafOf(AC));
    }
    
    @Test
    public void testIsDescendantOf() {
        TreePath AA = new TreePath("AA");
        
        TreePath AC = new TreePath("AA", "AC");
        
        TreePath CA = new TreePath("AA", "AC", "CA");
        
        assertTrue(CA.isDescendantOf(AA));
        assertTrue(CA.isDescendantOf(AC));
        
        assertFalse(AA.isDescendantOf(AC));
    }
    
    @Test
    public void testFromString() {
        TreePath path = TreePath.fromString("AA|BB|CC");
        assertEquals(3, path.getPath().size());
        
        TreePath path2 = TreePath.fromString("AA");
        assertEquals(1, path2.getPath().size());
        assertEquals("AA", path2.getPath().get(0));
    }
    
    @Test
    public void testToString() {
        TreePath CB = new TreePath("AA", "AC", "CB"); 
        
        assertEquals("AA|AC|CB", CB.toString());
    }
    
}
