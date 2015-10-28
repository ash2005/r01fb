package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;

@XmlRootElement(name="metaDataConfigForDoubleField")
public class FieldMetaDataForDouble
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = -7685424551771660474L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForDouble() {
		super();
	}
	public FieldMetaDataForDouble(final FieldMetaDataForDouble other) {
		this(other.getFieldId(),			 
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForDouble(final FieldMetaDataID fieldId,
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
		boolean acceptable = value instanceof Double;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Double.class.getSimpleName(),value.getClass())); 
	}
}
