package r01f.model.metadata;

import java.util.Collection;
import java.util.Map;

/**
 * An interface implemented by objects that encapsulates metadata about model object's fields: {@link ModelObjectTypeMetaData} objects
 */
public interface HasFieldsMetaData {
	/**
	 * @return a {@link Collection} of {@link FieldMetaDataID}s
	 */
	public Collection<FieldMetaDataID> getFieldsMetaDataIds();
	/**
	 * @return a type that encapsulates metadata about model object's fields
	 */
	public Map<FieldMetaDataID,? extends FieldMetaData> getFieldsMetaData();
	/**
	 * Returns the {@link FieldMetaData} for a given metadata id
	 * @param metaDataId
	 * @return
	 */
	public <M extends FieldMetaData> M getFieldMetaDataFor(final FieldMetaDataID metaDataId);
}
