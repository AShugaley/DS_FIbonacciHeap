package DS2;


/**
 * FibonacciHeap
 *
 * An implementation of fibonacci heap over non-negative integers.
 */
public class FibonacciHeap
{
	private static final double PHI = (1+Math.sqrt(5))/2; // constant golden ratio
	
	public static int totalLinks; // static number of links
	public static int totalCuts; // static number of cuts
	private int size; 
	private int numMarked; // num of marked nodes, needed for potential
	private int numOfTrees; // also for potential 
	private HeapNode min=null; 
	
	
   /**
    * public boolean empty()
    *
    * precondition: none
    * 
    * The method returns true if and only if the heap
    * is empty.
    *   
    */
    public boolean empty() // o(1)
    {
    	return min == null; 
    }
		
   /**
    * public HeapNode insert(int key)
    *
    * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap. 
    */
    public HeapNode insert(int key) // o(1)
    {    
    	HeapNode in = new HeapNode(key); // create a node
    	in = innerInsert(in);  // most of the insert is here
    	return in;
    }
    
    
    /**
     * help method
     * 
     * @param in
     * @return node inserted 
     */
    private HeapNode innerInsert(HeapNode in){ // o(1)
    	if (size==0) // insert first
    	{
    		insertfirst(in);
    		size=1;
    		return in;
    	}
    	// not insert first :)  
    	insertAfter(min, in); // insert after the min
    	if (in.key < min.key) // update min
    		min = in; 
    	size++; // update size
    	return in;
    }

    /**
     * @pre- fisrt is not none. 
     * @param first
     */
    private void insertfirst(HeapNode first) //insert to an empty heap 
    {
    	min = first; 
		numOfTrees = 1;
    }
    
    /**
     * 
     * insert node b after node a 
     * @pre- a is in the heap, a is not null
     * @param a
     * @param b
     */
    private void insertAfter(HeapNode a, HeapNode b) // o(1)
    { // this is a separate function as it will be useful for the successive linking // o(1)
    	
    	if(a==null & b==null) // actually we do take care of certain none cases here 
    		return;
    	else if(a==null) 
    		insertfirst(b);
    	else if(b==null)
    		insertfirst(a);
    	b.prev=a; // otherwise just insert in linked list
    	b.next=a.next;
    	a.next=b;
    	b.next.prev=b;
    	numOfTrees++;
    }
    
    

    /**
     * public void deleteMin()
     *
     * Delete the node containing the minimum key.
     *
     */
     public void deleteMin()  // amort o(logn), W.C. o(n)
     {
    	if(min==null)
    	{
    		System.out.println("\ncan't delete- empty heap\n"); // why would you try that?!
    		return;
    	}
    	if(size==1) // not much left if we delete
    	{
    		min=null;
    		size=0;
    		numOfTrees=0;
    		numMarked=0;
    		return; // well now we have nothing
    	}   	
    	if(numOfTrees==1)  //1 tree, with children 
    	{
    		numOfTrees=min.rank; // naturally
    		min=consolidate(min.child); // lets all put them together 
    		size--; // :(
    		return; 
    	}   	
     	min.next.prev=min.prev; // none of the above, we have a big, healthy heap! 
     	min.prev.next=min.next; // lets take min out of the list
     	if(min.child!=null) // if the min had children  
     		concatenate(min.child, min.prev.next); // add them to tree list
     	numOfTrees+= min.rank-1; // we added the trees but removed the min   
      	min=consolidate(min.prev.next); 
      	size--;
     }
     
     
     /**
      * 
      * successive linking, return the new min 
      * 
      */
     private HeapNode consolidate(HeapNode x) // amort o(logn), W.C. o(n)
     {
     	HeapNode[] b=new HeapNode[(int) (Math.log(size)/ Math.log(PHI))+1];
     	toBuckets(x,b); // send to array
     	min=null;
     	numOfTrees=0;
      	HeapNode newmin=fromBuckets(b); // take from array
     	return newmin;   //the new minimum 
     	
     }
     
