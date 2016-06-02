package r01f.services.client;

import lombok.experimental.Accessors;
import r01f.marshalling.Marshaller;
import r01f.usercontext.UserContext;



/**
 * Base type for every API implementation 
 */
@Accessors(prefix="_")
public abstract class ClientAPIImplBase<S extends ServiceProxiesAggregator> 
           implements ClientAPI {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR INJECTED
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Model objects marshaller
	 */
	protected final Marshaller _modelObjectsMarshaller;
	/**
	 * UserContext 
	 */
	protected final UserContext _userContext;
	/**
	 * Service proxies aggregator 
	 */
	protected final S _serviceProxiesAggregator;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
//	public ClientAPIImplBase(final UserContext userContext,
//						 	 final S servicesProxiesAggregator) {
//		this(userContext,
//			 null,
//			 servicesProxiesAggregator);
//	}	
	public ClientAPIImplBase(final UserContext userContext,
							 final Marshaller modelObjectsMarshaller,
						 	 final S servicesProxiesAggregator) {
		_userContext = userContext;
		_modelObjectsMarshaller = modelObjectsMarshaller;
		_serviceProxiesAggregator = servicesProxiesAggregator;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Marshaller getModelObjectsMarshaller() {
		return _modelObjectsMarshaller;
	}
	@Override @SuppressWarnings("unchecked")
	public <U extends UserContext> U getUserContext() {
		return (U)_userContext;
	}
	@Override @SuppressWarnings("unchecked")
	public <T extends ServiceProxiesAggregator> T getServiceProxiesAggregator() {
		return (T)_serviceProxiesAggregator;
	}
	@Override @SuppressWarnings("unchecked")
	public <T extends ServiceProxiesAggregator> T getServiceProxiesAggregatorAs(final Class<T> aggregatorType) {
		return (T)_serviceProxiesAggregator;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <A extends ClientAPI> A as(final Class<A> type) {
		return (A)this;
	}
}