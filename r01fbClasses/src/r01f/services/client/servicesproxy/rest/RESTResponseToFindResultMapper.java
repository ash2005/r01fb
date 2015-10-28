package r01f.services.client.servicesproxy.rest;

import java.util.ArrayList;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.marshalling.MarshallerException;
import r01f.model.PersistableModelObject;
import r01f.persistence.FindError;
import r01f.persistence.FindOK;
import r01f.persistence.FindResult;
import r01f.persistence.FindResultBuilder;
import r01f.services.ServiceProxyException;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;

@Slf4j
class RESTResponseToFindResultMapper<O extends OID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final Marshaller _marshaller;
	private final Class<M> _modelObjectType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToFindResultMapper(final Marshaller marshaller,
										  final Class<M> modelObjectType) {
		_marshaller = marshaller;
		_modelObjectType = modelObjectType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public FindResult<M> mapHttpResponseForEntities(final UserContext userContext,
													final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		FindResult<M> outOperationResult = null;
		if (httpResponse.isSuccess()) {
			outOperationResult = _mapHttpResponseForSuccessFindingEntities(userContext,
																	   	   restResourceUrl,httpResponse);
		} else {
			outOperationResult = _mapHttpResponseForErrorFindigEntities(userContext,
														  				restResourceUrl,httpResponse);
		}
		return outOperationResult;
	}
	@SuppressWarnings({ "unused" })
	protected FindOK<M> _mapHttpResponseForSuccessFindingEntities(final UserContext userContext,
												   	   			  final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		FindOK<M> outOperationResult = null;
		
		// [0] - Load the response		
		String responseStr = httpResponse.loadAsString();		// DO not move!!
		if (Strings.isNullOrEmpty(responseStr)) throw new ServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		// [1] - Map the response
		outOperationResult = _marshaller.beanFromXml(responseStr);
		if (outOperationResult.getOrThrow() == null) outOperationResult.setOperationExecResult(new ArrayList<M>());	// ensure an empty array list for no results
		
		// [2] - Return
		return outOperationResult;
	}
	protected FindError<M> _mapHttpResponseForErrorFindigEntities(final UserContext userContext,
												    			  final SerializedURL restResourceUrl,final HttpResponse httpResponse) {
		FindError<M> outOpError = null;
		
		// [0] - Load the http response text
		String responseStr = httpResponse.loadAsString();
		if (Strings.isNullOrEmpty(responseStr)) throw new ServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		
		// [1] - Server error (the request could NOT be processed)
		if (httpResponse.isServerError()) {
			outOpError = FindResultBuilder.using(userContext)
										  .on(_modelObjectType)
										  .errorFindingEntities()
										  	  		.causedBy(responseStr);
		}
		// [2] - Error while request processing: the FindError comes INSIDE the response
		else {
			try {
				outOpError = _marshaller.beanFromXml(responseStr);	// the rest endpoint response is a PersistenceCRUDError XML
			} catch(MarshallerException mEx) {
				log.error("Error parsing the {} response: {}",restResourceUrl,responseStr);
				throw mEx;
			}
		}
		// [4] - Return the CRUDOperationResult
		return outOpError;
	}
}
