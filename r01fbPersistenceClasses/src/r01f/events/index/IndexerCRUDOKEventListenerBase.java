package r01f.events.index;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import lombok.extern.slf4j.Slf4j;
import r01f.events.PersistenceOperationEvents.PersistenceOperationOKEvent;
import r01f.events.crud.CRUDOperationOKEventListenerBase;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.facets.HasOID;
import r01f.model.jobs.EnqueuedJob;
import r01f.persistence.CRUDOK;
import r01f.services.interfaces.IndexServicesForModelObject;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;

/**
 * Listener to {@link PersistenceOperationOKEvent}s thrown by the persistence layer through the {@link EventBus}
 * @param <M>
 */
@Slf4j
abstract class IndexerCRUDOKEventListenerBase<O extends OID,M extends IndexableModelObject,
											  S extends IndexServicesForModelObject<O,M>> 
       extends CRUDOperationOKEventListenerBase {

/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The indexers are computed only once... later on the cached value is used
	 */
	private final S _indexServices;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public IndexerCRUDOKEventListenerBase(final Class<M> type,
										  final S indexServices) {
		super(type);
		_indexServices = indexServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Subscribe	// subscribes this event listener at the EventBus
	@Override
	public void onPersistenceOperationOK(final PersistenceOperationOKEvent opEvent) {
		// [1] - Check if the event has to be handled
		// 				a) the event refers to the same model object type THIS event handler handles
		//				b) the operation is an update, create or delete operation
		CRUDOK<? extends M> opOK = opEvent.getResultAsCRUDOperationOK();
		boolean hasToBeHandled = opOK.getObjectType() == _type												// a)										
						      && (opOK.hasBeenUpdated() || opOK.hasBeenCreated() || opOK.hasBeenDeleted());		// b)
		
		// [2] - Debug
		if (log.isTraceEnabled()) {
			log.trace(_debugEvent(opEvent,
						  		  hasToBeHandled));
		} else if (log.isDebugEnabled() && hasToBeHandled) {
			log.debug(_debugEvent(opEvent,
						  hasToBeHandled));
		}
		
		// [3] - Handle the event: update the index
		if (hasToBeHandled) {
			_updateIndex(opEvent.getUserContext(),
					     opOK);
		}			
	}
	private String _debugEvent(final PersistenceOperationOKEvent opEvent,
							   final boolean hasToBeHandled) {
		return Strings.of("EventListener registered for events of [{}] entities\n" + 
						  "{}\n" + 
						  "Handle the event: {}")
					  .customizeWith(_type,
							  		 opEvent.debugInfo(),
							  		 hasToBeHandled)
					  .asString();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX & UN_INDEX
/////////////////////////////////////////////////////////////////////////////////////////	
    @SuppressWarnings("unchecked")
	private void _updateIndex(final UserContext userContext,
							  final CRUDOK<? extends M> opOK){
		M entity  = opOK.getOrThrow();		
		if (_indexServices == null) log.warn("Cannot index an instance of {} because the indexer is null!!",entity.getClass());		
		log.info("Updating index for record type {} with oid='{}'",entity.getClass(),entity.asFacet(HasOID.class).getOid());		
		String indexOp = "OP UNKNOWN";
		EnqueuedJob job = null;
		try {
			log.info("\t-{}",_indexServices.getClass());
			if (opOK.hasBeenCreated()) {
				indexOp = "INDEX";
				job = _indexServices.index(userContext,
							   			   entity);	
			} else if (opOK.hasBeenUpdated()) {
				indexOp = "UPDATE INDEX";
				job = _indexServices.updateIndex(userContext,
									 		     entity);
			} else if (opOK.hasBeenDeleted()) {
				indexOp = "DELETE FROM INDEX";
				job = _indexServices.removeFromIndex(userContext,
													 (O)entity.asFacet(HasOID.class).getOid());
			}
			if(job != null) {
				log.info("{} operation OK for type {}: jobOid={}, status={}",
						 indexOp,entity.getClass(),job.getOid(),job.getStatus());
			} else {
				log.info("{} operation OK for type {} : job is NULL!!!",
						 indexOp,entity.getClass());
			}
		} catch(Throwable th) {
			log.error("{} operation NOK for type {}",
					  indexOp,entity.getClass(),
					  th);
			th.printStackTrace(System.out);
		}
	}
}
