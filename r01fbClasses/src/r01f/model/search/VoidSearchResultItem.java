package r01f.model.search;

import r01f.guids.CommonOIDs.VoidOID;
import r01f.model.IndexableModelObject;
/*
 * {@link SearchResultItem} type to be used when annotating a NOT searchable model object
 * with {@link ModelObjectData}
 */
public interface VoidSearchResultItem 
		 extends SearchResultItemForModelObject<VoidOID,IndexableModelObject<VoidOID>> {
	/* marker interface */
}
