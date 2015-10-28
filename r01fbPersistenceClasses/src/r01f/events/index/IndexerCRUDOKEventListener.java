package r01f.events.index;

import r01f.events.PersistenceOperationEvents.PersistenceOperationOKEvent;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.services.interfaces.IndexServicesForModelObject;

import com.google.common.eventbus.EventBus;

/**
 * Listener to {@link PersistenceOperationOKEvent}s thrown by the persistence layer through the {@link EventBus}
 * @param <M>
 */
public abstract class IndexerCRUDOKEventListener<O extends OID,M extends IndexableModelObject<O>> 
              extends IndexerCRUDOKEventListenerBase<O,M,
              										 IndexServicesForModelObject<O,M>> {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexerCRUDOKEventListener(final Class<M> type,
									  final IndexServicesForModelObject<O,M> indexServices) {
		super(type,
			  indexServices);
	}
}
