package r01f.types.contact;

import java.util.Collection;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.locale.Language;
import r01f.util.types.collections.CollectionUtils;

/**
 * Contact info
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="contactInfo")
@Accessors(prefix="_")
@NoArgsConstructor
public class ContactInfo 
     extends ContactInfoBase<ContactInfo> {
	
	private static final long serialVersionUID = 8960930452114541680L;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Phones
	 */
	@XmlElementWrapper(name="phoneChannels")
	@Getter @Setter private Collection<ContactPhone> _phones;
	/**
	 * Social network users
	 */
	@XmlElementWrapper(name="socialNetworkChannels")
	@Getter @Setter private Collection<ContactSocialNetwork> _socialNetworks;
	/**
	 * email 
	 */
	@XmlElementWrapper(name="emailChannels")
	@Getter @Setter private Collection<ContactMail> _mailAddresses;
	/**
	 * web sites
	 */
	@XmlElementWrapper(name="webSiteChannels")
	@Getter @Setter private Collection<ContactWeb> _webSites;
	/**
	 * The language the user prefers to be used in the interaction with him/her 
	 */
	@XmlElement(name="preferredLanguage")
	@Getter @Setter private Language _preferedLanguage;
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static ContactInfo create() {
		ContactInfo outContact = new ContactInfo();
		return outContact;
	}	
