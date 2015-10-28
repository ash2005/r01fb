package r01f.services.client.servicesproxy.bean;


import java.util.Collection;

import lombok.RequiredArgsConstructor;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.jobs.EnqueuedJob;
import r01f.services.interfaces.IndexServicesForModelObject;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.usercontext.UserContext;

/**
 * Service proxy that just delegates to the service impl bean
 */
@RequiredArgsConstructor
public abstract class BeanIndexServicesProxyBase<O extends OID,M extends IndexableModelObject<O>>
           implements IndexServicesForModelObject<O,M>,
  			   		  ProxyForBeanImplementedService {		// it's a bean implementation of the R01MServiceInterface

/////////////////////////////////////////////////////////////////////////////////////////
// 	DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	protected final IndexServicesForModelObject<O,M> _indexServices;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILITY METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <T extends IndexServicesForModelObject<O,M>> T indexServicesAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_indexServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public EnqueuedJob index(final UserContext userContext,
							 final M modelObject) {
		return _indexServices.index(userContext,
								    modelObject);
	}
	@Override
	public EnqueuedJob updateIndex(final UserContext userContext,
							 	   final M modelObject) {
		return _indexServices.updateIndex(userContext,
								   		  modelObject);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  REMOVE FROM INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob removeFromIndex(final UserContext userContext,
							   		   final O oid) {
		return _indexServices.removeFromIndex(userContext,
									 		  oid);
	}
	@Override
	public EnqueuedJob removeAllFromIndex(final UserContext userContext) {
		return _indexServices.removeAllFromIndex(userContext);
	}
	@Override
	public EnqueuedJob removeAllFromIndex(final UserContext userContext,
								  		  final Collection<O> all) {
		return _indexServices.removeAllFromIndex(userContext,
												 all);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RE-INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EnqueuedJob reIndex(final UserContext userContext,
							   final O oid) {
		return _indexServices.reIndex(userContext,
								      oid);
	}
	@Override
	public EnqueuedJob reIndexAll(final UserContext userContext,
								final Collection<O> all) {
		return _indexServices.reIndexAll(userContext,
									   all);
	}
	@Override
	public EnqueuedJob reIndexAll(final UserContext userContext) {
		return _indexServices.reIndexAll(userContext);
	}
}
