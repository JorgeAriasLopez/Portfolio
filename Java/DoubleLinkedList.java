//Double linked list with generic type
//@author Jorge Arias Lopez

public class DoubleLinkedList<T> {
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

    //Empty constructor
    public DoubleLinkedList(){
        this.begining = null;
        this.end = null;
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

    //Print the list. Only will work if T is String
    public void print(){
        if(this.begining == null){
            System.out.println("The list is empty");
        }else if(this.begining.content instanceof String){
            for(Node<T> i = this.begining ; i != null; i = i.next){
                System.out.print("\t[" + i.content +"]");
            }
            System.out.println("");    
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

    //Main function for unit tests. The comments show what should be printed on screen
    public static void main(String[] args) {
        DoubleLinkedList<String> dll = new DoubleLinkedList<String>();

        /*EMPTY LIST*/
        dll.print();           //The list is empty
        if( dll.isEmpty()){
           System.out.println("Empty list");   //Empty list
        }else{
            System.out.println("Not empty list");
        }
        
        /*ADDLAST*/
        dll.addLast("3");
        dll.addLast("5");
        dll.print();            //3 5

        /*ADDFIRST*/
        dll.addFirst("2");
        dll.addFirst("0");
        dll.print();           //0 2 3 5

        /*SIZE*/
        System.out.println("Size: " + dll.size()); //Size: 4

        /*ADDBEFORE*/
        dll.addBefore("4", "5");
        dll.print();        //0 2 3 4 5

        /*ADDAFTER*/
        dll.addAfter("1", "0");
        dll.print();        //0 1 2 3 4 5

        /*ISEMPTY*/
        if(dll.isEmpty()){
            System.out.println("Empty list");  
        }else{
            System.out.println("Not empty list");   //Not empty list
        }

        /*CONTAINS*/
        if(dll.contains("2")){
            System.out.println("List contains 2");     //List contains 2
        }else{
            System.out.println("List does not contain 2");   
        }       
        if(dll.contains("22")){
            System.out.println("List contains 22");    
        }else{
            System.out.println("List does not contain 22");   //List does not contain 22 
        }

        /*REMOVE*/
        dll.remove("3");
        dll.print();     //0 1 2 4 5
        System.out.println("Size: " + dll.size()); //Size: 5
    }
}