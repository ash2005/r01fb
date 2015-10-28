package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;
import r01f.reflection.ReflectionUtils;
import r01f.types.CanBeRepresentedAsString;

@XmlRootElement(name="metaDataConfigForStringField")
public class FieldMetaDataForString
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 6480065284988093130L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForString() {
		super();
	}
	public FieldMetaDataForString(final FieldMetaDataForString other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
		if (other.getDataType() != String.class) _dataType = other.getDataType();
	}
	public FieldMetaDataForString(final FieldMetaDataID fieldId,
								  final LanguageTexts name,final LanguageTexts description,
								  final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  String.class);
	}
	public FieldMetaDataForString(final FieldMetaDataID fieldId,
								  final LanguageTexts name,final LanguageTexts description,
								  final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
								  final Class<? extends CanBeRepresentedAsString> stringType) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  stringType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof String || ReflectionUtils.isImplementing(value.getClass(),CanBeRepresentedAsString.class);
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} or {} FIELD (the provided value it's a {} type)",
																			   _fieldId,String.class.getSimpleName(),CanBeRepresentedAsString.class.getSimpleName(),value.getClass())); 
	}
}
