package r01f.httpclient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;

/**
 * Type in charge of retrieving a non-secure HTTP connection
 */
  class HttpConnectionRetriever 
extends ConnectionRetrieverBase {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  OVERRIDEN METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public HttpURLConnection _retrieveConnection(final String urlStr,
												 final String proxyHost,final String proxyPort,
												 final UserCode proxyUser,final Password proxyPassword) throws IOException {
		// Obtener una URL de la url en forma de texto
		final URL url = new URL(urlStr);
		
		HttpURLConnection conx = null;
		if (proxyHost != null && proxyPort != null) {
			Proxy proxy = new Proxy(Proxy.Type.HTTP,new InetSocketAddress(proxyHost,
																		  new Integer(proxyPort).intValue()));
			conx = (HttpURLConnection)url.openConnection(proxy);
		} else {
			conx = (HttpURLConnection)url.openConnection();
		}
		return conx;
		
	}
	
}
