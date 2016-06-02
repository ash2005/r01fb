package r01f.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.internal.R01FBootstrapGuiceModule;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServiceIDs.ClientApiAppAndModule;
import r01f.services.ServiceIDs.ClientApiAppCode;
import r01f.services.ServiceIDs.ClientApiModule;
import r01f.services.ServiceIDs.CoreAppAndModule;
import r01f.services.ServiceIDs.CoreAppCode;
import r01f.services.ServiceIDs.CoreModule;
import r01f.services.client.ClientAPI;
import r01f.services.client.ServiceProxiesAggregator;
import r01f.services.client.internal.ServiceInterfaceTypesToImplOrProxyMappings;
import r01f.services.client.internal.ServiceToImplAndProxyDef;
import r01f.services.client.internal.ServicesClientAPIBootstrapGuiceModuleBase;
import r01f.services.client.internal.ServicesClientAPIFinder;
import r01f.services.client.internal.ServicesClientBootstrapModulesFinder;
import r01f.services.client.internal.ServicesClientGuiceModule;
import r01f.services.client.internal.ServicesClientInterfaceToImplAndProxyFinder;
import r01f.services.core.internal.BeanImplementedServicesCoreBootstrapGuiceModuleBase;
import r01f.services.core.internal.EJBImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.RESTImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.core.internal.ServicesCoreBootstrapModulesFinder;
import r01f.services.core.internal.ServicesCoreForAppModulePrivateGuiceModule;
import r01f.services.core.internal.ServletImplementedServicesCoreGuiceModuleBase;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceInterfaceFor;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Bootstraps a service-oriented guice-based application
 * Basic usage:
 * <pre class='brush:java'>
 *		Collection<Module> bootstrapModules = ServicesMainGuiceBootstrap.createFor(ServicesInitDataBuilder.createForClientApi(apiAppCode,apiModule)
 *						 								  												  .bootstrapedBy(MyBootstrapGuiceModule.class)
 *						 								  												  .findServiceInterfacesAtDefaultPackage()	// finds services interfaces at apiAppCode.api.apiModule.interfaces
 *						 								  												  .forServiceImplementations(CoreServiceImpl.of(coreAppCode,coreModule)
 *						 										  																				    .defaultImpl(ServicesImpl.REST))
 *						 								  												  .exposedByApiType(AA81CommonClientAPI.class)
 *						 								  												  .withNamedServiceHandler(AA81AppCodes.COMMON_APP_AND_MOD_STR)
 *						 								  												  .build())
 *																		 .loadBootstrapModuleInstances();
 *		Injector injector = Guice.createInjector(bootstrapModuleInstances);
 * </pre>
 * 
 * A service-oriented application has the following basic components:
 * <ul>
 * 		<li>The services</li>
 * 		<li>The modules that bootstrap the client</li>
 * 		<li>The modules that bootstrap the core (server)<li>
 * </ul>
 * 
 * 
 * If xx is the client appCode and yy is the core appCode (they can be the same appCode)
 * 
 * Client bootstraping
 * ===================
 * at the java package xx.client.internal create two types:
 * 1.- A type that will get injected with the {@link ServiceInterface} to bean impl or proxy
 * 	   	- if the {@link ServiceInterface} bean impl is available the system will bind the interface to this impl
 * 		- if not, the {@link ServiceInterface} will be binded to a proxy set at xx.client.properties.xml, 
 * 		  for example a proxy to the REST {@link ServiceInterface} impl
 * 	    <pre class='brush:java'>
 *			public class XXServiceInterfaceTypesToImplOrProxyMappings 
 *			  implements ServiceInterfaceTypesToImplOrProxyMappings {
 *			  
 *				@Inject @Named("yy.mymodule") @SuppressWarnings({ "rawtypes" })
 *				private Map<Class,ServiceInterface> _grantedBenefitsServiceInterfaceTypesToImplOrProxy;
 *			}			
 * 		</pre> 
 * 	   Is important that:
 * 			- There MUST exist a Map<Class,ServiceInterface> field for every core appCode / module combination
 * 			  set at xx.client.properties.xml
 * 			- Each Map<Class,ServiceInterface> field MUST be annotated with @Named("yy.my_module") where yy.my_module is 
 * 			  the proxy's appCode/id at xx.client.properties.xml 
 * 			  <pre class='brush:xml'>
 *				<proxies>
 *					<proxy appCode="yy" id="my_module" impl="REST">My Module</proxy>
 *					<proxy appCode="yy" id="my_otherModule" impl="REST">My other module</proxy>
 *				</proxies>
 * 			  </pre>
 * 
 * 2.- A bootstrap guice module extending {@link ServicesClientAPIBootstrapGuiceModuleBase}
 * 		<pre class='brush:java'>
 *			public class XXClientBootstrapGuiceModule 
 *			  	 extends ServicesClientAPIBootstrapGuiceModuleBase {	// this is a client guice bindings module
 *				
 *				public XXClientBootstrapGuiceModule() {
 *					super(XXAppCode.API.code(),
 *						  new XXServiceInterfaceTypesToImplOrProxyMappings());
 *				}
 *				@Override
 *				protected void _configure(final Binder binder) {
 *					_bindModelObjectsMarshaller(binder);
 *					_bindModelObjectExtensionsModule(binder);
 *				}
 *				@Override @SuppressWarnings("unchecked")
 *				protected <U extends UserContext> U _provideUserContext() {
 *					XXMockUserContextProvider provider = new XXMockUserContextProvider();
 *					return (U)provider.get();
 *				}
 *			}
 * 		</pre> 
 * 
 *	<clientPackages>
 *		<!-- The package where the service interfaces are located -->
 *		<lookForServiceInterfacesAt>aa81f.api.common.interfaces</lookForServiceInterfacesAt>
 *		<!-- In order to bootstrap the client, the system will look for types extending ServicesClientAPIBootstrapGuiceModuleBase at the provided package -->
 *		<lookForClientBootstrapTypesAt>aa81f.client.common.internal</lookForClientBootstrapTypesAt>
 *		<!-- The package where the client api is located -->
 *		<lookForClientApiAggregatorTypeAt>aa81f.client.common.api</lookForClientApiAggregatorTypeAt>
 *	</clientPackages>
 *	
 *	<!--
 *	Client Proxies to be used to access the services
 *	(see ServicesMainGuiceBootstrap.java for details about how services client APIs are 
 *	 bound to the services core implementation through a proxy)
 *	Values:
 *		- REST	 : use HTTP to access the REST end-point that exposes the services layer functions
 *		- Servlet: a pseudo-REST http access end-point
 *		- Bean	 : use the services layer functions directly accessing the beans that encapsulates them
 *		- EJB 	 : use RMI to access the EJB end-point that exposes the services layer functions
 *		- Mock 	 : use a mock-ed services layer functions
 *	-->
 *	<proxies>
 *		<proxy appCode="aa81b" id="common" impl="REST">Granted Benefits core</proxy>
 *	</proxies>
 * 
 * 
 * SERVER/CORE BOOTSTRAPING
 * ========================
 * At the java package yy.internal there MUST exist a type extending {@link BeanImplementedServicesCoreBootstrapGuiceModuleBase}
 * that bootstraps the core side.
 * This guice module type contains the core's bindings, usually installing other guice modules as db persistence, searching, notifier, etc
 * For convenience, if the core is in charge of the DB persistence, the type might extend {@link BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase}
 * This type MUST be annotated with @ServicesCore with the core's module id
 * <pre class='brush:java'> 
 *		@ServicesCore(moduleId="my_module",dependsOn=ServicesImpl.NULL) 	// see xx.client.properties.xml
 *		@EqualsAndHashCode(callSuper=true)									// This is important for guice modules
 *		public class YYServicesBootstrapGuiceModule
 *		     extends BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase {
 *		  
 *			public YYServicesBootstrapGuiceModule() {
 *				super(XXAppCode.API.code(),
 *					  new YYDBGuiceModule(YYServicesBootstrapGuiceModule.class),			// DB
 *					  new YYSearchGuiceModule(YYServicesBootstrapGuiceModule.class),		// search
 *					  Lists.<Module>newArrayList(new YYNotifierGuiceModule(YYServicesBootstrapGuiceModule.class)));	
 *			}
 *		}
 * </pre>
 */
