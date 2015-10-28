package r01f.model.metadata;

import r01f.guids.CommonOIDs.AppCode;
import r01f.types.summary.Summary;


public interface HasFieldsMetaDataForHasFullTextSummaryModelObject 
		 extends HasFieldsMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
// 	Usually it's a bad practice to put constants at interfaces since they're exposed
//	alongside with the interface BUT this time this is the deliberately desired behavior
/////////////////////////////////////////////////////////////////////////////////////////
	public static final FieldMetaDataID FULL_TEXT_SUMMARY_FIELD_ID = MetaDataConfigUtil.idFor(AppCode.forId("r01"),"fullTextSummary");
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a {@link Summary} metaData for the full-text indexed data about the record
	 */
	public FieldMetaData getFullTextSummaryFieldMetaData();
}
