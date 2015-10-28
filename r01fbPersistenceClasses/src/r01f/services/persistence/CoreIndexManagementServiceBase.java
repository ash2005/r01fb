package r01f.services.persistence;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.jobs.EnqueuedJob;
import r01f.model.jobs.EnqueuedJobStatus;
import r01f.model.jobs.SuppliesJobOID;
import r01f.persistence.index.IndexManager;
import r01f.services.delegates.ServicesDelegateProvider;
import r01f.services.interfaces.IndexManagementServices;
import r01f.services.interfaces.ServiceInterface;
import r01f.usercontext.UserContext;


/**
 * Implements {@link IndexManagementServices} 
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class CoreIndexManagementServiceBase
              extends CoreServiceBase					  
           implements IndexManagementServices,
           			  SuppliesJobOID {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final IndexManager _indexManager;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected ServicesDelegateProvider<? extends ServiceInterface> getDelegateProvider() {
		throw new UnsupportedOperationException();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ServiceHandler
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void start() {
		this.openIndex(null);	// no user context
	}
	@Override
	public void stop() {
		this.closeIndex(null);	// no user context
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob openIndex(final UserContext userContext) {
		try {
			_indexManager.open(userContext);
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_OK);
		} catch (Throwable th) {
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_ERROR,
								   th.getMessage()); 
		}
	}
	@Override
	public EnqueuedJob closeIndex(final UserContext userContext) {
		try {
			_indexManager.close(userContext);
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_OK);
		} catch (Throwable th) {
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_ERROR,
								   th.getMessage()); 
		}
	}
	@Override
	public EnqueuedJob optimizeIndex(final UserContext userContext) {
		try {
			_indexManager.optimize(userContext);
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_OK);
		} catch (Throwable th) {
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_ERROR,
								   th.getMessage()); 
		}
	}
	@Override
	public EnqueuedJob truncateIndex(final UserContext userContext) {
		try {
			_indexManager.truncate(userContext);
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_OK);
		} catch (Throwable th) {
			return new EnqueuedJob(this.supplyJobOID(),
								   EnqueuedJobStatus.FINALIZED_ERROR,
								   th.getMessage()); 
		}
	}

	
}
