package r01f.rest.resources.delegates;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.jobs.EnqueuedJob;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.services.interfaces.IndexServicesForModelObject;
import r01f.usercontext.UserContext;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base type for REST services that encapsulates the common search index ops: indexing, searching
 */
@Accessors(prefix="_")
public abstract class RESTIndexDelegateBase<O extends OID,M extends IndexableModelObject<O>> 
              extends RESTDelegateForModelObjectBase<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final IndexServicesForModelObject<O,M> _indexServices;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <T extends IndexServicesForModelObject<O,M>> T indexeServicesAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_indexServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTIndexDelegateBase(final Class<M> modelObjType,
								 final IndexServicesForModelObject<O,M> indexServices) {
		super(modelObjType);
		_indexServices = indexServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Indexes the provided model object
     * @param userContext
	 * @param resourcePath
     * @param modelObject the model object
     * @return a ticket for the enqueued job
     */
	public Response index(final UserContext userContext,final String resourcePath,
						  final M modelObject) {
		EnqueuedJob job = _indexServices.index(userContext,
									      	   modelObject);
		Response outResponse = Response.ok()
									   .entity(job)
									   .build();			
		return outResponse;
	}
    /**
     * Updates the index data for the provided model object
     * @param userContext
	 * @param resourcePath
     * @param modelObject the model object
     * @return a ticket for the enqueued job
     */
	public Response updateIndex(final UserContext userContext,final String resourcePath,
							 	final M modelObject) {
		EnqueuedJob job = _indexServices.updateIndex(userContext,
											    	 modelObject);
		Response outResponse  = RESTOperationsResponseBuilder.searchIndex()
															 .at(URI.create(resourcePath))
															 .build(job);			
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  REMOVE FROM INDEX
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Un-Indexes the model object whose oid is provided
     * @param userContext
	 * @param resourcePath
     * @param oid the model object's oid
     * @return a ticket for the enqueued job
     */
	public Response removeFromIndex(final UserContext userContext,final String resourcePath,
							   		final O oid) {
		EnqueuedJob job = _indexServices.removeFromIndex(userContext,
											   	    	 oid);
		Response outResponse  = RESTOperationsResponseBuilder.searchIndex()
															 .at(URI.create(resourcePath))
															 .build(job);			
		return outResponse;
	}
	/**
	 * Un-Indexes the model objects provided
	 * @param userContext
	 * @param resourcePath
	 * @param all
	 * @return
	 */
	public Response removeAllFromIndex(final UserContext userContext,final String resourcePath,
							      	   final Collection<O> all) {
		EnqueuedJob job = null;
		if (CollectionUtils.isNullOrEmpty(all)) {
			job = _indexServices.removeAllFromIndex(userContext);
		} else {
			job = _indexServices.removeAllFromIndex(userContext,
												   	all);		
		}
		Response outResponse  = RESTOperationsResponseBuilder.searchIndex()
															 .at(URI.create(resourcePath))
															 .build(job);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  REINDEX	
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Indexes the provided model object oid
     * @param userContext
	 * @param resourcePath
     * @param oid the model object
     * @return a ticket for the enqueued job
     */
	public Response reIndex(final UserContext userContext,final String resourcePath,
						    final O oid) {
		EnqueuedJob job = _indexServices.reIndex(userContext,
									      	     oid);
		Response outResponse  = RESTOperationsResponseBuilder.searchIndex()
															 .at(URI.create(resourcePath))
															 .build(job);			
		return outResponse;
	}
	/**
	 * Indexes the model objects provided
	 * @param userContext
	 * @param resourcePath
	 * @param all
	 * @return
	 */
	public Response reIndexAll(final UserContext userContext,final String resourcePath,
						       final Collection<O> all) {
		EnqueuedJob job = null;
		if (CollectionUtils.isNullOrEmpty(all)) {
			job = _indexServices.reIndexAll(userContext);
		} else {
			job = _indexServices.reIndexAll(userContext,
											all);
		}
		Response outResponse  = RESTOperationsResponseBuilder.searchIndex()
															 .at(URI.create(resourcePath))
															 .build(job);
		return outResponse;
	}
}
