package r01f.services.client.internal;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.patterns.Memoized;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesImpl;
import r01f.services.ServicesPackages;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceInterfaceFor;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
public class ServicesClientInterfaceToImplAndProxyFinder {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The api app code
	 */
	private final AppCode _apiAppCode;
	/**
	 * The core app codes and modules and the default service proxy impl 
	 * (it's loaded from {apiAppCode}.client.properties.xml
	 */
	private final Map<AppAndComponent,ServicesImpl> _coreAppAndModulesDefaultProxy;
/////////////////////////////////////////////////////////////////////////////////////////
//  SERVICES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all {@link ServiceInterface} types:
	 * <ul>
	 * 		<li>the {@link ServiceInterface} itself</li>
	 * 		<li>the {@link ServiceProxyImpl}s (Bean, REST, EJB...) -note that the {@link ServiceProxyImpl}s also implements {@link ServiceInterface})</li>
	 * 		<li>the {@link ServiceInterface} implementations</li>
	 * </ul>
	 * with all this info, the method returns a correlation of the {@link ServiceInterface} with the implementation and the {@link ServiceProxyImpl}
	 * @param coreAppCode
	 * @return
	 */
	public Map<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> findServiceInterfacesToImplAndProxiesBindings(final AppCode coreAppCode) {
		return this.findServiceInterfacesToImplAndProxiesBindings(Sets.newHashSet(coreAppCode));
	}
	/**
	 * Finds all {@link ServiceInterface} types:
	 * <ul>
	 * 		<li>the {@link ServiceInterface} itself</li>
	 * 		<li>the {@link ServiceProxyImpl}s (Bean, REST, EJB...) -note that the {@link ServiceProxyImpl}s also implements {@link ServiceInterface})</li>
	 * 		<li>the {@link ServiceInterface} implementations</li>
	 * </ul>
	 * with all this info, the method returns a correlation of the {@link ServiceInterface} with the implementation and the {@link ServiceProxyImpl}
	 * @param apiAppCode
	 * @param coreAppCodes
	 * @return
	 */
	public Map<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> findServiceInterfacesToImplAndProxiesBindings(final Collection<AppCode> coreAppCodes) {
		log.warn("\t\tFinding {}-implementing types at [{}] client app code and {} core app codes",
				 ServiceInterface.class.getSimpleName(),_apiAppCode,coreAppCodes);
		
		// Find all the ServiceInterface implementing types
		ServiceInterfaceImplementingTypes serviceInterfaceImplementingTypes = new ServiceInterfaceImplementingTypes(_apiAppCode,
																													coreAppCodes);
		
		// ... within all filter all service interfaces
		Collection<Class<? extends ServiceInterface>> interfaceTypes = serviceInterfaceImplementingTypes.getInterfaceTypes().get();

		// ... within all filter all the proxies 
		Collection<Class<? extends ServiceProxyImpl>> proxyTypes = serviceInterfaceImplementingTypes.getProxyTypes().get();
		
		// ... within all filter all the implementations (the beans that implements the service interfaces) 
		// 	   note that NOT all the service interface implementations will be available on the classpath
		Collection<Class<? extends ServiceInterface>> implTypes = serviceInterfaceImplementingTypes.getImplementationTypes().get();
		
		// Correlate the service interface with it's proxies and implementation (where available)
		Map<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> outServiceInterfacesToImpls = Maps.newHashMapWithExpectedSize(coreAppCodes.size());
		for (final Class<? extends ServiceInterface> interfaceType : interfaceTypes) {
			// Create the definition type
			AppAndComponent appAndModule = ServicesPackages.appAndModuleFromServiceInterfaceType(interfaceType);
			ServicesImpl configuredDefaultProxy = _coreAppAndModulesDefaultProxy.get(appAndModule);
//			if (configuredDefaultProxy == null) {
//				log.error("Cannot bind {}: NO proxy config defined for {}. Please check that {}.client.properties.xml and {}.client.properties.xml files are properly configured for {}",
//						  interfaceType,appAndModule,_apiAppCode,_apiAppCode,appAndModule);
//				continue;
//			}
			ServiceToImplAndProxyDef<? extends ServiceInterface> serviceToImplDef = ServiceToImplAndProxyDef.createFor(appAndModule,
																									   interfaceType,
																									   configuredDefaultProxy);
			
			// Filter the proxies and get the ones suitables for the service interface (many proxy impls might be suitable)
			Collection<Class<? extends ServiceProxyImpl>> proxyTypeForServiceInterface = FluentIterable.from(proxyTypes)
																									   .filter(new Predicate<Class<? extends ServiceProxyImpl>>() {
																														@Override
																														public boolean apply(final Class<? extends ServiceProxyImpl> proxyType) {
																															return ReflectionUtils.isImplementing(proxyType,
																																								  interfaceType);
																														}
																									  		  })
																									   .toSet();
			if (CollectionUtils.hasData(proxyTypeForServiceInterface)) {
				for (Class<? extends ServiceProxyImpl> proxyType : proxyTypeForServiceInterface) {
					serviceToImplDef.putProxyImplType(proxyType);
				}
			}
			// Filter the impls and get the one suitable for the service interface (only ONE should be suitable)
			Collection<Class<? extends ServiceInterface>> serviceImplTypes = FluentIterable.from(implTypes)
																						   .filter(new Predicate<Class<? extends ServiceInterface>>() {
																											@Override
																											public boolean apply(final Class<? extends ServiceInterface> implType) {																												
																												// check directly implemented interfaces...
																												Class<?>[] implTypeInterfaces = implType.getInterfaces();
																												boolean isImplementing = false;
																												if (CollectionUtils.hasData(implTypeInterfaces)) {
																													for (Class<?> implTypeInterface : implTypeInterfaces) {
																														if (implTypeInterface == interfaceType) {
																															isImplementing = true;
																															break;
																														}
																													}
																												} 
//																												System.out.println("---->" + implType + " > " + isImplementing + " > " + interfaceType);
																												return isImplementing;
																											}
																							  	   })
																							 .toSet();
			if (CollectionUtils.hasData(serviceImplTypes)) {
				Class<? extends ServiceInterface> serviceImplType = CollectionUtils.of(serviceImplTypes)
																			   	   .pickOneAndOnlyElement("There's more than a single implementation for service {}: {}",
																									  	  interfaceType,serviceImplTypes);
				serviceToImplDef.setServiceBeanImpl(serviceImplType);
			}
			// Add to the output
			Set<ServiceToImplAndProxyDef<? extends ServiceInterface>> defsForAppModule = outServiceInterfacesToImpls.get(serviceToImplDef.getCoreAppAndModule());
			if (defsForAppModule == null) {
				defsForAppModule = Sets.newHashSet();
				outServiceInterfacesToImpls.put(serviceToImplDef.getCoreAppAndModule(),
												defsForAppModule);
			}
			defsForAppModule.add(serviceToImplDef);
		}
		
		// A bit of debug
		if (CollectionUtils.hasData(outServiceInterfacesToImpls)) {
			for (Map.Entry<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> me : outServiceInterfacesToImpls.entrySet()) {
				log.warn("\t\t-{}: {} matchings",
						 me.getKey(),
						 (me.getValue() != null ? me.getValue().size() : 0));
			}
		}
		
		return outServiceInterfacesToImpls;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	private static class ServiceInterfaceImplementingTypes
		  		 extends HashSet<Class<? extends ServiceInterface>> {
		private static final long serialVersionUID = 1764833596552304436L;
		
		@Getter private final AppCode _apiAppCode;
		@Getter private final Collection<AppCode> _coreAppCodes;
		
		@Getter private final Memoized<Set<Class<? extends ServiceInterface>>> _interfaceTypes = new Memoized<Set<Class<? extends ServiceInterface>>>() {
																									@Override
																									protected Set<Class<? extends ServiceInterface>> supply() {
																										return _getInterfaceTypes();
																									}
																				   		  };
		@Getter private final Memoized<Set<Class<? extends ServiceProxyImpl>>> _proxyTypes = new Memoized<Set<Class<? extends ServiceProxyImpl>>>() {
																									@Override
																									protected Set<Class<? extends ServiceProxyImpl>> supply() {
																										return _getProxyTypes();
																									}
																				   		  };
		@Getter private final Memoized<Map<AppCode,Set<Class<? extends ServiceInterface>>>> _implementationTypesByCoreAppCode = new Memoized<Map<AppCode,Set<Class<? extends ServiceInterface>>>>() {
																																		@Override
																																		protected Map<AppCode,Set<Class<? extends ServiceInterface>>> supply() {
																																			return _getImplementationTypesByCoreAppCode();
																																		}
																				   		  	  									};
		@Getter private final Memoized<Set<Class<? extends ServiceInterface>>> _implementationTypes = new Memoized<Set<Class<? extends ServiceInterface>>>() {
																											@Override
																											protected Set<Class<? extends ServiceInterface>> supply() {
																												Map<AppCode,Set<Class<? extends ServiceInterface>>> byAppCode = _getImplementationTypesByCoreAppCode();
																												Set<Class<? extends ServiceInterface>> outImpls = Sets.newHashSet();
																												if (CollectionUtils.hasData(byAppCode)) {
																													for (Set<Class<? extends ServiceInterface>> appImpls : byAppCode.values()) {
																														outImpls.addAll(appImpls);
																													}
																												}
																												return outImpls;
																											}
																				   		  	  		  };
						
		public ServiceInterfaceImplementingTypes(final AppCode apiAppCode,
												 final Collection<AppCode> coreAppCodes) {
			_apiAppCode = apiAppCode;
			_coreAppCodes = coreAppCodes;
			
			// Find all service interface implementations...
			List<String> pckgs = Lists.newLinkedList();		
			
			// Service interfaces
			pckgs.add(ServiceInterface.class.getPackage().getName());			// service interfaces
			pckgs.add(ServicesPackages.serviceInterfacePackage(apiAppCode));	// xx.api.interfaces...
			
			// Proxies
			pckgs.add(ServiceProxyImpl.class.getPackage().getName());
			pckgs.add(ServicesPackages.serviceProxyPackage(apiAppCode));		// xxx.client.servicesproxy.(bean|rest|ejb...)
			
			// Core implementations
			if (CollectionUtils.hasData(coreAppCodes)) {
				for (AppCode coreAppCode : coreAppCodes) {
					pckgs.add(ServicesPackages.servicesCorePackage(coreAppCode));	// impls
				}
			}
			
			// do find
			Set<Class<? extends ServiceInterface>> serviceInterfaceImplementingTypes = ServicesPackages.findSubTypesAt(ServiceInterface.class,
																										   			   pckgs,
																										   			   this.getClass().getClassLoader());
	    	if (CollectionUtils.hasData(serviceInterfaceImplementingTypes)) {
	    		for (Class<? extends ServiceInterface> serviceInterfaceType : serviceInterfaceImplementingTypes) {
	    			this.add(serviceInterfaceType);
	    		}
	    	}
		}
		
		private Set<Class<? extends ServiceInterface>> _getInterfaceTypes() {
			return FluentIterable.from(this)
								 .filter(new Predicate<Class<? extends ServiceInterface>>() {
												@Override
												public boolean apply(final Class<? extends ServiceInterface> type) {
													// A ServiceInterface MUST:
													//		a) be an interface at service interfaces package
													//		b) is annotated with @ServiceInterfaceFor
													
													// a) check that is an interface at service interfaces package
													boolean canBeServiceInterface = type.getPackage().getName().startsWith(ServicesPackages.serviceInterfacePackage(_apiAppCode))	// it's a service interface
																				&&  ReflectionUtils.isInterface(type);																// it's NOT instanciable
													
													// b) check that directly extends ServiceInterface
													boolean isAnnotated = ReflectionUtils.typeAnnotation(type,ServiceInterfaceFor.class) != null;
													if (!isAnnotated) log.info("{} is NOT considered as a {} because is not annotated with @{}",
																			   type,ServiceInterface.class.getSimpleName(),ServiceInterfaceFor.class.getSimpleName());
													 
													boolean isServiceInterface = canBeServiceInterface & isAnnotated;
													return isServiceInterface;
												}
								 		 })
								 .toSet();
		}
		private Set<Class<? extends ServiceProxyImpl>> _getProxyTypes() {
			return FluentIterable.from(this)
								 .filter(new Predicate<Class<? extends ServiceInterface>>() {
												@Override
												public boolean apply(final Class<? extends ServiceInterface> type) {
													return type.getPackage().getName().startsWith(ServicesPackages.serviceProxyPackage(_apiAppCode))		// it's a service proxy
													    && ReflectionUtils.isImplementing(type,ServiceProxyImpl.class)										// it's a service proxy impl
													    && ReflectionUtils.isInstanciable(type);															// it's instanciable
												}
								 		 })
								 .transform(new Function<Class<? extends ServiceInterface>,Class<? extends ServiceProxyImpl>>() {
													@Override @SuppressWarnings("unchecked")
													public Class<? extends ServiceProxyImpl> apply(final Class<? extends ServiceInterface> type) {
														return (Class<? extends ServiceProxyImpl>)type;		// proxies MUST implement service interface!
													}
								 			})
								 .toSet();
		}
		private Map<AppCode,Set<Class<? extends ServiceInterface>>> _getImplementationTypesByCoreAppCode() {
			Map<AppCode,Set<Class<? extends ServiceInterface>>> outImplsByCore = Maps.newHashMapWithExpectedSize(_coreAppCodes.size());
			for (final AppCode coreAppCode : _coreAppCodes) {
				Set<Class<? extends ServiceInterface>> impls = null;
				impls = FluentIterable.from(this)
									  .filter(new Predicate<Class<? extends ServiceInterface>>() {
													@Override
													public boolean apply(final Class<? extends ServiceInterface> type) {
														String servicesCorePackage = ServicesPackages.servicesCorePackage(coreAppCode);
														String servicesDelegatesPackage = servicesCorePackage + ".delegate";
														
														boolean isImpl = type.getPackage().getName().startsWith(servicesCorePackage)		// it's a service implementation
																	  && !type.getPackage().getName().startsWith(servicesDelegatesPackage)	// it's NOT a delegate
																	  && ReflectionUtils.isInstanciable(type);								// it's instanciable
														return isImpl;
													}
									 		 })
									  .toSet();
				if (CollectionUtils.hasData(impls)) outImplsByCore.put(coreAppCode,impls);
			}
			return outImplsByCore;
		}
	}
}
