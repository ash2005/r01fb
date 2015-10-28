package r01f.types;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;

/**
 * Coordinates
 */
@Inmutable
@XmlRootElement(name="xy")
@Accessors(prefix="_")
@RequiredArgsConstructor
public class XY 
  implements Serializable {

	private static final long serialVersionUID = -3175355518015641559L;

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS 
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="x")
	@Getter private final long _x;
	
	@XmlAttribute(name="y")
	@Getter private final long _y;
}
