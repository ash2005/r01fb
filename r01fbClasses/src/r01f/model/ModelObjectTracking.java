package r01f.model;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.guids.CommonOIDs.UserCode;

/**
 * Object's Tracking info (author/a, create date, update date, etc)
 */

@ConvertToDirtyStateTrackable
@XmlRootElement(name="tracking")
@Accessors(prefix="_")
@NoArgsConstructor
public class ModelObjectTracking 
  implements Serializable {
	
	private static final long serialVersionUID = 2286660970580116262L;
///////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Object create date
     */
	@XmlAttribute(name="createDate")
    @Getter @Setter private Date _createDate =  new Date();//Not compatible in GWT :Calendar.getInstance().getTime();
    /**
     * Creator user code
     */
	@XmlAttribute(name="creatorUserCode")
    @Getter @Setter private UserCode _creatorUserCode;
    /**
     * Object's create date
     */
	@XmlAttribute(name="lastUpdate")
    @Getter @Setter private Date _lastUpdateDate =  new Date(); //Not compatible in GWT :Calendar.getInstance().getTime();
    /**
     * Last update user code
     */
	@XmlAttribute(name="lastUpdaterUserCode")
    @Getter @Setter private UserCode _lastUpdatorUserCode;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ModelObjectTracking(final UserCode creator,final Date createDate) {
		this(creator,createDate,
			 creator,createDate);
	}
	public ModelObjectTracking(final UserCode creator,final Date createDate,
							   final UserCode lastUpdator,final Date lastUpdateDate) {
		_createDate = createDate;
		_lastUpdateDate = lastUpdateDate;
		_creatorUserCode = creator;
		_lastUpdatorUserCode = lastUpdator;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Sets that the provided user code has made an update just now
	 * @param userCode
	 * @return 
	 */
	public ModelObjectTracking setModifiedBy(final UserCode userCode) {
		this.setLastUpdatorUserCode(userCode);
		this.setLastUpdateDate(new Date());
		return this;
	}
}
