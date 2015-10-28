package r01f.guids;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;

/**
 * Models an oid by encapsulating an id that can be either a String, an int, a long, etc
 * @param <T> the type of the id
 */
@Inmutable
@Accessors(prefix="_")
@EqualsAndHashCode(callSuper=false)
public abstract class OIDBaseInmutable<T>
	extends OIDBase<T> {
	
	private static final long serialVersionUID = 3256491732245845341L;
///////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="oid")		// Esta anotación es importante para serializar a xml
	@Getter private final T _id;
///////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	protected OIDBaseInmutable(final T id) {
		if (id == null) throw new IllegalArgumentException("An OID cannot be created with null value!");
		_id = id;
	}
}
