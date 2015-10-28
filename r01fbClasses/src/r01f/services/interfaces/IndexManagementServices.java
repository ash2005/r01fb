package r01f.services.interfaces;

import r01f.inject.ServiceHandler;
import r01f.model.jobs.EnqueuedJob;
import r01f.usercontext.UserContext;


/**
 * Services for managing search engine indexes
 */
public interface IndexManagementServices 
		 extends ServiceInterface,
		 		 ServiceHandler {	// used to start & stop services (see ServletContextListenerBase)
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX MANAGEMENT
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Opens the index
	 * @param userContext
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob openIndex(final UserContext userContext);
	/**
	 * Closes the index
	 * @param userContext
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob closeIndex(final UserContext userContext);
	/**
	 * Optimizes the index
	 * @param userContext
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob optimizeIndex(final UserContext userContext);
	/**
	 * Truncates the index removing all records
	 * @param userContext
	 * @return a job oid that provides a way to later know the job status
	 */
	public EnqueuedJob truncateIndex(final UserContext userContext);
}
