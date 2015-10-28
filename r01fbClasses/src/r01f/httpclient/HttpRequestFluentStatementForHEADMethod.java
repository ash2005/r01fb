package r01f.httpclient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient.RequestMethod;

public class HttpRequestFluentStatementForHEADMethod
	 extends HttpRequestFluentStatementForMethodBase<HttpRequestFluentStatementForHEADMethod> {
///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////
	HttpRequestFluentStatementForHEADMethod(final String newTargetUrlStr,final List<HttpRequestURLEncodedParameter> newUrlEncodedParameters,
											final Charset newTargetServerCharset,
						  				    final Map<String,String> newRequestHeaders,final Map<String,String> newRequestCookies,
										    final long newConxTimeOut,
						  				    final String newProxyHost,final String newProxyPort,final UserCode newProxyUser,final Password newProxyPassword,
						  				    final UserCode authUserCode,final Password authPassword) {
		super(RequestMethod.HEAD,
			  newTargetUrlStr,newUrlEncodedParameters,
			  newTargetServerCharset,
			  newRequestHeaders,newRequestCookies,
			  newConxTimeOut,
			  newProxyHost,newProxyPort,newProxyUser,newProxyPassword,
			  authUserCode,authPassword);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PAYLOAD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	String payloadContentType() {
		return null;
	}
	@Override
	void payloadToOutputStream(final DataOutputStream dos) throws IOException {
		throw new IOException("A HEAD HTTP call cannot have payload!");
	}
}
