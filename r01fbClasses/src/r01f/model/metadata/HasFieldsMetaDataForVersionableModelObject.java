package r01f.model.metadata;

import r01f.guids.VersionIndependentOID;
import r01f.model.ModelObject;
import r01f.model.facets.Versionable;


public interface HasFieldsMetaDataForVersionableModelObject 
		 extends HasFieldsMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link VersionIndependentOID} of the entity if it's a {@link Versionable} {@link ModelObject}
	 */
	public FieldMetaData getVersionIndependentOidFieldMetaData();
	/**
	 * @return the VersionOID of the entity if it's a {@link Versionable} {@link ModelObject}
	 */
	public FieldMetaData getVersionFieldMetaData();
}
