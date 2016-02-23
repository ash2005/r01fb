package r01f.services.client.internal;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.inject.Module;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesPackages;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.util.types.collections.CollectionUtils;

/**
 * Utility methods for loading the guice modules where the services client are bootstraping
 * The {@link #loadProxyBingingsGuiceModuleTypes(Collection)} scans the classpath for types implementing {@link ServicesCoreBootstrapGuiceModule} (a guice {@link Module} interface extension)
 * that simply MARKS that a type is a GUICE module in charge of bootstraping the services CORE (the real service implementation) 
 */
@Slf4j
public class ServicesClientBootstrapModulesFinder {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Api app code
	 */
	private final AppAndComponent _apiAppAndModule;
	/**
	 * Bootstrap client guice modules
	 */
	private final Set<Class<? extends ServicesClientGuiceModule>> _clientBootstrapGuiceModuleTypes;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesClientBootstrapModulesFinder(final AppAndComponent apiAppAndComponent) {
		_apiAppAndModule = apiAppAndComponent;
		
		// Try to find guice modules
    	String clientGuiceModulePackage = ServicesPackages.clientGuiceModulePackage(apiAppAndComponent);
		
		List<String> pckgs = Lists.newArrayListWithExpectedSize(2);
		pckgs.add(ServicesClientGuiceModule.class.getPackage().getName());	// beware to include also the package where ServicesClientGuiceModule is
		pckgs.add(clientGuiceModulePackage);
		Set<Class<? extends ServicesClientGuiceModule>> foundModuleTypes = ServicesPackages.findSubTypesAt(ServicesClientGuiceModule.class,
																										   pckgs,
																										   this.getClass().getClassLoader());
		_clientBootstrapGuiceModuleTypes = foundModuleTypes;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Scans for types implementing {@link ServicesClientAPIBootstrapGuiceModuleBase}
     * (the {@link Module}s where client-side bindings are done
     * @return
     */
	public Collection<Class<? extends ServicesClientAPIBootstrapGuiceModuleBase>> findProxyBingingsGuiceModuleTypes() {
		Set<Class<? extends ServicesClientAPIBootstrapGuiceModuleBase>> bootstrapModuleTypes = _filterModulesOfType(ServicesClientAPIBootstrapGuiceModuleBase.class);
		if (CollectionUtils.isNullOrEmpty(bootstrapModuleTypes)) {
			log.warn("There's NO binding for client bindings-module in the classpath! There MUST be AT LEAST a guice binding module extending {} at package {} in the classpath: " + 
					 "The client-side bindings could NOT be bootstraped",
					 ServicesClientAPIBootstrapGuiceModuleBase.class,
					 ServicesPackages.clientGuiceModulePackage(_apiAppAndModule));
//			throw new IllegalStateException(Throwables.message("There's NO binding for client bindings-module in the classpath! There MUST be AT LEAST a guice binding module extending {} at package {}.client.internal in the classpath: " + 
//														       "The client-side bindings could NOT be bootstraped",
//															   ServicesClientAPIBootstrapGuiceModuleBase.class,_apiAppCode));
		}
		return bootstrapModuleTypes;
    }
	private <M extends ServicesClientGuiceModule> Set<Class<? extends M>> _filterModulesOfType(final Class<M> moduleType) {
		Set<Class<? extends M>> outModuleTypes = FluentIterable.from(_clientBootstrapGuiceModuleTypes)
															   .filter(new Predicate<Class<? extends ServicesClientGuiceModule>>() {
																			@Override
																			public boolean apply(final Class<? extends ServicesClientGuiceModule> modType) {
																				return ReflectionUtils.isInstanciable(modType)		// avoid abstract & interface types
																				    && ReflectionUtils.isImplementing(modType,		
																				    								  moduleType);
																			}
															 		 })
															   .transform(new Function<Class<? extends ServicesClientGuiceModule>,Class<? extends M>>() {																											
																				@Override @SuppressWarnings("unchecked")
																				public Class<? extends M> apply(final Class<? extends ServicesClientGuiceModule> modType) {
																					return (Class<? extends M>)modType;
																				}
															 			})
															   .toSet();	
		return outModuleTypes;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Logs a collection of guice module types
	 * @param moduleTypes
	 */
	public static <M extends ServicesClientGuiceModule> void logFoundModules(final Collection<Class<? extends M>> moduleTypes) {
		if (CollectionUtils.hasData(moduleTypes)) {
			for (Class<? extends M> moduleType : moduleTypes) {
				log.warn("\t\t- Found {} client services guice module",
						 moduleType);
			}
		} 
	}
}
