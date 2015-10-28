package r01f.services.interfaces;

import java.util.Collection;

import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.jobs.EnqueuedJob;
import r01f.usercontext.UserContext;

public interface IndexServicesForModelObject<O extends OID,M extends IndexableModelObject<O>> 
		 extends ServiceInterface {
/////////////////////////////////////////////////////////////////////////////////////////
//  ENTITY INDEX
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Indexes the provided model object
     * @param userContext
     * @param modelObject the model object
     * @return a ticket for the enqueued job
     */
	public EnqueuedJob index(final UserContext userContext,
							 final M modelObject);
    /**
     * Updates the index data for the provided model object
     * @param userContext
     * @param modelObject the model object
     * @return a ticket for the enqueued job
     */
	public EnqueuedJob updateIndex(final UserContext userContext,
							 	   final M modelObject);
/////////////////////////////////////////////////////////////////////////////////////////
//  UN-INDEX
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Un-Indexes the model object whose oid is provided
     * @para userContext
     * @param oid the model object's oid
     * @return a ticket for the enqueued job
     */
	public EnqueuedJob removeFromIndex(final UserContext userContext,
							   		   final O oid);
	/**
	 * Un-Indexes all records 
	 * @param userContext
	 * @return
	 */
	public EnqueuedJob removeAllFromIndex(final UserContext userContext);
	/**
	 * Un-Indexes the model objects provided
	 * @param userContext
	 * @param all
	 * @return
	 */
	public EnqueuedJob removeAllFromIndex(final UserContext userContext,
							      		  final Collection<O> all);
/////////////////////////////////////////////////////////////////////////////////////////
//  RE-INDEXING	
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Updates the index data fir the model object whose oid is provided
     * @param userContext
     * @param oid the model object's oid
     * @return a ticket for the enqueued job
     */
	public EnqueuedJob reIndex(final UserContext userContext,
							   final O oid);
	/**
	 * Indexes the model objects provided
	 * @param userContext
	 * @param all
	 * @return
	 */
	public EnqueuedJob reIndexAll(final UserContext userContext,
								  final Collection<O> all);
	/**
	 * Indexes all records 
	 * @param userContext
	 * @return
	 */
	public EnqueuedJob reIndexAll(final UserContext userContext);

}
