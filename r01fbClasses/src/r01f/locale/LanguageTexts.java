package r01f.locale;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * Models a collection of texts in different languages
 */
public interface LanguageTexts
         extends Serializable {

/////////////////////////////////////////////////////////////////////////////////////////
//  Behavior when a text in a language is not found
/////////////////////////////////////////////////////////////////////////////////////////	
	public enum LangTextNotFoundBehabior {
		RETURN_NULL,
		RETURN_DEFAULT_VALUE,
		THROW_EXCEPTION;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the behavior of the {@link LanguageTexts} collection when a required language text
	 * is NOT found
	 * @param behavior
	 */
	public void setLangTextNotFoundBehabior(final LangTextNotFoundBehabior behavior);
	/**
	 * Gets the behavior of the {@link LanguageTexts} collection when a required language text
	 * is NOT found
	 * @return
	 */
	public LangTextNotFoundBehabior getLangTextNotFoundBehabior();
	/**
	 * If the {@link LangTextNotFoundBehabior} is RETURN_DEFAULT_VALUE this value is returned
	 * @return the default value
	 */
	public String getDefaultValue();
	/**
	 * If the {@link LangTextNotFoundBehabior} is RETURN_DEFAULT_VALUE this value is returned
	 * @param defaultValue
	 */
	public void setDefaultValue(final String defaultValue);
	/**
	 * Sets a text in a language
	 * @param lang
	 * @param text
	 */
	public void add(final Language lang,final String text);
	/**
	 * A add() equivalent method
	 * @param lang
	 * @param text
	 */
	public void set(final Language lang,final String text);
	/**
	 * Returns a text in a language
	 * @param lang the language
	 * @return the text in the provided language
	 */
	public String get(Language lang);
	/**
	 * Returns a text in a language
	 * @param lang the language
	 * @return the text in the provided language
	 */
	public String getFor(Language lang);
	/**
	 * Returns the text in the system's default language
	 * @return
	 */
	public String getForSystemDefaultLanguage();
	/**
	 * Returns a text in any (random) language
	 * @return
	 */
	public String getAny();
	/**
	 * Returns true if some text is defined for the lang
	 * @param lang the language
	 * @return false is NO text is defined for the lang; false otherwise
	 */
	public boolean isTextDefinedFor(Language lang);
	/**
	 * @return the {@link Set} of {@link Language} that have some text associated with
	 */
	public Set<Language> getDefinedLanguages();
	/**
	 * @return a {@link Map} indexed by {@link Language} with the texts
	 */
	public Map<Language,String> asMap();
}
