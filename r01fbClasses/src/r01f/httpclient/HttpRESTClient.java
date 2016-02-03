/**
 *
 */
package r01f.httpclient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import r01f.exceptions.Throwables;
import r01f.mime.MimeType;
import r01f.types.url.Url;
import r01f.util.types.collections.CollectionUtils;

/**
 * Aux type to do REST client-calls.
 * Una característica completamente alineada con REST del protocolo HTTP es el hecho de que tenga
 * una interfaz uniforme para todos los recursos web. HTTP define un conjunto predefinido y cerrado
 * de acciones o métodos HTTP. Es importante tener en cuenta que la propia especificación define los
 * conceptos de seguridad e idempotencia, y clasifica los métodos conforme a estos dos criterios.
 *
 * Un método se considera seguro si no produce efectos secundarios. Por efecto secundario se entiende
 * cualquier modificación del estado del servidor, o interacción de éste con cualquier otro sistema, que
 * produzca efectos perceptibles por el usuario. Normalmente sólo los métodos que representan lectura
 * se consideran seguros.
 *
 * Un método es idempotente si la ejecución repetida de éste, con exactamente los mismos parámetros,
 * tiene el mismo efecto que si sólo se hubiera ejecutado una vez. Esta propiedad nos permite reintentar
 * con seguridad una petición una y otra vez, y tener la seguridad de que la operación no se va a duplicar.
 *
 * Los métodos (o también llamados verbos) HTTP usados com más frecuencia son los siguientes:
 * Método     Seguro Idempotente 	Semántica
 * ------	  ------ -----------    ---------
 *  GET 		Sí 		Sí 			Leer el estado del recurso
 *  HEAD 		Sí 		Sí 			Leer, pero sólo las cabeceras
 *  PUT 		No 		Sí 			Actualizar o crear
 *  DELETE 		No 		Sí 			Eliminar un recurso
 *  POST 		No 		No 			Cualquier acción genérica no idempotente
 *  OPTIONS 	Sí 		Sí 			Averiguar las opciones de comunicación disponibles de un recurso
 *
 * En general el uso de cada método es bastante explicativo. Sin embargo, conviene aclarar algunos aspectos.
 *
 * El primero es la diferencia entre HEAD y GET. Ambos leen el recurso, pero el segundo devuelve tanto
 * los datos del recurso web, como las cabeceras HTTP, mientras que el primero sólo las cabeceras.
 *
 * Por otro lado el método POST es bastante misterioso y objeto de frecuentes malentendidos. En general
 * se usa para crear un nuevo recurso, modificar uno existente o para ejecutar una acción genérica que
 * no sea idempotente como realizar una transacción monetaria. Como veis la semántica de PUT parece
 * que se solapa con la de POST de alguna manera, y esto es fuente de numerosos malentendidos.
 *
 * El método OPTIONS se usa para determinar las opciones de comunicación de un recurso, tales como
 * qué métodos HTTP podemos usar contra esa URI. El resultado de OPTIONS no es cacheable, ya que
 * el conjunto de métodos que podemos usar puede cambiar con el estado en el que se encuentre el
 * recurso. Junto con HEAD nos permiten descubrir automáticamente cómo podemos comunicarnos con
 * el servidor.
 *
 * Existen otros métodos que no suelen ser muy usados en servicios REST, como CONNECT o TRACE.
 */
