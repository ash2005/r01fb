package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForLongField")
public class FieldMetaDataForLong
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = -6051852909743764059L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForLong() {
		super();
	}
	public FieldMetaDataForLong(final FieldMetaDataForLong other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForLong(final FieldMetaDataID fieldId,
							    final LanguageTexts name,final LanguageTexts description,
							    final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  Long.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Long;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Long.class.getSimpleName(),value.getClass())); 
	}
}
