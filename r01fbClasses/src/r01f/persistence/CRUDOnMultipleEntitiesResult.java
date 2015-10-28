package r01f.persistence;

import java.util.Collection;

import r01f.debug.Debuggable;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

public interface CRUDOnMultipleEntitiesResult<M extends PersistableModelObject<? extends OID>>
	     extends PersistenceOperationResult,
			     Debuggable {	
	/**
	 * Returns the successful persistence operations or throws a {@link PersistenceException}
	 * if any operation failed
	 * @return
	 * @throws PersistenceException
	 */
	public Collection<M> getSuccessfulOperationsOrThrow() throws PersistenceException;
	/**
	 * @return a {@link CRUDOK} instance
	 */
	public CRUDOnMultipleEntitiesOK<M> asOK();
	/**
	 * @return a {@link CRUDError} instance
	 */
	public CRUDOnMultipleEntitiesError<M> asError();
}
