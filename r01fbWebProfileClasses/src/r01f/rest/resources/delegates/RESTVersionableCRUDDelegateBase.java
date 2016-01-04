package r01f.rest.resources.delegates;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.guids.OIDs;
import r01f.guids.VersionIndependentOID;
import r01f.guids.VersionOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDOnMultipleResult;
import r01f.persistence.CRUDResult;
import r01f.persistence.PersistenceException;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.services.interfaces.CRUDServicesForVersionableModelObject;
import r01f.usercontext.UserContext;

/**
 * Base type for REST services that encapsulates the common CRUD ops
 * @param <M>
 * @param <F>
 * @param <I>
 */
@Accessors(prefix="_")
public class RESTVersionableCRUDDelegateBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet> 
     extends RESTCRUDDelegateBase<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTVersionableCRUDDelegateBase(final Class<M> modelObjectType,
									   	   final CRUDServicesForModelObject<O,M> persistenceServices) {
		super(modelObjectType,
			  persistenceServices);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads the entity with the oid and version provided
	 * @param userContext
	 * @param resourcePath
	 * @param oid
	 * @param version
	 * @return
	 */
	public Response load(final UserContext userContext,final String resourcePath,
						 final VersionIndependentOID oid,final VersionOID version) {
		O vOid = OIDs.createOIDForVersionableModelObject(_modelObjectType,
														 oid,version);
		return super.load(userContext,resourcePath,
				   		  vOid);
	}
	/**
	 * Loads active version at a provided date
	 * @param userContext
	 * @param resourcePath
	 * @param oid
	 * @param date
	 * @return
	 * @throws PersistenceException 
	 */
	@SuppressWarnings("unchecked")
	public Response loadActiveVersionAt(final UserContext userContext,final String resourcePath,
						 				final VersionIndependentOID oid,final Date date) throws PersistenceException {
		CRUDResult<M> loadResult = this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
											.loadActiveVersionAt(userContext,
												  				 oid,date);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(loadResult);
		return outResponse;
	}
	/**
	 * Loads work version 
	 * @param userContext
	 * @param resourcePath
	 * @param oid
	 * @return
	 * @throws PersistenceException 
	 */
	@SuppressWarnings("unchecked")
	public Response loadWorkVersion(final UserContext userContext,final String resourcePath,
						 		    final VersionIndependentOID oid) throws PersistenceException {
		CRUDResult<M> loadResult = this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
												.loadWorkVersion(userContext,
												       	   		 oid);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(loadResult);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads the entity with the oid and version provided
	 * @param userContext
	 * @param resourcePath
	 * @param oid
	 * @param version
	 * @return
	 */
	public Response delete(final UserContext userContext,final String resourcePath,
						   final VersionIndependentOID oid,final VersionOID version) {
		O vOid = OIDs.createOIDForVersionableModelObject(_modelObjectType,
														 oid,version);
		return super.delete(userContext,resourcePath,
				   		    vOid);
	}
	/**
	 * Removes all db record versions
	 * @param userContext
	 * @param resourcePath
	 * @param entityOid
	 * @param version
	 * @return
	 * @throws PersistenceException 
	 */
	@SuppressWarnings("unchecked")
	public Response deleteAllVersions(final UserContext userContext,final String resourcePath,
						   			  final VersionIndependentOID entityOid) throws PersistenceException {
		CRUDOnMultipleResult<M> deleteResults = this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
																	.deleteAllVersions(userContext,
																				       entityOid);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(deleteResults);	
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Activates a version
	 * @param userContext
	 * @param resourcePath
	 * @param entityToBeActivated
	 * @return
	 * @throws PersistenceException 
	 */
	@SuppressWarnings("unchecked")
	public Response activateVersion(final UserContext userContext,final String resourcePath,
						 			final M entityToBeActivated) throws PersistenceException {
		CRUDResult<M> activationResult = this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
													.activate(userContext,
															  entityToBeActivated);
		Response outResponse = RESTOperationsResponseBuilder.crudOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(activationResult);
		return outResponse;
	}
}
