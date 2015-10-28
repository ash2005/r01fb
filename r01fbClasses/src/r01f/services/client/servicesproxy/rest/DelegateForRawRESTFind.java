package r01f.services.client.servicesproxy.rest;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.model.PersistableModelObject;
import r01f.persistence.FindOIDsResult;
import r01f.persistence.FindResult;
import r01f.persistence.FindSummariesResult;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;

@Slf4j
public class DelegateForRawRESTFind<O extends OID,M extends PersistableModelObject<O>>
     extends DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final RESTResponseToFindOIDsResultMapper<O,M> _responseToFindOIDsResultMapper;
	private final RESTResponseToFindResultMapper<O,M> _responseToFindResultMapper;
	private final RESTResponseToFindSummariesResultMapper<O,M> _responseToFindSummariesResultMapper;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DelegateForRawRESTFind(final Marshaller marshaller,
								  final Class<M> modelObjectType) {
		super(marshaller);
		_responseToFindOIDsResultMapper = new RESTResponseToFindOIDsResultMapper<O,M>(marshaller,
																					  modelObjectType);
		_responseToFindResultMapper = new RESTResponseToFindResultMapper<O,M>(marshaller,
																			  modelObjectType);
		_responseToFindSummariesResultMapper = new RESTResponseToFindSummariesResultMapper<O,M>(marshaller,
																								modelObjectType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected RESTResponseToFindOIDsResultMapper<O,M> getResponseToFindOIDsResultMapper() {
		return _responseToFindOIDsResultMapper;
	}
	protected RESTResponseToFindResultMapper<O,M> getResponseToFindResultMapper() {
		return _responseToFindResultMapper;
	}
	protected RESTResponseToFindSummariesResultMapper<O,M> getResponseToFindSummariesResultMapper() {
		return _responseToFindSummariesResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND OIDs
/////////////////////////////////////////////////////////////////////////////////////////
	public FindOIDsResult<O> doFindOids(final UserContext userContext,
										 final SerializedURL restResourceUrl) {
		// do the http call
		String userContextXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.GET(restResourceUrl,
												 		   userContextXml);
		// map the response
		FindOIDsResult<O> outResponse = this.getResponseToFindOIDsResultMapper()
													.mapHttpResponseForOids(userContext,
																			restResourceUrl,httpResponse); 
		// log & return
		_logOidsResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND Entities
/////////////////////////////////////////////////////////////////////////////////////////
	public FindResult<M> doFindEntities(final UserContext userContext,
										final SerializedURL restResourceUrl) {
		// do the http call
		String userContextXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.GET(restResourceUrl,
												 		   userContextXml);
		// map the response
		FindResult<M> outResponse = this.getResponseToFindResultMapper()
											.mapHttpResponseForEntities(userContext,
														   				restResourceUrl,httpResponse); 
		// log & return
		_logEntitiesResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND Summaries
/////////////////////////////////////////////////////////////////////////////////////////
	public FindSummariesResult<M> doFindSummaries(final UserContext userContext,
												  final SerializedURL restResourceUrl) {
		SerializedURL theRESResourceURL;
		if (!restResourceUrl.containsQueryStringParam("summarized")) {
			theRESResourceURL = SerializedURL.of(restResourceUrl.asString())				// clone
											 .addQueryStringParam("summarized","true");	// add param
		} else {
			theRESResourceURL = restResourceUrl;
		}
		// do the http call
		String userContextXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.GET(theRESResourceURL,
												 		   userContextXml);
		// map the response
		FindSummariesResult<M> outResponse = this.getResponseToFindSummariesResultMapper()
													.mapHttpResponseForSummaries(userContext,
																   				 theRESResourceURL,httpResponse); 
		// log & return
		_logSummariesResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LOG
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _logOidsResponse(final SerializedURL restResourceUrl,
						   	    	final FindOIDsResult<O> opResult) {
		if (opResult.hasSucceeded()) {
			log.info("Successful REST find oids operation at resoure with path={} > ({} results)",restResourceUrl,opResult.getOrThrow().size());
		}
		else if (opResult.asError().wasBecauseClientCouldNotConnectToServer()) {			// as(FindOIDsError.class)
			log.error("Client cannot connect to REST endpoint with path={}",restResourceUrl);
		} else if (!opResult.asError().wasBecauseAClientError()) {							// as(FindOIDsError.class)
			log.error("REST: On requesting the find oids operation, the REST resource with path={} returned an error code={}",
					  restResourceUrl,opResult.asError().getErrorType().getCode());			// as(FindOIDsError.class)					
			log.debug("[ERROR]: {}",opResult.getDetailedMessage());
		} else {
			log.debug("Client error on requesting the {} operation, the REST resource with path={} returned: {}",
					  opResult.getRequestedOperationName(),restResourceUrl,opResult.getDetailedMessage());
		}
	}
	protected void _logEntitiesResponse(final SerializedURL restResourceUrl,
						   	    		final FindResult<M> opResult) {
		if (opResult.hasSucceeded()) {
			log.info("Successful REST find entities operation at resoure with path={} > ({} results)",restResourceUrl,opResult.getOrThrow().size());
		}
		else if (opResult.asError().wasBecauseClientCouldNotConnectToServer()) {		// as(FindError.class)
			log.error("Client cannot connect to REST endpoint with path={}",restResourceUrl);
		} else if (!opResult.asError().wasBecauseAClientError()) {						// as(FindError.class)
			log.error("REST: On requesting the find oids operation, the REST resource with path={} returned an error code={}",
					  restResourceUrl,opResult.asError().getErrorType().getCode());		// as(FindError.class)				
			log.debug("[ERROR]: {}",opResult.getDetailedMessage());
		} else {
			log.debug("Client error on requesting the {} operation, the REST resource with path={} returned: {}",
					  opResult.getRequestedOperationName(),restResourceUrl,opResult.getDetailedMessage());
		}
	}
	protected void _logSummariesResponse(final SerializedURL restResourceUrl,
						   	    		 final FindSummariesResult<M> opResult) {
		if (opResult.hasSucceeded()) {
			log.info("Successful REST find summaries operation at resoure with path={} > ({} results)",restResourceUrl,opResult.getOrThrow().size());
		}
		else if (opResult.asError().wasBecauseClientCouldNotConnectToServer()) {		// as(FindSummariesError.class)
			log.error("Client cannot connect to REST endpoint with path={}",restResourceUrl);
		} else if (!opResult.asError().wasBecauseAClientError()) {						// as(FindSummariesError.class)
			log.error("REST: On requesting the find oids operation, the REST resource with path={} returned an error code={}",
					  restResourceUrl,opResult.asError().getErrorType().getCode());		// as(FindSummariesError.class)				
			log.debug("[ERROR]: {}",opResult.getDetailedMessage());
		} else {
			log.debug("Client error on requesting the {} operation, the REST resource with path={} returned: {}",
					  opResult.getRequestedOperationName(),restResourceUrl,opResult.getDetailedMessage());
		}
	}
}
