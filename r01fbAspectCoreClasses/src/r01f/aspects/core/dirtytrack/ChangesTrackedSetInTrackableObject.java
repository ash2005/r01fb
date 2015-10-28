package r01f.aspects.core.dirtytrack;

import java.util.Set;

import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.interfaces.ChangesTrackableCollection;

public class ChangesTrackedSetInTrackableObject<V> 
     extends ChangesTrackedCollectionInTrackableObject<V> 
  implements Set<V> {
	private static final long serialVersionUID = -7849665023469647785L;
///////////////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////////////
	public ChangesTrackedSetInTrackableObject(DirtyStateTrackable container,
											  ChangesTrackableCollection<V> theCol) {
		super(container,theCol);
	}
}
