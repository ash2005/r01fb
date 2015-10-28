package r01f.rest.resources.delegates;

import java.net.URI;

import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDResult;
import r01f.persistence.PersistenceException;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.usercontext.UserContext;

/**
 * Base type for REST services that encapsulates the common CRUD ops>
 */
@Accessors(prefix="_")
public abstract class RESTCRUDDelegateBase<O extends OID,M extends PersistableModelObject<O>> 
	          extends RESTDelegateForModelObjectBase<M> { 
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final CRUDServicesForModelObject<O,M> _persistenceServices;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <C extends CRUDServicesForModelObject<O,M>> C getPersistenceServicesAs(@SuppressWarnings("unused") final Class<C> type) {
		return (C)_persistenceServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTCRUDDelegateBase(final Class<M> modelObjectType,
							    final CRUDServicesForModelObject<O,M> persistenceServices) {
		super(modelObjectType);
		_persistenceServices = persistenceServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PERSISTENCE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads a db entity 
	 * @param userContext
	 * @param resourcePath
	 * @param oid
	 * @return
	 * @throws PersistenceException 
	 */
	public Response load(final UserContext userContext,final String resourcePath,
						 final O oid) throws PersistenceException {
		
		CRUDResult<M> loadResult = _persistenceServices.load(userContext,
									  					     oid);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
														    .at(URI.create(resourcePath))
															.build(loadResult);
		return outResponse;
	}
	/**
	 * Creates a db entity
	 * @param userContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException 
	 */ 
	public Response create(final UserContext userContext,final String resourcePath,
						   final M modelObject) throws PersistenceException {
		CRUDResult<M> createResult = _persistenceServices.create(userContext,
										   	   					 modelObject);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
														    .at(URI.create(resourcePath))
														    .build(createResult);
		return outResponse;
	}
	/**
	 * Updates a db entity
	 * @param userContext
	 * @param resourcePath
	 * @param modelObject
	 * @return
	 * @throws PersistenceException 
	 */ 
	public Response update(final UserContext userContext,final String resourcePath,
						   final M modelObject) throws PersistenceException {
		CRUDResult<M> updateResult = _persistenceServices.update(userContext,
										   	      				 modelObject);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(updateResult);
		return outResponse;
	}
	/**
	 * Removes a db entity
	 * @param userContext
	 * @param resourcePath
	 * @param oid
	 * @return
	 * @throws PersistenceException 
	 */
	public Response delete(final UserContext userContext,final String resourcePath,
						   final O oid) throws PersistenceException {
		CRUDResult<M> deleteResult = _persistenceServices.delete(userContext,
																 oid);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
														    .at(URI.create(resourcePath))
															.build(deleteResult);
		return outResponse;
	}
}
