package r01f.httpclient;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient.RequestMethod;

public class HttpRequestFluentStatementForGETMethod
	 extends HttpRequestFluentStatementForMethodBase<HttpRequestFluentStatementForGETMethod> {
///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////
	HttpRequestFluentStatementForGETMethod(final String newTargetUrlStr,final List<HttpRequestURLEncodedParameter> newUrlEncodedParameters,
										   final Charset newTargetServerCharset,
						  				   final Map<String,String> newRequestHeaders,final Map<String,String> newRequestCookies,
										   final long newConxTimeOut,
						  				   final String newProxyHost,final String newProxyPort,final UserCode newProxyUser,final Password newProxyPassword,
						  				   final UserCode authUserCode,final Password authPassword) {
		super(RequestMethod.GET,
			  newTargetUrlStr,newUrlEncodedParameters,
			  newTargetServerCharset,
			  newRequestHeaders,newRequestCookies,
			  newConxTimeOut,
			  newProxyHost,newProxyPort,newProxyUser,newProxyPassword,
			  authUserCode,authPassword);
	}
}
