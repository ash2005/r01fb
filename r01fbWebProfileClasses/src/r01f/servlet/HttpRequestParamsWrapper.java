package r01f.servlet;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import r01f.util.types.StringConverter;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * A wrapper of {@link HttpServletRequest} params to provide easyer access
 * <pre class='brush:java'>
 * 		HttpRequestParamsWrapper params = new HttpRequestParamsWrapper(request);
 * 		params.getParameter(paramName).asInteger(defValue);
 * </pre>
 */
@Slf4j
public class HttpRequestParamsWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Map<String,Object> _requestParams;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public HttpRequestParamsWrapper(final HttpServletRequest req) {
		_requestParams = req.getParameterMap();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public StringConverter getParameter(final String name) {
		if (CollectionUtils.isNullOrEmpty(_requestParams)) return null;
		
		Object paramObjValue = _requestParams.get(name);
		
		String paramValue = null;
		if (paramObjValue != null 
		 && paramObjValue.getClass().isArray()) {
			String[] paramStrValues = (String[])paramObjValue;
			if (paramStrValues.length == 1) {
				paramValue = paramStrValues[0];
			} else {
				throw new IllegalArgumentException("Request param with name='" + name + "' has more than a single value!");
			}
		} else {
			paramValue = (String)paramObjValue;
		}
		return Strings.isNOTNullOrEmpty(paramValue) ? new StringConverter(paramValue)
													: new StringConverter(null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public void checkParam(final String paramName) {
		this.checkParam(paramName,Strings.customized("The param with name={} was NOT received or it's null",paramName));
	}
	public void checkParam(final String paramName,final String msg) {
		this.checkParam(paramName,msg,(Object[])null);
	}
	public void checkParam(final String paramName,final String msg,final Object... params) {
		if (CollectionUtils.isNullOrEmpty(_requestParams) || !_requestParams.containsKey(paramName)) {
			String theMessage = Strings.customized(msg,params);
			log.error(theMessage);
			throw new IllegalStateException(theMessage);
		}
	}
}
