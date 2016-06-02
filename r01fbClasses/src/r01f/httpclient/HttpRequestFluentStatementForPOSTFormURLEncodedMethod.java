package r01f.httpclient;

import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient.RequestMethod;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

public class HttpRequestFluentStatementForPOSTFormURLEncodedMethod
	 extends HttpRequestFluentStatementForMethodBase<HttpRequestFluentStatementForPOSTFormURLEncodedMethod> {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Params to be POSTed to the server
	 */
	private List<HttpRequestFormParameter> _formParameters;  
 
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	HttpRequestFluentStatementForPOSTFormURLEncodedMethod(final String newTargetUrlStr,final List<HttpRequestURLEncodedParameter> newUrlEncodedParameters,
													      final Charset newTargetServerCharset,
						  				   				  final Map<String,String> newRequestHeaders,final Map<String,String> newRequestCookies,
						  				   				  final long newConxTimeOut,
						  				   				  final String newProxyHost,final String newProxyPort,final UserCode newProxyUser,final Password newProxyPassword,
						  				   				  final UserCode authUserCode,final Password authPassword) {
		super(RequestMethod.POST_FORM_URL_ENCODED,
			  newTargetUrlStr,newUrlEncodedParameters,
			  newTargetServerCharset,
			  newRequestHeaders,newRequestCookies,
			  newConxTimeOut,
			  newProxyHost,newProxyPort,newProxyUser,newProxyPassword,
			  authUserCode,authPassword);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  API
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets a POSTed params
	 * @param params the params
	 */
	public HttpRequestFluentStatementForPOSTFormURLEncodedMethod withPOSTFormParameters(final HttpRequestFormParameter... params) {
		if (_formParameters == null) _formParameters = new ArrayList<HttpRequestFormParameter>();
		if (CollectionUtils.hasData(params)) {
			_formParameters = new ArrayList<HttpRequestFormParameter>(params.length);
			for (HttpRequestFormParameter param : params) {
				_formParameters.add(param);		
			}
		}
		return this;
	}
	/**
	 * Thess the POSTed params
	 * @param params parametros
	 */
	public HttpRequestFluentStatementForPOSTFormURLEncodedMethod withPOSTFormParameters(final List<HttpRequestFormParameter> params) {
		_formParameters = params;
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Writes the POSTed form into the body of the request sent to the server
//	See http://www.w3.org/TR/html401/interact/forms.html#h-17.13.4
//
// IMPORTANT!!!		The multipart/form-data enctype is handled by HttpRequestFluentStatementForPOSTMultiPartMethod type
//
// 	If the form is like:
//		<FORM action="http://server.com/cgi/handle"
//            enctype="application/x-www-form-urlencoded"
//            method="post">
//   				What is your name? 			<INPUT type="text" name="user_name">
//					What is your surname? 		<INPUT type="text" name="user_surname">
//   		<INPUT type="submit" value="Send"> 
//			<INPUT type="reset">
// 		</FORM>
//
// If the user enters "Larry" and "Page" in the text inputs, the user agent might send back the following data:
//
//   	HEADER: Content-Type: application/x-www-form-urlencoded
//
//		user_name=Larry&user_surname=Page
// 
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	String payloadContentType() {
		return "application/x-www-form-urlencoded";
	}
	@Override
	void payloadToOutputStream(final DataOutputStream dos) throws IOException {
		// Binary params are not allowed in a POST of a form in its url-encoded form
		Collection<HttpRequestFormParameter> binaryParams = Collections2.filter(_formParameters,
																				Predicates.instanceOf(HttpRequestFormParameterForMultiPartBinaryData.class));
		if (CollectionUtils.hasData(binaryParams)) throw new IOException("An form-url-encoded POST method cannot be used when posting a binary param!");
		
		// Encode params in its url-encoded form
		for(Iterator<HttpRequestFormParameter> paramIt = _formParameters.iterator(); paramIt.hasNext(); ) {
			HttpRequestFormParameter param = paramIt.next();
			byte[] paramBytes = param._serializeFormParam(_targetServerCharset,
														  false);
			dos.write(paramBytes);
			if (paramIt.hasNext()) dos.write("&".getBytes());
		}
	}
}
