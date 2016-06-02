package r01f.types.contact;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;

import com.google.common.annotations.GwtIncompatible;

/**
 * Contact person's phone
 * <pre class='brush:java'>
 *	ContactPhone phone = ContactPhone.createToBeUsedFor(ContactInfoUsage.PERSONAL)
 *									 .useAsDefault()
 *									 .withNumber("688671967")
 *									 .availableRangeForCalling(Ranges.closed(0,22));
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="phoneChannel")
@Accessors(prefix="_")
@NoArgsConstructor
public class ContactPhone 
	 extends ContactInfoMediaBase<ContactPhone> {
	
	private static final long serialVersionUID = 6677974112128068298L;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Phone type (mobile, non-mobile, fax, ...)
	 */
	@XmlAttribute(name="type")
	@Getter @Setter private ContactPhoneType _type = ContactPhoneType.MOBILE;
	/**
	 * Phone number
	 */
	@XmlAttribute(name="number")
	@Getter @Setter private Phone _number;
	/**
	 * Hour range when could be contacted
	 * 
	 * It is stored like a String but internaly is used like a Range<Integer>. That is to avoid GWT incompatibility.
	 * The lombok Getter and Setter are necesary because the explicit methods have the @GWTIncompatible annotation
	 * and wont be generated in the GWT compilation.
	 */
	@XmlAttribute(name="availability")
	@Getter(AccessLevel.PROTECTED) @Setter(AccessLevel.PROTECTED) private String _availableRangeForCallingStr = null;
	@GwtIncompatible(value="METHOD")
	public r01f.types.Range<Integer> getAvailableRangeForCalling() {
		return r01f.types.Range.parse(this._availableRangeForCallingStr, Integer.class);
	}
	@GwtIncompatible(value="METHOD")
	public void setAvailableRangeForCalling(r01f.types.Range<Integer> _availableRangeForCalling) {
		this._availableRangeForCallingStr = _availableRangeForCalling.asString();
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API: CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public static ContactPhone createToBeUsedFor(final ContactInfoUsage usage) {
		ContactPhone outPhone = new ContactPhone();
		outPhone.usedFor(usage);
		return outPhone;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public ContactPhone type(final ContactPhoneType type) {
		_type = type;
		return this;
	}
	public ContactPhone withNumber(final Phone number) {
		_number = number;
		return this;
	}
	public ContactPhone withNumber(final String number) {
		_number = Phone.create(number);
		return this;
	}
	
	@GwtIncompatible(value="METHOD")
	public ContactPhone availableRangeForCalling(final r01f.types.Range<Integer> range) {
		_availableRangeForCallingStr = range.asString();
		return this;
	}
	
}
