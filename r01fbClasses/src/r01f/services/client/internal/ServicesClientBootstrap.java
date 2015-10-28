package r01f.services.client.internal;

import java.net.URL;
import java.util.Collection;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.AppCode;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesGuiceBootstrap;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.inject.Module;

/**
 * Utility methods for loading the guice modules where the services client are bootstraping
 * The {@link #loadBootstrapGuiceModuleTypes(Collection)} scans the classpath for types implementing {@link ServicesCoreBootstrapGuiceModule} (a guice {@link Module} interface extension)
 * that simply MARKS that a type is a GUICE module in charge of bootstraping the services CORE (the real service implementation) 
 */
@Slf4j
public class ServicesClientBootstrap {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Bootstrap client guice modules
	 */
	private final Set<Class<? extends ServicesClientGuiceModule>> _clientGuiceModules;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesClientBootstrap(final AppCode apiAppCode) {
		// Try to find guice modules
    	String clientGuiceModulePackage = ServicesGuiceBootstrap.clientGuiceModulePackage(apiAppCode);
		log.info("\t  Finding subtypes of {} at package {}",
				 ServicesClientGuiceModule.class,clientGuiceModulePackage);
		
		Collection<URL> urls = Lists.newArrayListWithExpectedSize(2);
		urls.addAll(ClasspathHelper.forPackage(ServicesClientGuiceModule.class.getPackage().getName()));	// beware to include also the package where ServicesClientGuiceModule is
		urls.addAll(ClasspathHelper.forPackage(clientGuiceModulePackage));
		
		Reflections typeScanner = new Reflections(new ConfigurationBuilder()
															.setScanners(new SubTypesScanner(true))
															.setUrls(urls));
		Set<Class<? extends ServicesClientGuiceModule>> foundModuleTypes = typeScanner.getSubTypesOf(ServicesClientGuiceModule.class);
		_clientGuiceModules = foundModuleTypes;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Scans for types implementing {@link ServicesClientBootstrapGuiceModule}
     * (the {@link Module}s where client-side bindings are done
     * @return
     */
	public Collection<Class<? extends ServicesClientBootstrapGuiceModule>> loadBootstrapGuiceModuleTypes(final AppCode apiAppCode) {
		Set<Class<? extends ServicesClientBootstrapGuiceModule>> bootstrapModuleTypes = _filterModulesOfType(ServicesClientBootstrapGuiceModule.class);
		if (CollectionUtils.isNullOrEmpty(bootstrapModuleTypes)) throw new IllegalStateException(Throwables.message("There's NO binding for client bindings-module in the classpath! There MUST be AT LEAST a guice binding module extending {} at package {}.client.internal in the classpath: " + 
																												    "The client-side bindings could NOT be bootstraped",
																												    ServicesClientBootstrapGuiceModule.class,apiAppCode));
		return bootstrapModuleTypes;
    }
    /**
     * Scans for types implementing {@link ServicesClientBindingsGuiceModule}
     * (the {@link Module}s where client-side bindings are done
     * @param apiAppCode
     * @return
     */
	public Collection<Class<? extends ServicesClientBindingsGuiceModule>> loadBootstrapGuiceBindingsModuleTypes(final AppCode apiAppCode) {
		return _filterModulesOfType(ServicesClientBindingsGuiceModule.class);
    }
	private <M extends ServicesClientGuiceModule> Set<Class<? extends M>> _filterModulesOfType(final Class<M> moduleType) {
		Set<Class<? extends M>> outModuleTypes = FluentIterable.from(_clientGuiceModules)
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
    /**
     * Loads a services bootstrap guice module
     * @param moduleType
     * @return
     */
    public static Module loadModule(final Class<?> moduleType) {
		try {
			Module clientGuiceModule = ReflectionUtils.createInstanceOf(moduleType,
																		new Class<?>[] {},
																		new Object[] {});
			return  clientGuiceModule;
		} catch (ReflectionException refEx) {																					
			log.error("Could NOT create an instance of {} services bootstrap guice module. The module MUST have a no-args constructor",moduleType);
			throw refEx;
		}
    }
}
