package r01f.model.metadata;

import r01f.model.ModelObject;
import r01f.model.facets.HasID;


public interface HasFieldsMetaDataForHasIDModelObject 
		 extends HasFieldsMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the ID of the record if the record is a {@link HasID} {@link ModelObject}
	 */
	public FieldMetaData getIDFieldMetaData();
}
