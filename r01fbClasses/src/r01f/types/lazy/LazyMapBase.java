package r01f.types.lazy;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.types.dirtytrack.DirtyTrackAdapter;
import r01f.types.dirtytrack.internal.CollectionChangesTracker;
import r01f.types.lazy.LazyCollectionsInterfaces.KeyIntrospector;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * {@link LazyMap} base type
 * @param <K> Map key type
 * @param <V> Map value type
 */
abstract class LazyMapBase<K,V> 
       extends LazyChangesTrackerBase<K>
    implements Map<K,V>,
    		   LazyMap<K,V> {
//////////////////////////////////////////////////////////////////////////////////////////////
//	ESTADO
//////////////////////////////////////////////////////////////////////////////////////////////
	@NotDirtyStateTrackable
	protected final boolean _fullLoadedOnCreation;		// Sets if the map is FULLY loaded when it's created
	
	@NotDirtyStateTrackable
	protected final KeyIntrospector<K,V> _keyIntrospector;
	
	@NotDirtyStateTrackable
	protected final Supplier<Set<K>> _keySetSupplier;
	
//////////////////////////////////////////////////////////////////////////////////////////////
//	PRIVATE CONSTRUCTOR 
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Inicializa el mapa
	 * @param fullLoadedOnCreation Indica si el mapa se carga COMPLETAMENTE cuando se crea (la primera vez que se accede)
	 * @param keyIntrospector clase que implementa {@link r01f.types.lazy.LazyCollectionsInterfaces.KeyIntrospector}
	 * 						  y que obtiene la clave a partir de un elemento del mapa
	 * @param keySetSupplier supplier de las claves de los elementos del mapa
	 */
	LazyMapBase(final boolean fullLoadedOnCreation,
				final KeyIntrospector<K,V> keyIntrospector,
				final Supplier<Set<K>> keySetSupplier) {
		// ¿el mapa se carga completamente en la creación? 
		_fullLoadedOnCreation = fullLoadedOnCreation;

		// Introspector de claves: obtiene la key a partir de un objeto
		_keyIntrospector = keyIntrospector;
		
		// Cache que gestiona el LazyLoad
		_keySetSupplier = keySetSupplier;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ ChangesTrackableLazyMap
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CollectionChangesTracker<K> getChangesTracker() {
		return _changesTracker;
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	METODOS ABSTRACTOS
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Provee de un valor a partir de una clave
	 * @param key la clave
	 * @return el valor
	 */
	protected abstract V supplyValue(final K key);
	/**
	 * Inserta una nueva entrada en el mapa subyacente
	 * @param key la clave
	 * @param value el valor
	 * @return el antiguo valor presente en la colección subyacente
	 */
	protected abstract V putInCurrentEntries(final K key,final V value);
	/**
	 * Elimina una entrada en el mapa subyacente 
	 * @param key la clave de la entrada a eliminar
	 * @return la entrada eliminada si estaba en el mapa, null en caso contrario
	 */
	protected abstract V removeFromCurrentEntries(final K key);
	/**
	 * Borra todas las entradas del mapa subyacente
	 */
	protected abstract void clearCurrentEntries();
	/**
	 * Devuelve el mapa subyacente como un mapa
	 * @return el mapa de entradas cargadas
	 */
	protected abstract Map<K,V> currentEntries();
//////////////////////////////////////////////////////////////////////////////////////////////
//	INTERFAZ MAP
//////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int size() {
		int outSize = 0;
		Set<K> actualKeys = _actualKeys();
		outSize = actualKeys != null ? actualKeys.size() : 0;
		return outSize;
	}
	@Override
	public boolean isEmpty() {
		boolean outEmpty = true;
		Set<K> actualKeys = _actualKeys();
		outEmpty = actualKeys != null ? actualKeys.size() == 0 : true;
		return outEmpty;
	}
	@Override
	public boolean containsKey(Object key) {
		boolean outContains = false;
		Set<K> actualKeys = _actualKeys();
		outContains = actualKeys != null ? actualKeys.contains(key) : false;
		return outContains;
	}
	@Override 	@SuppressWarnings("unchecked")
	public boolean containsValue(Object value) {
		boolean outContains = false;
		K key = _keyIntrospector.of((V)value);
		if (key != null) outContains = (this.get(key) != null);
		return outContains; 
	}
	@Override @SuppressWarnings("unchecked")
	public V get(Object key) {
		if (key == null) return null;
		V outVal = null;	
		// NO permitir poner una key que NO esté dentro de las claves iniciales o de las nuevas
		boolean isSuppliedKey = _fullLoadedOnCreation ? false 
													  : _keySetSupplier.get().contains(key);
		boolean isNewOrUpdatedKey = this.isLoaded((K)key);
		boolean isValidKey = isSuppliedKey || isNewOrUpdatedKey;
		if (isValidKey && !this.isLoaded((K)key)) {
			outVal = this.supplyValue((K)key);
		} else {
			Map<K,V> currentEntries = this.currentEntries();
			outVal = currentEntries != null ? currentEntries.get(key) : null;
		}
		return outVal;
	}
	@Override
	public V put(K key,V value) {
		V outVal = null;
		if (key != null) {
			// Si el value es DirtyStateTrackable y se está haciendo tracking, 
			// extender este estado
			if (value instanceof DirtyStateTrackable 
			 && this.getTrackingStatus().isThisDirtyTracking() 
			 && !DirtyTrackAdapter.adapt(value).getTrackingStatus().isThisDirtyTracking()) {
				DirtyTrackAdapter.adapt(value).startTrackingChangesInState(true);
			}
			// Poner el elemento en el mapa y apuntar el cambio
			outVal = this.putInCurrentEntries(key,value);	// Poner en el mapa subyacente
			_changesTracker.trackEntryInsertion(key);		// apuntar la nueva entrada
		}
		return outVal;
	}
	@Override
	public void putAll(Map<? extends K,? extends V> m) {
		if (!CollectionUtils.isNullOrEmpty(m)) {
			for (Map.Entry<? extends K,? extends V> me : m.entrySet()) {
				this.put(me.getKey(),me.getValue());
			}
		}
	}
	@Override
	public Set<K> keySet() {
		Set<K> outKeySet = null;
		outKeySet = _actualKeys(); 	// outKeySet = _keySetSupplier.get();
		return outKeySet;
	}
	@Override
	public Collection<V> values() {
		Collection<V> outCol = null;
		outCol = new AbstractUnmodifiableEntriesSet<V>(_actualKeys()) {
						@Override
						public Iterator<V> iterator() {
							return new Iterator<V>() {
											@SuppressWarnings("unchecked")
											private Iterator<K> _keySetIterator = ((Set<K>)_keys).iterator();
											@Override
											public boolean hasNext() {
												return _keySetIterator.hasNext();
											}
											@Override
											public V next() {
												K nextKey = _keySetIterator.next();
												// IMPORTANTE!!! aquí se fuerza la carga de la clave 
												//				 si es que NO está cargada
												return LazyMapBase.this.get(nextKey);
											}
											@Override
											public void remove() {
												throw new UnsupportedOperationException ("The values() collection of a lazily loaded cache does not supports remove() method");
											}
										};
						}
						@Override
						public boolean contains(Object o) {
							return LazyMapBase.this.containsValue(o);
						}
						@Override
						public boolean containsAll(Collection<?> c) {
							boolean outContains = false;
							if (!CollectionUtils.isNullOrEmpty(c) && !_keySetSupplier.get().isEmpty()) {
								outContains = true;
								for (Object val : c) {
									if (!LazyMapBase.this.containsValue(val)) {
										outContains = false;
										break;
									}
								}
							}
							return outContains;
						}
						@Override
						public Object[] toArray() {
							return LazyMapBase.this.currentEntries().values().toArray();
						}
						@Override
						public <T> T[] toArray(T[] a) {
							Collection<V> values = LazyMapBase.this.currentEntries().values();
							return values.toArray(a);
						}
			 	};
		return outCol;
	}
	@Override
	public Set<Map.Entry<K, V>> entrySet() {
		Set<Map.Entry<K,V>> outEntrySet = null;
		outEntrySet = new AbstractUnmodifiableEntriesSet<Map.Entry<K,V>>(_actualKeys()) {
							@Override
							public Iterator<Map.Entry<K,V>> iterator() {
								return new Iterator<Map.Entry<K,V>>() {
												@SuppressWarnings("unchecked")
												private Iterator<K> _keySetIterator = ((Set<K>)_keys).iterator();
												@Override
												public boolean hasNext() {
													return _keySetIterator.hasNext();
												}
												@Override
												public Map.Entry<K,V> next() {
													K nextKey = _keySetIterator.next();
													// IMPORTANTE!!! aquí se fuerza la carga de la clave 
													//				 si es que NO está cargada
													return Maps.immutableEntry(nextKey,
																			   LazyMapBase.this.get(nextKey));
												}
												@Override
												public void remove() {
													throw new UnsupportedOperationException ("The values() collection of a lazily loaded cache does not supports remove() method");
												}
											};
							}
							@Override	@SuppressWarnings("unchecked")
							public boolean contains(Object o) {
								Map.Entry<K,V> me = (Map.Entry<K,V>)o;
								return LazyMapBase.this.containsKey(me.getKey());
							}
							@Override 	@SuppressWarnings("unchecked")
							public boolean containsAll(Collection<?> c) {
								boolean outContains = false;
								if (!CollectionUtils.isNullOrEmpty(c) && !_keySetSupplier.get().isEmpty()) {
									outContains = true;
									for (Object entry : c) {
										Map.Entry<K,V> me = (Map.Entry<K,V>)entry;
										if (!LazyMapBase.this.containsKey(me.getKey())) {
											outContains = false;
											break;
										}
									}
								}
								return outContains;
							}
							@Override
							public Object[] toArray() {
								return LazyMapBase.this.currentEntries().entrySet().toArray();
							}
							@Override
							public <T> T[] toArray(T[] a) {
								Set<Map.Entry<K,V>> entries = LazyMapBase.this.currentEntries().entrySet();
								return entries.toArray(a);
							}
					 };
		return outEntrySet;
	}
	@Override	@SuppressWarnings("unchecked")
	public V remove(Object key) {
		if (key == null) return null;
		V outVal = this.get(key);		// Forzar la carga del elemento a borrar
		if (outVal != null) {
			outVal = this.removeFromCurrentEntries((K)key);						// sacar del mapa subyacente
			if (outVal != null) _changesTracker.trackEntryRemoval((K)key);		// apuntar el borrado
		}
		return outVal;
	}
	@Override
	public void clear() {
		for (K k : this.currentEntries().keySet()) {
			_changesTracker.trackEntryRemoval(k);		// apuntar el borrado
		}
		this.clearCurrentEntries();
	}
	@Override
	public boolean removeLazily(final K key) {
		if (key == null) return false;
		// NO es necesario cargar el valor si NO estaba cargado
		boolean existsKey = this.containsKey(key);
		if (existsKey) {
			if (this.isLoaded(key)) this.removeFromCurrentEntries(key);
			_changesTracker.trackEntryRemoval(key);
		}
		return existsKey;
	}	
//////////////////////////////////////////////////////////////////////////////////////////////
// 	Conjunto de claves ACTUALES: inicialmente el conjunto de claves se obtiene del objeto
//								 keySetSupplier, pero a medida que se van cargando y borrando 
//								 claves del mapa, el conjunto varia
//	claves REALES = claves iniciales + claves nuevas - claves borradas
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return claves iniciales + claves nuevas - claves borradas
	 */
	protected Set<K> _actualKeys() {
		Set<K> originalKeys = null;
		if (_fullLoadedOnCreation) {
			 originalKeys = this.currentEntries().keySet();
		} else {
			originalKeys = _keySetSupplier != null ? _keySetSupplier.get() : null;
		}
		return _changesTracker.currentKeys(originalKeys);	
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PARA SABER SI EL MAPA ESTÁ COMPLETAMENTE CARGADO
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isFullyLoaded() {
		if (_fullLoadedOnCreation) return true;
		boolean outLoaded = true;
		Set<K> keys = _keySetSupplier.get();
		if (CollectionUtils.hasData(keys)) {
			for (K key : keys) {
				boolean thisLoaded = this.isLoaded(key);
				if (!thisLoaded) {
					outLoaded = false;
					break;
				}
			}
		}
		return outLoaded;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METODOS PARA RECUPERAR LOS DATOS CARGADOS EN EL MOMENTO
//	(Devuelve SOLO aquellos que han sido accedidos y por lo tanto se han cargado, 
//	 NO devuelve por lo tanto aquellos que puedan formar parte del mapa pero NO se
//	 accedido y por lo tanto cargado)
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public Map<K,V> loadedEntriesAsMap() {
		return this.currentEntries();
	}
	@Override
	public Set<Map.Entry<K,V>> loadedEntrySet() {
		Map<K,V> currentEntries = this.currentEntries();
		return currentEntries != null ? currentEntries.entrySet() : null;
	}
	@Override
	public Set<K> loadedKeySet() {
		Map<K,V> currentEntries = this.currentEntries();
		return currentEntries != null ? currentEntries.keySet() : null;
	}
	@Override
	public Collection<V> loadedValues() {
		Map<K,V> currentEntries = this.currentEntries();
		return currentEntries != null ? currentEntries.values() : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METODOS PARA RECUPERAR ENTRADAS EN FUNCION DE SI SON NUEVAS, BORRADAS U ORIGINALES
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Set<K> newKeys() {
		return _changesTracker.getNewEntries();
	}
	@Override
	public Set<K> removedKeys() {
		return _changesTracker.getRemovedEntries();
	}
	@Override
	public Set<K> notNewOrRemovedKeys() {
		return this.notNewOrRemovedKeys(false);
	}
	@Override
	public Set<K> notNewOrRemovedKeys(boolean onlyLoaded) {
		// Si onlyLoaded = true solo se filtran las entradas borradas de las originales
		// de entre aquellas cargadas
		Map<K,V> currentEntries = this.currentEntries();
		Set<K> setToFilter = onlyLoaded ? (currentEntries != null ? currentEntries.keySet() : null)	// solo las entradas cargadas
										: _keySetSupplier.get();									// todas las entradas
		// Filtrar las entradas borradas de las entradas originales
		Set<K> outNotNewOrRemoved = Sets.filter(setToFilter,
						   						Predicates.not(_changesTracker.newOrDeletedEntriesFilter));		// que NO esté entre las borradas
		return outNotNewOrRemoved;
	}
	@Override
	public Map<K,V> newEntries() {
		Map<K,V> outEntries = null;		
		final Set<K> newKeys = this.newKeys();
		if (!CollectionUtils.isNullOrEmpty(newKeys)) {
			Predicate<K> filter = new Predicate<K>() {
											@Override
											public boolean apply(K key) {
												return newKeys.contains(key);
											}
									};
			outEntries = Maps.filterKeys(this.loadedEntriesAsMap(),	// OJO!! no poner this ya que Maps.filterKeys() llamaría a entrySet() que fuerza la carga de cada elemento
										 filter);
		}
		return outEntries;
	}
	@Override
	public Map<K,V> notNewOrRemovedEntries() {
		// Cuidado!!! fuerza la carga de todas las entradas que cumplen el filtro (ver entrySet())
		//			  si NO es necesario cargar los valores utilizar notNewOrRemovedKeys() en lugar de notNewOrRemovedEntries()
		return this.notNewOrRemovedEntries(false);
	}
	@Override
	public Map<K,V> notNewOrRemovedEntries(boolean onlyLoaded) {
		Map<K,V> outEntries = null;
		final Set<K> notNewOrRemovedKeys = this.notNewOrRemovedKeys(onlyLoaded);	// solo las claves no nuevas ni borradas
		if (!CollectionUtils.isNullOrEmpty(notNewOrRemovedKeys)) {
			Predicate<K> filter = new Predicate<K>() {
											@Override
											public boolean apply(K key) {
												return notNewOrRemovedKeys.contains(key);
											}
									};
			Map<K,V> mapToFilter = onlyLoaded ? this.loadedEntriesAsMap()	// solo las cargadas
											  : this;						// todas --> Cuidado!!! fuerza la carga de todas las entradas que cumplen el filtro (ver entrySet())
																			// 						si NO es necesario cargar los valores utilizar notNewOrRemovedKeys() en lugar de notNewOrRemovedEntries()
			outEntries = Maps.filterKeys(mapToFilter,filter);
		}
		return outEntries;
	}
	@Override 
	public Map<K,V> notNewOrRemovedDirtyEntries() {
		Map<K,V> outNotNewOrRemovedDirtyEntries = null;
		
		Map<K,V> notNewOrRemovedEntries = this.notNewOrRemovedEntries(true);	// SOLO entradas cargadas
		final Collection<K> dirtyKeys = Sets.newHashSet();
		if (CollectionUtils.hasData(notNewOrRemovedEntries)) {
			// Obtener una colección de las claves de los elementos sucios
			for (Map.Entry<K,V> me : notNewOrRemovedEntries.entrySet()) {
				// Si el valor es una instancia de DirtyStateTrackable, ver está "sucia"
				if (me.getValue() != null 
				 && me.getValue() instanceof DirtyStateTrackable && DirtyTrackAdapter.adapt(me.getValue()).isDirty()) {
					dirtyKeys.add(me.getKey());
				}
			}
			// Filtrar el mapa original para dejar solo las entradas que estén en dirtyKeys
			if (CollectionUtils.hasData(dirtyKeys)) {
				outNotNewOrRemovedDirtyEntries = Maps.filterKeys(notNewOrRemovedEntries,new Predicate<K>() {
																								@Override
																								public boolean apply(K key) {
																									return dirtyKeys.contains(key);
																								}
																							});
			}
		}
		return outNotNewOrRemovedDirtyEntries;
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	CONCILIAR KeySet CON LOS CAMBIOS EN CLIENTE
//////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Set<K> conciliateKeySetOnValues(final Set<K> suppliedKeySet,
								   		   final Predicate<V> filterOnValues) {
		Set<K> outKeys = suppliedKeySet;
		
		// Tener en cuenta que en el cliente pueden haberse:
		//		1.- Eliminado entradas
		//		2.- Creado nuevas entradas que cumplen el filtro
		// ... y puede que aún NO se hayan guardado los cambios, así que hay que "conciliar" el keySet 
		//	   devuelto por el supplier con los cambios del cliente
		//		1.- Eliminar las entradas devueltas por el servidor pero que se han borrado en el cliente
		//		2.- Añadir las nuevas entradas del cliente que aún no están en el servidor
		if ( _changesTracker.isDirty() ) {
			// [1]-Eliminar entradas borradas
			outKeys = _removeRemovedKeys(suppliedKeySet);
			
			// [2]-Añadir entradas nuevas
			final Set<K> newKeys = _changesTracker.getNewEntries();
			if (CollectionUtils.hasData(newKeys)) {
				// Añadir la lista de nuevas que cumplan las condiciones
				Map<K,V> matchingValues = Maps.filterValues(this.loadedEntriesAsMap(),	// SOLO entradas cargadas
															filterOnValues);
				if (outKeys == null) outKeys = Sets.newHashSet();
				outKeys.addAll(matchingValues.keySet());
			}
		} 
		return outKeys;
	}
	@Override
	public Set<K> conciliateKeySetOnKeys(final Set<K> suppliedKeySet,
								   		 final Predicate<K> filterOnKeys) {
		Set<K> outKeys = suppliedKeySet;
		
		// Tener en cuenta que en el cliente pueden haberse:
		//		1.- Eliminado entradas
		//		2.- Creado nuevas entradas que cumplen el filtro
		// ... y puede que aún NO se hayan guardado los cambios, así que hay que "conciliar" el keySet 
		//	   devuelto por el supplier con los cambios del cliente
		//		1.- Eliminar las entradas devueltas por el servidor pero que se han borrado en el cliente
		//		2.- Añadir las nuevas entradas del cliente que aún no están en el servidor
		if ( _changesTracker.isDirty() ) {
			// [1]-Eliminar entradas borradas
			outKeys = _removeRemovedKeys(suppliedKeySet);
			
			// [2]-Añadir entradas nuevas
			final Set<K> newKeys = _changesTracker.getNewEntries();
			if (CollectionUtils.hasData(newKeys)) {
				// Añadir la lista de nuevas que cumplan las condiciones
				Map<K,V> matchingValues = Maps.filterKeys(this.loadedEntriesAsMap(),	// SOLO entradas cargadas
														  filterOnKeys);
				if (outKeys == null) outKeys = Sets.newHashSet();
				outKeys.addAll(matchingValues.keySet());
			}
		} 
		return outKeys;
	}
	/**
	 * Elimina del keySet facilitado las entradas que figuran como borradas
	 * @param suppliedKeySet el keySet de entrada
	 * @return el keySet SIN las entradas borradas
	 */
	private Set<K> _removeRemovedKeys(final Set<K> suppliedKeySet) {
		Set<K> outKeys = suppliedKeySet;
		final Set<K> removedKeys = _changesTracker.getRemovedEntries();
		if (CollectionUtils.hasData(removedKeys) && CollectionUtils.hasData(outKeys)) {
			// Eliminar de la lista de Keys devuelta por el supplier aquellas que se han borrado
			outKeys = Sets.filter(outKeys,
							      new Predicate<K>() {
											@Override
											public boolean apply(K oid) {
												return !removedKeys.contains(oid);
											}
							   	  });
		}
		return outKeys;
	}
//////////////////////////////////////////////////////////////////////////////////////////////
//	FILTROS
//////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Map<K,V> filterEntriesWithKeys(final Set<K> keys,
										  final boolean onlyLoaded) {
		Predicate<K> filter = new Predicate<K>() {
										@Override
										public boolean apply(final K oid) {
											return keys != null ? keys.contains(oid) : false;
										}
								};
		// Dependiendo de si se quiere filtrar entre las entradas CARGADAS o no, el mapa
		// a filtrar es uno u otro
		Map<K,V> mapToFilter = onlyLoaded ? this.loadedEntriesAsMap()
										  : this;
		Map<K,V> outMap = Maps.filterKeys(mapToFilter,
										  filter);
		return outMap;
	}
	
	
//////////////////////////////////////////////////////////////////////////////////////////////
//	CLASES DE UTILIDAD
//////////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Clase de utilidad para construir los iteradores entrySet() y values() de 
	 * los mapas LazyLoad (ver {@link CachedLazyMap} y {@link LazyMap})
	 * @param <T>
	 */
	@AllArgsConstructor
	static abstract class AbstractUnmodifiableSet<T>
	           implements Set<T> {
		
		private String _errMsg = "This map";
		
		@Override
		public boolean add(T o) {
			throw new UnsupportedOperationException (_errMsg + " does not supports add() method");
		}
		@Override
		public boolean addAll(Collection<? extends T> c) {
			throw new UnsupportedOperationException (_errMsg + " does not supports addAll() method");
		}
		@Override
		public boolean retainAll(Collection<?> c) {
			throw new UnsupportedOperationException (_errMsg + " does not supports retainAll() method");
		}
		@Override
		public boolean remove(Object o) {
			throw new UnsupportedOperationException (_errMsg + " does not supports remove() method");
		}
		@Override
		public boolean removeAll(Collection<?> c) {
			throw new UnsupportedOperationException (_errMsg + " does not supports removeAll() method");
		}
		@Override
		public void clear() {
			throw new UnsupportedOperationException (_errMsg + " does not supports clear() method");
		}
	}
	/**
	 * Clase auxiliar para facilitar la construcción de los métodos values() y entrySet()
	 * @param <T>
	 */
	static abstract class AbstractUnmodifiableEntriesSet<T> 
				  extends AbstractUnmodifiableSet<T> {
		
		protected final Set<?> _keys;
		
		public AbstractUnmodifiableEntriesSet(Set<?> keys) {
			super("The values() / entrySet() collections of a lazily loaded cache");
			_keys = keys;
		}
		@Override
		public int size() {
			return _keys.size();
		}
		@Override
		public boolean isEmpty() {
			return _keys.isEmpty();
		}
	}
}
