package r01f.util.types;

import java.util.Map;

import r01f.types.Provider;
import r01f.types.annotations.Inmutable;

/**
 * Helper type to build string-encoded parameters that encapsulates all the string building stuff offering an api
 * that isolates user from string concat errors
 */
@Inmutable
public class ParametersWrapper 
	 extends ParametersWrapperBase<ParametersWrapper> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public ParametersWrapper(final Map<String,String> params) {
		super(params);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a new instance form a full params string
	 * @param paramsSeparator
	 * @param paramsStr
	 * @return
	 */
	public static ParametersWrapper fromParamsString(final String paramsStr) {
		return ParametersWrapperBase._loadFromString(ParametersWrapper.class,
											  		 paramsStr,
											  		 false);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Joins this params with the new one
	 * BEWARE that this type is inmutable so a new ParametersWrapper instance is created
	 * @param paramName
	 * @param paramValue
	 * @return
	 */
	public ParametersWrapper join(final String paramName,final String paramValue) {
		return ParametersWrapperBase.join(ParametersWrapper.class,
					 					  this,
					 					  paramName,paramValue);	
	}
	/**
	 * Joins this params with the new one
	 * BEWARE that this type is inmutable so a new ParametersWrapper instance is created
	 * @param paramName
	 * @param paramValueProvider
	 * @return
	 */
	public ParametersWrapper join(final String paramName,final Provider<String> paramValueProvider) {
		return ParametersWrapperBase.join(ParametersWrapper.class,
					 					  this,
					 					  paramName,paramValueProvider);	
	}
}
