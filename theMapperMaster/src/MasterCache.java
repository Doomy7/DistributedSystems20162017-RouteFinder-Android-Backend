
public class MasterCache<K,V>{


	K key;
	V value;
	int size; 
	LinkedHashMap<K,V> lhmap;
	
	MasterCache(int size){
		this.size = size;
		lhmap = new LinkedHashMap<K,V>(size);
	}
	

	public V lookUp(K key) {
		double[] pair = (double[]) key;
		V result = lhmap.get(pair);
		if(result == null) {	
			return null;
		}else {
			lhmap.moveToTop(key,result);
			return result;
		}
	}
	
	public void store(K key, V value) {
		double[] pair = (double[]) key;
		if(lhmap.get(pair) == null){
			if(cacheSize() == lhmap.getCurrentSize()) {
				lhmap.removeLeastRecentlyUsed();
				lhmap.put(pair, value);
			}else {
				lhmap.put(pair, value);
			}		
		}else{
			System.out.println("Found");
		}
	}

	public int cacheSize(){		
		return size;
	}
	
	public int curSize(){
		return lhmap.getCurrentSize();
	
	}
	
}
