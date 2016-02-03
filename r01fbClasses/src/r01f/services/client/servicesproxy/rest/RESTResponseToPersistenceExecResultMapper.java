package r01f.services.client.servicesproxy.rest;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.marshalling.MarshallerException;
import r01f.persistence.PersistenceErrorType;
import r01f.persistence.PersistenceException;
import r01f.persistence.PersistenceOperationExecResult;
import r01f.persistence.PersistenceOperationExecResultBuilder;
import r01f.services.ServiceProxyException;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;

@Slf4j
public class RESTResponseToPersistenceExecResultMapper {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Marshaller _marshaller;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTResponseToPersistenceExecResultMapper(final Marshaller marshaller) {
		_marshaller = marshaller;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Maps the entity contained in the {@link HttpResponse}
	 * @param userContext
	 * @param restResourceUrl
	 * @param httpResponse
	 * @param Class<T> expectedType
	 * @return
	 * @throws PersistenceException
	 */
	public <T> PersistenceOperationExecResult<T> mapHttpResponse(final UserContext userContext,
															 	 final Url restResourceUrl,final HttpResponse httpResponse) {
		PersistenceOperationExecResult<T> outResult = null;
		if (httpResponse.isSuccess()) {
			outResult = _mapHttpResponseForSuccess(userContext,
												   restResourceUrl,httpResponse);
		} else {
			outResult = _mapHttpResponseForError(userContext,
												 restResourceUrl,httpResponse);
		}
		return outResult;
	}
	@SuppressWarnings({ "unused" })
	protected <T> PersistenceOperationExecResult<T> _mapHttpResponseForSuccess(final UserContext userContext,
												   	   			  		   	   final Url restResourceUrl,final HttpResponse httpResponse) {
		PersistenceOperationExecResult<T> outOperationResult = null;
		
		// [0] - Load the response		
		String responseStr = httpResponse.loadAsString();		// DO not move!!
		if (Strings.isNullOrEmpty(responseStr)) throw new ServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		// [1] - Map the response
		outOperationResult = _marshaller.beanFromXml(responseStr);
		
		// [2] - Return
		return outOperationResult;
	}
	protected <T> PersistenceOperationExecResult<T> _mapHttpResponseForError(final UserContext userContext,
																			 final Url restResourceUrl,final HttpResponse httpResponse) {
		PersistenceOperationExecResult<T> outOpError = null;
		
		// [0] - Load the http response text
		String responseStr = httpResponse.loadAsString();
		if (Strings.isNullOrEmpty(responseStr)) throw new ServiceProxyException(Throwables.message("The REST service {} worked BUT it returned an EMPTY RESPONSE. This is a developer mistake! It MUST return the target entity data",
															   									   restResourceUrl));
		
		// [1] - Server error (the request could NOT be processed)
		if (httpResponse.isServerError()) {
			outOpError = PersistenceOperationExecResultBuilder.using(userContext)
															  .notExecuted("unknown operation")
															  .because(responseStr,
																	   PersistenceErrorType.SERVER_ERROR);
		}
		// [2] - Error while request processing: the PersistenceOperationExecError comes INSIDE the response
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
