package r01f.persistence.index;

import java.util.Date;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.guids.VersionIndependentOID;
import r01f.guids.VersionOID;
import r01f.locale.Language;
import r01f.model.IndexableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Facetables;
import r01f.model.facets.FullTextSummarizable.HasFullTextSummaryFacet;
import r01f.model.facets.HasID;
import r01f.model.facets.HasLanguage;
import r01f.model.facets.HasOID;
import r01f.model.facets.Summarizable.HasSummaryFacet;
import r01f.model.facets.Versionable;
import r01f.model.metadata.FieldMetaData;
import r01f.model.metadata.FieldMetaDataID;
import r01f.model.metadata.HasFieldMetaDataForHasLanguageModelObject;
import r01f.model.metadata.HasFieldsMetaDataForHasFullTextSummaryModelObject;
import r01f.model.metadata.HasFieldsMetaDataForHasIDModelObject;
import r01f.model.metadata.HasFieldsMetaDataForHasSummaryModelObject;
import r01f.model.metadata.HasFieldsMetaDataForVersionableModelObject;
import r01f.model.metadata.ModelObjectTypeMetaDataBaseImpl;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.persistence.index.document.IndexDocumentFieldValue;
import r01f.persistence.index.document.IndexDocumentFieldValueSet;
import r01f.types.summary.Summary;
import r01f.usercontext.UserContext;

/**
 * Extracts COMMON field values from {@link PersistableModelObject}s
 * This type MUST be extended to extract the type-specific fields
 * 
 * IMPORTANT!! it's NOT thread safe!!!!!
 */
