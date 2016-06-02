package r01f.services;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.inject.Binding;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.inject.ServiceHandler;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.collections.CollectionUtils;

/**
 * Utility type that encapsulates the services life cycle operations
 * <ul>
 * 	<li>Guice injector creation</li>
 * 	<li>Start / Stop of services that needs an explicit starting (ie Persistence services, thread pools, indexexers, etc)</li>
 * </ul>
 * 
 * This type is mainly used at:
 * <ul>
 * 	<li>ServletContextListeners of web apps that controls the lifecycle of the app</li>
 * 	<li>Test init classes</li>
 * </ul>
 */
@Slf4j
public class ServicesLifeCycleUtil {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a guice module instance
	 * @param moduleType
	 * @return
	 */
	static Module createGuiceModuleInstance(final Class<? extends Module> moduleType) {
		try {
			return ReflectionUtils.createInstanceOf(moduleType,
												    new Class<?>[] {},
													new Object[] {});
		} catch (ReflectionException refEx) {																					
			log.error("Could NOT create an instance of {} bootstrap guice module. The module MUST have a no-args constructor",moduleType);
			throw refEx;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the guice modules to do the bootstrapping
	 * @param servicesInitData
	 * @return
	 */
	public static ServicesMainGuiceBootstrapCommonBindingModules getBootstrapGuiceModules(final ServicesInitData... servicesInitData) {
		if (CollectionUtils.isNullOrEmpty(servicesInitData)) throw new IllegalArgumentException();
		return ServicesLifeCycleUtil.getBootstrapGuiceModules(Arrays.asList(servicesInitData));
	}
	/**
	 * Returns the guice modules to do the bootstrapping
	 * @param servicesInitData
	 * @return
	 */
	public static ServicesMainGuiceBootstrapCommonBindingModules getBootstrapGuiceModules(final Collection<ServicesInitData> servicesInitData) {
		Collection<Module> bootstrapModules = ServicesMainGuiceBootstrap.createFor(servicesInitData)
																		.loadBootstrapModuleInstances();
		return new ServicesMainGuiceBootstrapCommonBindingModules(bootstrapModules);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ServicesMainGuiceBootstrapCommonBindingModules {
		private final Collection<Module> _bootstrapModules;
		
		public Collection<Module> withoutCommonBindingModules() {
			return _bootstrapModules;
		}
		public Iterable<Module> withCommonBindingModules(final Module... modules) {
			return CollectionUtils.hasData(modules) ? this.withCommonBindingModules(Arrays.asList(modules))
													: this.withoutCommonBindingModules();
		}
		public Iterable<Module> withCommonBindingModules(final Collection<Module> modules) {
			Iterable<Module> allBootstrapModuleInstances = CollectionUtils.hasData(modules) ? Iterables.concat(_bootstrapModules,
																											   modules)
																						    : _bootstrapModules;
			return allBootstrapModuleInstances;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates the guice injector
	 * @param servicesInitData
	 * @return
	 */
	public static ServicesLifeCycleInjectorCommonBindingModules createGuiceInjector(final ServicesInitData... servicesInitData) {
		if (CollectionUtils.isNullOrEmpty(servicesInitData)) throw new IllegalArgumentException();
		return new ServicesLifeCycleInjectorCommonBindingModules(Arrays.asList(servicesInitData));
	}
	/**
	 * Creates the guice injector
	 * @param servicesInitData
	 * @return
	 */
	public static ServicesLifeCycleInjectorCommonBindingModules createGuiceInjector(final Collection<ServicesInitData> servicesInitData) {
		return new ServicesLifeCycleInjectorCommonBindingModules(servicesInitData);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ServicesLifeCycleInjectorCommonBindingModules {
		private final Collection<ServicesInitData> _servicesInitData;
		
		public Injector withoutCommonBindingModules() {
			return Guice.createInjector(ServicesLifeCycleUtil.getBootstrapGuiceModules(_servicesInitData)
															 .withoutCommonBindingModules());
		}
		public Injector withCommonBindingModules(final Module... modules) {
			return Guice.createInjector(ServicesLifeCycleUtil.getBootstrapGuiceModules(_servicesInitData)
															 .withCommonBindingModules(modules));
		}
		public Injector withCommonBindingModules(final Collection<Module> modules) {
			return Guice.createInjector(ServicesLifeCycleUtil.getBootstrapGuiceModules(_servicesInitData)
															 .withCommonBindingModules(modules));
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Starts services that needs to be started
	 * @param hasServiceHandlerTypes
	 * @param injector
	 */
	public static void startServices(final Injector injector) {
		if (injector == null) throw new IllegalStateException("Cannot start services: no injector present!");
		
		// Init JPA's Persistence Service, Lucene indexes and everything that has to be started
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		Collection<Key<? extends ServiceHandler>> serviceHandlerBindingKeys = _getServiceHandlersGuiceBindingKeys(injector);
		if (CollectionUtils.hasData(serviceHandlerBindingKeys)) {
			for (Key<? extends ServiceHandler> key : serviceHandlerBindingKeys) {
				ServiceHandler serviceHandler = injector.getInstance(key);
				log.warn("\t--START SERVICE using {} type: {}",ServiceHandler.class.getSimpleName(),key);
				serviceHandler.start();
			}
		}
	}
	/**
	 * Stops services that needs to be started
	 * @param hasServiceHandlerTypes
	 * @param injector
	 */
	public static void stopServices(final Injector injector) {
		if (injector == null) {
			log.warn("NO injector present... cannot stop services");
			return;
		}
	
		// Close JPA's Persistence Service, Lucene indexes and everything that has to be closed
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		Collection<Key<? extends ServiceHandler>> serviceHandlerBindingKeys = _getServiceHandlersGuiceBindingKeys(injector);
		if (CollectionUtils.hasData(serviceHandlerBindingKeys)) {
			for (Key<? extends ServiceHandler> key : serviceHandlerBindingKeys) {
				ServiceHandler serviceHandler = injector.getInstance(key);
				if (serviceHandler != null) {
					log.warn("\t--END SERVICE {} type: {}",ServiceHandler.class.getSimpleName(),key);
					serviceHandler.stop();
				}
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Introspects the injector bindings to find all binding keys for {@link ServiceHandler} types
	 * @param injector
	 * @return
	 */
	private static Collection<Key<? extends ServiceHandler>> _getServiceHandlersGuiceBindingKeys(final Injector injector) {
		List<Binding<ServiceHandler>> bindings = injector.findBindingsByType(TypeLiteral.get(ServiceHandler.class));
		Collection<Key<? extends ServiceHandler>> outKeys = Lists.newArrayListWithExpectedSize(bindings.size());
		for (Binding<ServiceHandler> binding : bindings) {
			Key<? extends ServiceHandler> key = binding.getKey();
			outKeys.add(key);
		}
		return outKeys;
	}
}
