package r01f.model.metadata;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.locale.Language;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.locale.LanguageTextsBuilder;
import r01f.model.ModelObject;
import r01f.model.annotations.ModelObjectData;
import r01f.model.facets.ModelObjectFacet;
import r01f.patterns.Memoized;
import r01f.reflection.ReflectionUtils;
import r01f.types.summary.Summary;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;


/**
 * Base type for the metaData that describes a {@link ModelObject} a
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
public abstract class ModelObjectTypeMetaDataBaseImpl<SELF_TYPE extends ModelObjectTypeMetaDataBaseImpl<SELF_TYPE>> 
    	   implements ModelObjectTypeMetaData {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter private final Class<? extends ModelObject> _type;
	
	@Getter private final long _typeCode;
	
	@Getter private final Set<ModelObjectTypeMetaData> _typeFacetsMetaData;
	
	@Getter private final Map<FieldMetaDataID,FieldMetaData> _fieldsMetaData;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ModelObjectTypeMetaDataBaseImpl(final Class<? extends ModelObject> type,final Class<? extends OID> oidType,final long typeCode) {
		// Type
		_type = type;
		_typeCode = typeCode;
		
		// Type Facets
		_typeFacetsMetaData = Sets.newLinkedHashSet(ModelObjectTypeMetaDataBuilder.facetTypesMetaDataFor(type));
		_typeFacetsMetaData.add(this);	// do not forget!
		
		// Fields
		_fieldsMetaData = Maps.newHashMap();
		
		// Init the common meta data fields
		_initCommonMetaData(type,oidType);
	}
	private void _initCommonMetaData(final Class<? extends ModelObject> type,
									 final Class<? extends OID> oidType) {
		_addFieldsMetaData(
						   // Document ID
						   FieldMetaDataConfigBuilder.forId(DOCUMENT_ID_FIELD_ID)
						 		.withName(LanguageTextsBuilder.createMapBacked()
								  					   .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
								  					   .addForLang(Language.SPANISH,"Identificador único del documento indexado")
								  					   .addForLang(Language.BASQUE,"[eu] Identificador único del documento indexado")
								  					   .addForLang(Language.ENGLISH,"Document unique identifier")
								  					   .build())
								.withNODescription()
								.forStringField()
								.searchEngine()
										.stored()
										.indexed().notTokenized(),		// do not tokenize document id!
						   // Model object type name
						   FieldMetaDataConfigBuilder.forId(TYPE_NAME_FIELD_ID)
						   		.withName(LanguageTextsBuilder.createMapBacked()
										  					  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
										  					  .addForLang(Language.SPANISH,"Nombre del tipo del objeto del modelo")
										  					  .addForLang(Language.BASQUE,"[eu] Nombre del tipo del objeto del modelo")
										  					  .addForLang(Language.ENGLISH,"Model object's type's name")
										  					  .build())
								.withNODescription()
								.forJavaTypeField(type)
								.searchEngine()
										.stored()
										.notIndexed(),
						   // Model object Type 
						   FieldMetaDataConfigBuilder.forId(TYPE_FIELD_ID)
						   		.withName(LanguageTextsBuilder.createMapBacked()
										  					  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
										  					  .addForLang(Language.SPANISH,"Tipo del objeto del modelo")
										  					  .addForLang(Language.BASQUE,"[eu] Tipo del objeto del modelo")
										  					  .addForLang(Language.ENGLISH,"Model object's Type")
										  					  .build())
								.withNODescription()
								.forLongField()
								.searchEngine()
										.stored()
										.indexed(),
						   // Model object facet types 
						   // A model object can have many facets (think of a benefit content that's also a government service content)
						   FieldMetaDataConfigBuilder.forId(TYPE_FACETS_FIELD_ID)
						   		.withName(LanguageTextsBuilder.createMapBacked()
										  					  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
										  					  .addForLang(Language.SPANISH,"Facets del objeto del modelo")
										  					  .addForLang(Language.BASQUE,"[eu] Facets del objeto del modelo")
										  					  .addForLang(Language.ENGLISH,"Model object's facets")
										  					  .build())
								.withNODescription()
								.forCollectionField(Long.class)
								.searchEngine()
										.stored()
										.indexed().notTokenized(),
						   // OID 
						   FieldMetaDataConfigBuilder.forId(OID_FIELD_ID)
							    .withName(LanguageTextsBuilder.createMapBacked()
									  					      .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
									  					      .addForLang(Language.SPANISH,"Identificador único del objeto del modelo")
									  					      .addForLang(Language.BASQUE,"[eu] Identificador único del objeto del modelo")
									  					      .addForLang(Language.ENGLISH,"Model Object's unique identifier")
									  					      .build())
								.withNODescription()
								.forOIDField(oidType)
								.searchEngine()
										.notIndexed(),
					       // Numeric ID
						   FieldMetaDataConfigBuilder.forId(NUMERIC_ID_FIELD_ID)
							    .withName(LanguageTextsBuilder.createMapBacked()
									  					      .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
									  					      .addForLang(Language.SPANISH,"Identificador numérico del objeto del modelo")
									  					      .addForLang(Language.BASQUE,"[eu] Identificador único del objeto del modelo")
									  					      .addForLang(Language.ENGLISH,"Model Object's unique identifier")
									  					      .build())
								.withNODescription()
								.forLongField()
								.searchEngine()
										.stored()
										.indexed(),
						   // Entity version
						   FieldMetaDataConfigBuilder.forId(ENTITY_VERSION_FIELD_ID)
								.withName(LanguageTextsBuilder.createMapBacked()
										  					  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
										  					  .addForLang(Language.SPANISH,"Versión persistida (BBDD) del objeto del modelo (optimistic locking)")
										  					  .addForLang(Language.BASQUE,"[eu] Versión persistida (BBDD) del objeto del modelo (optimistic locking)")
										  					  .addForLang(Language.ENGLISH,"Model object's persisted version (BBDD) (used for optimistic locking)")
										  					  .build())
								.withNODescription()
								.forLongField()
								.searchEngine()
										.stored()
										.notIndexed(),
						   // Create date
						   FieldMetaDataConfigBuilder.forId(CREATE_DATE_FIELD_ID)
							  	.withName(LanguageTextsBuilder.createMapBacked()
									  					      .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
									  					      .addForLang(Language.SPANISH,"Fecha de creación")
									  					      .addForLang(Language.BASQUE,"[eu] Fecha de creación")
									  					      .addForLang(Language.ENGLISH,"Create date")
									  					      .build())
							    .withNODescription()
							    .forDateField()
							    .searchEngine()
							    		.stored()
							    		.indexed(),
						   // Last update date
						   FieldMetaDataConfigBuilder.forId(LAST_UPDATE_DATE_FIELD_ID)
							 	.withName(LanguageTextsBuilder.createMapBacked()
									  					   	  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
									  					   	  .addForLang(Language.SPANISH,"Fecha de última actualización")
									  					   	  .addForLang(Language.BASQUE,"[eu] Fecha de última actualización")
									  					   	  .addForLang(Language.ENGLISH,"Last update date")
									  					   	  .build())
							    .withNODescription()
							    .forDateField()
							    .searchEngine()
							    		.stored()
							    		.indexed(),
						   // Creator
						   FieldMetaDataConfigBuilder.forId(CREATOR_FIELD_ID)
							    .withName(LanguageTextsBuilder.createMapBacked()
									  					      .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
									  					      .addForLang(Language.SPANISH,"Usuario/a que ha creado el objeto del modelo")
									  					      .addForLang(Language.BASQUE,"[eu] Usuario/a que ha creado el objeto del modelo")
									  					      .addForLang(Language.ENGLISH,"The user who created the model object")
									  					      .build())
							    .withNODescription()
							    .forOIDField(UserCode.class)
							    .searchEngine()
							    		.indexed(),
						   // Last updator
						   FieldMetaDataConfigBuilder.forId(LAST_UPDATOR_FIELD_ID)
								.withName(LanguageTextsBuilder.createMapBacked()
										  					  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
										  					  .addForLang(Language.SPANISH,"Usuario/a que ha actualizado el objeto del modelo por última vez")
										  					  .addForLang(Language.BASQUE,"[eu] Usuario/a que ha actualizado el objeto del modelo por última vez")
										  					  .addForLang(Language.ENGLISH,"The user who last updated the model object")
										  					  .build())
								.withNODescription()
								.forOIDField(UserCode.class)
								.searchEngine()
										.indexed()
						   );
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static FieldMetaData _createModelObjectSummaryFieldMetaData(final Class<? extends Summary> summaryType) {
		return FieldMetaDataConfigBuilder.forId(HasFieldsMetaDataForHasSummaryModelObject.SUMMARY_FIELD_ID)
					  .withName(LanguageTextsBuilder.createMapBacked()
			  					   					.withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
			  					   .addForLang(Language.SPANISH,"Resumen del objeto")
			  					   .addForLang(Language.BASQUE,"[eu] Resumen del objeto")
			  					   .addForLang(Language.ENGLISH,"Object's summary")
			  					   .build())
					  .withNODescription()
					  .forSummaryField(summaryType)
					  .searchEngine()
					  		.stored()
					  		.notIndexed();
	}
	protected static FieldMetaData _createModelObjectFullTextSummaryFieldMetaData(final Class<? extends Summary> summaryType) {
		return FieldMetaDataConfigBuilder.forId(HasFieldsMetaDataForHasFullTextSummaryModelObject.FULL_TEXT_SUMMARY_FIELD_ID)
	 				  .withName(LanguageTextsBuilder.createMapBacked()
			  					   					.withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
			  					   .addForLang(Language.SPANISH,"Resumen para búsqueda en texto completo del objeto")
			  					   .addForLang(Language.BASQUE,"[eu] Resumen para búsqueda en texto completo del objeto")
			  					   .addForLang(Language.ENGLISH,"Object's full-text summary")
			  					   .build())
					  .withNODescription()
					  .forSummaryField(summaryType)
					  .searchEngine()
					  		.notStored()
					  		.indexed().withDefaultBoosting();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static FieldMetaData _createLanguageFieldMetaData() {
		return FieldMetaDataConfigBuilder.forId(HasFieldMetaDataForHasLanguageModelObject.LANGUAGE_FIELD_ID)
	 				  .withName(LanguageTextsBuilder.createMapBacked()
			  					   					.withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
			  					   .addForLang(Language.SPANISH,"Lenguaje del objeto")
			  					   .addForLang(Language.BASQUE,"[eu] Lenguaje del objeto")
			  					   .addForLang(Language.ENGLISH,"Model object's language")
			  					   .build())
					  .withNODescription()
					  .forLanguageField()
					  .searchEngine()
					  		.stored()
					  		.indexed();
	}
	protected static FieldMetaData _createLanguageCodeFieldMetaData() {
		return FieldMetaDataConfigBuilder.forId(HasFieldMetaDataForHasLanguageModelObject.LANGUAGE_CODE_FIELD_ID)
	 				  .withName(LanguageTextsBuilder.createMapBacked()
			  					   					.withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
			  					   .addForLang(Language.SPANISH,"Código del lenguaje del objeto")
			  					   .addForLang(Language.BASQUE,"[eu] Código del lenguaje del objeto")
			  					   .addForLang(Language.ENGLISH,"Model object's language code")
			  					   .build())
					  .withNODescription()
					  .forIntegerField()
					  .searchEngine()
					  		.stored()
					  		.notIndexed();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <M extends FieldMetaData> SELF_TYPE _addFieldsMetaData(final M... fieldsMetaData) {
		if (CollectionUtils.hasData(fieldsMetaData)) {
			for (M fieldMetaData : fieldsMetaData) {
				_fieldsMetaData.put(fieldMetaData.getFieldId(),
									fieldMetaData);
			}
		}
		return (SELF_TYPE)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    private Memoized<Set<Long>> _typeFacetsCodes = 
    			new Memoized<Set<Long>>() {
						@Override
						protected Set<Long> supply() {
							return FluentIterable.from(_typeFacetsMetaData)
												 .transform(new Function<ModelObjectTypeMetaData,Long>() {
																	@Override
																	public Long apply(final ModelObjectTypeMetaData metaData) {
																		return metaData.getTypeCode();
																	}
														     })
												 .toSet();
						}
    			};
	public Set<Long> getTypeFacetsCodes() {
		return _typeFacetsCodes.get();
	}
	private Memoized<Set<Class<? extends ModelObject>>> _typeFacets =
				new Memoized<Set<Class<? extends ModelObject>>>() {
						@Override
						protected Set<Class<? extends ModelObject>> supply() {
							return FluentIterable.from(_typeFacetsMetaData)
												 .transform(new Function<ModelObjectTypeMetaData,Class<? extends ModelObject>>() {
																	@Override
																	public Class<? extends ModelObject> apply(final ModelObjectTypeMetaData metaData) {
																		return metaData.getType();
																	}
														     })
												 .toSet();
						}
				};
	public Set<Class<? extends ModelObject>> getTypeFacets() {
		return _typeFacets.get();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public Collection<FieldMetaDataID> getFieldsMetaDataIds() {
		return (Collection<FieldMetaDataID>)(_fieldsMetaData != null ? _fieldsMetaData.keySet() : Lists.newArrayList());
	}
	@Override @SuppressWarnings("unchecked")
	public <M extends FieldMetaData> M getFieldMetaDataFor(final FieldMetaDataID metaDataId) {
		M outFieldMetaData = null;
		
		// first try this type fields
		outFieldMetaData = _fieldsMetaData != null ? (M)_fieldsMetaData.get(metaDataId)
									   			   : null;
		// if the field is NOT found, try all facet's fields
		if (outFieldMetaData == null && CollectionUtils.hasData(_typeFacetsMetaData)) {
			for (ModelObjectTypeMetaData facetMetaData : _typeFacetsMetaData) {
				if (facetMetaData == this) continue;
				outFieldMetaData = facetMetaData.<M>getFieldMetaDataFor(metaDataId);
				if (outFieldMetaData != null) break;
			}
		}
		return outFieldMetaData;
	}
	@Override
	public FieldMetaData getTypeNameFieldMetaData() {
		return this.getFieldMetaDataFor(TYPE_NAME_FIELD_ID);
	}
	@Override
	public FieldMetaData getTypeFieldMetaData() {
		return this.getFieldMetaDataFor(TYPE_FIELD_ID);
	}
	@Override
	public FieldMetaData getFacetTypesFieldMetaData() {
		return this.getFieldMetaDataFor(TYPE_FACETS_FIELD_ID);
	}
	@Override
	public FieldMetaData getDocumentIDFieldMetaData() {
		return this.getFieldMetaDataFor(DOCUMENT_ID_FIELD_ID);
	}
	@Override
	public FieldMetaData getOIDFieldMetaData() {
		return this.getFieldMetaDataFor(OID_FIELD_ID);
	}
	@Override
	public FieldMetaData getNumericIDFieldMetaData() {
		return this.getFieldMetaDataFor(NUMERIC_ID_FIELD_ID);
	}
	@Override
	public FieldMetaData getEntityVersionFieldMetaData() {
		return this.getFieldMetaDataFor(ENTITY_VERSION_FIELD_ID);
	}
	@Override
	public FieldMetaData getCreateDateFieldMetaData() {
		return this.getFieldMetaDataFor(CREATE_DATE_FIELD_ID);
	}
	@Override
	public FieldMetaData getCreatorFieldMetaData() {
		return this.getFieldMetaDataFor(CREATOR_FIELD_ID);
	}
	@Override
	public FieldMetaData getLastUpdateDateFieldMetaData() {
		return this.getFieldMetaDataFor(LAST_UPDATE_DATE_FIELD_ID);
	}
	@Override
	public FieldMetaData getLastUpdatorFieldMetaData() {
		return this.getFieldMetaDataFor(LAST_UPDATOR_FIELD_ID);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <T extends HasFieldsMetaData> T as(final Class<T> hasFieldsMetaDataType) {
		// Try to find the first ModelObjectMetaData assignable to the interface
		if (ReflectionUtils.isImplementing(this.getClass(),hasFieldsMetaDataType)) {
			return (T)this;
		} 
		// try to find the requested type in the facets 
		for (ModelObjectTypeMetaData currTypeMetaData : _typeFacetsMetaData) {
			if (currTypeMetaData.getType() == _type) continue;		// ignore this type metadata
			if (ReflectionUtils.isImplementing(currTypeMetaData.getClass(),hasFieldsMetaDataType)) {
				return (T)currTypeMetaData;
			}
		}
		throw new IllegalArgumentException(Throwables.message("The type {} or any of it's base types is NOT implementing {}",
															  this.getClass(),hasFieldsMetaDataType));
	}
	@Override
	public <T extends HasFieldsMetaData> boolean is(final Class<T> hasFieldsMetaDataType) {		
		// Try to find the first ModelObjectMetaData assignable to the interface
		if (ReflectionUtils.isImplementing(this.getClass(),hasFieldsMetaDataType)) {
			return true;
		} 
		// try to find the requested type in the facets 
		for (ModelObjectTypeMetaData currTypeMetaData : _typeFacetsMetaData) {
			if (currTypeMetaData.getType() == _type) continue;		// ignore this type metadata
			if (ReflectionUtils.isImplementing(currTypeMetaData.getClass(),hasFieldsMetaDataType)) {
				return true;
			}
		}
		return false;
	}
	@Override
	public <F extends ModelObjectFacet> boolean hasFacet(final Class<F> facetType) {
		// Try to find the first ModelObjectMetaData assignable to the interface
		if (ReflectionUtils.isImplementing(_type,facetType)) {
			return true;
		} 
		// try to find the requested type in the facets 
		for (ModelObjectTypeMetaData currTypeMetaData : _typeFacetsMetaData) {
			if (currTypeMetaData.getType() == _type) continue;		// ignore this type metadata
			if (ReflectionUtils.isImplementing(currTypeMetaData.getType(),facetType)) {
				return true;
			}
		}
		return false;
	}
}
