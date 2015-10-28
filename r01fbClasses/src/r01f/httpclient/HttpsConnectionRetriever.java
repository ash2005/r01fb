package r01f.httpclient;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.Provider;
import java.security.Security;
import java.util.Properties;

import javax.net.ssl.SSLSocketFactory;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;

/**
 * Type in charge of retrieving a securs server connection (HTTPS)
 */
public class HttpsConnectionRetriever 
     extends ConnectionRetrieverBase {
///////////////////////////////////////////////////////////////////////////////
// MIEMBROS
///////////////////////////////////////////////////////////////////////////////
	private boolean _streamHandlerInitialized = false;   // Indica si se ha establecido el stream handler

///////////////////////////////////////////////////////////////////////////////
// CONSTANTES
///////////////////////////////////////////////////////////////////////////////
	// Different HTTPS imps
	private static String  _httpsDefaultConnectionClass ="r01f.httpclient.jsse.handler.HttpsURLConnectionImpl";
	//private static String  _httpsDefaultConnectionClass ="javax.net.ssl.HttpsURLConnection";
	private static String  _httpsSunConnectionClass = "com.sun.net.ssl.HttpsURLConnection";
	private static String  _httpsIBMConnectionClass = "com.ibm.net.ssl.HttpsURLConnection";

	// Different HTTPS StreamHandlers
	private static String  _defaultURLStreamHandler = "r01f.httpclient.jsse.handler.Handler";
	//private static String  _sunURLStreamHandler = "com.sun.net.ssl.internal.www.protocol.https.Handler";
	//private static String  _ibmURLStreamHandler = "com.ibm.net.ssl.internal.www.protocol.https.Handler";
	//private static String  _ibmURLStreamHandler = "com.ibm.net.ssl.www2.protocol.https.Handler";
	
///////////////////////////////////////////////////////////////////////////////
// METODOS
///////////////////////////////////////////////////////////////////////////////
	@Override
	public HttpURLConnection _retrieveConnection(final String urlStr,final String proxyHost,final String proxyPort,
												 final UserCode proxyUser,final Password proxyPassword) throws IOException {
		
		URLStreamHandler streamHandler = _getURLStreamHandler();
		URL theURL = new URL(null,urlStr,streamHandler);
		theURL = new URL(theURL,theURL.toExternalForm(),streamHandler);	// Wrap to JSEE Handler (IBM or SUN) & Do HandShake */
		
		URLConnection conx = theURL.openConnection();
		
		if (proxyHost != null) {
			// Obtener el tipo de conexión para determinar si hay que envolver la url
			String connectionClassName = _httpsDefaultConnectionClass;

			// Invocar al método setSSLSocketFactory en la conexión pasando la tunnelSocketFactory
			SSLSocketFactory tunnelSocketFactory = new SSLTunnelSocketFactory(proxyHost,proxyPort,proxyUser,proxyPassword);
			_invokeSSLFactoryMethod(connectionClassName,
									conx,
									tunnelSocketFactory);
		}
		return (HttpURLConnection)conx;
	}
	
///////////////////////////////////////////////////////////////////////////////
// PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtiene el URL Stream Handler para la maquina virtual dada.
	 * @return
	 * @throws IOException
	 */
	private static URLStreamHandler _getURLStreamHandler () throws IOException {
		ClassLoader cl = Thread.currentThread().getContextClassLoader();	// No utilizar Class.forName... problemas en los EAR
		String streamHandlerClass = _defaultURLStreamHandler;
		try {
			return (URLStreamHandler)cl.loadClass(streamHandlerClass).newInstance();
		} catch (ClassNotFoundException cnfEx) {
			throw new IOException("streamHandlerSSL type could not be found: '" + streamHandlerClass + "': " + cnfEx.getMessage(),cnfEx);
		} catch (InstantiationException instEx) {
			throw new IOException("streamHandlerSSL type instance could not be created '" + streamHandlerClass + "': " + instEx.getMessage(),instEx);
		} catch (IllegalAccessException illAccEx) {
			throw new IOException("streamHandlerSSL type illegal access '" + streamHandlerClass + "': " + illAccEx.getMessage(),illAccEx);
		}
	}
	/**
	 * Invoca el metodosetSSLSocketFactory(
	 * 		SSLSocketFactory sslSocketFactory = new SSLTunnelSocketFactory(proxyHost,proxyPort,proxyUser,proxyPassword);
	 * 		connectionClass.setSSLSocketFactory(sslSocketFactory);
	 * @param connectionClassName
	 * @param conx
	 * @param proxyHost
	 * @param _proxyPort
	 * @param proxyUser
	 * @param proxyPassword
	 * @throws IOException
	 */
	private static void _invokeSSLFactoryMethod(final String connectionClassName,
												final URLConnection conx,
												final SSLSocketFactory theSocketFactory) throws IOException {
		// Invocar el método setSSLSocketFactory en la conexión
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();	// No utilizar Class.forName... problemas en los EAR
			Method setSSLSocketFactoryMethod = cl.loadClass(connectionClassName).getMethod("setSSLSocketFactory",new Class[] {SSLSocketFactory.class});
			setSSLSocketFactoryMethod.invoke(conx,new Object[] {theSocketFactory});
		} catch (ClassNotFoundException cnfEx) {
			cnfEx.printStackTrace(System.out);
			throw new IOException("getSSLMethodByClassName" + "className:" + connectionClassName + cnfEx.getMessage(),cnfEx);
		} catch (SecurityException secEx) {
			throw new IOException("getSSLMethodByClassName" + "className:" + connectionClassName + secEx.getMessage(),secEx);
		} catch (NoSuchMethodException nsmEx) {
			nsmEx.printStackTrace(System.out);
			throw new IOException("getSSLMethodByClassName" + "className:" + connectionClassName + nsmEx.getMessage(),nsmEx);
		} catch (InvocationTargetException invTgtEx) {
			invTgtEx.printStackTrace(System.out);
			throw new IOException("getSSLMethodByClassName" + "className:" + connectionClassName + invTgtEx.getMessage(),invTgtEx);
		} catch (IllegalAccessException illAccEx) {
			illAccEx.printStackTrace(System.out);
			throw new IOException("getSSLMethodByClassName" + "className:" + connectionClassName + illAccEx.getMessage(),illAccEx);
		}
	}
	@SuppressWarnings("unused")
	private static boolean _isHttpsDefaultConnectionInstance(final Class<?> classInstance){
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();	// No utilizar Class.forName... problemas en los EAR
			Class<?> httpsDefaultConnectionClass = cl.loadClass(_httpsDefaultConnectionClass);
			return httpsDefaultConnectionClass.isAssignableFrom(classInstance);
		} catch (ClassNotFoundException cnfEx) {
			return false;
		}
	}
	@SuppressWarnings("unused")
	private static boolean _isSunConnectionInstance(final Class<?> classInstance){
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();	// No utilizar Class.forName... problemas en los EAR
			Class<?> sunConnectionClass = cl.loadClass(_httpsSunConnectionClass);
			return sunConnectionClass.isAssignableFrom(classInstance);
		} catch (ClassNotFoundException cnfEx) {
			return false;
		}
	}
	@SuppressWarnings("unused")
	private static boolean _isIBMClassInstance(final Class<?> classInstance){
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();	// No utilizar Class.forName... problemas en los EAR
			Class<?> ibmConnectionClass = cl.loadClass(_httpsIBMConnectionClass);
			return ibmConnectionClass.isAssignableFrom(classInstance);
		} catch (ClassNotFoundException cnfEx) {
			return false;
		}
	}
	/**
	 * Carga el Stream Handler de SSL
	 */
	@SuppressWarnings("unused")
	private void _loadStreamHandler() {
		if( !_streamHandlerInitialized ) {
			_streamHandlerInitialized = false;

			String szVendor = System.getProperty("java.vendor");
			String szVersion = System.getProperty("java.version");
			// Se asume que la cadena de version tiene la forma [major].[minor].[release] (ej: 1.2.1)
			Double dVersion = new Double(szVersion.substring(0,3));

			// Si se esta en un entorno Microsoft utilizar el stream handler de Micro$oft
			if( -1 < szVendor.indexOf("Microsoft") ) {
				try {
					ClassLoader cl = Thread.currentThread().getContextClassLoader();	// No utilizar Class.forName... problemas en los EAR
					Class<?> clsFactory = cl.loadClass("com.ms.net.wininet.WininetStreamHandlerFactory");
					if ( null != clsFactory) {
						URL.setURLStreamHandlerFactory((URLStreamHandlerFactory)clsFactory.newInstance());
					}
					// Si el Stream Handler Factory se ha creado correctamente, asegurarse de que se actualiza el flag
					_streamHandlerInitialized = true;
				} catch( ClassNotFoundException cfe ) {
					throw new RuntimeException( "No se puede cargar el Stream Handler para SSL de Microsoft. Verifica el classpath para poder acceder a com.ms.net.wininet.WininetStreamHandlerFactory"  + cfe.toString() );
				} catch ( InstantiationException instEx ) {
					throw new RuntimeException( "No se puede instanciar el Stream Handler SSL de Microsoft: " + instEx.toString() );
				} catch ( IllegalAccessException illAccEx ) {
					throw new RuntimeException( "Aceso ilegal al Stream Handler SSL de Microsoft: " + illAccEx.toString() );
				} catch( Exception ex ) {
					throw new RuntimeException( "Error desconocido al cargar la clase StreamHandler SSL de Microsoft: " + ex.toString() );
				}
			} else if( 1.2 <= dVersion.doubleValue() ) {
				// Registra un el protocolHandler para ssl.
				// Normalmente esta parte no es necesario hacerla ya que de esto se encarga
				// WLS al iniciarse si la propiedad weblogic.security.ssl.enable=true
				// Simplemente se incluye weblogic.net en la propiedad del sistema
				// java.protocol.handler.pkgs
				
				final String JSSE_HANDLER = "com.sun.net.ssl.internal.www.protocol";
				final String WLS_HANDLER = "weblogic.net";
				
				// Detectar si se esta utilizando Weblogic o jsse
				String handler = (System.getProperty("weblogic.class.path") != null) ? WLS_HANDLER : JSSE_HANDLER;
				
				Properties sysProps = System.getProperties();
				String handlerValue = sysProps.getProperty("java.protocol.handler.pkgs");
				// Comprobar si en la propiedad estaba establecido el handler
				if (handlerValue == null) {
					handlerValue = handler;
				} else if (handlerValue.indexOf(handler) == -1) {
					handlerValue += ("|" + handler);
				}
				sysProps.put("java.protocol.handler.pkgs", handlerValue);
				System.setProperties(sysProps);
				
				// Si el J2EE provider esta disponible y NO se ha establecido, hacerlo
				// y añadirlo como un nuevo proveedor en la clase de seguridad
				if (handler.equals(WLS_HANDLER)) return;    // No hace falta registrar el provider
				try {
					ClassLoader cl = Thread.currentThread().getContextClassLoader();				// No utilizar Class.forName... problemas en los EAR
					Class<?> clsFactory = cl.loadClass("com.sun.net.ssl.internal.ssl.Provider");
					if( (null != clsFactory) && (null == Security.getProvider("SunJSSE")) ) {
						Security.addProvider((Provider)clsFactory.newInstance());
					}
					// Si el Stream Handler Factory se ha creado correctamente, asegurarse de que se actualiza el flag
					_streamHandlerInitialized = true;
				} catch( ClassNotFoundException cfe ) {
					throw new RuntimeException( "No se ha podido cargar el Stream Handler de J2EE, verifica que la clase com.sun.net.ssl.internal.ssl.Provider esta accesible en el classpath"  + cfe.getMessage() );
				} catch ( InstantiationException instEx ) {
					throw new RuntimeException( "No se puede instanciar el Stream Handler SSL de de J2EE: " + instEx.getMessage() );
				} catch ( IllegalAccessException illAccEx ) {
					throw new RuntimeException( "Aceso ilegal al Stream Handler SSL de J2EE: " + illAccEx.getMessage() );
				} catch( Exception ex ) {
					throw new RuntimeException( "Error desconocido al cargar la clase StreamHandler SSL: " + ex.getMessage() );
				}
			}
		}
	}
}
