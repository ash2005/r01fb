package r01f.resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Loads a file from an URL via HTTP
 */
@Accessors(prefix="_")
public class ResourcesLoaderFromURL 
     extends ResourcesLoaderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	static final String PROXY_HOST_PROP = "proxyHost";
	static final String PROXY_PORT_PROP = "proxyPort";
	static final String PROXY_USER_PROP = "proxyUser";
	static final String PROXY_PASSWORD_PROP = "proxyPassword";
	static final String SERVER_CHARSET_PROP = "serverCharsetName";
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter @Setter private String _serverCharsetName = Charset.defaultCharset().name();	
	@Getter @Setter private String _proxyHost;
	@Getter @Setter private String _proxyPort;
	@Getter @Setter private UserCode _proxyUser;
	@Getter @Setter private Password _proxyPassword;
///////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	ResourcesLoaderFromURL(final ResourcesLoaderDef def) {
		super(def);
		if (CollectionUtils.hasData(def.getLoaderProps())) {
			String proxyHost = def.getProperty(PROXY_HOST_PROP);
			if (!Strings.isNullOrEmpty(proxyHost)) {
				_proxyHost = proxyHost;
				
				String proxyPort = def.getProperty(PROXY_PORT_PROP);
				_proxyPort = !Strings.isNullOrEmpty(proxyPort) ? proxyPort : "8080";
				
				String proxyUser = def.getProperty(PROXY_USER_PROP);
				String proxyPwd = def.getProperty(PROXY_PASSWORD_PROP);
				_proxyUser = !Strings.isNullOrEmpty(proxyUser) ? UserCode.forId(proxyUser) : null;
				_proxyPassword = !Strings.isNullOrEmpty(proxyPwd) ? Password.forId(proxyPwd) : null;
			}
			String serverCharsetName = def.getProperty(SERVER_CHARSET_PROP);
			_serverCharsetName = !Strings.isNullOrEmpty(serverCharsetName) ? serverCharsetName : null;
		}
	}
	@Override
	boolean _checkProperties(final Map<String,String> props) {
		boolean outOK = true;	// none of the properties are mandatory
		return outOK;
	}
///////////////////////////////////////////////////////////////////////////////
// 	METHODS
///////////////////////////////////////////////////////////////////////////////
	@Override
	public InputStream getInputStream(final String resourceUrl,
									  final boolean reload) throws IOException {
		try {
	        HttpURLConnection conx = _getURLConnection(resourceUrl,Charset.forName(this._serverCharsetName));
	        InputStream is = conx.getInputStream();
	        return is;
		} catch(IOException ioEx) {
			StringBuilder msg = new StringBuilder("Error when loading a resource from the url: " + resourceUrl);
			if (this._proxyHost != null) {
				msg.append("(");
				msg.append(" proxy host:port=" + this._proxyHost + ":" + this._proxyPort);
				msg.append(" proxy usr/pwd=" + this._proxyUser + "/" + this._proxyPassword);
				msg.append(")");
			}
			throw new IOException(msg + " > " + ioEx.getMessage());
		}
	}

	@Override
	public Reader getReader(final String resourceUrl,
							final boolean reload) throws IOException {
		return new InputStreamReader(this.getInputStream(resourceUrl,reload));
	}
    /**
     * Returns an {@link InputStream} to the resource 
     * @param url the url
     * @param charset the server {@link Charset}
     * @return the resource {@link InputStream}
     * @throws IOException 
     */
    private HttpURLConnection _getURLConnection(final String url,final Charset charset) throws IOException {
        HttpURLConnection conx = null;
        boolean useProxy = !Strings.isNullOrEmpty(this._proxyHost);
        if (useProxy) {
        	conx = HttpClient.forUrl(url).usingCharset(charset)
        				     .withConnectionTimeOut(10000)		// timeout de 10 sg
        					 .usingProxy(this._proxyHost,this._proxyPort,this._proxyUser,this._proxyPassword)
        					 .GET()
        					 .getConnection();
        } else {
        	conx = HttpClient.forUrl(url).usingCharset(charset)
        					  .withConnectionTimeOut(10000)		// timeout de 10 sg
        					  .GET()
        					  .getConnection();
        }
        return conx;
    }

}
