package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;
import r01f.reflection.ReflectionUtils;
import r01f.types.IsPath;

@XmlRootElement(name="metaDataConfigForPathField")
public class FieldMetaDataForPath
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 929667993308526216L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForPath() {
		super();
	}
	@SuppressWarnings("unchecked")
	public FieldMetaDataForPath(final FieldMetaDataForPath other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig(),
			 (Class<? extends IsPath>)other.getDataType());
	}
	public FieldMetaDataForPath(final FieldMetaDataID fieldId,
							    final LanguageTexts name,final LanguageTexts description,
							    final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
								final Class<? extends IsPath> pathType) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  pathType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof IsPath && ReflectionUtils.isSameClassAs(value.getClass(),_dataType);
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,_dataType.getSimpleName(),value.getClass())); 
	}
}
