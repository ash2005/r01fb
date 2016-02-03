package r01f.httpclient;

import java.io.Serializable;

import com.google.common.annotations.GwtIncompatible;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.types.url.Url;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Proxy info
 */
@Slf4j
@Accessors(prefix="_")
public class HttpClientProxySettings
  implements Debuggable,
  			 Serializable {

	private static final long serialVersionUID = -4581831883858484268L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final HttpClientProxySettings NO_PROXY = new HttpClientProxySettings();
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Getter @Setter private boolean _enabled = true;
	@Getter @Setter private Url _proxyUrl;
	@Getter @Setter private UserCode _userCode;
	@Getter @Setter private Password _password;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public HttpClientProxySettings() {
		// no args constructor
	}
	public HttpClientProxySettings(final boolean enabled) {
		_enabled = enabled;
	}
	public HttpClientProxySettings(final Url proxyUrl,
								   final UserCode userCode,final Password password) {
		_enabled = true;
		_proxyUrl = proxyUrl;
		_userCode = userCode;
		_password = password;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public CharSequence debugInfo() {
		String outDbg = null;
		if (_enabled) {
			outDbg = Strings.of("ENABLED [{} {}/{}]",_proxyUrl.asStringNotUrlEncodingQueryStringParamsValues())
					.customizeWith(_userCode,_password)
					.asString();
		} else {
			outDbg = "DISABLED";
		}
		return outDbg;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONFIG LOAD
/////////////////////////////////////////////////////////////////////////////////////////
	@GwtIncompatible("GWT does not supports XML parsing")
	public static HttpClientProxySettings loadFromProperties(final XMLPropertiesForAppComponent props,
															 final String baseXPath) {
		boolean enabled = props.propertyAt(baseXPath + "/@enabled")
							   .asBoolean(false);
		Url proxyUrl = props.propertyAt(baseXPath + "/host")
							   		  .asUrl();
		UserCode userCode = props.propertyAt(baseXPath + "/user")
								 .asUserCode();
		Password password = props.propertyAt(baseXPath + "/password")
								 .asPassword();
		
		HttpClientProxySettings outProxySettings = null;
		if (enabled && (proxyUrl == null || userCode == null || password == null)) {
			log.warn("Proxy info is NOT propertly configured at {}: there's no host, user or password info!",
					 baseXPath);
		} 
		else if (proxyUrl == null && userCode == null && password == null) {
			outProxySettings = new HttpClientProxySettings();
			outProxySettings.setEnabled(false);
		}
		else {
			if (proxyUrl == null) throw new IllegalStateException("Proxy url cannot be null");
			outProxySettings = new HttpClientProxySettings();
			outProxySettings.setEnabled(enabled);
			outProxySettings.setProxyUrl(proxyUrl);
			outProxySettings.setUserCode(userCode);
			outProxySettings.setPassword(password);
		}
		return outProxySettings;
	}
}
