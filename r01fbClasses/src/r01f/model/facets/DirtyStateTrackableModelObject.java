package r01f.model.facets;

import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;


/**
 * A object whose state can be tracked to see if it has changed
 * @see DirtyStateTrackable
 */
public interface DirtyStateTrackableModelObject 
		 extends DirtyStateTrackable {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  HasDirtyStateTrackableFacet
/////////////////////////////////////////////////////////////////////////////////////////
	public interface HasDirtyStateTrackableModelObjectFacet 
			 extends ModelObjectFacet {
		
		public DirtyStateTrackableModelObject asDirtyStateTrackable();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DirtyStateTrackableModelObject interface methods are the SAME as the
//	DirtyStateTrackable's interface methods
/////////////////////////////////////////////////////////////////////////////////////////
}
