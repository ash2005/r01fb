package r01f.types.contact;

import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.annotations.GwtIncompatible;

import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;
import r01f.util.types.Strings;



@XmlRootElement(name="email")
@Inmutable
@NoArgsConstructor
@Accessors(prefix="_")
public class EMail 
     extends ValidatedContactID {
	
	private static final long serialVersionUID = -6976066522439926427L;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public EMail(final String mail) {
		super(mail);
	}
	public static EMail of(final String mail) {
		return Strings.isNOTNullOrEmpty(mail) ? new EMail(mail)
											  : null;
	}
	public static EMail valueOf(final String mail) {
		return EMail.of(mail);
	}
	public static EMail create(final String mail) {
		return EMail.of(mail);
	}
	@GwtIncompatible(value = "Not Compatible for GWT")
	public static EMail createValidating(final String mail) {
		if (EMail.validate(mail)) throw new IllegalArgumentException("Not a valid email address!!");
		return EMail.of(mail);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	@GwtIncompatible(value = "Not Compatible for GWT")
	public boolean isValid() {
		return EMail.validate(this.asString());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Validates eMail
	 * @param emailStr
	 * @return
	 */
	public static boolean validate(final String emailStr) {
		if (Strings.isNullOrEmpty(emailStr)) return false;
		
		boolean outValid = true;
		
		int atIndex = emailStr.indexOf('@');
		if (atIndex <= 0) {
			outValid = false;
		} else {
			String domain = emailStr.substring(atIndex+1);
			String local = emailStr.substring(0,atIndex);
			
			int domainLength = domain.length();
			int localLength = local.length();
			
			if (localLength < 1 || localLength > 64) {
				outValid = false;
			} else if (domainLength < 1 || domainLength > 255) {
				outValid = false;
			} else if (local.charAt(0) == '.' || local.charAt(localLength-1) == '.') {
				outValid = false;
			} else if (local.contains("..")) {
				outValid = false;	// local part has two consecutive dots
			} else if (!domain.matches("^[A-Za-z0-9\\-\\.]+$")) {
				outValid = false;
			} else if (domain.contains("..")) {
				outValid = false;	// domain part has two consecutive dots
			} else if (!local.replaceAll("\\\\","")
							 .matches("^(\\\\.|[A-Za-z0-9!#%&`_=\\/$\'*+?^{}|~.-])+$")) {
				// character not valid in local part unless local part is quoted
				if (!local.replaceAll("\\\\","")
							 .matches("^\"(\\\\\"|[^\"])+\"$")) {
					outValid = false;
				}
			}
//			// Domain checking
//			if (outValid && !(checkDNSrecord(domain,"MX") || checkDNSrecord(domain,"A"))) {
//				outValid = false;
//			}
		}
		return outValid;
	}

}
