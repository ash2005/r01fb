package r01f.model.builders.facets;

import java.util.Map;

import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.model.facets.LangDependentNamed.HasLangDependentNamedFacet;


public class LanguageDependentNamedBuilder<CONTAINER_TYPE,
										   SELF_TYPE extends HasLangDependentNamedFacet> 
  	 extends FacetBuilderBase<CONTAINER_TYPE,SELF_TYPE> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public LanguageDependentNamedBuilder(final CONTAINER_TYPE parentType,
										 final SELF_TYPE hasLanguageDependentNamedFacet) {
		super(parentType,
			  hasLanguageDependentNamedFacet);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public LanguageDependentNamedBuilder<CONTAINER_TYPE,SELF_TYPE> withNameIn(final Language lang,final String name) {
		if (_modelObject.getNamesByLanguage() == null) _modelObject.ensureNamesByLanguageContainerIsNOTNull();
		_modelObject.getNamesByLanguage()
					.add(lang,name);
    	return this;
    }
    public CONTAINER_TYPE withNames(final LanguageTexts names) {
    	if (_modelObject.getNamesByLanguage() == null) _modelObject.ensureNamesByLanguageContainerIsNOTNull();
    	for (Language lang : names.getDefinedLanguages()) {
    		_modelObject.getNamesByLanguage()
    					.add(lang,names.get(lang));
    	}
    	return _parentType;
    }
    public CONTAINER_TYPE withNames(final Map<Language,String> names) {
    	if (_modelObject.getNamesByLanguage() == null) _modelObject.ensureNamesByLanguageContainerIsNOTNull();
    	for (Language lang : names.keySet()) {
    		_modelObject.getNamesByLanguage()
    					.add(lang,names.get(lang));
    	}
    	return _parentType;
    }
}
