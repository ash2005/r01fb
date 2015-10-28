package r01f.services.persistence;

import java.util.Date;

import com.google.inject.persist.Transactional;

import lombok.experimental.Accessors;
import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDOnMultipleEntitiesResult;
import r01f.persistence.CRUDResult;
import r01f.services.delegates.persistence.CRUDServicesForVersionableModelObjectDelegateBase;
import r01f.services.interfaces.CRUDServicesForVersionableModelObject;
import r01f.usercontext.UserContext;


/**
 * Implements the {@link HasVersionableFacet}-related services which in turn are 
 * delegated to {@link CRUDServicesForVersionableModelObjectDelegateBase}
 */
@Accessors(prefix="_")
public abstract class CoreCRUDServiceForVersionableModelObjectBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet>
			  extends CoreCRUDServiceForModelObjectBase<O,M>
		   implements CRUDServicesForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadActiveVersionAt(final UserContext userContext,
									   		 final VersionIndependentOID oid,final Date date) {
		return this.createDelegateAs(CRUDServicesForVersionableModelObject.class)
						.loadActiveVersionAt(userContext,
								 	   		 oid,date);
	}
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadWorkVersion(final UserContext userContext,
									   	 		    final VersionIndependentOID oid) {
		return this.createDelegateAs(CRUDServicesForVersionableModelObject.class)
						.loadWorkVersion(userContext,
								   		 oid);
	}
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public CRUDOnMultipleEntitiesResult<M> deleteAllVersions(final UserContext userContext, 
														   				final VersionIndependentOID oid) {
		return this.createDelegateAs(CRUDServicesForVersionableModelObject.class)
						.deleteAllVersions(userContext,
									 	   oid);
	}
	@Transactional
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> activate(final UserContext userContext,
								  final M entityToBeActivated) {
		return this.createDelegateAs(CRUDServicesForVersionableModelObject.class)
						.activate(userContext, 
								  entityToBeActivated);
	}
}
