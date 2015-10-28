package r01f.services.client;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.marshalling.Marshaller;
import r01f.usercontext.UserContext;

/**
 * Base for every sub-api
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class ClientSubAPIBase<S extends ClientAPI,
									   P extends ServiceProxiesAggregator> {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS (injected by constructor)
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * User context
	 */
	private final UserContext _userContext;
	/**
	 * Reference to the client-apis
	 * it's normal that another sub-api must be used from a sub-api
	 */
	private final S _clientAPIs;
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return other sub-apis
	 */
	public S getClientAPIs() {
		return _clientAPIs;
	}
	/**
	 * @return  an aggregator of proxies for the services real services impl
	 */
	public P getServicesProxiesAggregator() {
		P clientProxy = _clientAPIs.<P>getServiceProxiesAggregator();
		return clientProxy;
	}
	/**
	 * @return the model object's marshaller
	 */
	public Marshaller getModelObjectsMarshaller() {
		Marshaller marshaller = _clientAPIs.getModelObjectsMarshaller();
		return marshaller;
	}
	/**
	 * @return the user context
	 */
	@SuppressWarnings("unchecked")
	public <U extends UserContext> U getUserContext() {
		return (U)_userContext;
	}
}
