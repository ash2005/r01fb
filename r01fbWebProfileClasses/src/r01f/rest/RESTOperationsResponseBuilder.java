package r01f.rest;

import java.net.URI;
import java.util.Collection;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.jobs.EnqueuedJob;
import r01f.model.search.SearchResults;
import r01f.patterns.IsBuilder;
import r01f.persistence.CRUDError;
import r01f.persistence.CRUDOK;
import r01f.persistence.CRUDOnMultipleOK;
import r01f.persistence.CRUDOnMultipleResult;
import r01f.persistence.CRUDResult;
import r01f.persistence.FindOIDsOK;
import r01f.persistence.FindOIDsResult;
import r01f.persistence.FindOK;
import r01f.persistence.FindResult;
import r01f.persistence.FindSummariesOK;
import r01f.persistence.FindSummariesResult;
import r01f.persistence.PersistenceException;
import r01f.persistence.PersistenceOperationExecOK;
import r01f.persistence.PersistenceOperationExecResult;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.services.client.servicesproxy.rest.RESTServicesProxyBase;
import r01f.util.types.collections.CollectionUtils;


/**
 * See {@link RESTServicesProxyBase}
 * Usage:
 * <pre class='brush:java'>
 * 
 * </pre>
 */
public class RESTOperationsResponseBuilder 
  implements IsBuilder {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends OID,M extends PersistableModelObject<O>> RESTCRUDOperationResponseBuilderForModelObjectURIStep<O,M> crudOn(final Class<M> modelObjectType) {
		return new RESTCRUDOperationResponseBuilderForModelObjectURIStep<O,M>(modelObjectType);
	}
	public static <O extends OID,M extends PersistableModelObject<O>> RESTFindOperationResponseBuilderForModelObjectURIStep<O,M> findOn(final Class<M> modelObjectType) {
		return new RESTFindOperationResponseBuilderForModelObjectURIStep<O,M>(modelObjectType);
	}
	public static RESTExecOperationResponseBuilderForModelObjectURIStep executed() {
		return new RESTExecOperationResponseBuilderForModelObjectURIStep();
	}
	public static RESTSearchIndexOperationResponseBuilderForModelObjectURIStep searchIndex() {
		return new RESTSearchIndexOperationResponseBuilderForModelObjectURIStep();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTCRUDOperationResponseBuilderForModelObjectURIStep<O extends OID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		
		public RESTCRUDOperationResponseBuilderForModelObjectResultStep<O,M> at(final URI resourceURI) {
			return new RESTCRUDOperationResponseBuilderForModelObjectResultStep<O,M>(_modelObjectType,
																  					 resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTFindOperationResponseBuilderForModelObjectURIStep<O extends OID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		
		public RESTFindOperationResponseBuilderForModelObjectResultStep<O,M> at(final URI resourceURI) {
			return new RESTFindOperationResponseBuilderForModelObjectResultStep<O,M>(_modelObjectType,
																  				     resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTExecOperationResponseBuilderForModelObjectURIStep {
		
		@SuppressWarnings("static-method")
		public RESTEXECOperationResponseBuilderResultStep at(final URI resourceURI) {
			return new RESTEXECOperationResponseBuilderResultStep(resourceURI);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTSearchIndexOperationResponseBuilderForModelObjectURIStep {
		
		@SuppressWarnings("static-method")
		public RESTSearchIndexOperationResponseBuilderResultStep at(final URI resourceURI) {
			return new RESTSearchIndexOperationResponseBuilderResultStep(resourceURI);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTCRUDOperationResponseBuilderForModelObjectResultStep<O extends OID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		/**
		 * Returns a REST {@link Response} for a CRUD operation
		 * @param persistenceOpResult 
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final CRUDResult<M> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asCRUDError()		// as(PersistenceOperationError.class)
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else if (persistenceOpResult.hasSucceeded()) {
				CRUDOK<M> persistCRUDOK = persistenceOpResult.asCRUDOK();		//as(CRUDOK.class);
				
				if (persistCRUDOK.hasBeenLoaded()) {
					outResponse = Response.ok()
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	  .entity(persistCRUDOK)
									  	  .type(MediaType.APPLICATION_XML_TYPE)
									  	  .build();
				} else if (persistCRUDOK.hasBeenDeleted()) {
					outResponse = Response.ok()
										  .contentLocation(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	  .entity(persistCRUDOK)
									  	  .type(MediaType.APPLICATION_XML_TYPE)
									  	  .build();
				} else if (persistCRUDOK.hasBeenCreated()) {
					outResponse = Response.created(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	  .entity(persistCRUDOK)
									  	  .type(MediaType.APPLICATION_XML_TYPE)
									  	  .build();
				} else if (persistCRUDOK.hasBeenUpdated()) {
					outResponse = Response.ok()
									  	  .contentLocation(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
									  	  .entity(persistCRUDOK)
									  	  .type(MediaType.APPLICATION_XML_TYPE)
									  	  .build();
				} else if (persistCRUDOK.hasNotBeenModified()) {
					outResponse = Response.notModified()	
										  .contentLocation(_resourceURI)
										  .header("x-r01-modelObjType",_modelObjectType.getName())
										  .entity(persistCRUDOK)	
										  .type(MediaType.APPLICATION_XML_TYPE)
									  	  .build();
				} else {
					throw new UnsupportedOperationException(Throwables.message("{} is NOT a supported operation",persistCRUDOK.getRequestedOperation()));
				}
			}
			return outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a CRUD operation
		 * @param persistenceOpResult 
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final CRUDOnMultipleResult<M> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asCRUDOnMultipleError()		//as(PersistenceOperationError.class)
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else {
				CRUDOnMultipleOK<M> multipleCRUDOK = persistenceOpResult.asCRUDOnMultipleOK();		//as(CRUDOnMultipleEntitiesOK.class);
				Collection<CRUDOK<M>> opsOK = multipleCRUDOK.getOperationsOK();
				Collection<CRUDError<M>> opsNOK = multipleCRUDOK.getOperationsNOK();
				
				if (CollectionUtils.isNullOrEmpty(opsOK) && CollectionUtils.hasData(opsNOK)) {			// only errors!
					// Throw the exception for the first error... it'll be mapped by the RESTExceptionMappers REST type mapper
					CRUDError<M> anError = CollectionUtils.pickOneElement(opsNOK);
					anError.throwAsPersistenceException();
					
				} else if (CollectionUtils.hasData(opsOK) && CollectionUtils.isNullOrEmpty(opsNOK)) {	// all ok
					// Non error
					if (multipleCRUDOK.getRequestedOperation() == PersistenceRequestedOperation.DELETE) {
						outResponse = Response.ok()
											  .contentLocation(_resourceURI)
											  .header("x-r01-modelObjType",_modelObjectType.getName())
											  .entity(multipleCRUDOK)
											  .type(MediaType.APPLICATION_XML_TYPE)
											  .build();
					} else {
						throw new UnsupportedOperationException(Throwables.message("{} is NOT a supported operation",multipleCRUDOK.getRequestedOperation()));
					}
				} else {	// some successful ops and some erroneous ops
					// Throw the exception for the first error... it'll be mapped by the RESTExceptionMappers REST type mapper
					CRUDError<M> anError = CollectionUtils.pickOneElement(opsNOK);
					anError.throwAsPersistenceException();
				}
			}
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTFindOperationResponseBuilderForModelObjectResultStep<O extends OID,M extends PersistableModelObject<O>> {
		private final Class<M> _modelObjectType;
		private final URI _resourceURI;
		/**
		 * Returns a REST {@link Response} for a FIND operation
		 * @param persistenceOpResult 
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final FindOIDsResult<O> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asCRUDError()		// as(PersistenceOperationError.class)
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else {
				FindOIDsOK<O> findOK = persistenceOpResult.asCRUDOK();		//as(FindOIDsOK.class);
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .header("x-r01-modelObjType",_modelObjectType.getName())
									  .entity(findOK)
									  .type(MediaType.APPLICATION_XML_TYPE)
									  .build();
			}
			return outResponse;
		}
		public Response build(final FindResult<M> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asFindError()		//as(PersistenceOperationError.class)
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else {
				FindOK<M> findOK = persistenceOpResult.asFindOK();			// as(FindOK.class);
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .header("x-r01-modelObjType",_modelObjectType.getName())
									  .entity(findOK)
									  .type(MediaType.APPLICATION_XML_TYPE)
									  .build();
			}
			return outResponse;
		}
		public Response build(final FindSummariesResult<M> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asCRUDError()		// as(PersistenceOperationError.class)
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else {
				FindSummariesOK<M> findOK = persistenceOpResult.asCRUDOK();	// as(FindSummariesOK.class);
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .header("x-r01-modelObjType",_modelObjectType.getName())
									  .entity(findOK)
									  .type(MediaType.APPLICATION_XML_TYPE)
									  .build();
			}
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EXEC
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTEXECOperationResponseBuilderResultStep {
		private final URI _resourceURI;
		/**
		 * Returns a REST {@link Response} for a core-layer executed persistence operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final PersistenceOperationExecResult<?> persistenceOpResult) throws PersistenceException {
			Response outResponse = null;
			
			// Failed operation
			if (persistenceOpResult.hasFailed()) {
				// Throw the exception... it'll be mapped by the RESTExceptionMappers REST type mapper
				persistenceOpResult.asOperationExecError()
								   .throwAsPersistenceException();	// throw an exception
				
			}
			// Successful operation
			else if (persistenceOpResult.hasSucceeded()) {
				PersistenceOperationExecOK<?> execOK = persistenceOpResult.asOperationExecOK();
				outResponse = Response.ok()
									  .contentLocation(_resourceURI)
									  .entity(execOK)
									  .type(MediaType.APPLICATION_XML_TYPE)
									  .build();
			}
			return outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a core-layer returned object
		 * @param obj
		 * @return
		 */
		public Response build(final Object obj) {
			Response outResponse = null;
			outResponse = Response.ok()
								  .contentLocation(_resourceURI)
								  .entity(obj)
								  .type(MediaType.APPLICATION_XML_TYPE)
								  .build();
			return outResponse;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class RESTSearchIndexOperationResponseBuilderResultStep {
		private final URI _resourceURI;
		
		/**
		 * Returns a REST {@link Response} for a search operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final SearchResults<?,?> searchResults) {
			Response outResponse = Response.ok()
										   .contentLocation(_resourceURI)
										   .entity(searchResults)
										   .build();
			return outResponse;
		}
		/**
		 * Returns a REST {@link Response} for a search operation
		 * @param persistenceOpResult
		 * @return the response
		 * @throws PersistenceException
		 */
		public Response build(final EnqueuedJob job) {
			Response outResponse = Response.ok()
										   .contentLocation(_resourceURI)
										   .entity(job)
										   .build();
			return outResponse;
		}
	}
}
