package r01f.services;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import r01f.enums.Enums;
import r01f.enums.Enums.EnumWrapper;
import r01f.exceptions.Throwables;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.ClientAPI;
import r01f.services.client.ClientAPIForBeanServices;
import r01f.services.client.ClientAPIForDefaultServices;
import r01f.services.client.ClientAPIForEJBServices;
import r01f.services.client.ClientAPIForMockServices;
import r01f.services.client.ClientAPIForRESTServices;
import r01f.services.client.ClientUsesBeanServices;
import r01f.services.client.ClientUsesDefaultServices;
import r01f.services.client.ClientUsesEJBServices;
import r01f.services.client.ClientUsesMockServices;
import r01f.services.client.ClientUsesRESTServices;
import r01f.services.client.ServiceProxiesAggregatorForBeanImpls;
import r01f.services.client.ServiceProxiesAggregatorForDefaultImpls;
import r01f.services.client.ServiceProxiesAggregatorForEJBImpls;
import r01f.services.client.ServiceProxiesAggregatorForRESTImpls;
import r01f.services.client.ServiceProxiesAggregatorImpl;
import r01f.services.core.BeanImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.EJBImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.RESTImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.ServletImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.services.interfaces.ProxyForEJBImplementedService;
import r01f.services.interfaces.ProxyForMockImplementedService;
import r01f.services.interfaces.ProxyForRESTImplementedService;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public enum ServicesImpl {
	Default,		// Priority 0 (highest)
	Bean,			// Priority 1
	REST,			// Priority 2
	EJB,			// Priority 3
	Servlet,		// Priority 4 (it's NOT a full-fledged service since it's NOT consumed using a client api; it's called from a web browser so it has NO associated client-proxy)
	Mock,			// Priority 5 (lower)
	NULL;			// used at ServicesCore annotation
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the priority of the impl; the lower the priority value, the higher priority it has
	 */
	public int getPriority() {
		return this.ordinal();
	}
	public Class<? extends ServicesCoreBootstrapGuiceModule> getCoreGuiceModuleType() {
		Class<? extends ServicesCoreBootstrapGuiceModule> outCoreGuiceModule = null;
		switch(this) {
		case Bean:
			outCoreGuiceModule = BeanImplementedServicesCoreGuiceModuleBase.class;
			break;
		case REST:
			outCoreGuiceModule = RESTImplementedServicesCoreGuiceModuleBase.class;
			break;
		case EJB:
			outCoreGuiceModule = EJBImplementedServicesCoreGuiceModuleBase.class;	
			break;
		case Servlet:
			outCoreGuiceModule = ServletImplementedServicesCoreGuiceModuleBase.class;
			break;
		case Mock:
		case Default:
		default:
			throw new IllegalStateException();
		}
		return outCoreGuiceModule;
	}
	public Class<? extends ServiceProxyImpl> getServiceProxyType() {
		Class<? extends ServiceProxyImpl> outProxyType = null;
		switch(this) {
		case Bean:
			outProxyType = ProxyForBeanImplementedService.class;
			break;
		case REST:
			outProxyType = ProxyForRESTImplementedService.class;
			break;
		case EJB:
			outProxyType = ProxyForEJBImplementedService.class;	
			break;
		case Servlet:
			throw new UnsupportedOperationException(Throwables.message("{} is NOT a full-fledged service since it's NOT consumed using a client api; it's called from a web browser so it has NO associated client-proxy",Servlet));
		case Mock:
			outProxyType = ProxyForMockImplementedService.class;
			break;
		case Default:
		default:
			throw new IllegalStateException(Throwables.message("NO {} for {}",
															   ServiceProxyImpl.class,this));
		}
		return outProxyType;
	}
	public Class<? extends ClientAPI> getClientAPIType() {
		Class<? extends ClientAPI> apiAggregatorSuperType = null;
		switch (this) {
		case Bean:
			apiAggregatorSuperType = ClientAPIForBeanServices.class;
			break;
		case REST:
			apiAggregatorSuperType = ClientAPIForRESTServices.class;
			break;
		case EJB:
			apiAggregatorSuperType = ClientAPIForEJBServices.class;
			break;
		case Servlet:
			throw new UnsupportedOperationException(Throwables.message("{} is NOT a full-fledged service since it's NOT consumed using a client api; it's called from a web browser so it has NO associated client-proxy",Servlet));
		case Mock:
			apiAggregatorSuperType = ClientAPIForMockServices.class;
			break;
		case Default:
			apiAggregatorSuperType = ClientAPIForDefaultServices.class;
			break;
		default:
			throw new IllegalStateException();
		}
		return apiAggregatorSuperType;
	}
	public Class<? extends ServiceProxiesAggregatorImpl> getServicesAggregatorClientProxyType() {
		Class<? extends ServiceProxiesAggregatorImpl> proxyAggregatorSuperType = null;
		switch (this) {
		case Bean:
			proxyAggregatorSuperType = ServiceProxiesAggregatorForBeanImpls.class;
			break;
		case REST:
			proxyAggregatorSuperType = ServiceProxiesAggregatorForRESTImpls.class;
			break;
		case EJB:
			proxyAggregatorSuperType = ServiceProxiesAggregatorForEJBImpls.class;
			break;
		case Servlet:
			throw new UnsupportedOperationException(Throwables.message("{} is NOT a full-fledged service since it's NOT consumed using a client api; it's called from a web browser so it has NO associated client-proxy",Servlet));
		case Default:
			proxyAggregatorSuperType = ServiceProxiesAggregatorForDefaultImpls.class;
			break;
		default:
			throw new IllegalStateException();
		}
		return proxyAggregatorSuperType;
	}
	public Class<? extends Annotation> getClientAnnotation() {
		Class<? extends Annotation> clientAnnot = null;
		switch (this) {
		case Bean:
			clientAnnot = ClientUsesBeanServices.class;
			break;
		case REST:
			clientAnnot = ClientUsesRESTServices.class;
			break;
		case EJB:
			clientAnnot = ClientUsesEJBServices.class;
			break;
		case Servlet:
			throw new UnsupportedOperationException(Throwables.message("{} is NOT a full-fledged service since it's NOT consumed using a client api; it's called from a web browser so it has NO associated client-proxy",Servlet));
		case Mock:
			clientAnnot = ClientUsesMockServices.class;
			break;
		case Default:
			clientAnnot = ClientUsesDefaultServices.class;
			break;
		default:
			throw new IllegalStateException();
		}
		return clientAnnot;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the {@link ServicesImpl} from a type extending {@link ServicesCoreBootstrapGuiceModule}
	 * <pre class='brush:java'>
	 * 		public class MyBindingModule
	 * 		 	 extends BeanImplementedServicesGuiceBindingModule {
	 * 			...
	 * 		}
	 * </pre>
	 * @param type
	 * @return
	 */
	public static ServicesImpl fromBindingModule(final Class<? extends ServicesCoreBootstrapGuiceModule> type) {
		ServicesImpl outImpl = null;
		if (ReflectionUtils.isSubClassOf(type,BeanImplementedServicesCoreGuiceModuleBase.class)) {
			outImpl = ServicesImpl.Bean;
		} else if (ReflectionUtils.isSubClassOf(type,RESTImplementedServicesCoreGuiceModuleBase.class)) {
			outImpl = ServicesImpl.REST;
		} else if (ReflectionUtils.isSubClassOf(type,EJBImplementedServicesCoreGuiceModuleBase.class)) {
			outImpl = ServicesImpl.EJB;
		} else if (ReflectionUtils.isSubClassOf(type,ServletImplementedServicesCoreGuiceModuleBase.class)) {
			outImpl = ServicesImpl.Servlet;
		} else {
			throw new IllegalStateException(Throwables.message("The {} implementation {} is NOT of one of the supported types {}",
															   ServicesCoreBootstrapGuiceModule.class,type,ServicesImpl.values()));
		}
		return outImpl;
	}
	public static ServicesImpl fromClientAPIType(final Class<? extends ClientAPI> type) {
		ServicesImpl outImpl = null;
		if (ReflectionUtils.isSubClassOf(type,ClientAPIForBeanServices.class)) {
			outImpl = ServicesImpl.Bean;
		} else if (ReflectionUtils.isSubClassOf(type,ClientAPIForRESTServices.class)) {
			outImpl = ServicesImpl.REST;
		} else if (ReflectionUtils.isSubClassOf(type,ClientAPIForEJBServices.class)) {
			outImpl = ServicesImpl.EJB;
		} else if (ReflectionUtils.isSubClassOf(type,ClientAPIForDefaultServices.class)) {
			outImpl = ServicesImpl.Default;
		} else {
			throw new IllegalStateException(Throwables.message("The {} implementation {} is NOT of one of the supported types {}",
															   ClientAPI.class,type,ServicesImpl.values()));
		}
		return outImpl;
	}
	public static ServicesImpl fromServiceProxyAggregatorType(final Class<? extends ServiceProxiesAggregatorImpl> type) {
		ServicesImpl outImpl = null;
		if (ReflectionUtils.isSubClassOf(type,ServiceProxiesAggregatorForBeanImpls.class)) {
			outImpl = ServicesImpl.Bean;
		} else if (ReflectionUtils.isSubClassOf(type,ServiceProxiesAggregatorForRESTImpls.class)) {
			outImpl = ServicesImpl.REST;
		} else if (ReflectionUtils.isSubClassOf(type,ServiceProxiesAggregatorForEJBImpls.class)) {
			outImpl = ServicesImpl.EJB;
		} else if (ReflectionUtils.isSubClassOf(type,ServiceProxiesAggregatorForDefaultImpls.class)) {
			outImpl = ServicesImpl.Default;
		} else {
			throw new IllegalStateException(Throwables.message("The {} implementation {} is NOT of one of the supported types {}",
															   ServiceProxiesAggregatorImpl.class,type,ServicesImpl.values()));
		}
		return outImpl;
	}
	public static ServicesImpl fromServiceProxyType(final Class<? extends ServiceProxyImpl> type) {
		ServicesImpl outImpl = null;
		if (ReflectionUtils.isSubClassOf(type,ProxyForBeanImplementedService.class)) {
			outImpl = ServicesImpl.Bean;
		} else if (ReflectionUtils.isSubClassOf(type,ProxyForRESTImplementedService.class)) {
			outImpl = ServicesImpl.REST;
		} else if (ReflectionUtils.isSubClassOf(type,ProxyForEJBImplementedService.class)) {
			outImpl = ServicesImpl.EJB;
		} else {
			throw new IllegalStateException(Throwables.message("The {} implementation {} is NOT of one of the supported types {}",
															   ServiceProxyImpl.class,type,ServicesImpl.values()));
		}
		return outImpl;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static EnumWrapper<ServicesImpl> ENUMS = Enums.of(ServicesImpl.class);
	
	public static ServicesImpl fromName(final String name) {
		return ENUMS.fromName(name);
	}
	public static ServicesImpl fromNameOrNull(final String name) {
		return ENUMS.fromName(name);
	}
	public static Collection<ServicesImpl> fromNames(final String... names) {
		if (CollectionUtils.isNullOrEmpty(names)) return null;
		Collection<ServicesImpl> outImpls = Lists.newArrayListWithExpectedSize(names.length);
		for (String name : names) {
			if (Strings.isNullOrEmpty(name)) continue;
			outImpls.add(ServicesImpl.fromName(name));
		}
		return CollectionUtils.hasData(outImpls) ? outImpls : null;
	}
	public static Set<ServicesImpl> asSet() {
		return Sets.newHashSet(ServicesImpl.values());
	}
	public boolean is(final ServicesImpl other) {
		return ENUMS.is(this,other);
	}
	public boolean isNOT(final ServicesImpl other) {
		return !ENUMS.is(this,other);
	}

}
