package r01f.model.facets;

import r01f.locale.Language;

/**
 * Interface for language dependant model objects
 */
public interface HasLanguage 
	     extends ModelObjectFacet {
	/**
	 * Gets the language
	 * @return
	 */
	public Language getLanguage();
	/**
	 * Sets the language
	 * @param lang
	 */
	public void setLanguage(final Language lang);
}
