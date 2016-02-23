package r01f.services.client.internal;

import java.util.Iterator;
import java.util.Map;

import com.google.common.collect.Maps;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.exceptions.Throwables;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.services.ServicesImpl;
import r01f.services.interfaces.ProxyForEJBImplementedService;
import r01f.services.interfaces.ProxyForRESTImplementedService;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.collections.CollectionUtils;

/**
 * A descriptor that matches the service interface with the bean impl (if available) and proxies
 * @param <S>
 */
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class ServiceToImplAndProxyDef<S extends ServiceInterface> 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Core AppCode
	 */
	@Getter private final AppCode _coreAppCode;
	/**
	 * Module
	 */
	@Getter private final AppComponent _module;
	/**
	 * A client interface definition for the {@link ServiceInterface} 
	 * (an interface type extending {@link ServiceInterface} )
	 */
	@Getter private final Class<S> _interfaceType;
	/**
	 * The service proxy impl (rest, bean, ejb, etc) configured at {apiAppCode}.client.properties.xml
	 * (it might or might not be the one to be used)
	 */
	@Getter private final ServicesImpl _configuredDefaultProxyImpl;
	/**
	 * A client proxy for the {@link ServiceInterface} implementation at core side
	 * (a type extending {@link ServiceProxyImpl}: {@link ProxyForRESTImplementedService}, {@link ProxyForEJBImplementedService}, etc)
	 */
	@Getter private Map<ServicesImpl,Class<? extends ServiceProxyImpl>> _proxyTypeByImpl;
	/**
	 * The service proxy impl (rest, bean, ejb, etc) to be used
	 * (the one that the system guess it have to be used)
	 */
	@Getter private ServicesImpl _proxyImplToUse;
	/**
	 * A bean core type implementing the {@link ServiceInterface} 
	 * (a concrete type -a bean- implementing the {@link ServiceInterface})
	 * this 
	 */
	@Getter private Class<? extends S> _beanServiceImplType;	
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static <S extends ServiceInterface> ServiceToImplAndProxyDef<S> createFor(final AppAndComponent appAndModule,
																			 		 final Class<S> serviceInterface,
																			 		 final ServicesImpl configuredDefaultProxyImpl) {
		ServiceToImplAndProxyDef<S> outImplDef = new ServiceToImplAndProxyDef<S>(appAndModule.getAppCode(),appAndModule.getAppComponent(),
									   						  	 				 serviceInterface,
									   						  	 				 configuredDefaultProxyImpl);
		// By default, the proxy type to be used is the configured one... only if the bean impl is available
		// this proxy type to be used is changed (see setServiceBeanImpl() method)
		outImplDef._proxyImplToUse = configuredDefaultProxyImpl;
		
		return outImplDef;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public Class<? extends ServiceProxyImpl> putProxyImplType(final Class<? extends ServiceProxyImpl> proxyImplType) {
		if (_proxyTypeByImpl == null) _proxyTypeByImpl = Maps.newHashMap();
		return _proxyTypeByImpl.put(ServicesImpl.fromServiceProxyType(proxyImplType),
								    proxyImplType);
	}
	public Class<? extends S> getServiceProxyImplTypeFor(final ServicesImpl impl) {
		Class<? extends S> outProxyImpl = this.getServiceProxyImplTypeOrNullFor(impl); 
		if (outProxyImpl == null) throw new IllegalStateException(Throwables.message("There's NO proxy impl for {}",_interfaceType));
		return outProxyImpl;			// the proxy MUST implement the service interface
	}
	@SuppressWarnings("unchecked")
	public Class<? extends S> getServiceProxyImplTypeOrNullFor(final ServicesImpl impl) {
		return CollectionUtils.hasData(_proxyTypeByImpl) ? (Class<? extends S>)_proxyTypeByImpl.get(impl)		// the proxy MUST implement the service interface
														 : null;
	}										
	@SuppressWarnings("unchecked")
	public void setServiceBeanImpl(final Class<? extends ServiceInterface> implType) {
		_proxyImplToUse = ServicesImpl.Bean;
		_beanServiceImplType = (Class<? extends S>)implType;
	}
	public AppAndComponent getCoreAppAndModule() {
		return AppAndComponent.composedBy(_coreAppCode,_module);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG (called from ServicesClientInterfaceToImplOrProxyBinder)
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		StringBuilder dbg = new StringBuilder();
		dbg.append(_interfaceType);
		if (_beanServiceImplType != null) {
			dbg.append("\n").append("\t\t\t\t\t- ").append("BEAN implementation is available in the classpath: ").append(_beanServiceImplType);
		}
		if (CollectionUtils.hasData(_proxyTypeByImpl)) {
			dbg.append("\n");
			for (Iterator<Map.Entry<ServicesImpl,Class<? extends ServiceProxyImpl>>> meIt = _proxyTypeByImpl.entrySet().iterator(); meIt.hasNext(); ) {
				Map.Entry<ServicesImpl,Class<? extends ServiceProxyImpl>> me = meIt.next();
				dbg.append("\t\t\t\t\t- ").append(me.getKey()).append(" proxy > ").append(me.getValue());
				if (meIt.hasNext()) dbg.append("\n");
			}
		}
		return dbg.toString();
	}
}
