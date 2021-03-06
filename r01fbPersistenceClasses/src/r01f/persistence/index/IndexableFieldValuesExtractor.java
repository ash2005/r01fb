package r01f.persistence.index;

import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.metadata.FieldMetaDataID;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.persistence.index.document.IndexDocumentFieldValue;
import r01f.persistence.index.document.IndexDocumentFieldValueSet;
import r01f.usercontext.UserContext;

/**
 * Interface for types extracting field values from {@link PersistableModelObject}s in order to be indexed
 */
public interface IndexableFieldValuesExtractor<M extends IndexableModelObject> {
	/**
	 * Returns the type of the model object subject for the extraction 
	 * @return
	 */
	public Class<M> getSubjectModelObjectType();
	/**
	 * Returns a field value
	 * @param metaDataId
	 * @return
	 */
	public <T> T getFieldValue(final FieldMetaDataID metaDataId);
	/**
	 * Extracts all fields values from the {@link PersistableModelObject}
	 * @param userContext
	 * @param modelObj
	 * @param reqOp the requested persistence operation
	 */
	public void extractFields(final UserContext userContext,
							  final M modelObj,
							  final PersistenceRequestedOperation reqOp);
	/**
	 * Returns all extracted fields from the {@link PersistableModelObject}
	 * as a {@link IndexDocumentFieldValueSet}
	 * @return
	 */
	public IndexDocumentFieldValueSet getFields();
	/**
	 * Adds a field to the extracted field set
	 * @param fieldValue
	 */
	public <T> void addField(final IndexDocumentFieldValue<T> fieldValue);
}
