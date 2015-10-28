package r01f.html;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Maps;


/**
 * Models an html link presentation data
 * Usage:
 * <pre class='brush:java'>
 * 		HtmlLinkPresentationData presentation = HtmlLinkPresentationData.create()
 * 													.withId("myLink")
 * 													.withText("My link to some resource")
 * 													.withTitle("a link")
 * 													.forTargetResource(HtmlLinkTargetResourceData.create()
 * 																			.relatedAs(LICENSE)
 * 																			.withLang(Language.ENGLISH)
 * 																			.withMimeType(MimeTypeForDocument.PDF))
 * 													.openingInNewWindowAs(HtmlLinkWindowOpeningMode.create()
 * 																			.withName("My new window")
 * 																			.withAppearance(OpeningWindowAppearance.create(CENTERED,false)
 * 																								.withDimensions(800,600)
 * 																								.notResizable()
 * 																								.withBars(OpeningWindowBars.create()
 * 																												.showingLocationBar()
 * 																												.showingMenuBar()
 * 																												.hidingStatusBar()
 * 																												.hidingScrollBars())))
 * 													.withStyleClasses("myStyleClass1","myStyleClass2")
 * 													.addJavaScriptEvent("onClick","doSomething()")
 * 													.withMediaQuery(MediaQuery.createForDevice(MediaQueryDevice.SCREEN)
 *											 										.pixelRatioMinForWebKit(1.5F));
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="linkPresentationData")
@Accessors(prefix="_")
public class HtmlLinkPresentationData 
  implements Serializable {
	
	private static final long serialVersionUID = 6720826281308071548L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="id")
	@Getter @Setter private String _id;
    /**
     * Link title
     */
	@XmlElement(name="title") @XmlCDATA
    @Getter @Setter private String _title;
	/**
	 * Link destination resource description
	 */
	@XmlElement(name="targetResourceData")
	@Getter @Setter private HtmlLinkTargetResourceData _targetResourceData;
    /**
     * If the link is opened in a new window this object contains this new window properties 
     */
	@XmlElement(name="windowOpeningMode")
    @Getter @Setter private HtmlLinkWindowOpeningMode _newWindowOpeningMode;
    /**
     * Style classes to apply to the html link
     */
	@XmlElementWrapper(name="styleClasses")
    @Getter @Setter private Collection<String> _styleClasses;
	/**
	 * Inline style
	 */
	@XmlElement(name="style")
	@Getter @Setter private String _inlineStyle;
    /**
     * JavaScript events to be handled: eventName - event code
     */
	@XmlElementWrapper(name="javaScriptEvents") @XmlCDATA
    @Getter @Setter private Map<String,String> _javaScriptEvents;
	/**
	 * Media queries
	 */
	@XmlElement(name="mediaQuery") @XmlCDATA
	@Getter @Setter private String _mediaQueries;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static HtmlLinkPresentationData create() {
		HtmlLinkPresentationData outData = new HtmlLinkPresentationData();
		return outData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public HtmlLinkPresentationData withId(final String id) {
		_id = id;
		return this;
	}
	public HtmlLinkPresentationData withTitle(final String title) {
		_title = title;
		return this;
	}
	public HtmlLinkPresentationData forTargetResource(final HtmlLinkTargetResourceData targetResource) {
		_targetResourceData = targetResource;
		return this;
	}
	public HtmlLinkPresentationData openingInNewWindowAs(final HtmlLinkWindowOpeningMode newWindowOpeningMode) {
		_newWindowOpeningMode = newWindowOpeningMode;
		return this;
	}
	public HtmlLinkPresentationData withStyleClasses(final String... classes) {
		String[] styles = null;
		if (CollectionUtils.hasData(classes)) {
			if (classes.length > 1) {
				styles = classes;
			} else {
				styles = Strings.of(classes[0])
								.splitter(",")
							    .trimResults()
							    .toArray();
				
			}
			_styleClasses = CollectionUtils.of(styles)
										   .asCollection();
		}
		return this;
	}
	public HtmlLinkPresentationData addJavaScriptEvent(final String eventName,final String scriptCode) {
		if (_javaScriptEvents == null) _javaScriptEvents = Maps.newHashMap();
		_javaScriptEvents.put(eventName,scriptCode);
		return this;
	}
	public HtmlLinkPresentationData withMediaQuery(final MediaQuery... orCombinedMediaQueries) {
		if (CollectionUtils.hasData(orCombinedMediaQueries)) {
			Collection<MediaQuery> mq = CollectionUtils.of(orCombinedMediaQueries)
													   .asCollection();
			_mediaQueries = MediaQuery.toMediaQueryString(mq);		// put in css format
		}
		return this;
	}
}
