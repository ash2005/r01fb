package r01f.persistence.index.document;

import java.util.regex.Pattern;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.guids.OIDBase;
import r01f.locale.Language;
import r01f.model.metadata.FieldMetaData;
import r01f.model.metadata.IndexableFieldID;
import r01f.model.metadata.FieldMetaDataID;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.types.summary.Summary;

/**
 * The id of an index-stored metadata (the id of a field in an indexed document)
 * Beware that the {@link IndexDocumentFieldID} might NOT be the same as the {@link FieldMetaDataID} for fields
 * with multiple "dimensions" like multi-language {@link Summary} fields where each language {@link Summary}
 * is stored in a separate field with an id like: metaDataId.{dimensionId}
 * For example, a multi-language {@link Summary} field with {@link FieldMetaDataID}=r01.summary
 * is stored in multiple lucene fields, one for each document: r01.summary.es, r01.summary.eu, etc
 */
@EqualsAndHashCode(callSuper=true)
@Accessors(prefix="_")
@RequiredArgsConstructor
public class IndexDocumentFieldID 
     extends OIDBase<String> {

	private static final long serialVersionUID = -1938356780090697086L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final String _id; 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static IndexDocumentFieldID forId(final String id) {
		return new IndexDocumentFieldID(id);
	}
	public static IndexDocumentFieldID forId(final IndexableFieldID fieldId) {
		return IndexDocumentFieldID.forId(fieldId.asString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Sometimes a metadata value depends on some dimension 
//  For example, language dependant metadata has different values for every available lang 
//	For these types of metaData, the values are stored in multiple fields at the SAME document 
// 	(there's a SINGLE document with all language's data, NOT a document per language)
//	These language-dependant fields are named as: {fieldBaseName}.{language}
//	For example, the [name] property could be stored in the SAME document as:
//			name.SPANISH = the name in spanish
//			name.ENGLISH = the name in english
//			...
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the {@link IndexDocumentFieldID} for a dynamic dimension dependent field (ie language)
	 * Sometimes a metadata value depends on some dimension 
  	 * For example, language dependant metadata has different values for every available lang 
	 * For these types of metaData, the values are stored in multiple fields at the SAME document 
	 * (there's a SINGLE document with all language's data, NOT a document per language)
	 * These language-dependant fields are named as: {fieldBaseName}.{language}
	 * For example, the [name] property could be stored in the SAME document as:
	 *			name.SPANISH = the name in spanish
 	 *			name.ENGLISH = the name in english
	 *			...
	 * @param fieldName
	 * @param point
	 * @return
	 */
	public static <D> IndexDocumentFieldID fieldIdOf(final IndexableFieldID fieldId,
								   		  		 	 final D point) {
		if (point == null) throw new IllegalArgumentException("Language cannot be null to compose a language dependent index document's field id for " + fieldId.asString());
		return IndexDocumentFieldID.forId(Throwables.message("{}.{}",fieldId.asString(),point.toString()));
	}
	/**
	 * Returns the {@link IndexDocumentFieldID} for a language IN-dependent field
	 * @param fieldName
	 * @return
	 */
	public static IndexDocumentFieldID fieldIdOf(final IndexableFieldID fieldId) {
		return IndexDocumentFieldID.forId(fieldId.asString());
	}
	/**
	 * Gets the {@link Language} from the language dependent field id (as {fieldBaseName}.{language})
	 * @param langDependantFieldNamePattern
	 * @param fieldName
	 * @return
	 */
	public static String dynamicDimensionPointFromFieldId(final IndexDocumentFieldID fieldId) {
		String outPoint = null;
		String[] fieldIdSplitted = fieldId.asString().split("\\.");
		if (fieldIdSplitted.length > 1) {
			outPoint = fieldIdSplitted[fieldIdSplitted.length-1];
		} else {
			throw new IllegalArgumentException(Throwables.message("The field with id={} does NOT match the [fieldId].[dynamicPoint] pattern. It's NOT a dynamic dimension dependent field!!!",
																  fieldId));
		}
		return outPoint;
	}
	/**
	 * Gets a regex {@link Pattern} that matches all the language fields 
	 * @param baseName
	 * @return
	 */
	public static final Pattern dynamicDimensionDependantFieldNamePattern(final IndexableFieldID fieldId) { 
		return Pattern.compile(fieldId + "\\.(.+)");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method finds the {@link FieldMetaDataID} from the {@link IndexDocumentFieldID} 
	 * Beware that the {@link IndexDocumentFieldID} might NOT be the same as the {@link FieldMetaDataID} for fields
	 * with multiple "dimensions" like multi-language {@link Summary} fields where each language {@link Summary}
	 * is stored in a separate field with an id like: metaDataId.{dimensionId}
	 * For example, a multi-language {@link Summary} field with {@link FieldMetaDataID}=r01.summary
	 * is stored in multiple lucene fields, one for each document: r01.summary.es, r01.summary.eu, etc
	 * 
	 * @param modelObjectMetaData
	 * @param fieldId
	 * @return
	 */
	public static FieldMetaDataID findMetaDataId(final ModelObjectTypeMetaData modelObjectMetaData,
									   		final IndexDocumentFieldID indexFieldId) {
		FieldMetaDataID outMetaDataId = null;
		String[] indexFieldIdSplitted = indexFieldId.asString().split("\\.");
		int i = indexFieldIdSplitted.length;
		boolean found = false;
		do {
			// Compose a possible metaDataId
			StringBuilder metaDataIdAsString = new StringBuilder(indexFieldId.asString().length());
			for (int j=0; j<i; j++) {
				metaDataIdAsString.append(indexFieldIdSplitted[j]);
				if (j < i-1) metaDataIdAsString.append(".");
			}
			// Try to find a metaData with the composed id
			FieldMetaDataID metaDataId = FieldMetaDataID.forId(metaDataIdAsString.toString());
			FieldMetaData metaData = modelObjectMetaData.getFieldMetaDataFor(metaDataId);			
			if (metaData != null) {
				outMetaDataId = metaDataId;
				found = true;
			}
			i = i-1;
		} while (!found && i >= 0);
		return outMetaDataId;
	}
}
