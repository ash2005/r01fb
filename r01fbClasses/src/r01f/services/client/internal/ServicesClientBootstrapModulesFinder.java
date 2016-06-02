package r01f.services.client.internal; 

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.Module;
import com.google.inject.name.Named;

import lombok.extern.slf4j.Slf4j;
import r01f.reflection.ReflectionUtils;
import r01f.reflection.ReflectionUtils.FieldAnnotated;
import r01f.services.ServiceIDs.CoreAppAndModule;
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
	 * The package where the client bootstrap module will be looked for
	 */
	private final String _packageToLookForClientBootstrapType;
	/**
	 * Bootstrap client guice modules
	 */
	private final Set<Class<? extends ServicesClientGuiceModule>> _clientBootstrapGuiceModuleTypes;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesClientBootstrapModulesFinder(final String packageToLookForClientBootstrapType) {
		_packageToLookForClientBootstrapType = packageToLookForClientBootstrapType;
		
		// Try to find guice modules
		List<String> pckgs = Lists.newArrayListWithExpectedSize(2);
		pckgs.add(ServicesClientGuiceModule.class.getPackage().getName());	// beware to include also the package where ServicesClientGuiceModule is
		pckgs.add(_packageToLookForClientBootstrapType);
		Set<Class<? extends ServicesClientGuiceModule>> foundModuleTypes = ServicesPackages.findSubTypesAt(ServicesClientGuiceModule.class,
																										   pckgs,
																										   this.getClass().getClassLoader());
		_clientBootstrapGuiceModuleTypes = foundModuleTypes;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public ServiceInterfaceTypesToImplOrProxyMappings findServiceInterfaceTypesToImplOrProxyMappingsFor(final Collection<CoreAppAndModule> coreAppAndComponents) {
		List<String> pckgs = Lists.newArrayListWithExpectedSize(2);
		pckgs.add(ServiceInterfaceTypesToImplOrProxyMappings.class.getPackage().getName());	// beware to include also the package where ServiceInterfaceTypesToImplOrProxyMappings is
		pckgs.add(_packageToLookForClientBootstrapType);
		Set<Class<? extends ServiceInterfaceTypesToImplOrProxyMappings>> ts = ServicesPackages.findSubTypesAt(ServiceInterfaceTypesToImplOrProxyMappings.class,
																											  pckgs,
																											  this.getClass().getClassLoader());
		if (CollectionUtils.isNullOrEmpty(ts)) throw new IllegalStateException("Did NOT found a type extending " + ServiceInterfaceTypesToImplOrProxyMappings.class.getSimpleName() + " at " + _packageToLookForClientBootstrapType);
		if (ts.size() > 1) throw new IllegalStateException("There MUST be a single type extending " + ServiceInterfaceTypesToImplOrProxyMappings.class.getSimpleName() + " at " + _packageToLookForClientBootstrapType);
		Class<? extends ServiceInterfaceTypesToImplOrProxyMappings> t = Iterables.getOnlyElement(ts);
		
		// Ensure the type contains a Map annotated with @Named("{apiAppCode}.{apiComponent}")
		FieldAnnotated<Named>[] fieldsAnnotated = ReflectionUtils.fieldsAnnotated(t,Named.class);
		if (CollectionUtils.isNullOrEmpty(fieldsAnnotated)) throw new IllegalStateException("Type " + t.getName() + " MUST contain a Map<Class,ServiceInterface> field annotated with @Named(\"{coreAppCode}.{coreModule}\") for every core module: " + coreAppAndComponents);
		Set<CoreAppAndModule> mapFields = Sets.newHashSet();
		for (FieldAnnotated<Named> fieldAnnotated : fieldsAnnotated) {
			if (!fieldAnnotated.getField().getType().isAssignableFrom(Map.class)) continue;		// skip non maps
			for (CoreAppAndModule coreAppAndComponent : coreAppAndComponents) {
				if (fieldAnnotated.getAnnotation().value().equals(coreAppAndComponent.toString())) {
					mapFields.add(coreAppAndComponent);
					break;
				}
			}
		}
		Set<CoreAppAndModule> diff = Sets.difference(Sets.newHashSet(coreAppAndComponents),
													 mapFields);
		for (CoreAppAndModule coreAppAndComponent : diff) {
			log.warn("If {} is a CORE module, service interface to proxy/impl mapping type {} MUST contain a Map<Class,ServiceInterface> field annotated with @Named(\"{}\")",
					 coreAppAndComponent,t.getName(),coreAppAndComponent);
		}
		
		return ReflectionUtils.createInstanceOf(t);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Scans for types implementing {@link ServicesClientAPIBootstrapGuiceModuleBase}
     * (the {@link Module}s where client-side bindings are done
     * @return
     */
	public Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> findClientBootstrapGuiceModuleTypes() {
		Class<? extends ServicesClientAPIBootstrapGuiceModuleBase> outClientBootstrapModuleType = null;
				
		Set<Class<? extends ServicesClientAPIBootstrapGuiceModuleBase>> bootstrapModuleTypes = _filterModulesOfType(ServicesClientAPIBootstrapGuiceModuleBase.class);
		if (CollectionUtils.isNullOrEmpty(bootstrapModuleTypes)) {
			log.warn("There's NO client bootstrap module in the classpath! There MUST be AT LEAST a type extending {} at package {}: " + 
					 "The client api could NOT be bootstraped",
					 ServicesClientAPIBootstrapGuiceModuleBase.class,
					 _packageToLookForClientBootstrapType);
//			throw new IllegalStateException(Throwables.message("There's NO binding for client bindings-module in the classpath! There MUST be AT LEAST a guice binding module extending {} at package {}.client.internal in the classpath: " + 
//														       "The client-side bindings could NOT be bootstraped",
//															   ServicesClientAPIBootstrapGuiceModuleBase.class,_apiAppCode));
		} else if (bootstrapModuleTypes.size() > 1) {
			log.warn("There's more than a single client bootstrap module extending {} at package {}: {} > The client api could NOT be bootstraped!",
					 ServicesClientAPIBootstrapGuiceModuleBase.class,
					 _packageToLookForClientBootstrapType,
					 bootstrapModuleTypes);
		} else {
			outClientBootstrapModuleType = Iterables.getFirst(bootstrapModuleTypes,null);
		}
		return outClientBootstrapModuleType;
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
}
