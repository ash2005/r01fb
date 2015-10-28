package r01f.model.metadata;


import java.util.Set;

import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.UserCode;
import r01f.model.ModelObject;
import r01f.model.facets.ModelObjectFacet;

/**
 * An interface to be implemented by objects containing metadata about {@link ModelObject}s
 * such as:
 * <ul>
 * 		<li>The model object's type</li>
 * 		<li>A user-friendly name/description</li>
 * 		<li>A numeric code to be indexed/stored alongisde the object data that eases filtering</li>
 * 		<li>MetaData about model object's fields</li>
 * 		<li>etc</li>
 * </ul>
 */
public interface ModelObjectTypeMetaData 
	  	 extends HasFieldsMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
// 	Usually it's a bad practice to put constants at interfaces since they're exposed
//	alongside with the interface BUT this time this is the deliberately desired behavior
/////////////////////////////////////////////////////////////////////////////////////////
	public static final FieldMetaDataID TYPE_NAME_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"type","name");
	public static final FieldMetaDataID TYPE_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"type","code");
	public static final FieldMetaDataID TYPE_FACETS_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"type","facets");
	public static final FieldMetaDataID DOCUMENT_ID_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"DOCID");
	public static final FieldMetaDataID OID_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"oid");
	public static final FieldMetaDataID NUMERIC_ID_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"numericId");
	public static final FieldMetaDataID ENTITY_VERSION_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"entityVersion");
	public static final FieldMetaDataID CREATE_DATE_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"createDate");
	public static final FieldMetaDataID CREATOR_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"creator");
	public static final FieldMetaDataID LAST_UPDATE_DATE_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"lastUpdateDate");
	public static final FieldMetaDataID LAST_UPDATOR_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"lastUpdator");
/////////////////////////////////////////////////////////////////////////////////////////
//  CONVERSION
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Return this model object type meta data as another {@link ModelObjectTypeMetaData}
	 * There are two possibilities to this cast:
	 * <ul>
	 * 		<li>If this {@link ModelObjectTypeMetaData} is directly implementing the provided type THIS object is simply casted and returned</li>
	 * 		<li>... BUT a model object can have many facets and any of these facets {@link ModelObjectTypeMetaData} might implement the provided type
	 * 			so each of these facets should be checked to see if the type is implemented by the facet's {@link ModelObjectTypeMetaData} type</li>
	 * </ul>
	 * @param type
	 * @return
	 */
	public <T extends HasFieldsMetaData> T as(final Class<T> type);
	/**
	 * @see ModelObjectTypeMetaData#as(Class)
	 * @param hasFieldsMetaDataType
	 * @return
	 */
	public <T extends HasFieldsMetaData> boolean is(final Class<T> hasFieldsMetaDataType);
/////////////////////////////////////////////////////////////////////////////////////////
//  TYPE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the model object's type
	 */
	public Class<? extends ModelObject> getType();
	/**
	 * Returns a numeric code for the model object type that's used when indexing/storing it
	 * and eases the filtering
	 * @param the model object type
	 * @return 
	 */
	public long getTypeCode();
	/**
	 * A given model object might have many facets (can be seen in different ways)
	 * For example, a car model object is also a vehicle model object and also does a bicycle
	 * ... so a car instance has Car and Vehicle facets 
	 * @return a {@link Set} with all the facet types meta-data (ie CarMetaData or VehicleMetaData)
	 */
	public Set<ModelObjectTypeMetaData> getTypeFacetsMetaData();
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the metadata for the field that stores the java type name 
	 */
	public FieldMetaData getTypeNameFieldMetaData();
	/**
	 * @return the metadata for the field that stores the type code 
	 */
	public FieldMetaData getTypeFieldMetaData();
	/**
	 * @return the metadata for the field that stores all facet type codes
	 */
	public FieldMetaData getFacetTypesFieldMetaData();
	/**
	 * @return the metadata for the field that stores the oid 
	 */
	public FieldMetaData getOIDFieldMetaData();
	/**
	 * @return the metadata for the field that stores the id of the indexed document
	 */
	public FieldMetaData getDocumentIDFieldMetaData();
	/**
	 * @returnt he metadata for the field that stores  a numeric id for the record
	 */
	public FieldMetaData getNumericIDFieldMetaData();
	/**
	 * @return the metadata for the field that stores a record's stored entity version used to achieve optimistic locking
	 */
	public FieldMetaData getEntityVersionFieldMetaData();
	/**
	 * @return the metadata for the field that stores the record's create date
	 */
	public FieldMetaData getCreateDateFieldMetaData();
	/**
	 * @return the metadata for the field that stores the {@link UserCode} of the user that created the record
	 */
	public FieldMetaData getCreatorFieldMetaData();
	/**
	 * @return the metadata for the field that stores the time the record's entity was last updated
	 */
	public FieldMetaData getLastUpdateDateFieldMetaData();
	/**
	 * @return the metadata for the field that stores the {@link UserCode} of the user that last updated the record's entity
	 */
	public FieldMetaData getLastUpdatorFieldMetaData();
/////////////////////////////////////////////////////////////////////////////////////////
//  FACETS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Checks if the {@link ModelObject} type implements a {@link ModelObjectFacet}
	 * @param facetType
	 * @return
	 */
	public <F extends ModelObjectFacet> boolean hasFacet(final Class<F> facetType);
}
