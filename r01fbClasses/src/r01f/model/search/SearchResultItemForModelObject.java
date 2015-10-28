package r01f.model.search;

import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.facets.HasEntityVersion;
import r01f.model.facets.HasNumericID;
import r01f.model.facets.HasOID;
import r01f.model.facets.Summarizable.HasSummaryFacet;



/**
 * Marker interface for search result items
 */
public interface SearchResultItemForModelObject<O extends OID,M extends IndexableModelObject<O>>
		 extends SearchResultItem,
		 		 HasOID<O>,
		 		 HasEntityVersion,
		 		 HasNumericID,
		 		 HasSummaryFacet {
/////////////////////////////////////////////////////////////////////////////////////////
//  DATA
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the model object type
	 */
	public Class<M> getModelObjectType();
	/**
	 * Sets the model object type
	 * @param modelObjectType 
	 */
	public void setModelObjectType(final Class<M> modelObjectType);
	/**
	 * @return the model object
	 */
	public M getModelObject();
	/**
	 * Sets the model object
	 * @param modelObject
	 */
	public void setModelObject(final M modelObject);
}
