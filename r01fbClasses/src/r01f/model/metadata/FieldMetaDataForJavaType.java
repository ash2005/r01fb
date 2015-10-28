package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForJavaTypeField")
public class FieldMetaDataForJavaType
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = -7396870074131906003L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForJavaType() {
		super();
	}
	public FieldMetaDataForJavaType(final FieldMetaDataForJavaType other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig(),
			 other.getDataType());
	}
	public FieldMetaDataForJavaType(final FieldMetaDataID fieldId,
							   		final LanguageTexts name,final LanguageTexts description,
							   		final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
							   		final Class<?> type) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  type);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Class;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Class.class.getSimpleName(),value.getClass())); 
	}
}
