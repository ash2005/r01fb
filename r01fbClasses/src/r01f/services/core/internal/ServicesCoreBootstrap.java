package r01f.services.core.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.w3c.dom.Node;

import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesGuiceBootstrap;
import r01f.services.ServicesImpl;
import r01f.services.core.RESTImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.ServicesCore;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLProperties;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Module;

/**
 * Utility methods for loading the guice modules where the services core are bootstraping
 * The {@link #loadBootstrapGuiceModuleTypes(Collection)} scans the classpath for types implementing {@link ServicesCoreBootstrapGuiceModule} (a guice {@link Module} interface extension)
 * that simply MARKS that a type is a GUICE module in charge of bootstraping the services CORE (the real service implementation) 
 */
@Slf4j
public class ServicesCoreBootstrap {
    /**
     * Returns the implementation (REST, Bean, Mock, etc) of every services bootstrap module listed at [appCode].core.properties.xml file
     * to do so, it scans the packages under {core appCode}.internal for types implementing {@link ServicesCoreBootstrapGuiceModule}
     * 
	 * Some times a services implementation NEEDS (or DEPENDS UPON) another service implementation, for example, the REST services implementation
	 * NEEDS the Bean services implementation because REST services is only an ACCESS LAYER on top of the Bean services layer that
	 * is where the real services logic resides. 
	 * For these cases, the {@link ServicesCoreBootstrapGuiceModule} guice module MUST be annotated with {@link ServicesCoreDependencies}
     * @return
     */
    public static Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> loadBootstrapGuiceModuleTypes(final AppCode apiAppCode) {
    	// Load the core modules to be loaded from the r01m.core.properties file
    	Collection<ServicesCoreBootstrapConfig> coreBootstrapModules = _coreBootstrapModules(apiAppCode);
    	
    	if (coreBootstrapModules == null) return Lists.newArrayList();	// do not return a null config
    	
    	Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> outModuleTypes = Lists.newArrayList();
    	
    	if (CollectionUtils.hasData(coreBootstrapModules)) {    	    		
    		
			// Iterate over all the apps/modules (each app/module can have many ServicesCoreGuiceModules, ie: REST, Bean, etc.. one of them is the DEFAULT one)
			// NOTE: If more than one implementation is found, the BEAN has the highest priority followed by the REST implementation
			//
			// for each apps/module
			//		1.- Find the available ServicesCoreGuiceModules
			//		2.- For each found module found, try to find the needed modules 
    		//			(sometimes a module (ie REST) NEEDS another modules (ie Bean or EJB) to do delegate the work)
    		//			... this task is a bit tricky since the order in which the modules are found is important
    		//		    ... the checking of the presence of needed modules MUST be done AFTER all modules are processed
    		
			// Find guice modules implementing ServicesCoreGuiceModule either BeanImplementedServicesGuiceModuleBase, RESTImplementedServicesGuiceModuleBase, EJBImplementedServicesGuiceModuleBase, etc)
			Map<AppCode,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> servicesCoreBootstrapModuleTypesByApp = _findCoreGuiceModules(apiAppCode,
																										  					   						 coreBootstrapModules,
																										  					   						 ServicesCoreBootstrapGuiceModule.class);
    		for (ServicesCoreBootstrapConfig moduleCfg : coreBootstrapModules) {
    			
    			AppCode coreAppCode = moduleCfg.getAppCode();
    			AppComponent module = moduleCfg.getModule();
    			
    			// [1] - Get the modules for the appCode
    			Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> appServicesCoreBootstrapModuleTypes = servicesCoreBootstrapModuleTypesByApp.get(coreAppCode);
    			if (appServicesCoreBootstrapModuleTypes == null) {
    				log.warn("\t\t-{}.{} core will NOT be bootstraped: There's NO type implementing {} at package {} or the {} package is NOT in the classpath. " +
    						 "If the {}.{} core is to be bootstraped there MUST be AT LEAST a guice binding module extending {} at {} ", 
				 		     coreAppCode,module,ServicesCoreBootstrapGuiceModule.class,ServicesGuiceBootstrap.coreGuiceModulePackage(coreAppCode),ServicesGuiceBootstrap.coreGuiceModulePackage(coreAppCode),
				 		     coreAppCode,module,ServicesCoreBootstrapGuiceModule.class,ServicesGuiceBootstrap.coreGuiceModulePackage(coreAppCode));
    				continue;
    			}
    			log.warn("\t\t-{}.{} core will be bootstraped with: {}",
    					 coreAppCode,module,servicesCoreBootstrapModuleTypesByApp.get(coreAppCode));
    			
    			Map<Class<? extends ServicesCoreBootstrapGuiceModule>,Collection<ServicesImpl>> moduleTypesWithDependencies = Maps.newHashMapWithExpectedSize(coreBootstrapModules.size());
    			
    			// [2] - for each found module try to find the needed modules
    			for (Class<? extends ServicesCoreBootstrapGuiceModule> foundModuleType : appServicesCoreBootstrapModuleTypes) {
    				if (ReflectionUtils.isInterface(foundModuleType)) continue;  
    				 
    				ServicesCore servicesCoreAnnot = ReflectionUtils.typeAnnotation(foundModuleType,
    																		  		ServicesCore.class);
    				
    				// Check if the module set at @ServicesCore annotation is the same as the expected one
    				if (servicesCoreAnnot == null) {
    					log.error("The module {} MUST be annotated with @{}(forId=\"[moduleName]\" where [moduleName] is the one configured at apiAppCode.core.properties.xml",
    							  foundModuleType.getClass(),ServicesCore.class.getSimpleName());
    					continue;
    				}
    				if (!servicesCoreAnnot.moduleId().equals(module.asString())) {
    					log.warn("The module id={} set at {} is NOT the expected one ({})... it'll be ignored",
    							 servicesCoreAnnot.moduleId(),foundModuleType,module);
    					continue;	// ignore modules whose id is NOT the expected one
    				}
    				
					// find the needed impl (the ServicesGuiceModule-implementing type MUST be annotated with ServicesGuiceModuleDependencies)
					// (sometimes a service impl requires of another service impl, for example, REST services USES Bean services)
					Collection<ServicesImpl> moduleDependencies = null;
					if (!CollectionUtils.of(servicesCoreAnnot.dependsOn())
								   		.contains(ServicesImpl.NULL)) {
						moduleDependencies = Lists.newArrayListWithCapacity(servicesCoreAnnot.dependsOn().length);
						for (ServicesImpl dependency : servicesCoreAnnot.dependsOn()) {
							moduleDependencies.add(dependency);
						}
						log.warn("\t\t\t- Found {} CORE services bootstrap module that DEPENDS UPON {} (see {})",
								 foundModuleType,servicesCoreAnnot.dependsOn(),foundModuleType);
					} else {
						log.warn("\t\t\t- Found {} CORE services bootstrap module",
								 foundModuleType);
					}
					
					// Put the found module alongside it's dependencies in the output map
					moduleTypesWithDependencies.put(foundModuleType,moduleDependencies);	
					
    			} // for bindingModules    			
    			
	    		// Make sure that the DEFAULT service implementation exists and it's loaded
    			ServicesImpl defaultServiceImpl = moduleCfg.getDefaultImpl();
				boolean defaultModuleIsPresent = false;
				for (Class<? extends ServicesCoreBootstrapGuiceModule> foundBootstrapModuleType : appServicesCoreBootstrapModuleTypes) {
					ServicesImpl foundServiceImpl = ServicesImpl.fromBindingModule(foundBootstrapModuleType);
					if (foundServiceImpl == defaultServiceImpl) {
						defaultModuleIsPresent = true;
						break;
					}
				}
				if (!defaultModuleIsPresent) log.warn("The default implementation for the appCode/module={}/{} services is {} BUT NO CORE Guice binding module extending {} was found. If this is a CLIENT module this is NOT AN ERROR simply ignore it",
													  coreAppCode,module,defaultServiceImpl,defaultServiceImpl.getCoreGuiceModuleType());
    		
				
				// [3] - Make sure that the dependencies are satisfied
	    		for (Map.Entry<Class<? extends ServicesCoreBootstrapGuiceModule>,Collection<ServicesImpl>> moduleTypeWithDependenciesEntry : moduleTypesWithDependencies.entrySet()) {
	    			Class<? extends ServicesCoreBootstrapGuiceModule> moduleType = moduleTypeWithDependenciesEntry.getKey();
	    			Collection<ServicesImpl> dependencies = moduleTypeWithDependenciesEntry.getValue();
					if (CollectionUtils.hasData(dependencies)) {
						for (ServicesImpl dependency : dependencies) {
							boolean isLoaded = false;
							for (Class<? extends ServicesCoreBootstrapGuiceModule> otherModule : moduleTypesWithDependencies.keySet()) {
								if (ServicesImpl.fromBindingModule(otherModule) == dependency) {
									isLoaded = true;
									break;
								}
							}
							if (!isLoaded) throw new IllegalStateException(Strings.customized("The module {}.{} NEEDS (depends on) a {} module implementation (see {})." + 
																							  "BUT this module could NOT be loaded." +
																							  "Please ensure that a {} annotated type with impl={} attribute is accesible in the run-time classpath (maybe de dependent project is NOT deployed and available at the classpath)",
																							  coreAppCode,module,dependency,moduleType,
																							  ServicesCoreBootstrapGuiceModule.class,dependency));
						}
					}
	    		}					
	    		// [4] - Finally put the modules in the output collection
	    		outModuleTypes.addAll(moduleTypesWithDependencies.keySet());
				
    		} // for configuredBindingModules
    		
    	}
    	return outModuleTypes;
    }
    /**
     * Finds types extending {@link ServicesCoreBootstrapGuiceModule}: {@link BeanImplementedServicesCoreGuiceModule}, {@link RESTImplementedServicesCoreGuiceModuleBase}, etc
     * @param coreAppCode
     * @param coreModule
     * @return
     */
	private static Map<AppCode,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> _findCoreGuiceModules(final AppCode apiAppCode,
																							  			   			final Collection<ServicesCoreBootstrapConfig> coreBootstrapModules,
																							  			   			final Class<? extends ServicesCoreBootstrapGuiceModule> coreGuiceModuleType) {
		// Get a Collection of core appCodes
		Collection<AppCode> coreAppCodes = FluentIterable.from(coreBootstrapModules)
														 .transform(new Function<ServicesCoreBootstrapConfig,AppCode>() {
																				@Override
																				public AppCode apply(final ServicesCoreBootstrapConfig coreBootstapModule) {
																					return coreBootstapModule.getAppCode();
																				}
															 
														 			})
														 .toSet();
		// Find the types implementing ServicesCoreGuiceModule
		Set<Class<? extends ServicesCoreBootstrapGuiceModule>> foundBootstrapModuleTypes = _findCoreGuiceModulesOrNull(coreAppCodes,
																											 	       coreGuiceModuleType);
		Map<AppCode,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> outCoreModules = null;
		if (CollectionUtils.isNullOrEmpty(foundBootstrapModuleTypes)) { 
			log.warn("There's NO type implementing {} in the classpath! For the CORE apps, there MUST be AT LEAST a guice binding module extending {} in the classpath: " +
					 "please review that the modules specified at <coreModules> section of {}.client.properties.xml are correct and that exists any guice module " + 
					 "at packages {} implementing {}", 
					 ServicesCoreBootstrapGuiceModule.class.getSimpleName(),ServicesCoreBootstrapGuiceModule.class,
					 apiAppCode,
					 ServicesGuiceBootstrap.coreGuiceModulePackage(AppCode.forId(coreAppCodes.toString())),
					 ServicesCoreBootstrapGuiceModule.class);
			outCoreModules = Maps.newHashMap();
		} else {
			// Group the modules by appCode
			outCoreModules = Maps.newHashMapWithExpectedSize(coreAppCodes.size());
			for (Class<? extends ServicesCoreBootstrapGuiceModule> bootstrapModuleType : foundBootstrapModuleTypes) {
				AppCode appCode = AppCode.forId(bootstrapModuleType.getPackage().getName().split("\\.")[0]);		// the appCode is extracted from the package
				Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> appModuleTypes = outCoreModules.get(appCode);
				if (appModuleTypes == null) {
					appModuleTypes = Sets.newHashSet();
					outCoreModules.put(appCode,appModuleTypes);
				}
				appModuleTypes.add(bootstrapModuleType);
			}
		}
		return outCoreModules;
    }
    /**
     * Finds types extending {@link ServicesCoreBootstrapGuiceModule}: {@link BeanImplementedServicesCoreGuiceModule}, {@link RESTImplementedServicesCoreGuiceModuleBase}, etc
     * if no type is found it returns null
     * @param coreAppCode
     * @param coreModule
     * @return
     */
    @SuppressWarnings("unchecked")
    private static Set<Class<? extends ServicesCoreBootstrapGuiceModule>> _findCoreGuiceModulesOrNull(final Collection<AppCode> coreAppCodes,
    																		   		  		 		  final Class<? extends ServicesCoreBootstrapGuiceModule> coreGuiceModuleType) {
		List<URL> urls = new ArrayList<URL>();	
		urls.addAll(ClasspathHelper.forPackage(ServicesCoreBootstrapGuiceModule.class.getPackage().getName()));	
		urls.addAll(ClasspathHelper.forPackage("r01f.internal"));
		for (AppCode coreAppCode : coreAppCodes) urls.addAll(ClasspathHelper.forPackage(ServicesGuiceBootstrap.coreGuiceModulePackage(coreAppCode)));
		
		Reflections typeScanner = new Reflections(new ConfigurationBuilder()
															.setUrls(urls)		// see https://code.google.com/p/reflections/issues/detail?id=53
															.setScanners(new SubTypesScanner()));
		Set<?> foundBootstrapModuleTypes = typeScanner.getSubTypesOf(coreGuiceModuleType);
		
		// Filter the interfaces
		Set<Class<? extends ServicesCoreBootstrapGuiceModule>> outModuleTypes = (Set<Class<? extends ServicesCoreBootstrapGuiceModule>>)foundBootstrapModuleTypes;
		return FluentIterable.from(outModuleTypes)
							 .filter(new Predicate<Class<? extends ServicesCoreBootstrapGuiceModule>>() {
											@Override
											public boolean apply(final Class<? extends ServicesCoreBootstrapGuiceModule> module) {
												return ReflectionUtils.isInstanciable(module);
											}
			
								     })
							 .toSet();
    }
    /**
     * Loads the core modules from the properties file
     * <pre class='brush:xml'>
	 *		<core>
	 *			<modules>
	 *				<module appCode="r01e" id="structures" impl="REST">Label manager</module>
	 *				...
	 *			</modules>
	 *		</core>
     * </pre>
     * @param apiAppCode the app code for the api (it's used to find the [core] properties xml)
     * @return
     */
    private static Collection<ServicesCoreBootstrapConfig> _coreBootstrapModules(final AppCode apiAppCode) {
    	return XMLProperties.createForAppComponent(apiAppCode,AppComponent.forId("core"))
    						.notUsingCache()
		  			        .propertyAt("/core/modules")
  					        .asObjectList(new Function<Node,ServicesCoreBootstrapConfig>() {
													@Override
													public ServicesCoreBootstrapConfig apply(final Node node) {
														String appCode = XMLUtils.nodeAttributeValue(node,"appCode");
														String moduleId = XMLUtils.nodeAttributeValue(node,"id");
														String implStr = XMLUtils.nodeAttributeValue(node,"impl");
														return new ServicesCoreBootstrapConfig(AppCode.forId(appCode),AppComponent.forId(moduleId),
																						       ServicesImpl.fromNameOrNull(implStr));	// null if implStr == null
													}
			 					          });
    }
    /**
     * Loads a services bootstrap guice module
     * @param moduleType
     * @return
     */
    public static Module loadModule(final Class<?> moduleType) {
		try {
			return  ReflectionUtils.createInstanceOf(moduleType,
												     new Class<?>[] {},
													 new Object[] {});
		} catch (ReflectionException refEx) {																					
			log.error("Could NOT create an instance of {} services bootstrap guice module. The module MUST have a no-args constructor",moduleType);
			throw refEx;
		}
    }
}
