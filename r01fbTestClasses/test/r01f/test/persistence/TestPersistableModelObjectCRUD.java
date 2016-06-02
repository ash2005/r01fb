package r01f.test.persistence;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;

import com.google.common.base.Stopwatch;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.patterns.CommandOn;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;

@Slf4j
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class TestPersistableModelObjectCRUD<O extends OID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final ClientAPIDelegateForModelObjectCRUDServices<O,M> _crudAPI;
	private final TestPersistableModelObjectFactory<O,M> _modelObjFactory;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends OID,M extends PersistableModelObject<O>> TestPersistableModelObjectCRUD<O,M> create(final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
																												 final TestPersistableModelObjectFactory<O,M> modelObjFactory) {
		return new TestPersistableModelObjectCRUD<O,M>(crudAPI,
													   modelObjFactory);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tests the CRUD API (creates an entity, updates it, loads it and finally deletes it)
	 * @param modelObject
	 */
	public void testPersistence(final CommandOn<M> modelObjectStateUpdateCommand) {		
		log.warn("[init][TEST BASIC PERSISTENCE {}]-----------------------------------------------------------------------",
				 _modelObjFactory.getModelObjType());
		
		Stopwatch stopWatch = Stopwatch.createStarted();
		
		// [1] Test create
		M createdModelObj = this.testCreate();
		
		// [2] Test update
		M updatedModelObj = this.testUpdate(createdModelObj,
											modelObjectStateUpdateCommand);
		
		// [3] Delete the entity
		this.testDelete(updatedModelObj);
		
		// WARNING!!! There's NO need to call _modelObjFactory.tearDownCreatedMockModelObjs() because all created model objs 
		//			  have been removed
		// _modelObjFactory.tearDownCreatedMockModelObjs();
		
		log.warn("[end ][TEST BASIC PERSISTENCE {}] (elapsed time: {} milis) -------------------------",
				 _modelObjFactory.getModelObjType(),
				 NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public M testCreate() {
		// Create an entity
		log.warn("\tCREATE AN ENTITY OF TYPE {}",
				 _modelObjFactory.getModelObjType());
		_modelObjFactory.setUpMockModelObjs(1);
		
		O createdModelObjOid = _modelObjFactory.getAnyCreatedModelObjectOid();
		M createdModelObj = _crudAPI.load(createdModelObjOid);	// load the created obj
		
		log.debug("\t...created entity version id={}",createdModelObj.getEntityVersion());
		
		Assert.assertNotNull(createdModelObj);
		
		return createdModelObj;
	}
	public M testUpdate(final M modelObjToUpdate,
						final CommandOn<M> modelObjectStateUpdateCommand) {
		long initialDBVersion = modelObjToUpdate.getEntityVersion();
		
		// [a] Try to update the entity not having modified it: This should not do anything since nothing was modified
		log.warn("\tSAVE WITHOUT MODIFY THE ENTITY OF TYPE {} with oid={}",
				 _modelObjFactory.getModelObjType(),modelObjToUpdate.getOid());
		M notUpdatedModelObj = _crudAPI.save(modelObjToUpdate);
		
		Assert.assertNotNull(notUpdatedModelObj);
		long notUpdatedDBVersion = notUpdatedModelObj.getEntityVersion();
		Assert.assertEquals(initialDBVersion,notUpdatedDBVersion);		// the DB version MUST remain (NO CRUD operation was issued)
		
		
		// [b]  Update the entity
		log.warn("\tSAVE MODIFYING THE ENTITY OF TYPE {} with oid={}",
				 _modelObjFactory.getModelObjType(),modelObjToUpdate.getOid());
		modelObjectStateUpdateCommand.executeOn(modelObjToUpdate);
		M updatedModelObj = _crudAPI.save(modelObjToUpdate);
				
		Assert.assertNotNull(updatedModelObj);
		long updatedDBVersion = updatedModelObj.getEntityVersion();
		if (updatedDBVersion > 0) Assert.assertNotEquals(initialDBVersion,updatedDBVersion);		// the DB version MUST NOT be the same (an UPDATE was issued)
		
		// [c] Load the modified model object
		log.warn("\tLOAD THE ENTITY OF TYPE {} WITH oid={}",_modelObjFactory.getModelObjType(),modelObjToUpdate.getOid());
		M loadedModelObj = _crudAPI.load(modelObjToUpdate.getOid());		
		log.debug("\t...updated entity with oid={} and version={}",modelObjToUpdate.getOid(),loadedModelObj.getEntityVersion());
		
		Assert.assertNotNull(loadedModelObj);
		long loadDBVersion = updatedModelObj.getEntityVersion();
		if (loadDBVersion > 0) Assert.assertEquals(updatedDBVersion,loadDBVersion);
		
//		// [d] Test Optimistic locking
//		log.info("[Optimistic Locking (this should fail)]");
//		loadedModelObj.setEntityVersion(100);		// setting the entityVersion at the client would BREAK the persisted version sequence so an exception should be raised
//		try {
//			_crudAPI.save(loadedModelObj);
//		} catch(Exception ex) {
//			System.out.println("\tFAILED!! the db's version is NOT the same as the client-provided one!");
//		}
		
		return loadedModelObj;
	}
	public void testDelete(final M modelObjectToDelete) {
		// wait for background jobs to complete (if there's any background job that depends on DB data -like lucene indexing-
		// 										 if the DB data is deleted BEFORE the background job finish, it'll fail)
		log.warn("\tDELETE THE ENTITY OF TYPE {} WITH oid={}",
				 _modelObjFactory.getModelObjType(),modelObjectToDelete.getOid());
		_modelObjFactory.tearDownCreatedMockModelObjs();
		
		// try to load the deleted object... it must NOT exist
		M shouldNotExists = _crudAPI.loadOrNull(modelObjectToDelete.getOid());
		Assert.assertNull(shouldNotExists);
	}
}
