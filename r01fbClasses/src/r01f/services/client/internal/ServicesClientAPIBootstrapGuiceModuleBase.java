package r01f.services.client.internal;

import java.util.Collection;

import org.aopalliance.intercept.MethodInterceptor;

import com.google.inject.Binder;
import com.google.inject.PrivateBinder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.services.ServicesMainGuiceBootstrap;
import r01f.services.client.ClientAPI;
import r01f.services.client.ServiceProxiesAggregator;
import r01f.services.core.internal.ServicesCoreForAppModulePrivateGuiceModule;
import r01f.services.interfaces.ServiceInterface;
import r01f.usercontext.UserContext;

/**
 * This GUICE module is where the client-api bindings takes place
 * 
 * This guice module is included from the bootstrap module: {@link ServicesMainGuiceBootstrap} (which is called from the client injector holder like R01MInjector) 
 * 
 * At this module some client-side bindings are done:
 * <ol>
 * 		<li>Client APIs: types that aggregates the services access</li>
 * 		<li>Model object extensions</li>
 * 		<li>Server services proxies (ie: REST, bean, ejb)</li>
 * </ol>
 * 
 * The execution flow is something like:
 * <pre>
 * ClientAPI
 *    |----> ServicesClientProxy
 * 						|---------------[ Proxy between client and server services ] 
 * 														  |
 * 														  |----- [ HTTP / RMI / Direct Bean access ]-------->[REAL server / core side Services implementation] 
 * </pre>
 * 
 * The API simply offers access to service methods to the client and frees him from the buzz of knowing how to deal with various service implementations
 * (REST, EJB, Bean...). 
 * All the logic related to transforming client method-calls to core services method calls is done at the PROXIES. There's one proxy per core service implementation
 * (REST, EJB, Bean...) 
 * 
 * <b>See file services-architecture.txt :: there is an schema of the app high level architecture</b>
 * </pre>
 */
