package r01f.services.client.api.delegates;

import r01f.services.client.ServiceProxiesAggregator;
import r01f.usercontext.UserContext;

public abstract class ClientAPIDelegateBase<P extends ServiceProxiesAggregator> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The user context
	 */
	protected final UserContext _userContext;
	/**
	 * a type that aggregates fine-grained proxies 
	 */
	protected final P _serviceProxiesAggregator;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected ClientAPIDelegateBase(final UserContext userContext,
								 	final P servicesProxy) {
		_userContext = userContext;
		_serviceProxiesAggregator = servicesProxy;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public <U extends UserContext> U getUserContext() {
		return (U)_userContext;
	}
	public P getServiceProxiesAggregator() {
		return _serviceProxiesAggregator;
	}
}
