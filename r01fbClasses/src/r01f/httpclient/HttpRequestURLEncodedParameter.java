package r01f.httpclient;

import java.nio.charset.Charset;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.codec.EncoderException;

import r01f.util.types.Strings;

/**
 * Creates a param to be send to the server encoded as a URL-param
 * The creation of a param is like:
 * <pre class='brush:java'>
 * 		HttpRequestURLEncodedParameter param = HttpRequestURLEncodedParameter.of("my param value")
 * 																	         .withName("myParam");
 * </pre>
 */
@NoArgsConstructor @AllArgsConstructor
@Accessors(prefix="_")
@Slf4j
public class HttpRequestURLEncodedParameter {
///////////////////////////////////////////////////////////////////////////////
// MIEMBROS
///////////////////////////////////////////////////////////////////////////////
	@Getter @Setter(AccessLevel.PRIVATE) private String _name;
	@Getter @Setter(AccessLevel.PRIVATE) private String _value;
	
/////////////////////////////////////////////////////////////////////////////////////////
//   BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpRequestURLEncodedParameter of(final String s) {
		HttpRequestURLEncodedParameter outParam = new HttpRequestURLEncodedParameter();
		outParam.setValue(s);
		return outParam;
	}
	public static HttpRequestURLEncodedParameter of(final int i) {
		HttpRequestURLEncodedParameter outParam = new HttpRequestURLEncodedParameter();
		outParam.setValue(Integer.toString(i));
		return outParam;
	}
	public static HttpRequestURLEncodedParameter of(final long l) {
		HttpRequestURLEncodedParameter outParam = new HttpRequestURLEncodedParameter();
		outParam.setValue(Long.toString(l));
		return outParam;
	}
	public static HttpRequestURLEncodedParameter of(final double d) {
		HttpRequestURLEncodedParameter outParam = new HttpRequestURLEncodedParameter();
		outParam.setValue(Double.toString(d));
		return outParam;
	}
	public static HttpRequestURLEncodedParameter of(final float f) {
		HttpRequestURLEncodedParameter outParam = new HttpRequestURLEncodedParameter();
		outParam.setValue(Float.toString(f));
		return outParam;
	}
	public static HttpRequestURLEncodedParameter of(final boolean b) {
		HttpRequestURLEncodedParameter outParam = new HttpRequestURLEncodedParameter();
		outParam.setValue(Boolean.toString(b));
		return outParam;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public HttpRequestURLEncodedParameter withName(final String name) {
		_name = name;
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
/////////////////////////////////////////////////////////////////////////////////////////
	public String get(final Charset encoding) {
		if (_value == null) return null;
		try {
			return Strings.of(_value)
						  .encode(encoding)		// Codificar al charset especificado
						  .urlEncode()			// Codificar en la URL escapando &, ;, etc
						  .asString();
		} catch(EncoderException encEx) {
			log.error(encEx.getMessage(),encEx);
			return encEx.getMessage();			// Devolver el error de codificación
		}
					
	}	

}
