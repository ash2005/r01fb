package r01f.services.client.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.aopalliance.intercept.MethodInterceptor;
import org.w3c.dom.Node;

import r01f.exceptions.Throwables;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.services.ClientAPIDef;
import r01f.services.ServiceToImplDef;
import r01f.services.ServicesFinderHelper;
import r01f.services.ServicesGuiceBootstrap;
import r01f.services.ServicesImpl;
import r01f.services.client.ClientAPI;
import r01f.services.client.ClientUsesBeanServices;
import r01f.services.client.ServiceProxiesAggregator;
import r01f.services.client.ServiceProxiesAggregatorForBeanImpls;
import r01f.services.client.ServiceProxiesAggregatorForDefaultImpls;
import r01f.services.client.ServiceProxiesAggregatorForEJBImpls;
import r01f.services.client.ServiceProxiesAggregatorForRESTImpls;
import r01f.services.client.ServiceProxiesAggregatorImpl;
import r01f.services.core.ServicesCoreImplementation;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.services.interfaces.ProxyForRESTImplementedService;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.usercontext.UserContext;
import r01f.util.types.collections.CollectionUtils;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01f.xmlproperties.XMLPropertiesGuiceModule;

import com.google.common.base.Function;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.MapBinder;

/**
 * This GUICE module is where the client-api bindings takes place
 * 
 * This guice module is included from the bootstrap module: {@link ServicesGuiceBootstrap} (which is called from the client injector holder like R01MInjector) 
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
 * 						|---------------[ Proxy between client and server services ] -----> SERVER (could be REST, as simple java bean, etc)
 * 														  |
 * 														  |----- [ HTTP / RMI / Direct Bean access ]-------->[REAL server / core side Services implementation] 
 * </pre>
 * 
 * The API simply offers access to service methods to the client and frees him from the buzz of knowing how to deal with various service implementations
 * (REST, EJB, Bean...). 
 * All the logic related to transforming client method-calls to core services method calls is done at the PROXIES. There's one proxy per core service implementation
 * (REST, EJB, Bean...) 
 * 
 * <b>See file resources/services-architecture.txt :: there is an schema of the app high level architecture</b>
 * </pre>
 */
