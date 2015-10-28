package r01f.persistence;

import java.util.Collection;

import r01f.debug.Debuggable;
import r01f.guids.OID;

public interface FindOIDsResult<O extends OID> 
       	 extends PersistenceOperationResult,
       	 		 Debuggable {
	/**
	 * Returns the found entities or throws a {@link PersistenceException} 
	 * if the find operation resulted on an error
	 * @return
	 */
	public Collection<O> getOrThrow() throws PersistenceException;
	/**
	 * @return a {@link FindOIDsError}
	 */
	public FindOIDsError<O> asError();
	/**
	 * @return a {@link FindOIDsOK}
	 */
	public FindOIDsOK<O> asOK();
}
