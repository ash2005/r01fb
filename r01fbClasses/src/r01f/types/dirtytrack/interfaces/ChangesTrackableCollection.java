package r01f.types.dirtytrack.interfaces;

import java.util.Collection;
import java.util.Set;

import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.internal.CollectionChangesTracker;


public interface ChangesTrackableCollection<V> 
         extends DirtyStateTrackable {
	/**
	 * @return the {@link CollectionChangesTracker} object in charge of collection changes tracking
	 */
	public CollectionChangesTracker<V> getChangesTracker();
	/**
	 * Returns a {@link Collection} with the new entries added to the original {@link Collection}
	 * @return a view of the original {@link Collection} that contains ONLY new entries
	 */
	public Set<V> newEntries();
	/**
	 * Returns a {@link Collection} with the new entries removed from the original {@link Collection}
	 * @return a view of the original {@link Collection} that contains ONLY removed entries
	 */
	public Set<V> removedEntries();
	/**
	 * Returns a {@link Collection} with the not new or removed entries added to the original {@link Collection}
	 * @return a view of the original {@link Collection} that contains ONLY not new or removed entries
	 */
	public Set<V> notNewOrRemovedEntries();
	/**
	 * @return the loaded values
	 */
	public Collection<V> loadedValues();
}
