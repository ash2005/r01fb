package r01f.services.client.servicesproxy.rest;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpRequestURLEncodedParameter;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchFilterAsCriteriaString;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.services.ServiceProxyException;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;

@Slf4j
public class DelegateForRawRESTSearch<F extends SearchFilter,I extends SearchResultItem>
	 extends DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public DelegateForRawRESTSearch(final Marshaller marshaller) {
		super(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchResults<F,I> doSEARCH(final Url restResourceUrl,
									   final UserContext userContext,
 								       final SearchFilter filter,
 								       final long firstRowNum,final int numberOfRows) {
		log.trace("\t\tSEARCH resource: {}",restResourceUrl);
		
		
		// [1] - Serialize params
		String userContextXml = _marshaller.xmlFromBean(userContext);
		SearchFilterAsCriteriaString filterCriteriaStr = filter.toCriteriaString();
		
		// [2] - Do http request
		HttpResponse httpResponse = null;
		try {
			httpResponse = HttpClient.forUrl(restResourceUrl)		
						             .withHeader("userContext",userContextXml)
									 .withURLParameters(HttpRequestURLEncodedParameter.of(filterCriteriaStr.asString()).withName("filter"),
											   		    HttpRequestURLEncodedParameter.of(firstRowNum).withName("start"),
														HttpRequestURLEncodedParameter.of(numberOfRows).withName("items"))
									 .GET()
									 	.getResponse();
		} catch(IOException ioEx) {
			log.error("Error connecting to {}",restResourceUrl,ioEx);
			throw new ServiceProxyException(ioEx);
		}		
		
		// [2] - De-serialize response
		SearchResults<F,I> outSearchResult = this.mapHttpResponseForSearchResults(userContext,
																			      restResourceUrl,
																			      httpResponse);
		return outSearchResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchResults<F,I> mapHttpResponseForSearchResults(final UserContext userContext,
															  final Url restResourceUrl,
														   	  final HttpResponse httpResponse) {
		SearchResults<F,I> outSearchResults = this.getResponseToResultMapper()
														.mapHttpResponse(userContext,
																   		 restResourceUrl,
																   		 httpResponse,
																   		 SearchResults.class);
		return outSearchResults;
	}
}
