package r01f.services;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.guids.OIDBaseMutable;
import r01f.util.types.Strings;

public class ServiceIDs {
/////////////////////////////////////////////////////////////////////////////////////////
//  BASE
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private static class AppCodeAndModuleBase<A,M> {
		@Getter private final A _appCode;
		@Getter private final M _module;
		
		public String asString() {
			return this.toString();
		}
		@Override
		public String toString() {
			return Strings.customized("{}.{}",_appCode,_module);
		}
		@Override
		public int hashCode() {
			return this.toString().hashCode();
		}
		@Override
		public boolean equals(final Object obj) {
			if (obj == null) return false;
			if (obj == this) return true;
			if (obj instanceof AppCodeAndModuleBase) {
				AppCodeAndModuleBase<?,?> otherAppCodeAndModule = (AppCodeAndModuleBase<?,?>)obj;
				return otherAppCodeAndModule.toString().equals(this.toString());		
			} else if (obj instanceof String) {
				return obj.toString().equals(this.toString());
			} 
			return super.equals(obj);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CLIENT
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * AppCode
	 */
	@XmlRootElement(name="clientApiAppCode")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class ClientApiAppCode 
	            extends OIDBaseMutable<String> {
		private static final long serialVersionUID = 7093516452073951301L;
		public ClientApiAppCode(final String oid) {
			super(oid);
		}
		public static ClientApiAppCode forId(final String id) {
			return new ClientApiAppCode(id);
		}
		public static ClientApiAppCode valueOf(final String id) {
			return ClientApiAppCode.forId(id);
		}
		public AppCode asAppCode() {
			return AppCode.forId(this.getId());
		}
	}
	/**
	 * AppCode component
	 */
	@XmlRootElement(name="clientApiModule")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class ClientApiModule 
	            extends OIDBaseMutable<String> {
		private static final long serialVersionUID = 4671654474443487500L;
		public static final ClientApiModule DEFAULT = ClientApiModule.forId("client");
		
		public ClientApiModule(final String oid) {
			super(oid);
		}
		public static ClientApiModule forId(final String id) {
			return new ClientApiModule(id);
		}
		public static ClientApiModule valueOf(final String id) {
			return ClientApiModule.forId(id);
		}
		public AppComponent asAppComponent() {
			return AppComponent.forId(this.getId());
		}
	}
	@Accessors(prefix="_")
	public static class ClientApiAppAndModule 
				extends AppCodeAndModuleBase<ClientApiAppCode,ClientApiModule> {
		public ClientApiAppAndModule(final ClientApiAppCode api,final ClientApiModule module) {
			super(api,module);
		}
		public static ClientApiAppAndModule of(final ClientApiAppCode appCode,final ClientApiModule module) {
			return new ClientApiAppAndModule(appCode,module);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CORE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * AppCode
	 */
	@XmlRootElement(name="coreAppCode")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class CoreAppCode 
	            extends OIDBaseMutable<String> {
		private static final long serialVersionUID = 7498970290219115981L;
		public CoreAppCode(final String oid) {
			super(oid);
		}
		public static CoreAppCode forId(final String id) {
			return new CoreAppCode(id);
		}
		public static CoreAppCode valueOf(final String id) {
			return CoreAppCode.forId(id);
		}
		public AppCode asAppCode() {
			return AppCode.forId(this.getId());
		}
	}
	/**
	 * AppCode component
	 */
	@XmlRootElement(name="coreModule")
	@EqualsAndHashCode(callSuper=true)
	@NoArgsConstructor
	public static class CoreModule 
	            extends OIDBaseMutable<String> {
		private static final long serialVersionUID = -8935025566081906908L;
		public CoreModule(final String oid) {
			super(oid);
		}
		public static CoreModule forId(final String id) {
			return new CoreModule(id);
		}
		public static CoreModule valueOf(final String id) {
			return CoreModule.forId(id);
		}
		public AppComponent asAppComponent() {
			return AppComponent.forId(this.getId());
		}
	}
	@Accessors(prefix="_")
	public static class CoreAppAndModule 
				extends AppCodeAndModuleBase<CoreAppCode,CoreModule> {
		public CoreAppAndModule(final CoreAppCode api,final CoreModule module) {
			super(api,module);
		}
		public static CoreAppAndModule of(final CoreAppCode appCode,final CoreModule module) {
			return new CoreAppAndModule(appCode,module);
		}
		public static CoreAppAndModule of(final String appCodeAndModule) {
			String[] parts = Strings.of(appCodeAndModule)
									.splitter(".")
									.toArray();
			if (parts.length == 2) {
				return CoreAppAndModule.of(CoreAppCode.forId(parts[0]),CoreModule.forId(parts[1]));
			} else if (parts.length > 2) {
				StringBuilder comp = new StringBuilder();
				for (int i=1; i < parts.length; i++) {
					comp.append(parts[i]);
					if (i < parts.length-1) comp.append(".");
				}
				return CoreAppAndModule.of(CoreAppCode.forId(parts[0]),CoreModule.forId(comp.toString()));
			} else {
				throw new IllegalStateException();
			}
		}
	}
}
