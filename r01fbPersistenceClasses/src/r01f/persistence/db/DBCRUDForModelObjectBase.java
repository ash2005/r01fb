package r01f.persistence.db;

import javax.persistence.EntityManager;

import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDResult;
import r01f.persistence.CRUDResultBuilder;
import r01f.persistence.PersistencePerformedOperation;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.usercontext.UserContext;
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
public abstract class DBCRUDForModelObjectBase<O extends OID,M extends PersistableModelObject<O>,
							     			   PK extends DBPrimaryKeyForModelObject,DB extends DBEntityForModelObject<PK>>
			  extends DBBaseForModelObject<O,M,
			 			     			   PK,DB>
	       implements DBCRUDForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBCRUDForModelObjectBase(final Class<M> modelObjectType,final Class<DB> dbEntityType,
									final EntityManager entityManager,
									final XMLPropertiesForAppComponent persistenceProps) {
		super(modelObjectType,dbEntityType,
			  entityManager,
			  persistenceProps);
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override 
	public CRUDResult<M> load(final UserContext userContext,
							  final O oid) {
		// Compose the pk
		PK pk = this.getDBEntityPrimaryKeyFor(oid);	
		return _doLoad(userContext,
					   oid,pk);
	}
	@Override 
	public CRUDResult<M> create(final UserContext userContext,
								final M modelObj) {
		if (modelObj.getEntityVersion() != 0) throw new IllegalStateException(Throwables.message("Cannot create a {} entity because the model object received at the persistence layer received does have the entityVersion attribute with a NON ZERO value. This is a developer's fault; please check that when persisting the model object, the entityVersion is NOT set",
																							     _modelObjectType));
		return _doCreateOrUpdateEntity(userContext,
									   modelObj,
									   PersistenceRequestedOperation.CREATE);		// it's a creation
	}
	@Override 
	public CRUDResult<M> update(final UserContext userContext,
								final M entity) {
		// some checks to help developers...
		if (entity.getEntityVersion() == 0) throw new IllegalStateException(Throwables.message("Cannot update a {} entity because the model object received at the persistence layer received does NOT have the entityVersion attribute. This is a developer's fault; please check that when persisting the model object, the entityVersion is set",
																							   _modelObjectType));

		return _doCreateOrUpdateEntity(userContext,
									   entity,
									   PersistenceRequestedOperation.UPDATE);		// it's an update
	}
	@Override
	public CRUDResult<M> delete(final UserContext userContext,
								final O oid) {
		PK pk = this.getDBEntityPrimaryKeyFor(oid);
		return _doDelete(userContext,
				  		 oid,pk);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates or updates the entity by checking the entity existence in the first place
	 * and then creating or updating it
	 * @param userContext
	 * @param pk 
	 * @param modelObj
	 * @param supposedNew
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected CRUDResult<M> _doCreateOrUpdateEntity(final UserContext userContext,
													final M modelObj,
													final PersistenceRequestedOperation requestedOp) {
		// [0]: find the pk
		PK pk = _dbEntityPrimaryKeyFor(modelObj);	
		if (pk == null) return CRUDResultBuilder.using(userContext)
												.on(_modelObjectType)
												.notCreated()
												.becauseClientBadRequest("The provided {} entity do not have primary key data",_modelObjectType)
												  		.about(modelObj);
		
		// [1]: Check if the entity exists
		DB dbEntityToPersist = this.getEntityManager().find(_DBEntityType,
												  		   	pk);
		// [2]: If the entity exists BUT it's supposed to be new... it's an error
		if (dbEntityToPersist != null && requestedOp.is(PersistenceRequestedOperation.CREATE)) {
			return CRUDResultBuilder.using(userContext)
									.on(_modelObjectType)
									.notCreated()
									.becauseClientRequestedEntityAlreadyExists()
									 		.about(modelObj);
		}
		
		// [3]: Depending on the existence of the entity create or update
		DB outManagedDBEntity = null;
		M outModelObj = null;
		PersistencePerformedOperation performedOp = null;
		if (dbEntityToPersist != null) {
			// Update
			log.debug("> updating a {} entity with pk={} and entityVersion={}",_DBEntityType,pk.asString(),modelObj.getEntityVersion());
					
			performedOp = PersistencePerformedOperation.UPDATED;
			dbEntityToPersist.fromModelObject(userContext,		// update!! do NOT call _modelObjectToDBEntity since it creates a new DB object
											  modelObj);
			dbEntityToPersist.setEntityVersion(modelObj.getEntityVersion());	// ... but do not forget to set the entity version
																				//	   (usually it's NOT set at fromModelObject method)
			if (userContext.getUserCode() != null)  dbEntityToPersist.setLastUpdatorUserCode(userContext.getUserCode());			
		} 
		else {
			// Create
			log.debug("> creating a {} entity with pk={}",_DBEntityType,pk.asString());
			
			performedOp = PersistencePerformedOperation.CREATED;
			dbEntityToPersist = _modelObjectToDBEntity(userContext,
												 	   modelObj);		
			if (userContext.getUserCode() != null) dbEntityToPersist.setCreatorUserCode(userContext.getUserCode());
		}
		// [4]: give the opportunity to complete the entity
		if (this instanceof CompletesDBEntityBeforeCreateOrUpdate) {
			((CompletesDBEntityBeforeCreateOrUpdate<DB>)this).completeDBEntityBeforeCreateOrUpdate(userContext, 
																							   	   PersistencePerformedOperation.UPDATED, 
																							   	   dbEntityToPersist);
		}			
		// [5]: persist > see the difference between persist & merge: http://stackoverflow.com/questions/1069992/jpa-entitymanager-why-use-persist-over-merge
		//						- PERSIST > takes an entity instance, adds it to the context and makes that instance managed (ie future updates to the entity will be tracked)
		//						- MERGE   > creates a new instance of your entity, copies the state from the supplied entity, and makes the new copy managed
		//									The instance supplied to merge will NOT be managed (any changes will not be part of the transaction)
		
		// Using merge (it issues an additional DB read)
//		outManagedDBEntity = this.getEntityManager()
//									.merge(dbEntityToPersist);		// dbEntityToPersist is NOT managed anymore!
		
		// Using persist
		this.getEntityManager().persist(dbEntityToPersist);
		outManagedDBEntity = dbEntityToPersist;
				
		// a flush() call is needed to get the jpa's assigned entity version
		this.getEntityManager().flush();	
		
		// [6]: build the result
		outModelObj = _dbEntityToModelObject(userContext,
										     outManagedDBEntity);	// beware that the managed object is the merge's returned entity
																	// the one that contains the updated entity version...
		CRUDResult<M> outResult = CRUDResultBuilder.using(userContext)
												   .on(_modelObjectType)
												   .executed(requestedOp,performedOp)	
												 		.entity(outModelObj);	
		return outResult;
	}
	protected CRUDResult<M> _doDelete(final UserContext userContext,
									  final O oid,final PK pk) {
		// check the pk is NOT null
		if (pk == null) return CRUDResultBuilder.using(userContext)
											    .on(_modelObjectType)
												.notDeleted()
												   .becauseClientBadRequest("The entity oid cannot be null in order to be deleted")
												  		.about(oid);
		
		log.debug("> deleting a {} entity with pk={}",_DBEntityType,pk.asString());
		
		CRUDResult<M> outResult = null;
		
		// [1]: Check the existence of the entity to be deleted
		DB dbEntity = this.getEntityManager().find(_DBEntityType,
								  	  	  		   pk);
		// [2]: Delete the entity
		if (dbEntity != null) {
			this.getEntityManager().refresh(dbEntity);		// refresh the dbentity since it could be modified (ie by setting a bi-directional relation)	
			this.getEntityManager().remove(dbEntity);		
			M outModelObj =  _dbEntityToModelObject(userContext,
												  	dbEntity);			
			outResult = CRUDResultBuilder.using(userContext)
										 .on(_modelObjectType)
										 .deleted()
											.entity(outModelObj);
		} else {
			outResult = CRUDResultBuilder.using(userContext)
										 .on(_modelObjectType)
											.notDeleted()
												.becauseClientRequestedEntityWasNOTFound()
														.about(oid);
			log.warn(outResult.getDetailedMessage());
		}
		return outResult;
	}
}
