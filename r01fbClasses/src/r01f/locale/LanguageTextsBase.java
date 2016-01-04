package r01f.locale;

import java.util.Iterator;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Text in different languages collection
 */
@Accessors(prefix="_")
public abstract class LanguageTextsBase<SELF_TYPE extends LanguageTextsBase<SELF_TYPE>> 
           implements LanguageTexts {
	
	private static final long serialVersionUID = -34749639584791088L;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Behavior when a required language text is NOT found 
	 */
	@XmlTransient
	@Getter @Setter protected transient LangTextNotFoundBehabior _langTextNotFoundBehabior = LangTextNotFoundBehabior.RETURN_NULL;
	/**
	 * Default value
	 */
	@XmlTransient
	@Getter @Setter protected transient String _defaultValue = "*** No text for {} ***";
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public LanguageTextsBase(final LangTextNotFoundBehabior langTextNotFoundBehabior,
							 final String defaultValue) {
		_langTextNotFoundBehabior = langTextNotFoundBehabior;
		if (Strings.isNOTNullOrEmpty(defaultValue)) _defaultValue = defaultValue;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Retrieves a text for the provided lang
	 * @param lang the lang
	 * @return the text associated with the lang
	 */
	protected abstract String _retrieve(final Language lang);
	/**
	 * Puts a text in a language
	 * @param lang the lang
	 * @param text the text associated with the lang
	 */
	protected abstract void _put(final Language lang,final String text);
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FLUENT API
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	public class TextInLangFluentAdapter {
		private final Language lang;
		public String get() {
			return _retrieve(lang);
		}
		/*
		 @SuppressWarnings("unchecked")
		public SELF_TYPE set(final String text) {
			if (!Strings.isNullOrEmpty(text)) _put(lang,text);
			return (SELF_TYPE)SELF_TYPE.this;
		}*/		
		
		@SuppressWarnings("unchecked")
		public SELF_TYPE set(final String text) {
			if (!Strings.isNullOrEmpty(text)) _put(lang,text);
			return (SELF_TYPE) LanguageTextsBase.this;
		}
		
	}
	/**
	 * Returns a fluent api adapter to get/set the text in a language
	 * This allows for fluent code like:
	 * <pre class='brush:java'>
	 * 		textByLang.in(Language.SPANISH).set("Hola")
	 * 				  .in(Langugae.BASQUE).set("Kaixo");
	 * 		String textInBasque = textByLang.in(Language.BASQUE).get();
	 * </pre>
	 * @param lang the lang
	 * @return the adapter
	 */
	public TextInLangFluentAdapter in(final Language lang) {
		return new TextInLangFluentAdapter(lang);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	GET
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void add(final Language lang,final String text) {
		_put(lang,text);
	}
	@Override
	public void set(final Language lang,final String text) {
		this.add(lang,text);
	}
	@Override
	public String get(final Language lang) {
		return this.getFor(lang);
	}
	@Override
	public String getFor(final Language lang) {
		String outText = _retrieve(lang);
		if (outText == null && _langTextNotFoundBehabior == LangTextNotFoundBehabior.RETURN_NULL) {
			/* outText is yet null */
		} else if (outText == null && _langTextNotFoundBehabior == LangTextNotFoundBehabior.RETURN_DEFAULT_VALUE) {
			outText = _defaultValue != null ? Strings.of(_defaultValue)
													 .customizeWith(lang)
													 .asString()
											: null;
		} else if (outText == null && _langTextNotFoundBehabior == LangTextNotFoundBehabior.THROW_EXCEPTION) {
			String msg = Strings.of(_defaultValue)
								.customizeWith(lang)
								.asString();
			throw new IllegalArgumentException(msg);
		}
		return outText;
	}	
	@Override
	public String getForSystemDefaultLanguage() {
		return this.getFor(Language.DEFAULT);
	}
	@Override 
	public String getAny() {
		Language anyLang = CollectionUtils.of(this.getDefinedLanguages())
										  .pickOneElement();
		return this.getFor(anyLang);
	}
	@Override
	public boolean isTextDefinedFor(final Language lang) {
		String text = this.getFor(lang);
		return Strings.isNOTNullOrEmpty(text);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  override
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		String outStr = null;
		Set<Language> langsWithText = this.getDefinedLanguages();
		if (CollectionUtils.hasData(langsWithText)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Iterator<Language> langIt = langsWithText.iterator(); langIt.hasNext(); ) {
				Language lang = langIt.next();
				String text = this.getFor(lang);
				sb.append(lang).append(":").append(text);
				if (langIt.hasNext()) sb.append(", ");
			}
			sb.append("]");
			outStr = sb.toString();
		} else {
			outStr = "NO data";
		}
		return outStr;
	}
}
