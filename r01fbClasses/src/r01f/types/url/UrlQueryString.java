package r01f.types.url;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import r01f.patterns.Memoized;
import r01f.types.annotations.Inmutable;
import r01f.util.types.ParametersWrapperBase;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


/**
 * Helper type to build url-string-encoded parameters that encapsulates all the string building stuff offering an api
 * that isolates user from string concat errors
 * @see ParametersWrapperBase
 */
@Inmutable
public class UrlQueryString 
	 extends ParametersWrapperBase<UrlQueryString> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public UrlQueryString(final Map<String,String> params) {
		super(params,
			  DEFAULT_URL_ENCODE_PARAM_VALUE_ENCODER_DECODER);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new instance form a params list
	 * @param params
	 * @return
	 */
	public static UrlQueryString fromParams(final Iterable<UrlQueryStringParam> params) {
		Map<String,String> paramsMap = Maps.newHashMap();
		for (UrlQueryStringParam param : params) {
			paramsMap.put(param.getName(),param.getValue());
		}
		return new UrlQueryString(paramsMap);
	}
	/**
	 * Creates a new instance form a params list
	 * @param params
	 * @return
	 */
	public static UrlQueryString fromParams(final Collection<UrlQueryStringParam> params) {
		if (CollectionUtils.isNullOrEmpty(params)) return new UrlQueryString(null);
		Map<String,String> paramsMap = Maps.newHashMapWithExpectedSize(params.size());
		for (UrlQueryStringParam param : params) {
			paramsMap.put(param.getName(),param.getValue());
		}
		return new UrlQueryString(paramsMap);
	}
	/**
	 * Creates a new instance form a params list
	 * @param params
	 * @return
	 */
	public static UrlQueryString fromParams(final UrlQueryStringParam... params) {
		if (CollectionUtils.isNullOrEmpty(params)) return new UrlQueryString(null);
		Map<String,String> paramsMap = Maps.newHashMapWithExpectedSize(params.length);
		for (UrlQueryStringParam param : params) {
			paramsMap.put(param.getName(),param.getValue());
		}
		return new UrlQueryString(paramsMap);
	}
	/**
	 * Creates a new instance form a full query string
	 * @param paramsStr
	 * @return
	 */
	public static UrlQueryString fromParamsString(final String paramsStr) {
		String theParamStr = paramsStr != null && paramsStr.trim().startsWith("?") ? paramsStr.trim().substring(1) : paramsStr;
		return ParametersWrapperBase._loadFromString(UrlQueryString.class,
											  		 theParamStr,
											  		 false);	// do not decode param values
	}
	/**
	 * Creates a new instance form a full query string
	 * @param paramsStr
	 * @return
	 */
	public static UrlQueryString fromUrlEncodedParamsString(final String paramsStr) {
		String theParamStr = paramsStr != null && paramsStr.trim().startsWith("?") ? paramsStr.trim().substring(1) : paramsStr;
		return ParametersWrapperBase._loadFromString(UrlQueryString.class,
													 theParamStr,
													 true);	// decode param values
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlTransient
	private final transient Memoized<Set<UrlQueryStringParam>> _qryStrParams = 
									new Memoized<Set<UrlQueryStringParam>>() {
											@Override
											protected Set<UrlQueryStringParam> supply() {
												Set<UrlQueryStringParam> outParams = null;
												if (CollectionUtils.hasData(_params)) {
													outParams = Sets.newHashSetWithExpectedSize(_params.size());
													for (Map.Entry<String,String> me : _params.entrySet()) {
														outParams.add(new UrlQueryStringParam(me.getKey(),me.getValue()));
													}
												} 
												return outParams;
											}
									 };
	/**
	 * Returns the query string params
	 * @return
	 */
	public Set<UrlQueryStringParam> getQueryStringParams() {
		return _qryStrParams.get();
	}
	/**
	 * Returns a query string param
	 * @param paramName
	 * @return
	 */
	public UrlQueryStringParam getQueryStringParam(final String paramName) {
		return FluentIterable.from(this.getQueryStringParams())
							 .filter(new Predicate<UrlQueryStringParam>() {
																	@Override
																	public boolean apply(final UrlQueryStringParam aParam) {
																		return aParam.getName().equals(paramName);
																	}
												  		  	  })
							 .first().orNull();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates an NEW instance of an {@link UrlQueryString} object joining 
	 * this query string with the given one
	 * @param other
	 * @return
	 */
	public UrlQueryString joinWith(final UrlQueryString other) {
		return ParametersWrapperBase.join(UrlQueryString.class,
										  this,other);
	}
}
