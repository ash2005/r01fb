package r01f.services.interfaces;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable;
import r01f.persistence.CRUDResult;
import r01f.usercontext.UserContext;

/**
 * CRUD (create, read, update, delete) interface for not {@link Versionable} model object
 * @param <O>
 * @param <M>
 */
public interface CRUDServicesForModelObject<O extends OID,M extends PersistableModelObject<O>> 
		 extends ServiceInterfaceForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
//	/**
//	 * @return the {@link DBEntity} type that models the persistence of the {@link PersistableModelObject}
//	 */
//	public Class<? extends DBEntity> getDBEntityType();
/////////////////////////////////////////////////////////////////////////////////////////
//	CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a entity from its identifier.
	 * If the entity is a {@link Versionable} {@link PersistableModelObject}, this method returns the 
	 * currently active version
	 * @param userContext the user auth data & context info
	 * @param oid the entity identifier
	 * @return a {@link CRUDResult} that encapsulates the entity if it was loaded successfully
	 */
	public CRUDResult<M> load(final UserContext userContext,
				  			  final O oid);	
	/**
	 * Creates a entity
	 * If the entity is a {@link Versionable} {@link PersistableModelObject}, and no other version exists 
	 * this creates a fresh new active version. Otherwise, if another version existed, it
	 * throws an exception
	 * @param userContext the user auth data & context info
	 * @param entity the entity to be created
	 * @return a {@link CRUDResult} that encapsulates the created entity
	 */
	public CRUDResult<M> create(final UserContext userContext,
				  				final M entity);
	/**
	 * Updates a entity 
	 * If a entity is a {@link Versionable} {@link PersistableModelObject}, it updates the currently
	 * active version
	 * @param userContext the user auth data & context info
	 * @param entity the entity to be updated
	 * @return a {@link CRUDResult} that encapsulates the updated entity
	 */
	public CRUDResult<M> update(final UserContext userContext,
				  				final M entity);
	/**
	 * Deletes a entity
	 * If a entity is a {@link Versionable} {@link PersistableModelObject}, it deletes the currently 
	 * active version
	 * @param userContext the user auth data & context info
	 * @param oid the identifier of the entity to be deleted
	 * @return a {@link CRUDResult} that encapsulates the deleted entity
	 */
	public CRUDResult<M> delete(final UserContext userContext,
								final O oid);
}
