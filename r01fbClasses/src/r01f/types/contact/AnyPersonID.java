package r01f.types.contact;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.annotations.GwtIncompatible;

import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.guids.OIDBaseMutable;
import r01f.types.annotations.Inmutable;

@ConvertToDirtyStateTrackable
@Inmutable
@XmlRootElement(name="personId")
public class AnyPersonID 
     extends OIDBaseMutable<String> 	// normally this should extend OIDBaseInmutable BUT it MUST have a default no-args constructor to be serializable
  implements PersonID {
	
	private static final long serialVersionUID = 4475634008696904179L;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public AnyPersonID() {
		/* default no args constructor for serialization purposes */
	}
	public AnyPersonID(final String id) {
		super(id);	// normalize!!
	}
	public static AnyPersonID valueOf(final String s) {
		return new AnyPersonID(s);
	}
	public static AnyPersonID forId(final String id) {
		return new AnyPersonID(id);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @GwtIncompatible(value = " Not Compatible for GWT")
	public boolean isValid() {
		return true;
	}
}
