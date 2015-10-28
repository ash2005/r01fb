package r01f.rest.resources.delegates;

import java.net.URI;
import java.util.Date;

import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable;
import r01f.persistence.FindOIDsResult;
import r01f.persistence.PersistenceOperationResult;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;
import r01f.usercontext.UserContext;

/**
 * Base type for REST services that encapsulates the common CRUD ops>
 */
@Accessors(prefix="_")
public abstract class RESTFindDelegateBase<O extends OID,M extends PersistableModelObject<O>> 
	          extends RESTDelegateForModelObjectBase<M> { 
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final FindServicesForModelObject<O,M> _findServices;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <F extends FindServicesForModelObject<O,M>> F getFindServicesAs(@SuppressWarnings("unused") final Class<F> type) {
		return (F)_findServices;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTFindDelegateBase(final Class<M> modelObjectType,
								final FindServicesForModelObject<O,M> findServices) {
		super(modelObjectType);
		_findServices = findServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all persisted model object oids
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public Response findAll(final UserContext userContext,final String resourcePath) {
		FindOIDsResult<O> findResult = _findServices.findAll(userContext);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
														    .at(URI.create(resourcePath))
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids which create date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param createDate
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public Response findByCreateDate(final UserContext userContext,final String resourcePath,
									 final Range<Date> createDate) {
		FindOIDsResult<O> findResult = _findServices.findByCreateDate(userContext,
																	  createDate);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids which last update date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param lastUpdateDate
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public Response findByLastUpdateDate(final UserContext userContext,final String resourcePath,
										 final Range<Date> lastUpdateDate) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdateDate(userContext,
																		 lastUpdateDate);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids created by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param creatorUserCode
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public Response findByCreator(final UserContext userContext,final String resourcePath,
								  final UserCode creatorUserCode) {
		FindOIDsResult<O> findResult = _findServices.findByCreator(userContext,
																   creatorUserCode);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(findResult);
		return outResponse;
	}
	/**
	 * Finds all persisted model object oids last updated by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param lastUpdtorUserCode
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public Response findByLastUpdator(final UserContext userContext,final String resourcePath,
									  final UserCode lastUpdtorUserCode) {
		FindOIDsResult<O> findResult = _findServices.findByLastUpdator(userContext,
																	   lastUpdtorUserCode);
		Response outResponse = RESTOperationsResponseBuilder.findOn(_modelObjectType)
															.at(URI.create(resourcePath))
															.build(findResult);
		return outResponse;
	}
}
