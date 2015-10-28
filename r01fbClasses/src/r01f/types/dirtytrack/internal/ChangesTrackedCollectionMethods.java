package r01f.types.dirtytrack.internal;

import java.util.Collection;
import java.util.Set;

import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.DirtyTrackAdapter;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

/**
 * Metodos utilizados en:
 * 	- El tipo: {@link r01f.types.dirtytrack.ChangesTrackedCollection}
 * 	- El aspecto ChangesTrackableCollectionInterfaceAspect
 */
public class ChangesTrackedCollectionMethods {
/////////////////////////////////////////////////////////////////////////////////////////
//	METODODS DEL INTERFAZ DirtyStateTrackable NO DELEGADOS
/////////////////////////////////////////////////////////////////////////////////////////
	public static <V> boolean isDirty(final Collection<V> currentEntries,
								  	  final CollectionChangesTracker<V> changesTracker) {
		// NO se puede delegar totalmente a el CollectionChangesTracker 
		// ya que este controla si se han añadido o borrado elementos,
		// pero NO puede controlar los cambios que se hacen en los objetos
		// del mapa
		//		ej: col.get(index).setXXX() <-- NO se controla... y el mapa ha cambiado!
		boolean outDirty = changesTracker.isDirty();
		if (!outDirty && CollectionUtils.hasData(currentEntries)) {
			for (V v : currentEntries) {
				if (v instanceof DirtyStateTrackable) {
					if ( ((DirtyStateTrackable)v).isDirty() ) {
						outDirty = true;
						break;
					}
				}
			}
		}
		return outDirty;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METODODS MUTATOR DEL INTERFAZ Collection
/////////////////////////////////////////////////////////////////////////////////////////
	public static <V> boolean add(final V e,
								  final Collection<V> currentEntries,
								  final CollectionChangesTracker<V> changesTracker) {
		// Si el value es DirtyStateTrackable y se está haciendo tracking, 
		// extender este estado
		if (e instanceof DirtyStateTrackable 
		 && changesTracker.getTrackingStatus().isThisDirtyTracking() 
		 && !DirtyTrackAdapter.adapt(e).getTrackingStatus().isThisDirtyTracking()) {
			DirtyTrackAdapter.adapt(e).startTrackingChangesInState(true);
		}
		// Añadir el nuevo elemento
		boolean outResult = currentEntries.add(e);
		if (outResult) changesTracker.trackEntryInsertion(e);
		return outResult;
	}
	public static <V> boolean addAll(final Collection<? extends V> c,
									 final Collection<V> currentEntries,
								  	 final CollectionChangesTracker<V> changesTracker) {
		boolean outModif = false;
		if (!CollectionUtils.isNullOrEmpty(c)) {
			for(V v : c) {
				outModif = outModif | ChangesTrackedCollectionMethods.add(v,currentEntries,changesTracker);
			}
		}
		return outModif;
	}
	@SuppressWarnings("unchecked")
	public static <V> boolean remove(final Object o,
								 final Collection<V> currentEntries,
								 final CollectionChangesTracker<V> changesTracker) {
		boolean outResult = currentEntries.remove(o);
		if (outResult) changesTracker.trackEntryRemoval((V)o);
		return outResult;
	}
	public static <V> boolean removeAll(final Collection<?> c,
										final Collection<V> currentEntries,
										final CollectionChangesTracker<V> changesTracker) {
		boolean outModif = false;
		if (!CollectionUtils.isNullOrEmpty(c)) {
			for(Object v : c) {
				outModif = outModif | ChangesTrackedCollectionMethods.remove(v,currentEntries,changesTracker);
			}
		}
		return outModif;
	}
	public static <V> boolean retainAll(final Collection<?> c,
										final Collection<V> currentEntries,
										final CollectionChangesTracker<V> changesTracker) {
		boolean outModif = false;
		if (!CollectionUtils.isNullOrEmpty(c)) {
			for(Object v : currentEntries) {
				if (!c.contains(v)) outModif = outModif | ChangesTrackedCollectionMethods.remove(v,currentEntries,changesTracker);
			}
		}
		return outModif;
	}
	public static <V> void clear(final Collection<V> currentEntries,
							     final CollectionChangesTracker<V> changesTracker) {
		for (V v : currentEntries) {
			changesTracker.trackEntryRemoval(v);
		}
		currentEntries.clear();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METODOS PARA RECUPERAR ENTRADAS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns new entries
	 * @param currentEntries
	 * @param changesTracker
	 * @return
	 */
	public static <V> Set<V> newEntries(final Collection<V> currentEntries,
							     		final CollectionChangesTracker<V> changesTracker) {
		return changesTracker != null ? changesTracker.getNewEntries()
									  : null;
	}
	/**
	 * Returns removed entries
	 * @param currentEntries
	 * @param changesTracker
	 * @return
	 */
	public static <V> Set<V> removedEntries(final Collection<V> currentEntries,
							     			final CollectionChangesTracker<V> changesTracker) {
		return changesTracker != null ? changesTracker.getRemovedEntries()
									  : null;
	}
	/**
	 * Returns not new or removed entries
	 * @param currentEntries
	 * @param changesTracker
	 * @return
	 */
	public static <V> Set<V> notNewOrRemovedEntries(final Collection<V> currentEntries,
							     					final CollectionChangesTracker<V> changesTracker) {
		// De la entradas actuales, quitar las nuevas y borradas (basta con quitar la nuevas ya que las borradas NO están)
		Collection<V> outCol = changesTracker != null ? Collections2.filter(currentEntries,changesTracker.newEntriesFilter)
													  : currentEntries;		// NO se ha modificado
		return Sets.newHashSet(outCol);
	}
}
