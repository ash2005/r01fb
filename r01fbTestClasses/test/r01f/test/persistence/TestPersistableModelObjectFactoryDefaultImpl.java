package r01f.test.persistence;

import java.util.Collection;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.types.Factory;
import r01f.util.types.collections.CollectionUtils;

@Accessors(prefix="_")
public class TestPersistableModelObjectFactoryDefaultImpl<O extends OID,M extends PersistableModelObject<O>>
  implements TestPersistableModelObjectFactory<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Class<M> _modelObjType;
	@Getter private final Factory<M> _mockObjectsFactory;
	@Getter private final ClientAPIDelegateForModelObjectCRUDServices<O,M> _CRUDApi;
	@Getter private final long _milisToWaitForBackgroundJobs;
	
	@Getter private Collection<O> _createdMockModelObjectsOids; 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectFactoryDefaultImpl(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory,
												 		final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
												 		final long milisToWaitForBackgroundJobs) {
		_modelObjType = modelObjType;
		_mockObjectsFactory = mockObjectsFactory;
		_CRUDApi = crudAPI;
		_milisToWaitForBackgroundJobs = milisToWaitForBackgroundJobs;
	}
	public TestPersistableModelObjectFactoryDefaultImpl(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory,
														final long milisToWaitForBackgroundJobs) {
		this(modelObjType,
			 mockObjectsFactory,
			 null,
			 milisToWaitForBackgroundJobs);
	}
	public TestPersistableModelObjectFactoryDefaultImpl(final Class<M> modelObjType,
														final long milisToWaitForBackgroundJobs) {
		this(modelObjType,
			 null,
			 null,
			 milisToWaitForBackgroundJobs);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends OID,M extends PersistableModelObject<O>> TestPersistableModelObjectFactory<O,M> create(final Class<M> modelObjType,
																													final Factory<M> mockObjectsFactory,
																													final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI,
																													final long milisToWaitForBackgroundJobs) {
		return new TestPersistableModelObjectFactoryDefaultImpl<O,M>(modelObjType,mockObjectsFactory,
															  		 crudAPI,
															  		 milisToWaitForBackgroundJobs);
	}
	public static <O extends OID,M extends PersistableModelObject<O>> TestPersistableModelObjectFactory<O,M> create(final Class<M> modelObjType,
																													final Factory<M> mockObjectsFactory,
																													final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		return new TestPersistableModelObjectFactoryDefaultImpl<O,M>(modelObjType,mockObjectsFactory,
															  		 crudAPI,
															  		 0L);		// no need to wait for crud-associated background jobs
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
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
	@Override
	public void tearDownCreatedMockModelObjs() {
		if (CollectionUtils.isNullOrEmpty(_createdMockModelObjectsOids)) return;
		
		// wait for background jobs to finish
		// an error in the background job will raise if the DB records are deleted before background jobs finish (ie lucene indexing or notification tasks)
		long milisToWaitForBackgroundJobs = _createdMockModelObjectsOids.size() * _milisToWaitForBackgroundJobs;
		if (milisToWaitForBackgroundJobs > 0) {
			System.out.println(".... give " + milisToWaitForBackgroundJobs + " milis for background jobs (ie lucene index or notifications) to complete before deleting created DB records (lucene indexing or notifications will fail if the DB record is deleted)");
			try {
				Thread.sleep(milisToWaitForBackgroundJobs);
			} catch(Throwable th) {
				th.printStackTrace(System.out);
			}
		}
		
		// delete all DB records
		for (O oid : _createdMockModelObjectsOids) {
			_CRUDApi.delete(oid);
			System.out.println("... Deleted " + _modelObjType.getSimpleName() + " mock object with oid=" + oid);
		}
		this.reset();
	}
	@Override
	public void reset() {
		_createdMockModelObjectsOids = null;	
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public O getAnyCreatedModelObjectOid() {
		if (CollectionUtils.isNullOrEmpty(_createdMockModelObjectsOids)) throw new IllegalStateException("There's NO created model object available at the factory");
		
		return CollectionUtils.of(_createdMockModelObjectsOids).pickOneElement();
	}
	@Override
	public M getAnyCreatedModelObject() {
		O oid = this.getAnyCreatedModelObjectOid();
		M outModelObj = _CRUDApi.load(oid);
		return outModelObj;
	}
	@Override
	public Collection<M> getCreatedModelObjects() {
		if (_createdMockModelObjectsOids == null) return null;
		Collection<M> outModelObjs = Lists.newArrayListWithExpectedSize(_createdMockModelObjectsOids.size());
		for (O oid : _createdMockModelObjectsOids) {
			outModelObjs.add(_CRUDApi.load(oid));
		}
		return outModelObjs;
	}
}
