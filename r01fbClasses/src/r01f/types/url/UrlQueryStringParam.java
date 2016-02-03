package r01f.types.url;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.locale.Language;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.Range;
import r01f.types.annotations.Inmutable;
import r01f.util.types.Dates;
import r01f.util.types.StringEncodeUtils;
import r01f.util.types.Strings;

@ConvertToDirtyStateTrackable
@Inmutable
@XmlRootElement(name="param")
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public class UrlQueryStringParam
  implements CanBeRepresentedAsString,
	   	     Serializable {

	private static final long serialVersionUID = 2469798253802346787L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="name")
	@Getter @Setter protected String _name;
	
	@XmlValue
	@Getter @Setter protected String _value;
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static UrlQueryStringParam of(final String paramName,final String paramValue) {
		UrlQueryStringParam outParam = new UrlQueryStringParam(paramName,paramValue);
		return outParam;
	}
	public static UrlQueryStringParam from(final String paramAndValue) {
		String[] paramAndValueSplitted = paramAndValue.split("=");
		if (paramAndValueSplitted.length == 2) {
			return UrlQueryStringParam.of(paramAndValueSplitted[0],Strings.of(paramAndValueSplitted[1])
																		  .urlDecodeNoThrow()		// ensure the param is decoded
																		  .asString());
		} else if (paramAndValueSplitted.length == 1) {
			return UrlQueryStringParam.of(paramAndValueSplitted[0],(String)null);
		} else {
			// sometimes a param value includes = (ie: W=sco_serie=11+and+sco_freun=20100421+order+by+sco_freun,sco_nasun)
			String paramName = paramAndValueSplitted[0];
			StringBuilder paramValue = new StringBuilder();
			for (int i=1; i < paramAndValueSplitted.length; i++) {
				paramValue.append(paramAndValueSplitted[i]);
				if (i < paramAndValueSplitted.length-1) paramValue.append("=");
			}
			return UrlQueryStringParam.of(paramName,paramValue.toString());
		}
	}
	public static UrlQueryStringParam of(final Language lang) {
		return UrlQueryStringParam.of("lang",lang.name());
	}
	public static UrlQueryStringParam of(final String paramName,final Language lang) {
		return new UrlQueryStringParam(paramName,lang.name());
	}
	public static UrlQueryStringParam of(final String paramName,final boolean val) {
		return new UrlQueryStringParam(paramName,Boolean.toString(val));
	}	
	public static UrlQueryStringParam of(final String paramName,final int num) {
		return new UrlQueryStringParam(paramName,Integer.toString(num));
	}
	public static UrlQueryStringParam of(final String paramName,final long num) {
		return new UrlQueryStringParam(paramName,Long.toString(num));
	}
	public static UrlQueryStringParam of(final String paramName,final double num) {
		return new UrlQueryStringParam(paramName,Double.toString(num));
	}
	public static UrlQueryStringParam of(final String paramName,final short num) {
		return new UrlQueryStringParam(paramName,Short.toString(num));
	}
	public static UrlQueryStringParam of(final String paramName,final float num) {
		return new UrlQueryStringParam(paramName,Float.toString(num));
	}
	public static UrlQueryStringParam of(final String paramName,final Date date) {
		return new UrlQueryStringParam(paramName,Long.toString(Dates.asEpochTimeStamp(date)));
	}
	public static UrlQueryStringParam of(final String paramName,final Range<Date> dateRange) {
		return new UrlQueryStringParam(paramName,dateRange.asString());
	}
	public static UrlQueryStringParam of(final String paramName,final com.google.common.collect.Range<Date> dateRange) {
		return UrlQueryStringParam.of(paramName,new Range<Date>(dateRange));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if the param contains data
	 */
	public boolean hasData() {
		return Strings.isNOTNullOrEmpty(_value);
	}
	/**
	 * @return the param value
	 */
	public String valueAsString() {
		return _value;
	}
	/**
	 * @return the param value url encoded
	 */
	public String valueAsStringUrlEncoded() {
		return StringEncodeUtils.urlEncodeNoThrow(_value)
								.toString();
	}
	/**
	 * The param as name=value with the value encoded
	 * @return
	 */
	public String asStringUrlEncoded() {
		return Strings.of("{}={}")
					  .customizeWith(_name,StringEncodeUtils.urlEncodeNoThrow(_value))
					  .asString();
	}
	/**
	 * The param as name=value with the value encoded as specified by the param
	 * @param encodeValues
	 * @return
	 */
	public String asString(boolean encodeValues) {
		return encodeValues ? this.asStringUrlEncoded()
							: this.asString();
	}
	@Override
	public String asString() {
		return Strings.of("{}={}")
					  .customizeWith(_name,_value)
					  .asString();
	}
	@Override
	public String toString() {
		return this.asString();
				
	}
	
}
