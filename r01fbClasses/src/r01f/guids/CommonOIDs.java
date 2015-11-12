package r01f.guids;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;

public class CommonOIDs {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * OID of a void oid
	 */
	@Inmutable
	@XmlRootElement(name="voidOid")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class VoidOID 
	            extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = 5898825736200388235L;
		
		public VoidOID(final String oid) {
			super(oid);
		}
		public static VoidOID forId(final String id) {
			return new VoidOID(id);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * AppCode
	 */
	@XmlRootElement(name="appCode")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class AppCode 
	            extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = -1130290632493385784L;
	
		public AppCode(final String oid) {
			super(oid);
		}
		public static AppCode forId(final String id) {
			return new AppCode(id);
		}
		public static AppCode forIdOrNull(final String id) {
			if (id == null) return null;
			return new AppCode(id);
		}
		public static AppCode forAuthenticatedUserId(final AuthenticatedActorID authActorId) {
			return new AppCode(authActorId.asString());
		}
	}
	/**
	 * AppCode component
	 */
	@XmlRootElement(name="appComponent")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class AppComponent 
	            extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = 137722031497569807L;
		
		public static final AppComponent DEFAULT = AppComponent.forId("default");
		
		public AppComponent(final String oid) {
			super(oid);
		}
		public static AppComponent forId(final String id) {
			return new AppComponent(id);
		}
		public static AppComponent forIdOrNull(final String id) {
			if (id == null) return null;
			return new AppComponent(id);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * UserCode
	 */
	@Inmutable
	@XmlRootElement(name="userCode")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class UserCode 
	     		extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = -8145305261344081383L;
	
		public UserCode(final String oid) {
			super(oid);
		}
		public static UserCode forId(final String id) {
			return new UserCode(id);
		}
		public static UserCode forAuthenticatedUserId(final AuthenticatedActorID authActorId) {
			return new UserCode(authActorId.asString());
		}
	}
	@Inmutable
	@XmlRootElement(name="password")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class Password 
	     		extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = -4110070527400569196L;
	
		public Password(final String oid) {
			super(oid);
		}
		public static Password forId(final String id) {
			return new Password(id);
		}
	}
	@XmlRootElement(name="userAndPassword")
	@Accessors(prefix="_")
	@NoArgsConstructor @AllArgsConstructor
	public static class UserAndPassword 
			 implements Serializable {
		private static final long serialVersionUID = 1549566021138557737L;

		@XmlAttribute(name="user")
		@Getter @Setter private UserCode _user;
		
		@XmlAttribute(name="password")
		@Getter @Setter private Password _password;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Inmutable
	@XmlRootElement(name="authenticatedActor")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class AuthenticatedActorID 
	     		extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = -7186228864961079493L;
		
		private boolean _app;	// sets if the auth actor is a physical user or an app
		
		public AuthenticatedActorID(final String id) {
			super(id);
		}
		public AuthenticatedActorID(final String id,
									final boolean isUser) {
			super(id);
			_app = !isUser;
		}
		public static AuthenticatedActorID forId(final String id,final boolean isUser) {
			return new AuthenticatedActorID(id,isUser);
		}
		public static AuthenticatedActorID forUser(final UserCode userCode) {
			return new AuthenticatedActorID(userCode.asString(),
											true);		// phisical user
		}
		public static AuthenticatedActorID forApp(final AppCode appCode) {
			return new AuthenticatedActorID(appCode.asString(),
											false);		// app
		}
		public boolean isApp() {
			return _app;
		}
		public boolean isUser() {
			return !this.isApp();
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tenant identifier
	 */
	@Inmutable
	@XmlRootElement(name="tenantId")
	@NoArgsConstructor
	public static class TenantID
		 		extends OIDBaseMutable<String> {
	
		private static final long serialVersionUID = -7631726260644902005L;
		
		public static final TenantID DEFAULT = TenantID.forId("default");
	
		public TenantID(final String id) {
			super(id);
		}
		public static TenantID valueOf(final String s) {
			return TenantID.forId(s);
		}
		public static TenantID fromString(final String s) {
			return TenantID.forId(s);
		}
		public static TenantID forId(final String id) {
			return new TenantID(id);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Inmutable
	@XmlRootElement(name="env")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class Environment 
	     		extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = -2820663417050382971L;
		
		public static Environment NO_ENV = Environment.forId("noEnv");
		public Environment(final String oid) {
			super(oid);
		}
		public static Environment forId(final String id) {
			return new Environment(id);
		}
	}
	@Inmutable
	@XmlRootElement(name="execContextOid")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class ExecContextId 
				extends OIDBaseMutable<String> {
		
		private static final long serialVersionUID = 6876006770063375473L;	
		
		public ExecContextId(final String oid) {
			super(oid);
		}
		public static ExecContextId forId(final String id) {
			return new ExecContextId(id);
		}
	}
}
