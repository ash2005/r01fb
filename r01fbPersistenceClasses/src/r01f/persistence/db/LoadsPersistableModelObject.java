package r01f.persistence.db;

import java.util.Set;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.PersistenceException;
import r01f.usercontext.UserContext;

/**
 * Interface for types in charge of loading persistable model objects 
 * @param <O>
 * @param <M>
 */
public interface LoadsPersistableModelObject<O extends OID,
					  						 M extends PersistableModelObject<O>> {
	/**
	 * Returns a entity from its identifier.
	 * @param userContext the user auth data & context info
	 * @param oid the entity identifier
	 * @return the loaded record
	 * @throws PersistenceException
	 */
	public M load(final UserContext userContext,
			   	  final O oid) throws PersistenceException;
	/**
	 * Returns all oids form the DB
	 * @param userContext the user auth data & context info
	 * @return a {@link Set} with the loaded oids
	 * @throws PersistenceException
	 */
	public Set<O> loadAllOids(final UserContext userContext) throws PersistenceException;
}
