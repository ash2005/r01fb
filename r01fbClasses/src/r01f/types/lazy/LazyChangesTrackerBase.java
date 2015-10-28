package r01f.types.lazy;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyTrackingStatus;
import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.types.dirtytrack.internal.CollectionChangesTracker;

@Accessors(prefix="_")
     class LazyChangesTrackerBase<V> 
implements DirtyStateTrackable {
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATED CHANGES TRACKER
/////////////////////////////////////////////////////////////////////////////////////////
	@NotDirtyStateTrackable
	@Getter protected final CollectionChangesTracker<V> _changesTracker = new CollectionChangesTracker<V>();

	@Override public DirtyTrackingStatus getTrackingStatus() {		return _changesTracker.getTrackingStatus();		}
	@Override public <T> T getWrappedObject() {						return _changesTracker.<T>getWrappedObject();		}
	@Override public boolean isThisDirty() {	return _changesTracker.isThisDirty();		}
	@Override public boolean isDirty() {		return _changesTracker.isDirty();			}
	@Override public DirtyStateTrackable touch() {			return _changesTracker.touch();			}
	@Override public DirtyStateTrackable resetDirty() {		return _changesTracker.resetDirty();	}
	@Override public DirtyStateTrackable startTrackingChangesInState() {	return _changesTracker.startTrackingChangesInState();	}
	@Override public DirtyStateTrackable stopTrackingChangesInState() {		return _changesTracker.stopTrackingChangesInState();	}
	@Override public DirtyStateTrackable startTrackingChangesInState(final boolean startTrackingInChilds) {		return _changesTracker.startTrackingChangesInState(startTrackingInChilds);	}
	@Override public DirtyStateTrackable startTrackingChangesInState(final boolean startTrackingInChilds,
																	 final boolean checkIfOldValueChanges) {	return _changesTracker.startTrackingChangesInState(startTrackingInChilds,
																			 																					   checkIfOldValueChanges);	}
	@Override public DirtyStateTrackable stopTrackingChangesInState(final boolean stopTrackingInChilds) {	return _changesTracker.stopTrackingChangesInState(stopTrackingInChilds);	}
}
