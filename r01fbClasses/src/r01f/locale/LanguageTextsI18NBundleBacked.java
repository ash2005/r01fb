package r01f.locale;

import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.bundles.ResourceBundleControl;
import r01f.exceptions.Throwables;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertyLocation;

/**
 * Collection of various language texts backed-up by a ResourceBoundle
 * <pre class='brush:java'>
 * 	XMLProperties xmlProperties = new XMLProperties();	// IMPORTANT!! XMLProperties is usually a singleton managed by guice or an static var
 *	LanguageTexts text = LanguageTextsFactory.createBundleBacked()
 *											 .forBundle("myBundle")
 *											 .loadedAsDefinedAt(xmlProperties,
 *															    AppCode.forId("r01fb"),AppComponent.forId("test"),Path.of("/resourcesLoader[@id='myResourcesLoader']"))
 *											 .forKey("myMessageKey");
 *	String text_in_spanish = text.getFor(Language.SPANISH);
 * </pre>
 */
@Slf4j
@ConvertToDirtyStateTrackable
@XmlRootElement(name="langTextsFromBundle")
@Accessors(prefix="_")
public class LanguageTextsI18NBundleBacked 
     extends LanguageTextsBase<LanguageTextsI18NBundleBacked> {

	private static final long serialVersionUID = -5152862690732303091L;
/////////////////////////////////////////////////////////////////////////////////////////
//  NON-PERSISTENT STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlTransient
	private transient I18NService _service;
	
	@XmlTransient
	private transient I18NServiceBuilder _i18nServiceFactory;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	LanguageTextsI18NBundleBacked() {
		super(LangTextNotFoundBehabior.THROW_EXCEPTION,
			  null);
	}
	LanguageTextsI18NBundleBacked(final ResourceBundleControl resourceBundleControl) {
		this();
		_i18nServiceFactory = I18NServiceBuilder.create(resourceBundleControl);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlElement(name="resourcesLoaderDefLocationInProperties")
	@Getter @Setter private XMLPropertyLocation _resourcesLoaderDefLocationInProperties;
	
	@XmlElementWrapper(name="bundleChain") @XmlElement(name="link")
	@Getter @Setter private String[] _bundleChain;
	
	@XmlAttribute(name="messageKey")
	@Getter @Setter private String _messageKey;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * Sets the key for the texts
		 * @param messageKey
		 */
		public LanguageTexts forKey(final String messageKey) {
			_messageKey = messageKey;
			return this;
		}
/////////////////////////////////////////////////////////////////////////////////////////
//  ABSTRACT METHODS IMPL
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected void _put(final Language lang,final String text) {
		throw new UnsupportedOperationException(Throwables.message("{} does not support addition of language strings",LanguageTextsI18NBundleBacked.class.getName()));
	}	
	@Override
	protected String _retrieve(final Language lang) {
		if (Strings.isNullOrEmpty(_messageKey)) throw new IllegalStateException("The bundle message key has not been setted! Call forKey(messageKey) before!");
		if (_service == null) {
			String[] theBundleChain = CollectionUtils.isNullOrEmpty(_bundleChain) ? new String[] {"default"}
																			      : _bundleChain;
			_service = _i18nServiceFactory.forBundleChain(theBundleChain);
		}
		String outMessage = _service.forLanguage(lang)
									.message(_messageKey);
		return outMessage;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	OVERRIDEN METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Set<Language> getDefinedLanguages() {
		log.warn("is not possible to say beforehand the defined languages when {} is backed by an I18N resource bundle",
				 LanguageTexts.class.getName());
		return null;
	}
	@Override
	public Map<Language,String> asMap() {
		log.warn("is not possible to say beforehand the defined languages when {} is backed by an I18N resource bundle",
				 LanguageTexts.class.getName());
		return null;
	}
}
