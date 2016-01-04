package r01f.persistence.db;

import java.util.Collection;
import java.util.Date;

import javax.persistence.EntityManager;

import com.google.common.collect.Lists;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDOnMultipleResult;
import r01f.persistence.CRUDResult;
import r01f.persistence.CRUDResultBuilder;
import r01f.persistence.db.entities.DBEntityForVersionableModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForVersionableModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForVersionableModelObjectImpl;
import r01f.reflection.ReflectionUtils;
import r01f.services.delegates.persistence.CRUDServicesForVersionableModelObjectDelegateBase;
import r01f.usercontext.UserContext;
import r01f.util.types.Dates;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Base type for every persistence layer type
 * @param <O>
 * @param <M>
 * @param <PK>
 * @param <DB>
 */
@Accessors(prefix="_")
@Slf4j
public abstract class DBCRUDForVersionableModelObjectBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet,
							  		 					  PK extends DBPrimaryKeyForVersionableModelObject,DB extends DBEntity & DBEntityForVersionableModelObject<PK>>
			  extends DBCRUDForModelObjectBase<O,M,
			  				     			   PK,DB>
	       implements DBCRUDForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR  
/////////////////////////////////////////////////////////////////////////////////////////
	public DBCRUDForVersionableModelObjectBase(final Class<M> modelObjType,final Class<DB> dbEntityType,
											   final EntityManager entityManager,
											   final XMLPropertiesForAppComponent persistenceProperties) {
		super(modelObjType,dbEntityType,
			  entityManager,
			  persistenceProperties);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	Some trickery to build the key used at the entity manager's finder method  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	protected PK getDBEntityPrimaryKeyFor(final O oid) {
		return (PK)DBPrimaryKeyForVersionableModelObjectImpl.from(oid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> activate(final UserContext userContext,
								  final M entityToBeActivated) { 
		throw new IllegalStateException(Throwables.message("Implemented at service level (see {}",CRUDServicesForVersionableModelObjectDelegateBase.class));
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadActiveVersionAt(final UserContext userContext,
						   			   		 final VersionIndependentOID oid,final Date date) {
		
		log.debug("> loading a {} entity with oid={} active at {}",_DBEntityType,oid,date);
		
		// [1] - Load the active version entity at the provided date
		String namedQuery = ReflectionUtils.classNameFromClassNameIncludingPackage(_DBEntityType.getName()) + "VersionActiveAt";
		Collection<DB> activeVersionEntities = this.getEntityManager().createNamedQuery(namedQuery)		// this named query MUST exist at the entity with this name
															 				.setParameter("theOid",oid.asString())
															 				.setParameter("theDate",Dates.asCalendar(date))
															 		  .getResultList();
		M activeVersion = null;
		if (CollectionUtils.hasData(activeVersionEntities)) {
			DB activeVersionDBEntity = CollectionUtils.of(activeVersionEntities)
													  .pickOneAndOnlyElement("The DB is NOT consistent: there's more than a single version of {} active at {}",
																			 oid,date);
			activeVersion = _dbEntityToModelObject(userContext,
												   activeVersionDBEntity);
		}
		// [2] - Compose the persistence operation result
		CRUDResult<M> outLoadResult = null;
		if (activeVersion != null) {
			outLoadResult = CRUDResultBuilder.using(userContext)
											 .on(_modelObjectType)
											 .loaded()
												.entity(activeVersion);
		} else {
			outLoadResult = CRUDResultBuilder.using(userContext)
											 .on(_modelObjectType)
											 .notLoaded()
												.becauseClientRequestedEntityWasNOTFound()
													.about(oid,date).build();
			log.warn(outLoadResult.getDetailedMessage());
		}
		return outLoadResult;
	}
	
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadWorkVersion(final UserContext userContext,
							 			 final VersionIndependentOID oid) {		
		log.debug("> loading a {} entity with oid={} work version",_DBEntityType,oid);
		
		// [1] - Load the work version entity
		String namedQuery = ReflectionUtils.classNameFromClassNameIncludingPackage(_DBEntityType.getName()) + "WorkVersion";
		Collection<DB> workVersionEntities = this.getEntityManager().createNamedQuery(namedQuery)		// this named query MUST exist at the entity with this name
														   				.setParameter("theOid",oid.asString())
														   			.getResultList();
		M activeVersion = null;
		if (CollectionUtils.hasData(workVersionEntities)) {
			DB activeVersionEntity = CollectionUtils.of(workVersionEntities)
												    .pickOneAndOnlyElement("The DB is NOT consistent: there's more than a single work version of {}",
																		   oid);
			activeVersion = _dbEntityToModelObject(userContext,
												   activeVersionEntity);
		}
		
		// [2] - Compose the RecordPersistenceOperationResult
		CRUDResult<M> outLoadResult = null;
		if (activeVersion != null) {
			outLoadResult = CRUDResultBuilder.using(userContext)
											 .on(_modelObjectType)
											 .loaded()
												 .entity(activeVersion);
		} else {
			outLoadResult = CRUDResultBuilder.using(userContext)
											 .on(_modelObjectType)
											 .notLoaded()
											 .becauseClientRequestedEntityWasNOTFound()
											 		.aboutWorkVersion(oid).build();
			log.warn(outLoadResult.getDetailedMessage());
		}
		return outLoadResult;
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDOnMultipleResult<M> deleteAllVersions(final UserContext userContext,
													  	   	 final VersionIndependentOID oid) {
		log.debug("> deleting all versions for a {} entity with oid={}",_DBEntityType,oid);
		
		// [1] - Load all version entities
		String namedQuery = ReflectionUtils.classNameFromClassNameIncludingPackage(_DBEntityType.getName()) + 
							"AllVersions";
		Collection<DB> allVersionEntities = this.getEntityManager().createNamedQuery(namedQuery)		// this named query MUST exist at the entity with this name
														  				.setParameter("theOid",oid.asString())
														  		   .getResultList();		
		// [2] - Call delete for every version entity
		Collection<M> deletedEntities = null;
		if (CollectionUtils.hasData(allVersionEntities)) {
			for (DB dbEntity : allVersionEntities) {
				if (dbEntity != null) {
					this.getEntityManager()
						.remove(this.getEntityManager().merge(dbEntity));	// TODO revisar			
					M deletedModelObj =  _dbEntityToModelObject(userContext,
														  	  dbEntity);
					if (deletedEntities == null) deletedEntities = Lists.newArrayList();
					deletedEntities.add(deletedModelObj);
				}
			}
		} else {
			log.trace("No versions to delete for a {} entity with oid={}",_DBEntityType,oid);
		}
		
		// [3] - Compose the result and return
		CRUDOnMultipleResult<M> outDeleteResults = CRUDResultBuilder.using(userContext)
																			.on(_modelObjectType)
																			.<M>versionable()
																				.deletedDBEntities(allVersionEntities);
		return outDeleteResults;
	}
}
