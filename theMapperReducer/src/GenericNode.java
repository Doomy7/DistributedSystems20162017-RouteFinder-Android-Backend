import java.io.Serializable;


public class GenericNode<T> implements Serializable{
	private static final long serialVersionUID = 8586259746835805413L;
	T item;
	GenericNode<T> next;
	
	GenericNode(T item, GenericNode<T> next){
		this.item = item;
		this.next = next;
	}
	
	GenericNode(T item){
		this.item = item;
		next = null;
	}
	
}
