package r01f.model.metadata;


import r01f.guids.CommonOIDs.AppCode;

/**
 * An interface to be implemented by objects containing metadata about the model objects
 * such as:
 * <ul>
 * 		<li>The model object's type</li>
 * 		<li>A user-friendly name/description</li>
 * 		<li>A numeric code to be indexed/stored alongisde the object data that eases filtering</li>
 * 		<li>MetaData about model object's fields</li>
 * 		<li>The search filter / result item types</li>
 * 		<li>etc</li>
 * </ul>
 */
public interface VersionableModelObjectTypeMetaData 
		 extends ModelObjectTypeMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
// 	Usually it's a bad practice to put constants at interfaces since they're exposed
//	alongside with the interface BUT this time this is the deliberately desired behavior
/////////////////////////////////////////////////////////////////////////////////////////
	public static final FieldMetaDataID VERSION_IDEPENDENT_OID_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"versionIndependentOid");
	public static final FieldMetaDataID VERSION_OID_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"versionOid");
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the version independent oid field metadata
	 */
	public FieldMetaData getVersionIndependentOidFieldMetaData();
	/**
	 * @return the version field metadata
	 */
	public FieldMetaData getVersionFieldMetaData();
}
