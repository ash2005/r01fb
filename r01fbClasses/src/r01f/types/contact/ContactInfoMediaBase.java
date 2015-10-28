package r01f.types.contact;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.annotations.XmlWriteIgnoredIfEquals;

/**
 * Base type for every {@link ContactInfo} media related object: {@link ContactMail}, {@link ContactPhone}, {@link ContactSocialNetwork}, etc
 * @param <SELF_TYPE>
 */
@Accessors(prefix="_")
abstract class ContactInfoMediaBase<SELF_TYPE extends ContactInfoMediaBase<SELF_TYPE>>   
       extends ContactInfoBase<ContactInfoMediaBase<SELF_TYPE>> {


	private static final long serialVersionUID = 8474784639738421690L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * usage of the contact media
	 */
	@XmlAttribute(name="usage")
	@Getter @Setter private ContactInfoUsage _usage;
	/**
	 * Usage details (usually used when _usage = OTHER
	 */
	@XmlAttribute(name="usageDetails")
	@Getter @Setter private String _usageDetails;
	/**
	 * true if this media is the default one
	 */
	@XmlAttribute(name="default") @XmlWriteIgnoredIfEquals(value="false")
	@Getter @Setter private boolean _default = false;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public SELF_TYPE useAsDefault() {
		_default = true;
		return (SELF_TYPE)this;
	}
	@SuppressWarnings("unchecked")
	public SELF_TYPE usedFor(final ContactInfoUsage usage) {
		_usage = usage;
		return (SELF_TYPE)this;
	}
	@SuppressWarnings("unchecked")
	public SELF_TYPE withUsageDetails(final String details) {
		_usageDetails = details;
		return (SELF_TYPE)this;
	}
}
