package com.mycompany.mavenproject1;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//Double linked list with generic type
//@author Jorge Arias Lopez

//Interface
interface IDoubleLinkedList<T>{
    public boolean isEmpty();
    public int size();

    public void addLast(T content);
    public void addFirst(T content);
    public void addBefore(T addThis, T beforeThis);
    public void addAfter(T addThis, T afterThis);
   
    public void remove(T dataToRemove);
    public boolean contains(T dataToSearch);

}

public class DoubleLinkedList<T> implements IDoubleLinkedList<T> {

    //Node class which save content and two references to the previous and the next node
    //This class is for internal use, so no other class can access it
    class Node<T> {
        Node previous, next;
        T content;

        //Only content constructor
        public Node(T content){
            this.previous = null;
            this.next = null;
            this.content = content;
        }

        //All arguments constructor
        public Node(T content, Node previous, Node next){
            this.previous = previous;
            this.next = next;
            this.content = content;
        }
    }

    //DoubleLinkedList variable. Where saved the first node and the last of the list.
    private Node<T> begining, end;
    final Logger logger = LoggerFactory.getLogger(DoubleLinkedList.class);

    //Empty constructor
    public DoubleLinkedList(){
        this.begining = null;
        this.end = null;
    }
    
    //Constructor that get an array of objects of type T to fill the list and do tests
    public DoubleLinkedList(T[] objects ){
        this.begining = null;
        this.end = null;
        for(T object : objects){
            Node<T> node = new Node<T>(object);
            if(this.end == null){
                this.begining = node;
            }else{
                this.end.next = node;
                node.previous = this.end;
            }
             this.end = node;
        }
    }
    
    // Overriding equals() to compare two Complex objects 
    @Override
    public boolean equals(Object o) { 
        // If the object is compared with itself then return true 
        if (o == this) { 
           return true; 
        } 

        /* Check if o is an instance of Complex or not 
        "null instanceof [type]" also returns false */
        if (!(o instanceof DoubleLinkedList)) { 
                return false; 
        } 

        // typecast o to Complex so that we can compare data members 
        DoubleLinkedList dll = (DoubleLinkedList) o;
        //If sizes are not equal. Return false.
        if(this.size() != dll.size()){
            return false;
        }
        
        for(Node<T> i = this.begining, j = dll.begining ; 
                i != null; i = i.next, j = j.next){
            if(!i.content.equals(j.content)){
                return false;
            }       
        }
        
        return true;
    } 

    //Add node on the tail of the list
    public void addLast(T content){
        Node<T> node = new Node<T>(content);
        if(this.end == null){
            this.begining = node;
        }else{
            this.end.next = node;
            node.previous = this.end;
        }
        this.end = node;
    }

    //Add node on the head of the list
    public void addFirst(T content){
        Node<T> node = new Node<T>(content);

        if(this.begining == null){
            this.begining = node;
        }else{
            this.begining.previous = node;
            node.next = this.begining;
        }
        this.begining = node;
    }

    //Check if list is empty
    public boolean isEmpty(){
        return (this.begining == null);
    }

    //To know size of the lsit
    public int size(){
        int size = 0;
        for(Node<T> i = this.begining ; i != null; i = i.next){
            size++;
        }
        return size;
    }

    //PRIVATE function to find nodes given a information
    //Take note that if the list have information repeated this will only return the first encounter
    private Node<T> findNode(T toFind){
      for(Node<T> i = this.begining ; i != null; i = i.next){
            if(toFind.equals(i.content)){
                return i;
            }
        }  
        return null;
    }

    //Add node before existing node
    public void addBefore(T addThis, T beforeThis){
        Node<T> node = findNode(beforeThis);
        if(node != null){
            Node<T> nodeInsert = new Node<T>(addThis, node.previous, node);
            node.previous.next = nodeInsert;
            node.previous = nodeInsert;
        }
    }

    //Add node after existing node
    public void addAfter(T addThis, T afterThis){
        Node<T> node = findNode(afterThis);
        if(node != null){
            Node<T> nodeInsert = new Node<T>(addThis, node, node.next);
            node.next.previous = nodeInsert;
            node.next = nodeInsert;
        }
    }

    //Remove a node.
    //Take note that if the list have information repeated this will only return the first encounter
    public void remove(T dataToRemove){
        Node node = findNode(dataToRemove);
        if(node == null){return;}

        if(node.previous == null){
            this.begining = node.next;
        }else{
            node.previous.next = node.next;
        }

        if(node.next == null){
           this.end = node.previous;
        }else{
            node.next.previous = node.previous;
        }

        node = null;
    }

    //Check if the list contains at least one node with this information
    public boolean contains(T dataToSearch){
        return (findNode(dataToSearch) != null);
    }

}