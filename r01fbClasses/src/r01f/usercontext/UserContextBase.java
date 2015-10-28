package r01f.usercontext;

import java.util.Date;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AuthenticatedActorID;
import r01f.guids.CommonOIDs.TenantID;
import r01f.guids.CommonOIDs.UserCode;

/**
 * R01M API {@link UserContext} implementation
 */
@Accessors(prefix="_")
@NoArgsConstructor
public abstract class UserContextBase 
           implements UserContext {

	private static final long serialVersionUID = 6313898883935383799L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="authActorId")
	@Getter @Setter protected AuthenticatedActorID _authenticatedActorId;
	/**
	 * Tenant id (tenatA, tenantB, ...)
	 * Allows the database and filesystem data to be partitioned by this value
	 */
	@XmlAttribute(name="tenantId")
	@Getter @Setter protected TenantID _tenantId; 	// TODO the tenant id should come with the authentication data (ie xlnets)
	/**
	 * The date this user context was created
	 */
	@XmlAttribute(name="createDate")	
	@Getter @Setter protected Date _createDate;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor from the {@link AppCode}.
	 * Usually this method uses some kind of security system to build the context
	 * @param appCode 
	 */
	public UserContextBase(final AppCode appCode) {
		this(AuthenticatedActorID.forApp(appCode));
	}
	/**
	 * Constructor from the {@link UserCode}.
	 * @param userCode
	 */
	public UserContextBase(final UserCode userCode) {
		this(AuthenticatedActorID.forUser(userCode));
	}
	/**
	 * Constructor from the {@link AuthenticatedActorID}.
	 * @param authActor
	 */
	public UserContextBase(final AuthenticatedActorID authActor) {
		this(authActor,
			 TenantID.DEFAULT);
	}
	/**
	 * Constructor from the {@link AppCode} and tenantId
	 * Usually this method uses some kind of security system to build the context
	 * @param appCode 
	 * @param tenantId
	 */
	public UserContextBase(final AppCode appCode,
						   final TenantID tenantId) {
		this(AuthenticatedActorID.forApp(appCode),
			 tenantId);
	}
	/**
	 * Constructor from the {@link UserCode} and tenantId
	 * @param userCode
	 * @param tenantId
	 */
	public UserContextBase(final UserCode userCode,
						   final TenantID tenantId) {
		this(AuthenticatedActorID.forUser(userCode),
			 tenantId);
	}
	/**
	 * Constructor from the {@link AuthenticatedActorID} and tenantId
	 * @param authActor
	 * @param tenantId
	 */
	public UserContextBase(final AuthenticatedActorID authActor,
						   final TenantID tenantId) {
		_authenticatedActorId = authActor;
		_tenantId = tenantId;
		_createDate = new Date();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public UserCode getUserCode() {
		UserCode outUserCode = null;
		if (_authenticatedActorId != null) {
			if (this.isForApp()) throw new IllegalArgumentException("The user context is NOT for a user (it's an app)");
			outUserCode = UserCode.forAuthenticatedUserId(_authenticatedActorId);
		} 
		return outUserCode;
	}
	@Override
	public AppCode getAppCode() {
		AppCode outUserCode = null;
		if (_authenticatedActorId != null) {
			if (this.isForUser()) throw new IllegalArgumentException("The user context is NOT for an app (it's a user context)");
			outUserCode = AppCode.forAuthenticatedUserId(_authenticatedActorId);
		} 
		return outUserCode;
	}
	@Override
	public boolean isForApp() {
		return this.getAuthenticatedActorId() != null ? this.getAuthenticatedActorId().isApp()
													  : false;
	}
	@Override
	public boolean isForUser() {
		return this.getAuthenticatedActorId() != null ? this.getAuthenticatedActorId().isUser()
													  : false;		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <CTX extends UserContext> CTX cast() {
		return (CTX)this;
	}
}
