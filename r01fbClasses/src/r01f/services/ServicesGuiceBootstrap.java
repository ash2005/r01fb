package r01f.services;

import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.internal.R01FBootstrapGuiceModule;
import r01f.services.client.internal.ServicesClientBindingsGuiceModule;
import r01f.services.client.internal.ServicesClientBootstrap;
import r01f.services.client.internal.ServicesClientBootstrapGuiceModule;
import r01f.services.core.internal.ServicesCoreBootstrap;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.inject.Module;

/**
 * Bootstraps a service-oriented guice-based application
 * (see R01MInjector type)
 * <ul>
 * 		<li>Core services implementation (a core service is ONLY bootstrapped if it's guice module is available in the classpath)</li>
 * 		<li>Client services proxy for the core services implementation</li>
 * </ul>
 */
@Slf4j
public class ServicesGuiceBootstrap {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Entry point for guice module loading
	 * @param appCode
	 * @return
	 */
	public static Collection<Module> loadBootstrapModuleInstances(final AppCode appCode) {
		// [1] - Load the CORE (server) guice module types for the cores defined at r01m.core.properties.xml file
    	log.warn("[START]-Finding CORE binding modules======================================================================================");
		Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> coreBootstrapModulesTypes = ServicesCoreBootstrap.loadBootstrapGuiceModuleTypes(appCode);
		log.warn("  [END]-Finding CORE binding modules======================================================================================");
		
		// [2] - Load the CLIENT guice module types for the proxies defined at r01m.client.properties.xml file
    	log.warn("[START]-Finding CLIENT binding modules====================================================================================");
    	ServicesClientBootstrap clientBootstrap = new ServicesClientBootstrap(appCode);
    	
		Collection<Class<? extends ServicesClientBootstrapGuiceModule>> clientBootstrapModulesTypes = clientBootstrap.loadBootstrapGuiceModuleTypes(appCode);
		Collection<Class<? extends ServicesClientBindingsGuiceModule>> clientBindingsModulesTypes = clientBootstrap.loadBootstrapGuiceBindingsModuleTypes(appCode);
		
		ServicesClientBootstrap.logFoundModules(clientBootstrapModulesTypes);
		ServicesClientBootstrap.logFoundModules(clientBindingsModulesTypes);
		log.warn("  [END]-Finding CLIENT binding modules====================================================================================");
		
		// [3] - Create a collection with the modules 
		int moduleNum = (CollectionUtils.hasData(coreBootstrapModulesTypes) ? coreBootstrapModulesTypes.size() : 0) +		// core guice modules
					    (CollectionUtils.hasData(clientBootstrapModulesTypes) ? clientBootstrapModulesTypes.size() : 0) +	// client proxies guice modules
					    (CollectionUtils.hasData(clientBindingsModulesTypes) ? clientBindingsModulesTypes.size() : 0) + 	// client bindings
					    1;																									// for R01F guice module
		List<Module> bootstrapModuleInstances = Lists.newArrayListWithCapacity(moduleNum);
		
		// 3.1 - Add the CORE guice modules
		if (CollectionUtils.hasData(coreBootstrapModulesTypes)) {
			bootstrapModuleInstances.addAll(Collections2.transform(coreBootstrapModulesTypes,
																   new Function<Class<? extends ServicesCoreBootstrapGuiceModule>,Module>() {
																			@Override
																			public Module apply(final Class<? extends ServicesCoreBootstrapGuiceModule> moduleType) {
																				return  ServicesCoreBootstrap.loadModule(moduleType);																			
																			}
																	}));
		}
		// 3.2 - Add the CLIENT guice modules
		for (Class<? extends ServicesClientBootstrapGuiceModule> clientBootstrapModule : clientBootstrapModulesTypes) {
			Module clientModule = ServicesClientBootstrap.loadModule(clientBootstrapModule);		
			bootstrapModuleInstances.add(0,clientModule);	// insert first!
		}
		// 3.3 - Add the CLIENT BINDINGS guice modules
		if (CollectionUtils.hasData(clientBindingsModulesTypes)) {
			for (Class<? extends ServicesClientBindingsGuiceModule> clientBindingsModule : clientBindingsModulesTypes) {
				Module clientModule = ServicesClientBootstrap.loadModule(clientBindingsModule);		
				bootstrapModuleInstances.add(clientModule);	// no matter if it's first or last
			}
		}
		// 3.4 - Add the mandatory R01F guice modules
		bootstrapModuleInstances.add(0,new R01FBootstrapGuiceModule());
		
		// [4] - Return the modules
		return bootstrapModuleInstances;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static String clientGuiceModulePackage(final AppCode appCode) {
		return Strings.of("{}.client.internal")
				  	  .customizeWith(appCode.getId())
				  	  .asString();
	}
	public static String coreGuiceModulePackage(final AppCode appCode) {
		return Strings.of("{}.internal")
					  .customizeWith(appCode.getId())
					  .asString();
	}
}
