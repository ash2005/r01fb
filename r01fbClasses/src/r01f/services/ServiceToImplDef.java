package r01f.services;

import java.util.Iterator;
import java.util.Map;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.exceptions.Throwables;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.services.interfaces.ProxyForEJBImplementedService;
import r01f.services.interfaces.ProxyForRESTImplementedService;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.interfaces.ServiceProxyImpl;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Maps;


@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class ServiceToImplDef<S extends ServiceInterface> 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * A client interface definition for the {@link ServiceInterface} 
	 * (an interface type extending {@link ServiceInterface} )
	 */
	@Getter private final Class<S> _interfaceType;
	/**
	 * A client proxy for the {@link ServiceInterface} implementation at core side
	 * (a type extending {@link ServiceProxyImpl}: {@link ProxyForBeanImplementedService}, {@link ProxyForRESTImplementedService}, {@link ProxyForEJBImplementedService}, etc)
	 */
	@Getter private Map<ServicesImpl,Class<? extends ServiceProxyImpl>> _proxyTypeByImpl;	
	/**
	 * A core type implementing the {@link ServiceInterface} 
	 * (a concrete type -a bean- implementing the {@link ServiceInterface})
	 */
	@Getter private Class<? extends S> _implementationType;
/////////////////////////////////////////////////////////////////////////////////////////
//  FACTORY
/////////////////////////////////////////////////////////////////////////////////////////
	public static <S extends ServiceInterface> ServiceToImplDef<S> createFor(final Class<S> serviceInterface) {
		return new ServiceToImplDef<S>(serviceInterface);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public Class<? extends ServiceProxyImpl> putProxyImplType(final Class<? extends ServiceProxyImpl> proxyImplType) {
		if (_proxyTypeByImpl == null) _proxyTypeByImpl = Maps.newHashMap();
		return _proxyTypeByImpl.put(ServicesImpl.fromServiceProxyType(proxyImplType),
								    proxyImplType);
	}
	@SuppressWarnings("unchecked")
	public Class<? extends S> getServiceProxyImplTypeFor(final ServicesImpl impl) {
		if (CollectionUtils.isNullOrEmpty(_proxyTypeByImpl)) throw new IllegalStateException(Throwables.message("There's NO proxy impl for {}",_interfaceType));
		return (Class<? extends S>)_proxyTypeByImpl.get(impl);		// the proxy MUST implement the service interface
	}
	@SuppressWarnings("unchecked")
	public void setImplementationType(final Class<? extends ServiceInterface> implType) {
		_implementationType = (Class<? extends S>)implType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		StringBuilder dbg = new StringBuilder();
		dbg.append(_interfaceType).append(" implemented by ").append(_implementationType)
		   .append("\n");
		if (CollectionUtils.hasData(_proxyTypeByImpl)) {
			for (Iterator<Map.Entry<ServicesImpl,Class<? extends ServiceProxyImpl>>> meIt = _proxyTypeByImpl.entrySet().iterator(); meIt.hasNext(); ) {
				Map.Entry<ServicesImpl,Class<? extends ServiceProxyImpl>> me = meIt.next();
				dbg.append("\t-").append(me.getKey()).append(" proxy > ").append(me.getValue());
				if (meIt.hasNext()) dbg.append("\n");
			}
		}
		return dbg.toString();
	}
}
