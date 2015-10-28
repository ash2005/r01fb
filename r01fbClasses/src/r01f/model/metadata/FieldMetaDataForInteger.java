package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForIntegerField")
public class FieldMetaDataForInteger
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 8410808480524544300L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForInteger() {
		super();
	}
	public FieldMetaDataForInteger(final FieldMetaDataForInteger other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForInteger(final FieldMetaDataID fieldId,
								   final LanguageTexts name,final LanguageTexts description,
								   final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  Integer.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Integer;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Integer.class.getSimpleName(),value.getClass())); 
	}
}
