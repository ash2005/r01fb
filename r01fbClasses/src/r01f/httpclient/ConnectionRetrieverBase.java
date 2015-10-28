package r01f.httpclient;

import java.io.IOException;
import java.net.HttpURLConnection;

import r01f.concurrent.TimeOutController;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;

abstract class ConnectionRetrieverBase {

///////////////////////////////////////////////////////////////////////////////
// PUBLIC INTERFACE
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a connection
	 * IMPORTANT!:
	 * The real work is delegated to the abstract _retrieveConnection method which must be implemented by
	 * concrete types:
	 * <ul>
	 * 		<li>{@link HttpConnectionRetriever}</li>
	 * 		<li>{@link HttpsConnectionRetriever}</li>
	 * </ul>
	 * 
	 * NOTE: To debug : -Djavax.net.debug=all
	 * 
	 * @param urlStr url to connect with
	 * @para timeout timeout to get the connection (this is not the timeout to get the response)
	 * @param proxyHost proxy host to use
	 * @param proxyPort proxy host port
	 * @param proxyUser proxy user
	 * @param proxyPassword proxy password
	 * @return the connection
	 * @throws IOException if a connection could not be retrieved
	 */
	public HttpURLConnection getConnection( final String urlStr,final long timeout,
											final String proxyHost,final String proxyPort,
											final UserCode proxyUser,final Password proxyPassword) throws IOException {
		HttpURLConnection outConx = null;
		if (timeout < 0) {
			outConx = _retrieveConnection(urlStr,
										  proxyHost,proxyPort,proxyUser,proxyPassword);
		} else {
			try {
				ObtainConnectionTask task = new ObtainConnectionTask() {
					@Override
					public void doit() throws IOException {
						this.conx = _retrieveConnection(urlStr,
														proxyHost,proxyPort,proxyUser,proxyPassword);
					}
				};
				TimeOutController.execute(task,timeout);
				outConx = task.conx;
				if (task.ioException != null) {
					throw task.ioException;
				}
			} catch (TimeOutController.TimeoutException timeOutEx) {
				throw new IOException("No se ha podido obtener la conexión con el host '" + urlStr + "' en el tiempo especificado: " + timeout + " millis");
			}
		}
		return outConx;
	}
	
///////////////////////////////////////////////////////////////////////////////
//	ABSTRACT METHODS
///////////////////////////////////////////////////////////////////////////////
	/** 
	 * The real method where the the connection is getted
	 * This is implemented by concrete types:
	 * <ul>
	 * 		<li>{@link HttpConnectionRetriever}</li>
	 * 		<li>{@link HttpsConnectionRetriever}</li>
	 * </ul>
	 * 
	 * @param urlStr url to connect with
	 * @para timeout timeout to get the connection (this is not the timeout to get the response)
	 * @param proxyHost proxy host to use
	 * @param proxyPort proxy host port
	 * @param proxyUser proxy user
	 * @param proxyPassword proxy password
	 * @return the connection
	 * @throws IOException if a connection could not be retrieved
	 */
	public abstract HttpURLConnection _retrieveConnection(final String urlStr,
														  final String proxyHost,final String proxyPort,
														  final UserCode proxyUser,final Password proxyPassword) throws IOException;

///////////////////////////////////////////////////////////////////////////////
// AUX TYPE TO HANDLE WITH CONNECTION TIMEOUT
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Aux type that encloses the connection timeout
	 */
	abstract class ObtainConnectionTask 
	    implements Runnable {
		
		public java.net.HttpURLConnection conx;
		public IOException ioException;
		/**
		 * Runs whatever
		 * @throws IOException if an error occurs..
		 */
		public abstract void doit() throws IOException;
		@Override
		public void run() {
			try {
				this.doit();		// Normally here a connection is retrieved
			} catch (IOException ioEx) {
				ioException = ioEx;
			}
		}
	}
	
}
