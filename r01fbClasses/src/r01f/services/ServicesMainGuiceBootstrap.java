package r01f.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Module;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.internal.R01FBootstrapGuiceModule;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.internal.ServiceToImplAndProxyDef;
import r01f.services.client.internal.ServicesClientAPIBootstrapGuiceModuleBase;
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
import r01f.util.types.collections.Lists;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForApp;

/**
 * Bootstraps a service-oriented guice-based application
 * Usage:
 * <pre class='brush:java'>
 *		Collection<Module> bootstrapModuleInstances = ServicesMainGuiceBootstrap.createForApi(R01MAppCode.API.code())
 *																				.loadBootstrapModuleInstances();
 *		Injector injector = Guice.createInjector(bootstrapModuleInstances);
 * </pre>
 * A service-oriented application has two types of guice modules:
 * <ul>
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
		@Getter private final AppAndComponent _apiAppAndModule;
		@Getter private final Map<AppAndComponent,ServicesImpl> _coreAppAndModulesDefProxies;
		
		public Collection<AppAndComponent> getCoreAppAndModules() {
			return _coreAppAndModulesDefProxies != null ? _coreAppAndModulesDefProxies.keySet() : null;
		}
	}
	// Client definitions
	private Collection<ServiceClientDef> _clientDefs;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	private ServicesMainGuiceBootstrap(final Collection<AppAndComponent> apiAppAndModules) {
		_clientDefs = Lists.newArrayList();
		
		for (final AppAndComponent apiAppAndModule : apiAppAndModules) {
			final XMLPropertiesForApp _apiProps = XMLProperties.createForApp(apiAppAndModule.getAppCode())
									 						   .notUsingCache();	
			// Find all core appCode / modules from {apiAppCode}.client.properties.xml
			Collection<AppAndComponent> coreAppAndModules = _apiProps.forComponent(apiAppAndModule.getAppComponent())		// usually this is simply client
												  			    	 .propertyAt("/client/proxies")
												  			    	 .asObjectList(new Function<Node,AppAndComponent>() {
																							@Override
																							public AppAndComponent apply(final Node node) {
																								AppCode coreAppCode = AppCode.forId(XMLUtils.nodeAttributeValue(node,"appCode"));
																								AppComponent module = AppComponent.forId(XMLUtils.nodeAttributeValue(node,"id"));
																								return AppAndComponent.composedBy(coreAppCode,module); 
																							}
													 					          });
			// Find all core appCode / modules default proxy from {apiAppCode}.client.properties.xml
			Map<AppAndComponent,ServicesImpl> coreAppAndModulesDefProxy = null;
			coreAppAndModulesDefProxy = Maps.toMap(coreAppAndModules,
												   new Function<AppAndComponent,ServicesImpl>() {
														@Override
														public ServicesImpl apply(final AppAndComponent coreAppAndModule) {
															String propsXPath = Strings.of("/client/proxies/proxy[@appCode='{}' and @id='{}']/@impl")
																					   .customizeWith(coreAppAndModule.getAppCode(),
																							   		  coreAppAndModule.getAppComponent())
																					   .asString();
															ServicesImpl configuredImpl = _apiProps.forComponent("client")
																								   .propertyAt(propsXPath)
																									 	.asEnumElement(ServicesImpl.class);
															if (configuredImpl == null) { 
																log.warn("NO proxy impl for appCode/module={} configured at {}.client.properties.xml, {} is used by default",
																		 coreAppAndModule,apiAppAndModule,ServicesImpl.REST);
																configuredImpl = ServicesImpl.REST;
															}
															return configuredImpl;
														}
											});
			// Create the service definition
			ServiceClientDef def = new ServiceClientDef(apiAppAndModule,
														coreAppAndModulesDefProxy);
			_clientDefs.add(def);
		} // for
	}
	public static ServicesMainGuiceBootstrap createForApi(final AppCode apiAppCode) {
		AppAndComponent appAndComponent = AppAndComponent.composedBy(apiAppCode,AppComponent.forId("client"));
		return new ServicesMainGuiceBootstrap(Lists.newArrayList(appAndComponent));
	}
	public static ServicesMainGuiceBootstrap createForApi(final AppCode... apiAppCodes) {
		return new ServicesMainGuiceBootstrap(FluentIterable.from(Arrays.asList(apiAppCodes))
														    .transform(new Function<AppCode,AppAndComponent>() {
																				@Override
																		 		public AppAndComponent apply(final AppCode apiAppCode) {
																					return AppAndComponent.composedBy(apiAppCode,AppComponent.forId("client"));
																				}
																	   })
														    .toList());
	}
	public static ServicesMainGuiceBootstrap createForApi(final AppAndComponent apiAppAndModule) {
		return new ServicesMainGuiceBootstrap(Lists.newArrayList(apiAppAndModule));
	}
	public static ServicesMainGuiceBootstrap createForApi(final AppAndComponent... apiAppAndModules) {
		return new ServicesMainGuiceBootstrap(Lists.newArrayList(apiAppAndModules));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Install all guice modules for every api appCode
	 * @param binder
	 */
	public void installBootstrapModuleInstances(final Binder binder) {
		// Load bootstrap module instances
		Collection<Module> modulesToInstall = this.loadBootstrapModuleInstances();
		
		// Install the modules
		if (CollectionUtils.hasData(modulesToInstall)) {
			for (Module mod : modulesToInstall) {
				binder.install(mod);
			}
		}
	}
	/**
  	 * Load bootstrap module instances
	 *	- If there's more than a single api appCode a private module for every api appCode is returned so 
	 *	  there's NO conflict between each api appCode
	 *	- If there's a single api appCode there's no need to isolate every api appcode in it's own private module
	 * @return
	 */
	public Collection<Module> loadBootstrapModuleInstances() {
		List<Module> outModules = Lists.newArrayList();
		
		// find the modules
		for (ServiceClientDef clientDef : _clientDefs) {
			Module currApiModule = _bootstrapGuiceModuleFor(clientDef);
			if (currApiModule != null) outModules.add(currApiModule);
		}
		
		// Add the mandatory R01F guice modules
		outModules.add(0,new R01FBootstrapGuiceModule());
		
		return outModules;
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
		log.warn("\n\n\n\n");
		log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		log.warn("[Find CLIENT & CORE MODULES for {} ({} core modules)]",serviceClientDef.getApiAppAndModule(),
																		 serviceClientDef.getCoreAppAndModules() != null ? serviceClientDef.getCoreAppAndModules().size() : 0);
		log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		// [1] - Find the CLIENT API BOOTSTRAP guice module types
    	log.warn("[START]-Find CLIENT binding modules============================================");
    	ServicesClientBootstrapModulesFinder clientBootstrapModulesFinder = new ServicesClientBootstrapModulesFinder(serviceClientDef.getApiAppAndModule());
    	
    	// The client bootstrap modules
		final Collection<Class<? extends ServicesClientAPIBootstrapGuiceModuleBase>> clientAPIBootstrapModulesTypes = clientBootstrapModulesFinder.findProxyBingingsGuiceModuleTypes();
		
		ServicesClientBootstrapModulesFinder.logFoundModules(clientAPIBootstrapModulesTypes);
		log.warn("  [END]-Find CLIENT binding modules============================================");
		
		
		// [2] - Find the CORE (server) bootstrap guice module types for the cores defined at r01m.client.properties.xml file
		//		 for each app/component combination there might be multiple Bean/REST/EJB/Servlet, etc core bootstrap modules
    	log.warn("[START]-Find CORE binding modules==============================================");
    	ServicesCoreBootstrapModulesFinder coreBootstrapModulesFinder = new ServicesCoreBootstrapModulesFinder(serviceClientDef.getApiAppAndModule(),
    																										   serviceClientDef.getCoreAppAndModules());
		final Map<AppAndComponent,
				  Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModulesTypesByAppAndModule = coreBootstrapModulesFinder.findBootstrapGuiceModuleTypes();
		log.warn("  [END]-Find CORE binding modules==============================================");

		
		// [3] - Find the client-api service interface to proxy and / or impls matchings 
		//		 At {apiAppCode}.client.properties.xml all proxy/impl for every coreAppCode / module is defined:
		//					<proxies>
		//						<proxy appCode="coreAppCode1" id="moduleA1" impl="REST">Module definition</proxy>
		//						<proxy appCode="coreAppCode1" id="moduleB1" impl="REST">Module definition</proxy>
		//						<proxy appCode="coreAppCode2" id="moduleA2" impl="Bean">Module definition</proxy>
		//					</proxies>
		//		 now every client-api defined service interface is matched to a proxy implementation
		log.warn("[START]-Find ServiceInterface to bean impl and proxy matchings ================");
		ServicesClientInterfaceToImplAndProxyFinder serviceIfaceToImplAndProxiesFinder = new ServicesClientInterfaceToImplAndProxyFinder(serviceClientDef.getApiAppAndModule(),
																											   				        	 serviceClientDef.getCoreAppAndModulesDefProxies());
		Collection<AppCode> coreAppCodes = _coreServicesAppCodes(serviceClientDef.getCoreAppAndModules());
		final Map<AppAndComponent,
				  Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceIfacesToImplAndProxiesByAppModule = serviceIfaceToImplAndProxiesFinder.findServiceInterfacesToImplAndProxiesBindings(coreAppCodes);
		log.warn("  [END]-Find ServiceInterface to bean impl and proxy matchings ================");

		
		// [3] - Create a module for the API appCode that gets installed with:
		//			- A module with the client API bindins
		//			- A private module with the core bindings for each core app module
		log.warn("\n\n\n\n");
		log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		log.warn("[Bind CLIENT & CORE MODULES for {} ({} core modules)]",serviceClientDef.getApiAppAndModule(),
																		 serviceClientDef.getCoreAppAndModules() != null ? serviceClientDef.getCoreAppAndModules().size() : 0);
		log.warn(":::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::");
		return new Module() {
					@Override
					public void configure(final Binder binder) {
						List<Module> bootstrapModuleInstances = Lists.newArrayList();
						
						// 3.1 - Add the CLIENT bootstrap guice modules
						for (Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientAPIBootstrapModule : clientAPIBootstrapModulesTypes) {
							Module clientModule = ServiceBootstrapGuiceModuleUtils.createGuiceModuleInstance(clientAPIBootstrapModule);
							((ServicesClientAPIBootstrapGuiceModuleBase)clientModule).setCoreAppAndModules(serviceClientDef.getCoreAppAndModules());
							
							bootstrapModuleInstances.add(0,clientModule);	// insert first!
						}		
						
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
									log.warn("=============================");
									log.warn("[Bind CLIENT Modules]");
									log.warn("=============================");
									clientBindingLogged = true;
								} else if (!coreBindingLogged && module instanceof ServicesCoreForAppModulePrivateGuiceModule) {
									log.warn("=============================");
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
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Get all the core apps
	 * @param appAndModules
	 * @return
	 */
	private static Collection<AppCode> _coreServicesAppCodes(final Collection<AppAndComponent> appAndModules) {
		Collection<AppCode> outCoreApps = Lists.newArrayList();
		for (AppAndComponent appAndMod : appAndModules) {
			if (!outCoreApps.contains(appAndMod.getAppCode())) outCoreApps.add(appAndMod.getAppCode());
		}
		return outCoreApps;
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
																		  		 	 final Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModulesTypesByAppAndModule,
																		  		 	 final Map<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceInterfacesToImplAndProxyByAppModule) {
		Map<AppAndComponent,ServiceBootstrapDef> outSrvcBootstrapDefs = Maps.newHashMap();
		
		// [1]: Configure the definitions only with the default proxy
		for(final AppAndComponent coreAppAndComponent : serviceClientDef.getCoreAppAndModules()) {
			log.warn("/----------------------------------------------------------------------------------------------------------------------------\\");
			ServiceBootstrapDef modDef = new ServiceBootstrapDef(serviceClientDef.getApiAppAndModule(),
															     coreAppAndComponent,
																 serviceClientDef.getCoreAppAndModulesDefProxies().get(coreAppAndComponent));
			log.warn("API MODULE {} to CORE MODULE {} default proxy={}",
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
							 "check that the types implementing {} has the @{} annotation ant the appCode and module attributes match the coreAppCode & module " +
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
