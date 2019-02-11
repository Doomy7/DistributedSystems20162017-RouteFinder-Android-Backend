public class CacheEntry<K,V> {

	K latlngPair;
	V routeList;
	CacheEntry<K,V> next,before,after;
	
	CacheEntry(K latlngPair, V routeList, CacheEntry<K,V> next){
		this.latlngPair = latlngPair;
		this.routeList = routeList;
		this.next = next;
	}
	
	K getKey(){
		return latlngPair;
	}
	
	V getRouteList(){
		return routeList;
	}
}
