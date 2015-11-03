package r01f.servlet;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.HttpMethod;

import lombok.Cleanup;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.ContentBody;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;

import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Maps;



@Accessors(prefix="_")
@Slf4j
public class ProxyServlet 
	 extends HttpServlet {

	private static final long serialVersionUID = 4118855885724222239L;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////	
	private static final int FOUR_KB = 4196;
	/**
	 * Key for proxy servlet information
	 */
	public static final String GWT_COMPILEDCODE_PROXIEDWAR_RELPATH_HEADER = "X-gwtCodeRelPath";
    /**
     * Key for redirect location header.
     */
    private static final String LOCATION_HEADER = "Location";
    /**
     * Key for content length header.
     */
    private static final String CONTENT_LENGTH_HEADER_NAME = "Content-Length";
    /**
     * Key for host header
     */
    private static final String HOST_HEADER_NAME = "Host";
    /**
     * The directory to use to temporarily store uploaded files
     */
    private static final File FILE_UPLOAD_TEMP_DIRECTORY = new File(System.getProperty("java.io.tmpdir"));
/////////////////////////////////////////////////////////////////////////////////////////
//	STATUS
/////////////////////////////////////////////////////////////////////////////////////////
    // Proxy host params
    /**
     * The host to which we are proxying requests. Default value is "localhost".
     */
    @Getter @Setter private String _proxyHost = "localhost";
    /**
     * The port on the proxy host to which we are proxying requests. Default value is 80.
     */
    @Getter @Setter private int _proxyPort = 80;
    /**
     * When the proxied-request is a GWT client-to-RemoteServlet request, a header called X-gwtCodeRelPath
     * is appended to the proxied request including the GWT-compiled code path relative to the destination WAR
     * This relative path is the location of the policy files generated by GWT-compiler for the serialized types
     */
    @Getter @Setter private String _gwtCompiledCodeProxiedWarRelativePath = "";
    /**
     * Setting that allows removing the initial path from client. Allows specifying /twitter/* as synonym for twitter.com.
     */
    @Getter @Setter private boolean _removePrefix;
    /**
     * The maximum size for uploaded files in bytes. Default value is 5MB.
     */
    @Getter @Setter private int _maxFileUploadSize = 5 * 1024 * 1024;
    
    @Getter @Setter private boolean _followRedirects;
    
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////   
    @Override
    public String getServletInfo() {
        return "GWT Proxy Servlet";
    } 
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Initialize the <code>ProxyServlet</code>
     *
     * @param servletConfig The Servlet configuration passed in by the servlet container
     */
    @Override
	public void init(final ServletConfig servletConfig) {
        // Get the proxy host
        String proxyHostFromWebXML = servletConfig.getInitParameter("proxyHost");
        if (proxyHostFromWebXML == null || proxyHostFromWebXML.length() == 0) {
            throw new IllegalArgumentException("Proxy host not set, please set init-param 'proxyHost' in web.xml");
        }
        this.setProxyHost(proxyHostFromWebXML);
        // Get the proxy port if specified
        String proxyPortFromWebXML = servletConfig.getInitParameter("proxyPort");
        if (proxyPortFromWebXML != null && proxyPortFromWebXML.length() > 0) {
            this.setProxyPort(Integer.parseInt(proxyPortFromWebXML));
        }
        // Get the proxy path if specified
        String gwtCompiledCodeProxiedWarRelativePathFromWebXML = servletConfig.getInitParameter("gwtCompiledCodeProxiedWarRelativePath");
        if (gwtCompiledCodeProxiedWarRelativePathFromWebXML != null && gwtCompiledCodeProxiedWarRelativePathFromWebXML.length() > 0) {
            this.setGwtCompiledCodeProxiedWarRelativePath(gwtCompiledCodeProxiedWarRelativePathFromWebXML);
        }
        // Get the maximum file upload size if specified
        String maxFileUploadSizeFromWebXML = servletConfig.getInitParameter("maxFileUploadSize");
        if (maxFileUploadSizeFromWebXML != null && maxFileUploadSizeFromWebXML.length() > 0) {
            this.setMaxFileUploadSize(Integer.parseInt(maxFileUploadSizeFromWebXML));
        }

    }
/////////////////////////////////////////////////////////////////////////////////////////
//	GET
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Performs an HTTP GET request
     * @param originalRequest  The {@link HttpServletRequest} object passed
     *                         in by the servlet engine representing the
     *                         client request to be proxied
     * @param responseToClient The {@link HttpServletResponse} object by which
     *                         we can send a proxied response to the client
     */
    @Override
	public void doGet(final HttpServletRequest originalRequest,
    				  final HttpServletResponse responseToClient) throws IOException, 
    				  													 ServletException {
        // [1] Create a GET request
        String destinationUrl = _getProxyURL(originalRequest);
        log.debug("GET Request URL: {} Destination URL {}",originalRequest.getRequestURL(),
        											 	   destinationUrl);
        //System.out.println("------------------->" + destinationUrl);
        
        HttpGet getRequestToBeProxied = new HttpGet(destinationUrl);
        
        // [2] Transfer the original request headers/cookies to the proxied request
        _transferRequestHeaders(originalRequest,
        						getRequestToBeProxied);
        _transferRequestCookies(originalRequest,
        						getRequestToBeProxied);
        
        // [3] Execute the proxy request
        _executeProxyRequest(originalRequest,responseToClient,
        					 getRequestToBeProxied);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	POST
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Performs an HTTP POST request
     * @param originalReqest  The {@link HttpServletRequest} object passed
     *                     	  in by the servlet engine representing the
     *                     	  client request to be proxied
     * @param responseToClient The {@link HttpServletResponse} object by which
     *                         we can send a proxied response to the client
     */
    @Override
	public void doPost(final HttpServletRequest originalReqest, 
    				   final HttpServletResponse responseToClient) throws IOException,
    				   													  ServletException {
        // [1] Create the POST request
        ContentType contentType = ContentType.create(originalReqest.getContentType());
        String destinationUrl = _getProxyURL(originalReqest);
        log.debug("POST Request URL: {} - Content-Type: {} - Destination URL: {}",originalReqest.getRequestURL(),
        																		  contentType,
        																		  destinationUrl);
        //System.out.println("------------------->" + destinationUrl);
        
        HttpPost postRequestToBeProxied = new HttpPost(destinationUrl);
        
        // [2] Transfer the original request headers/cookies to the proxied request
        _transferRequestHeaders(originalReqest,
        					    postRequestToBeProxied);
        _transferRequestCookies(originalReqest,
        						postRequestToBeProxied);
        
        // [3] Transfer the data depending on the post way:
        //		- mulitpart (file upload) POST data to the proxied request
        //		- form-url encoded
        //		- raw post
        if (ServletFileUpload.isMultipartContent(originalReqest)) {
            _transferMultipartPost(originalReqest,
            					   postRequestToBeProxied);
        } else {
            if (contentType == null || ContentType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
                _transferFormUrlEncodedPost(originalReqest,
                							postRequestToBeProxied);
            } else {
                _transferContentPost(originalReqest,
                					 postRequestToBeProxied);
            }
        }
        
        // [4] Execute the proxy request
        _executeProxyRequest(originalReqest,responseToClient,
        					 postRequestToBeProxied);
    }
    /**
     * Sets up the given {@link PostMethod} to send the same form/url-encoded POST
     * data as was sent in the given {@link HttpServletRequest}
     * @param postRequestToBeProxied The {@link PostMethod} that we are
     *                               configuring to send a standard POST request
     * @param originalRequest    The {@link HttpServletRequest} that contains
     *                           the POST data to be sent via the {@link PostMethod}
     */
    private static void _transferFormUrlEncodedPost(final HttpServletRequest originalRequest,
    										 		final HttpPost postRequestToBeProxied) throws UnsupportedEncodingException {
        // Get the client POST data as a Map
		Map<String,String[]> postParams = originalRequest.getParameterMap();
        
        // Create a List to hold the NameValuePairs to be passed to the PostMethod
        List<NameValuePair> nameAndValuePairs = new ArrayList<NameValuePair>();
        for (String paramName : postParams.keySet()) {
            // Iterate the values for each parameter name
            String[] paramValues = postParams.get(paramName);
            for (String paramValue : paramValues) {
                NameValuePair nameValuePair = new BasicNameValuePair(paramName,paramValue);
                nameAndValuePairs.add(nameValuePair);
            }
        }
        // Set the proxy request POST data
        UrlEncodedFormEntity paramEntity = new UrlEncodedFormEntity(nameAndValuePairs);        
        postRequestToBeProxied.setEntity(paramEntity);
    }
    /**
     * Sets up the given {@link PostMethod} to send the same content POST
     * data (JSON, XML, etc.) as was sent in the given {@link HttpServletRequest}
     * @param postRequestToBeProxied The {@link PostMethod} that we are
     *                               	 configuring to send a standard POST request
     * @param originalRequest    The {@link HttpServletRequest} that contains
     *                               		the POST data to be sent via the {@link PostMethod}
     */
    private void _transferContentPost(final HttpServletRequest originalRequest,
    								  final HttpPost postRequestToBeProxied) throws IOException {
    	// [1] Read the original POST content
        StringBuilder content = new StringBuilder();
		@SuppressWarnings("resource")
		@Cleanup BufferedReader reader = originalRequest.getReader();
        for (; ;) {
            String line = reader.readLine();
            if (line == null) break;
            content.append(line);
        }
        String postContent = content.toString();

        // [2] Replace all the references to the original server with the proxied one
        ContentType contentType = ContentType.create(originalRequest.getContentType());
        if (contentType.getMimeType().startsWith("text/x-gwt-rpc")) {
            String clientHost = originalRequest.getLocalName();
            if (clientHost.equals("127.0.0.1")) {
                clientHost = "localhost";
            }
            int clientPort = originalRequest.getLocalPort();
            String clientUrl = clientHost + ((clientPort != 80) ? ":" + clientPort : "");
            String serverUrl = _proxyHost + ((_proxyPort != 80) ? ":" + _proxyPort : "") + originalRequest.getServletPath();
            //debug("Replacing client (" + clientUrl + ") with server (" + serverUrl + ")");
            postContent = postContent.replace(clientUrl,
            								  serverUrl);
        }
        
        // [3] Hand de POST data to the proxied server
        log.debug("POST Content Type: {} - Content: {} ",contentType,
        												 postContent);
        StringEntity entity = new StringEntity(postContent,
            						  		   contentType);
        postRequestToBeProxied.setEntity(entity);
    }
    /**
     * Sets up the given {@link PostMethod} to send the same multipart POST
     * data as was sent in the given {@link HttpServletRequest}
     * @param postRequestToBeProxied The {@link PostMethod} that we are
     *                               configuring to send a multipart POST request
     * @param originalRequest     The {@link HttpServletRequest} that contains
     *                            the mutlipart POST data to be sent via the {@link PostMethod}
     */
	@SuppressWarnings("null")
	private void _transferMultipartPost(final HttpServletRequest originalRequest,
    							        final HttpPost postRequestToBeProxied) throws IOException,
    							        										      ServletException {
    	// Get the contentType
    	ContentType contentType = ContentType.create(originalRequest.getContentType(),
                    								 originalRequest.getCharacterEncoding());
    	
        // Create a factory for disk-based file items
        DiskFileItemFactory diskFileItemFactory = new DiskFileItemFactory();
        // Set factory constraints
        diskFileItemFactory.setSizeThreshold(this.getMaxFileUploadSize());
        diskFileItemFactory.setRepository(FILE_UPLOAD_TEMP_DIRECTORY);
        // Create a new file upload handler
        ServletFileUpload fileUploadServlet = new ServletFileUpload(diskFileItemFactory);
        
        // Parse the original request and hand it to the proxied endpoint
        List<FileItem> items = null;
        try {
            // Get the multipart items as a list (the FileUpload saves in a temp dir the file items)
            items = fileUploadServlet.parseRequest(originalRequest);
            
            // Process all parts
            Map<String,ContentBody> parts = Maps.newHashMap();
            for (FileItem item : items) {
                // If the current item is a form field, then create a string part
            	// ... otherwise if the current item is a file item, create a filePart
                if (item.isFormField()) {
                    StringBody stringPart = new StringBody(item.getString(),	 		// The field value
                    									   contentType);
                    parts.put(item.getFieldName(),
                    		  stringPart);
                } else {
					@SuppressWarnings("resource")
					@Cleanup InputStream is = item.getInputStream();
                	InputStreamBody isPart = new InputStreamBody(is,
                												 contentType,
                												 null);			// null filename
                    parts.put(item.getFieldName(),
                    		  isPart);
                }
            }
            // Create the multi part and do the POST
            MultipartEntityBuilder multiPartEntityBuilder = MultipartEntityBuilder.create();
            if (CollectionUtils.hasData(parts)) {
            	for (Map.Entry<String,ContentBody> partEntry : parts.entrySet()) {
            		multiPartEntityBuilder.addPart(partEntry.getKey(),
            									   partEntry.getValue());
            	}
            }
            HttpEntity multiPartEntity = multiPartEntityBuilder.build();
            postRequestToBeProxied.setEntity(multiPartEntity);
            
            // The current content-type header (received from the client) IS of
            // type "multipart/form-data", but the content-type header also
            // contains the chunk boundary string of the chunks. Currently, this
            // header is using the boundary of the client request, since we
            // blindly copied all headers from the client request to the proxy
            // request. However, we are creating a new request with a new chunk
            // boundary string, so it is necessary that we re-set the
            // content-type string to reflect the new chunk boundary string
            postRequestToBeProxied.setHeader(multiPartEntity.getContentType());
            
        } catch (FileUploadException fileUploadException) {
            throw new ServletException(fileUploadException);
        } finally {
        	// Temporal files cleanup
        	if (CollectionUtils.hasData(items)) {
        		for (FileItem item : items) item.delete();
        	}
        }
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Executes the {@link HttpMethod} passed in and sends the proxy response
     * back to the client via the given {@link HttpServletResponse}
     * @param originalRequest 	 The origingal servlet request
     * @param responseToClient   An object by which we can send the proxied
     *                           response back to the client
     * @param requestToBeProxied An object representing the proxy request to be made
     * @throws IOException      Can be thrown by the {@link HttpClient}.executeMethod
     * @throws ServletException Can be thrown to indicate that another error has occurred
     */
	@SuppressWarnings("resource")
	private void _executeProxyRequest(final HttpServletRequest originalRequest,final HttpServletResponse responseToClient,
    								  final HttpRequestBase requestToBeProxied) throws IOException, 
            						  											   	   ServletException {
    
        // [1] - Create a default HttpClient
    	HttpClientBuilder clientBuilder = HttpClientBuilder.create();	// HttpParams httpClientParams = new BasicHttpParams();
    	clientBuilder.disableRedirectHandling();						// httpClientParams.setParameter(ClientPNames.HANDLE_REDIRECTS,false);
    																	// httpClientParams.setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS,false);
    																	// HttpClientParams.setRedirecting(httpClientParams,false);
        HttpClient httpClient = clientBuilder.build(); 					// HttpClient httpClient = new SystemDefaultHttpClient(httpClientParams);
        
        // [2] - Execute the request
        HttpResponse endPointResponse = httpClient.execute(requestToBeProxied);

        // [3]  Handle redirects (301) or client cache usage advices (304)
        if (endPointResponse.getStatusLine().getStatusCode() >= HttpServletResponse.SC_MULTIPLE_CHOICES /* 300 */ 
         && endPointResponse.getStatusLine().getStatusCode() < HttpServletResponse.SC_NOT_MODIFIED /* 304 */) {
        	
        	boolean hasToContinue = _handleRedirection(originalRequest,responseToClient,
    								   				   requestToBeProxied,
    								   				   endPointResponse);
        	if (!hasToContinue) return;	// there is a redirection... do not continue

        } else if (endPointResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_NOT_MODIFIED) {
            // 304 needs special handling.  See: http://www.ics.uci.edu/pub/ietf/http/rfc1945.html#Code304
            // We get a 304 whenever passed an 'If-Modified-Since' header and the data on disk has not changed; 
        	// server responds with a 304 saying I'm not going to send the body because the file has not changed.
            responseToClient.setIntHeader(CONTENT_LENGTH_HEADER_NAME, 0);
            responseToClient.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
            return;
        }

        // [4] Copy the headers of the proxied server to the client response
        _transferResponseHeaders(endPointResponse,
        						 responseToClient);

        // [5] - Pass the response code back to the client
        // [5.1] transfer the status code sent by the proxied server to the response to client
        responseToClient.setStatus(endPointResponse.getStatusLine().getStatusCode());

        // [5.2] transfer the content sent by the proxied server to the response to client
        //		 (the response from the proxied server could be ziped... unzip befor transfer it to the response to client)
        InputStream  endPointResponseIS = endPointResponse.getEntity()
        												  .getContent();
        if (_isBodyParameterGzipped(endPointResponse)) {
            log.debug("GZipped: true");
            int length = 0;

            if (!_followRedirects 
             && endPointResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_MOVED_TEMPORARILY) {
            	
                String gz = requestToBeProxied.getFirstHeader(LOCATION_HEADER).getValue();
                responseToClient.setStatus(HttpServletResponse.SC_OK);
                endPointResponse.setStatusCode(HttpServletResponse.SC_OK);
                responseToClient.setHeader(LOCATION_HEADER,gz);
                
            } else {
            	
                final byte[] bytes = _ungzip(endPointResponseIS);
                length = bytes.length;
                endPointResponseIS = new ByteArrayInputStream(bytes);
            }
            responseToClient.setContentLength(length);
        } else {
        	// The response from the proxied server is NOT zipped
        }
        _copy(endPointResponseIS,
        	  responseToClient.getOutputStream());
        
        log.debug("Received status code: {} - Response: {}",endPointResponse,
        											  		endPointResponseIS);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Retrieves all of the headers from the servlet request and sets them on
     * the proxy request
     * @param originalRequest     The request object representing the client's
     *                            request to the servlet engine
     * @param requestToBeProxied The request that we are about to send to
     *                           the proxy host
     */
    private void _transferRequestHeaders(final HttpServletRequest originalRequest,
    									 final HttpRequestBase requestToBeProxied) {
        // Get an Enumeration of all of the header names sent by the client
		Enumeration<String> headerNames = originalRequest.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (headerName.equalsIgnoreCase(CONTENT_LENGTH_HEADER_NAME)) {
                continue;
            }
            // As per the Java Servlet API 2.5 documentation:
            //  ﻿  Some headers, such as Accept-Language can be sent by clients
            //﻿  ﻿  as several headers each with a different value rather than
            //﻿  ﻿  sending the header as a comma separated list.
            // Thus, an Enumeration of the header values sent by the client is getted
            Enumeration<String> headerValues = originalRequest.getHeaders(headerName);
            while (headerValues.hasMoreElements()) {
                String headerValue = headerValues.nextElement();
                // In case the proxy host is running multiple virtual servers,
                // rewrite the Host header to ensure that the content from
                // the correct virtual server is retrieved
                if (headerName.equalsIgnoreCase(HOST_HEADER_NAME)) {
                    headerValue = _getProxyHostAndPort();
                }
                Header header = new BasicHeader(headerName,headerValue);
                
                // Set the same header on the proxy request
                requestToBeProxied.setHeader(header);
            }
        }
    }
        
    /**
     * Retrieves all of the cookies from the servlet request and sets them on
     * the proxy request
     * @param originalRequest     	The request object representing the client's
     *                              request to the servlet engine
     * @param requestToBeProxied   	The request that we are about to send to
     *                             	the proxy host
     */
    private void _transferRequestCookies(final HttpServletRequest originalRequest,
    									 final HttpRequestBase requestToBeProxied) {
        // Get an array of all of all the cookies sent by the client
        Cookie[] cookies = originalRequest.getCookies();
        if (cookies == null) {
            return;
        }
        String cookiesStr = "";
        for (Cookie cookie : cookies) {
            cookie.setDomain(_proxyHost);
            cookie.setPath(originalRequest.getServletPath());
            cookiesStr = cookiesStr + " " + cookie.getName() + "=" + cookie.getValue() + "; Path=" + cookie.getPath() + ";";
        }
        requestToBeProxied.setHeader("Cookie", cookiesStr);
    }
    /**
     * Transfers to the response to send to the client all the headers 
     * received in the proxied server's response
     * @param endPointResponse	response received from the proxied server
     * @param responseToClient 	response to send to the client
     */
    private static void _transferResponseHeaders(final HttpResponse endPointResponse,
    									  		 final HttpServletResponse responseToClient) {
        Header[] endPointResponseHeaders = endPointResponse.getAllHeaders();
        for (Header header : endPointResponseHeaders) {
            if ((header.getName().equals("Transfer-Encoding") && header.getValue().equals("chunked"))
                 || 
                (header.getName().equals("Content-Encoding") && header.getValue().equals("gzip"))  // don't copy gzip header
                 || 
                (header.getName().equals("WWW-Authenticate"))) { 	// don't copy WWW-Authenticate header so browser doesn't prompt on failed basic auth
                // proxy servlet does not support chunked encoding
            } else {
                responseToClient.setHeader(header.getName(),
                						   header.getValue());
            }
        }
    }
    /**
     * Handles the proxied server redirection responses
     * The following code is adapted from org.tigris.noodle.filters.CheckForRedirect
     * @param originalRequest
     * @param responseToClient
     * @param requestToBeProxied
     * @param endPointResponse
     * @return
     * @throws IOException
     * @throws ServletException
     */
    private boolean _handleRedirection(final HttpServletRequest originalRequest,final HttpServletResponse responseToClient,
    								   final HttpRequestBase requestToBeProxied,
    								   final HttpResponse endPointResponse) throws IOException,
    								   											   ServletException {
    	boolean outHasToContinue = true;
    	
        String statusCodeStr = Integer.toString(endPointResponse.getStatusLine().getStatusCode());
        String stringLocation = requestToBeProxied.getFirstHeader(LOCATION_HEADER)
        										  .getValue();
        if (stringLocation == null) {
            throw new ServletException("Received status code: " + statusCodeStr + " but no " + LOCATION_HEADER + " header was found in the response");
        }
        // Modify the redirect to go to this proxy servlet rather that the proxied host
        String originalRequestedHostName = originalRequest.getServerName();
        if (originalRequest.getServerPort() != 80) {
            originalRequestedHostName += ":" + originalRequest.getServerPort();
        }
        originalRequestedHostName += originalRequest.getContextPath();
        if (_followRedirects) {
            if (stringLocation.contains("jsessionid")) {
                Cookie cookie = new Cookie("JSESSIONID", 
                						   stringLocation.substring(stringLocation.indexOf("jsessionid=") + 11));
                cookie.setPath("/");
                responseToClient.addCookie(cookie);
                //debug("redirecting: set jessionid (" + cookie.getValue() + ") cookie from URL");
            } else if (requestToBeProxied.getFirstHeader("Set-Cookie") != null) {
                Header header = requestToBeProxied.getFirstHeader("Set-Cookie");
                String[] cookieDetails = header.getValue().split(";");
                String[] nameValue = cookieDetails[0].split("=");

                Cookie cookie = new Cookie(nameValue[0], nameValue[1]);
                cookie.setPath("/");
                //debug("redirecting: setting cookie: " + cookie.getName() + ":" + cookie.getValue() + " on " + cookie.getPath());
                responseToClient.addCookie(cookie);
            }
            responseToClient.sendRedirect(stringLocation.replace(_getProxyHostAndPort(),
            										 			 originalRequestedHostName));
            outHasToContinue = false;
        }
        return outHasToContinue;
    }

/////////////////////////////////////////////////////////////////////////////////////////
//	
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * The response body will be assumed to be gzipped if the GZIP header has been set.
     * @param responseHeaders of response headers
     * @return true if the body is gzipped
     */
    private static boolean _isBodyParameterGzipped(final HttpResponse response) {
    	boolean outGzipped = false;
    	Header[] responseHeaders = response.getAllHeaders();
    	if (CollectionUtils.hasData(responseHeaders)) {
	        for (Header header : responseHeaders) {
	            if (header.getValue().equals("gzip")) {
	                outGzipped = true;
	                break;
	            }
	        }
    	}
        return outGzipped;
    }
    /**
     * A highly performant ungzip implementation. Do not refactor this without taking new timings.
     * See ElementTest in ehcache for timings
     * @param gzipped the gzipped content
     * @return an ungzipped byte[]
     * @throws java.io.IOException when something bad happens
     */
    private static byte[] _ungzip(final InputStream gzipped) throws IOException {
        final GZIPInputStream inputStream = new GZIPInputStream(gzipped);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final byte[] buffer = new byte[FOUR_KB];
        int bytesRead = 0;
        while (bytesRead != -1) {
            bytesRead = inputStream.read(buffer,0,FOUR_KB);
            if (bytesRead != -1) {
                byteArrayOutputStream.write(buffer,0,bytesRead);
            }
        }
        byte[] ungzipped = byteArrayOutputStream.toByteArray();
        inputStream.close();
        byteArrayOutputStream.close();
        return ungzipped;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	URL-UTILS	
/////////////////////////////////////////////////////////////////////////////////////////
    private String _getProxyURL(final HttpServletRequest originalRequest) {
        // Set the protocol to HTTP
        String protocol = (originalRequest.isSecure()) ? "https://" : "http://";
        String endPointURL = protocol + _getProxyHostAndPort();

        // simply use whatever servlet path that was part of the request as opposed to 
        // getting a preset/configurable proxy path
        if (!_removePrefix) {
            endPointURL += originalRequest.getServletPath();
        }
        endPointURL += "/";

        // Handle the path given to the servlet
        String pathInfo = originalRequest.getPathInfo();
        if (pathInfo != null && pathInfo.startsWith("/")) {
            if (endPointURL.endsWith("/")) {
                // avoid double '/'
                endPointURL += pathInfo.substring(1);
            }
        } else {
            endPointURL += originalRequest.getPathInfo();
        }
        // Handle the query string
        if (originalRequest.getQueryString() != null) {
            endPointURL += "?" + originalRequest.getQueryString();
        }
        return endPointURL;
    }
    private String _getProxyHostAndPort() {
        if (this.getProxyPort() == 80) {
            return this.getProxyHost();
        } 
        return this.getProxyHost() + ":" + this.getProxyPort();
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	STREAM UTILS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Copy the contents of the given InputStream to the given OutputStream.
     * Closes both streams when done.
     *
     * @param in  the stream to copy from
     * @param out the stream to copy to
     * @return the number of bytes copied
     * @throws IOException in case of I/O errors
     */
    private static long _copy(final InputStream in,
    						  final OutputStream out) throws IOException {
        try {
            int byteCount = 0;
            byte[] buffer = new byte[FOUR_KB];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
                byteCount += bytesRead;
            }
            out.flush();
            return byteCount;
        } finally {
            _close(in);
            _close(out);
        }
    }
    /**
     * Close the given stream if the stream is not null.
     *
     * @param s The stream
     */
    private static void _close(final InputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                log.error("Error closing stream: " + e.getMessage());
            }
        }
    }
    /**
     * Close the given stream if the stream is not null.
     *
     * @param s The stream
     */
    private static void _close(final OutputStream s) {
        if (s != null) {
            try {
                s.close();
            } catch (Exception e) {
                log.error("Error closing stream: " + e.getMessage());
            }
        }
    }
}