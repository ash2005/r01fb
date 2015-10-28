package r01f.services.client.api.delegates;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.services.interfaces.ServiceInterface;
import r01f.usercontext.UserContext;

@Accessors(prefix="_")
public abstract class ClientAPIServiceDelegateBase<S extends ServiceInterface> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The user context
	 */
	@Getter(AccessLevel.PROTECTED) protected final UserContext _userContext;
	/**
	 * The service interface 
	 */
	protected final S _serviceProxy;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIServiceDelegateBase(final UserContext userContext,
										final S serviceProxy) {
		_userContext = userContext;
		_serviceProxy = serviceProxy;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public S getServiceProxy() {
		return _serviceProxy;
	}
	@SuppressWarnings("unchecked")
	public <T extends ServiceInterface> T getServiceProxyAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_serviceProxy;
	}
}

