package r01f.services.client.api.delegates;

import lombok.extern.slf4j.Slf4j;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDResult;
import r01f.persistence.PersistenceException;
import r01f.services.interfaces.CRUDServicesForModelObject;
import r01f.types.dirtytrack.DirtyTrackAdapter;
import r01f.usercontext.UserContext;

/**
 * Adapts Persistence API method invocations to the service proxy that performs the core method invocations
 * @param <O>
 * @param <M>
 */
@Slf4j
public abstract class ClientAPIDelegateForModelObjectCRUDServices<O extends OID,M extends PersistableModelObject<O>> 
	          extends ClientAPIServiceDelegateBase<CRUDServicesForModelObject<O,M>> {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIDelegateForModelObjectCRUDServices(final UserContext userContext,
												   	   final CRUDServicesForModelObject<O,M> services) {
		super(userContext,
			  services);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Loads a record
	 * @param oid
	 * @return
	 * @throws PersistenceException 
	 */
	public M load(final O oid) throws PersistenceException {
		CRUDResult<M> loadOpResult = this.getServiceProxy()
												.load(this.getUserContext(),
	 	  		 								      oid);
		log.debug(loadOpResult.debugInfo().toString());
		
		M outRecord = loadOpResult.getOrThrow();	
		if (outRecord instanceof DirtyStateTrackable) {
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnLoaded(outRecord);
		}
		
		return outRecord;
	}
	/**
	 * The normal load method throws a {@link PersistenceException} if the requested record
	 * is NOT found
	 * This method simply returns null and does NOT throw a {@link PersistenceException} if the requestedRecord
	 * is NOT found
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public M loadOrNull(final O oid) throws PersistenceException {
		M outRecord = null;
		try {
			outRecord = this.load(oid);
		} catch(PersistenceException persistEx) {
			if (!persistEx.isEntityNotFound()) throw persistEx;  
		}
		return outRecord;
	}
	/**
	 * Checks a record existence
	 * @param oid
	 * @return
	 * @throws PersistenceException
	 */
	public boolean exists(final O oid) throws PersistenceException {
		return this.loadOrNull(oid) != null;
	}
	/**
	 * Updates a record. This method is usually used when {@link DirtyStateTrackable} aspect is NOT being used
	 * @param record
	 * @return
	 * @throws PersistenceException
	 */
	public M update(final M record) throws PersistenceException {
		// If the record is a DirtyStateTrackable instance check that the instance is NEW
		if (record instanceof DirtyStateTrackable) {
			DirtyStateTrackable trckReceivedRecord = DirtyTrackAdapter.adapt(record);
			if (trckReceivedRecord.getTrackingStatus().isThisNew()) throw new IllegalStateException(Throwables.message("{} instance new... maybe you have to call create() or save() method instead of update",
																													   record.getClass()));
		}
		// Do the update
		CRUDResult<M> saveOpResult = this.getServiceProxy()
											.update(this.getUserContext(),
						 				   		  	record);
		M outRecord = saveOpResult.getOrThrow();
		
		// Adapt the returned object
		if (outRecord != null && outRecord instanceof DirtyStateTrackable && record instanceof DirtyStateTrackable) {
			// [2.1]- Update the returned object dirty status AND the received object dirty status just for the case the caller continues using this instance instead of 
			//		  the received by the server
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnSaved(outRecord);
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnSaved(record);
		}
		return outRecord;
	}
	/**
	 * Creates a record. This method is usually used when {@link DirtyStateTrackable} aspect is NOT being used
	 * @param record
	 * @return
	 * @throws PersistenceException
	 */
	public M create(final M record) throws PersistenceException {
		// If the record is a DirtyStateTrackable instance check that the instance is NEW
		if (record instanceof DirtyStateTrackable) {
			DirtyStateTrackable trckReceivedRecord = DirtyTrackAdapter.adapt(record);
			if (!trckReceivedRecord.getTrackingStatus().isThisNew()) throw new IllegalStateException(Throwables.message("{} instance is NOT new... maybe you have to call update() or save() method instead of create",
																													    record.getClass()));
		}
		// Do the creation
		CRUDResult<M> saveOpResult = this.getServiceProxy()
											.create(this.getUserContext(),
						 				   		  	record);
		M outRecord = saveOpResult.getOrThrow();
		// Adapt the returned object
		if (outRecord != null && outRecord instanceof DirtyStateTrackable && record instanceof DirtyStateTrackable) {
			// [2.1]- Update the returned object dirty status AND the received object dirty status just for the case the caller continues using this instance instead of 
			//		  the received by the server
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnSaved(outRecord);
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnSaved(record);
		}
		return outRecord;
	}
	/**
	 * Saves a record. This method MUST be used when the model object is weaved with the {@link DirtyStateTrackable} aspect.
	 * If weaving is NOT being used, use the create() or update() methods instead
	 * @param record
	 * @return
	 * @throws PersistenceException 
	 */
	public M save(final M record) throws PersistenceException {
		if (!(record instanceof DirtyStateTrackable)) throw new IllegalStateException(Throwables.message("{} is NOT a {} instance... " +
																										 "maybe the {} aspect is NOT weaved because the JVM is NOT started with the -javaagent:aspectjweaver.jar, " +
																										 "or maybe if weaving is NOT an option, the create() or update() method should be used instead of the save() method", 
																										 record.getClass(),DirtyStateTrackable.class.getSimpleName(),
																										 ConvertToDirtyStateTrackable.class.getSimpleName()));
		// [0]- Get a trackable version of the record
		DirtyStateTrackable trckReceivedRecord = DirtyTrackAdapter.adapt(record);
	
		M outRecord = null; 
		CRUDResult<M> saveOpResult = null;
		
		// [1]- Check if the record is dirty (is changed)		
		if (trckReceivedRecord.getTrackingStatus().isThisNew()) {
			// [1.1] - the record is new
			saveOpResult = this.getServiceProxy()
										.create(this.getUserContext(),
					 				   		  	record);
			outRecord = saveOpResult.getOrThrow();
			
		} else if (trckReceivedRecord.isDirty()) {
			// [1.2] - The record already existed (it's an update)
			saveOpResult = this.getServiceProxy()
										.update(this.getUserContext(),
										 		record);
			outRecord = saveOpResult.getOrThrow();
			
		} else {
			// [1.3] - Nothing was done with the record
			log.warn("Record of type {} NOT updated, maybe you do NOT have to call api.save()",record.getClass());
			outRecord = record;
		}	
		// some debugging
		if (saveOpResult != null) log.debug(saveOpResult.debugInfo().toString());
		
		// [2] - Adapt the returned object
		if (outRecord != null) {
			// [2.1]- Update the returned object dirty status AND the received object dirty status just for the case the caller continues using this instance instead of 
			//		  the received by the server
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnSaved(outRecord);
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnSaved(record);
		}
		return outRecord;
	}
	/**
	 * Deletes a record
	 * @param record 
	 * @return the deleted record if the operation was successful, null otherwise
	 * @throws PersistenceException 
	 */
	public M delete(final M record) throws PersistenceException {
		return this.delete(record.getOid());
	}
	/**
	 * Deletes a record
	 * @param recordOid 
	 * @return the deleted record if the operation was successful, null otherwise
	 * @throws PersistenceException 
	 */
	public M delete(final O recordOid) throws PersistenceException {
		// [1] - Delete the record
		CRUDResult<M> deleteOpResult = this.getServiceProxy()
												.delete(this.getUserContext(),
	  	    										   	recordOid);
		log.debug(deleteOpResult.debugInfo().toString());
		
		// [2] - If the record has been deleted, it's a new record
		M outRecord = deleteOpResult.getOrThrow();
		
		if (outRecord instanceof DirtyStateTrackable) {
			ClientAPIModelObjectChangesTrack.startTrackingChangesOnDeleted(outRecord);
		}
		// [2] - Return the deleted record
		return outRecord;
	}
}
