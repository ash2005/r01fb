package r01f.persistence;

import java.util.Collection;

import r01f.debug.Debuggable;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

public interface FindResult<M extends PersistableModelObject<? extends OID>> 
   		 extends PersistenceOperationResult,
   		  		 Debuggable {
	/**
	 * Returns the found entities or throws a {@link PersistenceException} 
	 * if the find operation resulted on an error
	 * @return
	 */
	public Collection<M> getOrThrow() throws PersistenceException;
	/**
	 * @return a {@link FindOK}
	 */
	public FindOK<M> asOK();
	/**
	 * @return a {@link FindError}
	 */
	public FindError<M> asError();
}
