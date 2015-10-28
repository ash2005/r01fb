package r01f.services.delegates;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.events.HasEventBus;
import r01f.services.interfaces.ServiceInterface;
import r01f.services.persistence.CoreServiceBase;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.common.eventbus.EventBus;

@Accessors(prefix="_")
public abstract class ServicesDelegateBase 
           implements HasEventBus {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The service implementation
	 */
	@Getter protected final ServiceInterface _serviceImpl;
	/**
	 * An event bus to dispatch background jobs
	 */
	@Getter protected final EventBus _eventBus;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesDelegateBase(final ServiceInterface serviceImpl,
								final EventBus eventBus) {
		_serviceImpl = serviceImpl;
		_eventBus = eventBus;
	}
	public ServicesDelegateBase(final ServiceInterface serviceImpl) {
		this(serviceImpl,
			 null);			// no event bus
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public XMLPropertiesForAppComponent getServiceProperties() {
		if (!(_serviceImpl instanceof CoreServiceBase)) {
			return null;
		}
		return ((CoreServiceBase)_serviceImpl).getServiceProperties();
	}
//	@Override
//	public EventBus getEventBus() {
//		if (!(_serviceImpl instanceof HasEventBus)) {
//			return null;
//			//throw new IllegalStateException(Throwables.message("The service impl of type {} does NOT have an EventBus (it does NOT implements {}",
//			//												   _serviceImpl.getClass(),HasEventBus.class));
//		}
//		return ((HasEventBus)_serviceImpl).getEventBus();	// wtf!!!		
//	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked","unused" })
	protected <S extends ServiceInterface> S getServiceImplAs(final Class<S> serviceInterfaceType) {
		return (S)_serviceImpl;
	}
}
