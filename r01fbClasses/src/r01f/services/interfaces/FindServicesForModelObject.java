package r01f.services.interfaces;

import java.util.Date;

import r01f.guids.CommonOIDs.UserCode;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable;
import r01f.persistence.FindOIDsResult;
import r01f.persistence.FindResult;
import r01f.types.Range;
import r01f.usercontext.UserContext;

/**
 * Finding
 * @param <O>
 * @param <M>
 */
public interface FindServicesForModelObject<O extends OID,M extends PersistableModelObject<O>>
		 extends ServiceInterfaceForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	FINDING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Finds all persisted model object oids
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findAll(final UserContext userContext);	
	/**
	 * Finds all persisted model object oids which create date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param createDate
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByCreateDate(final UserContext userContext,
											  final Range<Date> createDate);
	/**
	 * Finds all persisted model object oids which last update date is in the provided range
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param lastUpdateDate
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByLastUpdateDate(final UserContext userContext,
											  	  final Range<Date> lastUpdateDate);
	/**
	 * Finds all persisted model object oids created by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param creatorUserCode
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByCreator(final UserContext userContext,
									   	   final UserCode creatorUserCode);
	/**
	 * Finds all persisted model object oids last updated by the provided user
	 * If the entity is a {@link Versionable}  {@link PersistableModelObject}, it returns the 
	 * currently active versions
	 * @param userContext the user auth data & context info
	 * @param lastUpdtorUserCode
	 * @return a {@link FindResult} that encapsulates the oids
	 */
	public FindOIDsResult<O> findByLastUpdator(final UserContext userContext,
										   	   final UserCode lastUpdtorUserCode);
}
