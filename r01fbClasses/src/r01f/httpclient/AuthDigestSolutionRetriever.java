package r01f.httpclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.MalformedChallengeException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.protocol.BasicHttpContext;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient.RequestMethod;
import r01f.httpclient.HttpResponse.HttpResponseCode;
import r01f.util.types.collections.CollectionUtils;


/**
 * Tipo auxiliar responsable de obtener el valor que hay que poner en la cabecera HTTP de la request "Authorization"
 * cuando el servidor requiere una autenticacion 
 * Ver http://en.wikipedia.org/wiki/Digest_access_authentication
 */
public class AuthDigestSolutionRetriever {
	/**
	 * Obtiene el valor que hay que poner en la cabecera "Authorization" de la request HTTP
	 * @param code cód
	 * @param headers
	 * @param requestMethod
	 * @param url
	 * @param authUser
	 * @param authPwd
	 * @return
	 */
	public static String getAuthorizationHeaderValue(final HttpURLConnection serverConnectionNoAuth,
									 				 final RequestMethod requestMethod,final String strUrl,
									 				 final UserCode authUser,final Password authPwd) throws IOException,
									 				 														AuthenticationException,
									 				 													 	MalformedChallengeException {
		// A org.apache.http.impl.auth.DigestScheme instance is
		// what will process the challenge from the web-server
		final DigestScheme md5Auth = new DigestScheme();
		// Validate that we got an HTTP 401 back
		HttpResponseCode serverNoAuthResponseCode = HttpResponseCode.of(serverConnectionNoAuth.getResponseCode());
		if (serverNoAuthResponseCode.is(HttpResponseCode.UNAUTHORIZED)) {
			if (CollectionUtils.isNullOrEmpty(serverConnectionNoAuth.getHeaderFields())) throw new IllegalStateException("HTTP Headers not received!");
			// headers normalization
			Map<String,String> headersKeys = _normalizeHeadersKeys(serverConnectionNoAuth.getHeaderFields());
			if (headersKeys.containsKey("WWW-AUTHENTICATE")) {
				// [1] Obtener un objeto HttpRequest a partir de la URL y el método (GET/POST/PUT/DELETE)
				java.net.URL url = null;
				try {
					url = new URL(strUrl);
				} catch (MalformedURLException e1) {
					throw new IllegalStateException("The url is malformed");
				}
				HttpRequestBase commonsHttpRequest = _commonsHttpClientRequestFrom(requestMethod,url.getPath());
				
				// [2] Generate a solution Authentication header using the username and password.
				// 2.1 Get the challenge and solve.
				String challenge = serverConnectionNoAuth.getHeaderFields()
													   	 .get(headersKeys.get("WWW-AUTHENTICATE"))
													   	 .get(0);
				commonsHttpRequest.addHeader(headersKeys.get("WWW-AUTHENTICATE"),
											 challenge);
				md5Auth.processChallenge(commonsHttpRequest.getHeaders(headersKeys.get("WWW-AUTHENTICATE"))[0]);
				
				// 2.2 Compose a Header object for the "Authorization" header
				Header solution = md5Auth.authenticate(new UsernamePasswordCredentials(authUser.asString(),
																					   authPwd.asString()),
													   commonsHttpRequest,
													   new BasicHttpContext());
				return solution.getValue();		// the value of the composed Authorization header
				
			} 
			throw new IllegalStateException("A 401 response (unauthorized) has been received, but NO WWW-Authenticate header in this response!");
		} 
		throw new IllegalStateException("The request is supossed to be authenticated but the server response code was NOT 401 (unauthorized)");
	}
	/**
	 * Normaliza el nombre de las cabeceras devolviendo un mapa que relaciona la cabecera en MAYUSCULAS
	 * con la cabecera recibida
	 * @param headers los headers recibidos
	 * @return las claves de las cabeceras normalizadas
	 */
	private static Map<String,String> _normalizeHeadersKeys(final Map<String,List<String>> headers) {
		Map<String,String> headersKeys = new HashMap<String,String>(headers.size());
		Iterator<String> itKeys = headers.keySet().iterator();
		while (itKeys.hasNext()) {
			String key = itKeys.next();
			if (key == null) continue;
			headersKeys.put(key.toUpperCase(),key);
		}
		return headersKeys;
	}
	/**
	 * Crea un objeto {@link HttpRequestBase} de commons HTTPClient a partir del método {@link RequestMethod} de R01F
	 * @param requestMethod el método request
	 * @param url la url
	 * @return el objeto {@link HttpRequestBase} de HTTPCommons equivalente a R01
	 */
	private static HttpRequestBase _commonsHttpClientRequestFrom(final RequestMethod requestMethod,
												   		  		 final String url) {
		HttpRequestBase outHttpReq = null;
		if (requestMethod.equals(RequestMethod.POST)) {
			outHttpReq = new HttpPost(url);
		} else if (requestMethod.equals(RequestMethod.GET)) {
			outHttpReq = new HttpGet(url);
		} else if (requestMethod.equals(RequestMethod.PUT)) {
			outHttpReq = new HttpPut(url);
		} else if (requestMethod.equals(RequestMethod.DELETE)) {
			outHttpReq = new HttpDelete(url);
		} else {
			throw new IllegalArgumentException("The http request method is NOT a valid one (GET, POST, PUT, DELETE).");
		}
		return outHttpReq;
	}
}
