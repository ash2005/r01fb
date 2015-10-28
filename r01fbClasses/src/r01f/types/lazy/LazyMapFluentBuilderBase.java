package r01f.types.lazy;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import r01f.patterns.IsBuilder;
import r01f.types.lazy.LazyCollectionsInterfaces.KeyIntrospector;
import r01f.types.lazy.LazyCollectionsInterfaces.MapKeySetSupplier;
import r01f.types.lazy.LazyCollectionsInterfaces.MapValuesSupplier;
import r01f.util.types.collections.CollectionUtils;

/**
 * {@link LazyMap} builder
 * @param <K> key
 * @param <V> value
 * @param <C> loadKeySetWith() method criteria
 */
public abstract class LazyMapFluentBuilderBase<K,V,C> 
		   implements IsBuilder {
	
	protected Class<?> _underlyingMapType;
	protected int _initialCapacity;
	protected boolean _fullLoadedOnCreation;
	protected Map<K,V> _initialEntries;
	protected KeyIntrospector<? extends K,? extends V> _keyIntrospector;
	protected MapKeySetSupplier<? extends K,?> _keySetSupplier;
	protected C _keySetLoadCriteria;
	protected MapValuesSupplier<? extends K,? extends V> _valuesSupplier;
	protected long _expirationPeriod = 5;
	protected TimeUnit _expirationPeriodTimeUnit = TimeUnit.MINUTES;
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the underlying map type. This type MUST implement {@link Map}
	 * NOTE: This method does not apply to {@link LazyCachedMap} type
	 * @param the {@link Map} type (ie: {@link HashMap}, {@link LinkedHashMap}, etc)
	 */
	public LazyMapFluentBuilderBase<K,V,C> backedBy(final Class<?> type) {
		_underlyingMapType = type;
		return this;
	}
	/**
	 * Sets the initial {@link Map} entries
	 * @param currentEntries the initial entries
	 */
	public LazyMapFluentBuilderBase<K,V,C> withEntries(final Map<K,V> currentEntries) {
		if (CollectionUtils.hasData(currentEntries)) {
			_initialEntries = currentEntries;
		}
		return this;
	}
	/**
	 * Sets that the {@link Map} loading is completelly done at creation time
	 */
	public LazyMapFluentBuilderBase<K,V,C> fullLoadedOnCreation() {
		_fullLoadedOnCreation = true;
		return this;
	}
	/**
	 * Sets the initial {@link Map} capacity
	 * @param initialCapacity initial capacity
	 */
	public LazyMapFluentBuilderBase<K,V,C> initialCapacity(final int initialCapacity) {
		_initialCapacity = initialCapacity;
		return this;
	}
	/**
	 * Sets an object that helps into extracting the key from the value object
	 * @param keyIntrospector
	 */
	@SuppressWarnings("unchecked")
	public <K1 extends K,V1 extends V> 
		   LazyMapFluentBuilderBase<K1,V1,C> introspectKeyFromValueWith(final KeyIntrospector<K,V> keyIntrospector) {
		_keyIntrospector = keyIntrospector;
		LazyMapFluentBuilderBase<K1,V1,C> outM = (LazyMapFluentBuilderBase<K1,V1,C>)this;
		return outM;
	}
	/** 
	 * Sets a key supplier that hands all the {@link Map} keys
	 * @param keySetSupplier 
	 * @param criteria filter criteria that is handed to the supplier to do it's job
	 * 			  	   it could be for example
	 * 					<ul>
	 * 						<li>an object with filter data</li>
	 * 						<li>the lazy-loaded Map container object whose oid could be used to filter Map entries</li>
	 * 					</ul> 
	 */
	@SuppressWarnings("unchecked")
	public <K1 extends K,V1 extends V,C1 extends C> 
		   LazyMapFluentBuilderBase<K1,V1,C1> loadKeySetWith(final MapKeySetSupplier<K1,C1> keySetSupplier,
												             final C1 criteria) {
		_keySetSupplier = keySetSupplier;
		_keySetLoadCriteria = criteria;
		LazyMapFluentBuilderBase<K1,V1,C1> outM = (LazyMapFluentBuilderBase<K1,V1,C1>)this;
		return outM;
	}
	/**
	 * Values Supplier
	 * @param valuesSupplier
	 */
	@SuppressWarnings("unchecked")
	public <K1 extends K,V1 extends V> 
		   LazyMapFluentBuilderBase<K1,V1,C> loadValuesWith(final MapValuesSupplier<K1,V1> valuesSupplier) {
		_valuesSupplier = valuesSupplier;
		LazyMapFluentBuilderBase<K1,V1,C> outM = (LazyMapFluentBuilderBase<K1,V1,C>)this;
		return outM;		
	}
	/**
	 * Key expiration policy (if it exists)
	 * @param period
	 * @param timeUnit
	 */
	public LazyMapFluentBuilderBase<K,V,C> expiringEntriesEvery(long period,TimeUnit timeUnit) {
		if (period > 0) _expirationPeriod = period;
		_expirationPeriodTimeUnit = timeUnit;
		return this;
	}
	/**
	 * Check the params
	 * Usually if any param is NOT correct, it throws an {@link IllegalArgumentException} 
	 */
	public void checkBuilderParams() {
		// usually is overriden
	}
	/**
	 * Builds the Map
	 * @return the lazily loaded map
	 */
	public abstract <K1 extends K,V1 extends V> Map<K1,V1> build();
}
