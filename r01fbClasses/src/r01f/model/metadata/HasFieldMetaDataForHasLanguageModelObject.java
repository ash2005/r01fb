package r01f.model.metadata;

import r01f.guids.CommonOIDs.AppCode;
import r01f.locale.Language;


public interface HasFieldMetaDataForHasLanguageModelObject 
		 extends HasFieldsMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
// 	Usually it's a bad practice to put constants at interfaces since they're exposed
//	alongside with the interface BUT this time this is the deliberately desired behavior
/////////////////////////////////////////////////////////////////////////////////////////
	public static final FieldMetaDataID LANGUAGE_CODE_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"language","code");
	public static final FieldMetaDataID LANGUAGE_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"language");	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the {@link Language} the record is in
	 */
	public FieldMetaData getLanguageFieldMetaData();
	/**
	 * @return
	 */
	public FieldMetaData getLanguageCodeFieldMetaData();
}
