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
public interface SearchResultItemForModelObject<O extends OID,M extends IndexableModelObject>
		 extends SearchResultItem,
		 		 HasOID<O>,
		 		 HasEntityVersion,
		 		 HasNumericID,
		 		 HasSummaryFacet {
/////////////////////////////////////////////////////////////////////////////////////////
//  MODEL OBJECT TYPE
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
	 * @return a code for the model object type
	 */
	public long getModelObjectTypeCode();
	/**
	 * Sets a model object type code
	 * @param code
	 */
	public void setModelObjectTypeCode(final long code);
/////////////////////////////////////////////////////////////////////////////////////////
//  MODEL OBJECT
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the model object with no guarantee that a {@link ClassCastException} is thrown 
	 * if the model object's type is not the expected
	 * @param modelObject
	 */
	public <U extends IndexableModelObject> void unsafeSetModelObjectType(final Class<U> modelObjectType);
	/**
	 * @return the model object
	 */
	public M getModelObject();
	/**
	 * Sets the model object
	 * @param modelObject
	 */
	public void setModelObject(final M modelObject);
	/**
	 * Sets the model object with no guarantee that a {@link ClassCastException} is thrown 
	 * if the model object's type is not the expected
	 * @param modelObject
	 */
	public <U extends IndexableModelObject> void unsafeSetModelObject(final U modelObject);	
}
