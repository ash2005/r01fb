package r01f.services.delegates.persistence;


import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.FindResult;
import r01f.persistence.db.DBFindForModelObject;
import r01f.services.interfaces.FindServicesForVersionableModelObject;
import r01f.usercontext.UserContext;

import com.google.common.eventbus.EventBus;

/**
 * Service layer delegated type for CRUD find operations
 */
public abstract class FindServicesForVersionableModelObjectDelegateBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet>
		      extends FindServicesForModelObjectDelegateBase<O,M> 
		   implements FindServicesForVersionableModelObject<O,M> {


/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR  
/////////////////////////////////////////////////////////////////////////////////////////
	public FindServicesForVersionableModelObjectDelegateBase(final Class<M> modelObjectType,
												  		 	 final DBFindForModelObject<O,M> findServices) {
		this(modelObjectType,
			 findServices,
			 null);
	}
	public FindServicesForVersionableModelObjectDelegateBase(final Class<M> modelObjectType,
												  		 	 final DBFindForModelObject<O,M> findServices,
												  		 	 final EventBus eventBus) {
		super(modelObjectType,
			  findServices,
			  eventBus);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  VERSIONABLE FIND
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindResult<M> findAllVersions(final UserContext userContext) {
		throw new UnsupportedOperationException("NOT jet implemented!");
	}
}