@Slf4j
@EqualsAndHashCode				// This is important for guice modules
public abstract class ServicesClientBootstrapGuiceModule
		   implements ServicesClientGuiceModule {	// this is a client guice bindings module
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * API app code
	 */
	private final AppCode _apiApp;
	/**
	 * Core apps codes
	 */
	private final Collection<AppCode> _coreApps;
	/**
	 * Api properties
	 */
	private final XMLPropertiesForApp _apiProps;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesClientBootstrapGuiceModule(final AppCode apiAppCode) {
		_apiApp = apiAppCode;
		_apiProps = Guice.createInjector(new XMLPropertiesGuiceModule())
					  	  .getInstance(XMLProperties.class)
					  	  .forApp(_apiApp);		
		_coreApps = _apiProps.forComponent("core")
		  			    	 .propertyAt("/core/modules")
		  			    	 .asObjectList(new Function<Node,AppCode>() {
													@Override
													public AppCode apply(final Node node) {
														return AppCode.forId(XMLUtils.nodeAttributeValue(node,"appCode"));
													}
			 					          });
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void configure(final Binder binder) {
		// Find the model object types
		ModelObjectTypeMetaDataBuilder.init(_apiApp);
		
		// Marshalling and other bindings
		_configure(binder);
		
		// ::: lowest level to higher
		//			[1] ServiceInterface --> [2] ServicesAggregatorClientProxy --> [3] ClientAPI
		
		// [1] - Bind the ServiceInterface proxies
		//		 (the fine-grained services proxies)
		_bindServiceInterfacesToProxiesOrImpls(binder,
											   _apiApp,
											   _coreApps);
		
		// [2] - Bind the Services proxy aggregator types as singletons
		//		 The services proxy aggregator instance contains fields for every fine-grained service proxy
		// 		 which are lazily created when accessed (see bindings at [1])
		_bindServiceProxiesAggregators(binder);
		
		// [3] - Bind the client API aggregator types as singletons
		//		 The ClientAPI is injected with a service proxy aggregator defined at [2]
		_bindAPIAggregatorImpls(binder);
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
	 * Binds the different API aggregators (REST, Bean, EJB, etc) that provides convenient access to the services
	 * To do so, the API in turn uses a {@link ServiceProxiesAggregator} instance for the concrete proxy type (REST, Bean, EJB, etc) which contains 
	 * references to {@link ServiceInterface} implementations that are on-demand injected at {@link ServicesClientProxyLazyLoaderGuiceMethodInterceptor} 
	 * @param binder
	 */
	private void _bindAPIAggregatorImpls(final Binder binder) {
		// Create a collection of ClientAPIDef objects that relates the ClientAPI interfaces to it's implementations 
		// (ClientAPIForBeanServices, ClientAPIForRESTServices, ClientAPIForEJBServices, etc)
		Set<ClientAPIDef<? extends ClientAPI>> apiInterfacesToImpls = ServicesFinderHelper.clientFindAPIInterfacesToImpls(_apiApp);
		log.warn("==================================================");
		log.warn("[Bind ClientAPI aggregators]: {} instances",apiInterfacesToImpls.size());
		log.warn("==================================================");
		for (ClientAPIDef<? extends ClientAPI> apiDef : apiInterfacesToImpls) {
			log.warn("\tClientAPI Aggregator > {} ({} implementations)",apiDef.getApiInterfaceType(),apiDef.getApiImplTypesByServiceImpl().size());
			_bindAPIAggregatorImpls2(binder,
									 apiDef);
		}
	}
	/**
	 * Captures java generics to do the API interface to implementation bindings
	 * @param binder
	 * @param apiDef
	 */
	private static <I extends ClientAPI> void _bindAPIAggregatorImpls2(final Binder binder,
																   	   final ClientAPIDef<I> apiDef) {
		for (Map.Entry<ServicesImpl,Class<? extends I>> apiImplByType : apiDef.getApiImplTypesByServiceImpl().entrySet()) {
			ServicesImpl impl = apiImplByType.getKey();
			Class<? extends I> apiImplType = apiImplByType.getValue();
			log.warn("\t\t- [{}] {} to {} annotated with @{}",
					 impl,apiDef.getApiInterfaceType(),apiImplType,impl.getClientAnnotation().getSimpleName());
			// binds api aggregator interface annotated with @ClientUses[Bean|REST|EJB...]Services to the concrete implementation XXXClientAPIFor[Bean|REST|EJB...]Services
			binder.bind(apiImplType)
				  .in(Singleton.class);
			binder.bind(apiDef.getApiInterfaceType())
				  .annotatedWith(impl.getClientAnnotation())	// annotated version ie @ClientUses[Bean|REST|EJB...]Services
			  	  .to(apiImplType);
			// for the Default impl bind the ClientAPI interface to the impl without annotation
			if (impl == ServicesImpl.Default) {
				binder.bind(apiDef.getApiInterfaceType())
					  .to(apiImplType);
			}
		}
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  SERVICES PROXY
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Binds the {@link ServiceProxiesAggregator} for every {@link ServicesImpl} as a {@link javax.inject.Singleton}: 
	 * <ul>
	 * 		<li>{@link ServiceProxiesAggregatorForBeanImpls} that contains fine-grained service proxies to bean implemented services</li>
	 * 		<li>{@link ServiceProxiesAggregatorForRESTImpls} that contains fine-grained service proxies to REST implemented services</li>
	 * 		<li>{@link ServiceProxiesAggregatorForEJBImpls} that contains fine-grained service proxies to EJB implemented services</li>
	 * 		<li>etc</li>
	 * </ul>
	 * A type extending {@link ServiceProxiesAggregator} MUST contain fields of types implementing {@link ServiceInterface} which are 
	 * the concrete proxy implementation to the services
	 * (there's a {@link ServiceInterface} implementing proxy type for every proxy method like REST, Bean, etc)
	 * 
	 * The {@link ServiceInterface} fields of {@link ServiceProxiesAggregator} implementing type are LAZY loaded by 
	 * {@link ServicesClientProxyLazyLoaderGuiceMethodInterceptor} which guesses what proxy implementation assign to the field:
	 * <ul>
	 * 		<li>If the {@link ServiceProxiesAggregator} extends {@link ServiceProxiesAggregatorForBeanImpls}, a {@link ProxyForBeanImplementedService} proxy instance
	 * 			 will be the one assigned to the {@link ServiceInterface} field.
	 * 			 Likewise, if the {@link ServiceProxiesAggregator} extends {@link ServiceProxiesAggregatorForRESTImpls}, a {@link ProxyForRESTImplementedService} proxy  
	 * 			 instance will be assigned to the {@link ServiceInterface} field.</li>
	 * 		<li>If the {@link ServiceProxiesAggregator} extends {@link ServiceProxiesAggregatorForDefaultImpls}, the concrete {@link ServiceInterface}-implementing 
	 * 			proxy instance is taken from the client properties XML file, so some service impls might be accessed using a BEAN proxy while others might be accessed
	 * 			using a REST proxy -depending on the properties file-</li>
	 * 		<li>If the {@link ServiceInterface} field's BEAN implementation is available this one will be assigned to the field no matter what type the aggregator is</li>
	 * </ul>
	 * @param binder
	 */
	private void _bindServiceProxiesAggregators(final Binder binder) {
		// Intercept all fine-grained proxy accessor method calls at ServicesAggregatorClientProxy subtypes (ie XXXServicesAggregatorClientProxyFor[Bean|REST|EJB...]Impl)
		// The interceptor lazily loads the fine-grained proxy instances and makes the aggregator creation simpler
		MethodInterceptor serviceProxyGetterInterceptor = new ServicesClientProxyLazyLoaderGuiceMethodInterceptor(_apiApp);
		binder.bindInterceptor(Matchers.subclassesOf(ServiceProxiesAggregator.class),
							   Matchers.any(),
							   serviceProxyGetterInterceptor);
		binder.requestInjection(serviceProxyGetterInterceptor);		// the method interceptor is feeded with a map of services created below
		
		// Bind every services proxy aggregator implementation
		Set<Class<? extends ServiceProxiesAggregatorImpl>> proxyAggregatorTypes = ServicesFinderHelper.clientFindServiceProxyAggregatorTypes(_apiApp);
		log.info("[ServiceProxyAggregator] > {} implementations",proxyAggregatorTypes.size());
		
		for (Class<? extends ServiceProxiesAggregatorImpl> proxyAggregatorType : proxyAggregatorTypes) {
			log.info("\t\t- {} instance using {}",
					 ServicesImpl.fromServiceProxyAggregatorType(proxyAggregatorType),proxyAggregatorType);
			binder.bind(proxyAggregatorType)
			      .in(Singleton.class);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({"rawtypes","unused"})
	private void _bindServiceInterfacesToProxiesOrImpls(final Binder binder,
											  			final AppCode apiApp,
											  			final Collection<AppCode> coreApps) {
		
		// Create a Set of a type that encapsulates the interface, it's implementation and the proxies
		Set<ServiceToImplDef<? extends ServiceInterface>> serviceInterfacesToImpl = ServicesFinderHelper.findServiceInterfacesToImpls(_apiApp,
																																	  coreApps);
		log.warn("=======================================================");
		log.warn("Service interface to implementation and client proxies:");
		log.warn("{} service interface detected",(CollectionUtils.hasData(serviceInterfacesToImpl) ? serviceInterfacesToImpl.size() : 0));
		log.warn("=======================================================");
		
		MapBinder<String,ServiceProxyImpl> servicesProxies = MapBinder.newMapBinder(binder,
																			  		String.class,ServiceProxyImpl.class);
		MapBinder<Class,ServiceInterface> servicesImpls = MapBinder.newMapBinder(binder,
																				 Class.class,ServiceInterface.class);
		// Bind every interface to it's implementation and proxies
		for (ServiceToImplDef<? extends ServiceInterface> serviceToImplDef : serviceInterfacesToImpl) {
			if (log.isWarnEnabled()) log.warn(serviceToImplDef.debugInfo().toString());
			
			// do the binding
			try {
				_bindServiceToImplAndProxy(binder,
										   serviceToImplDef,
										   servicesProxies,
										   servicesImpls);
			} catch(Throwable th) {
				th.printStackTrace(System.out);
			}
		}
	}
	/**
	 * Captures java generics to do the service interface to implementation / proxy bindings
	 * @param binder
	 * @param apiDef
	 */
	@SuppressWarnings({"unchecked","rawtypes"})
	private <S extends ServiceInterface> void _bindServiceToImplAndProxy(final Binder binder,
																   		 final ServiceToImplDef<S> serviceToImplDef,
																   		 final MapBinder<String,ServiceProxyImpl> proxies,
																   		 final MapBinder<Class,ServiceInterface> beanImpls) {
		// Do the bindings
		if (serviceToImplDef.getImplementationType() != null) {
			// Ensure that the BEAN Proxy exists: it's mandatory
			if (serviceToImplDef.getServiceProxyImplTypeFor(ServicesImpl.Bean) == null) {
				throw new IllegalStateException(Throwables.message("The {} proxy was NOT found for interface {} implemented by {}. Check that it exists at package {} and that it implements {}",
																   ServicesImpl.Bean,serviceToImplDef.getInterfaceType(),serviceToImplDef.getImplementationType(),
																   ServicesFinderHelper.serviceProxyPackage(_apiApp),ProxyForBeanImplementedService.class));
			}
			// The implementation IS available... NO proxy is needed
			log.warn("\t>Bind {} to the IMPLEMENTATION: {}",serviceToImplDef.getInterfaceType(),serviceToImplDef.getImplementationType());
//			System.out.println("binder.bind(" + serviceToImplDef.getInterfaceType().getSimpleName() + ")\n" +
//							   "      .annotatedWith(ServicesCoreImplementation.class)\n" +
//							   "      .to(" + serviceToImplDef.getImplementationType().getSimpleName() + ")");
			// The implementation is available:  Bind the service implementation to the interface
			// 									 annotated with @ServicesCoreImplementation
			binder.bind(serviceToImplDef.getImplementationType())
			  	  .in(Singleton.class);									// the service impl as singleton
			binder.bind(serviceToImplDef.getInterfaceType())
				  .to(serviceToImplDef.getImplementationType());		// not annotated binding
			binder.bind(serviceToImplDef.getInterfaceType())
				  .annotatedWith(ServicesCoreImplementation.class)
				  .to(serviceToImplDef.getImplementationType());		// annotated binding to service core
			
			// Proxy bindings...
			binder.bind(serviceToImplDef.getInterfaceType())
				  .annotatedWith(ClientUsesBeanServices.class)
				  .to(serviceToImplDef.getServiceProxyImplTypeFor(ServicesImpl.Bean));		// annotated binding of the bean proxy
			beanImpls.addBinding(serviceToImplDef.getInterfaceType())
					 .to(serviceToImplDef.getImplementationType());							// map binder (see ServicesClientProxyLazyLoaderGuiceMethodInterceptor)
		}
		else {
			// The implementation is NOT available:
			
			// a) bind the service interface annotated with @ServicesCoreImplementation with a fake implementation 
			//	  that simply throws an illegal state exception telling the developer that the implementation is NOT available
			binder.bind(serviceToImplDef.getInterfaceType())
			 	  .annotatedWith(ServicesCoreImplementation.class)
			 	  .toInstance((S)Proxy.newProxyInstance(serviceToImplDef.getInterfaceType().getClassLoader(),
			  					 					    new Class<?>[] {serviceToImplDef.getInterfaceType()},
			  					 					    new InvocationHandler() {
																@Override
																public Object invoke(final Object proxy,final Method method,final Object[] args) throws Throwable {
																	throw new IllegalStateException(Throwables.message("The {} implementation is NOT available in the classpath",
																													   serviceToImplDef.getInterfaceType()));
																}
														 }));
			// b) bind the service implementation default proxy
			ServicesImpl defaultServiceImpl = ServicesFinderHelper.configuredServiceProxyImplFor(serviceToImplDef.getInterfaceType(),
																								 _apiProps.forComponent("client"));
					
			log.warn("\t>Bind {} to the DEFAULT PROXY: {}",serviceToImplDef.getInterfaceType(),serviceToImplDef.getServiceProxyImplTypeFor(defaultServiceImpl));
//			System.out.println("binder.bind(" + serviceToImplDef.getInterfaceType().getSimpleName() + ")\n" +
//							   "      .to(" + serviceToImplDef.getServiceProxyImplTypeFor(ServicesImpl.REST) + ")");
			
			if (serviceToImplDef.getServiceProxyImplTypeFor(defaultServiceImpl) == null) {
				// The configured default service impl was not found... try to get another
				if (CollectionUtils.isNullOrEmpty(serviceToImplDef.getProxyTypeByImpl())) {
					// no other impl is available
					throw new IllegalStateException(Throwables.message("The default={} proxy for {} was not found",
																	   defaultServiceImpl,serviceToImplDef.getInterfaceType()));
				} else if (serviceToImplDef.getServiceProxyImplTypeFor(ServicesImpl.Bean) != null) {
					// the bean impl is available
					log.warn("The default={} proxy for {} was not found; using the {} instead",
							 defaultServiceImpl,serviceToImplDef.getInterfaceType(),ServicesImpl.Bean);		
					
					binder.bind(serviceToImplDef.getInterfaceType())
						  .to(serviceToImplDef.getServiceProxyImplTypeFor(ServicesImpl.Bean));
				} else {
					ServicesImpl anyImpl = CollectionUtils.pickOneElement(serviceToImplDef.getProxyTypeByImpl().keySet());
					log.warn("The default={} proxy for {} was not found; using the {} instead",
							 defaultServiceImpl,serviceToImplDef.getInterfaceType(),anyImpl);	
					binder.bind(serviceToImplDef.getInterfaceType())
						  .to(serviceToImplDef.getServiceProxyImplTypeFor(anyImpl));
				}
			} else {
				// the configured default service impl was found (the normal case)
				binder.bind(serviceToImplDef.getInterfaceType())
					  .to(serviceToImplDef.getServiceProxyImplTypeFor(defaultServiceImpl));
			}
			
			// b) bind all the service implementation proxies to be used at ServicesClientProxyLazyLoaderGuiceMethodInterceptor
			log.info("\t>MapBindings {} of:",serviceToImplDef.getInterfaceType());
			Set<BindingCheck> checkServices = Sets.newHashSetWithExpectedSize(serviceToImplDef.getProxyTypeByImpl().size());
			for (Map.Entry<ServicesImpl,Class<? extends ServiceProxyImpl>> me : serviceToImplDef.getProxyTypeByImpl().entrySet()) {
				ServicesImpl impl = me.getKey();
				Class<? extends ServiceProxyImpl> proxyType = me.getValue();
				
				// a) bind the proxy as singletion
				binder.bind(proxyType)
					  .in(Singleton.class);
				
				// b) add to the mapBinder that stores all proxies
				proxies.addBinding(proxyType.getName())
				  	   .to(proxyType);
				
				// c) bind the service interface to the service proxy impl annotated binding depending on the impl type (@ClientUses[Bean|REST|EJB...)Services
				//	  (check that the tuple service interface / service proxy / impl is NOT repeated 
				Class<? extends Annotation> annot = impl.getClientAnnotation();
				
				// Check that there's no two bindings for the same impl
				if (checkServices.add(new BindingCheck(serviceToImplDef.getInterfaceType(),proxyType,impl)) == false) { 
					throw new IllegalStateException(Throwables.message("The {} service proxy impl was ALREADY binded for {}. Another binding to to proxy {} cannot be done",
																	   serviceToImplDef.getInterfaceType(),proxyType,impl));
				}
//				System.out.println(">>>" + Strings.customized("{} to {} annotated with @{}",
//								   serviceToImplDef.getInterfaceType(),proxyType,annot.getSimpleName()));
				
				
				/*
				 * binder.bind((Class<ServiceInterface>)serviceToImplDef.getInterfaceType())
				 *	  .annotatedWith(annot)		// important!! annotated binding... DO NOT bind without annotation (see above note)
				 *	  .to((Class<ServiceInterface>)proxyType);
				 * 
				 * 
			     [javac] 		                                                       ^
			     [javac] /softbase_ejie/aplic/r01fb/tmp/compileLib/r01fbClasses/src/r01f/services/client/internal/ServicesClientGuiceModule.java:366: inconvertible types
			     [javac] found   : java.lang.Class<capture#10 of ? extends r01f.services.interfaces.ServiceProxyImpl>
			     [javac] required: java.lang.Class<r01f.services.interfaces.ServiceInterface>
			     [javac] 					  .to((Class<ServiceInterface>)proxyType);
			     [javac] 					                               ^
			     [javac] /softbase_ejie/aplic/r01fb/tmp/compileLib/r01fbClasses/src/r01f/services/client/internal/ServicesClientProxyLazyLoaderGuiceMethodInterceptor.java:159: 
			     			
				 * 
				 */
				
				Class<ServiceInterface>  _castedInterfaceType = (Class<ServiceInterface>)serviceToImplDef.getInterfaceType();
				Class<? extends ServiceInterface>  _castedProxyType = (Class<? extends ServiceInterface>)proxyType;
				
				binder.bind(_castedInterfaceType)
					  .annotatedWith(annot)		// important!! annotated binding... DO NOT bind without annotation (see above note)
					  .to( _castedProxyType);
			
				log.info("\t\t-{} annotated with @{}",proxyType,annot.getSimpleName());
			}
		}
	}
	
	@EqualsAndHashCode
	@RequiredArgsConstructor
	private class BindingCheck {
		private final Class<? extends ServiceInterface> _serviceInterfaceType;
		private final Class<? extends ServiceProxyImpl> _serviceProxyImplType;
		private final ServicesImpl _impl;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	XMLProperties PROVIDERS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Provider for the [apiAppCode].client.properties.xml (injected at {@link ServicesClientProxyLazyLoaderGuiceMethodInterceptor})
	 * @param props
	 * @return
	 */
	@Provides @XMLPropertiesComponent("api.client")
	XMLPropertiesForAppComponent provideXMLPropertiesForServices(final XMLProperties props) {
		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_apiApp),
																							 AppComponent.forId("client"));
		return outPropsForComponent;
	}
}
 