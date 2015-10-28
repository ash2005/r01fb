package r01f.types.weburl;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.types.annotations.Inmutable;

@ConvertToDirtyStateTrackable
@Inmutable
@Accessors(prefix="_")
@RequiredArgsConstructor
public class WebUrlSite<S extends WebUrlSecurityZone<S>,
						E extends WebUrlEnvironment<E>> 
  implements Serializable {
	
	private static final long serialVersionUID = -909680437188203889L;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Security zone (ie: intranet/extranet, Internet, external)
	 */
	@XmlAttribute(name="securityZone")
	@Getter private final S _securityZone;
	/**
	 * Environment (only for internal sites ie: DES, PRU, PROD)
	 */
	@XmlAttribute(name="environment")
	@Getter private final E _environment;
	/**
	 * The site
	 */
	@XmlAttribute(name="host")
	@Getter private final Host _host;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR   
/////////////////////////////////////////////////////////////////////////////////////////
	public WebUrlSite(WebUrlSite<S,E> other) {
		_securityZone = other.getSecurityZone();
		_environment = other.getEnvironment();
		_host = new Host(other.getHost());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return this.asString();
	}
	/**
	 * @return the site as a string
	 */
	public String asString() {
		return _host != null ? _host.asString() : null;
	}
}
