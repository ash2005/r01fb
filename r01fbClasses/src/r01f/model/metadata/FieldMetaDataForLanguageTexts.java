package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForLanguageTextsField")
public class FieldMetaDataForLanguageTexts
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = -250940472843050277L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForLanguageTexts() {
		super();
	}
	public FieldMetaDataForLanguageTexts(final FieldMetaDataForLanguageTexts other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForLanguageTexts(final FieldMetaDataID fieldId,
								   		 final LanguageTexts name,final LanguageTexts description,
								   		 final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  LanguageTexts.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof LanguageTexts;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,_dataType.getSimpleName(),value.getClass())); 
	}
}
