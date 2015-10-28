package r01f.model.metadata;

import java.sql.Date;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForBooleanField")
public class FieldMetaDataForBoolean
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 4385767083375845907L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForBoolean() {
		super();
	}
	public FieldMetaDataForBoolean(final FieldMetaDataForBoolean other) {
		this(other.getFieldId(),				
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForBoolean(final FieldMetaDataID fieldId,
								   final LanguageTexts name,final LanguageTexts description,
								   final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  Date.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Boolean;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Boolean.class.getSimpleName(),value.getClass())); 
	}
}
