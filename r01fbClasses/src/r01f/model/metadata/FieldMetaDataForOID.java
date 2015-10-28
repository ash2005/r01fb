package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.locale.LanguageTexts;
import r01f.reflection.ReflectionUtils;

@XmlRootElement(name="metaDataConfigForOIDField")
public class FieldMetaDataForOID
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 8974728672923211198L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForOID() {
		super();
	}
	@SuppressWarnings("unchecked")
	public FieldMetaDataForOID(final FieldMetaDataForOID other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig(),			 
			 (Class<? extends OID>)other.getDataType());
	}
	public FieldMetaDataForOID(final FieldMetaDataID fieldId,
							   final LanguageTexts name,final LanguageTexts description,
							   final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
							   final Class<? extends OID> oidType) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  oidType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof OID && ReflectionUtils.isSameClassAs(value.getClass(),_dataType);
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,_dataType.getSimpleName(),value.getClass())); 
	}
}
