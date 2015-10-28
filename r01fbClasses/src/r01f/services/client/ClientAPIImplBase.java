package r01f.services.client;

import lombok.experimental.Accessors;
import r01f.marshalling.Marshaller;
import r01f.model.annotations.ModelObjectsMarshaller;
import r01f.usercontext.UserContext;

import com.google.inject.Inject;



/**
 * Base type for every API implementation 
 */
@Accessors(prefix="_")
public abstract class ClientAPIImplBase<S extends ServiceProxiesAggregator> 
           implements ClientAPI {
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTION OF AUX OBJECT (see R01MClientAPIGuiceModule)
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * XML/Json<-->java marshaller
	 */
	@ModelObjectsMarshaller		// marshaller xml<->java 
	@Inject protected Marshaller _marshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR INJECTED
/////////////////////////////////////////////////////////////////////////////////////////
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
	public ClientAPIImplBase(final UserContext userContext,
						 final S servicesProxiesAggregator) {
		_userContext = userContext;
		_serviceProxiesAggregator = servicesProxiesAggregator;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
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
	@Override
	public Marshaller getModelObjectsMarshaller() {
		return _marshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <A extends ClientAPI> A as(final Class<A> type) {
		return (A)this;
	}
}