/////////////////////////////////////////////////////////////////////////////////////////
// 	MAIL
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if there are mail address associated with the contact info
	 */
	public boolean hasMailAddress() {
		return CollectionUtils.hasData(_mailAddresses);
	}
	/**
	 * Returns an email address for an intended usage
	 * @param id
	 * @return
	 */
	public EMail getMailAddress(final ContactInfoUsage usage) {
		ContactMail mail = _find(_mailAddresses,usage);
		return mail != null ? mail.getMail() : null;
	}
	/**
	 * Returns an email addess for an intended usage or any if the requested one
	 * does not exists
	 * @param usage
	 * @return
	 */
	public EMail getMailAddressOrAny(final ContactInfoUsage usage) {
		EMail outMail = this.getMailAddress(usage);
		if (outMail == null && CollectionUtils.hasData(_mailAddresses)) {
			ContactMail mail = CollectionUtils.pickOneElement(_mailAddresses);
			if (mail != null) outMail = mail.getMail();
		}
		return outMail;
	}
	/**
	 * Returns the default email address
	 * @return
	 */
	public EMail getDefaultMailAddress() {
		ContactMail mail = _findDefault(_mailAddresses);
		return mail != null ? mail.getMail() : null;
	}
	/**
	 * Returns the default email address or any of them if none is set as default one
	 * @return
	 */
	public EMail getDefaultMailAddressOrAny() {
		ContactMail mail = _findDefault(_mailAddresses);
		if (mail == null && CollectionUtils.hasData(_mailAddresses)) mail = CollectionUtils.pickOneElement(_mailAddresses);
		return mail != null ? mail.getMail() : null;
	}
	/**
	 * Returns a mail address (if there is more than one it returns one of them randomly)
	 * @return
	 */
	public EMail getMailAddress() {
		// Try to find the default 
		EMail mail = this.getDefaultMailAddress();
		// ... if there's NO default, return any of them
		if (mail == null) {
			ContactMail contactMail = _findOne(_mailAddresses);
			mail = contactMail != null ? contactMail.getMail() : null;
		}
		return mail;
	}
	/**
	 * Returns a {@link Collection} of {@link EMail}s
	 * @return
	 */
	public Collection<EMail> getMailAddreses() {
		Collection<EMail> mails = CollectionUtils.hasData(_mailAddresses)  
										? FluentIterable.from(_mailAddresses)
														.filter(new Predicate<ContactMail>() {
																	@Override
																	public boolean apply(final ContactMail contactMail) {
																		return contactMail.getMail() != null;
																	}
																})
														.transform(new Function<ContactMail,EMail>() {
																		@Override
																		public EMail apply(final ContactMail contactMail) {
																			return contactMail.getMail();
																		}
															     })
														.toList()
										 : null;
		return mails;
	}
	/**
	 * Returns the mail addresses as a comma separated list
	 * @param separator
	 * @return
	 */
	public String getMailAddresesCharSeparated(final char separator) {
		Collection<EMail> mails = this.getMailAddreses();
		return CollectionUtils.hasData(mails)
					? CollectionUtils.of(mails)
									 .toStringSeparatedWith(separator)
				    : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	PHONE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if there are phones associated with the contact info
	 */
	public boolean hasPhones() {
		return CollectionUtils.hasData(_phones);
	}
	/**
	 * Returns a phone for an intended usage
	 * @param id
	 * @return
	 */
	public Phone getPhone(final ContactInfoUsage usage) {
		ContactPhone phone = _find(_phones,usage);
		return phone != null ? phone.getNumber() : null;
	}
	/**
	 * Returns the default phone number
	 * @return
	 */
	public Phone getDefaultPhone() {
		ContactPhone phone = _findDefault(_phones);
		return phone != null ? phone.getNumber() : null;
	}
	/**
	 * Returns the default phone number or any of them if any is set as the default one
	 * @return
	 */
	public Phone getDefaultPhoneOrAny() {
		ContactPhone phone = _findDefault(_phones);
		if (phone == null && CollectionUtils.hasData(_phones)) phone = CollectionUtils.pickOneElement(_phones);
		return phone != null ? phone.getNumber() : null;
	}
	/**
	 * Returns a phone number (if there is more than one it returns one of them randomly)
	 * @return
	 */
	public Phone getPhone() {
		// Try to find the default 
		Phone phone = this.getDefaultPhone();
		// ... if there's NO default, return any of them
		if (phone == null) {
			ContactPhone contactPhone = _findOne(_phones);
			phone = contactPhone != null ? contactPhone.getNumber() : null;
		}
		return phone;
	}
	/**
	 * Returns a {@link Collection} of {@link Phone} numbers
	 * @return
	 */
	public Collection<Phone> getPhoneNumbers() {
		Collection<Phone> phones = CollectionUtils.hasData(_phones)  
										? FluentIterable.from(_phones)
														.filter(new Predicate<ContactPhone>() {
																		@Override
																		public boolean apply(final ContactPhone contactPhone) {
																			return contactPhone.getNumber() != null;
																		}
																})
													    .transform(new Function<ContactPhone,Phone>() {
																		@Override
																		public Phone apply(final ContactPhone contactPhone) {
																			return contactPhone.getNumber();
																		}
															       })
													    .toList()
										 : null;
		return phones;
	}
	/**
	 * Returns the mail addresses as a comma separated list
	 * @param separator
	 * @return
	 */
	public String getPhonesCharSeparated(final char separator) {
		Collection<Phone> phones = this.getPhoneNumbers();
		return CollectionUtils.hasData(phones)
					? CollectionUtils.of(phones)
									 .toStringSeparatedWith(separator)
				    : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	public ContactInfo addPhone(final ContactPhone phone) {
		if (_phones == null) _phones = Lists.newArrayList();
		_phones.add(phone);
		return this;
	}
	public ContactPhone removePhone(final ContactInfoUsage usage) {
		ContactPhone outPhone = _find(_phones,usage);
		if (outPhone != null) _socialNetworks.remove(outPhone);
		return outPhone;
	}
	public ContactInfo addSocialNetwork(final ContactSocialNetwork net) {
		if (_socialNetworks == null) _socialNetworks = Lists.newArrayList();
		_socialNetworks.add(net);
		return this;
	}
	public ContactSocialNetwork removeSocialNetwork(final ContactInfoUsage id) {
		ContactSocialNetwork outNet = _find(_socialNetworks,id);
		if (outNet != null) _socialNetworks.remove(outNet);
		return outNet;
	}
	public ContactInfo addMailAddress(final ContactMail mail) {
		if (_mailAddresses == null) _mailAddresses = Lists.newArrayList();
		_mailAddresses.add(mail);
		return this;
	}
	public ContactMail removeMailAddress(final ContactInfoUsage usage) {
		ContactMail outMail = _find(_mailAddresses,usage);
		if (outMail != null) _mailAddresses.remove(outMail);
		return outMail;
	}
	public ContactInfo addWebSite(final ContactWeb web) {
		if (_webSites == null) _webSites = Lists.newArrayList();
		_webSites.add(web);
		return this;
	}
	public ContactWeb removeWebSite(final ContactInfoUsage usage) {
		ContactWeb outWeb = _find(_webSites,usage);
		if (outWeb != null) _webSites.remove(outWeb);
		return outWeb;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	private static <M extends ContactInfoMediaBase<?>> M _find(final Collection<? extends ContactInfoMediaBase<?>> col,
													 		   final ContactInfoUsage usage) {
		M out = null;
		if (CollectionUtils.hasData(col)) out = (M)CollectionUtils.of(col)
														     	  .findFirstElementMatching(new Predicate<ContactInfoMediaBase<?>>() {
																									@Override
																									public boolean apply(final ContactInfoMediaBase<?> el) {
																										return el.getUsage() == usage;
																									}
															  							    });
		return out;
	}
	@SuppressWarnings("unchecked")
	private static <M extends ContactInfoMediaBase<?>> M _findDefault(final Collection<? extends ContactInfoMediaBase<?>> col) {
		M out = null;
		if (CollectionUtils.hasData(col)) out = (M)CollectionUtils.of(col)
														     	  .findFirstElementMatching(new Predicate<ContactInfoMediaBase<?>>() {
																									@Override
																									public boolean apply(final ContactInfoMediaBase<?> el) {
																										return el.isDefault();
																									}
															  							    });
		return out;
	}
	@SuppressWarnings("unchecked")
	private static <M extends ContactInfoMediaBase<?>> M _findOne(final Collection<? extends ContactInfoMediaBase<?>> col) {
		M out = null;
		if (CollectionUtils.hasData(col)) out = (M)CollectionUtils.of(col)
																  .pickOneElement();
		return out;
	}
}
