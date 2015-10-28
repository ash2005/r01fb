package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForLanguageField")
public class FieldMetaDataForLanguage
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 6603639636128569648L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForLanguage() {
		super();
	}
	public FieldMetaDataForLanguage(final FieldMetaDataForLanguage other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForLanguage(final FieldMetaDataID fieldId,
									final LanguageTexts name,final LanguageTexts description,
									final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  Language.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Language;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Language.class.getSimpleName(),value.getClass())); 
	}
}
