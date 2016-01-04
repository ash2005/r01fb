package r01f.persistence;

import java.util.Collection;

import r01f.debug.Debuggable;

public interface CRUDOnMultipleResult<M>
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
	public CRUDOnMultipleOK<M> asCRUDOnMultipleOK();
	/**
	 * @return a {@link CRUDError} instance
	 */
	public CRUDOnMultipleError<M> asCRUDOnMultipleError();
}
