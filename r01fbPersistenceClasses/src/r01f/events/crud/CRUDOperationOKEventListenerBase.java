package r01f.events.crud;

import com.google.common.eventbus.EventBus;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.events.PersistenceOperationEventListeners.PersistenceOperationOKEventListener;
import r01f.events.PersistenceOperationEvents.PersistenceOperationOKEvent;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;
import r01f.persistence.CRUDOK;
import r01f.persistence.CRUDResult;

/**
 * Listener to {@link PersistenceOperationOKEvent}s thrown by the persistence layer through the {@link EventBus}
 * @param <M>
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class CRUDOperationOKEventListenerBase 
           implements PersistenceOperationOKEventListener {

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The type is needed because guava's event bus does NOT supports generic event types
	 * 	- {@link CRUDOperationEvent} is a generic type parameterized with the persistable model object type, 
	 *  - Subscriptions to the event bus are done by event type, that's by {@link CRUDOperationEvent} type
	 *  - BUT since guava {@link EventBus} does NOT supports generics, the subscriptions are done to the raw {@link CRUDOperationEvent}
	 *  - ... so ALL listeners will be attached to the SAME event type: {@link CRUDOperationEvent}
	 *  - ... and ALL listeners will receive {@link CRUDOperationEvent} events
	 *  - ... but ONLY one should handle it.
	 * In order for the event handler (listener) to discriminate events to handle, the model object's type
	 * is used (see {@link #_hasToBeHandled(CRUDOperationEvent)} method)
	 */
	protected final Class<? extends IndexableModelObject<? extends OID>> _type;
	/**
	 * MetaData about the model object type
	 */
	protected final transient ModelObjectTypeMetaData _modelObjectMetaData;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOperationOKEventListenerBase(final Class<? extends IndexableModelObject<? extends OID>> type) {
		_type = type;
		_modelObjectMetaData = ModelObjectTypeMetaDataBuilder.createFor(_type);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create or update operation 
	 * @param opEvent
	 * @return
	 */
	protected boolean _isEventForSuccessfulCreateOrUpdate(final PersistenceOperationOKEvent opEvent) {
		CRUDResult<? extends IndexableModelObject<? extends OID>> opResult = opEvent.getOperationResult()
									    											.as(CRUDResult.class);
		boolean outHasToBeHandled = opResult.hasSucceeded()																		// the persistence operation has succeeded
							     && opResult.as(CRUDOK.class).getObjectType() == _type											// the event refers to the same model object type THIS event handler handles
								 && (opResult.as(CRUDOK.class).hasBeenCreated() || opResult.as(CRUDOK.class).hasBeenUpdated());	// it's a create or update event
		return outHasToBeHandled;
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful create 
	 * @param opEvent
	 * @return
	 */
	protected boolean _isEventForSuccessfulCreate(final PersistenceOperationOKEvent opEvent) {
		CRUDResult<? extends IndexableModelObject<? extends OID>> opResult = opEvent.getOperationResult()
									    											.as(CRUDResult.class);
		boolean outHasToBeHandled = opResult.hasSucceeded()														// the persistence operation has succeeded
							     && opResult.as(CRUDOK.class).getObjectType() == _type							// the event refers to the same model object type THIS event handler handles
								 && (opResult.as(CRUDOK.class).hasBeenCreated());								// it's a create event
		return outHasToBeHandled;
	}
	/**
	 * Returns true if:
	 * 	1.- the event refers to an object of the type handled by this listener
	 *  2.- the event refers to a successful update operation 
	 * @param opEvent
	 * @return
	 */
	protected boolean _isEventForSuccessfulUpdate(final PersistenceOperationOKEvent opEvent) {
		CRUDResult<? extends IndexableModelObject<? extends OID>> opResult = opEvent.getOperationResult()
									    											.as(CRUDResult.class);
		boolean outHasToBeHandled = opResult.hasSucceeded()														// the persistence operation has succeeded
							     && opResult.as(CRUDOK.class).getObjectType() == _type							// the event refers to the same model object type THIS event handler handles
								 && (opResult.as(CRUDOK.class).hasBeenUpdated());								// it's an update event
		return outHasToBeHandled;
	}
}
