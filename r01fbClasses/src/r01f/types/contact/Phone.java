package r01f.types.contact;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.patterns.Memoized;
import r01f.types.annotations.Inmutable;
import r01f.util.types.Strings;

import com.google.common.base.Preconditions;



@XmlRootElement(name="phone")
@Inmutable
@Accessors(prefix="_")
@NoArgsConstructor
@Slf4j
public class Phone 
     extends ValidatedContactID {
	
	private static final long serialVersionUID = 2718728842252439399L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern VALID_PHONE_COUNTRY_CODE = Pattern.compile("\\+[0-9]{2}");
	public static final Pattern VALID_PHONE_FORMAT_PATTERN = Pattern.compile("(" + VALID_PHONE_COUNTRY_CODE + ")?" +
																			 "([0-9]{9})");	
	private Memoized<Boolean> _valid = new Memoized<Boolean>() {
												@Override
												protected Boolean supply() {
													return VALID_PHONE_FORMAT_PATTERN.matcher(Phone.this.asString()).find();
												}
									   };
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public Phone(final String phone) {
		super(phone);
	}
	public static Phone of(final String phone) {
		return Strings.isNOTNullOrEmpty(phone) ? new Phone(phone)
											   : null;
	}
	public static Phone valueOf(final String phone) {
		return Phone.of(phone);
	}
	public static Phone create(final String phone) {
		return Phone.of(phone);
	}
	public static Phone createValidating(final String phone) {
		if (!Phone.create(phone).isValid()) throw new IllegalArgumentException("Not a valid phone number!!");
		return Phone.of(phone);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
///////////////////////////////////////////////////////////////////////////////////////// 
	@Override
	public boolean isValid() {
		return _valid.get();
	}
	@Override
	public String asString() {
		return _sanitize(this.getId());	// remove all non-numeric or + characters
	}
	@Override
	public String toString() {
		return this.asString();
	}
	@Override
	public boolean equals(final Object other) {
		if (this == other) {
			return true;
		} else if (other instanceof Phone) {
			Phone otherPhone = (Phone)other;
			return _sanitize(this.getId()).equals(otherPhone.toString());
		} else {
			return false;
		}
	}
	@Override
	public int hashCode() {
		return _sanitize(this.getId()).hashCode();
	}
	public String asStringWithoutCountryCode() {
		Matcher m = VALID_PHONE_FORMAT_PATTERN.matcher(this.asStringEnsuringCountryCode("+00"));	// this will throw an exception if the phone number is invalid
		String outPhone = m.find() ? m.group(2)	// phone (without country code)
								   : null;		// imposible
		return outPhone;
	}
	/**
	 * Returns the phone ensuring that it's prefixed with a provided country code
	 * it it's NOT already present 
	 * @param defaultCountryCode
	 */
	public String asStringEnsuringCountryCode(final String defaultCountryCode) {
		Preconditions.checkArgument(defaultCountryCode.length() == 3 && VALID_PHONE_COUNTRY_CODE.matcher(defaultCountryCode).find(),
									"The provided default phone country code %s is NOT valid",defaultCountryCode);
		String outPhone = null;
		Matcher m = VALID_PHONE_FORMAT_PATTERN.matcher(this.asString());
		if (m.find()) {
			String countryCode = null;
			String phoneNumber = null;
			if (m.groupCount() == 3) {
				countryCode = m.group(1);
				phoneNumber = m.group(2);
			} else {
				countryCode = defaultCountryCode;
				phoneNumber = m.group(2);
			}
			if (countryCode != null && !countryCode.equals(defaultCountryCode)) {
				log.info("The phone {} has a country code={} which does NOT match the provided default country code={}: {} will be returned",
						 this.asString(),countryCode,defaultCountryCode,countryCode);
			}			
			outPhone = countryCode + phoneNumber;
		} else {
			throw new IllegalStateException(Throwables.message("The phone number does NOT have a valid format: {}",
															   VALID_PHONE_FORMAT_PATTERN));
		}
		return outPhone;
	}
	/**
	 * Sanitizes the phone number removing all non-numeric or +
	 * characters
	 * @param phoneAsString 
	 * @return
	 */
	private static String _sanitize(final String phoneAsString) {
		String outPhone = Strings.of(phoneAsString)
								 .replaceAll("[^0-9^\\+]","")
								 .asString();
		return outPhone;
	}
}
