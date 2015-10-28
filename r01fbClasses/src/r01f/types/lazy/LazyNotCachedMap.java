package r01f.types.lazy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.reflection.ReflectionUtils;
import r01f.types.lazy.LazyCollectionsInterfaces.KeyIntrospector;
import r01f.types.lazy.LazyCollectionsInterfaces.MapKeysSupplier;
import r01f.types.lazy.LazyCollectionsInterfaces.MapValuesSupplier;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * {@link Map} wrapper that implements Lazy loading
 * @param <K> Map keys type
 * @param <V> Map values type
 */
public class LazyNotCachedMap<K,V> 
     extends LazyMapBase<K,V> {
//////////////////////////////////////////////////////////////////////////////////////////////
//	MIEMBROS
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * underlying map
	 */
	@NotDirtyStateTrackable
	protected final Map<K,V> _currentEntries;
	/**
	 * values supplier
	 */
	@NotDirtyStateTrackable
	protected final MapValuesSupplier<K,V> _valuesSupplier;
//////////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
//////////////////////////////////////////////////////////////////////////////////////////////
	LazyNotCachedMap(final boolean fullLoadedOnCreation,
					 final KeyIntrospector<K,V> keyIntrospector,
					 final Supplier<Set<K>> keySetSupplier,final MapValuesSupplier<K,V> valuesSupplier,
					 final Map<K,V> currentEntriesMapInstance) {
		super(fullLoadedOnCreation,
			  keyIntrospector, 
			  keySetSupplier);
		_valuesSupplier = valuesSupplier;
		_currentEntries = currentEntriesMapInstance;
	}

//////////////////////////////////////////////////////////////////////////////////////////////
//	LazyLoadedMap Fluent builder
//////////////////////////////////////////////////////////////////////////////////////////////
	public static class LazyNotCachedMapFluentBuilder<K1,V1,C1> 
	            extends LazyMapFluentBuilderBase<K1,V1,C1> {
		@Override
		public LazyMapFluentBuilderBase<K1,V1,C1> expiringEntriesEvery(final long period,
															       	   final TimeUnit timeUnit) {
			throw new UnsupportedOperationException("Entries expiration is NOT supported in a LazyMap; only in a CachedLazyMap");
			//CheckedToUnCheckedExceptionConverter.throwUnchecked(new MethodNotSupportedException("Entries expiration is NOT supported in a LazyMap; only in a CachedLazyMap"));
		}
		@Override
		public void checkBuilderParams() {
			// Check params: if the Map is NOT fully loaded on creation keys and values suppliers MUST be provided as so the KeyIntrospector
			if (!_fullLoadedOnCreation && _keyIntrospector == null) throw new IllegalStateException("The KeyIntrospector is needed in order to introspect the key for a given value. The lazyLoadedMap cannot be built");
			if (!_fullLoadedOnCreation && _keySetSupplier == null) throw new IllegalStateException("The keySetSupplier is needed in order to know ALL the keys for the map. The lazyLoadedMap cannot be built");
			if (!_fullLoadedOnCreation && _valuesSupplier == null) throw new IllegalStateException("The valuesSupplier is needed in order to load lazily the value for a given key. The LazyLoadedMap cannot be built");
		}
		@Override @SuppressWarnings("unchecked")		
		public Map<K1,V1> build() {
			super.checkBuilderParams();		// Check params
			
			// Memoize the key supplier
			Supplier<Set<K1>> theKeySetSupplier = _keySetSupplier != null ? Suppliers.memoize(new MapKeysSupplier<K1,C1>(_keySetSupplier,
																							   							 _keySetLoadCriteria))
																		  : null;
			
			// Build the underlying map
			Map<K1,V1> mapInstance = null;
			if (_underlyingMapType == null || _underlyingMapType == HashMap.class) {
				mapInstance = _initialCapacity > 0 ? new HashMap<K1,V1>(_initialCapacity) 
								 				   : new HashMap<K1,V1>();
			} else if (_initialCapacity > 0) {
				mapInstance = ReflectionUtils.createInstanceOf(_underlyingMapType,
												 			   new Class<?>[] {Integer.class},new Integer[] {_initialCapacity});
			} else {
				mapInstance = ReflectionUtils.createInstanceOf(_underlyingMapType);
			}
			// Add the initial map entries
			if (CollectionUtils.hasData(_initialEntries)) mapInstance.putAll(_initialEntries);
			
			// Build the LazyNotCachedMap
			Map<K1,V1> outMap = new LazyNotCachedMap<K1,V1>(_fullLoadedOnCreation,
															(KeyIntrospector<K1,V1>)_keyIntrospector,
											           		theKeySetSupplier,(MapValuesSupplier<K1,V1>)_valuesSupplier,
											           		mapInstance);
			return outMap;
		}
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	LazyMap
//////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isLoaded(final K key) {
		return _currentEntries.containsKey(key);
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	Abstract methods
//////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected V supplyValue(final K key) {
		// Use the value supplier to lazily load the value
		V outVal = null;
		try {
			outVal = _valuesSupplier.loadValue(key);
			if (outVal != null) _currentEntries.put(key,outVal);
		} catch(Exception ex) {
			throw new RuntimeException(ex);		// Transform any exception to a RuntimeException
		}
		return outVal;
	}
	@Override
	protected V putInCurrentEntries(final K key,final V value) {
		V outVal = _currentEntries.put(key,value);		// PUT at the underlying map
		return outVal;
	}
	@Override
	protected V removeFromCurrentEntries(final K key) {
		V outVal = _currentEntries.remove(key);
		return outVal;
	}
	@Override
	protected void clearCurrentEntries() {
		_currentEntries.clear();
	}
	@Override
	protected Map<K,V> currentEntries() {
		return _currentEntries;
	}	
}
