package r01f.services.client.servicesproxy.bean;


import java.util.Date;

import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDOnMultipleEntitiesResult;
import r01f.persistence.CRUDResult;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.services.interfaces.CRUDServicesForVersionableModelObject;
import r01f.usercontext.UserContext;

/**
 * Service proxy that just delegates to the service impl bean
 */
public abstract class BeanVersionablePersistenceCRUDServicesProxyBase<O extends OIDForVersionableModelObject,
																	  M extends PersistableModelObject<O> & HasVersionableFacet>
			  extends BeanPersistenceCRUDServicesProxyBase<O,M>
    	   implements CRUDServicesForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public <P extends CRUDServicesForModelObject<O,M> 
	 				& CRUDServicesForVersionableModelObject<O,M>> 
		   BeanVersionablePersistenceCRUDServicesProxyBase(final P persistenceServices) {
		super(persistenceServices);
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  VERSIONABLE CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadActiveVersionAt(final UserContext userContext,
									   		 final VersionIndependentOID oid,final Date date) {
		return this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
				   		.loadActiveVersionAt(userContext,
							   				 oid,
							   				 date);
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadWorkVersion(final UserContext userContext,
										 final VersionIndependentOID oid) {
		return this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
						.loadWorkVersion(userContext,
										 oid);
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDOnMultipleEntitiesResult<M> deleteAllVersions(final UserContext userContext,
													 		 final VersionIndependentOID oid) {
		return this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
							.deleteAllVersions(userContext,
											   oid);
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> activate(final UserContext userContext,
								  final M entityToBeActivated) {
		return this.getPersistenceServicesAs(CRUDServicesForVersionableModelObject.class)
						.activate(userContext,
								  entityToBeActivated);
	}
}
