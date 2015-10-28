package r01f.events.crud;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.events.PersistenceOperationEventListeners.PersistenceOperationOKEventListener;
import r01f.events.PersistenceOperationEvents.PersistenceOperationOKEvent;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.metadata.ModelObjectTypeMetaData;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;

import com.google.common.eventbus.EventBus;

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
}