     /**
      * 
      * creating an array with the linked trees
      * 
      */
     private void toBuckets(HeapNode x, HeapNode[] b){  // amort o(logn), W.C. o(n)
     	

     	for(int i=0; i<b.length; i++)
     		b[i]=null;
     	     	
     	x.prev.next= null;
     	while (x!= null)
     	{
     		HeapNode y=x;
     		x= x.next;
     		while(b[y.rank]!= null)
     		{
     			y= link(y,b[y.rank]);
     			b[y.rank-1]=null ;
     		}
     		b[y.rank]=y;
     		y.next=y;
     		y.prev=y;    		
     	}
     }
     
     /**
      * 
      * creating an array with the linked trees
      * 
      */
     private HeapNode fromBuckets(HeapNode[] b)  // o(logn)
     {
     	HeapNode x=null;
     	
     	for(int i=0; i<b.length; i++)
     	{
     		if (b[i]!=null)
     		{
     			if(x==null)
     			{
     				x=b[i];
     				x.next=x;
     				x.prev=x;  
     				insertfirst(x);	
     			}
     			else
     			{
     				insertAfter(x,b[i]);
     				if(b[i].key < x.key)
     					x=b[i];
       			}
     		}
     	}
     	return x;   ///the new minimum 
     }
 
     /**
      * 
      * @param x
      * @param y
      * 
      * linking 2 heapnodes- bigger key - child of smaller key
      */
     private HeapNode link(HeapNode x, HeapNode y) // link two nodes to create one tree - o(1)
     {
     	totalLinks++; // update static 
     	numOfTrees--; //every time you link, you kill a tree. Don't you care about the enviorment?! 
     	
     	if(x.key<y.key) // one is greated then the other 
     	{
     		HeapNode firstchild= x.child;
     		x.child=y;
     		y.parent=x;
     		y.next= y.prev=y;
     		concatenate(y, firstchild); // form a list 
     		x.rank++;
     		return x;
     	}
     	else // miror miror on the wall 
     	{
     		HeapNode firstchild= y.child;
     		y.child=x;
     		x.parent=y;
     		x.next= x.prev=x; 
     		concatenate(x, firstchild); // form a list 
     		y.rank++;
     		return y;
     	}    	
     }
     

   /**
    * public HeapNode findMin()
    *
    * Return the node of the heap whose key is minimal. 
    *
    */
    public HeapNode findMin() // o(1)
    {
    	return min; // well... 
    } 
    
   /**
    * public void meld (FibonacciHeap heap2)
    *
    * Meld the heap with heap2
    *
    */
    public void meld (FibonacciHeap heap2){ // o(1)     
    	  HeapNode otherMin = heap2.findMin();


    	  if (otherMin == null)  //the other heap is empty/null
    		  return;
    	  
    	  size+= heap2.size;
    	  numOfTrees+= heap2.numOfTrees;
    	  numMarked+= heap2.numMarked;
    	  
    	  if(min==null)  //our heap is empty/null
    	  {  
    		  min=otherMin;
    	  	  return;
    	  }
    	  else
    		  concatenate(min, otherMin);  // yeah concatenate 'em all
    	  
    	  if (otherMin.key < min.key) // update min
    		  min = otherMin;
    }
    
   
    /**
     *
     * @param a
     * @param b
     * 
     * something like this 
     */
    private void concatenate(HeapNode a, HeapNode b){ // consolidating one list from two (lists or nodes) in o(1) time
    	if(a==null && b==null) // if one is null
    		return;
    	if(a==null) // this is useful in certain cases, like from_buckets
    	{
    		b.next=b;
    		b.prev=b;
    	}
    	if(b==null)
    	{
    		a.next=a;
    		a.prev=a;
    	}
    	else // the nodes are legal 
    	{
	    	HeapNode aPrev = a.prev; // what was before a - this works even if a is a.prev...
	    	HeapNode bNext = b.next; // what was after b 
	    	aPrev.next = bNext; // kinda creating a loop
	    	bNext.prev = aPrev;
	    	b.next = a;
	    	a.prev = b;
    	}
    }

