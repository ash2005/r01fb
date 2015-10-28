package r01f.httpclient;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

abstract class HttpRequestFluentStatementBase<SELF_TYPE extends HttpRequestFluentStatementBase<SELF_TYPE>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  ABSTRACT METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	protected String _targetURLStr;    		// Destination url
	protected List<HttpRequestURLEncodedParameter> _urlEncodedParameters;  	// url parameters
	
/////////////////////////////////////////////////////////////////////////////////////////
//  API
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Establece un parametro de la llamada
	 * @param param el parametro
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE withURLParameter(final HttpRequestURLEncodedParameter param) {
		if (_urlEncodedParameters == null) _urlEncodedParameters = new ArrayList<HttpRequestURLEncodedParameter>();
		_urlEncodedParameters.add(param);
		return (SELF_TYPE)this;
	}
	/**
	 * Establece los parametros de la llamada
	 * @param params parametros
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE withURLParameters(final List<HttpRequestURLEncodedParameter> params) {
		_urlEncodedParameters = params;
		return (SELF_TYPE)this;
	}
	/**
	 * Establece los parametros de la llamada
	 * @param params parametros
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE withURLParameters(final HttpRequestURLEncodedParameter... params) {
		_urlEncodedParameters = Lists.newArrayList(params);
		return (SELF_TYPE)this;
	}
	/**
	 * La llamada HTTP NO tiene parametros
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE withoutURLParameters() {
		return (SELF_TYPE)this;
	}
}
