package r01f.services.interfaces;

import java.util.Date;
import java.util.Set;

import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDOnMultipleResult;
import r01f.persistence.CRUDResult;
import r01f.usercontext.UserContext;

/**
 * CRUD (create, read, update, delete) interface for versionable model object
 * @param <O>
 * @param <V>
 * @param <M>
 */
public interface CRUDServicesForVersionableModelObject<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet> 
		 extends CRUDServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	LOAD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Loads a version whose activation start date is NOT null and it's lower than the provided date
	 * If the provided date is null, the currently active version (the one with activation end date null) is returned
	 * @param userContext the user auth data & context info
	 * @param oid the record identifier
	 * @param date the date
	 * @return a {@link CRUDResult} instance that encapsulates the record if it was loaded successfully
	 */
	public CRUDResult<M> loadActiveVersionAt(final UserContext userContext,
						   			   		 final VersionIndependentOID oid,final Date date);
	/**
	 * Loads the work version: it's activation start date is null
	 * @param userContext
	 * @param oid
	 * @return a {@link CRUDResult} instance that encapsulates the record if it was loaded successfully
	 */
	public CRUDResult<M> loadWorkVersion(final UserContext userContext,
							 			 final VersionIndependentOID oid);
/////////////////////////////////////////////////////////////////////////////////////////
//  DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Deletes all versions of a record
	 * This method returns a {@link Set} of {@link RecordPersistenceOperationResult} for every version to delete 
	 * @param userContext the user auth data & context info
	 * @param oid the identifier of the record whose versions are to be deleted
	 * @return the versions delete operation result
	 */
	public CRUDOnMultipleResult<M> deleteAllVersions(final UserContext userContext,
									 				       	 	 final VersionIndependentOID oid);
/////////////////////////////////////////////////////////////////////////////////////////
//  ACTIVATION
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Activates an entity, that's:
	 * <ul>
	 * 		<li>Finds the currently active version (if there's any) and sets its activation end date to this moment</li>
	 * 		<li>Sets the entity activation start date to this moment (and null activation end date</li>
	 * </li>
	 * If the entity is active (has a not null activation start date), an {@link IllegalStateException} is raised
	 * @param userContext
	 * @param entityToBeActivated
	 * @return
	 */
	public CRUDResult<M> activate(final UserContext userContext,
								  final M entityToBeActivated);

}
