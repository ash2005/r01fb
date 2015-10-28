package r01f.model.facets.delegates;

import java.util.Collection;

import r01f.locale.Language;
import r01f.model.facets.LangDependentNamed;
import r01f.model.facets.LangDependentNamed.HasLangDependentNamedFacet;

/**
 * Encapsulates the {@link LangDependentNamed} behavior
 * @param <SELF_TYPE>
 */
public class LangDependentNamedDelegate<SELF_TYPE extends HasLangDependentNamedFacet>
	 extends FacetDelegateBase<SELF_TYPE>
  implements LangDependentNamed {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public LangDependentNamedDelegate(final SELF_TYPE hasLangDependentNameFacet) {
		super(hasLangDependentNameFacet);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getNameIn(final Language lang) {
		return _modelObject.getNamesByLanguage() != null ? _modelObject.getNamesByLanguage()
																	   .get(lang)
														  : null;
	}
	@Override
	public void setNameIn(final Language lang,final String name) {
		if (_modelObject.getNamesByLanguage() == null) _modelObject.ensureNamesByLanguageContainerIsNOTNull();	// ensure there's a LanguageTexts container
		_modelObject.getNamesByLanguage()
					.add(lang,name);
	}
	@Override
	public Collection<Language> getAvailableLanguages() {
		return  _modelObject.getNamesByLanguage() != null ? _modelObject.getNamesByLanguage()
																	   .getDefinedLanguages()
														  : null;
	}
}