@Slf4j
@Accessors(prefix="_")
public abstract class IndexableFieldValueExtractorBase<M extends IndexableModelObject> 
           implements IndexableFieldValuesExtractor<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected final Class<M> _modelObjectType;
	
	private final IndexDocumentFieldValueSet _fields = IndexDocumentFieldValueSet.create();
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexableFieldValueExtractorBase(final Class<M> modelObjectType) {
		_modelObjectType = modelObjectType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ACCESSORS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Class<M> getSubjectModelObjectType() {
		return _modelObjectType;
	}
	@Override
	public IndexDocumentFieldValueSet getFields() {
		return _fields;
	}
	@Override
	public <T> void addField(final IndexDocumentFieldValue<T> fieldValue) {
		_fields.add(fieldValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public void extractFields(final UserContext userContext,
							  final M modelObj,
							  final PersistenceRequestedOperation reqOp) {
		log.debug("Extracting indexable field values:");
		
		// The java type
		FieldMetaData typeNameField = modelObj.getModelObjectMetaData()
											  .getTypeNameFieldMetaData();
		_fields.add(IndexDocumentFieldValue.forMetaData(typeNameField)
										   .andValue(modelObj.getClass()));
		log.debug("\t-{}={}",typeNameField.getIndexableFieldId(),modelObj.getClass());
		
		// The concrete type in a single-valued field
		FieldMetaData typeField = modelObj.getModelObjectMetaData()
						   				  .getTypeFieldMetaData();
		long typeCode = ((ModelObjectTypeMetaDataBaseImpl<?>)modelObj.getModelObjectMetaData()).getTypeCode();
		_fields.add(IndexDocumentFieldValue.forMetaData(typeField)
									       .andValue(typeCode));
		log.debug("\t-{}={}",typeField.getIndexableFieldId(),typeCode);
		
		// The type facets in a multi-valued field
		FieldMetaData facetTypesField = modelObj.getModelObjectMetaData()
												.getFacetTypesFieldMetaData();
		Set<Long> facetTypesCodes = ((ModelObjectTypeMetaDataBaseImpl<?>)modelObj.getModelObjectMetaData()).getTypeFacetsCodes();
		_fields.add(IndexDocumentFieldValue.forMetaData(facetTypesField)
								       	   .andValues(facetTypesCodes));
		log.debug("\t-{}={}",facetTypesField.getIndexableFieldId(),facetTypesCodes);
		
		// oid
		OID oid = Facetables.asFacet(modelObj,HasOID.class)
							.getOid();
		if (oid != null) {
			FieldMetaData oidField = modelObj.getModelObjectMetaData().getOIDFieldMetaData();
			log.debug("\t-{}={}",oidField,oid);
			_fields.add(IndexDocumentFieldValue.forMetaData(oidField)
									       	   .andValue(oid));
		}
		// Entity Version
		long entityVersion = modelObj.getEntityVersion();
		if (entityVersion >= 0) {
			FieldMetaData entityVersionField = modelObj.getModelObjectMetaData().getEntityVersionFieldMetaData();
			log.debug("\t-{}={}",entityVersionField.getIndexableFieldId(),entityVersion);
			_fields.add(IndexDocumentFieldValue.forMetaData(entityVersionField)
										   	   .andValue(entityVersion));
		}
		// numeric id
		long numericId = modelObj.getNumericId();
		if (numericId >= 0) {
			FieldMetaData numericIdField = modelObj.getModelObjectMetaData().getNumericIDFieldMetaData();
			log.debug("\t-{}={}",numericIdField.getIndexableFieldId(),numericId);
			_fields.add(IndexDocumentFieldValue.forMetaData(numericIdField)
								       	   	   .andValue(numericId));
		}
		// version & version independent oid (it's INSIDE the OID)
		if (modelObj instanceof Versionable) {
			Versionable versionable = (Versionable)modelObj;
			VersionIndependentOID versionIndependentOid = versionable.getVersionIndependentOid();
			VersionOID version = versionable.getVersionOid();
			if (version != null) {
				FieldMetaData versionField = modelObj.getModelObjectMetaData().as(HasFieldsMetaDataForVersionableModelObject.class)
																			  .getVersionFieldMetaData();
				log.debug("\t-{}={}",versionField.getIndexableFieldId(),version);
				_fields.add(IndexDocumentFieldValue.forMetaData(versionField)
									           	   .andValue(version));
			}
			if (versionIndependentOid != null) {
				FieldMetaData versionIndependentField = modelObj.getModelObjectMetaData().as(HasFieldsMetaDataForVersionableModelObject.class)
																						 .getVersionFieldMetaData();
				log.debug("\t-{}={}",versionIndependentField.getIndexableFieldId(),versionIndependentOid);
				_fields.add(IndexDocumentFieldValue.forMetaData(versionIndependentField)
									           	   .andValue(versionIndependentOid));
			}
		}
		// language
		if (modelObj.hasFacet(HasLanguage.class)) {
			HasLanguage hasLang = modelObj.asFacet(HasLanguage.class);
			Language lang = hasLang.getLanguage();
			if (lang != null) {
				FieldMetaData langField = modelObj.getModelObjectMetaData().as(HasFieldMetaDataForHasLanguageModelObject.class)
																		   .getLanguageFieldMetaData();
				FieldMetaData langCodeField = modelObj.getModelObjectMetaData().as(HasFieldMetaDataForHasLanguageModelObject.class)
																			   .getLanguageCodeFieldMetaData();
				log.debug("\t-{}={}",langField.getIndexableFieldId(),lang);
				_fields.add(IndexDocumentFieldValue.forMetaData(langField)
											   	   .andValue(lang));
				_fields.add(IndexDocumentFieldValue.forMetaData(langCodeField)
											   	   .andValue(lang.getCode()));
			}
		}
		// id
		if (modelObj.hasFacet(HasID.class)) {
			HasID<?> hasId = modelObj.asFacet(HasID.class);
			OID id = hasId.getId();
			if (id != null) {
				FieldMetaData idField = modelObj.getModelObjectMetaData().as(HasFieldsMetaDataForHasIDModelObject.class)
																		 .getIDFieldMetaData();
				log.debug("\t-{}={}",idField.getIndexableFieldId(),id);
				_fields.add(IndexDocumentFieldValue.forMetaData(idField)
											   	   .andValue(id));
			}
		}
		// summary
		if (modelObj.hasFacet(HasSummaryFacet.class)) {
			HasSummaryFacet summarizable = modelObj.asFacet(HasSummaryFacet.class);
			Summary summary = summarizable.asSummarizable()
										  .getSummary();
			if (summary != null) {
				FieldMetaData summaryField = modelObj.getModelObjectMetaData().as(HasFieldsMetaDataForHasSummaryModelObject.class)
																			  .getSummaryFieldMetaData();
				log.debug("\t-{}={}",summaryField.getIndexableFieldId(),summary);
				_fields.add(IndexDocumentFieldValue.forMetaData(summaryField)
											   	   .andValue(summary));
			} else {
				log.warn("The model obj {} with oid={} has {} BUT no summary was extracted",
						 modelObj.getClass(),
						 Facetables.asFacet(modelObj,HasOID.class)
						 		   .getOid(),
						 HasSummaryFacet.class);
			}
		}
		// ... full text summary 
		if (modelObj.hasFacet(HasFullTextSummaryFacet.class)) {
			HasFullTextSummaryFacet summarizable = modelObj.asFacet(HasFullTextSummaryFacet.class);
			Summary fullText = summarizable.asFullTextSummarizable()
										   .getFullTextSummary();
			if (fullText != null) {
				if (!fullText.isFullTextSummary()) throw new IllegalArgumentException(Throwables.message("The returned summary for {} is NOT a FULL-TEXT summary",
																									    modelObj.getClass()));
				FieldMetaData summaryField = modelObj.getModelObjectMetaData().as(HasFieldsMetaDataForHasFullTextSummaryModelObject.class)
																			  .getFullTextSummaryFieldMetaData();
				log.debug("\t-{}={}",summaryField.getIndexableFieldId(),fullText);
				_fields.add(IndexDocumentFieldValue.forMetaData(summaryField)
										       	   .andValue(fullText));
			} else {
				log.warn("The model obj {} with oid={} has {} BUT no full text summary was extracted",
						 modelObj.getClass(),
						 Facetables.asFacet(modelObj,HasOID.class)
						 		   .getOid(),
						 HasFullTextSummaryFacet.class);
			}
		}
		// [3] - Add the create / last update info 
		if (reqOp.is(PersistenceRequestedOperation.CREATE)) {
			FieldMetaData createDateField = modelObj.getModelObjectMetaData().getCreateDateFieldMetaData();
			_fields.add(IndexDocumentFieldValue.forMetaData(createDateField)
										   .andValue(new Date()));
			if (userContext.getUserCode() != null) {
				FieldMetaData creatorField = modelObj.getModelObjectMetaData().getCreatorFieldMetaData();
				_fields.add(IndexDocumentFieldValue.forMetaData(creatorField)
										       	   .andValue(userContext.getUserCode()));
			}
		} else if (reqOp.is(PersistenceRequestedOperation.UPDATE)) {
			FieldMetaData lastUpdateDateField = modelObj.getModelObjectMetaData().getLastUpdateDateFieldMetaData();
			_fields.add(IndexDocumentFieldValue.forMetaData(lastUpdateDateField)
										   	   .andValue(new Date()));
			if (userContext.getUserCode() != null) {
				FieldMetaData lastUpdatorField = modelObj.getModelObjectMetaData().getLastUpdatorFieldMetaData();
				_fields.add(IndexDocumentFieldValue.forMetaData(lastUpdatorField)
												   .andValue(userContext.getUserCode()));
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <T> T getFieldValue(final FieldMetaDataID metaDataId) {
		IndexDocumentFieldValue<T> fieldValue = _fields.get(metaDataId);
		return fieldValue != null ? fieldValue.getValue()
								  : null;
	}


}
