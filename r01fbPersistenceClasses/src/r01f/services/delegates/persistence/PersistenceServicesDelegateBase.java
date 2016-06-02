package r01f.services.delegates.persistence;

import javax.persistence.EntityManager;

import com.google.common.eventbus.EventBus;

import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.marshalling.HasModelObjectsMarshaller;
import r01f.marshalling.Marshaller;
import r01f.persistence.db.DBBase;
import r01f.persistence.db.HasEntityManager;
import r01f.persistence.db.HasEntityManagerProvider;
import r01f.services.delegates.ServicesDelegateBase;
import r01f.services.interfaces.ServiceInterface;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Accessors(prefix="_")
public abstract class PersistenceServicesDelegateBase
			  extends ServicesDelegateBase
           implements HasEntityManager {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceServicesDelegateBase(final ServiceInterface serviceImpl,
										   final EventBus eventBus) {
		super(serviceImpl,
			  eventBus);
	}
	public PersistenceServicesDelegateBase(final ServiceInterface serviceImpl) {
		this(serviceImpl,
			 null);			// no event bus
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EntityManager getEntityManager() {
		// Get an entity manager from the service
		EntityManager outEntityManager = null;
		if (_serviceImpl instanceof HasEntityManagerProvider) {
			outEntityManager = ((HasEntityManagerProvider)_serviceImpl).getFreshNewEntityManager();
		} else if (_serviceImpl instanceof HasEntityManager) {
			outEntityManager = ((HasEntityManager)_serviceImpl).getEntityManager();
		} else {
			throw new IllegalStateException(Throwables.message("Cannot get an {} from the service impl type {}; it does NOT implements either {} neither {}",
															   EntityManager.class,_serviceImpl.getClass(),HasEntityManager.class,HasEntityManagerProvider.class));
		}
		return outEntityManager;
	}
	public XMLPropertiesForAppComponent getPersistenceProperties() {
		if (!(_serviceImpl instanceof DBBase)) return null;
		return ((DBBase)_serviceImpl).getPersistenceProperties();
	}
	public Marshaller getModelObjectsMarshaller() {
		Marshaller outMarshaller = null;
		if (_serviceImpl instanceof DBBase) {
			outMarshaller = ((DBBase)_serviceImpl).getModelObjectsMarshaller();
		} else if (_serviceImpl instanceof HasModelObjectsMarshaller) {
			outMarshaller = ((HasModelObjectsMarshaller)_serviceImpl).getModelObjectsMarshaller();
		} 
		return outMarshaller;
	}
}
