package r01f.services.core.internal;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Module;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesImpl;
import r01f.services.ServicesPackages;
import r01f.services.core.RESTImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.ServicesCore;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

/**
 * Utility methods for loading the guice modules where the services core are bootstraping
 * The {@link #loadBootstrapGuiceModuleTypes(Collection)} scans the classpath for types implementing {@link ServicesCoreBootstrapGuiceModule} (a guice {@link Module} interface extension)
 * that simply MARKS that a type is a GUICE module in charge of bootstraping the services CORE (the real service implementation) 
 */
@Slf4j
public class ServicesCoreBootstrapModulesFinder {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Api app code
	 */
	private final AppCode _apiAppCode;
	/**
	 * The core app and modules
	 */
	private final Collection<AppAndComponent> _coreAppAndModules;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreBootstrapModulesFinder(final AppCode apiAppCode,
											  final Collection<AppAndComponent> coreAppAndModules) {
		_apiAppCode = apiAppCode;
		_coreAppAndModules = coreAppAndModules;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the implementation type (REST, Bean, Mock, etc) of every services bootstrap module listed at [appCode].client.properties.xml file
     * to do so, it scans the packages under {core appCode}.internal for types implementing {@link ServicesCoreBootstrapGuiceModule}
     * 
	 * Some times a services implementation NEEDS (or DEPENDS UPON) another service implementation, for example, the REST services implementation
	 * NEEDS the Bean services implementation because REST services is only an ACCESS LAYER on top of the Bean services layer that
	 * is where the real services logic resides. 
     * @return
     */
    public Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> findBootstrapGuiceModuleTypes() {
    	if (_coreAppAndModules == null) return Maps.newHashMap();	// do not return a null config    	
    	
    	Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> outModuleTypes = Maps.newHashMap();
    	
		// Iterate over all the app/module collection (each app/module can have many ServicesCoreGuiceModules, ie: REST, Bean, etc.. one of them is the DEFAULT one)
		// NOTE: If more than one implementation is found, the BEAN has the highest priority followed by the REST implementation
		//
		// for each app/module
		//		1.- Find the available ServicesCoreGuiceModules
		//		2.- For each found module found, try to find the needed modules 
		//			(sometimes a module (ie REST) NEEDS another modules (ie Bean or EJB) to do delegate the work)
		//			... this task is a bit tricky since the order in which the modules are found is important
		//		    ... the checking of the presence of needed modules MUST be done AFTER all modules are processed
		
		// Find guice modules implementing ServicesCoreGuiceModule either BeanImplementedServicesGuiceModuleBase, RESTImplementedServicesGuiceModuleBase, EJBImplementedServicesGuiceModuleBase, etc)
		Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> coreBootstrapModuleTypesByApp = _findCoreBootstrapGuiceModuleTypesByAppModule(_coreAppAndModules);
		for (AppAndComponent coreAppModule : _coreAppAndModules) {
			
			AppCode coreAppCode = coreAppModule.getAppCode();
			AppComponent module = coreAppModule.getAppComponent();
			
			
			// [1] - Get the modules for the appCode
			Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> appModuleCoreBootstrapModuleTypes = coreBootstrapModuleTypesByApp.get(coreAppModule);
			if (appModuleCoreBootstrapModuleTypes == null) {
				log.warn("\t\t-{} core will NOT be bootstraped: There's NO type implementing {} at package {} or the {} package is NOT in the classpath. " +
						 "If the {} core is to be bootstraped there MUST be AT LEAST a guice binding module extending {} at {} ", 
			 		     coreAppModule,ServicesCoreBootstrapGuiceModule.class,ServicesPackages.coreGuiceModulePackage(coreAppCode),ServicesPackages.coreGuiceModulePackage(coreAppCode),
			 		     coreAppModule,ServicesCoreBootstrapGuiceModule.class,ServicesPackages.coreGuiceModulePackage(coreAppCode));
				continue;
			}
			log.warn("\t\t-{} core will be bootstraped with: {}",
					 coreAppModule,coreBootstrapModuleTypesByApp.get(coreAppModule));
			
			
			// [2] - for each found core bootstrap module try to find the needed modules (ie REST bootstrap modules depends on BEAN bootstrap modules)
			Map<Class<? extends ServicesCoreBootstrapGuiceModule>,Collection<ServicesImpl>> bootstrapTypesWithDependencies = Maps.newHashMapWithExpectedSize(_coreAppAndModules.size());
			for (Class<? extends ServicesCoreBootstrapGuiceModule> foundModuleType : appModuleCoreBootstrapModuleTypes) {
				if (ReflectionUtils.isInterface(foundModuleType)) continue;  
				 
				// Check if there's any module dependency set at @ServicesCore annotation
				ServicesCore servicesCoreAnnot = ReflectionUtils.typeAnnotation(foundModuleType,
																		  		ServicesCore.class);
				
				// find the needed impl (the ServicesGuiceModule-implementing type MUST be annotated with ServicesGuiceModuleDependencies)
				// (sometimes a service impl requires of another service impl, for example, REST services USES Bean services)
				Collection<ServicesImpl> moduleDependencies = null;
				if (!CollectionUtils.of(servicesCoreAnnot.dependsOn())
							   		.contains(ServicesImpl.NULL)) {
					moduleDependencies = Lists.newArrayList(servicesCoreAnnot.dependsOn()); 
					log.warn("\t\t\t- Found {} CORE services bootstrap type that DEPENDS UPON {}",
							 foundModuleType,moduleDependencies);
				} else {
					log.warn("\t\t\t- Found {} CORE services bootstrap module (no other bootstrap type dependency)",
							 foundModuleType);
				}
				
				// Put the found module alongside it's dependencies in the output map
				bootstrapTypesWithDependencies.put(foundModuleType,moduleDependencies);	
				
			} // for bindingModules    			
			
			
			// [3] - Make sure that the dependencies are satisfied
    		for (Map.Entry<Class<? extends ServicesCoreBootstrapGuiceModule>,Collection<ServicesImpl>> bootstrapTypeWithDependency : bootstrapTypesWithDependencies.entrySet()) {
    			Class<? extends ServicesCoreBootstrapGuiceModule> moduleType = bootstrapTypeWithDependency.getKey();	// bootstrap type
    			Collection<ServicesImpl> dependencies = bootstrapTypeWithDependency.getValue();							// it's dependencies
    			
    			// try to see if the dependencies are satified
				if (CollectionUtils.hasData(dependencies)) {
					for (ServicesImpl dependency : dependencies) {
						boolean isLoaded = false;
						for (Class<? extends ServicesCoreBootstrapGuiceModule> otherModule : bootstrapTypesWithDependencies.keySet()) {
							if (ServicesImpl.fromBindingModule(otherModule) == dependency) {
								isLoaded = true;
								break;
							}
						}
						if (!isLoaded) throw new IllegalStateException(Strings.customized("The module {} NEEDS (depends on) a {} module implementation (see {})." + 
																						  "BUT this module could NOT be loaded." +
																						  "Please ensure that a {} annotated type with impl={} attribute is accesible in the run-time classpath (maybe de dependent project is NOT deployed and available at the classpath)",
																						  coreAppModule,dependency,moduleType,
																						  ServicesCoreBootstrapGuiceModule.class,dependency));
					}
				}
    		}					
    		// [4] - Finally put the core bootstrap modules in the output collection indexed by the appCode/component
    		outModuleTypes.put(coreAppModule,bootstrapTypesWithDependencies.keySet());
			
		} // for configuredBindingModules
    	return outModuleTypes;
    }
    /**
     * Finds types extending {@link ServicesCoreBootstrapGuiceModule}: {@link BeanImplementedServicesCoreGuiceModule}, {@link RESTImplementedServicesCoreGuiceModuleBase}, etc
     * and returns them indexed by appCode / component 
     * @param coreAppCode
     * @param coreModule
     * @return
     */
	private Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> _findCoreBootstrapGuiceModuleTypesByAppModule(final Collection<AppAndComponent> coreAppAndComponents) {
		// get the core appcodes from the collection of core appCode/module
		Collection<AppCode> coreAppCodes = FluentIterable.from(coreAppAndComponents)
														 .transform(new Function<AppAndComponent,AppCode>() {
																			@Override
																			public AppCode apply(final AppAndComponent appAndComponent) {
																				return appAndComponent.getAppCode();
																			}
														 			})
														 .toSet();
		
		// Find the types implementing ServicesCoreGuiceModule at all the core appcodes (ie: r01t, aa14b, aa81b, etc)
		Set<Class<? extends ServicesCoreBootstrapGuiceModule>> foundBootstrapModuleTypes = _findCoreGuiceModulesOrNull(coreAppCodes,
																											 	       ServicesCoreBootstrapGuiceModule.class);
		
		// Group the found core bootstrap module types by appCode/module
		Map<AppAndComponent,Collection<Class<? extends ServicesCoreBootstrapGuiceModule>>> outCoreModules = null;
		if (CollectionUtils.hasData(foundBootstrapModuleTypes)) {
			
			outCoreModules = Maps.newHashMapWithExpectedSize(coreAppCodes.size());
			for (Class<? extends ServicesCoreBootstrapGuiceModule> bootstrapModuleType : foundBootstrapModuleTypes) {
				
				// get tye appCode from the bootstrap module type's package (ie: r01t.internal.XXX) and it's @ServiceCore annotation 
				AppCode appCode = ServicesPackages.appCodeFromCoreBootstrapModuleType(bootstrapModuleType);							// use the service's core bootstrap type's package to get the appCode
				AppComponent appComponent = ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(bootstrapModuleType);		// use the service's @ServicesCore annotation to get the module
				AppAndComponent appAndComponent = AppAndComponent.composedBy(appCode,appComponent);
				
				Collection<Class<? extends ServicesCoreBootstrapGuiceModule>> appModuleTypes = outCoreModules.get(appAndComponent);
				if (appModuleTypes == null) {
					appModuleTypes = Sets.newHashSet();
					outCoreModules.put(appAndComponent,appModuleTypes);
				}
				appModuleTypes.add(bootstrapModuleType);
			}
		} else {
			log.warn("There's NO type implementing {} in the classpath! For the CORE app codes {}, there MUST be AT LEAST a guice binding module extending {} in the classpath: " +
					 "please review that the modules specified at /client/module section of {}.client.properties.xml are correct and that exists any guice module " + 
					 "at packages {} implementing {}", 
					 ServicesCoreBootstrapGuiceModule.class.getSimpleName(),coreAppCodes,ServicesCoreBootstrapGuiceModule.class,
					 _apiAppCode,
					 ServicesPackages.coreGuiceModulePackage(AppCode.forId(coreAppCodes.toString())),
					 ServicesCoreBootstrapGuiceModule.class);
			outCoreModules = Maps.newHashMap();
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
		for (AppCode coreAppCode : coreAppCodes) urls.addAll(ClasspathHelper.forPackage(ServicesPackages.coreGuiceModulePackage(coreAppCode)));
		
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
 }
