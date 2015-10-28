package r01f.types.dirtytrack.internal;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyTrackingStatus;
import r01f.debug.Debuggable;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

/**
 * Se encarga de "apuntar" los cambios en un conjunto (Set) de claves
 * @param <K>
 */
@Accessors(prefix="_")
@NoArgsConstructor
public class CollectionChangesTracker<K> 
  implements DirtyStateTrackable,
			 Serializable,
			 Debuggable {
	private static final long serialVersionUID = -190729036750079312L;
/////////////////////////////////////////////////////////////////////////////////////////
//	ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private Set<K> _newEntries;
	@Getter private Set<K> _removedEntries;
	
	private boolean _dirty = false;
	private boolean _tracking = false;
	
	// Estado de tracking... simplemente delega en un objeto inline
	// (necesario para implementar el método DirtyStateTrackable.getTrackingStatus()
	private DirtyTrackingStatus _trackingStatus = new DirtyTrackingStatus() {
															private static final long serialVersionUID = 5816420380285360322L;
															@Override
															public void setThisNew(boolean newObj) {
																// No hace nada
															}
															@Override
															public boolean isThisNew() {
																return false;
															}
															@Override
															public void setThisDirty(boolean thisDirty) {
																if (thisDirty == false) {
																	CollectionChangesTracker.this.resetDirty();		// OJO!! no implica que los elementos del mapa se establezcan al valor newDirty
																} else {
																	CollectionChangesTracker.this._dirty = true;	// ???
																}
															}
															@Override
															public boolean isThisDirty() {
																return CollectionChangesTracker.this.isDirty();
															}
															@Override
															public void setThisDirtyTracking(boolean dirtyTrack) {
																CollectionChangesTracker.this._tracking = dirtyTrack;
															}
															@Override
															public boolean isThisDirtyTracking() {
																return CollectionChangesTracker.this._tracking;
															}
															@Override
															public void setThisCheckIfValueChanges(boolean check) {
																// no aplica
															}
															@Override
															public boolean isThisCheckIfValueChanges() {
																return false;
															}
															@Override
															public void _resetDirty(DirtyStateTrackable trck) {
																/* empty */
															}
															@Override
															public boolean _isThisDirty(DirtyStateTrackable trck) {
																return false;
															}
															@Override
															public boolean _isDirty(DirtyStateTrackable trck) {
																return false;
															}
															@Override
															public void _startTrackingChangesInState(DirtyStateTrackable trck) {
																/* empty */
															}
															@Override
															public void _stopTrackingChangesInState(DirtyStateTrackable trck) {
																/* empty */
															}
															@Override
															public void _startTrackingChangesInState(DirtyStateTrackable trck,
																									 boolean startTrackingInChilds) {
																/* empty */
															}
															@Override
															public void _startTrackingChangesInState(DirtyStateTrackable trck,
																									 boolean startTrackingInChilds,
																									 boolean checkIfOldValueChanges) {
																/* empty */
															}
															@Override
															public void _stopTrackingChangesInState(DirtyStateTrackable trck,
																									boolean stopTrackingInChilds) {
																/* empty */
															}
												  };
/////////////////////////////////////////////////////////////////////////////////////////
//	FILTROS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Filtrar claves que sean nuevas
	 */
	public transient Predicate<K> newEntriesFilter = new Predicate<K>() {
		@Override
		public boolean apply(K key) {
			Set<K> newEntries = CollectionChangesTracker.this.getNewEntries();
			return newEntries != null ? newEntries.contains(key) : false;
		}
	};
	/**
	 * Filtrar claves que sean borradas
	 */
	public transient Predicate<K> removedEntriesFilter = new Predicate<K>() {
		@Override
		public boolean apply(K key) {
			Set<K> removedEntries = CollectionChangesTracker.this.getRemovedEntries();
			return removedEntries != null ? removedEntries.contains(key) : false;
		}
	};
	/**
	 * Filtrar claves que sean nuevas o borradas
	 */
	public transient Predicate<K> newOrDeletedEntriesFilter = Predicates.<K>or(newEntriesFilter,
																  			removedEntriesFilter);
/////////////////////////////////////////////////////////////////////////////////////////
//	METODOS PARA AÑADIR ENTRADAS BORRADAS Y NUEVAS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Añadir una nueva entrada que se ha añadido en el mapa subyacente
	 * @param key la clave de la nueva entrada
	 */
	public void trackEntryInsertion(final K key) {
		if (!_tracking) return;		// NO se está haciendo tracking de los cambios
		
		if (_removedEntries != null && _removedEntries.contains(key)) {
			// Eliminarlo de la lista de claves eliminadas
			_removedEntries.remove(key);
		} else if (_newEntries != null && !_newEntries.contains(key)) {
			// Añadir la entrada a la lista de claves nuevas
			if (!_newEntries.contains(key)) _newEntries.add(key);
		} else {
			// Añadir la entrada a la lista de claves nuevas
			if (_newEntries == null) _newEntries = new HashSet<K>();
			if (!_newEntries.contains(key)) _newEntries.add(key);
		}
		_dirty = true;	// El mapa ha sido modificado
	}
	/**
	 * Añadir una nueva entrada que se ha borrado en el mapa subyacente
	 * @param key la clave de la entrada borrada
	 */
	public void trackEntryRemoval(final K key) {
		if (!_tracking) return;		// NO se está haciendo tracking de los cambios
		
		if (_newEntries != null && _newEntries.contains(key)) {
			// Eliminarlo de la lista de claves nuevas
			_newEntries.remove(key);
		} else if (_removedEntries != null && !_removedEntries.contains(key)) {
			// Añadir la entrada a la lista de entradas eliminadas
			if (!_removedEntries.contains(key)) _removedEntries.add(key);
		} else {
			// Añadir la entrada a la lista de eentradas eliminadas
			if (_removedEntries == null) _removedEntries = new HashSet<K>();
			if (!_removedEntries.contains(key)) _removedEntries.add(key);
		}
		_dirty = true;
	}
	/**
	 * Obtiene el conjunto de claves vigente en la actualidad a partir del conjunto de claves original
	 * @param originalKeys conjunto de claves original
	 * @return
	 */
	public Set<K> currentKeys(final Set<K> originalKeys) {
		// Ha habido cambios respecto a los elementos originales
		Set<K> originalPlusNew = !CollectionUtils.isNullOrEmpty(_newEntries) ? Sets.union(originalKeys,_newEntries)
																			 : originalKeys;
		Set<K> originalPlusNewWithoutRemoved = !CollectionUtils.isNullOrEmpty(_removedEntries) ? Sets.filter(originalPlusNew,
																											 new Predicate<K>() {
																													@Override
																													public boolean apply(K key) {
																														return !CollectionChangesTracker.this.getRemovedEntries().contains(key);
																													}
																											 })
																							    : originalPlusNew;
		return originalPlusNewWithoutRemoved;
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	DIRTY
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public DirtyTrackingStatus getTrackingStatus() {
		return _trackingStatus;
	}
	@Override
	public boolean isThisDirty() {
		return _dirty;
	}
	@Override
	public boolean isDirty() {
		return _dirty;
	}
	@Override
	public DirtyStateTrackable touch() {
		_dirty = true;
		return this;
	}
	@Override
	public DirtyStateTrackable resetDirty() {
		if (_newEntries != null) _newEntries = null;
		if (_removedEntries != null) _removedEntries = null;
		_dirty = false;
		return this;
	}
	@Override
	public DirtyStateTrackable startTrackingChangesInState() {
		_tracking = true;
		return this;
	}
	@Override
	public DirtyStateTrackable stopTrackingChangesInState() {
		_tracking = false;
		return this;
	}
	@Override
	public DirtyStateTrackable startTrackingChangesInState(final boolean startTrackingInChilds) {
		_tracking = true;
		return this;
	}
	@Override
	public DirtyStateTrackable startTrackingChangesInState(final boolean startTrackingInChilds,
														   final boolean checkIfOldValueChanges) {
		_tracking = true;
		// NO hace nada con los elementos... 
		// si hay que cambiar el estado de un objeto y sus dependientes se hace mediante
		// el aspecto DirtyStateTrackableAspect
		return this;
	}
	@Override
	public DirtyStateTrackable stopTrackingChangesInState(final boolean stopTrackingInChilds) {
		_tracking = false;
		// NO hace nada con los elementos... 
		// si hay que cambiar el estado de un objeto y sus dependientes se hace mediante
		// el aspecto DirtyStateTrackableAspect
		return this;
	}
	@Override @SuppressWarnings("unchecked")
	public <T> T getWrappedObject() {
		return (T)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String debugInfo() {
		return Strings.create(50)
					  .add("\r\n\t-         New entries : ").add(_newEntries != null ? _newEntries.size() : 0 ).add(" > ").add(_mapKeysToString(_newEntries))
					  .add("\r\n\t-     Removed entries : ").add(_removedEntries != null ? _removedEntries.size() : 0).add(" > ").add(_mapKeysToString(_removedEntries))
					  .asString();
	}
	private static <K> StringBuffer _mapKeysToString(final Collection<K> keys) {
		StringBuffer sb = new StringBuffer();
		if (keys != null) {
			for (Iterator<K> it = keys.iterator(); it.hasNext(); ) {
				sb.append(it.next());
				if (it.hasNext()) sb.append(", ");
			}
		}
		return sb;
	}
}
