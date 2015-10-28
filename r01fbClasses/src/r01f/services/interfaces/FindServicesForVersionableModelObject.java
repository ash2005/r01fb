package r01f.services.interfaces;

import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.FindResult;
import r01f.persistence.PersistenceOperationResult;
import r01f.usercontext.UserContext;

/**
 * Finding for versionable model objects
 * @param <O>
 * @param <M>
 */
public interface FindServicesForVersionableModelObject<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet>
		 extends FindServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FINDING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all model object's versions
	 * @param userContext the user auth data & context info
	 * @return a {@link PersistenceOperationResult} that encapsulates the oids
	 */
	public FindResult<M> findAllVersions(final UserContext userContext);
}
