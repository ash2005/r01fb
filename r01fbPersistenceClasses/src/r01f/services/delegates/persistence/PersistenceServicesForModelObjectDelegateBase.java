package r01f.services.delegates.persistence;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.events.PersistenceOperationEvents.PersistenceOperationErrorEvent;
import r01f.events.PersistenceOperationEvents.PersistenceOperationOKEvent;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDError;
import r01f.persistence.CRUDOK;
import r01f.persistence.CRUDResult;
import r01f.services.interfaces.ServiceInterfaceForModelObject;
import r01f.usercontext.UserContext;

import com.google.common.eventbus.EventBus;

@Slf4j
@Accessors(prefix="_")
public abstract class PersistenceServicesForModelObjectDelegateBase<O extends OID,M extends PersistableModelObject<O>>
			  extends PersistenceServicesDelegateBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The {@link PersistableModelObject}'s type
	 */
	@Getter protected final Class<M> _modelObjectType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceServicesForModelObjectDelegateBase(final Class<M> modelObjectType,
														 final ServiceInterfaceForModelObject<O,M> serviceImpl,
														 final EventBus eventBus) {
		super(serviceImpl,
			  eventBus);
		_modelObjectType = modelObjectType;
	}
	public PersistenceServicesForModelObjectDelegateBase(final Class<M> modelObjectType,
														 final ServiceInterfaceForModelObject<O,M> serviceImpl) {
		this(modelObjectType,
			 serviceImpl,
			 null);		// no event bus
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	EVENT FIRING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Throws an {@link EventBus} event
	 * @param userContext
	 * @param opResult
	 */
	protected void _fireEvent(final UserContext userContext,
							  final CRUDResult<M> opResult) {
		if (this.getEventBus() == null) {
			log.debug("NO event bus available; CRUD events will NOT be handled");
			return;
		}
		log.debug("Publishing an event of type: {}: ({}) success={}",
				  opResult.getClass(),
				  opResult.getRequestedOperationName(),
				  opResult.hasSucceeded());
		
		if (opResult.hasFailed()) {
			CRUDError<M> opNOK = opResult.asError();		// as(CRUDError.class)
			PersistenceOperationErrorEvent nokEvent = new PersistenceOperationErrorEvent(userContext,
												 					         	 		 opNOK);
			this.getEventBus().post(nokEvent);
			
		} else if (opResult.hasSucceeded()) {
			CRUDOK<M> opOK = opResult.asOK();				// as(CRUDOK.class);
			PersistenceOperationOKEvent okEvent = new PersistenceOperationOKEvent(userContext,
												 					      	  	  opOK);
			this.getEventBus().post(okEvent);
			
		} 
	}
}
