package r01f.httpclient.jsse.handler;

import java.io.IOException;
import java.net.URL;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocketFactory;
import sun.net.www.protocol.http.Handler;

// Referenced classes of package sun.net.www.protocol.https:
//            AbstractDelegateHttpsURLConnection

class DelegateHttpsURLConnection extends AbstractDelegateHttpsURLConnection {

	private HttpsURLConnection httpsURLConnection;
	
	@Override
	protected HostnameVerifier getHostnameVerifier() {
		return this.httpsURLConnection.getHostnameVerifier();
	}
	@Override
	protected SSLSocketFactory getSSLSocketFactory() {
		return this.httpsURLConnection.getSSLSocketFactory();
	}

	DelegateHttpsURLConnection(URL url, Handler handler, HttpsURLConnection httpsurlconnection) throws IOException {
		super(url, handler);
		this.httpsURLConnection = httpsurlconnection;
	}
}
