package r01f.util.types;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.MapDifference;
import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.reflection.ReflectionUtils;
import r01f.types.Provider;
import r01f.util.types.collections.CollectionUtils;

/**
 * Helper type to build string-encoded parameters that encapsulates all the string building stuff by offering an api
 * that isolates user from string concat errors
 * The simplest usage is to add param by name and value:
 * <pre class="brush:java">
 *		ParameterStringWrapper qryStr = ParameterStringWrapper.create('&')	// use the & char as param separator
 *															  .addParam("param0","param0Value");
 * </pre>
 * But the paramValue normally comes from run-time values that must be evaluated to compose the paramValue
 * This type offers a method to add those types of param values:
 * <pre class="brush:java">
 *	ParameterStringWrapper qryStr = ParameterStringWrapper.create('&')	// use the & char as param separator
 *										.addParam("param1",,
 *												  new ParamValueProvider() {
 *															@Override 
 *															public String provideValue() {
 *																return Strings.of("{},{}")
 *																			  .customizeWith(someVar.getA(),someVar.getB())
 *																			  .asString();
 *															}
 *												  })
 * <pre>
 * 
 * To get the string from the params, simply call:
 * <pre class="brush:java">
 * 		ParameterStringWrapper qryStrWrap = ...
 * 		String queryString = qryStrWrap.asString();
 * </pre>
 * 
 * A {@link ParametersWrapperBase} can be created from the query string:
 * <pre class="brush:java">
 * 		ParameterStringWrapper qryStr2 = ParameterStringWrapper.fromParamString("param1=a,b&param2=myParam2-a");
 * </pre> 
 */
