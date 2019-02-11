import java.io.Serializable;

public class ReducerObject<K, V> implements Serializable{

	private static final long serialVersionUID = 745566089162463763L;
	
	K key;
	V value;
	
	ReducerObject(K key, V value){
		this.key = key;
		this.value = value;
	}
	
	K getKey(){
		return key;
	}
	
	V getValue(){
		return value;
	}
}
