package r01f.httpclient;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;

import com.google.common.collect.Maps;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.mime.MimeType;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.Url.UrlComponents;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


public class HttpRequestFluentStatementForConnection 
     extends HttpRequestFluentStatementBase<HttpRequestFluentStatementForConnection> {
	
///////////////////////////////////////////////////////////////////////////////
// FIELDS
///////////////////////////////////////////////////////////////////////////////
	private Charset _targetServerCharset;	// charset utilizado por el servidor	

	private Map<String,String> _headers;	// request headers
	private Map<String,String> _cookies;	// Cookies
	
	private long _conxTimeOut = -1;			// timeout to get a connection with server
	// Variables Conexión vía proxy.
	private String _proxyHost;		// proxy host
	private String _proxyPort;		// proxy port
	private UserCode _proxyUser;	// proxy user
	private Password _proxyPassword;// proxy password
	
	// Variable usadas en la autorización Digest
	private UserCode _authUserCode;
	private Password _authPassword;
	
///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////
	HttpRequestFluentStatementForConnection(final String newTargetUrlStr) {
		_targetURLStr = newTargetUrlStr;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CHARSET
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Establece el charset utilizado en la comunicación con el servidor
	 * @param charset el charset
	 */
	public HttpRequestFluentStatementForConnection usingCharset(final Charset charset) {
		_targetServerCharset = charset;
		if (_targetServerCharset == null) _targetServerCharset = Charset.defaultCharset();	// Asegurarse de que hay un charset establecido
		return this;
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  HEADERS & COOKIES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets cookies
	 * @param cookies
	 * @return
	 */
	public HttpRequestFluentStatementForConnection settingCookies(final HttpRequestCookie... cookies) {
		if (CollectionUtils.hasData(cookies)) {
			Map<String,String> cookiesMap = Maps.newHashMapWithExpectedSize(cookies.length);
			for (HttpRequestCookie cookie : cookies) cookiesMap.put(cookie.getName(),
														     		cookie.getValue());
			this.settingCookies(cookiesMap);
		}
		return this;
	}
	/**
	 * Sets a cookie
	 * @param cookieName
	 * @param cookieValue
	 * @return
	 */
	public HttpRequestFluentStatementForConnection settingCookie(final String cookieName,final String cookieValue) {
		if (Strings.isNullOrEmpty(cookieName) || Strings.isNullOrEmpty(cookieValue)) return this;
		_setCookie(cookieName,cookieValue);
		return this;
	}
	/**
	 * Sets the cookies to send to the server 
	 * @param cookies the cookies
	 */
	public HttpRequestFluentStatementForConnection settingCookies(final Map<String,String> cookies) {
		if (cookies == null || cookies.size() == 0) return this;
		for(Map.Entry<String,String> me : cookies.entrySet()) _setCookie(me.getKey(),me.getValue());
		return this;
	}
	/**
	 * Sets a header to be sent to the server
	 * @param headers
	 * @return
	 */
	public HttpRequestFluentStatementForConnection withHeaders(final HttpRequestHeader... headers) {
		if (CollectionUtils.hasData(headers)) {
			Map<String,String> headersMap = Maps.newHashMapWithExpectedSize(headers.length);
			for (HttpRequestHeader header: headers) headersMap.put(header.getName(),
														    	   header.getValue());
			this.withHeaders(headersMap);
		}
		return this;
	}
	/**
	 * Sets a header to be sent to the server
	 * (this method can be called multiple times)
	 * @param headers the header (name / value pair)
	 */
	public HttpRequestFluentStatementForConnection withHeader(final String headerName,final String headerValue) {
		if (Strings.isNullOrEmpty(headerName) || Strings.isNullOrEmpty(headerValue)) return this;
		_setHeader(headerName,headerValue);
		return this;
	}
	/**
	 * Sets all the headers to send to the server
	 * @param headers all the headers (name/value pairs in a Map)
	 */
	public HttpRequestFluentStatementForConnection withHeaders(final Map<String,String> headers) {
		if (CollectionUtils.isNullOrEmpty(headers)) return this;
		for(Map.Entry<String,String> me : headers.entrySet()) _setHeader(me.getKey(),me.getValue());
		return this;
	}
	/**
	 * Sets a request header
	 * @param name name of the header
	 * @param value value of the header
	 */
	private void _setHeader(final String name,final String value) {
		if (_headers == null) _headers = new HashMap<String,String>();
		_headers.put(name,value);
	}
	/**
	 * Sets a cookie header
	 * @param cookieName name of the cookie
	 * @param cookieValue value of the cookie
	 */
	private void _setCookie(final String cookieName,final String cookieValue) {
		if (_cookies == null) _cookies = new HashMap<String,String>();
		_cookies.put(cookieName,cookieValue);
	}
///////////////////////////////////////////////////////////////////////////////
// API FOR CONNECTION
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the timeout to retrieve a server connection
	 * @param timeOutMillis max time (in millis) to get a connection
	 */
	public HttpRequestFluentStatementForConnection withConnectionTimeOut(final long timeOutMillis) {
		_conxTimeOut = timeOutMillis;
		return this;
	}
	/**
	 * Sets de proxy data needed to get a connection with the server through a proxy
	 * @param proxyInfo
	 */
	public HttpRequestFluentStatementForConnection usingProxy(final HttpClientProxySettings proxyInfo) {
		if (proxyInfo == null) throw new IllegalArgumentException("The proxy info cannot be null!");
		return this.usingProxy(proxyInfo.getProxyUrl(),
							   proxyInfo.getUserCode(),proxyInfo.getPassword());
	}
	/**
	 * Sets de proxy data needed to get a connection with the server through a proxy
	 * @param proxyUrl
	 * @param user user
	 * @param password
	 */
	public HttpRequestFluentStatementForConnection usingProxy(final Url proxyUrl,
											  				  final UserCode user,final Password password) {
		if (proxyUrl == null) throw new IllegalArgumentException("The proxyUrl cannot be null!");
		UrlComponents proxyUrlComps = proxyUrl.getComponents();
		if (proxyUrlComps.getHost() == null) throw new IllegalArgumentException("The proxy url site or host cannot be null!");
		return this.usingProxy(proxyUrlComps.getHost(),Integer.toString(proxyUrlComps.getPort()),
							   user,password);
	}
	/**
	 * Sets de proxy data needed to get a connection with the server through a proxy
	 * @param host host 
	 * @param port port
	 * @param user user
	 * @param password password
	 */
	public HttpRequestFluentStatementForConnection usingProxy(final Host host,final String port,
											  				  final UserCode user,final Password password) {
		return this.usingProxy(host.asString(),port,
							   user,password);
	}
	/**
	 * Sets de proxy data needed to get a connection with the server through a proxy
	 * @param host host 
	 * @param port port
	 * @param user user
	 * @param password password
	 */
	public HttpRequestFluentStatementForConnection usingProxy(final String host,final String port,
											  				  final UserCode user,final Password password) {
		_proxyHost = host;
		_proxyPort = port;
		_proxyUser = user;
		_proxyPassword = password;
		
		// IMPORTANT!!! If proxy is in use, an http header Proxy-Authorization MUST be set
		String authString = _proxyUser + ":" + _proxyPassword;
		String authStringEncoded = new String(Base64.encodeBase64String(authString.getBytes()));
		
		_setHeader("Proxy-Authorization","Basic " + authStringEncoded);
		
		return this;
	}
	public HttpRequestFluentStatementForConnection notUsingProxy() {
		return this;
	}
	/**
	 * Disables the caching of content at proxies
	 */
	public HttpRequestFluentStatementForConnection disablingProxyCache() {
		_setHeader("Cache-Control","no-cache,max-age=0");
		_setHeader("Pragma","no-cache");
		return this;
	}
	/**
	 * Establece la información de autorización para la URL a conectarse en la cabecera http (autenticación básica)
	 * El nombre y la clave van como una cadena "Basic usr:psswd" codificada en base64
	 * en un campo de la cabecera llamado Authorization
	 * La autorización al igual que los demás campos de la cabecera persisten
	 * a lo largo de múltiples llamadas.
	 * @param user El nombre del usuario
	 * @param password La clave
	 */
	public HttpRequestFluentStatementForConnection usingBasicAuthCredentials(final UserCode user,final Password password) {
		String authString = user.asString() + ":" + password.asString();
		String authStringEncoded = Base64.encodeBase64String(authString.getBytes());
		_setHeader("Authorization","Basic " + authStringEncoded);
		return this;
	}
	/**
	 * Establece la información de autorización para la URL a conectarse en la cabecera http (autenticación básica)
	 * El nombre y la clave van como una cadena "Basic usr:psswd" codificada en base64
	 * en un campo de la cabecera llamado Authorization
	 * La autorización al igual que los demás campos de la cabecera persisten
	 * a lo largo de múltiples llamadas.
	 * @param name El nombre del usuario
	 * @param password La clave
	 */
	public HttpRequestFluentStatementForConnection usingDigestAuthCredentials(final UserCode name,final Password password) {
		_authUserCode = name;
		_authPassword = password;
		return this;
	}
	/**
	 * Sets the contentType 
	 * @param contentType the contentType
	 */
	public HttpRequestFluentStatementForConnection settingContentTypeTo(final MimeType contentType) {
		if (contentType == null) return this;
		_setHeader("Content-Type",contentType.getTypeName());
		return this;
	}
	/**
	 * Sets the contentType 
	 * @param contentType the contentType
	 */
	public HttpRequestFluentStatementForConnection settingContentTypeTo(final String contentType) {
		if (Strings.isNullOrEmpty(contentType)) return this;
		_setHeader("Content-Type",contentType);
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODs
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the GET Http method
	 * @return
	 */
	public HttpRequestFluentStatementForGETMethod GET() {
		return new HttpRequestFluentStatementForGETMethod(_targetURLStr,_urlEncodedParameters,
														  _targetServerCharset,
														  _headers,_cookies,
														  _conxTimeOut,
														  _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
														  _authUserCode,_authPassword);
	}
	/**
	 * Sets the POST Http method
	 * @return
	 */
	public HttpRequestFluentStatementForHEADMethod HEAD() {
		return new HttpRequestFluentStatementForHEADMethod(_targetURLStr,_urlEncodedParameters,
														   _targetServerCharset,
														   _headers,_cookies,
														   _conxTimeOut,
														   _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
														   _authUserCode,_authPassword);
	}
	/**
	 * Sets the POST Http method
	 * @return
	 */
	public HttpRequestFluentStatementForDELETEMethod DELETE() {
		return new HttpRequestFluentStatementForDELETEMethod(_targetURLStr,_urlEncodedParameters,
															 _targetServerCharset,
														     _headers,_cookies,
														     _conxTimeOut,
														     _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
														     _authUserCode,_authPassword);
	}
	/**
	 * Sets the POST Http method
	 * @return
	 */
	public HttpRequestFluentStatementForPOSTMethod POST() {
		return new HttpRequestFluentStatementForPOSTMethod(_targetURLStr,_urlEncodedParameters,
														    _targetServerCharset,
														   _headers,_cookies,
														   _conxTimeOut,
														   _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
														   _authUserCode,_authPassword);
	}
	/**
	 * Sets the POST Form Http method
	 * @return
	 */
	public HttpRequestFluentStatementForPOSTFormURLEncodedMethod POSTForm() {
		return new HttpRequestFluentStatementForPOSTFormURLEncodedMethod(_targetURLStr,_urlEncodedParameters,
																		  _targetServerCharset,
																		 _headers,_cookies,
																		 _conxTimeOut,
																		 _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
																		 _authUserCode,_authPassword);
	}
	/**
	 * Sets the POST MultiPart Http method
	 * @return
	 */
	public HttpRequestFluentStatementForPOSTMultiPartMethod POSTMultiPart() {
		return new HttpRequestFluentStatementForPOSTMultiPartMethod(_targetURLStr,_urlEncodedParameters,
																	_targetServerCharset,
																    _headers,_cookies,
																	_conxTimeOut,
																	_proxyHost,_proxyPort,_proxyUser,_proxyPassword,
																	_authUserCode,_authPassword);
	}
	/**
	 * Sets the PUT Http method
	 * @return
	 */
	public HttpRequestFluentStatementForPUTMethod PUT() {
		return new HttpRequestFluentStatementForPUTMethod(_targetURLStr,_urlEncodedParameters,
														  _targetServerCharset,
														  _headers,_cookies,
														  _conxTimeOut,
														  _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
														  _authUserCode,_authPassword);
	}
	/**
	 * Sets the PUT Form Http method
	 * @return
	 */
	public HttpRequestFluentStatementForPUTFormURLEncodedMethod PUTForm() {
		return new HttpRequestFluentStatementForPUTFormURLEncodedMethod(_targetURLStr,_urlEncodedParameters,
																	    _targetServerCharset,
																		_headers,_cookies,
																		_conxTimeOut,
																		_proxyHost,_proxyPort,_proxyUser,_proxyPassword,
																		_authUserCode,_authPassword);
	}
	/**
	 * Sets the PUT Multipart Http method
	 * @return
	 */
	public HttpRequestFluentStatementForPUTMultiPartMethod PUTMultiPart() {
		return new HttpRequestFluentStatementForPUTMultiPartMethod(_targetURLStr,_urlEncodedParameters,
																   _targetServerCharset,
																   _headers,_cookies,
																   _conxTimeOut,
																   _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
																   _authUserCode,_authPassword);
	}
}
