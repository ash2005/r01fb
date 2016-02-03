package r01f.types.url;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.guids.OIDBaseMutable;

@XmlRootElement(name="host")
public class Host
	 extends OIDBaseMutable<String> {
	
	private static final long serialVersionUID = -3712825671090881670L;
	
	public Host() {
		/* default no args constructor for serialization purposes */
	}
	public Host(final String host) {
		super(host);
	}
	public Host(final Host other) {
		this(other != null ? other.getId() : (String)null);
	}
	public static Host of(final String host) {
		return new Host(host);
	}
	public static Host valueOf(final String host) {
		return new Host(host);
	}
	public static Host localhost() {
		return new Host("localhost");
	}
}
