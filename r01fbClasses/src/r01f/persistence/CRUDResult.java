package r01f.persistence;

import r01f.debug.Debuggable;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

public interface CRUDResult<M extends PersistableModelObject<? extends OID>>
		 extends PersistenceOperationResult,
		 		 Debuggable {
	/**
	 * Returns the CRUD operation's target object or throws a {@link PersistenceException}
	 * if the operation resulted on an error
	 * @return
	 * @throws PersistenceException
	 */
	public M getOrThrow() throws PersistenceException;
	/**
	 * @return a {@link CRUDOK} instance
	 */
	public CRUDOK<M> asOK();
	/**
	 * @return a {@link CRUDError} instance
	 */
	public CRUDError<M> asError();
}
