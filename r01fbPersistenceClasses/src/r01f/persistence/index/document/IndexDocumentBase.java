package r01f.persistence.index.document;

import java.util.Map;

import r01f.exceptions.Throwables;
import r01f.model.metadata.FieldMetaDataID;
import r01f.patterns.Memoized;

public abstract class IndexDocumentBase 
		   implements IndexDocument {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private Memoized<Map<FieldMetaDataID,IndexDocumentFieldValue<?>>> _fields = new Memoized<Map<FieldMetaDataID,IndexDocumentFieldValue<?>>>() {
																					@Override
																					protected Map<FieldMetaDataID,IndexDocumentFieldValue<?>> supply() {
																						return IndexDocumentBase.this.getFields();
																					}
																		   };
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <T> IndexDocumentFieldValue<T> getField(final FieldMetaDataID metaDataId) {
		return (IndexDocumentFieldValue<T>)_fields.get()
					  							  .get(metaDataId);
	}
	@Override
	public <T> T getFieldValue(final FieldMetaDataID metaDataId) {
		IndexDocumentFieldValue<T> indexedField = this.getField(metaDataId);
		return indexedField != null ? indexedField.getValue() : null;
	}
	@Override @SuppressWarnings("unchecked")
	public <T> IndexDocumentFieldValue<T> getFieldOrThrow(final FieldMetaDataID metaDataId) {
		IndexDocumentFieldValue<T> outField = (IndexDocumentFieldValue<T>)_fields.get()
					  							  								 .get(metaDataId);
		if (outField == null) throw new IllegalStateException(Throwables.message("The indexed document does NOT contains a field with name {}",metaDataId));
		return outField;
	}
	@Override
	public <T> T getFieldValueOrThrow(final FieldMetaDataID metaDataId) {
		return this.<T>getFieldOrThrow(metaDataId)
				   .getValue();
	}

}