   /**
    * public int size()
    *
    * Return the number of elements in the heap
    *   
    */
    public int size() // o(1)
    {
    	return size; // well...
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return a counters array, where the value of the i-th entry is the number of trees of order i in the heap. 
    * 
    */
    public int[] countersRep()  // o(n) worst case 
    {
    	int[] arr = new int[42];
    	HeapNode current= min;
    	if(min==null)
    		return arr;
    	arr[current.rank]++;
    	current= min.next; 
    	while(current!=min) // going over the trees, and updating ranks 
    	{
    		arr[current.rank]++;
    		current= current.next;   		
    	}	
        return arr; 
    }

//   /**
//    * public void arrayToHeap()
//    *
//    * Insert the array to the heap. Delete previous elemnts in the heap.
//    * 
//    */
//    public void arrayToHeap(int[] array)
//    {
//    	init();
//    	if(array!=null)
//    	{
//	    	for (int p : array)
//	    		this.insert(p);
//    	}
//    }
    
//    private void init(){
//    	numOfTrees = 0;
//    	numMarked = 0;
//    	size = 0;
//    	min = null;
//    }
	
   /**
    * public void delete(HeapNode x)
    *@pre- the given node is in the heap 
    * Deletes the node x from the heap. 
    *
    */
    public void delete(HeapNode x) // o(
    {    
    	if(x != min) // if it's not min -> make it min!
    		decreaseKey(x, x.key - min.key + 1);
    	deleteMin(); // hasta la vista, baby
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * The function decreases the key of the node x by delta. The structure of the heap should be updated
    * to reflect this chage (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta) // same as cascading cuts, amort o(1), W.C. o(logn)
    {    
    	x.key = x.key - delta; // update the key
    	if (x.parent !=  null)   //not a root 
    	{
    		if (x.key < x.parent.key)  //heap order violation
    		{
    			cascadingCuts(x, x.parent);
    		}
    	}
    	if (x.key < min.key)
    		min =x;

    }
    
    /**
     * 
     * @param child
     * @param parent
     */
    private void cascadingCuts(HeapNode child, HeapNode parent){ // tallest tree will be about o(logn), therfore this is the W.C. amort - o(1)
    	
    	cut(child, parent); // cut it first
    	if (parent.parent != null)   //parent is not a root 
    	{
    		if (parent.isMarked){ // parent already lost a child :(
    			cascadingCuts(parent, parent.parent);
    		}
			else{ // not marked? mark it
				numMarked++;
				parent.isMarked = true;
			}
    	}
    }
    
    
    /**
     * 
     * @param child
     * @param parent
     */
    private void cut(HeapNode child,HeapNode parent){ // o(1) - same as in the pseudo code
    	totalCuts++;
    	numOfTrees++;
    	
    	child.parent = null; // plus a few cases 
    	if (child.isMarked == true)
    		numMarked--;
    	child.isMarked = false;
    	parent.rank--;
    	if (child.next == child){
    		parent.child = null;
    	}
    	else{
    		parent.child = child.next;
    		child.prev.next = child.next;
    		child.next.prev = child.prev;
    	}
    	child.next = child;
    	child.prev = child;
    	concatenate(min, child); // link to one list 
    }
    
    
   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * The potential equals to the number of trees in the heap plus twice the number of marked nodes in the heap. 
    */
 
    public int potential() // o(1)
    {    
    	return numOfTrees+ 2*numMarked; // just return 
    }

    
    
    
   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the run-time of the program.
    * A link operation is the operation which gets as input two trees of the same rank, and generates a tree of 
    * rank bigger by one, by hanging the tree which has larger value in its root on the tree which has smaller value 
    * in its root.
    */
    public static int totalLinks() // o(1)
    {    
    	return totalLinks; // just return 
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the run-time of the program.
    * A cut operation is the operation which diconnects a subtree from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts() //o(1)
    {    
    	return totalCuts; // just return 
    }
       
   
   /**
    * public class HeapNode
    * 

    */
    public class HeapNode{

    	
    	
    	int key;
    	HeapNode child=null;
    	private HeapNode parent=null;
    	HeapNode next; 
    	HeapNode prev;
    	boolean isMarked;
    	private int rank;  // number of children 
    	
    	public HeapNode(int key){ //creating a new node 
    		this.key = key;
    		next= this;
    		prev=this;
    		rank=0;
    		
    	}

		public HeapNode() {} // empty builder, if we ever need it
  	
    } // Mor Huberman & Alexander Shugaley
}