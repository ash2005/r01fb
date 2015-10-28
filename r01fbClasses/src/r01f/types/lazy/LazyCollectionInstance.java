package r01f.types.lazy;

import java.util.Collection;

import r01f.types.lazy.LazyCollectionsInterfaces.CollectionValuesSupplier;

public class LazyCollectionInstance<V>	
	  extends LazyCollectionBase<V> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public LazyCollectionInstance(final Collection<V> backEnd,
							      final CollectionValuesSupplier<V> valuesSupplier) {
		super(backEnd,valuesSupplier);
	}
}
