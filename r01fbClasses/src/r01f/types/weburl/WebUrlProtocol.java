package r01f.types.weburl;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.enums.EnumWithCode;
import r01f.enums.EnumWithCodeWrapper;

/**
 * Protocol
 */
@Accessors(prefix="_")
public enum WebUrlProtocol implements EnumWithCode<String,WebUrlProtocol> {
	HTTP("http"),
	HTTPS("https");
	
	@Getter private final String _code;
	@Getter private final Class<String> _codeType = String.class;
	
	private WebUrlProtocol(final String code) {
		_code = code;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ EnumWithCode
/////////////////////////////////////////////////////////////////////////////////////////
	private static EnumWithCodeWrapper<String,WebUrlProtocol> _enums = new EnumWithCodeWrapper<String,WebUrlProtocol>(WebUrlProtocol.values());

	@Override
	public boolean isIn(WebUrlProtocol... status) {
		return _enums.isIn(this,status);
	}
	@Override
	public boolean is(WebUrlProtocol other) {
		return _enums.is(this,other);
	}
	public static boolean canBeFromCode(final String protocol) {
		return _enums.canBeFromCode(protocol);
	}
	public static WebUrlProtocol fromCode(final String code) {
		return _enums.fromCode(code);
	}
}