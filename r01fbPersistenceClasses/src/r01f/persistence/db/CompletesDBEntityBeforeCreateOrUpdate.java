package r01f.persistence.db;

import r01f.persistence.PersistencePerformedOperation;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.usercontext.UserContext;

public interface CompletesDBEntityBeforeCreateOrUpdate<DB extends DBEntityForModelObject<? extends DBPrimaryKeyForModelObject>> {
	/**
	 * Gives the CRUD layer the oportunity to complete the {@link DBEntity}... maybe setting dependent entities
	 * @param userContext
	 * @param requestedOperation
	 * @param dbEntity
	 */
	public abstract void completeDBEntityBeforeCreateOrUpdate(final UserContext userContext,
														      final PersistencePerformedOperation performedOp,
														      final DB dbEntity);
}
