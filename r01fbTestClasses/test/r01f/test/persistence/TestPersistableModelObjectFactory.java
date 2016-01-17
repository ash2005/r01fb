package r01f.test.persistence;

import java.util.Collection;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.concurrent.Threads;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.types.Factory;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
public class TestPersistableModelObjectFactory<O extends OID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Class<M> _modelObjType;
	@Getter private final Factory<M> _mockObjectsFactory;
	@Getter private final ClientAPIDelegateForModelObjectCRUDServices<O,M> _CRUDApi;
	@Getter private final long _milisToWaitForBackgroundJobs;
	
	@Getter private Collection<O> _createdMockModelObjectsOids; 
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS TO USE WHEN THERE'RE BACKGROUND JOBS (ie indexing)
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectFactory(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory,
												 		final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
												 		final long milisToWaitForBackgroundJobs) {
		_modelObjType = modelObjType;
		_mockObjectsFactory = mockObjectsFactory;
		_CRUDApi = crudAPI;
		_milisToWaitForBackgroundJobs = milisToWaitForBackgroundJobs;
	}
	public TestPersistableModelObjectFactory(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory,
														final long milisToWaitForBackgroundJobs) {
		this(modelObjType,
			 mockObjectsFactory,
			 null,
			 milisToWaitForBackgroundJobs);
	}
	public TestPersistableModelObjectFactory(final Class<M> modelObjType,
														final long milisToWaitForBackgroundJobs) {
		this(modelObjType,
			 null,
			 null,
			 milisToWaitForBackgroundJobs);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS TO USE WHEN THERE'RE NO BACKGROUND JOBS 
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectFactory(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory,
												 		final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		_modelObjType = modelObjType;
		_mockObjectsFactory = mockObjectsFactory;
		_CRUDApi = crudAPI;
		_milisToWaitForBackgroundJobs = 0L;
	}
	public TestPersistableModelObjectFactory(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory) {
		this(modelObjType,
			 mockObjectsFactory,
			 null,
			 0L);
	}
	public TestPersistableModelObjectFactory(final Class<M> modelObjType) {
		this(modelObjType,
			 null,
			 null,
			 0L);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends OID,M extends PersistableModelObject<O>> TestPersistableModelObjectFactory<O,M> create(final Class<M> modelObjType,
																													final Factory<M> mockObjectsFactory,
																													final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
																													final long milisToWaitForBackgroundJobs) {
		return new TestPersistableModelObjectFactory<O,M>(modelObjType,mockObjectsFactory,
															  		 crudAPI,
															  		 milisToWaitForBackgroundJobs);
	}
	public static <O extends OID,M extends PersistableModelObject<O>> TestPersistableModelObjectFactory<O,M> create(final Class<M> modelObjType,
																													final Factory<M> mockObjectsFactory,
																													final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		return new TestPersistableModelObjectFactory<O,M>(modelObjType,mockObjectsFactory,
															  		 crudAPI,
															  		 0L);		// no need to wait for crud-associated background jobs
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a number of model objects using the provided factory
	 * @param numOfObjectsToCreate
	 * @return the created objects
	 */
	public void setUpMockModelObjs(final int numOfObjsToCreate) {
		// create test model objects
		_createdMockModelObjectsOids = Lists.newArrayListWithExpectedSize(numOfObjsToCreate);
		for (int i=0; i < numOfObjsToCreate; i++) {
			M modelObjectToBeCreated = _mockObjectsFactory.create();
			_CRUDApi.save(modelObjectToBeCreated);
			_createdMockModelObjectsOids.add(modelObjectToBeCreated.getOid());
			System.out.println("... Created " + _modelObjType.getSimpleName() + " mock object with oid=" + modelObjectToBeCreated.getOid());
		}
	}
	/**
	 * Deletes a {@link Collection} of previously created objects
	 * @param createdObjs
	 */
	public void tearDownCreatedMockModelObjs() {
		if (CollectionUtils.isNullOrEmpty(_createdMockModelObjectsOids)) return;
		
		// wait for background jobs to finish
		// an error in the background job will raise if the DB records are deleted before background jobs finish (ie lucene indexing or notification tasks)
		long milisToWaitForBackgroundJobs = _createdMockModelObjectsOids.size() * _milisToWaitForBackgroundJobs;
		if (milisToWaitForBackgroundJobs > 0) {
			System.out.println(".... give " + milisToWaitForBackgroundJobs + " milis for background jobs (ie lucene index or notifications) to complete before deleting created DB records (lucene indexing or notifications will fail if the DB record is deleted)");
			Threads.safeSleep(milisToWaitForBackgroundJobs);
		}
		
		// delete all DB records
		for (O oid : _createdMockModelObjectsOids) {
			_CRUDApi.delete(oid);
			System.out.println("... Deleted " + _modelObjType.getSimpleName() + " mock object with oid=" + oid);
		}
		this.reset();
	}
	/**
	 * Reset the state removing the stored created mock objs oids
	 */
	public void reset() {
		_createdMockModelObjectsOids = null;	
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns {@link Collection} of the oids of the created model objects after calling {@link #setUpMockModelObjs(int)}
	 * @return
	 */
	public O getAnyCreatedModelObjectOid() {
		if (CollectionUtils.isNullOrEmpty(_createdMockModelObjectsOids)) throw new IllegalStateException("There's NO created model object available at the factory");

		return CollectionUtils.of(_createdMockModelObjectsOids).pickOneElement();
	}
	/**
	 * @return a {@link Collection} of the created model objects after calling {@link #setUpMockModelObjs(int)}
	 */
	public M getAnyCreatedModelObject() {
		O oid = this.getAnyCreatedModelObjectOid();
		M outModelObj = _CRUDApi.load(oid);
		return outModelObj;
	}
	/**
	 * @return the oid of any of the created model objects after calling after calling {@link #setUpMockModelObjs(int)}
	 */
	public Collection<M> getCreatedModelObjects() {
		if (_createdMockModelObjectsOids == null) return null;
		Collection<M> outModelObjs = Lists.newArrayListWithExpectedSize(_createdMockModelObjectsOids.size());
		for (O oid : _createdMockModelObjectsOids) {
			outModelObjs.add(_CRUDApi.load(oid));
		}
		return outModelObjs;
	}
}
