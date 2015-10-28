package r01f.services.delegates.persistence;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.persistence.db.ModelObjectValidationResult;
import r01f.persistence.db.ModelObjectValidationResults.ModelObjectValidationResultNOK;
import r01f.persistence.db.ModelObjectValidationResults.ModelObjectValidationResultOK;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.usercontext.UserContext;

/**
 * This interface is intended to be used at {@link CRUDServicesForModelObject} sub-types that validates
 * the model object BEFORE it's created or updated
 * @param <M>
 */
public interface ValidatesModelObjectBeforeCreateOrUpdate<M extends PersistableModelObject<? extends OID>> {
	/**
	 * Validates the model object BEFORE being created or updated
	 * If the model object is NOT valid, it MUST return a {@link ModelObjectValidationResultNOK} that encapsulates the reason
	 * If the model object is valid, it MUST return a {@link ModelObjectValidationResultOK}
	 * @param userContext
	 * @param requestedOperation
	 * @param modelObj
	 * @return a {@link ModelObjectValidationResult}
	 */
	public abstract ModelObjectValidationResult<M> validateModelObjBeforeCreateOrUpdate(final UserContext userContext,
																						final PersistenceRequestedOperation requestedOp,
															 				  			final M modelObj);
}
