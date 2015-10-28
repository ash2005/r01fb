package r01f.aspects.core.freezable;

import java.io.Serializable;
import java.util.Collection;

import lombok.Delegate;
import r01f.util.types.collections.CollectionUtils.CollectionMutatorMethods;

/**
 * Wrap de una colección que controla si está congelada o no
 * @param <V> value
 */
public class FreezableCollection<V> implements Collection<V>,
											   Serializable {
	private static final long serialVersionUID = -2955026845211623617L;
///////////////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Delegate(excludes=CollectionMutatorMethods.class)
	private final Collection<V> _col;
	
	private final boolean _frozen;
///////////////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////////////
	public FreezableCollection(final Collection<V> theCol,final boolean frozen) {
		_col = theCol;
		_frozen = frozen;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	OVERRIDE
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean add(V e) {
		if (_frozen) throw new IllegalStateException("The collection is FROZEN! you cannot add anything on it. This is because the obj where the collection is contained is frozen");
		return _col.add(e);
	}
	@Override
	public boolean addAll(Collection<? extends V> c) {
		if (_frozen) throw new IllegalStateException("The collection is FROZEN! you cannot add anything on it. This is because the obj where the collection is contained is frozen");	
		return _col.addAll(c);
	}
	@Override
	public void clear() {
		if (_frozen) throw new IllegalStateException("The collection is FROZEN! you cannot clear its contents. This is because the obj where the collection is contained is frozen");
		_col.clear();
	}
	@Override
	public boolean remove(Object o) {
		if (_frozen) throw new IllegalStateException("The collection is FROZEN! you cannot remove anything. This is because the obj where the collection is contained is frozen");
		return _col.remove(o);
	}
	@Override
	public boolean removeAll(Collection<?> c) {
		if (_frozen) throw new IllegalStateException("The collection is FROZEN! you cannot remove anything. This is because the obj where the collection is contained is frozen");
		return _col.removeAll(c);
	}
	@Override
	public boolean retainAll(Collection<?> c) {
		if (_frozen) throw new IllegalStateException("The collection is FROZEN! you cannot remove anything. This is because the obj where the collection is contained is frozen");
		return _col.retainAll(c);
	}
}
