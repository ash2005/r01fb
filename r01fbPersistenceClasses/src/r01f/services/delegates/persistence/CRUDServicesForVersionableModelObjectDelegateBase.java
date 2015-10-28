package r01f.services.delegates.persistence;

import java.util.Date;

import lombok.extern.slf4j.Slf4j;
import r01f.events.PersistenceOperationEvents.PersistenceOperationErrorEvent;
import r01f.events.PersistenceOperationEvents.PersistenceOperationOKEvent;
import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.CRUDError;
import r01f.persistence.CRUDOnMultipleEntitiesError;
import r01f.persistence.CRUDOnMultipleEntitiesOK;
import r01f.persistence.CRUDOnMultipleEntitiesResult;
import r01f.persistence.CRUDResult;
import r01f.persistence.CRUDResultBuilder;
import r01f.persistence.PersistenceOperationError;
import r01f.persistence.PersistenceOperationOK;
import r01f.persistence.PersistenceRequestedOperation;
import r01f.persistence.db.DBCRUDForVersionableModelObject;
import r01f.services.interfaces.CRUDServicesForVersionableModelObject;
import r01f.usercontext.UserContext;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.eventbus.EventBus;

@Slf4j
public abstract class CRUDServicesForVersionableModelObjectDelegateBase<O extends OIDForVersionableModelObject,M extends PersistableModelObject<O> & HasVersionableFacet> 
	          extends CRUDServicesForModelObjectDelegateBase<O,M> 
		   implements CRUDServicesForVersionableModelObject<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDServicesForVersionableModelObjectDelegateBase(final Class<M> modelObjectType,
													  		 final DBCRUDForVersionableModelObject<O,M> crud,
													  		 final EventBus eventBus) {
		super(modelObjectType,
			  crud,
			  eventBus);
	}
	public CRUDServicesForVersionableModelObjectDelegateBase(final Class<M> modelObjectType,
													  		 final DBCRUDForVersionableModelObject<O,M> crud) {
		super(modelObjectType,
			  crud,
			  null);	// no event bus
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LOAD
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadActiveVersionAt(final UserContext userContext,
						   					 final VersionIndependentOID oid,final Date date) {
		CRUDResult<M> outEntityLoadResult = null;
		
		// [0] - Check the parameters
		Date theDate = date != null ? date : new Date();
		if (oid == null) {
			outEntityLoadResult = CRUDResultBuilder.using(userContext)
															  .on(_modelObjectType)
															  .badClientRequestData(PersistenceRequestedOperation.LOAD,
																	  				"Cannot load {} entity since either the version independent oid is null",_modelObjectType)
																	  .about(oid,theDate);
		}
		// [1] - Do load
		outEntityLoadResult = this.getServiceImplAs(CRUDServicesForVersionableModelObject.class)
										.loadActiveVersionAt(userContext,
															 oid,theDate);
		return outEntityLoadResult;
	}
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> loadWorkVersion(final UserContext userContext,
							 			 final VersionIndependentOID oid) {
		CRUDResult<M> outEntityLoadResult = null;
		
		// [0] - Param checking
		if (oid == null) {
			outEntityLoadResult = CRUDResultBuilder.using(userContext)
															  .on(_modelObjectType)
															  .badClientRequestData(PersistenceRequestedOperation.LOAD,
																	  				"Cannot load {} work version entity since the version independent oid is null",_modelObjectType)
																	  .aboutWorkVersion(oid);
		}
		// [1] - Do load
		outEntityLoadResult = this.getServiceImplAs(CRUDServicesForVersionableModelObject.class)
									.loadWorkVersion(userContext,
		   									     	 oid);
		return outEntityLoadResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	DELETE 
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDOnMultipleEntitiesResult<M> deleteAllVersions(final UserContext userContext,
															 final VersionIndependentOID oid) {
		// [0] - Check the version info
		if (oid == null) {
			CRUDResultBuilder.using(userContext)
									    .on(_modelObjectType)
									    .badClientRequestData(PersistenceRequestedOperation.DELETE,
											  				  "Cannot delete all {} entity versions since the provided entity oid is null",_modelObjectType);
		}
		// [1] - Delete
		CRUDOnMultipleEntitiesResult<M> outResults = this.getServiceImplAs(CRUDServicesForVersionableModelObject.class)
																.deleteAllVersions(userContext,
							          					   	  	   				   oid);
		// [2] - Throw an event for each successful deletion 
		//		 and another for each deletion failure
		_fireEvents(userContext,
					outResults);
		
		return outResults;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public CRUDResult<M> activate(final UserContext userContext,
								  final M entityToBeActivated) {
		// [0] - Check params
		if (entityToBeActivated == null) {
			return CRUDResultBuilder.using(userContext)
											   .on(_modelObjectType)
											   .badClientRequestData(PersistenceRequestedOperation.CREATE,
													  				 "The {} entity cannot be null in order to activate that version",_modelObjectType)
													  .about(entityToBeActivated);
		}
		
		// [1] - Check that the requested version exists and is in DRAFT mode
		if (_readOnly) throw new IllegalStateException("The CRUD services object is in READ-ONLY status!");
		CRUDResult<M> storedEntityToBeActivatedLoad = this.getServiceImplAs(CRUDServicesForVersionableModelObject.class)
															.load(userContext,
																  entityToBeActivated.getOid());
		if (storedEntityToBeActivatedLoad.hasSucceeded()) {
			// the version exists... check that it's in draft mode
			if (!storedEntityToBeActivatedLoad.getOrThrow()
											  .asVersionable().isDraft()) {
				return CRUDResultBuilder.using(userContext)
												   .on(_modelObjectType)
												   .notUpdated()
											   	   .becauseTargetEntityWasInAnIllegalStatus("The entity with oid={} is NOT in draft mode. It cannot be activated!",
												  					  		  				entityToBeActivated.getOid())
												  		.about(entityToBeActivated.getOid());
													  			
			}
		} 
		// if the error is OTHER than the version to be activated does NOT exists
		else if (!storedEntityToBeActivatedLoad.asError()		// as(CRUDError.class)
											   .wasBecauseClientRequestedEntityWasNOTFound()) {
			return CRUDResultBuilder.using(userContext)
											   .on(_modelObjectType)
											   .notUpdated()
											   .because(storedEntityToBeActivatedLoad.asError()							// as(CRUDError.class)
													   								 .getPersistenceException())
											   		.about(entityToBeActivated.getOid());
		}
		
		// [2] the activation date is right now!
		Date activationDate = new Date();
		
		// [3] Find the currently active version and if it exists override it with the new version
		CRUDResult<M> currentlyActiveEntityLoad = this.getServiceImplAs(CRUDServicesForVersionableModelObject.class)
															.loadActiveVersionAt(userContext,
			   						     							   	   		 entityToBeActivated.getOid().getOid(),	// version independent oid
			   						     							   	   		 new Date());							// this moment active entity
		if (currentlyActiveEntityLoad.hasSucceeded()) {
			Date passivationDate = activationDate;
			
			// the currently active version exists... passivate it
			M currentlyActiveEntity = currentlyActiveEntityLoad.getOrThrow();		// gets the record or throws an exception 
			log.warn("{}'s with oid={}, is currently active and will be pasivated and {} will be activated",
					 _modelObjectType,
					 currentlyActiveEntity,
					 entityToBeActivated.getOid());
			
			// The currently active version must be overridden by the new version
			currentlyActiveEntity.asVersionable()
								 .overrideBy(entityToBeActivated.getOid().getVersion(),
											 passivationDate);	// passivated at the same time that the other entity is activated
			CRUDResult<M> currentlyActiveEntityPassivation = this.getServiceImplAs(CRUDServicesForVersionableModelObject.class)
																	.update(userContext,
								   									     	currentlyActiveEntity);
			// send event about passivated record update
			_fireEvent(userContext,
					   currentlyActiveEntityPassivation);
			
			M currentlyActiveRecordPassivated = null;
			if (currentlyActiveEntityPassivation.hasSucceeded()) {
				// passivation OK
				currentlyActiveRecordPassivated = currentlyActiveEntityPassivation.getOrThrow();
				if (!currentlyActiveRecordPassivated.asVersionable().isActive()) log.debug("... pasivation ok");
			} else {
				// passivation NOK
				CRUDError<M> persistError = currentlyActiveEntityPassivation.asError();		// as(CRUDError.class)
				return CRUDResultBuilder.using(userContext)
												   .on(_modelObjectType)
												   .notUpdated()
												   .because(persistError.getPersistenceException())
												   		.about(entityToBeActivated.getOid());
			}
		}
		// [4] Activate the version
		entityToBeActivated.asVersionable()
						   .activate(activationDate);
		CRUDResult<M> activationResult = this.getServiceImplAs(CRUDServicesForVersionableModelObject.class)
													.update(userContext,
						   							  	 	entityToBeActivated);
		// [5] send event
		_fireEvent(userContext,
				   activationResult);
		
		// [6] Return 
		return activationResult;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	EVENT FIRING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Throws an {@link EventBus} event
	 * @param userContext
	 * @param opResult
	 */
	protected void _fireEvents(final UserContext userContext,
							   final CRUDOnMultipleEntitiesResult<M> opResults) {
		if (this.getEventBus() == null) return;		// do nothing
		
		log.debug("Publishing events for: {}",opResults.getClass());
		if (opResults.hasFailed()) {
			CRUDOnMultipleEntitiesError<M> opNOK = opResults.asError();		// as(CRUDOnMultipleEntitiesError.class)
			PersistenceOperationErrorEvent errorEvent = new PersistenceOperationErrorEvent(userContext,
												 					         	 		   opNOK);
			this.getEventBus().post(errorEvent);
			
		} else if (opResults.hasSucceeded()) {
			CRUDOnMultipleEntitiesOK<M> opsPerformed = opResults.asOK();	// as(CRUDOnMultipleEntitiesOK.class)
			// Post OK results
			if (CollectionUtils.hasData(opsPerformed.getOperationsOK())) {
				for (PersistenceOperationOK opOk : opsPerformed.getOperationsOK()) {
					PersistenceOperationOKEvent okEvent = new PersistenceOperationOKEvent(userContext,
														 					      	  	  opOk);
					this.getEventBus().post(okEvent);
				}
			}
			// Post NOK results
			if (CollectionUtils.hasData(opsPerformed.getOperationsNOK())) {
				for (PersistenceOperationError opNOK : opsPerformed.getOperationsNOK()) {
					PersistenceOperationErrorEvent okEvent = new PersistenceOperationErrorEvent(userContext,
														 					      	  	  		opNOK);
					this.getEventBus().post(okEvent);
				}				
			}
			
		} 
	}
}
