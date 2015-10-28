package r01f.httpclient;

import java.io.IOException;
import java.nio.charset.Charset;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import org.apache.commons.codec.EncoderException;

import r01f.util.types.Strings;

/**
 * Creates a param to be send to the server using a POSTed form which could be:
 * <ol>
 * 		<li>a param in a form-url-encoded post</li>
 * 		<li>a form param in a multi-part post</li>
 * </ol>
 * The creation of a param is like:
 * <pre class='brush:java'>
 * 		HttpRequestFormParameterForText param = HttpRequestFormParameterForText.of("my param value")
 * 																	     	   .withName("myParam");
 * </pre>
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class HttpRequestFormParameterForText
  implements HttpRequestFormParameter {

/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter(AccessLevel.PRIVATE) private String _name;
	@Getter @Setter(AccessLevel.PRIVATE) private String _value;

/////////////////////////////////////////////////////////////////////////////////////////
//   BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static HttpRequestFormParameterForText of(final String s) {
		HttpRequestFormParameterForText outParam = new HttpRequestFormParameterForText();
		outParam.setValue(s);
		return outParam;
	}
	public static HttpRequestFormParameterForText of(final int i) {
		HttpRequestFormParameterForText outParam = new HttpRequestFormParameterForText();
		outParam.setValue(Integer.toString(i));
		return outParam;
	}
	public static HttpRequestFormParameterForText of(final long l) {
		HttpRequestFormParameterForText outParam = new HttpRequestFormParameterForText();
		outParam.setValue(Long.toString(l));
		return outParam;
	}
	public static HttpRequestFormParameterForText of(final double d) {
		HttpRequestFormParameterForText outParam = new HttpRequestFormParameterForText();
		outParam.setValue(Double.toString(d));
		return outParam;
	}
	public static HttpRequestFormParameterForText of(final float f) {
		HttpRequestFormParameterForText outParam = new HttpRequestFormParameterForText();
		outParam.setValue(Float.toString(f));
		return outParam;
	}
	public static HttpRequestFormParameterForText of(final boolean b) {
		HttpRequestFormParameterForText outParam = new HttpRequestFormParameterForText();
		outParam.setValue(Boolean.toString(b));
		return outParam;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public HttpRequestFormParameterForText withName(final String name) {
		_name = name;
		return this;
	}

/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public byte[] _serializeFormParam(final Charset targetServerCharset,
									  final boolean multiPart) throws IOException {

		Charset theTargetServerCharset = targetServerCharset == null ? Charset.defaultCharset()
																	 : targetServerCharset;
		try {
			byte[] outSerializedParamBytes = null;
			if (multiPart) {
				// Content-Disposition: form-data; name="param-name"
				//
				// Param-value
				outSerializedParamBytes = Strings.of("Content-Disposition: form-data; name=\"{}\"\r\n" +
													 "\r\n" + //mandatory newline, header and value must be separated.
													 "{}\r\n")
											     .customizeWith(_name,
														   		_value)
												 .getBytes(theTargetServerCharset);
			} else {
				outSerializedParamBytes = Strings.of("{}={}")
										         .customizeWith(Strings.of(_name).urlEncode(),
										    			   		Strings.of(_value).urlEncode())
										    	 .getBytes(theTargetServerCharset);
			}
			return outSerializedParamBytes != null ? outSerializedParamBytes
											  	   : null;
		} catch(EncoderException encEx) {
			throw new IOException(encEx);
		}
	}
}
