package r01f.persistence.db;

import java.io.Serializable;
import java.util.Date;

import r01f.guids.CommonOIDs.UserCode;
import r01f.model.ModelObject;
import r01f.model.ModelObjectTracking;
import r01f.model.facets.HasEntityVersion;
import r01f.usercontext.UserContext;

/**
 * Marker interface for JPA Entity
 */
public interface DBEntity 
		 extends HasEntityVersion,
		   		 Serializable {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return create operation timestamp
	 */
	public Date getCreateTimeStamp();
	/**
	 * Sets the create operation timestamp
	 * @param newTS the new timestamp
	 */
	public void setCreateTimeStamp(Date newTS);
	/**
	 * @return the last saving operation timestamp
	 */
	public Date getLastUpdateTimeStamp();
	/**
	 * Sets the last saving operation timestamp
	 * @param newTS the new timestamp
	 */
	public void setLastUpdateTimeStamp(Date newTS);
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets the user code of the creator (it comes from the user context)
	 * @param userCode
	 */
	public void setCreatorUserCode(final UserCode userCode);
	/**
	 * Gets the user code of the creator 
	 * @return
	 */
	public UserCode getCreatorUserCode();
	/**
	 * Sets the user code of the last updator (it comes from the user context)
	 * @param userCode
	 */
	public void setLastUpdatorUserCode(final UserCode userCode);
	/**
	 * Gets the user code of the last updator
	 * @return
	 */
	public UserCode getLastUpdatorUserCode();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sets common data from the {@link ModelObjectTracking} object
	 * @param trackingInfo
	 */
	public void setTrackingInfo(final ModelObjectTracking trackingInfo);
	/**
	 * @return the {@link ModelObjectTracking} info
	 */
	public ModelObjectTracking getTrackingInfo();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Builds a {@link ModelObject} from this {@link DBEntity} data
	 * @param userContext
	 * @return a model object
	 */
	public <T> T toModelObject(final UserContext userContext);
	
	/**
	 * Fills up the {@link DBEntity} object's data from the {@link ModelObject}
	 * @param userContext
	 * @param modelObj the model object
	 */
	public <T> void fromModelObject(final UserContext userContext,
									final T modelObj);
}
