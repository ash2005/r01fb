package r01f.services.delegates.persistence;


import com.google.common.eventbus.EventBus;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.guids.OIDIsGenerated;
import r01f.guids.OIDs;
import r01f.model.PersistableModelObject;
import r01f.model.facets.SelfValidates;
import r01f.persistence.CRUDResult;
import r01f.persistence.CRUDResultBuilder;
import r01f.persistence.PersistenceOperationError;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.persistence.db.DBCRUDForModelObject;
import r01f.reflection.ReflectionUtils;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.usercontext.UserContext;
import r01f.validation.ObjectValidationResult;

/**
 * Service layer delegated type for CRUD (Create/Read/Update/Delete) operations
 */
@Slf4j
public abstract class CRUDServicesForModelObjectDelegateBase<O extends OID,M extends PersistableModelObject<O>>
		      extends PersistenceServicesForModelObjectDelegateBase<O,M> 
		   implements CRUDServicesForModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * true if read-only operations are the only ones available
	 */
	@Getter protected final boolean _readOnly;
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR  
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDServicesForModelObjectDelegateBase(final Class<M> modelObjectType,
												  final DBCRUDForModelObject<O,M> dbCrud,
												  final EventBus eventBus) {
		super(modelObjectType,
			  dbCrud,
			  eventBus);
		_readOnly = false;
	}	
	public CRUDServicesForModelObjectDelegateBase(final Class<M> modelObjectType,
												  final DBCRUDForModelObject<O,M> dbCrud) {
		this(modelObjectType,
		     dbCrud,
		     null);		// no event bus
	}
