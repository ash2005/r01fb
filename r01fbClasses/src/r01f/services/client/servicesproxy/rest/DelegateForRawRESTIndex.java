package r01f.services.client.servicesproxy.rest;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpRequestPayload;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.mime.MimeType;
import r01f.model.jobs.EnqueuedJob;
import r01f.services.ServiceProxyException;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;

@Slf4j
public class DelegateForRawRESTIndex 
     extends DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public DelegateForRawRESTIndex(final Marshaller marshaller) {
		super(marshaller);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob index(final Url restResourceUrl,
						     final UserContext userContext,
 							 final Object data) {
		log.trace("\t\tINDEX resource: {}",restResourceUrl);
		
		// [1] - Serialize params
		String userContextXml = _marshaller.xmlFromBean(userContext);
		String dataXml = _marshaller.xmlFromBean(data);
		
		// [2] - Do http request
		HttpResponse httpResponse = null;
		try {
			// index some records
			if (data != null) {
				httpResponse = HttpClient.forUrl(restResourceUrl)		
							             .withHeader("userContext",userContextXml)
							             .POST()
							             	.withPayload(HttpRequestPayload.wrap(dataXml)
							             								   .mimeType(MimeType.APPLICATION_XML))
										 .getResponse();
			}
			// index all records
			else {
				httpResponse = HttpClient.forUrl(restResourceUrl)		
							             .withHeader("userContext",userContextXml)
							             .POST()
							             	.withoutPayload(MimeType.APPLICATION_XML)
										 .getResponse();
			}
		} catch(IOException ioEx) {
			throw new ServiceProxyException(ioEx);
		}
		
		// [3] - De-serialize response
		EnqueuedJob outJob = this.mapHttpResponseForEnqueuedJob(userContext,
																restResourceUrl,
																httpResponse);
		return outJob;
	}
	public EnqueuedJob updateIndex(final Url restResourceUrl,
							   	   final UserContext userContext,
							   	   final Object data) {
		log.trace("\t\tINDEX resource: {}",restResourceUrl);
		
		// [1] - Serialize params
		String userContextXml = _marshaller.xmlFromBean(userContext);
		String dataXml = _marshaller.xmlFromBean(data);
		
		// [2] - Do http request
		HttpResponse httpResponse = null;
		try {
			// index some records
			if (data != null) {
				httpResponse = HttpClient.forUrl(restResourceUrl)		
							             .withHeader("userContext",userContextXml)
							             .PUT()
							             	.withPayload(HttpRequestPayload.wrap(dataXml)
							             								   .mimeType(MimeType.APPLICATION_XML))
										 .getResponse();
			}
			// index all records
			else {
				httpResponse = HttpClient.forUrl(restResourceUrl)		
							             .withHeader("userContext",userContextXml)
							             .PUT()
							             	.withoutPayload(MimeType.APPLICATION_XML)
										 .getResponse();
			}
		} catch(IOException ioEx) {
			throw new ServiceProxyException(ioEx);
		}
		
		// [3] - De-serialize response
		EnqueuedJob outJob = this.mapHttpResponseForEnqueuedJob(userContext,
																restResourceUrl,
																httpResponse);
		return outJob;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UNINDEX
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob removeFromIndex(final Url restResourceUrl,
							   		   final UserContext userContext,
							   		   final Object data) {
		log.trace("\t\tUN INDEX resource: {}",restResourceUrl);
		
		// [1] - Serialize params
		String userContextXml = _marshaller.xmlFromBean(userContext);
		String dataXml = data != null ? _marshaller.xmlFromBean(data) : null;
		
		// [2] - Do http request
		HttpResponse httpResponse = null;
		try {
			// index some records
			if (data != null) {
				httpResponse = HttpClient.forUrl(restResourceUrl)		
							             .withHeader("userContext",userContextXml)
							             .DELETE()
							             		.withPayload(HttpRequestPayload.wrap(dataXml)
							             									   .mimeType(MimeType.APPLICATION_XML))
										 .getResponse();
			}
			// index all records
			else {
				httpResponse = HttpClient.forUrl(restResourceUrl)		
							             .withHeader("userContext",userContextXml)
							             .DELETE()
							             		.withoutPayload(MimeType.APPLICATION_XML)
										 .getResponse();
			}
		} catch(IOException ioEx) {
			throw new ServiceProxyException(ioEx);
		}
		
		// [3] - De-serialize response
		EnqueuedJob outJob = this.mapHttpResponseForEnqueuedJob(userContext,
																restResourceUrl,
																httpResponse);
		return outJob;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob mapHttpResponseForEnqueuedJob(final UserContext userContext,
													 final Url restResourceUrl,
													 final HttpResponse httpResponse) {
		EnqueuedJob outJob = this.getResponseToResultMapper()
										.mapHttpResponse(userContext,
												  		 restResourceUrl,
												  		 httpResponse,
												  		 EnqueuedJob.class);
		return outJob;
	}
}
