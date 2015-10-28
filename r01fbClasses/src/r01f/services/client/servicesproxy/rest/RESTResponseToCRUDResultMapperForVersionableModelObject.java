package r01f.services.client.servicesproxy.rest;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.VersionIndependentOID;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDError;
import r01f.persistence.CRUDOnMultipleEntitiesError;
import r01f.persistence.CRUDOnMultipleEntitiesOK;
import r01f.persistence.CRUDOnMultipleEntitiesResult;
import r01f.persistence.CRUDResult;
import r01f.persistence.CRUDResultBuilder;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.services.ServiceProxyException;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;

@Slf4j
public class RESTResponseToCRUDResultMapperForVersionableModelObject<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet> 
	 extends RESTResponseToCRUDResultMapperForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToCRUDResultMapperForVersionableModelObject(final Marshaller marshaller,
							    								   final Class<M> modelObjectType) {
		super(marshaller,
			  modelObjectType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MAP RESPONSE
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDResult<M> mapHttpResponseForEntity(final UserContext userContext,
												  final PersistenceRequestedOperation requestedOp,
												  final VersionIndependentOID oid,final Object version,
												  final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		CRUDResult<M> outOperationResult = null;
		if (httpResponse.isSuccess()) {
			outOperationResult = _mapHttpResponseForSuccess(userContext,
															requestedOp,
															restResourceUrl,httpResponse);
		} else {
			outOperationResult = _mapHttpResponseForError(userContext,
														  requestedOp,
														  oid,version,
														  restResourceUrl,httpResponse);
		}
		return outOperationResult;
	}
	private CRUDError<M> _mapHttpResponseForError(final UserContext userContext,
												  final PersistenceRequestedOperation requestedOp,
												  final VersionIndependentOID oid,final Object version,
												  final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		CRUDError<M> outOpError = null;
		
		// [0] - Load the http response text
		String httpResponseString = httpResponse.loadAsString();
		
		// [1] - Cannot connect to server
		if (httpResponse.isNotFound()) {
			outOpError = CRUDResultBuilder.using(userContext)
										  .on(_modelObjectType)
										  .not(requestedOp)													
										  .becauseClientCannotConnectToServer(restResourceUrl)
										 		.about(oid,version);
		} 
		// [2] - Server error (the request could NOT be processed)
		else if (httpResponse.isServerError()) {
			outOpError = CRUDResultBuilder.using(userContext)
										  .on(_modelObjectType)
										  .not(requestedOp)	
										  .becauseServerError(httpResponseString)	// the rest endpoint response is the error as TEXT
										 		.about(oid,version);
		}
		// [3] - Error while request processing: the PersistenceCRUDError comes INSIDE the response
		else {
			outOpError = _marshaller.beanFromXml(httpResponseString);	// the rest endpoint response is a PersistenceCRUDError XML
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOnMultipleEntitiesResult<M> mapHttpResponseOnMultipleEntity(final UserContext userContext,
																		   final PersistenceRequestedOperation requestedOp,
																		   final VersionIndependentOID requestedOid,
																		   final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		CRUDOnMultipleEntitiesResult<M> outOperationsResults = null;
		
		if (httpResponse.isSuccess()) {
			outOperationsResults = _mapHttpResponseForSuccessOnMultipleEntity(userContext, 
																			  requestedOp, 
																			  requestedOid, 
																			  restResourceUrl, 
																			  httpResponse);
		} else {
			outOperationsResults = _mapHttpResponseForErrorOnMultipleEntities(userContext,
														    				  requestedOp,
														    				  requestedOid,
														    				  restResourceUrl,
														    				  httpResponse);
		}
		return outOperationsResults;
	}
	@SuppressWarnings({ "unused" })
	protected CRUDOnMultipleEntitiesOK<M> _mapHttpResponseForSuccessOnMultipleEntity(final UserContext userContext,
															   					   	 final PersistenceRequestedOperation requestedOp,
															   					 	 final VersionIndependentOID requestedOid,
															   					   	 final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		CRUDOnMultipleEntitiesOK<M> outOperationsResults = null;
		
		// [0] - Load the http response text
		String responseStr = httpResponse.loadAsString();		// DO not move!!
		if (Strings.isNullOrEmpty(responseStr)) throw new ServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		// [1] - Map the response
		outOperationsResults = _marshaller.beanFromXml(responseStr);	// Get the REST Service's returned entity: transform from the result string representation (ie xml) to it's object representation

		// [2] - Return
		return outOperationsResults;
	}
	protected CRUDOnMultipleEntitiesError<M> _mapHttpResponseForErrorOnMultipleEntities(final UserContext userContext,
															   	 						final PersistenceRequestedOperation requestedOp,
															   	 						final VersionIndependentOID requestedOid,
															   	 						final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		CRUDOnMultipleEntitiesError<M> outOpError = null;
		
		// [0] - Load the http response text
		String responseStr = httpResponse.loadAsString();		// DO not move!!
		if (Strings.isNullOrEmpty(responseStr)) throw new ServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		
		
		// [1] - Cannot connect to server
		if (httpResponse.isNotFound()) {
			outOpError = CRUDResultBuilder.using(userContext)
									  	  .on(_modelObjectType) 	
									  	  .<M>versionable()
									  	  .not(requestedOp)
									  	  .becauseClientCannotConnectToServer(restResourceUrl)
									  	 	 .about(requestedOid);
		} 
		// [2] - Server error (the request could NOT be processed)
		else if (httpResponse.isServerError()) {
			log.error("REST: server error for {}",restResourceUrl);
			String errorDetail = httpResponse.loadAsString();
			outOpError = CRUDResultBuilder.using(userContext)
									  	  .on(_modelObjectType) 
									  	  .<M>versionable()
									  	  .not(requestedOp)
									  	  .becauseServerError(errorDetail)
									  	 	  .about(requestedOid);
		}
		// [3] - Error while request processing: the PersistenceCRUDError comes INSIDE the response
		else {
			outOpError = _marshaller.beanFromXml(responseStr);	// the rest endpoint response is a PersistenceCRUDError XML
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}
}