@Slf4j
@Accessors(prefix="_")
@EqualsAndHashCode				// This is important for guice modules
@RequiredArgsConstructor
public abstract class ServicesClientAPIBootstrapGuiceModuleBase
		   implements ServicesClientGuiceModule {	// this is a client guice bindings module
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * API app code
	 */
	private final AppCode _apiAppCode;
	/**
	 * Service interface type to bean impl or rest / ejb, etc proxy matchings (bindings) 
	 * This type instance has a Map member for every core appCode / module which key is the service interface type and the value is the
	 * concrete instance of the service interface bean impl or proxy to be used
	 * 		- if the service bean implementation is available, the service interface is binded to the bean impl directly
	 *		- otherwise, the best suitable proxy to the service implementation is binded
	 * Those Map member are {@link MapBinder}s injected at {@link ServicesCoreForAppModulePrivateGuiceModule}{@link #_bindServiceProxiesAggregators(Binder)} method
	 * 
	 * Since there's a {@link ServicesCoreForAppModulePrivateGuiceModule} private module for every core appCode / module,
	 * this type has a Map member for every core appCode / module
	 */
	private final ServiceInterfaceTypesToImplOrProxyMappings _serviceInterfaceTypesToImplOrProxyMappings;
	/**
	 * The core app and modules
	 * (is set at bootstraping time at {@link ServicesMainGuiceBootstrap})
	 */
	@Setter private Collection<AppAndComponent> _coreAppAndModules;
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void configure(final Binder binder) {		
		Binder theBinder = binder;
		
		// Find the model object types
		ModelObjectTypeMetaDataBuilder.init(_apiAppCode);
		
		// Other module-specific bindings
		_configure(theBinder);
		
		// [1] - Bind the Services proxy aggregator types as singletons
		//		 The services proxy aggregator instance contains fields for every fine-grained service proxy
		// 		 which are lazily created when accessed (see bindings at [1])
		_bindServiceProxiesAggregators(theBinder);
		
		// [2] - Bind the client API aggregator types as singletons
		//		 The ClientAPI is injected with a service proxy aggregator defined at [2]
		Collection<Class<? extends ClientAPI>> clientAPITypes = _bindAPIAggregatorImpls(theBinder);
	}
	@Provides 
	UserContext provideUserContext() {
		return _provideUserContext();
	}
	/**
	 * Module configurations: marshaller and other bindings
	 * @param binder
	 */
	protected abstract void _configure(final Binder binder);
	/**
	 * Provides an user context
	 */
	protected abstract <U extends UserContext> U _provideUserContext();
/////////////////////////////////////////////////////////////////////////////////////////
//  API Aggregator
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Binds the client API aggregator that provides convenient access to the services
	 * To do so, the API in turn uses a {@link ServiceProxiesAggregator} instance 
	 * references to {@link ServiceInterface} implementations that are on-demand injected at {@link ServicesClientProxyLazyLoaderGuiceMethodInterceptor} 
	 * @param binder
	 */
	private Collection<Class<? extends ClientAPI>> _bindAPIAggregatorImpls(final Binder binder) {
		ServicesClientAPIFinder clientAPIFinder = new ServicesClientAPIFinder(_apiAppCode);
		Collection<Class<? extends ClientAPI>> clientAPITypes = clientAPIFinder.findClientAPIs();
		log.warn("==================================================");
		log.warn("[Bind ClientAPIs]: {} instances",clientAPITypes.size());
		log.warn("==================================================");
		for (Class<? extends ClientAPI> clientAPIType : clientAPITypes) {
			log.warn("\tClientAPI > {} ",clientAPIType);
			binder.bind(clientAPIType)
				  .in(Singleton.class);
		}
		return clientAPITypes;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SERVICES PROXY
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Binds the {@link ServiceProxiesAggregator} that MUST contain fields of types implementing {@link ServiceInterface} which are 
	 * the concrete proxy implementation to the services
	 * 
	 * The {@link ServiceInterface} fields of {@link ServiceProxiesAggregator} implementing type are LAZY loaded by 
	 * {@link ServicesClientProxyLazyLoaderGuiceMethodInterceptor} which guesses what proxy implementation assign to the field:
	 * <ul>
	 * 		<li>If the {@link ServiceProxiesAggregator} extends {@link ServiceProxiesAggregatorForDefaultImpls}, the concrete {@link ServiceInterface}-implementing 
	 * 			proxy instance is taken from the client properties XML file, so some service impls might be accessed using a BEAN proxy while others might be accessed
	 * 			using a REST proxy -depending on the properties file-</li>
	 * 		<li>If the {@link ServiceInterface} field's BEAN implementation is available this one will be assigned to the field no matter what type the aggregator is</li>
	 * </ul>
	 * @param binder
	 */
	private void _bindServiceProxiesAggregators(final Binder binder) {
		// Inject all Map fields that matches the service interface types with the bean impl or proxy to be used
		// (this Map fields are injected by MapBinders created at ServicesForAppModulePrivateGuiceModule)									
		binder.requestInjection(_serviceInterfaceTypesToImplOrProxyMappings);
		
		// Create a private binder to be used to inject the MethodInterceptor that will intercept all fine-grained
		// proxy accessor method calls at ServicesAggregatorClientProxy 
		// The interceptor lazily loads the fine-grained proxy instances and makes the aggregator creation simpler
		PrivateBinder privateBinder = binder.newPrivateBinder();
		privateBinder.bind(ServiceInterfaceTypesToImplOrProxyMappings.class)
			  		 .toInstance(_serviceInterfaceTypesToImplOrProxyMappings);
		MethodInterceptor serviceProxyGetterInterceptor = new ServicesClientProxyLazyLoaderGuiceMethodInterceptor(_apiAppCode,
																												  _coreAppAndModules);
		privateBinder.requestInjection(serviceProxyGetterInterceptor);		// the method interceptor is feeded with a map of service interfaces to bean impl or proxy created below
		
		// Bind the interceptor to ServiceProxiesAggregator type's fine-grained method calls
		binder.bindInterceptor(Matchers.subclassesOf(ServiceProxiesAggregator.class),
							   Matchers.any(),
							   serviceProxyGetterInterceptor);
		
		// Bind every services proxy aggregator implementation
		ServicesClientAPIFinder clientProxyAggregatorFinder = new ServicesClientAPIFinder(_apiAppCode);
		Collection<Class<? extends ServiceProxiesAggregator>> proxyAggregatorTypes = clientProxyAggregatorFinder.findClientAPIProxyAggregatorTypes();
		log.info("[ServiceProxyAggregator] > {} implementations",proxyAggregatorTypes.size());
		
		for (Class<? extends ServiceProxiesAggregator> proxyAggregatorType : proxyAggregatorTypes) {
			log.info("\t\t- {}",proxyAggregatorType);
			binder.bind(proxyAggregatorType)
			      .in(Singleton.class);
		}
	}
}
 