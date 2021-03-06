package r01f.services.client.servicesproxy.rest;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.httpclient.HttpClient;
import r01f.httpclient.HttpRequestPayload;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.mime.MimeTypes;
import r01f.model.jobs.EnqueuedJob;
import r01f.persistence.index.IndexManagementCommand;
import r01f.services.ServiceProxyException;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;

@Slf4j
public class DelegateForRawRESTIndexManagement 
     extends DelegateForRawREST {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	public DelegateForRawRESTIndexManagement(final Marshaller marshaller) {
		super(marshaller);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public EnqueuedJob doIndexManagementCommand(final Url restResourceUrl,
							   	  				final UserContext userContext,
							   	  				final IndexManagementCommand indexCmd) {
		log.trace("\t\tINDEX resource: {}",restResourceUrl);
		
		// [1] - Serialize params
		String userContextXml = userContext != null ? _marshaller.xmlFromBean(userContext) : null;
		String dataXml = _marshaller.xmlFromBean(indexCmd);
		
		// [2] - Do http request
		HttpResponse httpResponse = null;
		try {
			// index some records
			if (indexCmd != null) {
				if (Strings.isNOTNullOrEmpty(userContextXml)) {
					httpResponse = HttpClient.forUrl(restResourceUrl)		
								             .withHeader("userContext",userContextXml)								             
								             .PUT()
								             	.withPayload(HttpRequestPayload.wrap(dataXml)
								             								   .mimeType(MimeTypes.APPLICATION_XML))								             
											 .getResponse();
				} else {
					httpResponse = HttpClient.forUrl(restResourceUrl)		
								             .PUT()
								             	.withPayload(HttpRequestPayload.wrap(dataXml)
								             								   .mimeType(MimeTypes.APPLICATION_XML))
											 .getResponse();
				}
			}
			else {
				throw new IllegalArgumentException(Throwables.message("The index resource {} is NOT valid",restResourceUrl));
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
