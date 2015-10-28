package r01f.html;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.types.weburl.SerializedURL;

@XmlRootElement(name="htmlLink")
@Accessors(prefix="_")
public class HtmlLink {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlElement(name="text") @XmlCDATA
	@Getter private String _linkText;
	/**
	 * The URL
	 * Use R01MUrlBuilder.from(_url) to get a typed R01MUrl
	 */
	@XmlElement(name="url")
	@Getter private SerializedURL _url;
    /**
     * link presentation
     */
	@XmlElement(name="presentation")
    @Getter private HtmlLinkPresentationData _presentation;
	/**
	 * The url to which the user is redirected BEFORE accessing the final url
	 * When the link is composed, the user is redirected to this url where the system can
	 * for example:
	 * 		a.- Collect user info 
	 * 		b.- log the url access (ie: ip, counting access, etc)
	 * 		c.- etc
	 * once this is done the user is redirected to the final url
	 * NOTA: The final url is provided to this url in a param as: ?R01PassThrough=[final url]
	 */
	@XmlElement(name="prePassThroughURL")
	@Getter @Setter private SerializedURL _prePassThroughURL;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public HtmlLink() {
		// default no-args constructor
	}
	public HtmlLink(final String text,
						final SerializedURL url) {
		_linkText = text;
		_url = url;
	}
	public HtmlLink(final String text,
						final SerializedURL url,
						final HtmlLinkPresentationData presentation) {
		_linkText = text;
		_url = url;
		_presentation = presentation;
	}
}