////////////////////////////////////////////////////////////////////////////////////////
//  LOAD | EXISTS
////////////////////////////////////////////////////////////////////////////////////////
	public boolean exists(final UserContext userContext,
						  final O oid) {
		CRUDResult<M> loadResult = this.load(userContext,
														oid);
		boolean outExists = loadResult.hasSucceeded();
		if (loadResult.hasFailed() && !loadResult.asCRUDError()		// as(CRUDError.class)
												 .wasBecauseClientRequestedEntityWasNOTFound()) {
			log.error("Error trying to check the existence of record with oid={}: {}",oid,
					  loadResult.asCRUDError()	// as(CRUDError.class)
					  		    .getPersistenceException().getMessage());
		}
		return outExists;
	}
	@Override 
	public CRUDResult<M> load(final UserContext userContext,
				  			  final O oid) {
		CRUDResult<M> outEntityLoadResult = null;
		
		// [0] - check the oid
		if (oid == null) {
			return CRUDResultBuilder.using(userContext)
									   .on(_modelObjectType)
									   .notLoaded()
									   .becauseClientBadRequest("The {} entity's oid cannot be null in order to be loaded",_modelObjectType)
									   		.about(oid).build();
		}
		// [1] - Load
		outEntityLoadResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
										.load(userContext,
										 	  oid);
		// [2] - Throw CRUD event
		_fireEvent(userContext,
				   outEntityLoadResult);
		// [3] - Return
		return outEntityLoadResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CREATE OR UPDATE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> create(final UserContext userContext,
								final M modelObj) {
		return _doUpdateOrCreate(userContext,
						  		 modelObj,
						  		 PersistenceRequestedOperation.CREATE);
	}
	@Override 
	public CRUDResult<M> update(final UserContext userContext,
								final M modelObj) {
		return _doUpdateOrCreate(userContext,
						  		 modelObj,
						  		 PersistenceRequestedOperation.UPDATE);
	}
	@SuppressWarnings("unchecked")
	private CRUDResult<M> _doUpdateOrCreate(final UserContext userContext,
											final M modelObj,
											final PersistenceRequestedOperation requestedOperation) {
		// [0] - check the entity
		if (modelObj == null) {
			return CRUDResultBuilder.using(userContext)
								       .on(_modelObjectType)
								       .badClientRequestData(PersistenceRequestedOperation.CREATE,
								    		 			     "The {} entity cannot be null in order to be {}ed",_modelObjectType,requestedOperation.name().toLowerCase())
								    		 .about(modelObj).build();
		}
		// [1] check that it's NOT in read only status
		CRUDResult<M> outOpResult = _errorIfReadOnlyOrNull(userContext,
														   requestedOperation,
														   modelObj);
		if (outOpResult != null) return outOpResult;
		
		// [2] ensure that the new object has an oid
		if (requestedOperation == PersistenceRequestedOperation.CREATE 
		 && modelObj.getOid() == null) {
			// ensure that the entity has an oid if the OID is NOT generated at DB-level
			Class<? extends OID> oidType = OIDs.oidTypeFor(_modelObjectType);
			if (!ReflectionUtils.isImplementing(oidType,OIDIsGenerated.class)) {
				O oid = (O)OIDs.supplyOid(oidType);				
				modelObj.setOid(oid);
				log.debug("The entity to be created does NOT have the oid set so a new one is generated: {}",oid);
			}
		} else if (requestedOperation == PersistenceRequestedOperation.UPDATE
				&& modelObj.getOid() == null) {
			throw new IllegalArgumentException("The entity to be updated does NOT have an OID, is it maybe a create operation?");
		}
		
		// [3] model object validation and create the object at the persistent store
		M theModelObjToPersist = modelObj;
		if (this instanceof CompletesModelObjectBeforeCreateOrUpdate) {
			CompletesModelObjectBeforeCreateOrUpdate<M> completes = (CompletesModelObjectBeforeCreateOrUpdate<M>)this;
			theModelObjToPersist = completes.completeModelObjBeforeCreateOrUpdate(userContext,
																	  			  requestedOperation,
																	  			  modelObj);
		}
		outOpResult = _errorIfNOTValidOrNull(userContext,
											 requestedOperation,
											 theModelObjToPersist);
		if (outOpResult != null) return outOpResult;

		// [4] create
		if (requestedOperation == PersistenceRequestedOperation.CREATE) {
			outOpResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
									.create(userContext,
									   		theModelObjToPersist);
		} else if (requestedOperation == PersistenceRequestedOperation.UPDATE) {
			outOpResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
									.update(userContext,
									   		theModelObjToPersist);
		}
		// [5] throw CRUD event
		_fireEvent(userContext,
				   outOpResult);
		// [6] return
		return outOpResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DELETE
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDResult<M> delete(final UserContext userContext,
								final O oid) {
		// [0] - check the entity
		if (oid == null) {
			return CRUDResultBuilder.using(userContext)
											   .on(_modelObjectType)
											   .badClientRequestData(PersistenceRequestedOperation.DELETE,
											    		 			 "The {} entity's oid cannot be null in order to be created",_modelObjectType)
											    		.about(oid).build();
		}
		// [1] check that it's NOT in read only status
		CRUDResult<M> outOpResult = _errorIfReadOnlyOrNull(userContext,
																	  PersistenceRequestedOperation.DELETE,
																	  oid);
		if (outOpResult != null) return outOpResult;
		
		// [2] delete
		outOpResult = this.getServiceImplAs(CRUDServicesForModelObject.class)
								.delete(userContext,
							  	   		oid);
		// [4] throw CRUD event
		_fireEvent(userContext,
				   outOpResult);
		// [5] return 
		return outOpResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private CRUDResult<M> _errorIfReadOnlyOrNull(final UserContext userContext,
												 final PersistenceRequestedOperation reqOp,
												 final M modelObject) {
		CRUDResult<M> outError = null;
		if (_readOnly) {
			if (reqOp.is(PersistenceRequestedOperation.CREATE)) {
				outError = CRUDResultBuilder.using(userContext)
													   .on(_modelObjectType)
													   .notCreated()
													   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
													   		.about(modelObject).build();
			} else if (reqOp.is(PersistenceRequestedOperation.UPDATE)) {
				outError = CRUDResultBuilder.using(userContext)
													   .on(_modelObjectType)
													   .notUpdated()
													   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
													   		.about(modelObject).build();
			}
		}
		return outError;
	}
	private CRUDResult<M> _errorIfReadOnlyOrNull(final UserContext userContext,
															  final PersistenceRequestedOperation reqOp,
															  final O oid) {
		CRUDResult<M> outError = null;
		if (_readOnly) {
			if (reqOp.is(PersistenceRequestedOperation.UPDATE)) {
				outError = CRUDResultBuilder.using(userContext)
													   .on(_modelObjectType)
													   .notUpdated()
													   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
													   		.about(oid).build();
			} else if (reqOp.is(PersistenceRequestedOperation.DELETE)) {
				outError = CRUDResultBuilder.using(userContext)
													   .on(_modelObjectType)
													   .notDeleted()
													   .becauseClientBadRequest("The CRUD services object is in READ-ONLY status!")
													   		.about(oid).build();
			}
		}
		return outError;
	}
	/**
	 * Invokes the _validateModelObjectBeforeCreateOrUpdate and return a {@link PersistenceOperationError} if it returns a NOT VALID result
	 * @param userContext
	 * @param reqOp
	 * @param modelObj
	 * @return
	 */
	@SuppressWarnings("unchecked")
	private CRUDResult<M> _errorIfNOTValidOrNull(final UserContext userContext,
												 final PersistenceRequestedOperation reqOp,
												 final M modelObj) {
		ObjectValidationResult<M> valid = null;
		
		// model object self validation
		if (ReflectionUtils.isImplementing(_modelObjectType,SelfValidates.class)) {
			valid = ((SelfValidates<M>)modelObj).validate();
		}
		// service logic validation
		if (valid != null && valid.isValid() || valid == null) {
			if (this instanceof ValidatesModelObjectBeforeCreateOrUpdate) {
				// Validation is being used
				ValidatesModelObjectBeforeCreateOrUpdate<M> validates = (ValidatesModelObjectBeforeCreateOrUpdate<M>)this;
				valid = validates.validateModelObjBeforeCreateOrUpdate(userContext,
																	   reqOp,
																	   modelObj);
			} 
		}
		
		// return 
		CRUDResult<M> outError = null;
		if (valid != null && valid.isNOTValid()) {
			if (reqOp.is(PersistenceRequestedOperation.CREATE)) {
				outError = CRUDResultBuilder.using(userContext)
													   .on(_modelObjectType)
													   .notCreated()
													   .becauseClientSentEntityValidationErrors(valid.asNOKValidationResult())
													   		.about(modelObj).build();
			} else if (reqOp.is(PersistenceRequestedOperation.UPDATE)) {
				outError = CRUDResultBuilder.using(userContext)
													   .on(_modelObjectType)
													   .notUpdated()
													   .becauseClientSentEntityValidationErrors(valid.asNOKValidationResult())
													   		.about(modelObj).build();
			}
		}
		return outError;
	}
}
