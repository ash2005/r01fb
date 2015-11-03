package r01f.services.persistence;

import java.util.Collection;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.model.ModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.jobs.EnqueuedJob;
import r01f.persistence.index.Indexer;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.services.interfaces.FindServicesForModelObject;
import r01f.services.interfaces.IndexServicesForModelObject;
import r01f.services.interfaces.ServiceProviders.CRUDServiceByModelObjectOIDTypeProvider;
import r01f.services.interfaces.ServiceProviders.FindServiceByModelObjectTypeProvider;
import r01f.usercontext.UserContext;


/**
 * Implements the {@link ModelObject} index-related services which in turn are delegated to 
 * a delegated object
 */
@Accessors(prefix="_")
public abstract class CoreIndexServicesForModelObjectBase<O extends OID,M extends IndexableModelObject<O>>
              extends CoreServiceBase					  
           implements IndexServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Indexer<M> _indexer;
	
	protected final CRUDServiceByModelObjectOIDTypeProvider _crudServiceByModelObjectOidTypeProvider;
	protected final FindServiceByModelObjectTypeProvider _findServiceByModelObjectTypeProvider;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public CoreIndexServicesForModelObjectBase(final Indexer<M> indexer,
											   final CRUDServiceByModelObjectOIDTypeProvider crudServiceByModelObjectOidTypeProvider,
											   final FindServiceByModelObjectTypeProvider findServiceByModelObjectTypeProvider) {
		_indexer = indexer;
		_crudServiceByModelObjectOidTypeProvider = crudServiceByModelObjectOidTypeProvider;
		_findServiceByModelObjectTypeProvider = findServiceByModelObjectTypeProvider;
	}
	public <P extends PersistableModelObject<O> & IndexableModelObject<O>> CoreIndexServicesForModelObjectBase(final Indexer<M> indexer,
											   																   final CRUDServicesForModelObject<O,P> crudService,
											   																   final FindServicesForModelObject<O,P> findService) {
		_indexer = indexer;
		_crudServiceByModelObjectOidTypeProvider = new CRUDServiceByModelObjectOIDTypeProvider() {
															@Override @SuppressWarnings("unchecked")
															public <O2 extends OID,M2 extends PersistableModelObject<O2>> CRUDServicesForModelObject<O2,M2> getFor(final Class<? extends OID> type) {
																return (CRUDServicesForModelObject<O2,M2>)crudService;
															}
							   					   };
		_findServiceByModelObjectTypeProvider = new FindServiceByModelObjectTypeProvider() {
															@Override @SuppressWarnings("unchecked")
															public <O2 extends OID,M2 extends PersistableModelObject<O2>> FindServicesForModelObject<O2,M2> getFor(final Class<?> type) {
																return (FindServicesForModelObject<O2,M2>)findService;
															}
												};
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PERSISTENCE ACCESS
/////////////////////////////////////////////////////////////////////////////////////////
	public Indexer<M> getIndexer() {
		return _indexer;
	}
	public CRUDServiceByModelObjectOIDTypeProvider getCRUDServiceByModelObjectOIDTypeProvider() {
		return _crudServiceByModelObjectOidTypeProvider;
	}
	public FindServiceByModelObjectTypeProvider getFindServiceByModelObjectTypeProvider() {
		return _findServiceByModelObjectTypeProvider;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public EnqueuedJob index(final UserContext userContext,
							 final M modelObject) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
								.index(userContext,
									   modelObject);
	}
	@Override @SuppressWarnings("unchecked")
	public EnqueuedJob updateIndex(final UserContext userContext,
							 	   final M modelObject) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
						.updateIndex(userContext,
									 modelObject);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UN-INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public EnqueuedJob removeFromIndex(final UserContext userContext,
							   		   final O oid) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
							.removeFromIndex(userContext,
						 			 		 oid);
	}
	@Override 
	public EnqueuedJob removeAllFromIndex(final UserContext userContext) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
							.removeAllFromIndex(userContext);
	}
	@Override @SuppressWarnings("unchecked")
	public EnqueuedJob removeAllFromIndex(final UserContext userContext,
								  		  final Collection<O> all) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
							.removeAllFromIndex(userContext,
												all);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RE-INDEX
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public EnqueuedJob reIndex(final UserContext userContext,
							 	   final O oid) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
							.reIndex(userContext,
								     oid);
	}
	@Override @SuppressWarnings("unchecked")
	public EnqueuedJob reIndexAll(final UserContext userContext,
								  final Collection<O> all) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
							.reIndexAll(userContext,
									  all);
	}
	@Override
	public EnqueuedJob reIndexAll(final UserContext userContext) {
		return this.createDelegateAs(IndexServicesForModelObject.class)
								.reIndexAll(userContext);
	}
}
