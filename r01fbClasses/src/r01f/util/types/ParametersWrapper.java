package r01f.util.types;

/**
 * Helper type to build string-encoded parameters that encapsulates all the string building stuff ofering an api
 * that isolates user from string concat errors
 */
public class ParametersWrapper 
	 extends ParametersWrapperBase<ParametersWrapper> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private ParametersWrapper() {
		super();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a new instance
	 */
	public static ParametersWrapper create() {
		return new ParametersWrapper();
	}
	/**
	 * Creates a new instance form a full params string
	 * @param paramsSeparator
	 * @param paramsStr
	 * @return
	 */
	public static ParametersWrapper fromParamsString(final String paramsStr) {
		ParametersWrapper paramWrap = new ParametersWrapper();
		paramWrap._loadFromString(paramsStr,
								  false);
		return paramWrap;
	}
}
