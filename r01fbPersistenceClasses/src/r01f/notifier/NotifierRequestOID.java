package r01f.notifier;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.guids.OID;
import r01f.guids.OIDBaseInmutable;
import r01f.guids.SuppliesOID;

import com.google.common.annotations.GwtIncompatible;

/**
 * A {@link NotifierRequest} {@link OID}
 */
@GwtIncompatible("does not have de default no-args constructor")
@XmlRootElement(name="notifierRequest")
public class NotifierRequestOID 
	 extends OIDBaseInmutable<String>
  implements SuppliesOID {

	private static final long serialVersionUID = -8429215763429456025L;
	
	protected NotifierRequestOID(final String id) {
		super(id);
	}
	public static NotifierRequestOID of(final String id) {
		return new NotifierRequestOID(id);
	}
	public static NotifierRequestOID valueOf(final String id) {
		return new NotifierRequestOID(id);
	}
}
