package r01f.httpclient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.Set;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.util.types.Strings;

import com.google.appengine.api.urlfetch.FetchOptions;
import com.google.appengine.api.urlfetch.HTTPHeader;
import com.google.appengine.api.urlfetch.HTTPMethod;
import com.google.appengine.api.urlfetch.HTTPRequest;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.common.collect.Sets;

@Slf4j
public class HttpGoogleURLFetchConnectionWrapper 
     extends HttpURLConnection {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  MEMBERS
/////////////////////////////////////////////////////////////////////////////////////////
	private double _conxTimeOut;
	
	private HTTPMethod _requestMethod;
	private Set<HTTPHeader> _requestHeaders;
	private GAEOutputStream _requestOS;		// FAKE OutputStream to the servidor
	private int _responseCode;				// Server Response code
	private InputStream _responseIS;		// FAKE InputStream with the response
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	protected HttpGoogleURLFetchConnectionWrapper(final String url,final long timeOut) throws MalformedURLException {
		this(new URL(url),timeOut);
	}
	
	protected HttpGoogleURLFetchConnectionWrapper(final URL url,final long timeOut) {
		super(url);
		_conxTimeOut = timeOut;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  OVERRIDEN METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Hace la llamada a URLFetch de google
	 * @throws IOException
	 */
	private void _doRequest() throws IOException {
		URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();

		FetchOptions fetchOptions = FetchOptions.Builder.withDefaults();
		fetchOptions.doNotValidateCertificate();
		fetchOptions.setDeadline(_conxTimeOut);

		HTTPRequest request = new HTTPRequest(this.getURL(),_requestMethod,
											  fetchOptions);
		if (_requestOS != null) {
			byte[] bytes = _requestOS.toByteArray();
			if (bytes != null && bytes.length > 0) {
				request.setPayload(bytes);
			}
		}
		HTTPResponse httpResponse = urlFetchService.fetch(request);
		_responseCode = httpResponse.getResponseCode();
		_responseIS = new ByteArrayInputStream(httpResponse.getContent());
	}
	
	@Override
	public OutputStream getOutputStream() throws IOException {
		// Cheat the client and return a custom OutputStream impl
		if (_requestOS == null) _requestOS = new GAEOutputStream();
		return _requestOS;
	}
	
	@Override
	public InputStream getErrorStream() {
		try {
			if (_responseIS == null) _doRequest();
		} catch (IOException ioEx) {
			log.error("Error retrieving connection for error stream",ioEx);
		}
		return _responseIS;
	}
	
	@Override
	public InputStream getInputStream() throws IOException {
		if (_responseIS == null) _doRequest();
		return _responseIS;
	}
	
	@Override
	public int getResponseCode() throws IOException {
		if (_responseIS == null) _doRequest();
		return _responseCode;
	}
	
	@Override
	public String getRequestMethod() {
		String outMethod = "unknown";
		if (_requestMethod == HTTPMethod.GET) {
			outMethod = "GET";
		} else if (_requestMethod == HTTPMethod.PUT) {
			outMethod = "PUT";
		} else if (_requestMethod == HTTPMethod.POST) {
			outMethod = "POST";
		} else if (_requestMethod == HTTPMethod.HEAD) {
			outMethod = "HEAD";
		} else if (_requestMethod == HTTPMethod.DELETE) {
			outMethod = "DELETE";
		}
		return outMethod;
	}
	
	@Override
	public void setRequestMethod(final String method) throws ProtocolException {
		// Pasar de lo que espera HttpURLConnection de java a lo que espera URLFetch
		if (method.equals("GET")) {
			_requestMethod = HTTPMethod.GET;
		} else if (method.equals("PUT")) {
			_requestMethod = HTTPMethod.PUT;
		} else if (method.equals("POST")) {
			_requestMethod = HTTPMethod.POST;
		} else if (method.equals("HEAD")) {
			_requestMethod = HTTPMethod.HEAD;
		} else if (method.equals("DELETE")) {
			_requestMethod = HTTPMethod.DELETE;
		}
	}
	
	@Override
	public void setRequestProperty(final String key,final String value) {
		// URLFeth does not allow to modify the following headers:
		//		- Content-Length
		//		- Host
		//		- Vary
		//		- Via
		//		- X-Forwarded-For
		//		- X-ProxyUser-IP
		String[] notAllowedHeaders = new String[] {"Content-Length","Host","Vary","Via","X-Forwarded-For","X-ProxyUser-IP"};
		if (Strings.of(key).in(notAllowedHeaders)) {
			log.warn("GAE does not allow any of theese headers: {}",notAllowedHeaders.toString());
			return;
		}
		if (_requestHeaders == null) _requestHeaders = Sets.newLinkedHashSet();
		_requestHeaders.add(new HTTPHeader(key,value));
	}
	
	@Override
	public void setDoInput(final boolean doinput) {
		// empty
	}
	@Override
	public void setDoOutput(final boolean dooutput) {
		// empty
	}
	@Override
	public void setUseCaches(final boolean usecaches) {
		// empty
	}
	@Override
	public void disconnect() {
		// empty
	}
	@Override
	public boolean usingProxy() {
		return false;
	}
	@Override
	public void connect() throws IOException {
		// empty
	}	
	
/////////////////////////////////////////////////////////////////////////////////////////
//  OUTPUTSTREAM
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * OutputStream impl where all the OutputStream methods other than toByteArray() are
	 * delegated
	 */
	@NoArgsConstructor
	class GAEOutputStream 
  extends ByteArrayOutputStream {
		ByteArrayOutputStream  _os = new ByteArrayOutputStream();
		
		@Override
		public synchronized byte[] toByteArray() {
			return _os.toByteArray();
		}
		@Override public void write(byte[] b) throws IOException 			{ 	_os.write(b); 	}
		@Override public synchronized void write(int b) 					{	_os.write(b); 	}
		@Override public synchronized void write(byte[] b,int off,int len) 	{		_os.write(b,off,len);		}
		@Override public synchronized void writeTo(OutputStream out) throws IOException {	_os.writeTo(out);		}
		@Override public void flush() throws IOException 	{	_os.flush();	}
		@Override public synchronized void reset() 			{		_os.reset();	}
		@Override public synchronized int size() 			{	return _os.size();	}
		@Override public void close() throws IOException 	{	_os.close();	}
	}

}
