package r01f.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Module;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.internal.R01FBootstrapGuiceModule;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.internal.ServiceToImplAndProxyDef;
import r01f.services.client.internal.ServicesClientAPIBootstrapGuiceModuleBase;
import r01f.services.client.internal.ServicesClientBootstrapModulesFinder;
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
	/**
	 * The api app codes
	 */
	private final Collection<AppCode> _apiAppCodes;
	/**
	 * The core app and modules by api appCode
	 */
	private final Map<AppCode,Collection<AppAndComponent>> _coreAppAndModules;
	/**
	 * The core app codes and modules and the default service proxy impl 
	 * (it's loaded from {apiAppCode}.client.properties.xml
	 */
	private final Map<AppCode,Map<AppAndComponent,ServicesImpl>> _coreAppAndModulesDefProxy;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesMainGuiceBootstrap(final AppCode... apiAppCodes) {
		_apiAppCodes = Lists.newArrayList(apiAppCodes);
		_coreAppAndModulesDefProxy = Maps.newHashMapWithExpectedSize(_apiAppCodes.size());
		_coreAppAndModules = Maps.newHashMapWithExpectedSize(_apiAppCodes.size());
		
		for (final AppCode apiAppCode : _apiAppCodes) {
			final XMLPropertiesForApp _apiProps = XMLProperties.createForApp(apiAppCode)
									 						   .notUsingCache();	
			// Find all core appCode / modules from {apiAppCode}.client.properties.xml
			Collection<AppAndComponent> coreAppAndModules = _apiProps.forComponent("client")
												  			    	 .propertyAt("/client/proxies")
												  			    	 .asObjectList(new Function<Node,AppAndComponent>() {
																							@Override
																							public AppAndComponent apply(final Node node) {
																								AppCode coreAppCode = AppCode.forId(XMLUtils.nodeAttributeValue(node,"appCode"));
																								AppComponent module = AppComponent.forId(XMLUtils.nodeAttributeValue(node,"id"));
																								return AppAndComponent.composedBy(coreAppCode,module); 
																							}
													 					          });
			// Find all core appCode / modules default proxy to use from {apiAppCode}.client.properties.xml
			Map<AppAndComponent,ServicesImpl> coreAppAndModulesDefProxy = null;
			coreAppAndModulesDefProxy = Maps.toMap(coreAppAndModules,
												   new Function<AppAndComponent,ServicesImpl>() {
														@Override
														public ServicesImpl apply(AppAndComponent coreAppAndModule) {
															String propsXPath = Strings.of("/client/proxies/proxy[@appCode='{}' and @id='{}']/@impl")
																					   .customizeWith(coreAppAndModule.getAppCode(),
																							   		  coreAppAndModule.getAppComponent())
																					   .asString();
															ServicesImpl configuredImpl = _apiProps.forComponent("client")
																								   .propertyAt(propsXPath)
																									 	.asEnumElement(ServicesImpl.class);
															if (configuredImpl == null) { 
																log.warn("NO proxy impl for appCode/module={} configured at {}.client.properties.xml, {} is used by default",
																		 coreAppAndModule,apiAppCode,ServicesImpl.REST);
																configuredImpl = ServicesImpl.REST;
															}
															return configuredImpl;
														}
											});
			_coreAppAndModulesDefProxy.put(apiAppCode,coreAppAndModulesDefProxy);
			
			_coreAppAndModules.put(apiAppCode,coreAppAndModulesDefProxy.keySet());
		} // for
	}
	public static ServicesMainGuiceBootstrap createForApi(final AppCode apiAppCode) {
		return new ServicesMainGuiceBootstrap(apiAppCode);
	}
	public static ServicesMainGuiceBootstrap createForApi(final AppCode... apiAppCodes) {
		return new ServicesMainGuiceBootstrap(apiAppCodes);
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
		for (AppCode apiAppCode : _apiAppCodes) {
			Module currApiModule = _bootstrapGuiceModuleFor(apiAppCode);
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
	private Module _bootstrapGuiceModuleFor(final AppCode apiAppCode) {
		// [1] - Find the CLIENT API BOOTSTRAP guice module types
    	log.warn("[START]-Find CLIENT binding modules====================================================================================");
    	ServicesClientBootstrapModulesFinder clientBootstrapModulesFinder = new ServicesClientBootstrapModulesFinder(apiAppCode);
    	
    	// The client bootstrap modules
		final Collection<Class<? extends ServicesClientAPIBootstrapGuiceModuleBase>> clientAPIBootstrapModulesTypes = clientBootstrapModulesFinder.findProxyBingingsGuiceModuleTypes();
		
		ServicesClientBootstrapModulesFinder.logFoundModules(clientAPIBootstrapModulesTypes);
		log.warn("  [END]-Find CLIENT binding modules====================================================================================");
		
		
		// [2] - Find the CORE (server) bootstrap guice module types for the cores defined at r01m.client.properties.xml file
		//		 for each app/component combination there might be multiple Bean/REST/EJB/Servlet, etc core bootstrap modules
    	log.warn("[START]-Find CORE binding modules======================================================================================");
    	ServicesCoreBootstrapModulesFinder coreBootstrapModulesFinder = new ServicesCoreBootstrapModulesFinder(apiAppCode,
    																										   _coreAppAndModules.get(apiAppCode));
		final Map<AppAndComponent,
				  Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModulesTypesByAppAndModule = coreBootstrapModulesFinder.findBootstrapGuiceModuleTypes();
		log.warn("  [END]-Find CORE binding modules======================================================================================");

		
		// [3] - Find the client-api service interface to proxy and / or impls matchings 
		//		 At {apiAppCode}.client.properties.xml all proxy/impl for every coreAppCode / module is defined:
		//					<proxies>
		//						<proxy appCode="coreAppCode1" id="moduleA1" impl="REST">Module definition</proxy>
		//						<proxy appCode="coreAppCode1" id="moduleB1" impl="REST">Module definition</proxy>
		//						<proxy appCode="coreAppCode2" id="moduleA2" impl="Bean">Module definition</proxy>
		//					</proxies>
		//		 now every client-api defined service interface is matched to a proxy implementation
		log.warn("[START]-Find ServiceInterface to bean impl and proxy matchings ========================================================");
		ServicesClientInterfaceToImplAndProxyFinder serviceIfaceToImplAndProxiesFinder = new ServicesClientInterfaceToImplAndProxyFinder(apiAppCode,
																											   				        	 _coreAppAndModulesDefProxy.get(apiAppCode));
		Collection<AppCode> coreAppCodes = _coreServicesAppCodes(_coreAppAndModulesDefProxy.get(apiAppCode).keySet());
		final Map<AppAndComponent,
				  Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceIfacesToImplAndProxiesByAppModule = serviceIfaceToImplAndProxiesFinder.findServiceInterfacesToImplAndProxiesBindings(coreAppCodes);
		log.warn("  [END]-Find ServiceInterface to bean impl and proxy matchings =========================================================");

		
		// [3] - Create a module for the API appCode that gets installed with:
		//			- A module with the client API bindins
		//			- A private module with the core bindings for each core app module
		return new Module() {
					@Override
					public void configure(final Binder binder) {
						List<Module> bootstrapModuleInstances = Lists.newArrayList();
						
						// 3.1 - Add the CLIENT bootstrap guice modules
						for (Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> clientAPIBootstrapModule : clientAPIBootstrapModulesTypes) {
							Module clientModule = ServiceBootstrapGuiceModuleUtils.createGuiceModuleInstance(clientAPIBootstrapModule);
							((ServicesClientAPIBootstrapGuiceModuleBase)clientModule).setCoreAppAndModules(_coreAppAndModules.get(apiAppCode));
							
							bootstrapModuleInstances.add(0,clientModule);	// insert first!
						}		
						
						// 3.2 - Add a private module for each appCode / module stack: service interface --> proxy --> impl (rest / bean / etc)
						//		 this way, each appCode / module is independent (isolated)
						Collection<ServiceBootstrapDef> coreBootstrapGuiceModuleDefs = _bootstrapGuiceModuleDefsFrom(apiAppCode,
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
							for (Module module : bootstrapModuleInstances) {
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
	@SuppressWarnings("unchecked")
	private Collection<ServiceBootstrapDef> _bootstrapGuiceModuleDefsFrom(final AppCode apiAppCode,
																		  final Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModulesTypesByAppAndModule,
																		  final Map<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceInterfacesToImplAndProxyByAppModule) {
		Collection<ServiceBootstrapDef> outGuiceModuleDefByAppAndModule = Lists.newArrayList();
		
		// There's no core bootstrap modules at the classpath (pure client)
		if (CollectionUtils.isNullOrEmpty(coreBootstrapModulesTypesByAppAndModule)) {
			for(Map.Entry<AppAndComponent,ServicesImpl> me : _coreAppAndModulesDefProxy.get(apiAppCode).entrySet()) {
				ServiceBootstrapDef modDef = new ServiceBootstrapDef(apiAppCode,
																     me.getKey(),
																	 me.getValue());
				// set the service interface to impl and proxy binding definition
				modDef.setServiceInterfacesToImplAndProxiesDefs(serviceInterfacesToImplAndProxyByAppModule.get(me.getKey()));
				outGuiceModuleDefByAppAndModule.add(modDef);
			}
		}
		// There exists core bootstrap modules at the classpath
		else {
			for (Map.Entry<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> me : coreBootstrapModulesTypesByAppAndModule.entrySet()) {
				AppAndComponent coreAppAndModule = me.getKey();
				Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> coreBootstrapGuiceModules = me.getValue();
				
				// Create the definition
				ServiceBootstrapDef modDef = new ServiceBootstrapDef(apiAppCode,
																     coreAppAndModule,
																	 _coreAppAndModulesDefProxy.get(apiAppCode).get(coreAppAndModule));				
				
				// divide the core bootstrap guice modules by type
				for (Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapGuiceModule : coreBootstrapGuiceModules) {
					if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,
													   BeanImplementedServicesCoreBootstrapGuiceModuleBase.class)) {
						modDef.addCoreBeanBootstrapModuleType((Class<? extends BeanImplementedServicesCoreBootstrapGuiceModuleBase>)coreBootstrapGuiceModule);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,
															  RESTImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreRESTBootstrapModuleType((Class<? extends RESTImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModule);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,
															  EJBImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreEJBBootstrapModuleType((Class<? extends EJBImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModule);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,
															  ServletImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreServletBootstrapModuleType((Class<? extends ServletImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModule);
					} else {
						throw new IllegalArgumentException("Unsupported bootstrap guice module type: " + coreBootstrapGuiceModule);
					}
				} 
				
				// set the service interface to impl and proxy binding definition
				if (serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndModule) == null) {
					log.warn("BEWARE!!!!! The core module {} is NOT accesible via a client-API service interface: " +
							 "there's NO client API service interface to impl and/or proxy binding for {}; " +
							 "check that the types implementing {} has the @{} annotation ant the appCode and module attributes match the coreAppCode & module " +
							 "(they MUST match the ones in {}.client.properties.xml). " + 
							 "This is usually an ERROR except on coreAppCode/modules that do NOT expose anything at client-api (ie the Servlet modules)",
							   coreAppAndModule,
							   coreAppAndModule,
							   ServiceInterface.class.getName(),ServiceInterfaceFor.class.getSimpleName(),
							   coreAppAndModule.getAppCode());
				} else {					
					modDef.setServiceInterfacesToImplAndProxiesDefs(serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndModule));
				}
				
				outGuiceModuleDefByAppAndModule.add(modDef);
			}
		}
		return outGuiceModuleDefByAppAndModule;
	}
}
