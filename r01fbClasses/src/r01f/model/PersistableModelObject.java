package r01f.model;

import r01f.guids.OID;
import r01f.model.facets.Facetable;
import r01f.model.facets.HasEntityVersion;
import r01f.model.facets.HasNumericID;
import r01f.model.facets.HasOID;
import r01f.model.facets.Summarizable.HasSummaryFacet;
import r01f.model.facets.TrackableModelObject.HasTrackableFacet;




/**
 * interface for model objects that can be persisted in some kind of storage
 */
public interface PersistableModelObject<O extends OID> 
		 extends ModelObject,
		 		 HasModelObjectMetaData,	// have model object meta-data
         		 Facetable,					// can have facets
 		 	     HasNumericID,				// all persistable model objects MUST have a numeric oid
 		 	     HasOID<O>,					// all persistable model objects MUST have an OID
 		 	     HasEntityVersion,			// all persistable model objects are persisted assuming persistence conflicts are unlike to happen (optimistic locking)
 		 	     HasTrackableFacet,			// all persistable model object has create / update dates and creator / last updator user code 
 		 	     HasSummaryFacet {			// all persistable model objects has a summary either lang dependent or lang independent
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
}
