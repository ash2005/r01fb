package r01f.services;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.inject.Module;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.internal.R01FBootstrapGuiceModule;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.internal.ServiceToImplAndProxyDef;
import r01f.services.client.internal.ServicesClientAPIBootstrapGuiceModuleBase;
import r01f.services.client.internal.ServicesClientBindingsGuiceModule;
import r01f.services.client.internal.ServicesClientBootstrapModulesFinder;
import r01f.services.client.internal.ServicesClientInterfaceToImplAndProxyFinder;
import r01f.services.core.BeanImplementedServicesCoreBootstrapGuiceModuleBase;
import r01f.services.core.EJBImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.RESTImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.ServletImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.core.internal.ServicesCoreBootstrapModulesFinder;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForApp;

/**
 * Bootstraps a service-oriented guice-based application
 * (see R01MInjector type)
 * <ul>
 * 		<li>Core services implementation (a core service is ONLY bootstrapped if it's guice module is available in the classpath)</li>
 * 		<li>Client services proxy for the core services implementation</li>
 * </ul>
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
	 * (it's loaded from {apiAppCode}.core.properties.xml
	 */
	private final Map<AppCode,Map<AppAndComponent,ServicesImpl>> _coreAppAndModulesDefProxy;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	private ServicesMainGuiceBootstrap(final AppCode... apiAppCodes) {
		_apiAppCodes = Lists.newArrayList(apiAppCodes);
		_coreAppAndModulesDefProxy = Maps.newHashMapWithExpectedSize(_apiAppCodes.size());
		_coreAppAndModules = Maps.newHashMapWithExpectedSize(_apiAppCodes.size());
		
		for (final AppCode apiAppCode : _apiAppCodes) {
			final XMLPropertiesForApp _apiProps = XMLProperties.createForApp(apiAppCode)
									 						   .notUsingCache();	
			// Find all core appCode / modules from {apiAppCode}.core.properties.xml
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
			// Fin all core appCode / modules default proxy to use from {apiAppCode}.client.properties.xml
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
		}
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
	 * Entry point for guice module loading
	 * @return
	 */
	public Collection<Module> loadBootstrapModuleInstances() {
		List<Module> outModules = Lists.newArrayList();
		
		// find the modules
		for (AppCode apiAppCode : _apiAppCodes) {
			Collection<Module> currApiModules = _loadBootstrapModuleInstances(apiAppCode);
			if (CollectionUtils.hasData(currApiModules)) outModules.addAll(currApiModules);
		}
		
		// Add the mandatory R01F guice modules
		outModules.add(0,new R01FBootstrapGuiceModule());
		
		return outModules;
	}
	
	/**
	 * Entry point for guice module loading
	 * @param apiAppCode
	 * @return
	 */
	private Collection<Module> _loadBootstrapModuleInstances(final AppCode apiAppCode) {
		// [1] - Find the CLIENT API BOOTSTRAP guice module types
    	log.warn("[START]-Find CLIENT binding modules====================================================================================");
    	ServicesClientBootstrapModulesFinder clientBootstrapModulesFinder = new ServicesClientBootstrapModulesFinder(apiAppCode);
    	
    	// The client bootstrap modules
		Collection<Class<? extends ServicesClientAPIBootstrapGuiceModuleBase>> clientAPIBootstrapModulesTypes = clientBootstrapModulesFinder.findProxyBingingsGuiceModuleTypes();
		// other client bindings
		Collection<Class<? extends ServicesClientBindingsGuiceModule>> clientBindingsModulesTypes = clientBootstrapModulesFinder.findOtherBindingsGuiceModuleTypes();
		
		ServicesClientBootstrapModulesFinder.logFoundModules(clientAPIBootstrapModulesTypes);
		ServicesClientBootstrapModulesFinder.logFoundModules(clientBindingsModulesTypes);
		log.warn("  [END]-Find CLIENT binding modules====================================================================================");
		
		
		// [2] - Find the CORE (server) bootstrap guice module types for the cores defined at r01m.core.properties.xml file
    	log.warn("[START]-Find CORE binding modules======================================================================================");
    	ServicesCoreBootstrapModulesFinder coreBootstrapModulesFinder = new ServicesCoreBootstrapModulesFinder(apiAppCode,
    																										   _coreAppAndModules.get(apiAppCode));
		Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModulesTypesByAppAndModule = coreBootstrapModulesFinder.findBootstrapGuiceModuleTypes();
		log.warn("  [END]-Find CORE binding modules======================================================================================");

		
		// [3] - Find the service interface to proxy and / or impls matchings 
		log.warn("[START]-Find ServiceInterface to bean impl and proxy matchings ========================================================");
		ServicesClientInterfaceToImplAndProxyFinder serviceIfaceToImplAndProxiesFinder = new ServicesClientInterfaceToImplAndProxyFinder(apiAppCode,
																											   				        	 _coreAppAndModulesDefProxy.get(apiAppCode));
		Collection<AppCode> coreAppCodes = _coreServicesAppCodes(_coreAppAndModulesDefProxy.get(apiAppCode).keySet());
		Map<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceIfacesToImplAndProxiesByAppModule = serviceIfaceToImplAndProxiesFinder.findServiceInterfacesToImplAndProxiesBindings(coreAppCodes);
		log.warn("  [END]-Find ServiceInterface to bean impl and proxy matchings =========================================================");

		
		// [3] - Create a collection with the guice modules
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
			Module coreAppAndModulePrivateGuiceModule = new ServicesForAppModulePrivateGuiceModule(bootstrapCoreModDef);
			bootstrapModuleInstances.add(coreAppAndModulePrivateGuiceModule);
			
			// ... BUT the REST or Servlet core bootstrap modules (the ones extending RESTImplementedServicesCoreGuiceModuleBase) MUST be binded here 
			// in order to let the world see (specially the Guice Servlet filter) see the REST resources bindings
			Collection<? extends ServicesCoreBootstrapGuiceModule> restCoreAppAndModuleGuiceModules = bootstrapCoreModDef.getPublicBootstrapGuiceModuleInstances();
			bootstrapModuleInstances.addAll(restCoreAppAndModuleGuiceModules);
		}
		
		
		// 3.3 - Add the CLIENT BINDINGS guice modules
		if (CollectionUtils.hasData(clientBindingsModulesTypes)) {
			for (Class<? extends ServicesClientBindingsGuiceModule> clientBindingsModule : clientBindingsModulesTypes) {
				Module clientModule = ServiceBootstrapGuiceModuleUtils.createGuiceModuleInstance(clientBindingsModule);		
				bootstrapModuleInstances.add(clientModule);	// no matter if it's first or last
			}
		}
		
		// [4] - Return the modules
		return bootstrapModuleInstances;
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
				// set the service interface to impl and proxy binding definition
				modDef.setServiceInterfacesToImplAndProxiesDefs(serviceInterfacesToImplAndProxyByAppModule.get(coreAppAndModule));
				
				
				// divide the core bootstrap guice modules by type
				for (Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapGuiceModule : coreBootstrapGuiceModules) {
					if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,BeanImplementedServicesCoreBootstrapGuiceModuleBase.class)) {
						modDef.addCoreBeanBootstrapModuleType((Class<? extends BeanImplementedServicesCoreBootstrapGuiceModuleBase>)coreBootstrapGuiceModule);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,RESTImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreRESTBootstrapModuleType((Class<? extends RESTImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModule);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,EJBImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreEJBBootstrapModuleType((Class<? extends EJBImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModule);
					} else if (ReflectionUtils.isImplementing(coreBootstrapGuiceModule,ServletImplementedServicesCoreGuiceModuleBase.class)) {
						modDef.addCoreServletBootstrapModuleType((Class<? extends ServletImplementedServicesCoreGuiceModuleBase>)coreBootstrapGuiceModule);
					} else {
						throw new IllegalArgumentException("Unsupported bootstrap guice module type: " + coreBootstrapGuiceModule);
					}
				}
				outGuiceModuleDefByAppAndModule.add(modDef);
			}
		}
		return outGuiceModuleDefByAppAndModule;
	}

}
