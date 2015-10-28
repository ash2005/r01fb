package r01f.aspects.core.dirtytrack;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;

import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.ChangesTrackedList;
import r01f.types.dirtytrack.interfaces.ChangesTrackableCollection;

public class ChangesTrackedListInTrackableObject<V> 
     extends ChangesTrackedCollectionInTrackableObject<V> 
  implements List<V> {
	private static final long serialVersionUID = -8196335074553302594L;
///////////////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////////////
	public ChangesTrackedListInTrackableObject(DirtyStateTrackable container,
											   ChangesTrackableCollection<V> theCol) {
		super(container,theCol);
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
//	OVERRIDE
///////////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public V get(int index) {
		return ((ChangesTrackedList<V>)_changesTrackedCollection).get(index);
	}	
	@Override
	public int indexOf(Object o) {
		return ((ChangesTrackedList<V>)_changesTrackedCollection).indexOf(o);
	}
	@Override
	public int lastIndexOf(Object o) {
		return ((ChangesTrackedList<V>)_changesTrackedCollection).lastIndexOf(o);
	}
	@Override
	public ListIterator<V> listIterator() {
		return ((ChangesTrackedList<V>)_changesTrackedCollection).listIterator();
	}
	@Override
	public ListIterator<V> listIterator(int index) {
		return ((ChangesTrackedList<V>)_changesTrackedCollection).listIterator(index);
	}
	@Override
	public List<V> subList(int fromIndex,int toIndex) {
		return ((ChangesTrackedList<V>)_changesTrackedCollection).subList(fromIndex,toIndex);
	}	
	@Override
	public V set(int index,V element) {
		V outResult = ((ChangesTrackedList<V>)_changesTrackedCollection).set(index,element);
		if (outResult != null) _trckContainerObj.getTrackingStatus().setThisDirty(true);
		return outResult;
	}	
	@Override
	public void add(int index,V element) {
		((ChangesTrackedList<V>)_changesTrackedCollection).add(index,element);
		_trckContainerObj.getTrackingStatus().setThisDirty(true);
	}	
	@Override
	public boolean addAll(int index,Collection<? extends V> c) {
		boolean outResult = ((ChangesTrackedList<V>)_changesTrackedCollection).addAll(index,c);
		if (outResult) _trckContainerObj.getTrackingStatus().setThisDirty(true);
		return outResult;
	}
	@Override
	public V remove(int index) {
		V outResult = ((ChangesTrackedList<V>)_changesTrackedCollection).remove(index);
		if (outResult != null) _trckContainerObj.getTrackingStatus().setThisDirty(true);
		return outResult;
	}

}
