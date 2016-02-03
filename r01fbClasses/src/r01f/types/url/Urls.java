package r01f.types.url;

import r01f.types.Paths;
import r01f.types.Provider;
import r01f.types.UrlPath;
import r01f.types.url.Url.UrlComponents;
import r01f.util.types.ParametersWrapperBase;

public class Urls {
	/**
	 * Creates a new url from the given one joining the given path
	 * ie: if url=www.mysite.com/foo/ and path=/bar, the returned url is www.mysite.com/foo/bar
	 * @param url
	 * @param path
	 * @return
	 */
	public static Url join(final Url url,
						   final UrlPath path) {
		if (path == null) return url;
		if (url == null) return Url.from(path);
		
		UrlComponents urlComps = url.getComponents();
		return Url.from(urlComps.getProtocol(),urlComps.getHost(),urlComps.getPort(),
					    Paths.forUrlPaths().join(urlComps.getUrlPath(),
					    						 path),
					    urlComps.getQueryString(),
					    urlComps.getAnchor());
	}
	/**
	 * Creates a new url from the given one joining the given queryString
	 * ie: if url=www.mysite.com/foo?param1=param1Value and queryString=param2=param2Value, the returned url is www.mysite.com/foo?param1=param1Value&param2=param2Value
	 * @param url
	 * @param qryString
	 * @return
	 */
	public static Url join(final Url url,
						   final UrlQueryString qryString) {
		if (qryString == null) return url;
		if (url == null) return null;
		
		UrlComponents urlComps = url.getComponents();
		return Url.from(urlComps.getProtocol(),urlComps.getHost(),urlComps.getPort(),
					    urlComps.getUrlPath(),
					    Urls.join(urlComps.getQueryString(),
					    		  qryString),
					    urlComps.getAnchor());
	}
	/**
	 * Creates a new url from the given one joining the given path and query stringa
	 * ie: if url=www.mysite.com/foo?param1=param1Value, path=/bar and queryString=param2=param2Value, the returned url is www.mysite.com/foo/bar?param1=param1Value&param2=param2Value
	 * @param url
	 * @param path
	 * @param qryString
	 * @return
	 */
	public static Url join(final Url url,
						   final UrlPath path,
						   final UrlQueryString qryString) {
		if (qryString == null) return url;
		if (url == null) return null;
		
		UrlComponents urlComps = url.getComponents();
		return Url.from(urlComps.getProtocol(),urlComps.getHost(),urlComps.getPort(),
					    Paths.forUrlPaths().join(urlComps.getUrlPath(),
					    						 path),
					    Urls.join(urlComps.getQueryString(),
					    		  qryString),
					    urlComps.getAnchor());
	}
	/**
	 * Creates a new url from the given one joining the given path and query stringa
	 * ie: if url=www.mysite.com/foo?param1=param1Value and queryString=param2=param2Value, the returned url is www.mysite.com/foo?param1=param1Value&param2=param2Value
	 * @param url
	 * @param qryStrParams
	 * @return
	 */
	public static Url join(final Url url,
						   final UrlQueryStringParam... qryStrParams) {
		return Urls.join(url,
				  		 UrlQueryString.fromParams(qryStrParams));
	}
	/**
	 * Creates a new url from the given one joining the given path and query stringa
	 * ie: if url=www.mysite.com/foo?param1=param1Value, path=/bar and queryString=param2=param2Value, the returned url is www.mysite.com/foo/bar?param1=param1Value&param2=param2Value
	 * @param url
	 * @param path
	 * @param qryStrParams
	 * @return
	 */
	public static Url join(final Url url,
						   final UrlPath path,
						   final UrlQueryStringParam... qryStrParams) {
		return Urls.join(url,
				  		 path,
				  		 UrlQueryString.fromParams(qryStrParams));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Joins a query string with the new one
	 * BEWARE that the {@link UrlQueryString} type is immutable so a new ParametersWrapper instance is created
	 * @param queryString 
	 * @param paramName
	 * @param paramValue
	 * @return
	 */
	public static UrlQueryString join(final UrlQueryString queryString,
							   		  final String paramName,final String paramValue) {
		return ParametersWrapperBase.join(UrlQueryString.class,
					 					  queryString,
					 					  paramName,paramValue);	
	}
	/**
	 * Joins a query stirng with the new one
	 * BEWARE that the {@link UrlQueryString} type is immutable so a new ParametersWrapper instance is created
	 * @param queryString
	 * @param paramName
	 * @param paramValueProvider
	 * @return
	 */
	public static UrlQueryString join(final UrlQueryString queryString,
							   		  final String paramName,final Provider<String> paramValueProvider) {
		return ParametersWrapperBase.join(UrlQueryString.class,
					 					  queryString,	
					 					  paramName,paramValueProvider);	
	}
	/**
	 * Joins this params with the given ones
	 * @param other
	 * @return
	 */
	public static UrlQueryString join(final UrlQueryString queryString,
									  final UrlQueryString other) {
		return ParametersWrapperBase.join(UrlQueryString.class,
					 					  queryString,
					 					  other);
	}
}
