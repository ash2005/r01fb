package r01f.model.metadata;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.guids.VersionIndependentOID;
import r01f.guids.VersionOID;
import r01f.locale.Language;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.locale.LanguageTextsBuilder;
import r01f.model.ModelObject;
import r01f.model.annotations.ModelObjectData;


/**
 * Base type for the metaData that describes a {@link ModelObject} 
 * This type is set using the {@link ModelObjectData} annotation set at model objects as:
 * <pre class='brush:java'>
 * 		@ModelObjectMetaData(MyModelObjectMetaData.class)
 * 		public class MyModelObject 
 * 		  implements ModelObject {
 * 			...
 * 		}
 * </pre>
 */
@Accessors(prefix="_")
public abstract class VersionableModelObjectTypeMetaDataBaseImpl<SELF_TYPE extends VersionableModelObjectTypeMetaDataBaseImpl<SELF_TYPE>>
			  extends ModelObjectTypeMetaDataBaseImpl<SELF_TYPE>
    	   implements VersionableModelObjectTypeMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
    		   
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public VersionableModelObjectTypeMetaDataBaseImpl(final Class<? extends ModelObject> type,final Class<? extends OID> oidType,final long typeCode,
													  final Class<? extends VersionIndependentOID> versionIndependentOidType,final Class<? extends VersionOID> versionOidType) {
		super(type,oidType,
			  typeCode);
		// Init the common meta data fields
		_initCommonMetaData(versionIndependentOidType,versionOidType);
	}
	private void _initCommonMetaData(final Class<? extends VersionIndependentOID> versionIndependentOidType,final Class<? extends VersionOID> versionOidType) {
		_addFieldsMetaData(// Version independent oid
						   FieldMetaDataConfigBuilder.forId(VERSION_IDEPENDENT_OID_FIELD_ID)
				 				  .withName(LanguageTextsBuilder.createMapBacked()
						  					   					.withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
						  					   .addForLang(Language.SPANISH,"Identificador único del objeto INDEPENDIENTE de la version (este identificador es igual en todas las versiones del mismo objeto)")
						  					   .addForLang(Language.BASQUE,"[eu] Identificador único del objeto INDEPENDIENTE de la version (este identificador es igual en todas las versiones del mismo objeto)")
						  					   .addForLang(Language.ENGLISH,"Model object version independent unique identifier (this id remains the same through all concrete entity's versions)")
						  					   .build())
								  .withNODescription()
								  .forOIDField(versionIndependentOidType)
								  .searchEngine()
								  		.notIndexed(),
						   // Version
						   FieldMetaDataConfigBuilder.forId(VERSION_OID_FIELD_ID)
				 				  .withName(LanguageTextsBuilder.createMapBacked()
						  					   					.withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
						  					   .addForLang(Language.SPANISH,"Identificador único de la versión del objeto")
						  					   .addForLang(Language.BASQUE,"[eu] Identificador único de la versión del objeto")
						  					   .addForLang(Language.ENGLISH,"Model object's version unique identifier")
						  					   .build())
								  .withNODescription()
								  .forOIDField(versionOidType)
								  .searchEngine()
								  		.notIndexed()
						   );
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FieldMetaData getVersionIndependentOidFieldMetaData() {
		return this.getFieldMetaDataFor(VERSION_IDEPENDENT_OID_FIELD_ID);
	}
	@Override
	public FieldMetaData getVersionFieldMetaData() {
		return this.getFieldMetaDataFor(VERSION_OID_FIELD_ID);
	}
	
}
