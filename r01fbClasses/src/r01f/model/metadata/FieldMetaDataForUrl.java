package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;
import r01f.types.url.Url;

@XmlRootElement(name="metaDataConfigForURLField")
public class FieldMetaDataForUrl
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = 6480065284988093130L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForUrl() {
		super();
	}
	public FieldMetaDataForUrl(final FieldMetaDataForUrl other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig());
	}
	public FieldMetaDataForUrl(final FieldMetaDataID fieldId,
							   final LanguageTexts name,final LanguageTexts description,
							   final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  Url.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Url;
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,Url.class.getSimpleName(),value.getClass())); 
	}
}
