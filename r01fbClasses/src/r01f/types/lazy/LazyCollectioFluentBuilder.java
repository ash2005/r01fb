package r01f.types.lazy;

import java.util.Collection;

import lombok.RequiredArgsConstructor;
import r01f.patterns.IsBuilder;
import r01f.types.lazy.LazyCollectionsInterfaces.CollectionValuesSupplier;
import r01f.util.types.collections.CollectionUtils;

import com.google.appengine.api.search.checkers.Preconditions;

/**
 * lazily-loaded collection builder base
 * @param <V> value
 */
@RequiredArgsConstructor
public class LazyCollectioFluentBuilder<V> 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	protected final Collection<V> _backEndCol;
	protected Collection<V> _initialEntries;
	protected int _initialCapacity = -1;
	protected CollectionValuesSupplier<? extends V> _valuesSupplier;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the initial collection entries
	 * @param initialEntries
	 */
	public LazyCollectioFluentBuilder<V> withInitialEntries(final Collection<V> initialEntries) {
		_initialEntries = initialEntries;
		return this;
	}
	/**
	 * Collection values supplier
	 * @param valuesSupplier
	 */
	@SuppressWarnings("unchecked")
	public <V1 extends V> LazyCollectioFluentBuilder<V1> loadValuesWith(final CollectionValuesSupplier<V1> valuesSupplier) {
		_valuesSupplier = valuesSupplier;
		LazyCollectioFluentBuilder<V1> outM = (LazyCollectioFluentBuilder<V1>)this;
		return outM;		
	}
	/**
	 * Builds the lazy loaded collection
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public Collection<V> build() {
		Preconditions.checkArgument(_backEndCol != null,"The backend Collection MUST NOT be null");
		if (CollectionUtils.hasData(_initialEntries)) _backEndCol.addAll(_initialEntries);	// put all the initial entries into the lazy collection
		return new LazyCollectionInstance<V>(_backEndCol,
										 	 (CollectionValuesSupplier<V>)_valuesSupplier);
	}
}
