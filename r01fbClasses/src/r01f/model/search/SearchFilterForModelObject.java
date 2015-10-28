package r01f.model.search;

import java.util.Collection;

import r01f.guids.OID;
import r01f.locale.Language;
import r01f.model.ModelObject;


/**
 * Interface for Search filters
 */
public interface SearchFilterForModelObject 
	     extends SearchFilter {
/////////////////////////////////////////////////////////////////////////////////////////
//  UILanguage
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the language of the user interface 
	 * @return
	 */
	public Language getUILanguage();
	/**
	 * Sets the language of the user interface
	 * @param uiLang
	 */
	public void setUILanguage(final Language uiLang);
/////////////////////////////////////////////////////////////////////////////////////////
//  FILTERED MODEL OBJECT TYPES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the model object types to be filtered
	 */
	public Collection<Class<? extends ModelObject>> getFilteredModelObjectTypes();
	/**
	 * Sets the model object types to be filtered
	 * @param modelObjectType
	 */
	public void setModelObjectTypesToBeFiltered(final Collection<Class<? extends ModelObject>> modelObjectTypes);
/////////////////////////////////////////////////////////////////////////////////////////
//  OID
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * If retrieving a specific a record from the search index
	 * (any other filter condition is ignored if oid is present) 
	 * @return
	 */
	public <O extends OID> O getOid();
	/**
	 * If retrieving a specific a record from the search index
	 * (any other filter condition is ignored if oid is present)
	 * @param oid  
	 */
	public <O extends OID> void setOid(final O oid);
/////////////////////////////////////////////////////////////////////////////////////////
//  TEXT
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if a text filter clause was set
	 */
	public boolean hasTextFilter();
	/**
	 * Gets the full text search words to be searched
	 * @return
	 */
	public String getText();
	/**
	 * Gets the language the full text search words are in
	 * (only if the searched model objects are language-dependent)
	 * @return
	 */
	public Language getTextLanguage();
	/**
	 * Sets the full text search words to be searched asumming
	 * that the searched model objects are language independent or the language is pre-fixed
	 * @param text
	 * @param lang
	 */
	public void setText(final String text);
	/**
	 * Sets the full text search words to be searched and the language
	 * these words are in (the model objects are language dependent)
	 * @param text
	 * @param lang
	 */
	public void setText(final String text,
						final Language lang);
/////////////////////////////////////////////////////////////////////////////////////////
//  LANGUAGE
/////////////////////////////////////////////////////////////////////////////////////////
//	/**
//	 * Gets the language of the items to be returned
//	 * @return
//	 */
//	public Language getLanguage();
//	/**
//	 * Sets the language of the items to be returned
//	 * @param lang
//	 */
//	public void setLanguage(final Language lang);
}
