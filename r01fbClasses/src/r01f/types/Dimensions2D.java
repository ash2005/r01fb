package r01f.types;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.types.annotations.Inmutable;

/**
 * Object dimensions (ie: an image)
 */
@ConvertToDirtyStateTrackable
@XmlRootElement(name="dimensions2D")
@Inmutable
@Accessors(prefix="_")
@RequiredArgsConstructor
public class Dimensions2D implements Serializable {
	private static final long serialVersionUID = -7580079139588296207L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * width
	 */
	@XmlAttribute(name="width")
	@Getter private final int _width;
	/**
	 * heigh
	 */
	@XmlAttribute(name="height")
	@Getter private final int _height;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
}