public class HttpRESTClient {
	/**
	 * Makes a GET call to an URL, read the state of web resource, returns web resource data and HTTP headers.
	 * @param url the url to GET
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @return the server-returned result
	 */
	public static HttpResponse doGET(final Url url,final Map<String,String> urlParameters,
									 final Map<String, String> headers) {
		HttpResponse outResponse = null;
		try {
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.GET()
										.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a HEAD call to an URL, read state of web resource, but only returns HTTP headers, not resource data.
	 * @param url the url to HEAD
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @return the server-returned result

	 */
	public static HttpResponse doHEAD(final Url url,final Map<String,String> urlParameters,
									  final Map<String,String> headers) {
		HttpResponse outResponse = null;
		try {
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.HEAD()
										.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500,new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a DELETE call to an URL, delete resource.
	 * @param url the url to DELETE
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @return the server-returned result
	 */
	public static HttpResponse doDELETE(final Url url,final Map<String,String> urlParameters,
										final Map<String,String> headers) {
		HttpResponse outResponse = null;
		try {
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.DELETE()
										.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a PUT call to an URL, create or update web resource (idempotent).
	 * @param url the url to PUT
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the PUTed data
	 * @return the server-returned result
	 */
	public static HttpResponse doPUT(final Url url,final Map<String,String> urlParameters,
									  final Map<String,String> headers,
									  final InputStream postPayload) {
		HttpResponse outResponse = HttpRESTClient.doPUT(url,urlParameters,
														headers,
														postPayload,MimeType.forName("application/xml"));
		return outResponse;
	}
	/**
	 * Makes a PUT call to an URL, create or update web resource (idempotent).
	 * @param url the url to PUT at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the PUTed data
	 * @param mimeType the content-type of the posted data (if null application/xml is assumed)
	 * @return the server-returned result
	 */
	public static HttpResponse doPUT(final Url url,final Map<String,String> urlParameters,
									 final Map<String,String> headers,
									 final InputStream postPayload,
									 final MimeType mimeType) {
		HttpResponse outResponse = null;
		try {
			MimeType theMimeType = mimeType != null ? mimeType
													: MimeType.forName("application/xml");
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.PUT().withPayload(HttpRequestPayload.wrap(postPayload)
																		 .mimeType(theMimeType))
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a PUT call to an URL sending a FORM in the payload, create or update web resource (idempotent).
	 * @param url the url to PUT at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param formParams the PUTed form data
	 * @return the server-returned result
	 */
	public static HttpResponse doPUTForm(final Url url,final Map<String,String> urlParameters,
										 final Map<String,String> headers,
										 final Map<String,String> formParams) {
		HttpResponse outResponse = null;
		try {
			List<HttpRequestFormParameter> postFormParams = null;
			if (CollectionUtils.hasData(formParams)) {
				postFormParams = Lists.newArrayListWithExpectedSize(formParams.size());
				for (Map.Entry<String,String> me : formParams.entrySet()) {
					postFormParams.add(HttpRequestFormParameterForText.of(me.getValue())
															  .withName(me.getKey()));
				}
			}
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.PUTForm().withPUTFormParameters(postFormParams)
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a POST call to an URL, create or update web resource (not idempotent action).
	 * @param url the url to POST at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the POSTed data
	 * @param mimeType the content-type of the posted data (if null application/xml is assumed)
	 * @return the server-returned result
	 */
	public static HttpResponse doPOST(final Url url,final Map<String,String> urlParameters,
									  final Map<String,String> headers,
									  final InputStream postPayload,
									  final MimeType mimeType) {
		HttpResponse outResponse = null;
		try {
			MimeType theMimeType = mimeType != null ? mimeType
													: MimeType.APPLICATION_XML;
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.POST()
										.withPayload(HttpRequestPayload.wrap(postPayload)
																	   .mimeType(theMimeType))
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
	/**
	 * Makes a POST call to an URL, create or update web resource (not idempotent action).
	 * @param url the url to POST at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param postPayload the POSTed data
	 * @return the server-returned result
	 */
	public static HttpResponse doPOST(final Url url,final Map<String,String> urlParameters,
									  final Map<String,String> headers,
									  final InputStream postPayload) {
		HttpResponse outResponse = HttpRESTClient.doPOST(url,urlParameters,
														 headers,
														 postPayload,MimeType.APPLICATION_XML);
		return outResponse;
	}
	/**
	 * Makes a POST call to an URL sending a FORM in the payload, create or update web resource (not idempotent action).
	 * @param url the url to POST at
	 * @param urlParameters the parameters to encode at the url query string
	 * @param headers the HTTP headers
	 * @param formParams the POSTed form data
	 * @return the server-returned result
	 */
	public static HttpResponse doPOSTForm(final Url url,final Map<String,String> urlParameters,
										  final Map<String,String> headers,
										  final Map<String,String> formParams) {
		HttpResponse outResponse = null;
		try {
			List<HttpRequestFormParameter> postFormParams = null;
			if (CollectionUtils.hasData(formParams)) {
				postFormParams = Lists.newArrayListWithExpectedSize(formParams.size());
				for (Map.Entry<String,String> me : formParams.entrySet()) {
					postFormParams.add(HttpRequestFormParameterForText.of(me.getValue())
															  .withName(me.getKey()));
				}
			}
			outResponse = HttpClient.forUrl(url)	// url-encode los parametros! en otro caso NO funciona con jersey
									.withURLParameters(_composeUrlParameters(urlParameters))
									.notUsingProxy()
									.disablingProxyCache()
									// .withConnectionTimeOut(20000)
									.withHeaders(headers)
									.POSTForm().withPOSTFormParameters(postFormParams)
									.getResponse();
		} catch (IOException ioEx) {
			outResponse = new HttpResponse(500, new ByteArrayInputStream(Throwables.getStackTraceAsString(ioEx).getBytes()));
		}
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// PRIVATE METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	private static List<HttpRequestURLEncodedParameter> _composeUrlParameters(final Map<String,String> urlParameters) {
		List<HttpRequestURLEncodedParameter> outParams = null;
		if (CollectionUtils.hasData(urlParameters)) {
			outParams = Lists.newArrayListWithExpectedSize(urlParameters.size());
			for (Map.Entry<String,String> me :  urlParameters.entrySet()) {
				outParams.add(HttpRequestURLEncodedParameter.of(me.getValue())
															.withName(me.getKey()));
			}
		}
		return outParams;
	}
}
