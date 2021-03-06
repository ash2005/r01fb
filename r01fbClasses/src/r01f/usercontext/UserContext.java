package r01f.usercontext;

import java.io.Serializable;
import java.util.Date;

import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.UserCode;

/**
 * Marker interface for every user context data
 */
public interface UserContext 
		 extends Serializable {
	
	/**
	 * @return this object casted to a {@link UserContext} impl
	 */
	public <CTX extends UserContext> CTX cast();
	/**
	 * If this user context is for a physical user, it returns his/her user code
	 * if it's an app user context it throws an {@link IllegalStateException}
	 * @return
	 */
	public UserCode getUserCode();
	/**
	 * If this user context is for an app, it returns the appCode
	 * if it's an user context it throws an {@link IllegalStateException}
	 * @return
	 */
	public AppCode getAppCode();
	/**
	 * @return true if this is an app user context
	 */
	public boolean isForApp();
	/**
	 * @return true if this is an user context
	 */
	public boolean isForUser();
	/**
	 * @return the Date when this user context was created
	 */
	public Date getCreateDate();
}