@Slf4j
public class ServicesMainGuiceBootstrap {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private class ServiceClientDef {
		@Getter private final ClientApiAppAndModule _apiAppAndModule;
		@Getter private final Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> _clientApiBootstrapModuleType;
		@Getter private final ServiceInterfaceTypesToImplOrProxyMappings _serviceInterfacesToImplOrProxyMappings;
		@Getter private final String _packageToLookForServiceInterfaces;
		@Getter private final Class<? extends ServiceProxiesAggregator> _servicesProxiesAggregatorType;
		@Getter private final Map<CoreAppAndModule,ServicesImpl> _coreAppAndModulesDefProxies;
		@Getter private final Class<? extends ClientAPI> _clientApiType;
		
		public Collection<CoreAppAndModule> getCoreAppAndModules() {
			return _coreAppAndModulesDefProxies != null ? _coreAppAndModulesDefProxies.keySet() : null;
		}
		public Collection<CoreAppCode> getCoreAppCodes() {
			Collection<CoreAppCode> outCoreApps = Lists.newArrayList();
			for (CoreAppAndModule appAndMod : this.getCoreAppAndModules()) {
				if (!outCoreApps.contains(appAndMod.getAppCode())) outCoreApps.add(appAndMod.getAppCode());
			}
			return outCoreApps;
		}
	}
	// Client definitions
	private Collection<ServiceClientDef> _clientDefs;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	private ServicesMainGuiceBootstrap(final ServiceClientDef... defs) {
		_clientDefs = Arrays.asList(defs);
	}
	private ServicesMainGuiceBootstrap(final ServicesInitData... initData) {
		// Transform the init data to client defs
		Collection<ServiceClientDef> clientDefs = Lists.newArrayListWithExpectedSize(initData.length);		
		
		for (ServicesInitData currInitData : initData) {
			// Find the type that holds a Map that binds the service interface to the proxy or implementation
			// (note that this type MUST be at the same package as the client api)
	    	ServicesClientBootstrapModulesFinder clientBootstrapModulesFinder = new ServicesClientBootstrapModulesFinder(currInitData.getClientApiBootstrapType().getPackage().getName());
			ServiceInterfaceTypesToImplOrProxyMappings serviceInterfaceTypesToImplOrProxyMappings = clientBootstrapModulesFinder.findServiceInterfaceTypesToImplOrProxyMappingsFor(currInitData.getCoreAppAndModules());
			
			// Find the type that aggregates all service interface proxies
			ServicesClientAPIFinder clientAPIFinder = new ServicesClientAPIFinder();
			Class<? extends ServiceProxiesAggregator> proxyAggregatorType = clientAPIFinder.findClientAPIProxyAggregatorType(currInitData.getApiType());
			ServiceClientDef clientDef = new ServiceClientDef(currInitData.getClientAppAndModule(),
															  currInitData.getClientApiBootstrapType(),
															  serviceInterfaceTypesToImplOrProxyMappings,
															  currInitData.getPackageToLookForServiceInterfaces(),
															  proxyAggregatorType,
															  currInitData.getCoreAppAndModulesDefProxies(),
															  currInitData.getApiType());
			clientDefs.add(clientDef);
		}
		_clientDefs = clientDefs;
	}
	private ServicesMainGuiceBootstrap(final ClientApiAppAndModule... apiAppAndModules) {
		// The service client defs are loaded from a properties file
		Collection<ServiceClientDef> clientDefs = Lists.newArrayListWithExpectedSize(apiAppAndModules.length);
		
		for (final ClientApiAppAndModule apiAppAndModule : apiAppAndModules) {
			final XMLPropertiesForApp apiProps = XMLProperties.createForApp(apiAppAndModule.getAppCode().asAppCode())
									 						  .notUsingCache();
			final XMLPropertiesForAppComponent apiModuleProperties = apiProps.forComponent(apiAppAndModule.getModule().asAppComponent());
			
			// [0] - Load client bootstrapping info from the properties files
			// The package where the certain mandatory client types are located
			String pckgToLookForClientBootstrapType = apiModuleProperties.propertyAt("/client/clientPackages/lookForClientBootstrapTypesAt")
															  			 .asString(ServicesPackages.clientBootstrapGuiceModulePackage(apiAppAndModule));
			String pckToLookForServiceInterfaces = apiModuleProperties.propertyAt("/client/clientPackages/lookForServiceInterfacesAt")
														   			  .asString(ServicesPackages.serviceInterfacePackage(apiAppAndModule));
			String pckgToLookForClientApiAggregator = apiModuleProperties.propertyAt("/client/clientPackages/lookForClientApiAggregatorTypeAt")
														   	  			 .asString(ServicesPackages.serviceProxyPackage(apiAppAndModule));
			
			
			// Find all core appCode / modules from {apiAppCode}.{apiComponent}.properties.xml
			Collection<CoreAppAndModule> coreAppAndModules = apiProps.forComponent(apiAppAndModule.getModule().asAppComponent())		// usually this is simply client
												  			    	  .propertyAt("/client/proxies")
												  			    	  .asObjectList(new Function<Node,CoreAppAndModule>() {
																							@Override
																							public CoreAppAndModule apply(final Node node) {
																								CoreAppCode coreAppCode = CoreAppCode.forId(XMLUtils.nodeAttributeValue(node,"appCode"));
																								CoreModule module = CoreModule.forId(XMLUtils.nodeAttributeValue(node,"id"));
																								return CoreAppAndModule.of(coreAppCode,module); 
																							}
													 					            });
			// Find all core appCode / modules default proxy from {apiAppCode}.client.properties.xml
			Map<CoreAppAndModule,ServicesImpl> coreAppAndModulesDefProxy = null;
			coreAppAndModulesDefProxy = Maps.toMap(coreAppAndModules,
												   new Function<CoreAppAndModule,ServicesImpl>() {
														@Override
														public ServicesImpl apply(final CoreAppAndModule coreAppAndModule) {
															String propsXPath = Strings.of("/client/proxies/proxy[@appCode='{}' and @id='{}']/@impl")
																					   .customizeWith(coreAppAndModule.getAppCode(),
																							   		  coreAppAndModule.getModule())
																					   .asString();
															ServicesImpl configuredImpl = apiModuleProperties.propertyAt(propsXPath)
																									 		 .asEnumElement(ServicesImpl.class);
															if (configuredImpl == null) { 
																log.warn("NO proxy impl for appCode/module={} configured at {}.client.properties.xml, {} is used by default",
																		 coreAppAndModule,apiAppAndModule,ServicesImpl.REST);
																configuredImpl = ServicesImpl.REST;
															}
															return configuredImpl;
														}
											});
			// a bit of log
			log.warn("[Client API: {}.{}]",apiAppAndModule.getAppCode(),apiAppAndModule.getModule());
			log.warn("\tLocations:");
			log.warn("\t\t-   Search for bootstrap guice modules at: {}",pckgToLookForClientBootstrapType);
			log.warn("\t\t-        Search for service interfaces at: {}",pckToLookForServiceInterfaces);
			log.warn("\t\t-Search for client api aggregator type at: {}",pckgToLookForClientApiAggregator);
			log.warn("\tCore modules:");
			if (CollectionUtils.hasData(coreAppAndModulesDefProxy)) {
				for (Map.Entry<CoreAppAndModule,ServicesImpl> me : coreAppAndModulesDefProxy.entrySet()) {
					log.warn("\t\t-{} > {}",me.getKey(),me.getValue());
				}
			}
			
			// [1] - Find the CLIENT API BOOTSTRAP guice module types
	    	log.warn("[START]-Find CLIENT bootstrap modules at {}===========================================",pckgToLookForClientBootstrapType);
	    	ServicesClientBootstrapModulesFinder clientBootstrapModulesFinder = new ServicesClientBootstrapModulesFinder(pckgToLookForClientBootstrapType);
	    	// Find the client bootstrap module type
			Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientAPIBootstrapModulesType = clientBootstrapModulesFinder.findClientBootstrapGuiceModuleTypes();
			
			// Find the type that holds a Map that binds the service interface to the proxy or implementation
			// (note that this type MUST be at the same package as the client api)
			ServiceInterfaceTypesToImplOrProxyMappings serviceInterfaceTypesToImplOrProxyMappings = clientBootstrapModulesFinder.findServiceInterfaceTypesToImplOrProxyMappingsFor(coreAppAndModules);
			log.warn("  [END]-Find CLIENT binding modules============================================");
			
			log.warn("[START]-Find CLIENT API at {}========================================================");
			ServicesClientAPIFinder clientAPIFinder = new ServicesClientAPIFinder();
			// Find the client api
			Class<? extends ClientAPI> clientAPIType = clientAPIFinder.findClientAPI(pckgToLookForClientApiAggregator);
			
			// Find the type that aggregates the fine-grained service proxy types
			// (note that this type is a generic param of the client api type)
			Class<? extends ServiceProxiesAggregator> proxyAggregatorType = clientAPIFinder.findClientAPIProxyAggregatorType(clientAPIType);
			
			// Create the service definition
			ServiceClientDef def = new ServiceClientDef(apiAppAndModule,
														clientAPIBootstrapModulesType,
														serviceInterfaceTypesToImplOrProxyMappings,
														pckToLookForServiceInterfaces,
														proxyAggregatorType,
														coreAppAndModulesDefProxy,
														clientAPIType);
			clientDefs.add(def);
		} // for
		_clientDefs = clientDefs;
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static ServicesMainGuiceBootstrap createForApi(final ClientApiAppCode apiAppCode) {
		ClientApiAppAndModule apiAppAndModule = ClientApiAppAndModule.of(apiAppCode,ClientApiModule.DEFAULT);
		return new ServicesMainGuiceBootstrap(apiAppAndModule);
	}
	public static ServicesMainGuiceBootstrap createForApi(final ClientApiAppCode... apiAppCodes) {
		return new ServicesMainGuiceBootstrap(FluentIterable.from(Arrays.asList(apiAppCodes))
														    .transform(new Function<ClientApiAppCode,ClientApiAppAndModule>() {
																				@Override
																		 		public ClientApiAppAndModule apply(final ClientApiAppCode apiAppCode) {
																					return ClientApiAppAndModule.of(apiAppCode,ClientApiModule.DEFAULT);
																				}
																	   })
														    .toArray(ClientApiAppAndModule.class));
	}
	public static ServicesMainGuiceBootstrap createForApi(final ClientApiAppAndModule apiAppAndModule) {
		return new ServicesMainGuiceBootstrap(apiAppAndModule);
	}
	public static ServicesMainGuiceBootstrap createForApi(final ClientApiAppAndModule... apiAppAndModules) {
		return new ServicesMainGuiceBootstrap(apiAppAndModules);
	}
	public static ServicesMainGuiceBootstrap createForApi(final Collection<ClientApiAppAndModule> apiAppAndModules) {
		return new ServicesMainGuiceBootstrap(apiAppAndModules.toArray(new ClientApiAppAndModule[apiAppAndModules.size()]));
	}
	public static ServicesMainGuiceBootstrap createFor(final ServicesInitData... initData) {
		if (CollectionUtils.isNullOrEmpty(initData)) throw new IllegalArgumentException();
		return new ServicesMainGuiceBootstrap(initData);
	}	
	public static ServicesMainGuiceBootstrap createFor(final Collection<ServicesInitData> defs) {
		if (CollectionUtils.isNullOrEmpty(defs)) throw new IllegalArgumentException();
		return new ServicesMainGuiceBootstrap(defs.toArray(new ServicesInitData[defs.size()]));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
  	 * Load bootstrap module instances
	 *	- If there's more than a single api appCode a private module for every api appCode is returned so 
	 *	  there's NO conflict between each api appCode
	 *	- If there's a single api appCode there's no need to isolate every api appcode in it's own private module
	 * @return
	 */
	Collection<Module> loadBootstrapModuleInstances() {
		List<Module> bootstrapModules = Lists.newArrayList();
		
		// find the modules
		log.warn("\n\n\n\n");
		log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		log.warn("CONFIGURING {} CLIENT APIs",_clientDefs.size());
		log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		for (ServiceClientDef clientDef : _clientDefs) {
			Module currApiModule = _bootstrapGuiceModuleFor(clientDef);
			if (currApiModule != null) bootstrapModules.add(currApiModule);
		}
		log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		log.warn("\n\n\n\n");
		
		// Add the mandatory R01F guice modules
		bootstrapModules.add(0,new R01FBootstrapGuiceModule());
		
		return bootstrapModules;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a guice module that encapsulates all bindings for an api app code
	 * @param apiAppCode
	 * @return
	 */
	private static Module _bootstrapGuiceModuleFor(final ServiceClientDef serviceClientDef) {
		log.warn("\n\n");
		log.warn("[CONFIGURING CLIENT & CORE MODULES for {}.{} ==> {} ({} core modules)]",
				  serviceClientDef.getApiAppAndModule().getAppCode(),serviceClientDef.getApiAppAndModule().getModule(),
				  serviceClientDef.getCoreAppAndModules(),
				  serviceClientDef.getCoreAppAndModules() != null ? serviceClientDef.getCoreAppAndModules().size() : 0);
		log.warn("Client app/module={}.{} will be bootstrapped with {}",
				 serviceClientDef.getApiAppAndModule().getAppCode(),serviceClientDef.getApiAppAndModule().getModule(),
				 serviceClientDef.getClientApiBootstrapModuleType());
		
		log.warn("Client API: {}",serviceClientDef.getClientApiType());
		log.warn("Service proxies will be looked for at: {}",
				 serviceClientDef.getServicesProxiesAggregatorType().getPackage().getName());
		
		
		// [2] - Find the CORE (server) bootstrap guice module types for the cores defined at r01m.client.properties.xml file
		//		 for each app/component combination there might be multiple Bean/REST/EJB/Servlet, etc core bootstrap modules
    	log.warn("[START]-Find CORE binding modules==============================================");
    	ServicesCoreBootstrapModulesFinder coreBootstrapModulesFinder = new ServicesCoreBootstrapModulesFinder(serviceClientDef.getCoreAppAndModules());
		final Map<CoreAppAndModule,
				  Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModulesTypesByAppAndModule = coreBootstrapModulesFinder.findBootstrapGuiceModuleTypes();
		log.warn("  [END]-Find CORE binding modules==============================================");

		
		// [3] - Find the client-api service interface to proxy and / or impls matchings 
		//		 now every client-api defined service interface is matched to a proxy implementation
		log.warn("[START]-Find ServiceInterface to bean impl/proxy matchings ================");
		ServicesClientInterfaceToImplAndProxyFinder serviceIfaceToImplAndProxiesFinder = new ServicesClientInterfaceToImplAndProxyFinder(serviceClientDef.getPackageToLookForServiceInterfaces(),
																																		 serviceClientDef.getServicesProxiesAggregatorType().getPackage().getName(),
																											   				        	 serviceClientDef.getCoreAppAndModulesDefProxies());
		final Map<CoreAppAndModule,
				  Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceIfacesToImplAndProxiesByAppModule = serviceIfaceToImplAndProxiesFinder.findServiceInterfacesToImplAndProxiesBindings(serviceClientDef.getCoreAppCodes());
		log.warn("  [END]-Find ServiceInterface to bean impl/proxy matchings ================");

		
		// [4] - Create a module for the API appCode that gets installed with:
		//			- A module with the client API bindins
		//			- A private module with the core bindings for each core app module
		log.warn("[START] Creating CLIENT & CORE MODULES");
		Module outModule = new Module() {
					@Override
					public void configure(final Binder binder) {
						log.warn("\n\n\n\n");
						log.warn("///////////////////////////////////////////////////////////////////////");
						log.warn("[BOOTSTRAPPING CLIENT & CORE MODULES for {}.{} ==> {} ({} core modules)]",
								  serviceClientDef.getApiAppAndModule().getAppCode(),serviceClientDef.getApiAppAndModule().getModule(),
								  serviceClientDef.getCoreAppAndModules(),
								  serviceClientDef.getCoreAppAndModules() != null ? serviceClientDef.getCoreAppAndModules().size() : 0);
						log.warn("///////////////////////////////////////////////////////////////////////");
						// contains all the guice modules to be bootstraped: client & core
						List<Module> bootstrapModuleInstances = Lists.newArrayList();
						
						// 3.1 - Add the CLIENT bootstrap guice module
						ServicesClientAPIBootstrapGuiceModuleBase clientModule = (ServicesClientAPIBootstrapGuiceModuleBase)ServicesLifeCycleUtil.createGuiceModuleInstance(serviceClientDef.getClientApiBootstrapModuleType());
						clientModule.setContext(serviceClientDef.getApiAppAndModule(),
										  		serviceClientDef.getClientApiType(),
										  		serviceClientDef.getServicesProxiesAggregatorType(),
										  		serviceClientDef.getServiceInterfacesToImplOrProxyMappings(),
										  		serviceClientDef.getCoreAppAndModules());
						
						bootstrapModuleInstances.add(0,clientModule);	// insert first!
						
						// 3.2 - Add a private module for each appCode / module stack: service interface --> proxy --> impl (rest / bean / etc)
						//		 this way, each appCode / module is independent (isolated)
						Collection<ServiceBootstrapDef> coreBootstrapGuiceModuleDefs = _coreBootstrapGuiceModuleDefsFrom(serviceClientDef,
																													 	 coreBootstrapModulesTypesByAppAndModule,
																													 	 serviceIfacesToImplAndProxiesByAppModule);
						for (ServiceBootstrapDef bootstrapCoreModDef : coreBootstrapGuiceModuleDefs) {
							// Each core bootstrap modules (the ones implementing BeanImplementedServicesCoreGuiceModuleBase) for every core appCode / module
							// SHOULD reside in it's own private guice module in order to avoid bindings collisions
							// (ie JPA's guice persist modules MUST reside in separate private guice modules -see https://github.com/google/guice/wiki/GuicePersistMultiModules-)
							Module coreAppAndModulePrivateGuiceModule = new ServicesCoreForAppModulePrivateGuiceModule(bootstrapCoreModDef);
							bootstrapModuleInstances.add(coreAppAndModulePrivateGuiceModule);
							
							// ... BUT the REST or Servlet core bootstrap modules (the ones extending RESTImplementedServicesCoreGuiceModuleBase) MUST be binded here 
							// in order to let the world see (specially the Guice Servlet filter) see the REST resources bindings
							Collection<? extends ServicesCoreBootstrapGuiceModule> restCoreAppAndModuleGuiceModules = bootstrapCoreModDef.getPublicBootstrapGuiceModuleInstances();
							bootstrapModuleInstances.addAll(restCoreAppAndModuleGuiceModules);
						}
						
						// 3.3 - Install the modules
						Binder theBinder = binder;
						if (CollectionUtils.hasData(bootstrapModuleInstances)) {
							
							boolean clientBindingLogged = false;
							boolean coreBindingLogged = false;
							
							for (Module module : bootstrapModuleInstances) {
								// a bit of log
								if (!clientBindingLogged && module instanceof ServicesClientGuiceModule) {
									clientBindingLogged = true;
									log.warn("[Bind CLIENT Modules] {}");
									log.warn("=============================");
									clientBindingLogged = true;
								} else if (!coreBindingLogged && module instanceof ServicesCoreForAppModulePrivateGuiceModule) {
									log.warn("[Bind PRIVATE CORE Modules]");
									log.warn("=============================");
									coreBindingLogged = true;
								}								
								// DO the install
								theBinder.install(module);
							}
						}
					}
				};
		log.warn("[END] Creating CLIENT & CORE MODULES");
		return outModule;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a list of the definition of core a module to be bootstrapped (a collection of {@link ServiceBootstrapDef} objects that encapsulates all data
	 * needed to bootstrap a core module)
	 * It's important to note that if a core module bootstrap type is NOT found, the DEFAULT proxy (see client config) is used 
	 * @param serviceClientDef
	 * @param coreBootstrapModulesTypesByAppAndModule
	 * @param serviceInterfacesToImplAndProxyByAppModule
	 * @return
	 */
	@SuppressWarnings({ "unchecked","null" })
	private static Collection<ServiceBootstrapDef> _coreBootstrapGuiceModuleDefsFrom(final ServiceClientDef serviceClientDef,
																		  		 	 final Map<CoreAppAndModule,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModulesTypesByAppAndModule,
																		  		 	 final Map<CoreAppAndModule,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceInterfacesToImplAndProxyByAppModule) {
		Map<CoreAppAndModule,ServiceBootstrapDef> outSrvcBootstrapDefs = Maps.newHashMap();
		
		// [1]: Configure the definitions only with the default proxy
		for (final CoreAppAndModule coreAppAndComponent : serviceClientDef.getCoreAppAndModules()) {
			log.warn("/----------------------------------------------------------------------------------------------------------------------------\\");
			ServiceBootstrapDef modDef = new ServiceBootstrapDef(serviceClientDef.getApiAppAndModule(),
															     coreAppAndComponent,
																 serviceClientDef.getCoreAppAndModulesDefProxies().get(coreAppAndComponent));
			log.warn("API MODULE {} to CORE MODULE {} using proxy={}",
					 modDef.getApiAppAndModule(),modDef.getCoreAppCodeAndModule(),
					 modDef.getDefaultProxyImpl());
			
			// check if the core bootstrap module is present
			Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> coreBootstrapGuiceModulesTypes = CollectionUtils.hasData(coreBootstrapModulesTypesByAppAndModule) ? coreBootstrapModulesTypesByAppAndModule.get(coreAppAndComponent)
																																									        : null;
			
			// the core bootstrap module is present
			if (CollectionUtils.hasData(coreBootstrapGuiceModulesTypes)) {
				log.warn("\t\tcore bootstrap modules detected: ");
				// divide the core bootstrap guice modules by type
				for (Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapGuiceModuleType : coreBootstrapGuiceModulesTypes) {
					log.warn("\t\t\t- {}",coreBootstrapGuiceModuleType);
					if (ReflectionUtils.isImplementing(coreBootstrapGuiceModuleType,
													   BeanImplementedServicesCoreBootstrapGuiceModuleBase.class)) {
						modDef.addCoreBeanBootstrapModuleType((Class<? extends BeanImplementedServicesCoreBootstrapGuiceModuleBase>)coreBootstrapGuiceModuleType);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModuleType,
															  RESTImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreRESTBootstrapModuleType((Class<? extends RESTImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModuleType);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModuleType,
															  EJBImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreEJBBootstrapModuleType((Class<? extends EJBImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModuleType);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModuleType,
															  ServletImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreServletBootstrapModuleType((Class<? extends ServletImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModuleType);
					} else {
						throw new IllegalArgumentException("Unsupported bootstrap guice module type: " + coreBootstrapGuiceModuleType);
					}
				} 
				
				// set the service interface to impl and proxy binding definition
				if (serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndComponent) == null) {
					log.warn("BEWARE!!!!! The core module {} is NOT accesible via a client-API service interface: " +
							 "there's NO client API service interface to impl and/or proxy binding for {}; " +
							 "check that the types implementing {} has the @{} annotation and the appCode/module attributes match the coreAppCode/module " +
							 "(they MUST match the ones in {}.client.properties.xml). " + 
							 "This is usually an ERROR except on coreAppCode/modules that do NOT expose anything at client-api (ie the Servlet modules)",
							   coreAppAndComponent,	
							   coreAppAndComponent,
							   ServiceInterface.class.getName(),ServiceInterfaceFor.class.getSimpleName(),
							   coreAppAndComponent.getAppCode());
				} else {					
					modDef.setServiceInterfacesToImplAndProxiesDefs(serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndComponent));
					
					// a bit of logging
					if (serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndComponent) != null) {
						for (ServiceToImplAndProxyDef<? extends ServiceInterface> serviceToImplAndProxyDef : serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndComponent)) {
							log.warn("\t\tservice interface: {}",serviceToImplAndProxyDef.debugInfo());
						}
					}
				}
			}
			// the core bootstrap module is NOT present: set the service interface to impl and proxy binding definition
			else {
				log.warn("\t\tNO core bootstrap modules detected; using default proxy");
				modDef.setServiceInterfacesToImplAndProxiesDefs(serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndComponent));
				
				// a bit of logging
				if (serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndComponent) != null) {
					for (ServiceToImplAndProxyDef<? extends ServiceInterface> serviceToImplAndProxyDef : serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndComponent)) {
						log.warn("\t\tservice interface: {}",serviceToImplAndProxyDef.debugInfo());
					}
				}
			}
			
			// finally add the def to the map
			outSrvcBootstrapDefs.put(coreAppAndComponent,modDef);
			log.warn("\\----------------------------------------------------------------------------------------------------------------------------/");
		}
		return outSrvcBootstrapDefs.values();
	}
}
