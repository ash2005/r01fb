package r01f.persistence.index.document;

import java.util.Map;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;

import r01f.model.ModelObject;
import r01f.model.metadata.FieldMetaDataID;
import r01f.model.metadata.ModelObjectTypeMetaData;

/**
 * Model a search engine's index document (for example a wrapper of a Lucene {@link Document})
 */
public interface IndexDocument {
	/**
	 * Gets the metadata for model object's type  
	 * (a model object is defined by two types: a base (generic) type and a concrete one)
	 * @return
	 */
	public ModelObjectTypeMetaData getModelObjectTypeMetaData();
	/**
	 * Gets the model object's type this document is about
	 * (a model object is defined by two types: a base (generic) type and a concrete one)
	 * @return
	 */
	public Class<? extends ModelObject> getModelObjectType();
	/**
	 * Transform all lucene indexed fields ({@link IndexableField}s) to instances of {@link IndexDocumentFieldValue}
	 * Beware that sometimes:
	 * 	- a single metadata is stored in multiple lucene-indexed fields (ie language-dependent summaries or ranges)
	 * 	- a metadata can contain multiple values
	 * @return
	 */
	public Map<FieldMetaDataID,IndexDocumentFieldValue<?>> getFields();
	/**
	 * Returns a document's field by it's id
	 * @param metaDataId
	 * @return
	 */
	public <T> IndexDocumentFieldValue<T> getField(final FieldMetaDataID metaDataId);
	/**
	 * Returns a document's field value by it's id
	 * @param metaDataId
	 * @return
	 */
	public <T> T getFieldValue(final FieldMetaDataID metaDataId);
	/**
	 * Returns a document's field by it's id or throw an {@link IllegalStateException} 
	 * if the field is NOT found
	 * @param metaDataId
	 * @return
	 */
	public <T> IndexDocumentFieldValue<T> getFieldOrThrow(final FieldMetaDataID metaDataId);
	/**
	 * Returns a document's field value by it's id or throw an {@link IllegalStateException} 
	 * if the field is NOT found
	 * @param metaDataId
	 * @return
	 */
	public <T> T getFieldValueOrThrow(final FieldMetaDataID metaDataId);
}
