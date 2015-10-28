package r01f.persistence.index;

import javax.inject.Provider;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.persistence.index.document.IndexDocumentFieldValueSet;
import r01f.usercontext.UserContext;

/**
 * Base type for indexers
 * @param <P>
 */
@Accessors(prefix="_")
public abstract class IndexerBase<P extends IndexableModelObject<? extends OID>> 
           implements Indexer<P> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object type for this indexer
	 */
	@Getter private final Class<P> _modelObjectType;
	/**
	 * MetaData about the model object
	 */
	@Getter private final transient ModelObjectTypeMetaData _modelObjectMetaData;
	/**
	 * Provider for the indexable field values extractor
	 * (the extractor is NOT thread safe!!!... that's why a {@link Provider} is used
	 */
	private final Provider<IndexableFieldValuesExtractor<P>> _indexableFieldValuesExtractorProvider;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexerBase(final Class<P> modelObjectType,
					   final Provider<IndexableFieldValuesExtractor<P>> indexableFieldValuesExtractorProvider) {
		_modelObjectType = modelObjectType;
		_modelObjectMetaData = ModelObjectTypeMetaDataBuilder.createFor(modelObjectType);
		_indexableFieldValuesExtractorProvider = indexableFieldValuesExtractorProvider;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Indexable field extractor
	 */
	protected IndexDocumentFieldValueSet _extractIndexableFields(final UserContext userContext,
															 final P modelObject,
															 final PersistenceRequestedOperation reqOp) {
		// The indexable field values extractor is NOT thread safe... it's mandatory to create a new instance 
		// every time an extraction has to be done
		IndexableFieldValuesExtractor<P> indexableFieldValuesExtractor = _indexableFieldValuesExtractorProvider.get();
		if (indexableFieldValuesExtractor.getSubjectModelObjectType() != _modelObjectType) throw new IllegalStateException(Throwables.message("A field extractor of type {} cannot be used with an indexer of type {}",
																																			  indexableFieldValuesExtractor.getSubjectModelObjectType(),_modelObjectType));
		indexableFieldValuesExtractor.extractFields(userContext,
										 			modelObject,
										 			reqOp);
		return indexableFieldValuesExtractor.getFields();
	}
}
