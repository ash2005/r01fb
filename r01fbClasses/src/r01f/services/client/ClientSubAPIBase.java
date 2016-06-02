package r01f.services.client;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.marshalling.HasModelObjectsMarshaller;
import r01f.marshalling.Marshaller;
import r01f.model.ModelObject;
import r01f.usercontext.UserContext;

/**
 * Base for every sub-api
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class ClientSubAPIBase<S extends ClientAPI,
									   P extends ServiceProxiesAggregator> 
		   implements HasModelObjectsMarshaller {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS (injected by constructor)
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * User context
	 */
	protected final UserContext _userContext;
	/**
	 * Marshaller
	 */
	protected final Marshaller _modelObjectsMarshaller;
	/**
	 * Reference to the client-apis
	 * it's normal that another sub-api must be used from a sub-api
	 */
	protected final S _clientAPIs;
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
	 * @return the user context
	 */
	@SuppressWarnings("unchecked")
	public <U extends UserContext> U getUserContext() {
		return (U)_userContext;
	}
	/**
	 * @return the {@link ModelObject}s {@link Marshaller}
	 */
	public Marshaller getModelObjectsMarshaller() {
		return _modelObjectsMarshaller;
	}
}
