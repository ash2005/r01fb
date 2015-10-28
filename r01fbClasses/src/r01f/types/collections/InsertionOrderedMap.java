package r01f.types.collections;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.OrderedMap;
import org.apache.commons.collections.map.ListOrderedMap;

import com.google.common.collect.ForwardingMap;

/**
 * Mapa ordenado por el orden de inserción que añade metodos como: 
 * <code>firstKey</code>, <code>lastKey</code>, <code>nextKey</code>, <code>previousKey</code> 
 */
public class InsertionOrderedMap<K,V> extends ForwardingMap<K,V> {
///////////////////////////////////////////////////////////////////////////////
// 	MIEMBROS Y CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////	
	private final OrderedMap _delegate;
	/**
	 * Constructor
	 * @param delegate el mapa al que se delegan las llamadas
	 */
	public InsertionOrderedMap() {
		_delegate = new ListOrderedMap();		
	}
	public InsertionOrderedMap(int size) {
		_delegate = ListOrderedMap.decorate(new HashMap<K,V>(size));
	}
///////////////////////////////////////////////////////////////////////////////
// 	FACTORIA
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Método factoría del StackedMap
	 * @param instance instancia de un mapa que se quiere convertir en StackedMap
	 * @return el StackedMap
	 */
	public static <K,V> Map<K,V> create() {
		return new InsertionOrderedMap<K,V>();
	}
///////////////////////////////////////////////////////////////////////////////
// 	METODOS SOBRE-ESCRITOS
///////////////////////////////////////////////////////////////////////////////	
	@Override
	protected Map<K,V> delegate() {		
		@SuppressWarnings("unchecked")
		Map<K,V> outMap = _delegate;
		return outMap;
	}	
///////////////////////////////////////////////////////////////////////////////
// 	NUEVOS METODOS
///////////////////////////////////////////////////////////////////////////////	
	public K firstKey() {
		@SuppressWarnings("unchecked")		
		K firstKey = (K)_delegate.firstKey();
		return firstKey;
	}
	public K lastKey() {
		@SuppressWarnings("unchecked")
		K lastKey = (K)_delegate.lastKey();
		return lastKey;
	}
	public K nextKey(K ofKey) {
		@SuppressWarnings("unchecked")		
		K nextKey = (K)_delegate.nextKey(ofKey);
		return nextKey;
	}
	public K previousKey(Object ofKey) {
		@SuppressWarnings("unchecked")		
		K prevKey = (K)_delegate.previousKey(ofKey);
		return prevKey;
	}	

	
	
//	public static void main(String[] args) {
//		StackedMap<String,String> stackedMap = new StackedMap<String,String>();
//		stackedMap.put("1","uno");
//		stackedMap.put("2","dos");
//		System.out.println(stackedMap.nextKey(stackedMap.firstKey()) + ":" + stackedMap.get(stackedMap.nextKey(stackedMap.firstKey())));
//	}	
}
