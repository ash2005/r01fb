package r01f.locale;

import java.util.Locale;

import r01f.exceptions.Throwables;
import r01f.util.types.Strings;

/**
 * Language locale
 * (this methods are NOT in the {@link Languages} type because GWT does NOT support
 *  {@link Locale})
 */
public class Languages {
/////////////////////////////////////////////////////////////////////////////////////////
//  http://www.w3.org/International/articles/language-tags/
//	http://download1.parallels.com/SiteBuilder/Windows/docs/3.2/en_US/sitebulder-3.2-win-sdk-localization-pack-creation-guide/30801.htm
/////////////////////////////////////////////////////////////////////////////////////////
	public static final Locale SPANISH = new Locale("es","ES");
	public static final Locale BASQUE = new Locale("eu","ES");
	public static final Locale ENGLISH = new Locale("en","EN");
	public static final Locale FRENCH = new Locale("fr","FR");
	public static final Locale DEUTCH = new Locale("de","DE");
	public static final Locale KOREAN = new Locale("ko","KR");
	public static final Locale POLISH = new Locale("pl","PL");
	public static final Locale SWEDISH = new Locale("sv","SE");
	public static final Locale HUNGARIAN = new Locale("hu","HU");
	public static final Locale CZECH = new Locale("cs","CZ");
	public static final Locale ROMANIAN = new Locale("ro","RO");
	public static final Locale JAPANESE = new Locale("ja","JP");
	public static final Locale RUSSIAN = new Locale("ru","RU"); 
	
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Builds a {@link Language} from the {@link Locale}
	 * @param loc
	 * @return
	 */
	public static Language of(final Locale loc) {
		Language outLang = null;
		for (Language lang : Language.values()) {
			if (lang == Language.ANY) continue;
			if (Languages.getLocale(lang).equals(loc)) {
				outLang = lang;
				break;
			}
		}
		return outLang;
	}
	/**
	 * Builds a {@link Language} from the language code (es, eu, en...)
	 * @param language
	 * @return
	 */
	public static Language of(final String language) {
		String theLang = language.toLowerCase().trim();
		Language outLang = null;
		for (Language lang : Language.values()) {
			if (lang == Language.ANY) continue;
			if (theLang.equals("cz")) theLang = "cs";	// bug: czech republic was incorrectly represented as cz BUT really it's cs-CZ  
			if (Languages.getLocale(lang).getLanguage().equals(theLang)) {
				outLang = lang;
				break;
			}
		}
		if (outLang == null 
		 && (theLang.equalsIgnoreCase("--") 
				|| theLang.equalsIgnoreCase("any") 
				|| theLang.equalsIgnoreCase("all"))) outLang = Language.ANY;
		return outLang;
	}
	/**
	 * Builds a {@link Language} from the language code (es, eu, en...) and the country code (ES, FR, EN...)
	 * @param language
	 * @param country
	 * @return
	 */
	public static Language of(final String language,
							  final String country) {
		String theLang = language.toLowerCase().trim();
		Language outLang = null;
		for (Language lang : Language.values()) {
			if (lang == Language.ANY) continue;
			
			Locale loc = Languages.getLocale(lang);
			if (loc.getLanguage().equals(theLang)
			 && loc.getCountry().equals(country)) {
				outLang = lang;
				break;
			}
		}
		if (outLang == null && (theLang.equals("--") || theLang.equals("any"))) outLang = Language.ANY;
		return outLang;
	}
	public static Language fromName(final String name) {
		return Language.fromName(name);
	}
	public static Language fromNameOrThrow(final String name) {
		Language outLang = Languages.fromName(name);
		if (outLang == null) throw new IllegalArgumentException(Throwables.message("{} is NOT a valid {}",name,Language.class));
		return outLang;
	}
	public static Language fromContentLangVersionFolder(final String folder) {
		return Languages.fromCountryCode(folder);
	}
	public static Language fromCountryCode(final String countryCode) {
		Language outLang = Languages.of(countryCode.substring(0,2));
		if (outLang == null 
		 && (countryCode.equalsIgnoreCase("--") 
				|| countryCode.equalsIgnoreCase("any") 
				|| countryCode.equalsIgnoreCase("all"))) outLang = Language.ANY;
		return outLang;
	}
	public static boolean canBe(final String name) {
		return Language.canBe(name);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a locale 
	 * @param lang
	 * @return
	 */
	public static Locale getLocale(final Language lang) {
		Locale outLocale = null;
		switch(lang) {
		case ANY:
			throw new IllegalStateException("unknown language");
		case BASQUE:
			outLocale = Languages.BASQUE;
			break;
		case DEUTCH:
			outLocale = Languages.DEUTCH;
			break;
		case ENGLISH:
			outLocale = Languages.ENGLISH;
			break;
		case FRENCH:
			outLocale = Languages.FRENCH;
			break;
		case SPANISH:
			outLocale = Languages.SPANISH;
			break;
		case KOREAN:
			outLocale = Languages.KOREAN;
			break;
		case POLISH:
			outLocale = Languages.POLISH;
			break;	
		case SWEDISH:
			outLocale = Languages.SWEDISH;
			break;	
		case HUNGARIAN:
			outLocale = Languages.HUNGARIAN;
			break;
		case CZECH:
			outLocale = Languages.CZECH;
			break;
		case ROMANIAN:
			outLocale = Languages.ROMANIAN;
			break;	
		case JAPANESE:
			outLocale = Languages.JAPANESE;
			break;	
		case RUSSIAN:
			outLocale = Languages.RUSSIAN;
			break;
		default:
			outLocale = Languages.getLocale(Language.DEFAULT);
			break;
		}
		return outLocale;
	}
	/**
	 * A var that represents a language
	 * @return
	 */
	public static String genericCountry() {
		return "%R01_LANG%";
	}
	/**
	 * @return the country
	 */
	public static String country(final Language lang) {
		String outCountry = null;
		if (lang == Language.ANY) {
			outCountry = Languages.genericCountry();
		} else {
			outCountry = Languages.getLocale(lang)
								  .getCountry();
		} 
		return outCountry;
	}
	/**
	 * @return the country
	 */
	public static String countryLowerCase(final Language lang) {
		return Languages.country(lang)
						.toLowerCase();
	}
	/**
	 * @return the language
	 */
	public static String language(final Language lang) {
		return Languages.getLocale(lang)
						.getLanguage();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a list of the countries
	 * @return ie: es,eu,en,fr,...
	 */
	public static String[] countries() {
		String[] outCountries = new String[Language.values().length-1];	// Language.UNKNOWN do not have locale
		int i=0;
		for (Language l : Language.values()) {
			if (l == Language.ANY) continue;
			if (Languages.getLocale(l) != null) outCountries[i++] = Languages.getLocale(l).getLanguage();
		}
		return outCountries;
	}
	/**
	 * Returns a regular expression to match the countries
	 * @param captureGroup if the regular expression conforms a capture group 
	 * @return (es|eu|en|fr...) if captureGroup=true and (?:es|eu|en|fr...) if captureGroup=false
	 */
	public static String countryMatchRegEx(final boolean captureGroup) {
		String[] countries = Languages.countries();
		StringBuilder sb = new StringBuilder(2 + countries.length*3);	// ( + 2 chars, the country and the separator)
		sb.append(captureGroup ? "(" : "(?:");
		for (int i=0; i<countries.length; i++) {
			sb.append(countries[i]);
			if (i < countries.length-1) sb.append("|");
		}
		sb.append(")");
		return sb.toString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Fakes a translation
	 * If the provided text is for example 'my text' and the language is ENGLISH, 
	 * the returned fake translation is '(en) my text'
	 * @param text
	 * @param lang
	 * @return
	 */
	public static String fakeTranslate(final String text,
									   final Language lang) {
		return Strings.create(text.length()+5)
					  .add("({}) ")
					  .add(text)
					  .customizeWith(Languages.country(lang))
					  .asString();
	}
}
