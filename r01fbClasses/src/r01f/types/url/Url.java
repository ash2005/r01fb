package r01f.types.url;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import com.google.common.annotations.GwtIncompatible;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.exceptions.Throwables;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.patterns.Memoized;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.Paths;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


/**
 * Encapsulates an url as a {@link String} 
 * It's used to store the URLs as a {@link String} at an XML
 */
@GwtIncompatible("Url NOT usable in GWT")
@XmlRootElement(name="url")
@Accessors(prefix="_")
public class Url 
  implements CanBeRepresentedAsString,
  			 Serializable,
  			 Debuggable {

	private static final long serialVersionUID = 5383405611707444269L;
/////////////////////////////////////////////////////////////////////////////////////////
// 	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@XmlValue @XmlCDATA
	@Getter private String _url;	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected Url() {
		// just needed for marshalling
	}
	public Url(final String url) {
		_url = url;
	}
	public Url(final UrlComponents components) {
		_url = _asString(components,
						 false);		// do not encode
	}
	public Url(final UrlPath path) {		
		_url = path.asAbsoluteString();
	}
	public Url(final Host host,final int port) {
		this(UrlProtocol.HTTP,host,port);
	}
	public Url(final UrlProtocol protocol,final Host host,final int port) {
		this(protocol,host,port,
			 null,
			 null,null);
	}
	public Url(final UrlProtocol protocol,final Host host,final int port,
			   final UrlPath urlPath) {
		this(protocol,host,port,
			 urlPath,
			 null,null);
	}
	public Url(final UrlProtocol protocol,final Host host,final int port,
			   final UrlPath urlPath,final UrlQueryString queryString) {
		this(protocol,host,port,
			 urlPath,
			 queryString,null);
	}
	public Url(final UrlProtocol protocol,final Host host,final int port,
			   final UrlPath urlPath,final UrlQueryString queryString,final String anchor) {
		UrlProtocol theProt = protocol == null ? UrlProtocol.HTTP : protocol;
		String theHost = null;
		String thePathStr = urlPath == null ? "" : "/" + urlPath.asRelativeString();
		String theQryStr = queryString == null ? "" : "?" + queryString.asString();
		String theAnchorStr = anchor == null ? "" : "#" + anchor;
		if (host == null) {			
			_url = Strings.customized("{}{}{}",
									  thePathStr,
									  theQryStr,theAnchorStr);
		} else {	
			String theHostAsString = host.asString();
			
			// Sometimes the host contains a the protocol, port or path (ie: http://www.host.com/andUrlPath)
			// ... split the host and the path
			if (theHostAsString.contains("://") || theHostAsString.contains("/") || theHostAsString.contains(":")) {
				Url url = Url.from(theHostAsString);
				if (url.getHost() == null) {
					int pathStartIndex = theHostAsString.indexOf('/');
					theHost = pathStartIndex > 0 ? theHostAsString.substring(0,pathStartIndex) : theHostAsString;
					if (pathStartIndex > 0) url = Url.from(theHostAsString.substring(pathStartIndex+1));
				} else {
					theHost = url.getHost().asString();
				}
				if (url.getUrlPath() != null) thePathStr = Paths.forUrlPaths().join(url.getUrlPath(),thePathStr)
																    	  	  .asAbsoluteString();
				if (url.getQueryString() != null) theQryStr = "?" + url.getQueryString().joinWith(UrlQueryString.fromParamsString(theQryStr))
																 						.asStringNotEncodingParamValues();
				if (url.getAnchor() != null && anchor == null) theAnchorStr = url.getAnchor();
			}
			// the usual case
			else {
				theHost = theHostAsString;
			}
			_url = Strings.customized("{}://{}:{}{}{}{}",
									  theProt.asString(),theHost,port,
									  thePathStr,
									  theQryStr,theAnchorStr);
		}
	}
	public Url(final UrlPath path,final UrlQueryString queryString) {
		this(null,null,0,
			 path,
			 queryString,null);
	}
	public Url(final UrlPath path,final String anchor) {
		this(null,null,0,
			 path,
			 null,anchor);
	}
	public Url(final UrlPath path,final UrlQueryString queryString,final String anchor) {
		this(null,null,0,
			 path,
			 queryString,anchor);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static Url valueOf(final String url) {
		return Url.from(url);
	}
	public static Url from(final Url other,
						   final UrlPath path) {
		UrlComponents otherComps = other.getComponents();
		return Url.from(otherComps.getProtocol(),otherComps.getHost(),otherComps.getPort(),
					    Paths.forUrlPaths().join(otherComps.getUrlPath(),path),
					    otherComps.getQueryString(),
					    otherComps.getAnchor());
	}
	public static Url from(final UrlComponents components) {
		String urlAsStr = _asString(components,
									false);
		return Url.from(urlAsStr);
	}
	public static Url from(final UrlProtocol protocol,final Host host,final int port) {
		return new Url(protocol,host,port);
	}
	public static Url from(final UrlProtocol protocol,final Host host,final int port,
						   final UrlPath urlPath) {
		return new Url(protocol,host,port,
					   urlPath);
	}
	public static Url from(final UrlProtocol protocol,final Host host,final int port,
						   final UrlPath urlPath,final UrlQueryString queryString) {
		return new Url(protocol,host,port,
					   urlPath,queryString);
	}
	public static Url from(final UrlProtocol protocol,final Host host,final int port,
						   final UrlPath urlPath,final UrlQueryString queryString,final String anchor) {
		return new Url(protocol,host,port,
					   urlPath,queryString,anchor);
	}
	public static Url from(final Host host,final int port) {
		return Url.from(UrlProtocol.HTTP,host,port);
	}
	public static Url from(final Host host,final int port,
						   final UrlPath urlPath) {
		return Url.from(UrlProtocol.HTTP,host,port,
						urlPath);
	}
	public static Url from(final Host host,final int port,
						   final UrlPath urlPath,final UrlQueryString queryString) {
		return Url.from(UrlProtocol.HTTP,host,port,
						urlPath,queryString);
	}
	public static Url from(final Host host,final int port,
						   final UrlPath urlPath,final UrlQueryString queryString,final String anchor) {
		return Url.from(UrlProtocol.HTTP,host,port,
					    urlPath,queryString,anchor);
	}
	public static Url from(final UrlPath path) {		
		return Url.from(path.asAbsoluteString());
	}
	public static Url from(final UrlPath path,final UrlQueryString queryString) {
		return new Url(path,queryString);
	}
	public static Url from(final UrlPath path,final String anchor) {
		return new Url(path,anchor);
	}
	public static Url from(final UrlPath path,final UrlQueryString queryString,final String anchor) {
		return new Url(path,queryString,anchor);
	}
	public static Url from(final Url other) {
		if (other == null) return null;
		Url outUrl = new Url(other.asString());
		return outUrl;
	}
	public static Url from(final String url) {
		if (url == null) return null;
		Url outUrl = new Url(url);
		return outUrl;
	}
	public static Url from(final String url,final Object... vars) {
		return Url.from(Strings.customized(url,vars));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ADD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a NEW {@link Url} object joining this one with then given url path
	 * @param urlPath
	 * @return
	 */
	public Url joinWith(final UrlPath urlPath) {
		return Urls.join(this,urlPath);
	}
	/**
	 * Returns a NEW {@link Url} object joining this one with then given url query string
	 * @param qryString
	 * @return
	 */
	public Url joinWith(final UrlQueryString qryString) {
		return Urls.join(this,qryString);
	}
	/**
	 * Returns a NEW {@link Url} object joining this one with then given url query string
	 * @param urlPath
	 * @param qryString
	 * @return
	 */
	public Url joinWith(final UrlPath urlPath,
						final UrlQueryString qryString) {
		return Urls.join(this,urlPath,qryString);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CLONE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Object clone() throws CloneNotSupportedException {
		return Url.from(_url.toString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  URL components parse
/////////////////////////////////////////////////////////////////////////////////////////
	// see http://mathiasbynens.be/demo/url-regex
	private static final transient String PROTOCOL_REGEX = "(?:(https?|ftp)://)?";
	private static final transient String SITE_REGEX = "([\\w\\.\\d]*)";
	private static final transient String PORT_REGEX = "(?::(\\d+))?";
	private static final transient String PATH_REGEX = "([^?#]*)";
	private static final transient String QUERY_REGEX = "(?:\\?([^#]*))?";
	private static final transient String ANCHOR_REGEX = "(?:#(.*))?";
	
	private static final transient Pattern FILE_URL_PATTERN = Pattern.compile("^file://(.+)$");
	private static final transient Pattern FULL_URL_PATTERN = Pattern.compile("^" + PROTOCOL_REGEX + SITE_REGEX + PORT_REGEX + "/*" + PATH_REGEX + QUERY_REGEX + ANCHOR_REGEX + "$");
	private static final transient Pattern PATH_URL_PATTERN = Pattern.compile("^" + PATH_REGEX + QUERY_REGEX + ANCHOR_REGEX + "$");
	
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class UrlComponents 
	  implements Debuggable {
		@Getter private final UrlProtocol _protocol;
		@Getter private final Host _host;
		@Getter private final int _port;
		@Getter private final UrlPath _urlPath;
		@Getter private final UrlQueryString _queryString;
		@Getter private final String _anchor;
		
		@Override
		public String debugInfo() {
			StringBuffer sb = new StringBuffer(200);
			sb.append("-   Protocol: ").append(this.getProtocol()).append("\r\n")
			  .append("-       Site: ").append(this.getHost()).append("\r\n")
			  .append("-       Port: ").append(this.getPort()).append("\r\n")
			  .append("-       Path: ").append(this.getUrlPath()).append("\r\n")
			  .append("Query String: ").append(this.getQueryString().asStringNotEncodingParamValues()).append("\r\n")
			  .append("-     Anchor: ").append(this.getAnchor());
			return sb.toString();
		}
	}
	
	private UrlComponents _parseFullUrl(final String urlStr) {
		UrlComponents outComponents = null;
		if (urlStr.startsWith("file://")) {
			Matcher m = FILE_URL_PATTERN.matcher(urlStr);
			if (m.find()) {
				String pathStr = m.group(1);
				
				outComponents = new UrlComponents(UrlProtocol.FILE,null,0,
												  UrlPath.from(pathStr),null,null);
			}
		} 
		else {
			// split protocol://site/port and path?queryString#anchor
			Matcher m = FULL_URL_PATTERN.matcher(urlStr);
			if (m.find()) {
				String protocolStr = m.group(1);
				String siteStr = m.group(2);
				String portStr = m.group(3);
				String pathStr = m.group(4);
				String queryStr = m.group(5);
				String anchorStr = m.group(6);
				UrlProtocol protocol = !Strings.isNullOrEmpty(protocolStr) ? UrlProtocol.fromCode(protocolStr) : null;
				Host host = !Strings.isNullOrEmpty(siteStr) ? new Host(siteStr) : null;	
				int port = !Strings.isNullOrEmpty(portStr) ? Integer.parseInt(portStr) : 80;
				UrlPath urlPath = !Strings.isNullOrEmpty(pathStr) ? UrlPath.from(pathStr)  : null;
				UrlQueryString qryString = !Strings.isNullOrEmpty(queryStr) ? UrlQueryString.fromParamsString(queryStr) : null;
				String anchor = anchorStr;
				
				outComponents = new UrlComponents(protocol,host,port,
												  urlPath,qryString,anchor);
					
			}
		}
		if (outComponents == null) throw new IllegalStateException(Throwables.message("{} is NOT a valid url",urlStr));
		return outComponents;
	}
	/**
	 * Parses an absolute url or a relative one without the protocol part protocol://site:port/
	 * @param pathUrl 
	 * @return true if it's a valid url
	 */
	private UrlComponents _parsePathUrl(final String pathUrl) {
		UrlComponents outComponents = null;
		Matcher m = PATH_URL_PATTERN.matcher(pathUrl);
		if (m.find()) {
			String pathStr = m.group(1);
			String queryStr = m.group(2);
			String anchorStr = m.group(3);
			UrlPath urlPath = !Strings.isNullOrEmpty(pathStr) ? UrlPath.from(pathStr) : null;
			UrlProtocol protocol = null;
			Host host = null;
			int port = 0;
			if (urlPath != null && CollectionUtils.hasData(urlPath.getPathElements()) && urlPath.getFirstPathElement().equals("localhost")) {
				protocol = UrlProtocol.HTTP;
				host = Host.localhost();
				port = 80;
				urlPath = urlPath.getPathElements().size() > 1 ? UrlPath.from(urlPath.getPathElementsFrom(1))	// skip first element
															   : null;
			}
			UrlQueryString qryString = !Strings.isNullOrEmpty(queryStr) ? UrlQueryString.fromParamsString(queryStr) : null;
			String anchor = anchorStr;

			outComponents = new UrlComponents(protocol,host,port,
											  urlPath,qryString,anchor);
		}
		return outComponents;
	}
	private final transient Memoized<UrlComponents> _urlComponents = 
									new Memoized<UrlComponents>() {
											@Override
											protected UrlComponents supply() {
												if (_url == null) throw new IllegalStateException("The url is null!!");
												
												String theUrl = _url.trim();
												
												UrlComponents outUrlComponents = null;
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
												if (theUrl.startsWith("http") || theUrl.startsWith("ftp")) {
													// It's sure that the url contains a site
													outUrlComponents = _parseFullUrl(theUrl);
												} 
												else if (theUrl.startsWith("/")) {
													// It's sure that the url is an absolute url
													outUrlComponents = _parsePathUrl(theUrl);
												} 
												else {
													// It can be a complete url (with the site) or a relative url
													if (theUrl.matches("[^/]+:.+")) {
														// the url is something like host:port/something
														outUrlComponents = _parseFullUrl(theUrl);
													} else if (theUrl.matches("(\\w+\\.\\w+)+.+")) {
														// the url is something like host.domain:port/something
														outUrlComponents = _parseFullUrl(theUrl);
													} else {
														// the url is something like host/something
														outUrlComponents = _parsePathUrl(theUrl);
													}
												}
												return outUrlComponents;
											}		
									};
	public UrlComponents getComponents() {
		return _urlComponents.get();
	}
	public Host getHost() {
		return _urlComponents.get().getHost();
	}
	public int getPort() {
		return _urlComponents.get().getPort();
	}
	public UrlPath getUrlPath() {
		return _urlComponents.get().getUrlPath();
	}
	public UrlQueryString getQueryString() {
		return _urlComponents.get().getQueryString();
	}
	public Set<UrlQueryStringParam> getQueryStringParams() {
		return this.getQueryString() != null ? this.getQueryString().getQueryStringParams()
											 : null;
	}
	/**
	 * Checks if the url's query string contains a param with a provided name
	 * @param name
	 * @return
	 */
	public boolean containsQueryStringParam(final String name) {
		return this.getQueryStringParamValue(name) != null;
	}
	/**
	 * Checks if the url's query string contains a param with a provided name
	 * @param name
	 * @return
	 */
	public String getQueryStringParamValue(final String name) {
		String outValue = null;
		Set<UrlQueryStringParam> queryStringParams = this.getQueryStringParams();
		if (CollectionUtils.hasData(queryStringParams)) {
			for (UrlQueryStringParam param : queryStringParams) {
				if (param.getName().equals(name)) {
					outValue = param.getValue();
					break;
				}
			}
		}
		return outValue;
	}
	public String getAnchor() {
		return _urlComponents.get().getAnchor();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Return an object of type {@link URL} from the string representing the url
	 * @return
	 * @throws MalformedURLException
	 */
	public URL asUrl() throws MalformedURLException {
		return new URL(_url.toString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  asString
/////////////////////////////////////////////////////////////////////////////////////////
	private final Memoized<String> _asStringEncodingQryStr = new Memoized<String>() {
																	@Override
																	protected String supply() {
																		UrlComponents comps = Url.this.getComponents();
																		return _asString(comps,true);
																	}
															 };
	private final Memoized<String> _asStringNotEncodingQryStr = new Memoized<String>() {
																	@Override
																	protected String supply() {
																		UrlComponents comps = Url.this.getComponents();
																		return _asString(comps,false);
																	}
															 	};
	/**
	 * Returns the url obtained from it's components as {@link String} url-encoding the query string param values as
	 * @param urlComps
	 * @param encodeQueryStringParams
	 * @return
	 */
	private static String _asString(final UrlComponents urlComps,
								    final boolean encodeQueryStringParams) {
		StringBuilder sb = new StringBuilder(200);
		
		if (urlComps.getProtocol() == UrlProtocol.FILE) {
			// file urls
			sb.append(urlComps.getProtocol().name().toLowerCase()).append("://").append(urlComps.getUrlPath().asRelativeString());
		} else {
			// usual http or https urls
			if (urlComps.getHost() != null) {
				if (urlComps.getProtocol() != null) {
					sb.append(urlComps.getProtocol().name().toLowerCase()).append("://").append(urlComps.getHost());
					if (urlComps.getPort() > 0 
					 && urlComps.getPort() != 80 
					 && urlComps.getPort() != 443) {
						sb.append(":").append(urlComps.getPort());
					}
				} else if (urlComps.getPort() == 80) {
					sb.append("http://").append(urlComps.getHost());
				} else if (urlComps.getPort() == 443) {
					sb.append("https://").append(urlComps.getHost());
				} else if (urlComps.getPort() == 444) {
					sb.append("https://").append(urlComps.getHost()).append(":444");
				} else if (urlComps.getPort() > 0) {
					// the protocol cannot be guessed
					sb.append(urlComps.getHost()).append(":").append(urlComps.getPort());
				} else {
					// the protocol cannot be guessed
					sb.append(urlComps.getHost());
				}
			}
			sb.append(urlComps.getUrlPath() != null ? urlComps.getUrlPath().asAbsoluteString() 
													: "");
			if (urlComps.getQueryString() != null) {
				sb.append("?").append(urlComps.getQueryString().asString(encodeQueryStringParams));
			}
			if (urlComps.getAnchor() != null) sb.append("#").append(urlComps.getAnchor());
		}
		return sb.toString();
	}
	/**
	 * Returns the url as {@link String} url-encoding the query string param values as 
	 * specified by the param
	 * @param encodeQueryStringParams
	 * @return
	 */
	public String asString(final boolean encodeQueryStringParams) {
		return encodeQueryStringParams ? _asStringEncodingQryStr.get()
									   : _asStringNotEncodingQryStr.get();
	}
	@Override
	public String asString() {
		return this.asStringNotUrlEncodingQueryStringParamsValues();
	}
	/**
	 * Returns the url as a {@link String} url-encoding the query string param values 
	 * @return
	 */
	public String asStringUrlEncodingQueryStringParamsValues() {
		return this.asString(true);
	}
	/**
	 * Returns the url as a {@link String} NOT encoding the query string param values 
	 * @return
	 */
	public String asStringNotUrlEncodingQueryStringParamsValues() {
		return this.asString(false);
	}
	@Override
	public String toString() {
		return this.asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public boolean equals(final Object other) {
		if (other == null) return false;
		if (other instanceof Url) {
			return ((Url) other).asString().equals(this.asString());
		}
		return false;
	}
	@Override
	public int hashCode() {
		return _url.toString().hashCode();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUGGALE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String debugInfo() {
		StringBuffer sb = new StringBuffer(200);
		UrlComponents urlComps = this.getComponents();
		sb.append("URL: ").append(this.asStringNotUrlEncodingQueryStringParamsValues()).append("\r\n")
		  .append(urlComps.debugInfo());
		return sb.toString();
	}
}
