package r01f.services.persistence.delegates.index;


import java.util.Collection;

import javax.persistence.PersistenceException;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.jobs.EnqueuedJob;
import r01f.model.jobs.EnqueuedJobStatus;
import r01f.model.jobs.SuppliesJobOID;
import r01f.persistence.index.Indexer;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.services.interfaces.IndexServicesForModelObject;
import r01f.services.interfaces.ServiceProviders.CRUDServiceByModelObjectOIDTypeProvider;
import r01f.services.interfaces.ServiceProviders.FindServiceByModelObjectTypeProvider;
import r01f.services.persistence.CoreIndexServicesForModelObjectBase;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Throwables;

/**
 * Service layer delegated type for index operations
 */
@Slf4j
public abstract class IndexServicesForModelObjectDelegateBase<O extends OID,M extends IndexableModelObject>
		   implements IndexServicesForModelObject<O,M>,
					  SuppliesJobOID {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Class<M> _modelObjectType;
	protected final Indexer<M> _indexer;
	protected final CRUDServiceByModelObjectOIDTypeProvider _crudServiceByModelObjectOidTypeProvider;
	protected final FindServiceByModelObjectTypeProvider _findServiceByModelObjectTypeProvider;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected IndexServicesForModelObjectDelegateBase(final Class<M> modelObjectType,
													  final CoreIndexServicesForModelObjectBase<O,M> coreServices) {
		_modelObjectType = modelObjectType;
		_indexer = coreServices.getIndexer();
		_crudServiceByModelObjectOidTypeProvider = coreServices.getCRUDServiceByModelObjectOIDTypeProvider();
		_findServiceByModelObjectTypeProvider = coreServices.getFindServiceByModelObjectTypeProvider();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob index(final UserContext userContext,
							 final M modelObject) {
		return _processOne(userContext,
						   modelObject,
						   IndexOperation.INDEX);
	}
	@Override
	public EnqueuedJob updateIndex(final UserContext userContext,
							 	   final M modelObject) {
		return _processOne(userContext,
						   modelObject,
						   IndexOperation.UPDATE_INDEX);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UN-INDEX
/////////////////////////////////////////////////////////////////////////////////////////						   
	@Override
	public EnqueuedJob removeFromIndex(final UserContext userContext,
							   		   final O oid) {
		return _processOne(userContext,
				   		   oid,
				   		   IndexOperation.UNINDEX);
	}
	@Override
	public EnqueuedJob removeAllFromIndex(final UserContext userContext) {
		return _processAll(userContext,
						   IndexOperation.UNINDEX);
	}
	@Override
	public EnqueuedJob removeAllFromIndex(final UserContext userContext,
								  		  final Collection<O> all) {
		return _processAll(userContext,
						   all,
						   IndexOperation.UNINDEX);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RE-INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob reIndex(final UserContext userContext,
							   final O oid) {
		return _processOne(userContext,
						   oid,
						   IndexOperation.REINDEX);
	}
	@Override
	public EnqueuedJob reIndexAll(final UserContext userContext) {
		return _processAll(userContext,
						   IndexOperation.REINDEX);
	}
	@Override
	public EnqueuedJob reIndexAll(final UserContext userContext,
								  final Collection<O> oids) {
		return _processAll(userContext,
						   oids,
						   IndexOperation.REINDEX);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected EnqueuedJob _processOne(final UserContext userContext,
							   		  final M modelObject,
							   		  final IndexOperation operation) {
		// Indexing is a synchronous task that does NOT (for the moment) throw any exception
		EnqueuedJob outJob = null;
		switch(operation) {
		case INDEX:
		case REINDEX:
			_indexer.index(userContext,
						   modelObject);
			break;
		case UPDATE_INDEX:
			_indexer.updateIndex(userContext,
						   		 modelObject);
			break;
		default:
			throw new IllegalStateException();
		}
		outJob = new EnqueuedJob(this.supplyJobOID(),
     			 				 EnqueuedJobStatus.FINALIZED_OK);		// TODO implement in an asynchronous way
		return outJob;
	}
	@SuppressWarnings("unchecked")
	private EnqueuedJob _processOne(final UserContext userContext,
							   		final O oid,
							   		final IndexOperation operation) {
		EnqueuedJob outJob = null;		
		// index / un-index
		// Indexing is a synchronous task that does NOT (for the moment) throw any exception
		switch(operation) {
		case UNINDEX:
			_indexer.removeFromIndex(userContext,
				 	 				 oid);
			break;
		case REINDEX:
			try {
				// Load the model object using a crud that's guessed by the model object's oid type
				CRUDServicesForModelObject<O,?> versionablePersistServices = _crudServiceByModelObjectOidTypeProvider.getFor(oid.getClass());
				M modelObject = (M) versionablePersistServices.load(userContext,
								      		  	  				oid)
								      		  	  		  .getOrThrow();
				// Index
				_indexer.updateIndex(userContext,
						       		 modelObject);
			} catch(PersistenceException persistEx) {
				outJob = new EnqueuedJob(this.supplyJobOID(),
		     	 						 EnqueuedJobStatus.FINALIZED_ERROR,
		     	 						 Throwables.getStackTraceAsString(persistEx));
			}
			break;
		default:
			throw new IllegalStateException();
		}
		outJob = new EnqueuedJob(this.supplyJobOID(),
		     			 		 EnqueuedJobStatus.FINALIZED_OK);		// TODO implement in an asynchronous way
		return outJob;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////

	private EnqueuedJob _processAll(final UserContext userContext,
									final IndexOperation operation) {
		EnqueuedJob outJob = null;
		try {
			// Load all persisted entities oids				
			FindServicesForModelObject<O,?>  findServices= _findServiceByModelObjectTypeProvider.getFor(_modelObjectType);
			Collection<O> all = findServices.findAll(userContext)
											.getOrThrow();
			// index / un-index
			switch(operation) {
			case REINDEX:
				outJob = _processAll(userContext,
						   			 all,
						   			 IndexOperation.REINDEX);
				break;
			case UNINDEX:
				outJob = _processAll(userContext,
						   			 all,
						   			 IndexOperation.UNINDEX);
				break;
			default:
				throw new IllegalStateException();
			}
		} catch(PersistenceException persistEx) {
			outJob = new EnqueuedJob(this.supplyJobOID(),
	     	 						 EnqueuedJobStatus.FINALIZED_ERROR,
	     	 						 Throwables.getStackTraceAsString(persistEx));
		}
		return outJob;
	}
	private EnqueuedJob _processAll(final UserContext userContext,
									final Collection<O> all,
									final IndexOperation operation) {
		EnqueuedJob outJob = null;		
		if (CollectionUtils.isNullOrEmpty(all)) {
			log.warn("NOT {}ing any {} since the provided oid set is null",operation,_modelObjectType);
			outJob = new EnqueuedJob(this.supplyJobOID(),
								     EnqueuedJobStatus.FINALIZED_OK,
								     Strings.customized("NOT reindexing any {} since the provided oid set is null",_modelObjectType));
		} else {
			// TODO implement in an async way WTF!
			log.warn("{}ing all {} records ({} to be processed)",operation,_modelObjectType,all.size());
					
			int i = 1;
			for (O oid : all) {
				log.warn("[{} of {}]: {} a {} record with oid: {}",
						 i,all.size(),operation,_modelObjectType.getSimpleName(),oid);					
				// index / un-index
				switch(operation) {
				case REINDEX:
					_processOne(userContext,
								oid,
								IndexOperation.REINDEX);
					break;
				case UNINDEX:
					_processOne(userContext,
								oid,
								IndexOperation.UNINDEX);
					break;
				default:
					throw new IllegalStateException();
				}
				i++;
			}
			// All OK
			outJob = new EnqueuedJob(this.supplyJobOID(),
				     			 	 EnqueuedJobStatus.FINALIZED_OK);
		}
		return outJob;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected enum IndexOperation {
		INDEX,
		UPDATE_INDEX,
		UNINDEX,
		REINDEX;
	}
}