@Accessors(prefix="_")
@Slf4j
@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class ParametersWrapperBase<SELF_TYPE extends ParametersWrapperBase<SELF_TYPE>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static final char DEFAUL_PARAM_SPLIT_CHAR = '&';
	protected static final Pattern DEFAULT_PARAM_VALUE_SPLIT_PATTERN = Pattern.compile("([^=]+)=(.+)");
	protected static final String DEFAULT_PARAM_VALUE_SERIALIZE_TEMPLATE = "{}={}";
	protected static final ParamValueEncoderDecoder DEFAULT_URL_ENCODE_PARAM_VALUE_ENCODER_DECODER = new ParamValueEncoderDecoder() {
																											@Override
																											public String encodeValue(final String value) {
																												return StringEncodeUtils.urlEncodeNoThrow(value)
																																		.toString();
																											}
																											@Override
																											public String decodeValue(final String value) {
																												return StringEncodeUtils.urlDecodeNoThrow(value)
																																		.toString();
																											}
																									 };
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The param join char to be used when serializing the parameters
	 */
	@Getter(AccessLevel.PROTECTED) private final char _paramSplitChar;
	/**
	 * The param and value split regex
	 */
	@Getter(AccessLevel.PROTECTED) private final Pattern _paramAndValueSplitPattern;
	/**
	 * The param and value serialize template
	 */
	@Getter(AccessLevel.PROTECTED) private final String _paramAndValueSerializeTemplate; 
	/**
	 * The param value encoder and decoder
	 */
	@Getter(AccessLevel.PROTECTED) private final ParamValueEncoderDecoder _paramValueEncoderDecoder;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Internal map holding the params
	 */
	@Getter protected final ImmutableMap<String,String> _params;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ParametersWrapperBase(final Map<String,String> params) {
		_paramSplitChar = DEFAUL_PARAM_SPLIT_CHAR;
		_paramAndValueSplitPattern = DEFAULT_PARAM_VALUE_SPLIT_PATTERN;
		_paramAndValueSerializeTemplate = DEFAULT_PARAM_VALUE_SERIALIZE_TEMPLATE;
		_paramValueEncoderDecoder = null;
		_params = ImmutableMap.copyOf(params);
	}
	public ParametersWrapperBase(final Map<String,String> params,
								 final ParamValueEncoderDecoder encoderDecoder) {
		_paramSplitChar = DEFAUL_PARAM_SPLIT_CHAR;
		_paramAndValueSplitPattern = DEFAULT_PARAM_VALUE_SPLIT_PATTERN;
		_paramAndValueSerializeTemplate = DEFAULT_PARAM_VALUE_SERIALIZE_TEMPLATE;
		_paramValueEncoderDecoder = encoderDecoder;
		_params = params != null ? ImmutableMap.<String,String>copyOf(params)
								 : ImmutableMap.<String,String>of();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected interface ParamValueEncoderDecoder {
		public String encodeValue(final String value);
		public String decodeValue(final String value);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	// a cache of the ParametersWrapperBase types constructor
	private static Map<Class<? extends ParametersWrapperBase<?>>,Constructor<? extends ParametersWrapperBase<?>>> CONSTRUCTOR_REF_CACHE = Maps.newHashMap();
	
	@SuppressWarnings("unchecked")
	private static <PW extends ParametersWrapperBase<PW>> PW _createParameterWrapperInstanceFromElements(final Class<PW> pwType,
																										 final Map<String,String> params) {
		PW outParamWrapperInstance = null;
		try {
			Constructor<? extends ParametersWrapperBase<?>> constructor = CONSTRUCTOR_REF_CACHE.get(pwType);
			if (constructor == null) {
				// load the constructor
				constructor = (Constructor<? extends ParametersWrapperBase<?>>)ReflectionUtils.getConstructor(pwType,
	        												 												  new Class<?>[] {Map.class},
	        												 												  true);
				if (constructor == null) throw new IllegalStateException(Throwables.message("ParametersWrapper type {} does NOT have a Map<String,String> based constructor! It's mandatory!",
																							pwType));
				CONSTRUCTOR_REF_CACHE.put(pwType,constructor);
			}
			outParamWrapperInstance = (PW)constructor.newInstance(new Object[] {params});		// BEWARE!! the params are encapsulated in a ImmutableList
		} catch(Throwable th) {
			log.error("Could NOT create a {} instance: {}",pwType.getName(),th.getMessage(),th);
			// should never happen
		}
		return outParamWrapperInstance;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static <PW extends ParametersWrapperBase<PW>> PW _loadFromString(final Class<PW> pwType,
																			   final String paramsStr,
								   											   final boolean decodeParamValues) {
		if (Strings.isNullOrEmpty(paramsStr)) return null;
		
		Map<String,String> paramMap = Maps.newHashMap();
		Iterable<String> params = Strings.of(paramsStr.trim())
								 		 .splitter(DEFAUL_PARAM_SPLIT_CHAR)
								 		 .split();
		Iterator<String> paramsIt = params.iterator();
		while(paramsIt.hasNext()) {
			String param = paramsIt.next();
			Matcher m = DEFAULT_PARAM_VALUE_SPLIT_PATTERN.matcher(param);
			if (m.find()) {
				String paramName = m.group(1).trim();
				String paramValue = m.group(2).trim();
				String theParamValue = decodeParamValues ? DEFAULT_URL_ENCODE_PARAM_VALUE_ENCODER_DECODER.decodeValue(paramValue)
														 : paramValue;
				paramMap.put(paramName,theParamValue);
			} else {
				String paramName = param;
				String theParamValue = "";
				paramMap.put(paramName,theParamValue);
			}
		}
		return _createParameterWrapperInstanceFromElements(pwType,
														   paramMap);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds a param from its name and value
	 * @param pwType the type of the {@link ParametersWrapperBase} instance
	 * @param paramWrapper 
	 * @param paramName
	 * @param paramValue
	 * @return
	 */
	public static <PW extends ParametersWrapperBase<PW>> PW join(final Class<PW> pwType,
																 final PW paramWrapper,
																 final String paramName,final String paramValue) {
		if (Strings.isNullOrEmpty(paramValue)) return paramWrapper;
		
		Map<String,String> newParams = Maps.newHashMapWithExpectedSize(paramWrapper.getParams().size() + 1);
		newParams.putAll(paramWrapper.getParams());
		newParams.put(paramName,paramValue);
		return _createParameterWrapperInstanceFromElements(pwType,
														   newParams);
	}
	/**
	 * Adds a param from its name and value which is provided by a {@link ParamValueProvider}
	 * @param pwType the type of the {@link ParametersWrapperBase} instance
	 * @param paramWrapper 
	 * @param paramValueProvider the param value provider to be used to get the value at runtime
	 * @return
	 */
	public static <PW extends ParametersWrapperBase<PW>> PW join(final Class<PW> pwType,
														   	  	 final PW paramWrapper,
														   	  	 final String paramName,final Provider<String> paramValueProvider) {
		String paramValue = paramValueProvider.provideValue();
		return ParametersWrapperBase.join(pwType,
					 					  paramWrapper,
					 					  paramName,paramValue);
	}
	public static <PW extends ParametersWrapperBase<PW>> PW join(final Class<PW> pwType,
																 final PW paramWrapper,
																 final PW otherParamWrapper) {
		if (otherParamWrapper == null) return paramWrapper;
		if (paramWrapper == null) return otherParamWrapper;
		
		Map<String,String> params = paramWrapper.getParams();
		Map<String,String> otherParams = otherParamWrapper.getParams();
		Map<String,String> allParams = Maps.newHashMapWithExpectedSize(params.size() + otherParams.size());
		allParams.putAll(params);
		allParams.putAll(otherParams);
		return _createParameterWrapperInstanceFromElements(pwType,
														   allParams);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ACCESSORS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a param value from it's name
	 * @param paramName
	 * @return
	 */
	public String getParamValue(final String paramName) {
		return _params != null ? _params.get(paramName)
							   : null;
	}
	/**
	 * Returns a param in the format to be placed at the query string (paramName=paramValue)
	 * NO url encoding is made
	 * @param paramName
	 * @return
	 */
	public String serializeParamNameAndValue(final String paramName,
											 final boolean encodeParamValue) {
		return CollectionUtils.hasData(_params) ? _serializeParamNameAndValue(paramName,_params.get(paramName),
																			  encodeParamValue)
												: null;
	}
	private String _serializeParamNameAndValue(final String paramName,final String paramValue,
											   final boolean encodeParamValue) {
		String outValueFormated = null;
		if (Strings.isNOTNullOrEmpty(paramValue)) {
			String theParamValue = encodeParamValue && _paramValueEncoderDecoder != null ? _paramValueEncoderDecoder.encodeValue(paramValue)
																	 					 : paramValue;
			outValueFormated = Strings.of(_paramAndValueSerializeTemplate)
									  .customizeWith(paramName,theParamValue)
									  .asString();
		} else {
			outValueFormated = paramName;
		}
		return outValueFormated;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the params string built from the params encoding the param 
	 * @return
	 */
	public String asStringEncodingParamValues() {
		return this.asString(true);
	}
	/**
	 * Returns the params string built from the params NOT encoding the param values
	 * @return
	 */
	public String asStringNotEncodingParamValues() {
		return this.asString(false);
	}
	/**
	 * Returns the params string built from the params
	 * @return
	 */
	public String asString() {
		return this.asString(false);
	}
	/**
	 * Returns the params string built from the params encoding the param values as specified
	 * @param encodeParamValues
	 * @return
	 */
	public String asString(final boolean encodeParamValues) {
		String outStr = null;
		if (CollectionUtils.hasData(_params)) {
			StringBuilder paramsSB = new StringBuilder();
			for (Iterator<Map.Entry<String,String>> meIt = _params.entrySet().iterator(); meIt.hasNext(); ) {
				Map.Entry<String,String> me = meIt.next();
				
				String paramNameAndValue = _serializeParamNameAndValue(me.getKey(),me.getValue(),
																	   encodeParamValues);
				
				if (Strings.isNOTNullOrEmpty(paramNameAndValue)) {
					paramsSB.append(paramNameAndValue);
					if (meIt.hasNext()) paramsSB.append(_paramSplitChar);	// params separator
				}
			}
			outStr = paramsSB.toString();
		}
		return outStr;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		return this.asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EQUALS & HASHCODE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean equals(final Object other) {
		if (other instanceof ParametersWrapperBase
		 && other.getClass() == this.getClass()) {
				ParametersWrapperBase<?> pw = (ParametersWrapperBase<?>)other;
				Map<String,String> otherParams = pw.getParams();
				MapDifference<String,String> diff = Maps.difference(_params,otherParams);
				return diff.entriesInCommon().size() == _params.size() && diff.entriesInCommon().size() == otherParams.size();
		}
		return false;
	}
	@Override
	public int hashCode() {
		return this.asString().hashCode();
	}
}
