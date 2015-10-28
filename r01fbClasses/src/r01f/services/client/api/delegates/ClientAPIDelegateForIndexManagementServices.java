package r01f.services.client.api.delegates;

import r01f.model.jobs.EnqueuedJob;
import r01f.services.interfaces.IndexManagementServices;
import r01f.usercontext.UserContext;

/**
 * Adapts Index management API method invocations to the service proxy that performs the core method invocations
 */
public final class ClientAPIDelegateForIndexManagementServices<S extends IndexManagementServices>
	 	   extends ClientAPIServiceDelegateBase<S> {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIDelegateForIndexManagementServices(final UserContext userContext,
								   			final S services) {
		super(userContext,
			  services);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Opens the index
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob openIndex() {
		return this.getServiceProxy()
						.openIndex(this.getUserContext());
	}
	/**
	 * Closes the index
	 * @param userContext
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob closeIndex() {
		return this.getServiceProxy()
						.closeIndex(this.getUserContext());		
	}
	/**
	 * Optimizes the index
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob optimizeIndex() {
		return this.getServiceProxy()
						.optimizeIndex(this.getUserContext());
	}
	/**
	 * Truncates the index removing all records
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob truncateIndex() {
		return this.getServiceProxy()
						.truncateIndex(this.getUserContext());
	}
}
