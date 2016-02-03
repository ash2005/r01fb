package r01f.services.client.servicesproxy.rest;

import java.util.Date;

import r01f.guids.VersionIndependentOID;
import r01f.httpclient.HttpResponse;
import r01f.marshalling.Marshaller;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDOnMultipleResult;
import r01f.persistence.CRUDResult;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.services.client.servicesproxy.rest.RESTServiceResourceUrlPathBuilders.RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase;
import r01f.services.interfaces.CRUDServicesForVersionableModelObject;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;

public abstract class RESTServicesForVersionableCRUDServicesProxyBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet>
              extends RESTServicesForDBCRUDProxyBase<O,M>
    	   implements CRUDServicesForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public <P extends RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase<O>>
		   RESTServicesForVersionableCRUDServicesProxyBase(final Marshaller marshaller,
										   	    		   final Class<M> modelObjectType,
										   	    		   final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder,
			  new RESTResponseToCRUDResultMapperForVersionableModelObject<O,M>(marshaller,
																			   modelObjectType));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private RESTResponseToCRUDResultMapperForVersionableModelObject<O,M> getResponseToCRUDResultMapperForVersionableModelObject() {
		return (RESTResponseToCRUDResultMapperForVersionableModelObject<O,M>)_responseToCRUDResultMapper;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> loadActiveVersionAt(final UserContext userContext,
						   			   		 final VersionIndependentOID oid,final Date date) {
		// do the http call: GET the version whose activation date is the provided one
		Url restResourceUrl = null;
		if (date != null) {
			this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase.class)
								   .pathOfActiveVersionAt(oid,date));	// version active at the provided date
		} else {
			this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase.class)
								   .pathOfActiveVersion(oid));			// currently active version
		}
		String ctxXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.GET(restResourceUrl,
										 				   ctxXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForVersionableModelObject()
											.mapHttpResponseForEntity(userContext,
															  		  PersistenceRequestedOperation.LOAD,
															  		  oid,date,
															  		  restResourceUrl,httpResponse); 
		return outResponse;
	}
	@Override 
	public CRUDResult<M> loadWorkVersion(final UserContext userContext,
							 			 final VersionIndependentOID oid) {
		// do the http call: GET the version whose activation date is NULL -it's not active-
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase.class)
															   .pathOfWorkVersion(oid));
		String ctxXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.GET(restResourceUrl,
										 				   ctxXml);
		// map the response
		CRUDResult<M> outResponse = this.getResponseToCRUDResultMapperForVersionableModelObject()
												.mapHttpResponseForEntity(userContext,
															  			  PersistenceRequestedOperation.LOAD,
															  			  oid,null,
															  			  restResourceUrl,httpResponse); 
		// log & return 
		_logResponse(restResourceUrl,outResponse);
		return outResponse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDOnMultipleResult<M> deleteAllVersions(final UserContext userContext,
													  	   	 	 final VersionIndependentOID oid) {
		// do the http call
		Url restResourceUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase.class)
															   .pathOfAllVersions(oid));
		String userContextXml = _marshaller.xmlFromBean(userContext);
		HttpResponse httpResponse = DelegateForRawREST.DELETE(restResourceUrl,
															  userContextXml);
		// map the response
		CRUDOnMultipleResult<M> outResults = this.getResponseToCRUDResultMapperForVersionableModelObject()
																.mapHttpResponseOnMultipleEntity(userContext,
												   					     	 		  			 PersistenceRequestedOperation.DELETE,
												   					     	 		  			 oid,	
												   					     	 		  			 restResourceUrl,httpResponse);
		return outResults;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ACTIVATION
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public CRUDResult<M> activate(final UserContext userContext,
								  final M entityToBeActivated) {
		// do the http call: a CREATION (POST) of the entiy at the /versions/activeVersion resource path
		Url restResourceVersionUrl = this.composeURIFor(this.getServicesRESTResourceUrlPathBuilderAs(RESTServiceResourceUrlPathBuilderForVersionableModelObjectPersistenceBase.class)
																				.pathOfActiveVersion(entityToBeActivated.getOid().getOid()));	// Version independent oid
		String userContextXml = _marshaller.xmlFromBean(userContext);
		String entityXml = _marshaller.xmlFromBean(entityToBeActivated);
		HttpResponse httpResponse = DelegateForRawREST.POST(restResourceVersionUrl,
										 				    userContextXml,
										 				    entityXml);	// Empty PUT
		// map the response
		CRUDResult<M> outResult = this.getResponseToCRUDResultMapperForVersionableModelObject()
											.mapHttpResponseForEntity(userContext,
														    		  PersistenceRequestedOperation.CREATE,
														    		  restResourceVersionUrl,httpResponse)
													.identifiedOnErrorBy(entityToBeActivated.getOid());
		// log & return 
		_logResponse(restResourceVersionUrl,outResult);
		return outResult;
	}
}
