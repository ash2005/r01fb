package r01f.types.weburl;

import r01f.util.types.ParametersWrapperBase;


/**
 * Helper type to build url-string-encoded parameters that encapsulates all the string building stuff offering an api
 * that isolates user from string concat errors
 * @see ParametersWrapperBase
 */
public class WebUrlQueryStringWrapper 
	 extends ParametersWrapperBase<WebUrlQueryStringWrapper> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTO
/////////////////////////////////////////////////////////////////////////////////////////
	private WebUrlQueryStringWrapper() {
		super(DEFAULT_URL_ENCODE_PARAM_VALUE_ENCODER_DECODER);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new instance
	 */
	public static WebUrlQueryStringWrapper create() {
		return new WebUrlQueryStringWrapper();
	}
	/**
	 * Creates a new instance form a full query string
	 * @param paramsStr
	 * @return
	 */
	public static WebUrlQueryStringWrapper fromParamsString(final String paramsStr) {
		WebUrlQueryStringWrapper urlQryStr = new WebUrlQueryStringWrapper();
		urlQryStr._loadFromString(paramsStr,
								  false);	// do not decode param values
		return urlQryStr;
	}
	/**
	 * Creates a new instance form a full query string
	 * @param paramsStr
	 * @return
	 */
	public static WebUrlQueryStringWrapper fromUrlEncodedParamsString(final String paramsStr) {
		WebUrlQueryStringWrapper urlQryStr = new WebUrlQueryStringWrapper();
		urlQryStr._loadFromString(paramsStr,
								  true);	// decode param values
		return urlQryStr;
	}
}
