/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mycompany.mavenproject1;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Alpha
 */
public class DoubleLinkedListTest {
    
    public DoubleLinkedListTest() {
    }
    
    /**
     * Test of addLast method, of class DoubleLinkedList.
     */
    @Test
    public void testAddLast() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        
        Integer[] listContentToTest = {1,2};
        DoubleLinkedList<Integer> instanceToTest = new DoubleLinkedList<Integer>(listContentToTest);
        instanceToTest.addLast(3);
        
        assertEquals(instance, instanceToTest);
    }

    /**
     * Test of addFirst method, of class DoubleLinkedList.
     */
    @Test
    public void testAddFirst() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        
        Integer[] listContentToTest = {2,3};
        DoubleLinkedList<Integer> instanceToTest = new DoubleLinkedList<Integer>(listContentToTest);
        instanceToTest.addFirst(1);
        
        assertEquals(instance, instanceToTest);
    }

    /**
     * Test of isEmpty method, of class DoubleLinkedList.
     */
    @Test
    public void testIsEmpty_Negative() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        assertEquals(false, instance.isEmpty());
    }
        @Test
    public void testIsEmpty_Positive() {
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>();
        assertEquals(true, instance.isEmpty());
    }


    /**
     * Test of size method, of class DoubleLinkedList.
     */
    @Test
    public void testSize() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        assertEquals(3, instance.size());
    }

    /**
     * Test of addBefore method, of class DoubleLinkedList.
     */
    @Test
    public void testAddBefore() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        
        Integer[] listContentToTest = {1,3};
        DoubleLinkedList<Integer> instanceToTest = new DoubleLinkedList<Integer>(listContentToTest);
        instanceToTest.addBefore(2,3);
        assertEquals(instance, instanceToTest);
    }

    /**
     * Test of addAfter method, of class DoubleLinkedList.
     */
    @Test
    public void testAddAfter() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        
        Integer[] listContentToTest = {1,3};
        DoubleLinkedList<Integer> instanceToTest = new DoubleLinkedList<Integer>(listContentToTest);
        instanceToTest.addAfter(2,1);
        assertEquals(instance, instanceToTest);
    }

    /**
     * Test of remove method, of class DoubleLinkedList.
     */
    @Test
    public void testRemove() {
        Integer[] listContent = {1,2,4};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        
        Integer[] listContentToTest = {1,2,3,4};
        DoubleLinkedList<Integer> instanceToTest = new DoubleLinkedList<Integer>(listContentToTest);
        instanceToTest.remove(3);
        assertEquals(instance, instanceToTest);
    }

    /**
     * Test of contains method, of class DoubleLinkedList.
     */
    @Test
    public void testContainss_Positive() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        assertEquals(true, instance.contains(2));
    }
    
    @Test
    public void testContains_Negative() {
        Integer[] listContent = {1,2,3};
        DoubleLinkedList<Integer> instance = new DoubleLinkedList<Integer>(listContent);
        assertEquals(false, instance.contains(5));
    }
    
}
