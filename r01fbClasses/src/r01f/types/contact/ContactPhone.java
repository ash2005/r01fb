package r01f.types.contact;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.types.Range;


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
	
	private static final long serialVersionUID = 5655704363671006182L;
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
	 * hour range when could be contacted
	 */
	@XmlAttribute(name="availability")
	@Getter @Setter private Range<Integer> _availableRangeForCalling;
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
	public ContactPhone availableRangeForCalling(final Range<Integer> range) {
		_availableRangeForCalling = range;
		return this;
	}
}
