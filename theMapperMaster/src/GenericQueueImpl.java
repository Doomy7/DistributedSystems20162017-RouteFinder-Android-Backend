import java.io.PrintStream;
import java.io.Serializable;
import java.util.NoSuchElementException;

public class GenericQueueImpl<T> implements Serializable{
	
	private static final long serialVersionUID = -2754587574625861525L;
	
	private GenericNode<T> front;
	private GenericNode<T> back;
    private int size;
    
    //checking if queue empty
	public boolean isEmpty(){
		return(front == null);
	}

	//putting node/item in queue
	public void put(T item){
		GenericNode<T> t = back;
		back = new GenericNode<T>(item);
		size++;
		if(isEmpty()){
			front = back;
		}else{
			t.next = back;
		}
	}

	//removing and returning item/node
	public T get() throws NoSuchElementException{
		if(isEmpty()){
			throw new NoSuchElementException();
		}else{
			T v = front.item;
			GenericNode<T> t = front.next;
			front = t;
			size--;
			return v;
		}
	}

    //returning item/node without removing item/node
	public T peek() throws NoSuchElementException{
		if(isEmpty()){
			throw new NoSuchElementException();
		}else{
			return front.item;
		}
	}


	//printing entire queue
	public void printQueue(PrintStream stream){
		if(isEmpty()){
			throw new NoSuchElementException();
		}else{
			GenericNode<T> cur = front;
			while(cur!=null){
				System.out.println(cur.item);
				cur = cur.next;
			}
		}
	}

	//getting size of queue
	public int size(){
		if(isEmpty()){
			return 0;
		}else{
			return size;
		}
		
	}
	
}
