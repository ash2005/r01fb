package r01f.persistence;

import r01f.guids.OID;

public interface FindOIDsResult<O extends OID> 
       	 extends FindResult<O> {
	/**
	 * @return a {@link FindOIDsError}
	 */
	public FindOIDsError<O> asCRUDError();
	/**
	 * @return a {@link FindOIDsOK}
	 */
	public FindOIDsOK<O> asCRUDOK();
}
