package r01f.services.client.servicesproxy.bean;


import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.FindResult;
import r01f.services.interfaces.FindServicesForVersionableModelObject;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.usercontext.UserContext;

/**
 * Service proxy that just delegates to the service impl bean
 */
public abstract class BeanVersionablePersistenceFindServicesProxyBase<O extends OIDForVersionableModelObject,
																  	  M extends PersistableModelObject<O> & HasVersionableFacet>
		      extends BeanFindServicesProxyBase<O,M>
    	   implements FindServicesForVersionableModelObject<O,M>,
    	   			  ProxyForBeanImplementedService {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public BeanVersionablePersistenceFindServicesProxyBase(final FindServicesForVersionableModelObject<O,M> findServices) {
		super(findServices);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public FindResult<M> findAllVersions(final UserContext userContext) {
		return this.getFindServicesAs(FindServicesForVersionableModelObject.class)
						.findAllVersions(userContext);
	}
}
