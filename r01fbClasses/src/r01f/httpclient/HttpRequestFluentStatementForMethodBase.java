package r01f.httpclient;

import static r01f.httpclient.HttpRequestFormParameterForMultiPartBinaryData.HttpRequestFormBinaryParameterTransferEncoding.BASE64;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.apache.commons.codec.binary.Base64;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient.RequestMethod;
import r01f.mime.MimeType;
import r01f.util.types.Strings;

import com.google.common.collect.Lists;

abstract class HttpRequestFluentStatementForMethodBase<SELF_TYPE extends HttpRequestFluentStatementForMethodBase<SELF_TYPE>> 
       extends HttpRequestFluentStatementBase<HttpRequestFluentStatementForMethodBase<SELF_TYPE>> {
///////////////////////////////////////////////////////////////////////////////
// FIELDS
///////////////////////////////////////////////////////////////////////////////
	protected final RequestMethod _method;			// Request Method
	
	protected final Charset _targetServerCharset;	// destination server charset	
	
	protected final Map<String,String> _headers;	// request headers
	protected final Map<String,String> _cookies;	// Cookies
	
	protected final long _conxTimeOut;				// timeout to get a connection with server
	
	// Proxy connection variables.
	protected final String _proxyHost;			// proxy host
	protected final String _proxyPort;			// proxy port
	protected final UserCode _proxyUser;		// proxy user
	protected final Password _proxyPassword;	// proxy password
	
	// AuthDigest variables
	protected final UserCode _authUserCode;
	protected final Password _authPassword;
	
	// Payload (usually only for POST method-calls BUT, either GET & DELETE could 
	// support payloads
	protected HttpRequestPayload _payload;
	
///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////
	HttpRequestFluentStatementForMethodBase(final RequestMethod newMethod,
											final String theTargetUrlStr,final List<HttpRequestURLEncodedParameter> newUrlEncodedParameters,
											final Charset theTargetServerCharset,
						  				    final Map<String,String> newRequestHeaders,final Map<String,String> newRequestCookies,
										    final long newConxTimeOut,
						  				    final String newProxyHost,final String newProxyPort,final UserCode newProxyUser,final Password newProxyPassword,
						  				    final UserCode authUserCode,final Password authPassword) {
		_method = newMethod;
		_targetURLStr = theTargetUrlStr;
		_urlEncodedParameters = newUrlEncodedParameters;
		_targetServerCharset = theTargetServerCharset;
		_headers = newRequestHeaders;
		_cookies = newRequestCookies;
		_conxTimeOut = newConxTimeOut;
		_proxyHost = newProxyHost;
		_proxyPort = newProxyPort;
		_proxyUser = newProxyUser;
		_proxyPassword = newProxyPassword;
		_authUserCode = authUserCode;
		_authPassword = authPassword;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Gets the server response in an object that contains the stream in an InputStream 
	 * and the server response code
	 */
	public HttpResponse getResponse() throws IOException {
		HttpResponse outResponse = this.getResponse(false);
		return outResponse;
	}
	/**
	 * Gets the server response in an object that contains the stream in an InputStream 
	 * and the server response code
	 * @param useGAEHttpFetch true if Google App Engine HTTPFetch is to be used 
	 */
	public HttpResponse getResponse(final boolean useGAEHttpFetch) throws IOException {
		return new HttpResponse(this.getConnection(useGAEHttpFetch));
	}
	/**
	 * Gets a connection with server
	 */
	public HttpURLConnection getConnection() throws IOException {
		return this.getConnection(false);
	}
	/**
	 * Gets a server connection 
	 * @param useGAEHttpFetch true si hay que utilizar Google App Engine HTTPFetch 
	 */
	public HttpURLConnection getConnection(final boolean useGAEHttpFetch) throws IOException {
		// Devolver la conexión
		return new HttpRequestFluentStatementForResponse(_method,
														 _targetURLStr,_urlEncodedParameters,
														 _targetServerCharset,_conxTimeOut,
										 				 _headers,_cookies,
										 				 _proxyHost,_proxyPort,_proxyUser,_proxyPassword,
										 				 this)
											.getConnection(useGAEHttpFetch,
														   _authUserCode,_authPassword);
	}
	/**
	 * Load the server response stream as a String
	 * @return a String containing the server response
	 * @throws IOException if a connection could not be retrieved
	 */
	public String loadAsString() throws IOException {
		return this.loadAsString(false,
								 Charset.defaultCharset());
	}
	/**
	 * Load the server response stream as a String
	 * @return a String containing the server response
	 * @param serverSentDataCharset server sent data charset
	 * @throws IOException if a connection could not be retrieved
	 */
	public String loadAsString(final Charset serverSentDataCharset) throws IOException {
		return this.loadAsString(false,
								 serverSentDataCharset);
	}
	
	/**
	 * Load the server response stream as a String
	 * @param useGAEUrlFetch true if Google AppEngine HTTPFecth service is to be used
	 * @return a String containing the server response
	 * @throws IOException if a connection could not be retrieved
	 */
	public String loadAsString(final boolean useGAEUrlFetch) throws IOException {
		return this.loadAsString(useGAEUrlFetch,
								 Charset.defaultCharset());
	}
	/**
	 * Load the server response stream as a String
	 * @param useGAEUrlFetch true if Google AppEngine HTTPFecth service is to be used
	 * @param serverSentDataCharset server sent data charset
	 * @return a String containing the server response
	 * @throws IOException if a connection could not be retrieved
	 */
	public String loadAsString(final boolean useGAEUrlFetch,
							   final Charset serverSentDataCharset) throws IOException {
		@Cleanup InputStream responseIs = this.loadAsStream(useGAEUrlFetch);
		String outStr = Strings.of(responseIs,serverSentDataCharset)
							   .asString();
		return outStr;
	}
	/**
	 * Load the server response as a {@link Collection} of strings
	 * @return
	 * @throws IOException
	 */
	public Collection<String> readLines() throws IOException {
		return this.readLines(false,
							  Charset.defaultCharset());	
	}
	/**
	 * Load the server response as a {@link Collection} of strings
	 * @param serverSentDataCharset server sent data charset
	 * @return
	 * @throws IOException
	 */
	public Collection<String> readLines(final Charset serverSentDataCharset) throws IOException {
		return this.readLines(false,
							  serverSentDataCharset);	
	}
	/**
	 * Load the server response as a {@link Collection} of strings
	 * @param useGAEUrlFetch true if Google AppEngine HTTPFecth service is to be used
	 * @return
	 * @throws IOException
	 */
	public Collection<String> readLines(final boolean useGAEUrlFetch) throws IOException {
		return this.readLines(useGAEUrlFetch,
							  Charset.defaultCharset());	
	}
	/**
	 * Load the server response as a {@link Collection} of strings
	 * @param useGAEUrlFetch true if Google AppEngine HTTPFecth service is to be used
	 * @param serverSentDataCharset server sent data charset
	 * @return
	 * @throws IOException
	 */
	public Collection<String> readLines(final boolean useGAEUrlFetch,
										final Charset serverSentDataCharset) throws IOException {
		@Cleanup InputStream responseIs = this.loadAsStream(useGAEUrlFetch);
		BufferedReader lineReader = new BufferedReader(new InputStreamReader(responseIs));
		Collection<String> outLines = Lists.newArrayList();
		String line = null;
		while((line = lineReader.readLine()) != null) {
			outLines.add(line);
		}
		return outLines;
	}
	/**
	 * Load the server response stream as a {@link InputStream}
	 * @return an {@link InputStream} containing the server response
	 * @throws IOException if a connection could not be retrieved
	 */
	public InputStream loadAsStream() throws IOException {
		return this.loadAsStream(false);
	}
	/**
	 * Load the server response stream as a {@link InputStream}
	 * @param useGAEHttpFetch true si hay que utilizar Google App Engine HTTPFetch
	 * @return an {@link InputStream} containing the server response
	 * @throws IOException if a connection could not be retrieved
	 */
	public InputStream loadAsStream(final boolean userGAEUrlFetch) throws IOException {
		InputStream responseIs = this.getConnection(userGAEUrlFetch)
									 .getInputStream();
		return responseIs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PAYLOAD API
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets a POSTed params
	 * @param params the params
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE withPayload(final HttpRequestPayload payload) {
		if (payload == null) throw new IllegalArgumentException("The payload for a POST request cannot be null");
		_payload = payload;
		return (SELF_TYPE)this;
	}
	/**
	 * Sets empty POSTed params
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE withoutPayload(final MimeType mimeType) {
		_payload = HttpRequestPayload.wrap("")
									 .mimeType(mimeType);
		return (SELF_TYPE)this;
	}
	/**
	 * Returns the contentType header
	 * @return
	 */
	String payloadContentType() {
		return _payload != null ? _payload.getMimeType().getTypeName()
								: null;
	}
	/**
	 * @return the payload length
	 */
	long payloadContentLength() {
		long payloadContentLength = -1;
		if (_payload != null) {
			byte[] contentBytes = _payload.getTransferEncoding() == BASE64 ? Base64.encodeBase64(_payload.getContent())
													   			    	   : _payload.getContent();
			payloadContentLength = contentBytes.length;
		}
		return payloadContentLength;
	}
	/**
	 * Puts the payload into the http connection OutputStream
	 * @param dos the {@link OutputStream}
	 * @throws IOException if an I/O error occurs
	 */
	void payloadToOutputStream(final DataOutputStream dos) throws IOException {
		if (_payload != null) {
			byte[] contentBytes = _payload.getTransferEncoding() == BASE64 ? Base64.encodeBase64(_payload.getContent())
													   			    	   : _payload.getContent();
			ByteArrayOutputStream partBos = new ByteArrayOutputStream(contentBytes.length);
			partBos.write(contentBytes);
			partBos.flush();
			partBos.close();
			dos.write(partBos.toByteArray());
		}
	}
}
