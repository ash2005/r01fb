package r01f.test.persistence;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.junit.Assert;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.patterns.CommandOn;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Stopwatch;

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
		System.out.println("[init][TEST BASIC PERSISTENCE]-----------------------------------------------------------------------");
		
		Stopwatch stopWatch = Stopwatch.createStarted();
		
		// [1] Create an entity
		System.out.println("CREATE AN ENTITY OF TYPE " + _modelObjFactory.getModelObjType() + "_________________________________");
		_modelObjFactory.setUpMockModelObjs(1);
		O createdModelObjOid = _modelObjFactory.getAnyCreatedModelObjectOid();
		M createdModelObj = _crudAPI.load(createdModelObjOid);	// load the created obj

		System.out.println("---->Version id=" + createdModelObj.getEntityVersion());
		
		Assert.assertNotNull(createdModelObj);
		long initialDBVersion = createdModelObj.getEntityVersion();
		
		// [2] Try to update the entity not having modified it: This should not do anything since nothing was modified
		System.out.println("SAVE WITHOUT MODIFY THE ENTITY OF TYPE " + _modelObjFactory.getModelObjType() + " ___________________");
		M notUpdatedModelObj = _crudAPI.save(createdModelObj);
		
		Assert.assertNotNull(notUpdatedModelObj);
		long notUpdatedDBVersion = notUpdatedModelObj.getEntityVersion();
		Assert.assertEquals(initialDBVersion,notUpdatedDBVersion);		// the DB version MUST remain (NO CRUD operation was issued)
		
		
		// [3]  Update the entity
		System.out.println("SAVE MODIFYING THE ENTITY OF TYPE " + _modelObjFactory.getModelObjType() + " ________________________");
		modelObjectStateUpdateCommand.executeOn(createdModelObj);
		M updatedModelObj = _crudAPI.save(createdModelObj);
				
		Assert.assertNotNull(updatedModelObj);
		long updatedDBVersion = updatedModelObj.getEntityVersion();
		Assert.assertNotEquals(initialDBVersion,updatedDBVersion);		// the DB version MUST NOT be the same (an UPDATE was issued)
		
		// [4] Load the modified creation request
		System.out.println("LOAD THE ENTITY OF TYPE " + _modelObjFactory.getModelObjType() + " WITH oid=" + createdModelObj.getOid() + " _________");
		M loadedModelObj = _crudAPI.load(createdModelObjOid);		
		System.out.println(">>>" + createdModelObjOid + " version=" + loadedModelObj.getEntityVersion());
		
		Assert.assertNotNull(loadedModelObj);
		long loadDBVersion = updatedModelObj.getEntityVersion();
		Assert.assertEquals(updatedDBVersion,loadDBVersion);		
		
//		// [5] Test Optimistic locking
//		System.out.println("[Optimistic Locking (this should fail)]");
//		loadedModelObj.setEntityVersion(100);		// setting the entityVersion at the client would BREAK the persisted version sequence so an exception should be raised
//		try {
//			_crudAPI.save(loadedModelObj);
//		} catch(Exception ex) {
//			System.out.println("\tFAILED!! the db's version is NOT the same as the client-provided one!");
//		}
		
		// [6] Delete the entity
		// wait for background jobs to complete (if there's any background job that depends on DB data -like lucene indexing-
		// 										 if the DB data is deleted BEFORE the background job finish, it'll fail)
		System.out.println("DELETE THE ENTITY OF TYPE " + _modelObjFactory.getModelObjType() + " WITH oid=" + createdModelObj.getOid() + " _______");
		_modelObjFactory.tearDownCreatedMockModelObjs();
		
		// try to load the deleted object... it must NOT exist
		loadedModelObj = _crudAPI.loadOrNull(createdModelObjOid);
		Assert.assertNull(loadedModelObj);
		
		// WARNING!!! There's NO need to call _modelObjFactory.tearDownCreatedMockModelObjs() because all created model objs 
		//			  have been removed
		// _modelObjFactory.tearDownCreatedMockModelObjs();
		
		System.out.println("[end ][TEST BASIC PERSISTENCE] (elapsed time: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)) + " milis) -------------------------");
	}
}
