package r01f.types.lazy;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.Getter;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheLoader;

public class LazyCollectionsInterfaces {
//////////////////////////////////////////////////////////////////////////////////////////////
//	KeyIntrospector
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Interface to access the key for a type to be indexed in a {@link Map}
	 * The type implementing this interface could use many ways to guess an object's key:
	 * 		- A instrospecto for each object type:
	 * 			KeyIntrospector<String,MyObj> intr = new KeyIntrospector<String,MyObj>() {
	 * 														@Override
	 * 														public String of(MyObj obj) {
	 * 															return obj.getTheKey();
	 * 														} 
	 * 												 }
	 * 		- Use reflection to find a @OidField -annotated field
	 */
	public interface KeyIntrospector<K,V> {
		/**
		 * Returns an object's instance key to be used as a {@link Map.Entry} key
		 * @param obj
		 * @return
		 */
		public K of(V obj);
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	Maps suppliers
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * KeySet supplier
	 * @param <K> map's keys type
	 * @param <C> filter criteria for the elements to be loaded
	 */
	public static interface MapKeySetSupplier<K,C> {
		public Set<K> keysFor(C c);
	}
	/**
	 * Values supplier
	 * @param <K> map's keys type
	 * @param <V> map's values type
	 */
	public abstract static class MapValuesSupplier<K,V> {
		public abstract V loadValue(K key);
		/**
		 * Returns a supplier as a Guava's {@link com.google.common.cache.CacheLoader}
		 * @return
		 */
		public CacheLoader<K,V> asCacheLoader() {
			return new CacheLoader<K,V>() {
							@Override
							public V load(K key) throws Exception {
								return MapValuesSupplier.this.loadValue(key);
							}
			};
		}
	}
	@SuppressWarnings("unchecked")
	public static class MapKeysSupplier<K,C> 
	         implements Supplier<Set<K>> {
		
		@Getter private final MapKeySetSupplier<K,C> _keySetSupplier;
		@Getter private final C _criteria;
		public MapKeysSupplier(final Object keySetSupplier,
							   final Object criteria) {
			_keySetSupplier = (MapKeySetSupplier<K,C>)keySetSupplier;
			_criteria = (C)criteria;
		}
		@Override
		public Set<K> get() {
			return _keySetSupplier.keysFor(_criteria);
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	Collections suppliersO
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Supplier de values para una colección
	 * @param <V> tipo de los valores de la colección
	 */
	public static interface CollectionValuesSupplier<V> {
		public Collection<V> loadValues();
	}
}
