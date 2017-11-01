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
        TreePath CB = new TreePath("AA", "AC", "CB");
        
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
