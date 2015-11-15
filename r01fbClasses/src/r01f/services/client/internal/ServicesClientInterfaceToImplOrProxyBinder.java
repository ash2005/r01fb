package r01f.services.client.internal;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Binder;
import com.google.inject.Singleton;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.services.ServicesImpl;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.collections.CollectionUtils;

@Slf4j
@RequiredArgsConstructor
public class ServicesClientInterfaceToImplOrProxyBinder {
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public Map<Class<? extends ServiceInterface>,
			   Class<? extends ServiceInterface>> bindServiceInterfacesToProxiesOrImpls(final Binder binder,
										  			  									final Map<AppAndComponent,Set<ServiceToImplAndProxyDef<? extends ServiceInterface>>> serviceIfaceToImplAndProxiesByAppModule) {
		// Bind every interface to it's implementation and proxies
		Collection<ServiceToImplAndProxyDef<? extends ServiceInterface>> allServiceToImplAndProxiesDefs = Lists.newArrayList(Iterables.concat(serviceIfaceToImplAndProxiesByAppModule.values()));
		return this.bindServiceInterfacesToProxiesOrImpls(binder,
														  allServiceToImplAndProxiesDefs);
	}
	
	public Map<Class<? extends ServiceInterface>,
			   Class<? extends ServiceInterface>> bindServiceInterfacesToProxiesOrImpls(final Binder binder,
										  			  									final Collection<ServiceToImplAndProxyDef<? extends ServiceInterface>> serviceIfacesToImplAndProxiesDef) {
		Map<Class<? extends ServiceInterface>,Class<? extends ServiceInterface>> outServiceInterfaceTypeToImplOrProxyType = Maps.newHashMap();
		
		// Bind every interface to it's implementation and proxies
		for (ServiceToImplAndProxyDef<? extends ServiceInterface> serviceToImplDef : serviceIfacesToImplAndProxiesDef) {
			// do the binding
			try {
				// do the bindings and return the finally binded impl or proxy  
				Class<? extends ServiceInterface> serviceImplOrProxyType = _bindServiceInterfaceToImplOrProxy(binder,
										   		   										 					  serviceToImplDef);
				// add the finally binded impl or proxy to the MapBinder that provides the service impl or proxy instance
				// to be binded to the service interface at ServicesClientProxyLazyLoaderGuiceMethodInterceptor
				if (serviceImplOrProxyType != null) {
					outServiceInterfaceTypeToImplOrProxyType.put(serviceToImplDef.getInterfaceType(),
														 		 serviceImplOrProxyType);
				}
			} catch(Throwable th) {
				th.printStackTrace(System.out);
			}
		}
		return outServiceInterfaceTypeToImplOrProxyType;
	}
	/**
	 * Captures java generics to do the service interface to implementation / proxy bindings
	 * @param binder
	 * @param apiDef
	 */
	@SuppressWarnings({"unchecked"})
	private <S extends ServiceInterface> Class<S> _bindServiceInterfaceToImplOrProxy(final Binder binder,
																   		 	  		 final ServiceToImplAndProxyDef<S> serviceToImplDef) {
		Class<S> serviceImplOrProxyType = null;
		
		// [1] - The bean impl ia available... NO proxy is needed
		if (serviceToImplDef.getBeanServiceImplType() != null) {
			
			log.warn("\t\t>Bind {} to the BEAN IMPLEMENTATION: {} (no proxy needed)",serviceToImplDef.getInterfaceType(),serviceToImplDef.getBeanServiceImplType());

			// The implementation is available:  Bind the service implementation to the interface
			// 									 annotated with @ServicesCoreImplementation
			serviceImplOrProxyType = (Class<S>)serviceToImplDef.getBeanServiceImplType();
			
			binder.bind(serviceToImplDef.getBeanServiceImplType())
			  	  .in(Singleton.class);							// the service impl as singleton
			binder.bind(serviceToImplDef.getInterfaceType())
				  .to(serviceImplOrProxyType);					// not annotated binding					
		}
		// [2] The bean implementation is NOT available: use the configured (REST, EJB...) proxy impl
		else {			
			// bind the service interface default proxy or any other if the default one is NOT present
			ServicesImpl defaultServiceImpl = serviceToImplDef.getConfiguredDefaultProxyImpl(); 
						
			if (serviceToImplDef.getServiceProxyImplTypeOrNullFor(defaultServiceImpl) != null) {
				// the configured default proxy was found (the normal case)
				serviceImplOrProxyType = (Class<S>)serviceToImplDef.getServiceProxyImplTypeFor(defaultServiceImpl);
				binder.bind(serviceToImplDef.getInterfaceType())
					  .to(serviceImplOrProxyType);
				log.warn("\t\t>Bind {} to the DEFAULT PROXY: {}",
						 serviceToImplDef.getInterfaceType(),serviceImplOrProxyType);

				
			} else if (serviceToImplDef.getServiceProxyImplTypeOrNullFor(ServicesImpl.REST) != null) {
				// the configured default proxy was not found BUT the REST impl proxy is available
				serviceImplOrProxyType = (Class<S>)serviceToImplDef.getServiceProxyImplTypeFor(ServicesImpl.REST);
				binder.bind(serviceToImplDef.getInterfaceType())
					  .to(serviceImplOrProxyType);
				log.warn("\t\t>Bind {} to the DEFAULT PROXY: {} (the default={} was NOT found so used {} instead)",
						 serviceToImplDef.getInterfaceType(),ServicesImpl.REST,
						 defaultServiceImpl,serviceImplOrProxyType);
				
			} else if (CollectionUtils.hasData(serviceToImplDef.getProxyTypeByImpl())) {
				// The configured proxy was not found... try to get another
				ServicesImpl anyImpl = CollectionUtils.pickOneElement(serviceToImplDef.getProxyTypeByImpl().keySet());
				serviceImplOrProxyType = (Class<S>)serviceToImplDef.getServiceProxyImplTypeFor(anyImpl);
				binder.bind(serviceToImplDef.getInterfaceType())
					  .to(serviceImplOrProxyType);
				log.warn("\t\t>Bind {} to the DEFAULT PROXY: {} (the default={} was NOT found so used {} instead)",
						 serviceToImplDef.getInterfaceType(),anyImpl,
						 defaultServiceImpl,serviceImplOrProxyType);
			} 
			
			// c) bind the proxy to service impl to be used at ServicesClientProxyLazyLoaderGuiceMethodInterceptor
			if (CollectionUtils.isNullOrEmpty(serviceToImplDef.getProxyTypeByImpl())) {
				log.error("\t\t>NO proxy for {} was not found: Is there any proxy type implementing {} available in the classpath?",
						  serviceToImplDef.getInterfaceType(),serviceToImplDef.getInterfaceType());
			}
		}
		return serviceImplOrProxyType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@EqualsAndHashCode
	@RequiredArgsConstructor
	private class ServiceInterfaceBindingCheck {
		private final Class<? extends ServiceInterface> _serviceInterfaceType;
		private final Class<? extends ServiceProxyImpl> _serviceProxyImplType;
		private final ServicesImpl _impl;
	}
}
