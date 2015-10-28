package r01f.types.weburl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.types.annotations.Inmutable;
import r01f.util.types.StringEncodeUtils;
import r01f.util.types.Strings;

@ConvertToDirtyStateTrackable
@Inmutable
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
public abstract class WebUrlQueryStringParamBase
		   implements WebUrlQueryStringParam {

	private static final long serialVersionUID = 2469798253802346787L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="name")
	@Getter @Setter protected String _name;
	
	@XmlValue
	@Getter @Setter protected String _value;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasData() {
		return Strings.isNOTNullOrEmpty(_value);
	}
	@Override
	public String valueAsString() {
		return _value;
	}
	@Override
	public String valueAsStringUrlEncoded() {
		return StringEncodeUtils.urlEncodeNoThrow(_value)
								.toString();
	}
	@Override
	public String asStringUrlEncoded() {
		return Strings.of("{}={}")
					  .customizeWith(_name,StringEncodeUtils.urlEncodeNoThrow(_value))
					  .asString();
	}
	@Override
	public String asString() {
		return Strings.of("{}={}")
					  .customizeWith(_name,_value)
					  .asString();
	}
	@Override
	public String asString(boolean encodeValues) {
		return encodeValues ? this.asStringUrlEncoded()
							: this.asString();
	}
	@Override
	public String toString() {
		return this.asString();
				
	}
	
}
