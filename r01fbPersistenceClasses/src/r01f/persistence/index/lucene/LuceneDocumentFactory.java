package r01f.persistence.index.lucene;


import java.io.StringReader;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.DoubleDocValuesField;
import org.apache.lucene.document.DoubleField;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.FloatDocValuesField;
import org.apache.lucene.document.FloatField;
import org.apache.lucene.document.IntField;
import org.apache.lucene.document.LongField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.persistence.index.IndexableFieldValuesExtractor;
import r01f.persistence.index.document.IndexDocumentFieldConfig;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.index.document.IndexDocumentFieldID;
import r01f.persistence.index.document.IndexDocumentFieldIndexOptions;
import r01f.persistence.index.document.IndexDocumentFieldType;
import r01f.persistence.index.document.IndexDocumentStandardFieldType;
import r01f.persistence.index.document.IndexDocumentValueFieldType;

import com.google.common.collect.Lists;

/**
 * Factory of a lucene's {@link Document} and its {@link Field}s from a {@link IndexDocumentFieldConfigSet}
 * that defines each field's name, whether it's value is stored, indexed, etc
 * @see IndexDocumentFieldConfigSet
 * 
 * The normal usage is:
 * <ol>
 * 		<li>Provide a {@link IndexDocumentFieldConfigSet}.
 * 			<pre class='brush:java'>
 * 				LuceneDocumentFactory fieldsFactory = LuceneDocumentFactory.of(fieldsConfigSet);	
 * 			</pre>
 * 		</li>
 * 		<li>Extract the field's values using some {@link IndexableFieldValuesExtractor}
 * 			<pre class='brush:java'> 
 *				IndexableFieldValuesExtractor<ModelObjectType> indexableFieldValuesExtractor = _indexableFieldValuesExtractorProvider.get();
 *				IndexableFieldValueSet indexableFieldsValues = indexableFieldValuesExtractor.extractFields(userContext,
 *										 			 													   modelObject);
 *			</pre>
 * 		</li>
 * 		<li>Create a template for each of the extracted fields using {@link LuceneDocumentFactory}
 * 			previously created from the {@link IndexDocumentFieldConfigSet}
 * 			<pre class='brush:java'>
 * 				if (indexableFieldsValues.hasData()) {
 * 					for (IndexableFieldValue<?> fieldValue : fieldsValues) {
 * 						_createField(fieldsFactory,		// the previously created fields factory
 *								 	 fieldValue);		// the previously extracted fields values
 * 					}
 * 				}	
 * 			</pre>
 * 		</li>
 * 		<li>Create the document
 * 			<pre class='brush:java'>
 * 				Document doc = fieldsFactory.createDocument();	
 * 			</pre>
 * 		</li>
 * </ol>
 * The _createField() method is where the lucene's {@link Field} is created from the {@link IndexDocumentFieldConfig} using 
 * the {@link LuceneDocumentFactory} like:
 * <pre class='brush:java'>
 *		private <T> void _createField(final LuceneDocumentFactory fieldsFactory,
 *								  	  final IndexableFieldValue<T> indexableField) {
 *			IndexableFieldId fieldId = indexableField.getId();
 *			T fieldValue = indexableField.getValue();
 *			
 *			if (fieldId.is(IndexDocumentFieldConfigSetBase.LUCENE_ID_FIELD)) {
 *				fieldsFactory.createField(luceneFieldId)
 *					 		 .setStringValue(indexableField.getValue());
 *			} else if (fieldValue instanceof OID) {
 *				fieldsFactory.createField(luceneFieldId)
 *					 		 .setStringValue(indexableField.getValue()
 *														   .asString());	// it's an oid
 *			} else if (fieldValue instanceof String) {
 *				fieldsFactory.createField(luceneFieldId)
 *					 		 .setStringValue(indexableField.getValue());
 *			} else {
 *				...
 *			}
 *		}
 * </pre>
 * @see LuceneIndexerBase
 * 
 * IMPORTANT: This type is NOT thread safe!!!!!!!!!!!!!!!!!!
 */
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
class LuceneDocumentFactory {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * index document field's config
	 */
	private final IndexDocumentFieldConfigSet<? extends IndexableModelObject<? extends OID>> _fieldsConfigSet;
	/**
	 * The created {@link Field}s 
	 */
	private Collection<Field> _createdFields = null;
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static LuceneDocumentFactory of(final IndexDocumentFieldConfigSet<? extends IndexableModelObject<? extends OID>> cfgSet) {
		return new LuceneDocumentFactory(cfgSet);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Lazily creates a field
	 * @param fieldId
	 * @return
	 */
	public Field createField(final IndexDocumentFieldID fieldId) {
		IndexDocumentFieldConfig<?> fieldCfg = _fieldsConfigSet.of(fieldId);
		if (fieldCfg == null) throw new IllegalStateException(Throwables.message("The Lucene field config for field '{}' was NOT found at the config set",
																				 fieldId));
		
		Field field = LuceneDocumentFactory.createField(fieldCfg);
		if (field != null) {
			if (_createdFields == null) _createdFields = Lists.newArrayList();
			_createdFields.add(field);
		}
		return field;
	}
	/**
	 * Returns a Lucene {@link Field} for the provided field based on the {@link IndexDocumentFieldConfig}
	 * but with certain boosting value that overrides the boosting value of the {@link IndexDocumentFieldConfig}
	 * @param fieldId
	 * @param boost
	 * @return
	 */
	public Field createField(final IndexDocumentFieldID fieldId,
							 final float boost) {
		Field outField = this.createField(fieldId);
		outField.setBoost(boost);
		return outField;
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the created fields after calling createField method for every {@link IndexDocumentFieldConfig} at the {@link IndexDocumentFieldConfigSet}
	 */
	public Collection<Field> getCreatedFields() {
		return _createdFields;
	}
	public Document createDocument() {
		// [2] - Put the fields values into a document
		Document outDocument = new Document();
		for (Field field : this.getCreatedFields()) {
			outDocument.add(field);
		}
		return outDocument;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Template: static methods
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public static <FIELD_TYPE extends IndexDocumentFieldType,
				   FIELD extends Field> FIELD createField(final IndexDocumentFieldConfig<FIELD_TYPE> fieldCfg) {		
		FIELD outField = null;
		if (fieldCfg.getType() instanceof IndexDocumentValueFieldType) {
			IndexDocumentFieldConfig<IndexDocumentValueFieldType> fieldDocValueCfg = (IndexDocumentFieldConfig<IndexDocumentValueFieldType>)fieldCfg;
			outField = LuceneDocumentFactory.<FIELD>createDocValueFieldTemplate(fieldDocValueCfg);
		} 
		else if (fieldCfg.getType() instanceof IndexDocumentStandardFieldType) {
			IndexDocumentFieldConfig<IndexDocumentStandardFieldType> fieldDocValueCfg = (IndexDocumentFieldConfig<IndexDocumentStandardFieldType>)fieldCfg;
			outField = LuceneDocumentFactory.<FIELD>createStandardFieldTemplate(fieldDocValueCfg);
		}
		// Boost
		if (fieldCfg.isIndexed()) {
			if (outField != null) outField.setBoost(fieldCfg.getIndexOptions().getBoost());
		}
		
		return outField;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DocValueFields
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a DocValue field template for a NON-language dependent type
	 * @param fieldCfg
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <FIELD extends Field> FIELD createDocValueFieldTemplate(final IndexDocumentFieldConfig<IndexDocumentValueFieldType> fieldCfg) {
		if (fieldCfg == null) return null;

		IndexDocumentFieldID fieldId = fieldCfg.getId();
		FIELD outField = null;
		switch(fieldCfg.getType()) {
		case Double:
			outField = (FIELD)(new DoubleDocValuesField(fieldId.asString(),0D));
			break;
		case Float:
			outField = (FIELD)(new FloatDocValuesField(fieldId.asString(),0F));
			break;
		default:
			throw new IllegalArgumentException(fieldCfg.getType() + "is NOT a supported type");
		}
		return outField;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  StandardFields
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a NON-language dependent field template
	 * @param fieldCfg
	 * @return
	 */
	public static <FIELD extends Field> FIELD createStandardFieldTemplate(final IndexDocumentFieldConfig<IndexDocumentStandardFieldType> fieldCfg) {
		if (fieldCfg == null) return null;		
		
		IndexDocumentFieldID fieldId = fieldCfg.getId();
		FieldType fieldType = _fieldTypeFrom(fieldCfg.getType(),
										     fieldCfg.isStored(),
											 fieldCfg.getIndexOptions());
		FIELD outField = LuceneDocumentFactory.<FIELD>_createStandardField(fieldId,
											  							   fieldCfg.getType(),
											  							   fieldType);
		return outField;
	}
	private static FieldType _fieldTypeFrom(final IndexDocumentStandardFieldType fieldType,
											final boolean stored,
											final IndexDocumentFieldIndexOptions options) {
		FieldType outFieldType = new FieldType();
		
		// [1] - Set if the field is stored or not
		outFieldType.setStored(stored);
		
		// [2] - Set the options: indexed, tokenized, etc
		if (options != null) {
			outFieldType.setIndexed(true);
			outFieldType.setOmitNorms(options.isOmitingNorms());
			outFieldType.setTokenized(options.isTokenized());
			if (options.getTermVectorsStoring() != null) {
				outFieldType.setStoreTermVectors(options.getTermVectorsStoring().isEnabled());
				if (options.getTermVectorsStoring().isEnabled()) {
					outFieldType.setStoreTermVectorOffsets(options.getTermVectorsStoring().isIncludeTermOffsets());
					outFieldType.setStoreTermVectorPositions(options.getTermVectorsStoring().isIncludeTermPositions());
					outFieldType.setStoreTermVectorPayloads(options.getTermVectorsStoring().isIncludeTermPayLoads());
				}
			}
		}
		// [3] - Set the lucene's FieldType's numericType
		switch(fieldType) {
		case Double:
			outFieldType.setNumericType(FieldType.NumericType.DOUBLE);
			break;
		case Float:
			outFieldType.setNumericType(FieldType.NumericType.FLOAT);
			break;
		case Int:
			outFieldType.setNumericType(FieldType.NumericType.INT);
			break;
		case Long:
			outFieldType.setNumericType(FieldType.NumericType.LONG);
			break;
		case String:
			// nothing
			break;
		case Text:
			// nothing
			break;
		default:
			throw new IllegalArgumentException(fieldType + "is NOT a supported type");
		}
		return outFieldType;
	}
	@SuppressWarnings("unchecked")
	private static <FIELD extends Field> FIELD _createStandardField(final IndexDocumentFieldID fieldId,
																    final IndexDocumentStandardFieldType type,
																    final FieldType fieldType) {
		// guess the store condition from the fieldType
		Field.Store store = fieldType != null ? fieldType.stored() ? Field.Store.YES	
											   					   : Field.Store.NO
											  : null;
		// Create the field by means of
		//		- store: the field has to be stored or not 
		//		- fieldType: the field type
		FIELD outField = null;
		switch(type) {
		case Double:
			outField = fieldType == null ? (FIELD)(new DoubleField(fieldId.asString(),0D,store))
										 : (FIELD)(new DoubleField(fieldId.asString(),0D,fieldType));
			break;
		case Int:
			outField = fieldType == null ? (FIELD)(new IntField(fieldId.asString(),0,store))
										 : (FIELD)(new IntField(fieldId.asString(),0,fieldType));
			break;
		case Long:
			outField = fieldType == null ? (FIELD)(new LongField(fieldId.asString(),0L,store))
										 : (FIELD)(new LongField(fieldId.asString(),0L,fieldType));
			break;
		case Float:
			outField = fieldType == null ? (FIELD)(new FloatField(fieldId.asString(),0F,store))
										 : (FIELD)(new FloatField(fieldId.asString(),0F,fieldType));
			break;
		case String:
			outField = fieldType == null ? (FIELD)(new StringField(fieldId.asString(),"",store))
										 : (FIELD)(new Field(fieldId.asString(),"",fieldType));
			break;
		case Text:
			outField = fieldType == null ? (FIELD)(new TextField(fieldId.asString(),new StringReader("")))
										 : (FIELD)(new Field(fieldId.asString(),new StringReader(""),fieldType));
			break;
		default:
			throw new IllegalArgumentException(fieldType + "is NOT a supported type");
		}
		return outField;
	}
}
