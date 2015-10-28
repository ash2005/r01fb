package r01f.util.types;

import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Maps;

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
@RequiredArgsConstructor(access=AccessLevel.PROTECTED)
public abstract class ParametersWrapperBase<SELF_TYPE extends ParametersWrapperBase<SELF_TYPE>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static final char DEFAUL_PARAM_SPLIT_CHAR = '&';
	protected static final Pattern DEFAULT_PARAM_VALUE_SPLIT_PATTERN = Pattern.compile("([^=]+)=(.+)");
	protected static final String DEFAULT_PARAM_VALUE_SERIALIZE_TEMPLATE = "{}= {}";
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
	@Getter private Map<String,String> _params;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ParametersWrapperBase() {
		_paramSplitChar = DEFAUL_PARAM_SPLIT_CHAR;
		_paramAndValueSplitPattern = DEFAULT_PARAM_VALUE_SPLIT_PATTERN;
		_paramAndValueSerializeTemplate = DEFAULT_PARAM_VALUE_SERIALIZE_TEMPLATE;
		_paramValueEncoderDecoder = null;
	}
	public ParametersWrapperBase(final ParamValueEncoderDecoder encoderDecoder) {
		_paramSplitChar = DEFAUL_PARAM_SPLIT_CHAR;
		_paramAndValueSplitPattern = DEFAULT_PARAM_VALUE_SPLIT_PATTERN;
		_paramAndValueSerializeTemplate = DEFAULT_PARAM_VALUE_SERIALIZE_TEMPLATE;
		_paramValueEncoderDecoder = encoderDecoder;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public interface ParamValueProvider {
		public String provideValue();
	}
	public interface ParamValueEncoderDecoder {
		public String encodeValue(final String value);
		public String decodeValue(final String value);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds a param from its name and value
	 * @param paramName
	 * @param paramValue
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE addParam(final String paramName,final String paramValue) {
		if (Strings.isNOTNullOrEmpty(paramValue)) {
			if (_params == null) _params = Maps.newLinkedHashMap();
			_params.put(paramName,
					    paramValue);
		}
		return (SELF_TYPE)this;
	}
	/**
	 * Adds a param from its name and value which is provided by a {@link ParamValueProvider}
	 * @param paramName the param name
	 * @param paramValueProvider the param value provider to be used to get the value at runtime
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public SELF_TYPE addParam(final String paramName,
							  final ParamValueProvider paramValueProvider) {
		String paramValue = paramValueProvider.provideValue();
		if (Strings.isNOTNullOrEmpty(paramValue)) this.addParam(paramName,paramValue);
		return (SELF_TYPE)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _loadFromString(final String paramsStr,
								   final boolean decodeParamValues) {
		Iterable<String> params = Strings.of(paramsStr)
								 		 .splitter(_paramSplitChar)
								 		 .split();
		Iterator<String> paramsIt = params.iterator();
		while(paramsIt.hasNext()) {
			String param = paramsIt.next();
			Matcher m = _paramAndValueSplitPattern.matcher(param);
			if (m.find()) {
				String paramName = m.group(1).trim();
				String paramValue = m.group(2).trim();
				String theParamValue = decodeParamValues && _paramValueEncoderDecoder != null ? _paramValueEncoderDecoder.decodeValue(paramValue)
																		 					  : paramValue;
				this.addParam(paramName,theParamValue);
			}			
		}
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
//  
/////////////////////////////////////////////////////////////////////////////////////////
}
