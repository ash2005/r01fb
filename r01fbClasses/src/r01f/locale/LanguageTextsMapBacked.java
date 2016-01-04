package r01f.locale;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.util.types.collections.CollectionUtils;


/**
 * Collection of various language texts backed-up by a Map
 * <pre class='brush:java'>
 *	LanguageTexts text = LanguageTextsFactory.createMapBacked()
 *									         .addForLang(Language.BASQUE,"testu1")
 *									         .addForLang(Language.ENGLISH,"text1");
 *	String text_in_spanish = text.getFor(Language.SPANISH);
 * </pre>
 */
@ConvertToDirtyStateTrackable
//@XmlWriteTransformer(using=TextByLanguageMapBackedXmlWriteTransformer.class) 
//@XmlReadTransformer(using=TextByLanguageMapBackedXmlReadTransformer.class)
@Accessors(prefix="_")
public class LanguageTextsMapBacked 
     extends LanguageTextsBase<LanguageTextsMapBacked> 
  implements Map<Language,String> {
	
	private static final long serialVersionUID = -3302253934368756020L;
/////////////////////////////////////////////////////////////////////////////////////////
//	STATE
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlTransient
	@Getter(AccessLevel.PRIVATE) private Map<Language,String> _backEndTextsMap;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public LanguageTextsMapBacked() {
		super(LangTextNotFoundBehabior.THROW_EXCEPTION,
			  null);
		_backEndTextsMap = new LinkedHashMap<Language,String>();
	}
	public LanguageTextsMapBacked(final LangTextNotFoundBehabior langTextNotFoundBehabior,final String defaultValue) {
		super(langTextNotFoundBehabior,defaultValue);
	}
	public LanguageTextsMapBacked(final int size) {
		this();
		_backEndTextsMap = new LinkedHashMap<Language,String>(size);
	}
	public LanguageTextsMapBacked(final int size,final LangTextNotFoundBehabior notFoundBehabiour) {
		this(size);
		_langTextNotFoundBehabior = notFoundBehabiour;
	}
	public LanguageTextsMapBacked(final Map<Language,String> texts) {
		this();
		if (CollectionUtils.hasData(texts)) {
			_backEndTextsMap = new LinkedHashMap<Language,String>(texts);
		}
	}
	public LanguageTextsMapBacked(final LanguageTexts other) {
		super(other.getLangTextNotFoundBehabior(),
			  other.getDefaultValue());
		Set<Language> definedLangs = other.getDefinedLanguages();
		if (CollectionUtils.hasData(definedLangs)) {
			_backEndTextsMap = new LinkedHashMap<Language,String>(definedLangs.size());
			for (Language lang : definedLangs) {
				_backEndTextsMap.put(lang,
									 other.getFor(lang));
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static LanguageTextsMapBacked create() {
		return new LanguageTextsMapBacked(LangTextNotFoundBehabior.RETURN_NULL,null);
	}
	public static LanguageTextsMapBacked create(final LangTextNotFoundBehabior behavior,final String defaultValue) {
		LanguageTextsMapBacked outTexts = new LanguageTextsMapBacked(behavior,defaultValue);
		return outTexts;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Set<Language> getDefinedLanguages() {
		return CollectionUtils.hasData(_backEndTextsMap) ? _backEndTextsMap.keySet() 
														 : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Map interface
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int size() {
		return _backEndTextsMap.size();
	}
	@Override
	public boolean isEmpty() {
		return _backEndTextsMap.isEmpty();
	}
	@Override
	public boolean containsKey(Object key) {
		return _backEndTextsMap.containsKey(key);
	}
	@Override
	public boolean containsValue(Object value) {
		return _backEndTextsMap.containsValue(value);
	}
	@Override
	public String get(Object key) {
		return _backEndTextsMap.get(key);
	}
	@Override
	public String put(Language key,String value) {
		_put(key,value);
		return _backEndTextsMap.put(key,value);
	}
	@Override
	public String remove(Object key) {
		return _backEndTextsMap.remove(key);
	}
	@Override
	public void putAll(Map<? extends Language,? extends String> m) {
		if (CollectionUtils.hasData(m)) {
			for (Map.Entry<? extends Language,? extends String> me : m.entrySet()) {
				_put(me.getKey(),me.getValue());
			}
		}
	}
	@Override
	public void clear() {
		_backEndTextsMap.clear();
	}
	@Override
	public Set<Language> keySet() {
		return _backEndTextsMap.keySet();
	}
	@Override
	public Collection<String> values() {
		return _backEndTextsMap.values();
	}
	@Override
	public Set<Map.Entry<Language,String>> entrySet() {
		return _backEndTextsMap.entrySet();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ABSTRACT METHODS IMPL
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void _put(final Language lang,final String text) {
		if (_backEndTextsMap == null) _backEndTextsMap = Maps.newLinkedHashMap();
		_backEndTextsMap.put(lang,text);
	}
	@Override
	protected String _retrieve(final Language lang) {
		String outText = CollectionUtils.hasData(_backEndTextsMap) ? _backEndTextsMap.get(lang)
																   : null;
		return outText;
	}
	@Override
	public Map<Language,String> asMap() {
		return _backEndTextsMap;
	}
	/**
	 * @return true if there is some data
	 */
	public boolean hasData() {
		return CollectionUtils.hasData(_backEndTextsMap);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  XML<->java
/////////////////////////////////////////////////////////////////////////////////////////	
//	static class TextByLanguageMapBackedXmlWriteTransformer 
//	  implements SimpleMarshallerCustomXmlTransformers.XmlWriteCustomTransformer<LanguageTexts> {
//		@Override
//		public String xmlFromBean(final Object obj) {
//			StringBuilder outSb = null;
//			LanguageTexts textByLang = (LanguageTexts)obj;
//			Set<Language> langs = textByLang.getDefinedLanguages();
//			if (CollectionUtils.hasData(langs)) {
//				outSb = new StringBuilder(langs.size()*100);	// aprox del tamaño
////				outSb.append("<langTexts>");
//				for (Language lang : langs) {
//					outSb.append("<").append(Languages.getLocale(lang)).append("><![CDATA[");
//					outSb.append(textByLang.getFor(lang));
//					outSb.append("]]></").append(Languages.getLocale(lang)).append(">");
//				}
////				outSb.append("</langTexts>");
//			}
//			return outSb != null ? outSb.toString()	// "<langTexts><SPANISH>aaa</SPANISH></langTexts>";
//								 : null;
//		}
//	}
//	static class TextByLanguageMapBackedXmlReadTransformer 
//	  implements SimpleMarshallerCustomXmlTransformers.XmlReadCustomTransformer<LanguageTexts> {
//		@Override
//		public LanguageTexts beanFromXml(final CharSequence xml) {
//			LanguageTexts outTexts = null;
//			// Utilizar DOM para parsear
//			try {
//				DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance()
//																   .newDocumentBuilder();
//				@SuppressWarnings("resource")
//				@Cleanup Reader xmlReader = new CharSequenceReader(xml);
//				Document doc = docBuilder.parse(new InputSource(xmlReader));
//				NodeList langTextNodes = doc.getDocumentElement()
//											.getChildNodes();
//				if (langTextNodes != null && langTextNodes.getLength() > 0) {
//					outTexts = LanguageTextsFactory.createMapBacked();
//					for (int i=0; i < langTextNodes.getLength(); i++) {
//						Node langTextNode = langTextNodes.item(i);
//						String[] langAndCountry = langTextNode.getNodeName().split("_");
//						Language lang = Languages.of(langAndCountry[0],
//													 langAndCountry[1]);
//						String text = langTextNode.getTextContent();
//						outTexts.addForLang(lang,text);
//					}
//				}
//			} catch(Throwable th) {
//				// NO puede pasar...
//				th.printStackTrace(System.out);
//			}
//			return outTexts;
//		}
//	}


}
