package r01f.services.client.api.delegates;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.marshalling.HasModelObjectsMarshaller;
import r01f.marshalling.Marshaller;
import r01f.model.ModelObject;
import r01f.services.interfaces.ServiceInterface;
import r01f.usercontext.UserContext;

@Accessors(prefix="_")
public abstract class ClientAPIServiceDelegateBase<S extends ServiceInterface> 
		   implements HasModelObjectsMarshaller {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The user context
	 */
	@Getter protected final UserContext _userContext;
	/**
	 * {@link ModelObject}s {@link Marshaller}
	 */
	@Getter protected final Marshaller _modelObjectsMarshaller;
	/**
	 * The service interface 
	 */
	protected final S _serviceProxy;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIServiceDelegateBase(final UserContext userContext,
										final Marshaller modelObjectsMarshaller,
										final S serviceProxy) {
		_userContext = userContext;
		_modelObjectsMarshaller = modelObjectsMarshaller;
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

