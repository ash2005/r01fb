package r01f.util;

/**
 * Factoría de un objeto que contiene una clave y un valor  
 * @param <K> el tipo de key
 * @param <V> el ipo de value
 */
public interface KeyValueFactory<K,V> {
	public KeyValue<K,V> createFor(final K name,final V value);
}
