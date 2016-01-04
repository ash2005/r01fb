package r01f.persistence.db;

import java.util.Collection;

import javax.persistence.EntityManager;

import com.google.common.base.Function;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDResult;
import r01f.persistence.CRUDResult;
import r01f.persistence.CRUDResultBuilder;
import r01f.persistence.db.entities.DBEntityForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObject;
import r01f.persistence.db.entities.primarykeys.DBPrimaryKeyForModelObjectImpl;
import r01f.reflection.ReflectionUtils;
import r01f.usercontext.UserContext;
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
public abstract class DBBaseForModelObject<O extends OID,M extends PersistableModelObject<O>,
				      					   PK extends DBPrimaryKeyForModelObject,DB extends DBEntityForModelObject<PK>>
			  extends DBBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object's type
	 */
	@Getter protected final Class<M> _modelObjectType;
	/**
	 * entity java type
	 */
	@Getter protected final Class<DB> _DBEntityType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DBBaseForModelObject(final Class<M> modelObjectType,final Class<DB> dbEntityType,
								final EntityManager entityManager,
								final XMLPropertiesForAppComponent persistenceProps) {
		super(entityManager,
			  persistenceProps);
		_modelObjectType = modelObjectType;
		_DBEntityType = dbEntityType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONVERTERS
/////////////////////////////////////////////////////////////////////////////////////////
	protected DB _modelObjectToDBEntity(final UserContext userContext,
										final M modelObj) {
		DB outEntity = ReflectionUtils.<DB>createInstanceOf(_DBEntityType);
		outEntity.fromModelObject(userContext,
						     	  modelObj);
		// do not forget!!
		outEntity.setEntityVersion(modelObj.getEntityVersion());
		return outEntity;
	}
	protected M _dbEntityToModelObject(final UserContext userContext,
									   final DB dbEntity) {		
		// Convert to model object
		Function<DB,M> transformer = DBEntityToModelObjectTransformerBuilder.<DB,M>createFor(userContext,
									  													  	 _modelObjectType);
		return transformer.apply(dbEntity);
	}
	/**
	 * Builds the primary key from the model object
	 * @param entity
	 * @return
	 */
	protected PK _dbEntityPrimaryKeyFor(final M entity) {
		// the key is an unique column primary key
		O oid = entity.getOid();
		PK outKey = this.getDBEntityPrimaryKeyFor(oid);	
		return outKey;
	}
	/**
	 * Builds the primary key for the given oid
	 * @param oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	protected PK getDBEntityPrimaryKeyFor(final O oid) {		
		return (PK)DBPrimaryKeyForModelObjectImpl.from(oid);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LOAD  
/////////////////////////////////////////////////////////////////////////////////////////
	protected CRUDResult<M> _doLoad(final UserContext userContext,
								    final O oid,final PK pk) {
		// check the oid
		if (pk == null) return CRUDResultBuilder.using(userContext)
														   .on(_modelObjectType)
													  	   .notLoaded()
													  	   .becauseClientBadRequest("The {} entity's oid cannot be null in order to be loaded",_modelObjectType)
													  	   		.about(oid).build();
		// Load the entity
		DB dbEntity = _doLoadEntity(userContext,
									pk);
		
		// Compose the PersistenceOperationResult object
		CRUDResult<M> outEntityLoadResult = null;
		if (dbEntity != null) {
			M modelObj = _dbEntityToModelObject(userContext,
											    dbEntity);
			outEntityLoadResult = CRUDResultBuilder.using(userContext)
															  .on(_modelObjectType)
															  .loaded()
															  .entity(modelObj);
		} else {
			outEntityLoadResult = CRUDResultBuilder.using(userContext)
															  .on(_modelObjectType)
															  .notLoaded()
															  .becauseClientRequestedEntityWasNOTFound()
															  		.about(oid).build();
			log.warn(outEntityLoadResult.getDetailedMessage());
		}
		return outEntityLoadResult;
	}
	/**
	 * Loads the db entity using the oid
	 * @param userContext
	 * @param oid
	 * @return
	 */
	protected DB _doLoadEntity(final UserContext userContext,
							   final O oid) {
		PK pk = this.getDBEntityPrimaryKeyFor(oid);
		if (pk == null) return null;
		return _doLoadEntity(userContext,
							 pk);
	}
	/**
	 * Loads the db entity using the pk
	 * @param userContext
	 * @param pk
	 * @return
	 */
	protected DB _doLoadEntity(final UserContext userContext,
							   final PK pk) {
		log.debug("> loading a {} entity with pk={}",_DBEntityType,pk.asString());
		DB dbEntity = this.getEntityManager()
						  .find(_DBEntityType,
							    pk);
		return dbEntity;
	}
	/**
	 * Composes a {@link CRUDResult} for a load operation where there should be
	 * only one result
	 * @param userContext
	 * @param id
	 * @param dbEntities
	 * @return
	 */
	protected CRUDResult<M> _crudResultForSingleEntity(final UserContext userContext,
													   final OID id,
													   final Collection<DB> dbEntities) {
		// Return
		CRUDResult<M> outResult = null;
		if (CollectionUtils.hasData(dbEntities)) {
			if (dbEntities.size() > 1) {
				// there are two entities with the same id!!!
				outResult = CRUDResultBuilder.using(userContext)
											 .on(_modelObjectType)
											 .notLoaded()
											 	.becauseServerError("There MUST be a single entity of {} with id {}",_DBEntityType,id)
											 	.about(id).build();
			} else {
				// normal 
				DB dbEntity = CollectionUtils.of(dbEntities)
											 .pickOneAndOnlyElement();
				outResult = CRUDResultBuilder.using(userContext)
											 .on(_modelObjectType)
											 .loaded()
												.dbEntity(dbEntity);
			}
		} else {
			// no results
			outResult = CRUDResultBuilder.using(userContext)
										 .on(_modelObjectType)
										 .notLoaded()
										 	.becauseClientRequestedEntityWasNOTFound()
										 	.about(id).build();
		}
		return outResult;
	}
}
