package r01f.inject;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.annotations.Inmutable;

/**
 * Guice binding reference
 * This object us useful when storing a named binding name at a config file (ie a XML)
 */
@Inmutable
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class GuiceNamedBindingBase
           implements CanBeRepresentedAsString,
           			  Serializable {
	
	private static final long serialVersionUID = 2044221458412357689L;
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS  
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="id")
	@Getter private final String _id;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String asString() {
		return _id;
	}
	@Override
	public String toString() {
		return this.asString();
	}
}
