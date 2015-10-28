package r01f.aspects.core.freezable;

import java.io.Serializable;
import java.util.Map;

import lombok.Delegate;
import r01f.util.types.collections.CollectionUtils.MapMutatorMethods;

/**
 * Wrap de un mapa que controla si está congelado o no
 * @param <K>
 * @param <V>
 */
public class FreezableMap<K,V> implements Map<K,V>,
										  Serializable {
	private static final long serialVersionUID = 7785966352446057153L;

/////////////////////////////////////////////////////////////////////////////////////////
//	ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	@Delegate(excludes=MapMutatorMethods.class)
	private final Map<K,V> _map;
	
	private final boolean _frozen;
	
///////////////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////////////
	public FreezableMap(final Map<K,V> theMap,final boolean frozen) {
		_map = theMap;
		_frozen = frozen;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METODOS MUTATOR
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public V put(K key,V value) {
		if (_frozen) throw new IllegalStateException("The map is FROZEN! you cannot put anything in it. This is because the obj where the map is contained is frozen");
		return _map.put(key,value);
	}
	@Override
	public void putAll(Map<? extends K,? extends V> m) {
		if (_frozen) throw new IllegalStateException("The map is FROZEN! you cannot put anything in it. This is because the obj where the map is contained is frozen");
		_map.putAll(m);
	}
	@Override
	public void clear() {
		if (_frozen) throw new IllegalStateException("The map is FROZEN! you cannot clear its values. This is because the obj where the map is contained is frozen");
		_map.clear();
	}
	@Override
	public V remove(Object key) {
		if (_frozen) throw new IllegalStateException("The map is FROZEN! you cannot remove any key. This is because the obj where the map is contained is frozen");
		return _map.remove(key);
	}
}
