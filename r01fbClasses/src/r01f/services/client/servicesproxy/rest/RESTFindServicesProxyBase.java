package r01f.services.client.servicesproxy.rest;

import java.util.Date;

import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.marshalling.Marshaller;
import r01f.model.PersistableModelObject;
import r01f.persistence.FindOIDsResult;
import r01f.services.client.servicesproxy.rest.RESTServicesPathBuilders.ServicesRESTResourcePathBuilderForModelObject;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.types.Range;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;

public abstract class RESTFindServicesProxyBase<O extends OID,M extends PersistableModelObject<O>>
              extends RESTServicesForModelObjectProxyBase<O,M>
           implements FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Delegate
	 */
	protected final DelegateForRawRESTFind<O,M> _findDelegate;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends ServicesRESTResourcePathBuilderForModelObject<O>> 
		   RESTFindServicesProxyBase(final Marshaller marshaller,
								   	 final Class<M> modelObjectType,
								   	 final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
		_findDelegate = new DelegateForRawRESTFind<O,M>(marshaller,
														modelObjectType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public FindOIDsResult<O> findAll(final UserContext userContext) {
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfEntityList());
		return _findDelegate.doFindOids(userContext,
				           				restResourceUrl);
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByCreateDate(final UserContext userContext,
										  	  final Range<Date> createDate) {
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfEntityListByCreateDate(createDate));	
		return _findDelegate.doFindOids(userContext,
				           				restResourceUrl);
	}
	@Override @SuppressWarnings("unchecked")
	public FindOIDsResult<O> findByLastUpdateDate(final UserContext userContext,
											  	  final Range<Date> lastUpdateDate) {
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfEntityListByLastUpdateDate(lastUpdateDate));
		return _findDelegate.doFindOids(userContext,
				           				restResourceUrl);
	}
	@Override
	public FindOIDsResult<O> findByCreator(final UserContext userContext,
									   	   final UserCode creatorUserCode) {
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfEntityListByCreator(creatorUserCode));
		return _findDelegate.doFindOids(userContext,
				           				restResourceUrl);
	}
	@Override
	public FindOIDsResult<O> findByLastUpdator(final UserContext userContext,
										   	   final UserCode lastUpdtorUserCode) {
		SerializedURL restResourceUrl = this.composePersistenceURIFor(this.getServicesRESTResourceUrlPathBuilderAs(ServicesRESTResourcePathBuilderForModelObject.class)
															   			  .pathOfEntityListByLastUpdator(lastUpdtorUserCode));
		return _findDelegate.doFindOids(userContext,
				           				restResourceUrl);
	}
}
