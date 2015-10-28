package r01f.types.weburl;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.debug.Debuggable;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Sets;

/**
 * Base type to url modeling
 * 
 * It's an abstract type because concrete types modeling security zones (intranet, extranet, internet, etc) and the environment (des, pru, prod)
 * depends on the concrete implementation (not every use case have the same security zones or environments)
 * 
 * Types extending this are usually like:
 * <pre class='brush:java'>
 * 		public class MyWebUrl<MyWebUrlSecurityZone,MyWebUrlEnvironment> {
 * 			
 * 		}
 * </pre>
 * 
 * A {@link WebUrl} contains:
 * 		- Protocol
 * 		- Host/Port
 * 		- Path
 * 		- Parameters and anchors
 * And also has:
 * <pre>
 * 		- {@link WebUrlEnvironment}: the environment (ie DES / PRU / PROD)
 * 		- {@link WebUrlSecurityZone}: The security zone (ie EXTRANET, INTRANET, INTERNET)
 * </pre> 
 * Either {@link WebUrlEnvironment} or {@link WebUrlSecurityZone} are INTERFACES that usually are implemented 
 * as {@link Enum}s. Those {@link Enum} are NOT known at R01F so the {@link WebUrl} object creation MUST be done 
 * at the concrete {@link WebUrl} implementation
 * To do so, the {@link WebUrl} concrete type MUST implements the abstract method WebUrl<S,E> _siteFrom(String)
 * that returns a {@link WebUrlSite} object from a {@link String}
 * For example:
 * <pre class='brush:java'>
 * 		public class MyWebUrl<MyWebUrlSecurityZone,MyWebUrlEnvironment> {
 *			public static R01MWebUrl from(final String url) {
 *				R01MWebUrl outUrl = new R01MWebUrl();
 *				outUrl._from(url);
 *				return outUrl;
 *			}
 *			@Override
 *			protected WebUrlSite<MyWebUrlSecurityZone,MyWebUrlEnvironment> _siteFrom(String site,int port) {
 *				// MyWebUrlSecurityZone and MyWebUrlEnvironment objects (enums) can ONLY be "interpreted"
 *				// in this method
 *				MyWebUrlSecurityZone securityZone = MyWebUrlSecurityZone.from(site);
 *				R01MWebUrlEnvironment env = MyWebUrlEnvironment.from(site,port);
 *		
 *				// Crete the WebUrlSite object
 *				WebUrlSite<MyWebUrlSecurityZone,MyWebUrlEnvironment> outSite = new WebUrlSite<MyWebUrlSecurityZone,MyWebUrlEnvironment>(securityZone,env,site);
 *				return outSite;
 *			}
 * 		}
 * </pre>
 * @see {@link WebUrlEnvironment} and {@link WebUrlSecurityZone} 
 * 
 * @param <Z> type modeling the security zone (intranet, extranet, etc); usually an ENUM
 * @param <E> type modeling an environment (des, prue, prod); usually an ENUM
 */
