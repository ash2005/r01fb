package r01f.httpclient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.EncoderException;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient.RequestMethod;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
public class HttpRequestFluentStatementForResponse 
     extends HttpRequestFluentStatementBase<HttpRequestFluentStatementForResponse> {
///////////////////////////////////////////////////////////////////////////////
// STATUS
///////////////////////////////////////////////////////////////////////////////
	protected final RequestMethod _method;			// Request method
	
	protected final Charset _targetServerCharset;	// charset utilizado por el servidor	
	
	protected 		Map<String,String> _headers;	// request headers
	protected final Map<String,String> _cookies;	// Cookies
	
	protected final long _conxTimeOut;				// timeout to get a connection with server
	
	// Proxy connection variables.
	protected final String _proxyHost;				// proxy host
	protected final String _proxyPort;				// proxy port
	protected final UserCode _proxyUser;			// proxy user
	protected final Password _proxyPassword;		// proxy password

	protected final HttpRequestFluentStatementForMethodBase<? extends HttpRequestFluentStatementForMethodBase<?>> _methodStatement;
	
	private boolean _connected = false;
	private HttpURLConnection _conx;

///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////
	HttpRequestFluentStatementForResponse(final RequestMethod newRequestMethod,
										  final String theTargetUrlStr,final List<HttpRequestURLEncodedParameter> newUrlEncodedParameters,
										  final Charset theTargetServerCharset,final long newConxTimeOut,
						  				  final Map<String,String> newRequestHeaders,final Map<String,String> newRequestCookies,
						  				  final String newProxyHost,final String newProxyPort,final UserCode newProxyUser,final Password newProxyPassword,
						  				  final HttpRequestFluentStatementForMethodBase<? extends HttpRequestFluentStatementForMethodBase<?>> newMethodStatement) {
		_method = newRequestMethod;
		_targetURLStr = theTargetUrlStr;
		_urlEncodedParameters = newUrlEncodedParameters;
		
		_targetServerCharset = theTargetServerCharset;
		_conxTimeOut = newConxTimeOut;

		_headers = newRequestHeaders;
		_cookies = newRequestCookies;

		_proxyHost = newProxyHost;
		_proxyPort = newProxyPort;
		_proxyUser = newProxyUser;
		_proxyPassword = newProxyPassword;
		
		_methodStatement = newMethodStatement;
	}
	
///////////////////////////////////////////////////////////////////////////////
// API
///////////////////////////////////////////////////////////////////////////////
	public HttpURLConnection getConnection(final boolean useGAEHttpFetch,
										   final UserCode authUserCode,final Password authPassword) throws IOException {
		// [1]: Obtener la url final anexando los parámetros
		String url = _completeUrl();
		log.trace("Conectig to: {}",url);
		
		// [2]: If the request is authenticated
		if (authUserCode != null && authPassword != null) {
			// If URI Digest Authorization is in use some steps must be followed:
			// http://en.wikipedia.org/wiki/Digest_access_authentication
			// The getAuthorizationHeaderValue method from AuthDigestSolutionRetriever type gets the value to be 
			// set at the Authorization header
			log.trace("...using user/password auth: {}/{}",authUserCode,authPassword);
			try {
				HttpURLConnection conxNOAuth = this.getConnection(useGAEHttpFetch,
																  null,null);		// no auth
				String authHeaderValue = AuthDigestSolutionRetriever.getAuthorizationHeaderValue(conxNOAuth,
																	   		  	 				 RequestMethod.GET,_targetURLStr,
																	   		  	 				 authUserCode,authPassword);
				_setHeader("Authorization",authHeaderValue);
			} catch(AuthenticationException authEx) {
				throw new IOException(authEx);
			} catch(MalformedChallengeException mfcEx) {
				throw new IOException(mfcEx);
			}
		}

		// [3]: Set the content-type & content-length header
		if (_method.hasPayload()) {
			String payloadContentType = _methodStatement.payloadContentType();
			if (payloadContentType != null) {
				String otherContentType = _headers != null ? _headers.get("Content-Type") : null;
				if (otherContentType != null && !otherContentType.equals(payloadContentType)) throw new IllegalArgumentException("The Content-Type set for the http request is NOT the same as the one set for the payload!");
				_setHeader("Content-Type",payloadContentType);
			}
			long payloadContentLength = _methodStatement.payloadContentLength();
			if (payloadContentLength > 0) _setHeader("Content-Length",Long.toString(payloadContentLength));
		}
		
		// [4]: Set the cookies header
		if (!CollectionUtils.isNullOrEmpty(_cookies)) {
			StringBuilder cookiesStr = new StringBuilder(_cookies.size() * 15);
			for(Iterator<Map.Entry<String,String>> it = _cookies.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<String,String> cookie = it.next();
				cookiesStr.append(cookie.getKey() + "=" + cookie.getValue());
				if (it.hasNext()) cookiesStr.append(";");
			}
			_setHeader("Cookie",cookiesStr.toString());
		}
		if (log.isTraceEnabled() && CollectionUtils.hasData(_headers)) {
			StringBuilder headersDbg = new StringBuilder();
			headersDbg.append("[HEADERS]:\n");
			for(Iterator<Map.Entry<String,String>> hdIt = _headers.entrySet().iterator(); hdIt.hasNext(); ) {
				Map.Entry<String,String> hd = hdIt.next();
				headersDbg.append("\t* ").append(hd.getKey()).append(": ").append(hd.getValue());
				if (hdIt.hasNext()) headersDbg.append("\n");
			}
			log.trace(headersDbg.toString());
		}
		
		// [5]: Establish the connection
		if (!_connected && useGAEHttpFetch) {
			_doGAERequest(url);
		} else if (!_connected) {
			_doRequest(url);			// <-- this is where the connection is really done
		}
		return _conx;
	}
///////////////////////////////////////////////////////////////////////////////
// METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Exec the server http call, sending headers and url parameters
	 * @param url the final url
	 * @return an InputStream to read the response
	 * @exception IOException if an I/O error occurs
	 */
	private void _doRequest(final String url) throws IOException {
		log.trace("...retrieving connection using {} method",_method);
		// Get the server connection and send headers
		HttpURLConnection conx = null;
		if (url.startsWith("https")) {
			HttpsConnectionRetriever connectionRetriever = new HttpsConnectionRetriever();
			conx = connectionRetriever.getConnection(url,_conxTimeOut,
													 _proxyHost,_proxyPort,_proxyUser,_proxyPassword);
		} else if (url.startsWith("http")) {
			HttpConnectionRetriever connectionRetriever = new HttpConnectionRetriever();
			conx = connectionRetriever.getConnection(url,_conxTimeOut,
													 _proxyHost,_proxyPort,_proxyUser,_proxyPassword);
		} else {
			throw new IOException("NO se soporta el protocolo especificado en la url: '" + url + "'");
		}
		if (conx == null) throw new IOException( "No se ha podido obtener una conexión con '" + url + "'" );
		conx.setDoInput(true);
		conx.setUseCaches(false);
		log.trace("...connection retrieved!");
		
		_setConnectionRequestMethod(conx);				// Sets the http method POST/PUT/GET/HEAD/DELETE
		_sendHeaders(conx);								// Sends the http headers
		
		if (_method.hasPayload()) {
			_sendPayload(conx);	// Sends the payload 
		}
		_conx = conx;
	}
	/**
	 * Execs the server http call using GAE mechanics
	 * @param url the final url
	 * @throws IOException if an I/O error occurs
	 */
	private void _doGAERequest(final String url) throws IOException {
		log.trace("...retrieving GAE connection using {} method",_method);
		HttpURLConnection conx = new HttpGoogleURLFetchConnectionWrapper(url,_conxTimeOut);
		conx.setDoInput(true);
		conx.setUseCaches(false);
		log.trace("...GAE connection retrieved!");
		
		_setConnectionRequestMethod(conx);				// Sets the http method POST/PUT/GET/HEAD/DELETE
		_sendHeaders(conx);								// Sends the http headers
		
		if (_method.hasPayload()) {
			_sendPayload(conx);	// Sends the payload 
		}
		_conx = conx;
	}
	private void _setConnectionRequestMethod(final HttpURLConnection conx) throws IOException {
		if (_method.isPOST()) {
			conx.setDoOutput(true);
			conx.setRequestMethod("POST");
		} else if (_method.isPUT()) {
			conx.setDoOutput(true);
			conx.setRequestMethod("PUT");
		} else if (_method.isDELETE()) {
			conx.setRequestMethod("DELETE");
		} else if (_method.isHEAD()) {
			conx.setDoOutput(false);
			conx.setRequestMethod("HEAD");
		} else if (_method.isGET()) {
			conx.setDoOutput(false);
			conx.setRequestMethod("GET");
		}
	}
	/**
	 * Sends http headers to the servers
	 * @param conx the server http connection
	 * @throws IOException if an I/O error occurs
	 */
	private void _sendHeaders(final URLConnection conx) throws IOException {
		if (_headers != null) {
			for (Map.Entry<String,String> me : _headers.entrySet()) {
				conx.setRequestProperty(me.getKey(),me.getValue() );
			}
		}
	}
	/**
	 * Sends http call payload if the method is POST or PUT in any of its variants
	 * @param conx the server http connection
	 * @throws IOException if an I/O error occurs
	 */
	private void _sendPayload(final URLConnection conx) throws IOException {
		@Cleanup DataOutputStream out = new DataOutputStream(conx.getOutputStream());		
		_methodStatement.payloadToOutputStream(out);
		out.flush();
	}
///////////////////////////////////////////////////////////////////////////////
// PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets a request header
	 * @param name name of the header
	 * @param value value of the header
	 */
	private void _setHeader(final String name,final String value) {
		if (_headers == null) _headers = new HashMap<String,String>();
		_headers.put(name,value);
	}
	private String _completeUrl() {
		// Si la peticion es GET / DELETE / HEAD, la URL contiene todos los parametros codificados
		String completeURL = _targetURLStr;
		String parameters = _urlEncodedParametersAsQueryString();
		if (!Strings.isNullOrEmpty(parameters)) {
			completeURL =  _targetURLStr + (completeURL.indexOf("?") < 0 ? "?" : "&") + parameters;
		}  
		return completeURL;
	}
	/**
	 * Creates the URL encoded query string to be appended to the target server url
	 */
	private String _urlEncodedParametersAsQueryString() {
		if (_urlEncodedParameters == null || _urlEncodedParameters.size() == 0) return null;
		StringBuilder sb = new StringBuilder(_urlEncodedParameters.size()*10);
		
		for (Iterator<HttpRequestURLEncodedParameter> paramIt = _urlEncodedParameters.iterator(); paramIt.hasNext(); ) {
			HttpRequestURLEncodedParameter param = paramIt.next();
			try {
				if (_targetServerCharset != null) {
					sb.append(Strings.of(param.getName()).urlEncode().encode(_targetServerCharset));
				} else {
					sb.append(Strings.of(param.getName()).urlEncode());
				}
				sb.append('=');
				if (_targetServerCharset != null) {
					sb.append(Strings.of(param.get(_targetServerCharset)));
				} else {
					sb.append(Strings.of(param.get(Charset.defaultCharset())));
				}
			} catch(EncoderException encEx) {	
				// try without encoding
				if (_targetServerCharset != null) {
					sb.append(Strings.of(param.getName()).encode(_targetServerCharset));
				} else {
					sb.append(Strings.of(param.getName()));
				}
				sb.append('=');
				if (_targetServerCharset != null) {
					sb.append(Strings.of(param.get(_targetServerCharset)));
				} else {
					sb.append(Strings.of(param.get(Charset.defaultCharset())));
				}
			}
			if (paramIt.hasNext()) sb.append('&');
		}
		return sb.toString();
	}

}
