package r01f.services;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import r01f.exceptions.Throwables;
import r01f.guids.AppAndComponent;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.patterns.Memoized;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.ClientAPI;
import r01f.services.client.ClientAPIForBeanServices;
import r01f.services.client.ClientAPIForDefaultServices;
import r01f.services.client.ClientAPIForEJBServices;
import r01f.services.client.ClientAPIForRESTServices;
import r01f.services.client.ServiceProxiesAggregatorImpl;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceInterfaceFor;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Slf4j
public class ServicesFinderHelper {
/////////////////////////////////////////////////////////////////////////////////////////
//  CLIENT API AGGREGATORS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a {@link Set} that contains {@link ClientAPIDef} objects that relates the {@link ClientAPI} interfaces 
	 * with it's implementations ({@link ClientAPIForBeanServices}, {@link ClientAPIForEJBServices}, {@link ClientAPIForRESTServices}, etc)
	 * and also the default impl ({@link ClientAPIForDefaultServices}) specified at the [apiAppCode].client.properties.xml
	 * @param apiAppCode
	 * @return
	 */
	public static Set<ClientAPIDef<? extends ClientAPI>> clientFindAPIInterfacesToImpls(final AppCode apiAppCode) {
		// Find all ClientAPI interface subtypes
		//		- some will be interfaces
		//		- other will be implementations, either ClientAPIForBeanServices, ClientAPIForRESTServices, ClientAPIForEJBServices, etc 
		String apiAggregatorPackage = ServicesFinderHelper.apiAggregatorPackage(apiAppCode);
		Reflections ref = new Reflections(apiAggregatorPackage);
		Collection<Class<? extends ClientAPI>> clientAPITypes = ref.getSubTypesOf(ClientAPI.class);
		
		if (CollectionUtils.isNullOrEmpty(clientAPITypes)) throw new IllegalStateException(Throwables.message("NO {} was found at {}",ClientAPI.class,apiAggregatorPackage));
		
		// Process the found types
		final Set<ClientAPIDef<? extends ClientAPI>> outAPIInterfacesToImpls = Sets.newHashSet();
		if (CollectionUtils.hasData(clientAPITypes)) {
			// Two iterations over the found ClientAPIs collection
			// 		a) the first to find the ClientAPI interfaces
			//		b) the second to bind the ClientAPI implementations: ClientAPIForBeanServices, ClientAPIForRESTServices, ClientAPIForEJBServices, etc
			
			// a) find the ClientAPI interfaces
			for (Class<? extends ClientAPI> foundClientAPIType : clientAPITypes) {
				boolean isInterface = ReflectionUtils.isInterface(foundClientAPIType);
				if (isInterface) outAPIInterfacesToImpls.add(ClientAPIDef.createFor(foundClientAPIType));
			}
			// b) find the ClientAPI implementations: ClientAPIForBeanServices, ClientAPIForRESTServices, ClientAPIForEJBServices, etc
			for (final Class<? extends ClientAPI> foundClientAPIType : clientAPITypes) {
				boolean isInterfaceOrAbstract = ReflectionUtils.isInterface(foundClientAPIType) || ReflectionUtils.isAbstract(foundClientAPIType);
				if (isInterfaceOrAbstract) continue;	// skip interfaces
				
				// Find the ClientAPI interface for the impl (it has to be previously put at the Map in (a))
				Collection<ClientAPIDef<? extends ClientAPI>> apiDefs = FluentIterable.from(outAPIInterfacesToImpls)
																				      .filter(new Predicate<ClientAPIDef<? extends ClientAPI>>() {
																									@Override
																									public boolean apply(final ClientAPIDef<? extends ClientAPI> apiDef) {
																										return ReflectionUtils.isImplementing(foundClientAPIType,
																																			  apiDef.getApiInterfaceType());
																									}
																				  		  })
																				      .toSet();
				if (apiDefs.size() == 0) throw new IllegalStateException(Throwables.message("There's NO interface for client API impl {}",foundClientAPIType));
				if (apiDefs.size() > 1) throw new IllegalStateException(Throwables.message("There's more than one client API interface for impl {}",foundClientAPIType));
				
				// Put the API impl at the object that relates all impls to it's interface
				CollectionUtils.of(apiDefs)
							   .pickOneAndOnlyElement()
									   .putAPIImplType(foundClientAPIType);
				
			}
			// Check that the default impl exists for every api and a little bit of logging
			for (ClientAPIDef<? extends ClientAPI> apiDef : outAPIInterfacesToImpls) {
				if (apiDef.getAPIImplTypeFor(ServicesImpl.Default) == null) throw new IllegalStateException(Throwables.message("There's NO {} impl for {} client api interface: there MUST exist a type implementing {} at package {}",
																														       ServicesImpl.Default,apiDef.getApiInterfaceType(),
																														       ClientAPIForDefaultServices.class,apiAggregatorPackage));
			}
			
		}
		if (CollectionUtils.isNullOrEmpty(outAPIInterfacesToImpls)) throw new IllegalStateException(Throwables.message("There's NO {} at {} app: there MUST exist types implementing {}, {}, {}... at package {}",
																													   ClientAPI.class,apiAppCode,
																													   ClientAPIForDefaultServices.class,ClientAPIForRESTServices.class,ClientAPIForBeanServices.class,apiAggregatorPackage));
		return outAPIInterfacesToImpls;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROXY AGGREGATORS
/////////////////////////////////////////////////////////////////////////////////////////
	public static Set<Class<? extends ServiceProxiesAggregatorImpl>> clientFindServiceProxyAggregatorTypes(final AppCode apiAppCode) {
		// Find all ServiceProxiesAggregatorImpl interface subtypes
		List<URL> serviceInterfaceUrls = new ArrayList<URL>();
		serviceInterfaceUrls.addAll(ClasspathHelper.forPackage(ServicesFinderHelper.serviceProxyPackage(apiAppCode)));	// xxx.client.servicesproxy
		serviceInterfaceUrls.addAll(ClasspathHelper.forPackage(ServiceProxiesAggregatorImpl.class.getPackage().getName()));
		Reflections serviceProxyAggregatorTypesScanner = new Reflections(new ConfigurationBuilder()					// Reflections library NEEDS to have both the interface containing package and the implementation containing package
																				.setUrls(serviceInterfaceUrls)		// see https://code.google.com/p/reflections/issues/detail?id=53
																				.setScanners(new SubTypesScanner(true)));
		Collection<Class<? extends ServiceProxiesAggregatorImpl>> proxyImplTypes = serviceProxyAggregatorTypesScanner.getSubTypesOf(ServiceProxiesAggregatorImpl.class);
		
		if (CollectionUtils.isNullOrEmpty(proxyImplTypes)) throw new IllegalStateException(Throwables.message("NO {} was found at package {}",ServiceProxiesAggregatorImpl.class,ServicesFinderHelper.serviceProxyPackage(apiAppCode)));
				
		return FluentIterable.from(proxyImplTypes)
							 .filter(new Predicate<Class<? extends ServiceProxiesAggregatorImpl>>() {
											@Override
											public boolean apply(final Class<? extends ServiceProxiesAggregatorImpl> proxyAggregatorImplType) {
												return !ReflectionUtils.isInterface(proxyAggregatorImplType);	// ignore interfaces
											}
							 		 })
							 .toSet();
	}
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
	 * @param apiAppCode
	 * @param coreAppCode
	 * @return
	 */
	public static Set<ServiceToImplDef<? extends ServiceInterface>> findServiceInterfacesToImpls(final AppCode apiAppCode,
																								 final AppCode coreAppCode) {
		return ServicesFinderHelper.findServiceInterfacesToImpls(apiAppCode,
																 Sets.newHashSet(coreAppCode));
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
	public static Set<ServiceToImplDef<? extends ServiceInterface>> findServiceInterfacesToImpls(final AppCode apiAppCode,
																								 final Collection<AppCode> coreAppCodes) {
		// Find all the ServiceInterface implementing types
		ServiceInterfaceImplementingTypes serviceInterfaceImplementingTypes = new ServiceInterfaceImplementingTypes(apiAppCode,
																													coreAppCodes);
		
		// ... within all filter all service interfaces
		Collection<Class<? extends ServiceInterface>> interfaceTypes = serviceInterfaceImplementingTypes.getInterfaceTypes().get();

		// ... within all filter all the proxies 
		Collection<Class<? extends ServiceProxyImpl>> proxyTypes = serviceInterfaceImplementingTypes.getProxyTypes().get();
		
		// ... within all filter all the implementations (the beans that implements the service interfaces) 
		// 	   note that NOT all the service interface implementations will be available on the classpath
		Collection<Class<? extends ServiceInterface>> implTypes = serviceInterfaceImplementingTypes.getImplementationTypes().get();
		
		// Correlate the service interface with it's proxies and implementation (where available)
		Set<ServiceToImplDef<? extends ServiceInterface>> outServiceInterfacesToImpls = Sets.newHashSetWithExpectedSize(interfaceTypes.size());
		for (final Class<? extends ServiceInterface> interfaceType : interfaceTypes) {
			// Create the definition type
			ServiceToImplDef<? extends ServiceInterface> serviceToImplDef = ServiceToImplDef.createFor(interfaceType);
			
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
				serviceToImplDef.setImplementationType(serviceImplType);
			}
			// Add to the output
			outServiceInterfacesToImpls.add(serviceToImplDef);
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
			log.warn("Finding {}-implementing types at [{}] client app code and {} core app codes",
					 ServiceInterface.class.getSimpleName(),apiAppCode,coreAppCodes);
			_apiAppCode = apiAppCode;
			_coreAppCodes = coreAppCodes;
			
			// Find all service interface implementations...
			List<URL> urls = new ArrayList<URL>();			
			
			// Service interfaces
			urls.addAll(ClasspathHelper.forPackage(ServiceInterface.class.getPackage().getName()));				// service interfaces
			urls.addAll(ClasspathHelper.forPackage(ServicesFinderHelper.serviceInterfacePackage(apiAppCode)));	// xx.api.interfaces... 
			
			// Proxies
			urls.addAll(ClasspathHelper.forPackage(ServiceProxyImpl.class.getPackage().getName()));
			urls.addAll(ClasspathHelper.forPackage(ServicesFinderHelper.serviceProxyPackage(apiAppCode)));		// xxx.client.servicesproxy.(bean|rest|ejb...)
			
			// Core implementations
			if (CollectionUtils.hasData(coreAppCodes)) {
				for (AppCode coreAppCode : coreAppCodes) {
					urls.addAll(ClasspathHelper.forPackage(ServicesFinderHelper.servicesCorePackage(coreAppCode)));		// impls
				}
			}
			
			// do find
			Reflections servicesInterfaceScanner = new Reflections(new ConfigurationBuilder()	// Reflections library NEEDS to have both the interface containing package and the implementation containing package
																			.setUrls(urls)		// see https://code.google.com/p/reflections/issues/detail?id=53
																			.setScanners(new SubTypesScanner()));		
	    	Set<Class<? extends ServiceInterface>> serviceInterfaceImplementingTypes = servicesInterfaceScanner.getSubTypesOf(ServiceInterface.class);
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
													boolean canBeServiceInterface = type.getPackage().getName().startsWith(ServicesFinderHelper.serviceInterfacePackage(_apiAppCode))	// it's a service interface
																				&&  ReflectionUtils.isInterface(type);																	// it's NOT instanciable
													
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
													return type.getPackage().getName().startsWith(ServicesFinderHelper.serviceProxyPackage(_apiAppCode))		// it's a service proxy
													    && ReflectionUtils.isImplementing(type,ServiceProxyImpl.class)											// it's a service proxy impl
													    && ReflectionUtils.isInstanciable(type);																// it's instanciable
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
														String servicesCorePackage = ServicesFinderHelper.servicesCorePackage(coreAppCode);
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
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds the service proxy impl configured at [clientAppCode].client.properties.xml 
	 * for a provided service interface type
	 * @param serviceInterfaceType
	 * @param clientProps
	 * @return
	 */
	public static ServicesImpl configuredServiceProxyImplFor(final Class<? extends ServiceInterface> serviceInterfaceType,
												   			 final XMLPropertiesForAppComponent clientProps) {
		AppAndComponent appAndComponent = _appAndComponentFor(serviceInterfaceType);
		
		String propsXPath = Strings.of("/client/proxies/proxy[@appCode='{}' and @id='{}']/@impl")
								   .customizeWith(appAndComponent.getAppCode(),
										   		  appAndComponent.getAppComponent())
								   .asString();
		ServicesImpl configuredImpl = clientProps.propertyAt(propsXPath)
												 .asEnumElement(ServicesImpl.class);
		if (configuredImpl == null) { 
			log.warn("NO proxy impl for appCode={} and module={} configured at {}.client.properties.xml, {} is used by default",
					 appAndComponent.getAppCode(),appAndComponent.getAppComponent(),appAndComponent.getAppCode(),ServicesImpl.REST);
			configuredImpl = ServicesImpl.REST;
		}
		return configuredImpl;
	}
	/**
	 * Finds the {@link AppCode} and {@link AppComponent} for a {@link ServiceInterface} 
	 * To do so it looks for the {@link ServiceInterfaceFor} annotation that contains the {@link AppCode} and {@link AppComponent}
	 * @param serviceInterfaceType
	 * @return
	 */
	private static AppAndComponent _appAndComponentFor(final Class<? extends ServiceInterface> serviceInterfaceType) {
		// guess the appCode & module (ServiceInterface MUST be annotated with @ServiceInterfaceFor(appCode=xxx,module=yyy)
		ServiceInterfaceFor serviceInterfaceAnnot = ReflectionUtils.typeAnnotation(serviceInterfaceType,
															   					   ServiceInterfaceFor.class);
		if (serviceInterfaceAnnot == null 
		 || Strings.isNullOrEmpty(serviceInterfaceAnnot.appCode()) 
		 || Strings.isNullOrEmpty(serviceInterfaceAnnot.module())) throw new IllegalStateException(Throwables.message("{} service interface type MUST be annotated with @{}(appCode=xxx,module=yyy",
																							  						  serviceInterfaceType,ServiceInterfaceFor.class.getSimpleName()));
				
		AppCode coreAppCode = AppCode.forId(serviceInterfaceAnnot.appCode());
		AppComponent coreAppComponent = AppComponent.forId(serviceInterfaceAnnot.module());
		return AppAndComponent.composedBy(coreAppCode,coreAppComponent);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static String serviceInterfacePackage(final AppCode apiAppCode) {
		return Strings.of("{}.api.interfaces")
					  .customizeWith(apiAppCode)
					  .asString();
	}
	public static String serviceProxyPackage(final AppCode apiAppCode) {
		return Strings.of("{}.client.servicesproxy")
					  .customizeWith(apiAppCode)
					  .asString();
	}
	public static String apiAggregatorPackage(final AppCode apiAppCode) {
		return Strings.of("{}.client.api")
					  .customizeWith(apiAppCode)
					  .asString();
	}
	public static String servicesCorePackage(final AppCode coreAppCode) {
		return Strings.of("{}.services")
					  .customizeWith(coreAppCode)
					  .asString();
	}
}
