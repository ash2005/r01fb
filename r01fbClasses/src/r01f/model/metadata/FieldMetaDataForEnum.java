package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;
import r01f.reflection.ReflectionUtils;

@XmlRootElement(name="metaDataConfigForEnumField")
public class FieldMetaDataForEnum
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = -9030568267462067246L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForEnum() {
		super();
	}
	@SuppressWarnings("unchecked")
	public FieldMetaDataForEnum(final FieldMetaDataForEnum other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig(),
			 (Class<? extends Enum<?>>)other.getDataType());
	}
	public FieldMetaDataForEnum(final FieldMetaDataID fieldId,
							    final LanguageTexts name,final LanguageTexts description,
							    final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
								final Class<? extends Enum<?>> enumType) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  enumType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Enum && ReflectionUtils.isSameClassAs(value.getClass(),_dataType);
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,_dataType.getSimpleName(),value.getClass())); 
	}
}
