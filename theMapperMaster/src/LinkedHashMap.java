import java.util.Arrays;

public class LinkedHashMap<K,V> {

	private CacheEntry<K,V> lhmtable[]; //table of entry lists
	private int size; //max items able to contain
	private int currentSize; //current items contained
	private CacheEntry<K,V> head,tail; //head and tail of doubly linked list
	
	/**
	 * LinkedHashMap constuctor
	 * Initializes hash table, max size, and currentSize
	 * @param size
	 */
	@SuppressWarnings("unchecked")
	LinkedHashMap(int size){
		lhmtable = new CacheEntry[size];
		this.size = size;
		currentSize = 0;
	}	
	
	/**
	 * Removes LeastRecentryUsed entry
	 */
	public void removeLeastRecentlyUsed() {
		double[] deletePair = (double[]) tail.getKey();
		remove(deletePair);
	}	
	
	/**
	 * Transfers entry at the top of the DoublyLinkedList
	 * @param key
	 * @param value
	 */
	public void moveToTop(K key, V value) {
		double[] topPair = (double[]) key;
		double[] headPair = (double[]) head.latlngPair;
		if(!Arrays.equals(topPair, headPair)){
			put(topPair,value);
		}
	}
	
	/**
	 * Puts entry at the hash table and doubly linked list
	 * @param key
	 * @param value
	 */
	@SuppressWarnings("unchecked")
	public void put(double[] pair, V value) {
		if(pair == null){
			return;	
		}
		Object hashd = pair[0]+pair[1]+pair[2]+pair[3];
		int hash = hash(hashd);	
		CacheEntry<K,V> newEntry = new CacheEntry<K,V>((K) pair, value, null);
		insertFirst(newEntry);	
		if(lhmtable[hash] == null) {
			lhmtable[hash] = newEntry;
			currentSize++;
		}else {
			CacheEntry<K,V> previous = null;
			CacheEntry<K,V> current = lhmtable[hash];
			while(current != null) {
				previous = current;
				current = current.next;
			}
			previous.next = newEntry;
			currentSize++;
		}	
	}
	
	/**
	 * Search entry on HashTable which has key
	 * If found return entrys' value
	 * @param key
	 * @return
	 */
	public V get(double[] pair) {
		Object hashd = pair[0]+pair[1]+pair[2]+pair[3];
		int hash = hash(hashd);	
		if(lhmtable[hash] == null) {
			return null;
		}else {
			CacheEntry<K,V> temp = lhmtable[hash];
			double[] tempPair;
			while(temp != null) {
				tempPair =(double[]) temp.getKey();
				if(Arrays.equals(tempPair, pair)) {
					return temp.getRouteList();
				}
				temp = temp.next;
				
			}
			return null;
		}
	}
	
	/**
	 * Removes entry on hash table and DoublyLinkedList which has deletedKey
	 * @param deleteKey
	 * @return
	 */
	public boolean remove(double[] deleteKeyPair) {	
		Object hashd = deleteKeyPair[0]+deleteKeyPair[1]+deleteKeyPair[2]+deleteKeyPair[3];
		int hash = hash(hashd);	
		if(lhmtable[hash] == null) {
			return false;
		}else {
			CacheEntry<K,V> previous = null;
			CacheEntry<K,V> current = lhmtable[hash];	
			double[] currentPair;
			while(current != null) {
				currentPair =(double[]) current.getKey();
				if(Arrays.equals(currentPair, deleteKeyPair)/*current.key.equals(deleteKey)*/) {
					updatePriorityAfterDeletion(current);
					if(previous == null) {
						lhmtable[hash] = lhmtable[hash].next;
						currentSize--;
						return true;
					}else {
						previous.next = current.next;
						currentSize--;
						return true;
					}
				}
				previous = current;
				current = current.next;
				
			}
			return false;
		}
	}

	/**
	 * Reconnects DoublyLinkedList after entry deletion.
	 */
	private void updatePriorityAfterDeletion(CacheEntry<K,V> deleteEntry) {
		double[] deletePair = (double[]) deleteEntry.latlngPair;
		double[] tailPair = (double[]) tail.getKey();
		if(Arrays.equals(tailPair,deletePair)){
			deleteLast();
			return;
		}
		deleteSpecificEntry(deleteEntry);
	}
	
	/**
	 * Inserts given entry at the top of DoubleLinkedList
	 * @param newEntry
	 */
	private void insertFirst(CacheEntry<K,V> newEntry) {
		if(head == null) {
			head = newEntry;
			tail = newEntry;
			return;
		}
		newEntry.after = head;
		head.before = newEntry;
		head = newEntry;
	}	
	
	/**
	 * Deletes last item in DoublyLinkedList
	 */
	private void deleteLast() {
		if(head == tail) {
			head = tail = null;
			return;
		}
		tail = tail.before;
		tail.after = null;
	}
	
	/**
	 * Deletes entry in EntryList based on entrys' key
	 * @param newEntry
	 * @return deletedEntry
	 */
	private CacheEntry<K,V> deleteSpecificEntry(CacheEntry<K,V> newEntry){
		CacheEntry<K,V> current = head;
		double[] newPair = (double[]) newEntry.latlngPair;
		double[] currentPair = (double[]) current.getKey();
		while(!Arrays.equals(currentPair,newPair)){
			if(current.after == null){
				return null;
			}
			current = current.after;
			currentPair =(double[]) current.getKey();
		}
		CacheEntry<K,V> beforeDeleteEntry = current.before;
		current.before.after = current.after;
		current.after.before = current.before;
		return beforeDeleteEntry;
	}
	
	/**
	 * Hash method to associate with hash buckets' index
	 * @param key
	 * @return hash value
	 */
	private int hash(Object key) {
		return Math.abs(key.hashCode()) % size;
	}
	
	
	/**
	 * Returns number of items in hashtable
	 * @return
	 */
	public int getCurrentSize() {
		return currentSize;
	}
}
