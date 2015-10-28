package r01f.types.weburl;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.types.annotations.Inmutable;
import r01f.util.types.Strings;

/**
 * An URL query string param
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="param")
@Inmutable
@Accessors(prefix="_")
@NoArgsConstructor
public class WebUrlQueryStringParamImpl 
     extends WebUrlQueryStringParamBase {
	
	private static final long serialVersionUID = 3288540339928967052L;
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public WebUrlQueryStringParamImpl(final String name,final String value) {
		super(name,value);
	}
	public static WebUrlQueryStringParam of(final String paramName,final String paramValue) {
		WebUrlQueryStringParam outParam = new WebUrlQueryStringParamImpl(paramName,paramValue);
		return outParam;
	}
	public static WebUrlQueryStringParam from(final String paramAndValue) {
		String[] paramAndValueSplitted = paramAndValue.split("=");
		if (paramAndValueSplitted.length == 2) {
			return WebUrlQueryStringParamImpl.of(paramAndValueSplitted[0],Strings.of(paramAndValueSplitted[1])
																				 .urlDecodeNoThrow()		// ensure the param is decoded
																				 .asString());
		} else if (paramAndValueSplitted.length == 1) {
			return WebUrlQueryStringParamImpl.of(paramAndValueSplitted[0],null);
		} else {
			// sometimes a param value includes = (ie: W=sco_serie=11+and+sco_freun=20100421+order+by+sco_freun,sco_nasun)
			String paramName = paramAndValueSplitted[0];
			StringBuilder paramValue = new StringBuilder();
			for (int i=1; i < paramAndValueSplitted.length; i++) {
				paramValue.append(paramAndValueSplitted[i]);
				if (i < paramAndValueSplitted.length-1) paramValue.append("=");
			}
			return WebUrlQueryStringParamImpl.of(paramName,paramValue.toString());
		}
	}
}
