package r01f.model.metadata;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.exceptions.Throwables;
import r01f.locale.LanguageTexts;
import r01f.reflection.ReflectionUtils;
import r01f.types.summary.LangDependentSummary;
import r01f.types.summary.LangIndependentSummary;
import r01f.types.summary.Summary;

@XmlRootElement(name="metaDataConfigForSummaryField")
public class FieldMetaDataForSummary
	 extends FieldMetaDataConfigBase {

	private static final long serialVersionUID = -7719936429450922842L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FieldMetaDataForSummary() {
		super();
	}
	@SuppressWarnings("unchecked")
	public FieldMetaDataForSummary(final FieldMetaDataForSummary other) {
		this(other.getFieldId(),
			 other.getName(),other.getDescription(),
			 other.getSearchEngineIndexingConfig(),
			 (Class<? extends Summary>)other.getDataType());
	}
	public FieldMetaDataForSummary(final FieldMetaDataID fieldId,
								   final LanguageTexts name,final LanguageTexts description,
								   final FieldMetaDataSearchEngineIndexingConfig searchEngineIndexingConfig,
								   final Class<? extends Summary> summaryType) {
		super(fieldId,
			  name,description,
			  searchEngineIndexingConfig,
			  summaryType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isForLangDependentSummary() {
		return ReflectionUtils.isImplementing(_dataType,LangDependentSummary.class);
	}
	public boolean isForLangIndependentSummary() {
		return ReflectionUtils.isImplementing(_dataType,LangIndependentSummary.class);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void checkIfIsAcceptableValueOrThrow(final Object value) {
		boolean acceptable = value instanceof Summary && ReflectionUtils.isSubClassOf(value.getClass(),_dataType);
		if (!acceptable) throw new IllegalArgumentException(Throwables.message("The metaData {} is NOT defined as a {} FIELD (the provided value it's a {} type)",
																			   _fieldId,_dataType.getSimpleName(),value.getClass())); 
	}
}
