package r01f.services;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import r01f.enums.Enums;
import r01f.enums.Enums.EnumWrapper;
import r01f.exceptions.Throwables;
import r01f.reflection.ReflectionUtils;
import r01f.services.core.internal.BeanImplementedServicesCoreBootstrapGuiceModuleBase;
import r01f.services.core.internal.EJBImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.RESTImplementedServicesCoreGuiceModuleBase;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.core.internal.ServletImplementedServicesCoreGuiceModuleBase;
import r01f.services.interfaces.ProxyForEJBImplementedService;
import r01f.services.interfaces.ProxyForMockImplementedService;
import r01f.services.interfaces.ProxyForRESTImplementedService;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

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
			outCoreGuiceModule = BeanImplementedServicesCoreBootstrapGuiceModuleBase.class;
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
		if (ReflectionUtils.isSubClassOf(type,BeanImplementedServicesCoreBootstrapGuiceModuleBase.class)) {
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
	public static ServicesImpl fromServiceProxyType(final Class<? extends ServiceProxyImpl> type) {
		ServicesImpl outImpl = null;
		if (ReflectionUtils.isSubClassOf(type,ProxyForRESTImplementedService.class)) {
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
