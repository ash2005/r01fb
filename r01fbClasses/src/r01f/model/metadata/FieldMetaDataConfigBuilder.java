package r01f.model.metadata;

import java.util.Set;

import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.locale.LanguageTexts;
import r01f.model.ModelObject;
import r01f.patterns.IsBuilder;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.IsPath;
import r01f.types.summary.Summary;

public abstract class FieldMetaDataConfigBuilder 
           implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static MetaDataConfigBuilderNameStep forId(final FieldMetaDataID fieldId) {
		return new FieldMetaDataConfigBuilder() { /* nothing */ }
						.new MetaDataConfigBuilderNameStep(fieldId);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderNameStep {
		private final FieldMetaDataID _fieldId;
		
		public MetaDataConfigBuilderDescriptionStep withName(final LanguageTexts name) {
			return new MetaDataConfigBuilderDescriptionStep(_fieldId,
															name);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderDescriptionStep {
		private final FieldMetaDataID _fieldId;
		private final LanguageTexts _name;
		
		public MetaDataConfigBuilderTypeStep withDescription(final LanguageTexts description) {
			return new MetaDataConfigBuilderTypeStep(_fieldId,
													 _name,description);
		}
		public MetaDataConfigBuilderTypeStep withNODescription() {
			return new MetaDataConfigBuilderTypeStep(_fieldId,
													 _name,null);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderTypeStep {
		private final FieldMetaDataID _fieldId;
		private final LanguageTexts _name;
		private final LanguageTexts _description;
		
		
		public PolimorphicFieldMetaDataConfigBuilderTypeStep1 forPolymorphicField(final Class<?> baseType) {
			return new FieldMetaDataConfigBuilder() {/* nothing */}
							.new PolimorphicFieldMetaDataConfigBuilderTypeStep1(new FieldMetaDataForPolymorphicType(_fieldId,
												 	   													  			_name,_description,
												 	   													  			new FieldMetaDataSearchEngineIndexingConfig(),
												 	   													  			baseType));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForString,
											    MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForString>> forStringField() {
			FieldMetaDataForString fieldMetaData = new FieldMetaDataForString(_fieldId,
											  								  _name,_description,
											  								  new FieldMetaDataSearchEngineIndexingConfig());
			// strings can be indexed tokenized or not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForString,
													    MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForString>>(fieldMetaData,
																					 											 new MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForString>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForString,
											    MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForString>> forStringField(final Class<? extends CanBeRepresentedAsString> type) {
			FieldMetaDataForString fieldMetaData = new FieldMetaDataForString(_fieldId,
														   					  _name,_description,
														   					  new FieldMetaDataSearchEngineIndexingConfig(),
														   					  type);
			// strings can be indexed tokenized or not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForString,
														MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForString>>(fieldMetaData,
																					 											 new MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForString>(fieldMetaData));			
		}
		public MetaDataConfigBuilderIndexingAlwaysStoredCfg<MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForOID>> forOIDField(final Class<? extends OID> oidType) {
			FieldMetaDataForOID fieldMetaData = new FieldMetaDataForOID(_fieldId,
																	    _name,_description,
																	    new FieldMetaDataSearchEngineIndexingConfig(),
																	    oidType);
			// oids are always stored and can be indexed not tokenized
			fieldMetaData.getSearchEngineIndexingConfig()
						 .setStored(true);
			return new MetaDataConfigBuilderIndexingAlwaysStoredCfg<MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForOID>>(new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForOID>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForInteger,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForInteger>> forIntegerField() {
			FieldMetaDataForInteger fieldMetaData = new FieldMetaDataForInteger(_fieldId,
																			    _name,_description,
																			    new FieldMetaDataSearchEngineIndexingConfig());
			// numbers can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForInteger,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForInteger>>(fieldMetaData,
																	 														     	 new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForInteger>(fieldMetaData));			
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForLong,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForLong>> forLongField() {
			FieldMetaDataForLong fieldMetaData = new FieldMetaDataForLong(_fieldId,
																		  _name,_description,
																		  new FieldMetaDataSearchEngineIndexingConfig());
			// numbers can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForLong,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForLong>>(fieldMetaData,
																	 														      new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForLong>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForFloat,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForFloat>> forFloatField() {
			FieldMetaDataForFloat fieldMetaData = new FieldMetaDataForFloat(_fieldId,
																			_name,_description,
																			new FieldMetaDataSearchEngineIndexingConfig());
			// numbers can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForFloat,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForFloat>>(fieldMetaData,
																	 														       new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForFloat>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForDate,
											    MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForDate>> forDateField() {
			FieldMetaDataForDate fieldMetaData = new FieldMetaDataForDate(_fieldId,
																	      _name,_description,
																	      new FieldMetaDataSearchEngineIndexingConfig());
			// dates can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForDate,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForDate>>(fieldMetaData,
																	 														      new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForDate>(fieldMetaData));			
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForBoolean,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForBoolean>> forBooleanField() {
			FieldMetaDataForBoolean fieldMetaData = new FieldMetaDataForBoolean(_fieldId,
																			    _name,_description,
																			    new FieldMetaDataSearchEngineIndexingConfig());
			// booleans can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForBoolean,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForBoolean>>(fieldMetaData,
																	 														         new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForBoolean>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForEnum,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForEnum>> forEnumField(final Class<? extends Enum<?>> enumType) {
			FieldMetaDataForEnum fieldMetaData = new FieldMetaDataForEnum(_fieldId,
																		  _name,_description,
																		  new FieldMetaDataSearchEngineIndexingConfig(),
																		  enumType);
			// enums can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForEnum,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForEnum>>(fieldMetaData,
																	 														      new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForEnum>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForLanguage,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForLanguage>> forLanguageField() {
			FieldMetaDataForLanguage fieldMetaData = new FieldMetaDataForLanguage(_fieldId,
																				  _name,_description,
																				  new FieldMetaDataSearchEngineIndexingConfig());
			// language can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForLanguage,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForLanguage>>(fieldMetaData,
																	 														      	  new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForLanguage>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForPath,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForPath>> forPathField(final Class<? extends IsPath> pathType) {
			FieldMetaDataForPath fieldMetaData = new FieldMetaDataForPath(_fieldId,
																		  _name,_description,
																		  new FieldMetaDataSearchEngineIndexingConfig(),
																		  pathType);
			// paths can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForPath,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForPath>>(fieldMetaData,
																	 														      new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForPath>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForUrl,
												MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForUrl>> forURLField() {
			FieldMetaDataForUrl fieldMetaData = new FieldMetaDataForUrl(_fieldId,
																	    _name,_description,
																	    new FieldMetaDataSearchEngineIndexingConfig());
			// urls can be indexed not tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForUrl,
														MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForUrl>>(fieldMetaData,
																	 														     new MetaDataConfigBuilderIndexingCfgNotTokenizableStep<FieldMetaDataForUrl>(fieldMetaData));			
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForJavaType,
											    MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForJavaType>> forJavaTypeField(final Class<?> type) {
			FieldMetaDataForJavaType fieldMetaData = new FieldMetaDataForJavaType(_fieldId,
																				  _name,_description,
																				  new FieldMetaDataSearchEngineIndexingConfig(),
																				  type);
			// java types can be indexed always tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForJavaType,
														MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForJavaType>>(fieldMetaData,
																	 														      	   new MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForJavaType>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForSummary,
												MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForSummary>> forSummaryField(final Class<? extends Summary> summaryType) {
			FieldMetaDataForSummary fieldMetaData = new FieldMetaDataForSummary(_fieldId,
																			    _name,_description,
																			    new FieldMetaDataSearchEngineIndexingConfig(),
																			    summaryType);
			// summaries can be indexed always tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForSummary,
														MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForSummary>>(fieldMetaData,
																	 														      	  new MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForSummary>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForLanguageTexts,
												MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForLanguageTexts>> forLanguageTextsField() {
			FieldMetaDataForLanguageTexts fieldMetaData = new FieldMetaDataForLanguageTexts(_fieldId,
																						    _name,_description,
																						    new FieldMetaDataSearchEngineIndexingConfig());
			// language texts can be indexed always tokenized
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForLanguageTexts,
														MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForLanguageTexts>>(fieldMetaData,
																	 														      	  		new MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<FieldMetaDataForLanguageTexts>(fieldMetaData));			
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForCollection,
												MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForCollection>> forCollectionField(final Class<?> componentType) {
			FieldMetaDataForCollection fieldMetaData = new FieldMetaDataForCollection(_fieldId,
																					  _name,_description,
																					  new FieldMetaDataSearchEngineIndexingConfig(),
																					  componentType);
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForCollection,
														MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForCollection>>(fieldMetaData,
																	 														      	 new MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForCollection>(fieldMetaData));
		}
		public MetaDataConfigBuilderIndexingCfg<FieldMetaDataForMap,
												MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForMap>> forMapField(final Class<?> keyType,final Class<?> valueType) {
			FieldMetaDataForMap fieldMetaData = new FieldMetaDataForMap(_fieldId,
																	    _name,_description,
																	    new FieldMetaDataSearchEngineIndexingConfig(),
																	    keyType,valueType);
			return new MetaDataConfigBuilderIndexingCfg<FieldMetaDataForMap,
														MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForMap>>(fieldMetaData,
																	 														  new MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForMap>(fieldMetaData));
			
		}
		public FieldMetaDataForDependentObject forDependantObject(final Class<?> objType,
																   final Set<FieldMetaData> childMetaData) {
			return new FieldMetaDataForDependentObject(_fieldId,
											   		   _name,_description,
											   		   new FieldMetaDataSearchEngineIndexingConfig(),
											   		   objType,
											   		   childMetaData);	
		}
		public FieldMetaDataForDependentObject forDependantObject(final Class<?> objType,
																  final FieldMetaData... childMetaData) {
			return new FieldMetaDataForDependentObject(_fieldId,
											   		   _name,_description,
											   		   new FieldMetaDataSearchEngineIndexingConfig(),
											   		   objType,
											   		   Sets.newHashSet(childMetaData));	
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class PolimorphicFieldMetaDataConfigBuilderTypeStep1 {
		@Getter(AccessLevel.PRIVATE) private final FieldMetaDataForPolymorphicType _fieldMetaData;
		
		public PolimorphicFieldMetaDataConfigBuilderTypeStep2 forModelObjectType(final Class<? extends ModelObject> modelObjType) {
			return new PolimorphicFieldMetaDataConfigBuilderTypeStep2(modelObjType,
																	  this);
		}
		public MetaDataConfigBuilderIndexingCfgStoreStep<FieldMetaDataForPolymorphicType,
														 MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForPolymorphicType>> searchEngine() {
			return new MetaDataConfigBuilderIndexingCfgStoreStep<FieldMetaDataForPolymorphicType,
																 MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForPolymorphicType>>(_fieldMetaData,
																																  				   new MetaDataConfigBuilderIndexingCfgTokenizableStep<FieldMetaDataForPolymorphicType>(_fieldMetaData));
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class PolimorphicFieldMetaDataConfigBuilderTypeStep2 {
		private final Class<? extends ModelObject> _modelObjType;
		private final PolimorphicFieldMetaDataConfigBuilderTypeStep1 _step1;
		
		public PolimorphicFieldMetaDataConfigBuilderTypeStep1 use(final Class<?> type) {
			_step1.getFieldMetaData().getFieldDataTypeMap()
						  			 .put(_modelObjType,type);
			return _step1;		
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingCfg<F extends FieldMetaData,
											      BUILDER_NEXT_STEP> {
		private final F _fieldMetaDataCfg;
		private final BUILDER_NEXT_STEP _builderNextStep;
		
		public MetaDataConfigBuilderIndexingCfgStoreStep<F,BUILDER_NEXT_STEP> searchEngine() {
			return new MetaDataConfigBuilderIndexingCfgStoreStep<F,BUILDER_NEXT_STEP>(_fieldMetaDataCfg,
																					  _builderNextStep);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingAlwaysStoredCfg<BUILDER_NEXT_STEP> {
		private final BUILDER_NEXT_STEP _builderNextStep;
		
		public BUILDER_NEXT_STEP searchEngine() {
			return _builderNextStep;
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingCfgStoreStep<F extends FieldMetaData,
														   BUILDER_NEXT_STEP> {
		private final F _fieldMetaDataCfg;
		private final BUILDER_NEXT_STEP _builderNextStep;
	
		public BUILDER_NEXT_STEP stored() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setStored(true);
			return _builderNextStep;
		}
		public BUILDER_NEXT_STEP notStored() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setStored(false);
			return _builderNextStep;
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingCfgAlwaysTokenizedStep<F extends FieldMetaData> {
		private final F _fieldMetaDataCfg;
		
		public F notIndexed() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setIndexed(false);
			return _fieldMetaDataCfg;
		}
		public MetaDataConfigBuilderIndexingCfgBoostingStep<F> indexed() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setIndexed(true);
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setTokenized(true);	// Tokenized
			return new MetaDataConfigBuilderIndexingCfgBoostingStep<F>(_fieldMetaDataCfg);
		}
	}																  
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingCfgNotTokenizableStep<F extends FieldMetaData> {
		private final F _fieldMetaDataCfg;
		
		public F notIndexed() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setIndexed(false);
			return _fieldMetaDataCfg;
		}
		public F indexed() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setIndexed(true);
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setTokenized(false);	// NOT tokenized!!
			return _fieldMetaDataCfg;
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingCfgTokenizableStep<F extends FieldMetaData> {
		private final F _fieldMetaDataCfg;	
		
		public F notIndexed() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setIndexed(false);
			return _fieldMetaDataCfg;
		}
		public MetaDataConfigBuilderIndexingCfgTokenizeStep<F> indexed() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setIndexed(true);
			return new MetaDataConfigBuilderIndexingCfgTokenizeStep<F>(_fieldMetaDataCfg);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingCfgTokenizeStep<F extends FieldMetaData> {
		private final F _fieldMetaDataCfg;
		
		public F notTokenized() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setTokenized(false);
			return _fieldMetaDataCfg;
		}
		public MetaDataConfigBuilderIndexingCfgBoostingStep<F> tokenized() {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setTokenized(true);
			return new MetaDataConfigBuilderIndexingCfgBoostingStep<F>(_fieldMetaDataCfg);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class MetaDataConfigBuilderIndexingCfgBoostingStep<F extends FieldMetaData> {
		private final F _fieldMetaDataCfg;
		
		public F withDefaultBoosting() {
			return _fieldMetaDataCfg;
		}
		public F withBoosting(final float boosting) {
			_fieldMetaDataCfg.getSearchEngineIndexingConfig()
							 .setBoost(boosting);
			return _fieldMetaDataCfg;
		}
	}
}
