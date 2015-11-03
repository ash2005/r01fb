package r01f.model;

import java.util.Date;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.debug.Debuggable;
import r01f.guids.VersionOID;
import r01f.marshalling.annotations.XmlDateFormat;
import r01f.marshalling.annotations.XmlTypeDiscriminatorAttribute;



/**
 * Version info
 */
@XmlRootElement(name="versionInfo")
@ConvertToDirtyStateTrackable
@Accessors(prefix="_")
@NoArgsConstructor
public class ModelObjectVersionInfo 
  implements Debuggable {
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Version start of use date (the date when this version is set to be active)
	 */
	@XmlElement(name="startOfUseDate") @XmlDateFormat("milis")
	@Getter @Setter private Date _startOfUseDate;
	/**
	 * Version end of use date (the date when another version was created and this one becomes obsolete)
	 */
	@XmlElement(name="endOfUseDate") @XmlDateFormat("milis")
	@Getter @Setter private Date _endOfUseDate;
	/**
	 * Next version identifier (if this version is not the current version)
	 */
	@XmlElement(name="nextVersionOid") @XmlTypeDiscriminatorAttribute(name="type")
	@Getter @Setter private VersionOID _nextVersionOid;
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public static ModelObjectVersionInfo create() {
		return new ModelObjectVersionInfo();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isActive() {
		return this.getStartOfUseDate() != null 
			&& this.getEndOfUseDate() == null;
	}
	public boolean isNotActive() {
		return this.getStartOfUseDate() != null 
			&& this.getEndOfUseDate() != null;
	}
	public boolean isDraft() {
		return this.getStartOfUseDate() == null 
			&& this.getEndOfUseDate() == null;
	}
	public void activate(final Date activationDate) {
		_startOfUseDate = activationDate;
		_nextVersionOid = null;
	}
	public void overrideBy(final VersionOID otherVersion,
    					   final Date otherVersionStartOfUseDate) {
    	_nextVersionOid = otherVersion;
    	_endOfUseDate = otherVersionStartOfUseDate;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
    public ModelObjectVersionInfo startedToBeUsedAt(final Date startOfUseDate) {
    	this.activate(startOfUseDate);
    	return this;
    }
    public ModelObjectVersionInfo overridenBy(final VersionOID otherVersion) {
    	_nextVersionOid = otherVersion;
    	_endOfUseDate = new Date();
    	return this;
    }
    public ModelObjectVersionInfo overridenBy(final VersionOID otherVersion,
    										  final Date endOfUseDate) {
    	this.overrideBy(otherVersion,
    					endOfUseDate);
    	return this;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public StringBuffer debugInfo() {
        StringBuffer sb = new StringBuffer(72);
        sb.append("\r\n\tStartOfUseDate : ");sb.append(_startOfUseDate);
        sb.append("\r\n\t  EndOfUseDate : ");sb.append(_endOfUseDate);
        sb.append("\r\n\tNextVersionOid : ");sb.append(_nextVersionOid);
        sb.append("\r\n");
        return sb;
    }
    
}
