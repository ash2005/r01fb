package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForFloatField")
public class FieldMetaDataForFloat
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 2346548292276365808L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForFloat() {
		super();
	}
	public FieldMetaDataForFloat(final FieldMetaDataForFloat other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForFloat(final FieldMetaDataID fieldId,
								 final LanguageTexts name,final LanguageTexts description,
								 final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  Float.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Float;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Float.class.getSimpleName(),value.getClass())); 
	}
}
