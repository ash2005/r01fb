package r01f.types.lazy;

import java.util.Map;
import java.util.Set;

import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.types.lazy.LazyCollectionsInterfaces.KeyIntrospector;
import r01f.types.lazy.LazyCollectionsInterfaces.MapKeysSupplier;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Guava's {@link LoadingCache} wrapper to implement lazy-loaded {@link Map}
 * @param <K> keys type
 * @param <V> values type
 */
public class LazyCachedMap<K,V> 
     extends LazyMapBase<K,V> {
//////////////////////////////////////////////////////////////////////////////////////////////
//	MIEMBROS
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Underlying map
	 */
	@NotDirtyStateTrackable
	protected final LoadingCache<K,V> _currentEntries;
//////////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
//////////////////////////////////////////////////////////////////////////////////////////////
	LazyCachedMap(final boolean fullLoadedOnCreation,
				  final KeyIntrospector<K,V> keyIntrospector,
				  final Supplier<Set<K>> keySetSupplier,
				  final LoadingCache<K,V> currentEntriesMapInstance) {
		super(fullLoadedOnCreation,
			  keyIntrospector, 
			  keySetSupplier);
		
		_currentEntries = currentEntriesMapInstance;
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	LazyLoadedMap Builder
//////////////////////////////////////////////////////////////////////////////////////////////
	public static class LazyCachedMapFluentBuilder<K1,V1,C1> 
	            extends LazyMapFluentBuilderBase<K1,V1,C1> {
		@Override
		public LazyMapFluentBuilderBase<K1,V1,C1> backedBy(Class<?> type) { 
			throw new IllegalArgumentException("A LazyCachedMap is allways backed by a GUAVA's LoadingCache, so there is no need to specify the underlying Map type");
		}
		@Override
		public void checkBuilderParams() {
			// Check Params
			if (_keyIntrospector == null) throw new IllegalStateException("The KeyIntrospector is needed in order to introspect the key for a given value. The lazyLoadedMap cannot be built");
			if (_keySetSupplier == null) throw new IllegalStateException("The keySetSupplier is needed in order to know ALL the keys for the map. The lazyLoadedMap cannot be built");
			if (_valuesSupplier == null) throw new IllegalStateException("The valuesSupplier is needed in order to load lazily the value for a given key. The LazyLoadedMap cannot be built");
			if (_expirationPeriod <= 0) throw new IllegalStateException("The expirationPeriod is needed in order to know how ofter clear the cached value for a given key. The LazyLoadedMap cannot be built");
		}
		@Override @SuppressWarnings("unchecked")		
		public Map<K1,V1> build() {
			super.checkBuilderParams();		// Comprobar parametros
			
			// Memoize the keyset supplier
			Supplier<Set<K1>> theKeySetSupplier = Suppliers.memoize(new MapKeysSupplier<K1,C1>(_keySetSupplier,_keySetLoadCriteria));
			
			// Build the guava's cached Map
			LoadingCache<K1,V1> mapInstance = CacheBuilder.newBuilder()
												  		  .initialCapacity(_initialCapacity)
												  		  .expireAfterWrite(_expirationPeriod,_expirationPeriodTimeUnit)	// Expirar las entradas de la cache cada 5 min
												  		  .build((CacheLoader<K1,V1>)_valuesSupplier.asCacheLoader());
			// PUT the initial entries
			if (CollectionUtils.hasData(_initialEntries)) mapInstance.putAll(_initialEntries);
			
			// Build the map
			Map<K1,V1> outMap = new LazyCachedMap<K1,V1>(_fullLoadedOnCreation,
														 (KeyIntrospector<K1,V1>)_keyIntrospector,
											             theKeySetSupplier,
											             mapInstance);
			return outMap;
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	LAZY MAP
//////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isLoaded(final K key) {
		return _currentEntries.getIfPresent(key) != null;
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	ABSTRACT METHODS
//////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected V supplyValue(final K key) {
		// Simply ask for a new key to force Guava Cache ask for the value
		try {
			return _currentEntries.get(key);
		} catch(Exception ex) {
			throw new RuntimeException(ex);		// convert to RuntimeException
		}
	}
	@Override
	protected V putInCurrentEntries(final K key,final V value) {
		V outVal = _currentEntries.getIfPresent(key);		// Get the current value (if loaded)
		_currentEntries.put(key,value);						// put at the cache...
		return outVal;
	}
	@Override
	protected V removeFromCurrentEntries(final K key) {
		V outVal = _currentEntries.getIfPresent(key);
		if (outVal != null) _currentEntries.invalidate(key);	// remove from the cache
		return outVal;
	}
	@Override
	protected void clearCurrentEntries() {
		_currentEntries.invalidateAll();
	}
	@Override
	protected Map<K,V> currentEntries() {
		return _currentEntries.asMap();
	}
}
