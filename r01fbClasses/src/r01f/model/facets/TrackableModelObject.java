package r01f.model.facets;

import r01f.model.ModelObjectTracking;

public interface TrackableModelObject {
/////////////////////////////////////////////////////////////////////////////////////////
//  R01MHasTrackableFacet
/////////////////////////////////////////////////////////////////////////////////////////
	public static interface HasTrackableFacet 
					extends ModelObjectFacet {
		public TrackableModelObject asTrackable();
		
		public ModelObjectTracking getTrackingInfo();
		public void setTrackingInfo(ModelObjectTracking trackingInfo);
	}

}