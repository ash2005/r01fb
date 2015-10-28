package r01f.services.client.servicesproxy.rest;

import r01f.marshalling.Marshaller;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.FindResult;
import r01f.services.client.servicesproxy.rest.RESTServicesPathBuilders.ServicesRESTResourcePathBuilderForVersionableModelObjectBase;
import r01f.services.interfaces.FindServicesForVersionableModelObject;
import r01f.usercontext.UserContext;

public abstract class RESTVersionableFindServicesProxyBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet>
              extends RESTFindServicesProxyBase<O,M>
           implements FindServicesForVersionableModelObject<O,M> {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	public <P extends ServicesRESTResourcePathBuilderForVersionableModelObjectBase<O>>
		   RESTVersionableFindServicesProxyBase(final Marshaller marshaller,
											   	final Class<M> modelObjectType,
											   	final P servicesRESTResourceUrlPathBuilder) {
		super(marshaller,
			  modelObjectType,
			  servicesRESTResourceUrlPathBuilder);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindResult<M> findAllVersions(final UserContext userContext) {
		return null;
	}
}
