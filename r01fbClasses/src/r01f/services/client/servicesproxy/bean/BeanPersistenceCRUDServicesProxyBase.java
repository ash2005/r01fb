package r01f.services.client.servicesproxy.bean;


import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDResult;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.usercontext.UserContext;

/**
 * Service proxy that just delegates to the service impl bean
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class BeanPersistenceCRUDServicesProxyBase<O extends OID,M extends PersistableModelObject<O> >
    	   implements CRUDServicesForModelObject<O,M>,
   			   		  ProxyForBeanImplementedService {
/////////////////////////////////////////////////////////////////////////////////////////
//  DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	protected final CRUDServicesForModelObject<O,M> _persistenceServices;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILITY METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <T extends CRUDServicesForModelObject<O,M>> T getPersistenceServicesAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_persistenceServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> load(final UserContext userContext,
							  final O oid) {
		return _persistenceServices.load(userContext,
								         oid);
	}
	@Override
	public CRUDResult<M> create(final UserContext userContext,
								final M entity) {
		return _persistenceServices.create(userContext, 
									       entity);
	}
	@Override
	public CRUDResult<M> update(final UserContext userContext,
								final M entity) {
		return _persistenceServices.update(userContext,
									       entity);
	}
	@Override
	public CRUDResult<M> delete(final UserContext userContext,
								final O oid) {
		return _persistenceServices.delete(userContext,
									       oid);
	}
}
