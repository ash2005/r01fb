package r01f.services.client.internal;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.matcher.Matcher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.AppCode;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServicesFinderHelper;
import r01f.services.ServicesImpl;
import r01f.services.client.ServiceProxiesAggregator;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * A GUICE {@link MethodInterceptor} that lazy loads the {@link ServiceProxiesAggregator}'s sub type that provides access to the
 * {@link SubServiceInterface} proxy implementation depending on the {@link ServicesImpl}
 * 
 * This type contains a cache that maps the {@link SubServiceInterface} to it's concrete implementation depending on the {@link ServicesImpl}
 */
@Slf4j
@RequiredArgsConstructor
public class ServicesClientProxyLazyLoaderGuiceMethodInterceptor 
  implements MethodInterceptor {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The API appCode
	 */
	private final AppCode _apiAppCode;
	/**
	 * Provider for [apiAppCode].client.properties.xml 
	 */
	@Inject @XMLPropertiesComponent("api.client")	// provider at ServicesClientGuiceModule
	private XMLPropertiesForAppComponent _clientProperties;
	/**
	 * A {@link Map} with the service proxy impl by service interface
	 */
	@Inject
	private Map<String,ServiceProxyImpl> _serviceProxies;
	/**
	 * A {@link Map} with the bean service impl by service interface
	 */
	@Inject @SuppressWarnings({ "rawtypes" })
	private Map<Class,ServiceInterface> _serviceImpls;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  Intercept methods returning a ServiceInterface implementing type
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings({ "cast","unchecked" })
	public Object invoke(final MethodInvocation invocation) throws Throwable {	
		
		// Do not intercept the ServicesClientProxy base type methods
		if (!_isInterceptedMethodCall(invocation.getMethod())) return invocation.proceed();

		// If the return type is a ServiceInteface or a SubServiceInterface, do the lazy load if needed
		Object out = invocation.proceed();	// <-- this can return null if the ServiceInterface or SubServiceInterface was NOT created previously
		
		if (out == null) {
			// Type of the proxy aggregator
			ServiceProxiesAggregator serviceProxyAggregator = (ServiceProxiesAggregator)invocation.getThis();
			Class<? extends ServiceProxiesAggregator> serviceProxyAggregatorType = (Class<? extends ServiceProxiesAggregator>)serviceProxyAggregator.getClass();
			
			// the ServiceInterface or SubServiceInterface concrete type 
			Class<?> returnType = invocation.getMethod().getReturnType();
			
			if (returnType != null && ReflectionUtils.isImplementing(returnType,
																	 ServiceInterface.class)) {
				// Find the field at the aggregator type that contains the proxy
				Class<? extends ServiceInterface> serviceInterfaceType = (Class<? extends ServiceInterface>)returnType;
				Field serviceInterfaceBaseField = _findServiceInterfaceField(serviceProxyAggregatorType,
																		 	 serviceInterfaceType);
				
				// If the bean impl is available return it
				ServiceInterface serviceImpl = _serviceImpls.get(serviceInterfaceType);
				if (serviceImpl != null) {
					ReflectionUtils.setFieldValue(serviceProxyAggregator,serviceInterfaceBaseField,
												  serviceImpl,
												  false);
					out = serviceImpl;
					log.info("[ServiceProxy aggregation] > {} field of type {} was not initialized on services proxy aggregator {} so an instance of {} was lazily created",
							 serviceInterfaceBaseField.getName(),serviceInterfaceType,serviceProxyAggregator.getClass(),serviceImpl.getClass());
				}
				// The bean impl is NOT available... use a proxy
				else {
					ServiceInterface serviceProxy = _createServiceProxy(serviceProxyAggregator,
												    				    serviceInterfaceType);
					ReflectionUtils.setFieldValue(serviceProxyAggregator,serviceInterfaceBaseField,
												  serviceProxy,
												  false);
					out = serviceProxy;
					log.info("[ServiceProxy aggregation] > {} field of type {} was not initialized on services proxy aggregator {} so an instance of {} was lazily created",
							 serviceInterfaceBaseField.getName(),serviceInterfaceType,serviceProxyAggregator.getClass(),serviceProxy.getClass());
				}
			}
		}
		return out;
	}
	
	private static Field _findServiceInterfaceField(final Class<? extends ServiceProxiesAggregator> servicesProxyAggregator,
											 		final Class<?> serviceType) {
		Field[] subServiceInterfaceFields = ReflectionUtils.fieldsOfType(servicesProxyAggregator,
																	  	 serviceType);
		if (CollectionUtils.isNullOrEmpty(subServiceInterfaceFields)) throw new IllegalStateException(Strings.customized("The proxy aggregator type {} does NOT have a field of type {}",
																													     servicesProxyAggregator,serviceType));
		if (subServiceInterfaceFields.length > 1) {
			//for (int i=0; i<serviceInterfaceFields.length; i++) System.out.println(">>>>>" + serviceInterfaceFields[i].getName());
			throw new IllegalStateException(Strings.customized("The proxy aggregator type {} have MORE THAN ONE field of type {}",
															   servicesProxyAggregator,serviceType));
			
		}
		return subServiceInterfaceFields[0];
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Create an instance of a type that is the proxy implementation (= service proxy) for the {@link ServiceInterface}
	 * Note that there could be many implementations for the service interface each of them MUST:
	 *		a) implement the ServiceInterface
	 *		b) implement a marker interface to know what proxy type it is:
	 *				- ProxyForRESTImplementedService: it's a service interface REST service proxy implementation 
	 *				- ProxyForBeanImplementedService: it's a service interface BEAN service proxy implementation
	 *				- ProxyForEJBImplementedService: it's a service interface EJB service proxy implementation
	 *				...
	 */
	private <P extends ServiceInterface> P _createServiceProxy(final ServiceProxiesAggregator serviceProxyAggregator,
												 			   final Class<? extends ServiceInterface> serviceInterfaceType) {
		P outProxy = null;
		
		// Among every concrete proxy implementation feeded at _serviceProxies get the one for the given aggregator
		// Note that the aggregator must be for a service impl (be it REST, Bean, EJB...)
		ServicesImpl servicesImpl = serviceProxyAggregator.getServicesImpl();
		
		// If the serviceProxyAggregator ServicesImpl type is a concrete one (REST, Bean, EJB, etc), 
		// find the concrete ServiceClientProxy impl
		if (servicesImpl.isNOT(ServicesImpl.Default)) {
			// Filter the proxy for the concrete impl
			outProxy = this.<P>_findServiceProxyFor(serviceInterfaceType,
													servicesImpl);
			if (outProxy == null) {
				log.warn("There's NO concrete implementation for {} (the services proxy type): " +
						 "a search for a type implementing both {} and {} was done at {} BUT no concrete implementation was found... " +
						 "the {}.client.properties.xml configured one will be tried",
						 serviceInterfaceType,
						 serviceInterfaceType,servicesImpl.getServiceProxyType(),
						 ServicesFinderHelper.serviceProxyPackage(_apiAppCode),_apiAppCode);
			}
		}
		if (outProxy == null) {
			// If the serviceProxyAggregator ServicesImpl type is the DEFAULT one or the requested one was NOT found,
			// the concrete ServiceClientProxy impl is looked at the {clientAppCode}.client.properties.xml properties file
			ServicesImpl configuredImpl = ServicesFinderHelper.configuredServiceProxyImplFor(serviceInterfaceType,
																				   			 _clientProperties);
			outProxy = this.<P>_findServiceProxyFor(serviceInterfaceType,
													configuredImpl);
			// Last resort: if the proxy is still not found, try this order: Bean, REST, EJB, ...
			if (outProxy == null && configuredImpl.isNOT(ServicesImpl.Bean)) {
				outProxy = this.<P>_findServiceProxyFor(serviceInterfaceType,
														ServicesImpl.Bean);
				if (outProxy != null) log.warn("{}: Using a {}-type proxy implementing {} as last resort since the configured proxy type ({}) was NOT available",
												serviceProxyAggregator.getClass(),ServicesImpl.Bean,serviceInterfaceType,configuredImpl);
			}
			if (outProxy == null && configuredImpl.isNOT(ServicesImpl.REST)) {
				outProxy = this.<P>_findServiceProxyFor(serviceInterfaceType,
														ServicesImpl.REST);
				if (outProxy != null) log.warn("{}: Using a {}-type proxy implementing {} as last resort since the configured proxy type ({}) was NOT available",
												serviceProxyAggregator.getClass(),ServicesImpl.REST,serviceInterfaceType,configuredImpl);
			}
			if (outProxy == null && configuredImpl.isNOT(ServicesImpl.EJB)) {
				outProxy = this.<P>_findServiceProxyFor(serviceInterfaceType,
														ServicesImpl.EJB);
				if (outProxy != null) log.warn("{}: Using a {}-type proxy implementing {} as last resort since the configured proxy type ({}) was NOT available",
												serviceProxyAggregator.getClass(),ServicesImpl.EJB,serviceInterfaceType,configuredImpl);
			}
		}
		
		// If NO proxy was found... error
		if (outProxy == null) {
			if (servicesImpl.isNOT(ServicesImpl.Default)) throw new IllegalStateException(Throwables.message("Error at {} finding a proxy implementing {}: a search for a type implementing both {} and {} was done at {} BUT no concrete implementation was found",
						 									   					 							 serviceProxyAggregator.getClass(),serviceInterfaceType,serviceInterfaceType,servicesImpl.getServiceProxyType(),ServicesFinderHelper.serviceProxyPackage(_apiAppCode)));
			throw new IllegalStateException(Throwables.message("Error at {} finding a proxy implementing {}: a search for a type implementing {} was done at {} BUT no concrete implementation was found",
						 									   serviceProxyAggregator.getClass(),serviceInterfaceType,serviceInterfaceType,ServicesFinderHelper.serviceProxyPackage(_apiAppCode)));
		}
		return outProxy;
	}
	private <P extends ServiceInterface> P _findServiceProxyFor(final Class<? extends ServiceInterface> serviceInterfaceType,
																final ServicesImpl serviceImpl) {
		Set<P> outProxies = FluentIterable.from(_serviceProxies.values()) 
										  // Filter proxies implementing the requested ServiceInterface 
										  // in the requested flavor (Bean, REST, EJB, etc)
										  .filter(new Predicate<ServiceProxyImpl>() {
															@Override
															public boolean apply(final ServiceProxyImpl proxy) {
																return ReflectionUtils.isSubClassOf(proxy.getClass(),serviceInterfaceType)
																	&& ServicesImpl.fromServiceProxyType(proxy.getClass())
																				   .is(serviceImpl);
															}
												  })
										  // transform to P
										  .transform(new Function<ServiceProxyImpl,P>() {
															@Override @SuppressWarnings("unchecked")
															public P apply(final ServiceProxyImpl proxy) {
																return (P)proxy;
															}
												      })
									      .toSet();
		if (outProxies != null && outProxies.size() > 1) throw new IllegalStateException(Throwables.message("There're more than a single {} implementation for {}: {}",
																											serviceInterfaceType,serviceImpl,outProxies));
		return CollectionUtils.hasData(outProxies) ? CollectionUtils.of(outProxies).pickOneAndOnlyElement()
								  				   : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static Method[] NOT_INTERCEPTED_METHODS = new Method[] {ReflectionUtils.method(ServiceProxiesAggregator.class,
														   		    					   "getServicesImpl")};
	/**
	 * Checks if a method should be intercepted
	 * (an alternative implementation could be to create a {@link Matcher} subtype)
	 * @return true if its an intercepted method
	 */
	private static boolean _isInterceptedMethodCall(final Method invokedMethod) {
		boolean outIntercepted = true;
		for (Method notInterceptedMethod : NOT_INTERCEPTED_METHODS) {
			if (invokedMethod.equals(notInterceptedMethod)) {
				outIntercepted = false;
				break;
			}
		}
		return outIntercepted;
	}
}
