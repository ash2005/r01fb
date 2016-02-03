package r01f.types.url;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCode;
import r01f.enums.EnumWithCodeWrapper;
import r01f.types.CanBeRepresentedAsString;

/**
 * Protocol
 */
@Accessors(prefix="_")
public enum UrlProtocol 
 implements EnumWithCode<String,UrlProtocol>,
  		    CanBeRepresentedAsString {
	HTTP("http"),
	HTTPS("https"),
	FILE("file"),
	FTP("ftp");
	
	@Getter private final String _code;
	@Getter private final Class<String> _codeType = String.class;
	
	private UrlProtocol(final String code) {
		_code = code;
	}
	@Override
	public String asString() {
		return this.getCode();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ EnumWithCode
/////////////////////////////////////////////////////////////////////////////////////////
	private static EnumWithCodeWrapper<String,UrlProtocol> _enums = new EnumWithCodeWrapper<String,UrlProtocol>(UrlProtocol.values());

	@Override
	public boolean isIn(UrlProtocol... status) {
		return _enums.isIn(this,status);
	}
	@Override
	public boolean is(UrlProtocol other) {
		return _enums.is(this,other);
	}
	public static boolean canBeFromCode(final String protocol) {
		return _enums.canBeFromCode(protocol);
	}
	public static UrlProtocol fromCode(final String code) {
		return _enums.fromCode(code);
	}
	public static UrlProtocol fromPort(final int port) {
		if (port == 80) {
			return HTTP;
		} else if (port == 443 || port == 444) {
			return HTTPS;
		} else if (port == 21) {
			return FTP;
		}
		return null;
	}
}