@ConvertToDirtyStateTrackable
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class WebUrlBase<SITE extends WebUrlSite<? extends WebUrlSecurityZone<?>,
				    		 	 	   					 ? extends WebUrlEnvironment<?>>> 
		   implements Serializable,
			  	      Debuggable {
	private static final long serialVersionUID = -6918493978725737285L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Protocol (HTTP / HTTPs)
	 */
	@XmlAttribute(name="protocol")
	@Getter @Setter private WebUrlProtocol _protocol;
	/**
	 * Host 
	 */
	@XmlElement(name="site")
	@Getter @Setter private SITE _site;
	/**
	 * Port
	 */
	@XmlAttribute(name="port")
	@Getter @Setter private int _port;
	/**
	 * Absolute path
	 */
	@XmlElement(name="path")
	@Getter @Setter private Path _absolutePath;
	/**
	 * Query String params
	 */
	@XmlElementWrapper(name="queryStringParams")
	@Getter @Setter private Set<WebUrlQueryStringParam> _queryStringParams;
	/**
	 * Anchor
	 */
	@XmlElement(name="anchor")
	@Getter @Setter private String _anchor;
	/**
	 * URL as a text
	 */
	@XmlElement(name="fullURL")
	@Getter @Setter private String _fullURL;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR   
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link WebUrl} from another
	 * @param other 
	 */
	public WebUrlBase(final WebUrlBase<SITE> other) {
		_protocol = other.getProtocol();
		_site  = other.getSite() != null ? _siteFrom(other.getSite()) 
										 : null;
		_port = other.getPort();
		_absolutePath = other.getAbsolutePath() != null ? new Path(other.getAbsolutePath())
														: null;
		if (!CollectionUtils.isNullOrEmpty(other.getQueryStringParams())) {
			_queryStringParams = Sets.newLinkedHashSet();
			for (WebUrlQueryStringParam p : other.getQueryStringParams()) {
				_queryStringParams.add( new WebUrlQueryStringParamImpl(new String(p.getName()),
																   	   new String(p.getValue())) );
			}
		}
		_anchor = other.getAnchor() != null ? new String(other.getAnchor()) : null;
		_fullURL = other.getFullURL() != null ? new String(other.getFullURL()) : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DATA LOADING METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads the {@link WebUrl} provided as parameter with the url as string also provided as
	 * parameter
	 * @param outUrl the {@link WebUrl} object to be loaded (it's modified in this method)
	 * @param url the url whose data is loaded
	 */
	protected void _from(final String url) {
		if (Strings.isNullOrEmpty(url)) return;
		
		// Re-init all fields
		_reset();
		_fullURL = url;
		
		// It's NOT known if the url includes host or not, that's to say, it's NOT known if the url is like:
		//		site/path?params
		// or simply:
		//		path?params
		// in the later case, it's difficult to know if it's an url including the site or not
		// for example in the relative url myPath?params, myPath could be interpreted as the site
		// The only way to know if it's really a path or a site is that "someone" knowing the possible sites "tells"
		// if myPath is a site or a path
		// This function could be done by either type WebUrlSecurityZone or WebUrlEnvironment because both of the
		// are supposed to "know" the sites
		if (url.startsWith("http") || url.startsWith("ftp")) {
			// It's sure that the url contains a site
			_parseFullUrl(url);
		} else if (url.startsWith("/")) {
			// It's sure that the url is an absolute url
			_parsePathUrl(url);
		} else {
			// It can be a complete url (with the site) or a relative url
			_parseFullUrl(url);
			
			if (this.getSite() != null 
			 && this.getSite().getSecurityZone() == null && this.getSitePort() > 0) {
				// do nothing
			} else if (this.getSite() != null 
					&& this.getSite().getSecurityZone().isExternal()) {
				// This could be a false positive and be a relative url... try to see if the site
				// match the pattern of a site; it's not a very scientific approach to do the checkings,
				// but there's no other
				if (!this.getSite().getHost().asString().contains(".")) {
					// It could be a relative path like images/myimage.jpg)   --> images is detected as host
					// BUT in this case it should NOT be detected as an EXTERNAL url, it should be detected as an INTERNA url,
					// that's to say it'll not enter this code... so it's a relative path 
					_reset();
					_fullURL = url;
					_parsePathUrl(url);
				}
			}
		}
	}
	/**
	 * Resets all fields 
	 */
	private void _reset() {
		_protocol = WebUrlProtocol.HTTP;
		_site = null;
		_port = 80;
		_absolutePath = null;
		_queryStringParams = null;
		_anchor = null;
		_fullURL = null;
	}
	/**
	 * Parses a COMPLETE url (protocol://sitio:puerto/path?params#anchor) and splits it in it's
	 * components
	 * @param url the url
	 * @return true if it's a valid url
	 */
	private boolean _parseFullUrl(final String fullUrl) {
		if (Strings.isNullOrEmpty(fullUrl)) return false;
		boolean outValid = false;
		// First split protocol://site/port and path?queryString#anchor
		Matcher m = FULL_URL_PATTERN.matcher(fullUrl.trim());
		if (m.find()) {
			if (m.groupCount() == 6) {
				String protocol = m.group(1);
				String site = m.group(2);
				String port = m.group(3);
				String path = m.group(4);
				String query = m.group(5);
				String anchor = m.group(6);
				_protocol = !Strings.isNullOrEmpty(protocol) ? WebUrlProtocol.fromCode(m.group(1)) : WebUrlProtocol.HTTP;
				_port = !Strings.isNullOrEmpty(port) ? Integer.parseInt(port) : 80;
				_site = !Strings.isNullOrEmpty(site) ? _siteFrom(new Host(site),_port)  : null;	// call the implementing type!
				_absolutePath = !Strings.isNullOrEmpty(path) ? Path.of(path)  : null;
				_queryStringParams = !Strings.isNullOrEmpty(query) ? _parseParams(query) : null;
				_anchor = !Strings.isNullOrEmpty(anchor) ? anchor : null;
			}
			// Check if it's the correct port when dealing with https
			if (_port == 443 || _port == 444) {
				outValid = false;	// It's mandatory to set the protocol if it's https	
			} else {
				outValid = true;
			}
		}
		return outValid;
	}
	/**
	 * Parses an absolute url or a relative one without the protocol part protocol://site:port/
	 * @param pathUrl 
	 * @return true if it's a valid url
	 */
	private boolean _parsePathUrl(final String pathUrl) {
		if (Strings.isNullOrEmpty(pathUrl)) return false;
		boolean outValid = false;
		Matcher m = PATH_URL_PATTERN.matcher(pathUrl);
		if (m.find()) {
			String path = m.group(1);
			String query = m.group(2);
			String anchor = m.group(3);
			_absolutePath = !Strings.isNullOrEmpty(path) ? Path.of(path) : null;
			_queryStringParams = !Strings.isNullOrEmpty(query) ? _parseParams(query) : null;
			_anchor = !Strings.isNullOrEmpty(anchor) ? anchor : null;
			outValid = true;
		}
		return outValid;
	}
	private static Set<WebUrlQueryStringParam> _parseParams(final String queryStr) {
		String[] params = queryStr.split("&");
		Set<WebUrlQueryStringParam> outParams = Sets.newLinkedHashSet();
		for (String paramAndValue : params) {
			WebUrlQueryStringParam param = WebUrlQueryStringParamImpl.from(paramAndValue);
			outParams.add(param);
		}
		return outParams;
	}
	// see http://mathiasbynens.be/demo/url-regex
	private static final transient String PROTOCOL_REGEX = "(?:(https?)://)?";
	private static final transient String SITE_REGEX = "([\\w\\.\\d]*)";
	private static final transient String PORT_REGEX = "(?::(\\d+))?";
	private static final transient String PATH_REGEX = "([^?#]*)";
	private static final transient String QUERY_REGEX = "(?:\\?([^#]*))?";
	private static final transient String ANCHOR_REGEX = "(?:#(.*))?";
	
	private static final transient Pattern FULL_URL_PATTERN = Pattern.compile("^" + PROTOCOL_REGEX + SITE_REGEX + PORT_REGEX + "/*" + PATH_REGEX + QUERY_REGEX + ANCHOR_REGEX + "$");
	private static final transient Pattern PATH_URL_PATTERN = Pattern.compile("^" + PATH_REGEX + QUERY_REGEX + ANCHOR_REGEX + "$");
/////////////////////////////////////////////////////////////////////////////////////////
//  ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the site from a {@link String} only containing the site (no path, no query string params no anchor)
	 * It's an abstract method because only concrete implementations can know how to extract it
	 * @param site 
	 * @param port
	 * @return the {@link WebUrlSite} object
	 */
	protected abstract SITE _siteFrom(final Host site,final int port);
	/**
	 * Gets a site from another
	 * @param other other site
	 * @return the {@link WebUrlSite} object
	 */
	protected abstract SITE _siteFrom(final SITE other);
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds all or part of the path (id: Path.of("/myWar/myServlet"))
	 * @param path the path to be added
	 * @return url
	 */
	@SuppressWarnings("unchecked")
	public <W extends WebUrlBase<SITE>> W addPath(final Path path) {
		if (path == null) return (W)this;
		if (_absolutePath == null) {
			_absolutePath = new Path(path);
		} else {
			_absolutePath.add(path);
		}
		return (W)this;
	}
	/**
	 * Adds all or part of the path (ie: /myWar/myServlet)
	 * @param path the path to be added
	 * @return url
	 */
	public <W extends WebUrlBase<SITE>> W addPath(final CanBeRepresentedAsString path) {
		return this.<W>addPath(path.asString());
	}
	/**
	 * Adds all or part of the path (ie: /myWar/myServlet)
	 * @param path the path to be added
	 * @return url
	 */
	@SuppressWarnings("unchecked")
	public <W extends WebUrlBase<SITE>> W addPath(final String path) {
		if (path == null) return (W)this;
		Path thePath = Path.of(path);
		return this.<W>addPath(thePath);
	}
	/**
	 * Adds all or part of the path from it's components
	 * (ie: myWar,myServlet)
	 * @param paths every single path elements
	 * @return url
	 */
	@SuppressWarnings("unchecked")
	public <W extends WebUrlBase<SITE>> W addPath(final String... paths) {
		if (CollectionUtils.isNullOrEmpty(paths)) return (W)this;
		Path thePath = Path.of(paths);
		return this.<W>addPath(thePath);
	}
	/**
	 * Adds a new query string parameter 
	 * @param name parameter name
	 * @param value parameter value
	 * @return 
	 */
	@SuppressWarnings("unchecked")
	public <W extends WebUrlBase<SITE>> W addQueryStringParam(final String name,final String value) {
		if (Strings.isNullOrEmpty(name) || Strings.isNullOrEmpty(value)) return (W)this;
		WebUrlQueryStringParam param = WebUrlQueryStringParamImpl.of(name,value);
		if (_queryStringParams == null) _queryStringParams = Sets.newLinkedHashSet();
		_queryStringParams.add(param);
		return (W)this;
	}
	/**
	 * Returns a param from the query string
	 * @param paramName the parameter name
	 * @return the {@link WebUrlQueryStringParam} object that encapsulates the parameter and it's value
	 */
	public WebUrlQueryStringParam getQueryStringParam(final String paramName) {
		if (CollectionUtils.isNullOrEmpty(_queryStringParams)) return null;
		WebUrlQueryStringParam outParam = null;
		for (WebUrlQueryStringParam p : _queryStringParams) {
			if (p.getName().equals(paramName)) {
				outParam = p;
				break;
			}
		}
		return outParam;
	}
	/**
	 * Returns the query string as a {@link String}
	 * @param encodeParamValues true if the param values should be encoded
	 * @return the query string as as {@link String}
	 */
	public String getQueryString(boolean encodeParamValues) {
		StringBuilder outQS = null;
		if (!CollectionUtils.isNullOrEmpty(_queryStringParams)) {
			outQS = new StringBuilder();
			for (Iterator<WebUrlQueryStringParam> it = _queryStringParams.iterator(); it.hasNext(); ) {
				outQS.append(it.next().asString(encodeParamValues));
				if (it.hasNext()) outQS.append("&");
			}
		}
		return outQS != null ? outQS.toString() : null;
	}
	/**
	 * Returns the query string as a {@link String} url-encoding params
	 * @return 
	 */
	public String getQueryStringUrlEncoded() {
		return this.getQueryString(true);
	}
	/**
	 * Returns the query string as a {@link String} NOT url-encoding params
	 * @return 
	 */
	public String getQueryStringNOTUrlEncoded() {
		return this.getQueryString(false);
	}
	/**
	 * @return the url as a string
	 */
	public SerializedURL asSerializedUrl() {
		String thePort = _port == 80 ? "" 
									 : Integer.toString(_port);
		StringBuilder sb = new StringBuilder(200);
		if (_site != null) {
			if (_protocol != null) {
				sb.append(_protocol.name().toLowerCase()).append("://").append(_site);
			} else if (_port == 80) {
				sb.append("http://").append(_site);
			} else if (_port == 443 || _port == 444) {
				sb.append("https://").append(_site);
			} else {
				throw new IllegalStateException("Thpe url protocol (http or https) cannot be known");
			}
			if (_port != 80 && _port != 443 && _port != 444) sb.append(":").append(thePort);
		}
		sb.append(_absolutePath != null ? _absolutePath.asAbsoluteString() : "/");
		if (!CollectionUtils.isNullOrEmpty(_queryStringParams)) {
			sb.append("?").append(this.getQueryString(false));
		}
		if (_anchor != null) sb.append("#").append(_anchor);
		return SerializedURL.of(sb.toString());
	}
	public String getSiteHostAsString() {
		return _site != null && _site.getHost() != null ? _site.getHost().asString() : null;
	}
	public int getSitePort() {
		return _port;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String debugInfo() {
		StringBuffer sb = new StringBuffer(200);
		sb.append("URL: ").append(this.asSerializedUrl().asStringNotUrlEncodingQueryStringParamsValues()).append("\r\n")
		  .append("-   Protocol: ").append(_protocol).append("\r\n")
		  .append("-       Site: ").append(_site != null ? _site.toString() : "").append("\r\n")
		  .append("-       Port: ").append(_port).append("\r\n")
		  .append("-       Path: ").append(_absolutePath).append("\r\n");
		if (!CollectionUtils.isNullOrEmpty(_queryStringParams)) {
			StringBuilder p = new StringBuilder();
			for (Iterator<WebUrlQueryStringParam> it = _queryStringParams.iterator(); it.hasNext(); ) {
				p.append(it.next().asString(false));
				if (it.hasNext()) p.append("&");
			}
			sb.append("Query String: ").append(p).append("\r\n");
		}
		if (_anchor != null) sb.append("-     Anchor: ").append(_anchor);
		return sb.toString();
	}	
}
