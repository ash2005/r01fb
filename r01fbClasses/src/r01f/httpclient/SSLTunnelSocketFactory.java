package r01f.httpclient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.codec.binary.Base64;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;

/** 
 * Esta clase se utilizará cada vez que se quiera conectar a una url https através de Proxy.
 * Realiza el HandShake entre un Cliente, el Servidor destino através de tunneling(proxy.)
 */
public class SSLTunnelSocketFactory 
     extends SSLSocketFactory {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static String USER_AGENT = _composeUserAgent();
	/**
	 * An alternative to access sun.net.www.protocol.http.HttpURLConnection.userAgent that require a compile-time dependency 
	 * to jvm rt.jar (see http://grepcode.com/file/repository.grepcode.com/java/root/jdk/openjdk/6-b14/sun/net/www/protocol/http/HttpURLConnection.java#HttpURLConnection.0userAgent)
	 * @return
	 */
	private static String _composeUserAgent() {
		String version = System.getProperty("java.version");	// java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("java.version"));
        String agent = System.getProperty("http.agent"); 		// java.security.AccessController.doPrivileged(new sun.security.action.GetPropertyAction("http.agent"));
        if (agent == null) {
        	agent = "Java/" + version;
        } else {
        	agent = agent + " Java/"+version;
        }
        return agent;
	}
///////////////////////////////////////////////////////////////////////////////
// FIELDS
///////////////////////////////////////////////////////////////////////////////
	private SSLSocketFactory _dfactory;		// Clase factory para la creación de Sockets
	private String _proxyHost;				// Host/Proxy
	private int _proxyPort;					// Port/Proxy
	private UserCode _proxyUser;			// User
	private Password _proxyPassword;		// Password

///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////
	public SSLTunnelSocketFactory (final String proxyHost,final String proxyPort,
								   final UserCode usr,final Password pwd) {
		_proxyUser = usr;
		_proxyPassword = pwd;
		_proxyHost = proxyHost;
		_proxyPort = Integer.parseInt(proxyPort);        
		_dfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
	}
	
///////////////////////////////////////////////////////////////////////////////
// METODOS
///////////////////////////////////////////////////////////////////////////////
	@Override
	public Socket createSocket(final String remoteHost,final int remotePort) throws IOException, 
																					UnknownHostException {
		return this.createSocket((Socket)null,
								 remoteHost,remotePort,true);
	}
	@Override
	public Socket createSocket(final InetAddress remoteHostAddr,final int remotePort) throws IOException {
		return this.createSocket((Socket)null,
								 remoteHostAddr.getHostName(),remotePort,true);
	}
	@Override
	public Socket createSocket(final String remoteHost,final int remotePort,
							   final InetAddress proxyAddr,final int proxyPort) throws IOException, 
							   														   UnknownHostException {
		if (proxyAddr != null) _proxyHost = proxyAddr.getHostName();
		_proxyPort = proxyPort;
		return this.createSocket((Socket)null,
								 remoteHost,remotePort,true);
	}
	@Override
	public Socket createSocket(final InetAddress remoteHostAddr,final int remotePort,
							   final InetAddress proxyAddr,final int proxyPort) throws IOException {
		if (proxyAddr != null) _proxyHost = proxyAddr.getHostName();
		_proxyPort = proxyPort;
		return this.createSocket((Socket)null,
								 remoteHostAddr.getHostName(),remotePort,true);
	}
	@Override @SuppressWarnings("resource")
	public Socket createSocket(final Socket socket,
							   final String remoteHost,final int remotePort,
							   final boolean flag) throws IOException, 
							   							  UnknownHostException {
		Socket proxySocket = socket != null ? socket 
											: new Socket(_proxyHost,_proxyPort);
		_doTunnelHandshake(proxySocket,remoteHost,remotePort);
		SSLSocket sslsocket = (SSLSocket)_dfactory.createSocket(proxySocket,
																remoteHost,remotePort,
																flag);
		return sslsocket;
	}
	@Override
	public String[] getDefaultCipherSuites() {
		return _dfactory.getDefaultCipherSuites();
	}
	@Override
	public String[] getSupportedCipherSuites() {
		return _dfactory.getSupportedCipherSuites();
	}
	@Override
	public String toString() {
		return "  <SSLTunnelSocketFactory proxyPort=" + _proxyPort + " proxyHost=" + _proxyHost + " delegate=" + _dfactory + "/>";
	}
	public void setDelegateFactory(final SSLSocketFactory sslsocketfactory) {
		_dfactory = sslsocketfactory;
	}
	public void setProxyAuth(final UserCode usr,final Password pwd) {
		_proxyUser = usr;
		_proxyPassword = pwd;
	}
	
///////////////////////////////////////////////////////////////////////////////
// PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Realiza la comunicación SSL entre el proxy y el host remoto.
	 * @param proxySocket socket con el proxy
	 * @param host host a acceder a través del proxy
	 * @param port puerto del host a acceder a través del proxy
	 * @throws IOException si NO se puede acceder al host a través del proxy
	 */
	@SuppressWarnings("resource")
	private void _doTunnelHandshake(final Socket proxySocket,
									final String host,final int port) throws IOException {
		// No usar la anotación @Cleanup, ya que provoca la excepción:
		//     java.net.SocketException: Socket is closed
		OutputStream outputstream = proxySocket.getOutputStream();
		String s1 = "";
		if (_proxyUser != null) {
			// Usar la función encode, ya que encodeBuffer provoca una excepción del tipo:
			//     java.io.EOFException: SSL peer shut down incorrectly
			s1 = "Proxy-Authorization: Basic " + Base64.encodeBase64String((_proxyUser + ":" + _proxyPassword).getBytes()) + "\r\n";
		}
		String s2 = "CONNECT " + host + ":" + port + " HTTP/1.0\n" + s1 + "User-Agent: " + USER_AGENT + "\r\n\r\n";
		
		byte abyte0[];
		try {
			abyte0 = s2.getBytes("ASCII7");
		} catch (UnsupportedEncodingException unsupportedencodingexception) {
			abyte0 = s2.getBytes();
		}
		outputstream.write(abyte0);
		outputstream.flush();
		byte abyte1[] = new byte[200];
		int j = 0;
		int k = 0;
		boolean flag = false;
		
		// Do not use @Cleanup annotation since it forces 
		// java.net.SocketException: Socket is closed
		InputStream inputstream = proxySocket.getInputStream();
		do {
			if (k >= 2) break;
			int l = inputstream.read();
			if (l < 0) throw new IOException("Unexpected EOF from proxy");
			if (l == 10) {
				flag = true;
				k++;
			} else if (l != 13) {
				k = 0;
				if (!flag && j < abyte1.length) abyte1[j++] = (byte)l;
			}
		} while (true);
		
		String s3;
		try {
			s3 = new String(abyte1,0,j,"ASCII7");
		} catch (UnsupportedEncodingException unsupportedencodingexception1) {
			s3 = new String(abyte1, 0, j);
		}		
		if (s3.toLowerCase().indexOf(" 200 ") == -1) throw new IOException("Unable to tunnel through " + _proxyHost + ":" + _proxyPort + ".  Proxy returns \"" + s3 + "\"");
	}
}
