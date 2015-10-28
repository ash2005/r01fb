package r01f.types.contact;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.marshalling.annotations.XmlCDATA;


/**
 * Contact's social network
 * <pre class='brush:java'>
 *	ContactSocialNetwork user = ContactSocialNetwork.createToBeUsedFor(ContactInfoUsage.PERSONAL)
 *													.forNetwork(R01MContactSocialNetworkType.TWITTER)
 *												   	.user("futuretelematics")
 *													.profileAt("http://twitter.com/futuretelematics");
 * </pre>
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="socialNetworkData")
@Accessors(prefix="_")
@NoArgsConstructor
public class ContactSocialNetwork 
     extends ContactInfoMediaBase<ContactSocialNetwork> {
	
	private static final long serialVersionUID = 4611690233960483088L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Social network type: twitter, facebook, youtube, tec
	 */
	@XmlAttribute(name="type")
	@Getter @Setter private ContactSocialNetworkType _type;
	/**
	 * Phone number
	 */
	@XmlAttribute(name="user")
	@Getter @Setter private String _user;
	/**
	 * Profile url (ie: twitter.com/futuretelematics)
	 */
	@XmlElement @XmlCDATA
	@Getter @Setter private String _profileUrl;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static ContactSocialNetwork createToBeUsedFor(final ContactInfoUsage usage) {
		ContactSocialNetwork outNetwork = new ContactSocialNetwork();
		outNetwork.usedFor(usage);
		return outNetwork;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public ContactSocialNetwork forNetwork(final ContactSocialNetworkType type) {
		_type = type;
		return this;
	}
	public ContactSocialNetwork user(final String user) {
		_user = user;
		return this;
	}
	public ContactSocialNetwork profileAt(final String profileUrl) {
		_profileUrl = profileUrl;
		return this;
	}
}
