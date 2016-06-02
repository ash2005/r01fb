package r01f.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.patterns.IsBuilder;
import r01f.services.ServiceIDs.ClientApiAppAndModule;
import r01f.services.ServiceIDs.ClientApiAppCode;
import r01f.services.ServiceIDs.ClientApiModule;
import r01f.services.ServiceIDs.CoreAppAndModule;
import r01f.services.ServiceIDs.CoreAppCode;
import r01f.services.ServiceIDs.CoreModule;
import r01f.services.client.ClientAPI;
import r01f.services.client.internal.ServicesClientAPIBootstrapGuiceModuleBase;
import r01f.util.types.collections.CollectionUtils;

@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class ServicesInitDataBuilder
		   implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static Collection<ServicesInitData> multiple(final ServicesInitData... initData) {
		if (CollectionUtils.isNullOrEmpty(initData)) throw new IllegalArgumentException();
		return Arrays.asList(initData);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static ServicesInitDataBuilderApiBootstrapTypeStep createForClientApi(final ClientApiAppAndModule clientAppAndModule) {
		return new ServicesInitDataBuilder() { /* nothing */ }
					.new ServicesInitDataBuilderApiBootstrapTypeStep(clientAppAndModule);
	}
	public static ServicesInitDataBuilderApiBootstrapTypeStep createForClientApi(final ClientApiAppCode clientAppCode,final ClientApiModule clientApiModule) {
		return new ServicesInitDataBuilder() { /* nothing */ }
					.new ServicesInitDataBuilderApiBootstrapTypeStep(ClientApiAppAndModule.of(clientAppCode,
																					 		  clientApiModule));
	}
	public static ServicesInitDataBuilderApiBootstrapTypeStep createForClientApi(final ClientApiAppCode clientAppCode) {
		return ServicesInitDataBuilder.createForClientApi(clientAppCode,ClientApiModule.DEFAULT);
	}
	public static ServicesInitDataBuilderServiceInterfacesPackageStep createForBootstrapModule(final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientApiBootstrapType) {
		ClientApiAppAndModule clientApiAppAndModule = ServicesPackages.appAndModuleFromClientBootstrapModule(clientApiBootstrapType);
		return ServicesInitDataBuilder.createForClientApi(clientApiAppAndModule)
									  .bootstrapedBy(clientApiBootstrapType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesInitDataBuilderApiBootstrapTypeStep {
		private final ClientApiAppAndModule _clientAppAndModule;
		
		public ServicesInitDataBuilderServiceInterfacesPackageStep bootstrapedBy(final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientApiBootstrapType) {
			return new ServicesInitDataBuilderServiceInterfacesPackageStep(_clientAppAndModule,
																		   clientApiBootstrapType);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesInitDataBuilderServiceInterfacesPackageStep {
		private final ClientApiAppAndModule _clientAppAndModule;
		private final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> _clientApiBootstrapType;
		
		public ServicesInitDataBuilderCoreModulesStep findServiceInterfacesAtPackage(final String packageToLookForServiceInterfaces) {
			return new ServicesInitDataBuilderCoreModulesStep(_clientAppAndModule,
															  _clientApiBootstrapType,
															  packageToLookForServiceInterfaces);
		}
		public ServicesInitDataBuilderCoreModulesStep findServiceInterfacesAtDefaultPackage() {
			return this.findServiceInterfacesAtPackage(ServicesPackages.serviceInterfacePackage(_clientAppAndModule));
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesInitDataBuilderCoreModulesStep {
		private final ClientApiAppAndModule _clientAppAndModule;
		private final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> _clientApiBootstrapType;
		private final String _packageToLookForServiceInterfaces;
		
		public ServicesInitDataBuilderClientApiStep proxiedTo(final CoreServiceImpl... coreServiceImpl) {
			if (CollectionUtils.isNullOrEmpty(coreServiceImpl)) throw new IllegalArgumentException("Core services impls cannot be null!!");
			Map<CoreAppAndModule,ServicesImpl> coreAppAndModulesDefProxies = Maps.newHashMapWithExpectedSize(coreServiceImpl.length);
			for (CoreServiceImpl impl : coreServiceImpl) {
				coreAppAndModulesDefProxies.put(impl.getCoreAppAndModule(),
												impl.getDefaultImpl());
			}
			return new ServicesInitDataBuilderClientApiStep(_clientAppAndModule,
															_clientApiBootstrapType,
															_packageToLookForServiceInterfaces,
															coreAppAndModulesDefProxies);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesInitDataBuilderClientApiStep {
		private final ClientApiAppAndModule _clientAppAndModule;
		private final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> _clientApiBootstrapType;
		private final String _packageToLookForServiceInterfaces;
		private final Map<CoreAppAndModule,ServicesImpl> _coreAppAndModulesDefProxies;
		
		public ServicesInitDataBuilderBuildStep exposedByApiType(final Class<? extends ClientAPI> apiType) {
			return new ServicesInitDataBuilderBuildStep(_clientAppAndModule,
														_clientApiBootstrapType,
														_packageToLookForServiceInterfaces,
														_coreAppAndModulesDefProxies,
													    apiType);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ServicesInitDataBuilderBuildStep {
		private final ClientApiAppAndModule _clientAppAndModule;
		private final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> _clientApiBootstrapType;
		private final String _packageToLookForServiceInterfaces;
		private final Map<CoreAppAndModule,ServicesImpl> _coreAppAndModulesDefProxies;
		private final Class<? extends ClientAPI> _apiType;
		
		public ServicesInitData build() {
			return new ServicesInitData(_clientAppAndModule,
										_clientApiBootstrapType,
										_packageToLookForServiceInterfaces,
										_coreAppAndModulesDefProxies,
									    _apiType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)	
	public static class CoreServiceImpl {
		@Getter private final CoreAppAndModule _coreAppAndModule;
		@Getter private final ServicesImpl _defaultImpl;
		
		public static CoreServiceBuilderImplStep of(final CoreAppAndModule coreAppAndModule) {
			return new CoreServiceBuilderImplStep(coreAppAndModule);
		}
		public static CoreServiceBuilderImplStep of(final CoreAppCode coreAppCode,final CoreModule coreModule) {
			return new CoreServiceBuilderImplStep(CoreAppAndModule.of(coreAppCode,coreModule));
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class CoreServiceBuilderImplStep {
		private final CoreAppAndModule _coreAppAndModule;
		public CoreServiceImpl usingDefaultProxy(final ServicesImpl impl) {
			return new CoreServiceImpl(_coreAppAndModule,
									   impl);
		}
	}
}
