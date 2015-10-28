package r01f.test.persistence;

import java.util.Collection;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.types.Factory;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Lists;

@Accessors(prefix="_")
public class TestPersistableModelObjectFactoryDefaultImpl<O extends OID,M extends PersistableModelObject<O>>
  implements TestPersistableModelObjectFactory<O,M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Class<M> _modelObjType;
	@Getter private final Factory<M> _mockObjectsFactory;
	@Getter private final ClientAPIDelegateForModelObjectCRUDServices<O,M> _CRUDApi;
	
	@Getter private Collection<O> _createdMockModelObjectsOids; 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public TestPersistableModelObjectFactoryDefaultImpl(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory,
												 		final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		_modelObjType = modelObjType;
		_mockObjectsFactory = mockObjectsFactory;
		_CRUDApi = crudAPI;
	}
	public TestPersistableModelObjectFactoryDefaultImpl(final Class<M> modelObjType,
														final Factory<M> mockObjectsFactory) {
		this(modelObjType,
			 mockObjectsFactory,
			 null);
	}
	public TestPersistableModelObjectFactoryDefaultImpl(final Class<M> modelObjType) {
		this(modelObjType,
			 null,
			 null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <O extends OID,M extends PersistableModelObject<O>> TestPersistableModelObjectFactory<O,M> create(final Class<M> modelObjType,
																													final Factory<M> mockObjectsFactory,
																													final ClientAPIDelegateForModelObjectCRUDServices<O,M> crudAPI) {
		return new TestPersistableModelObjectFactoryDefaultImpl<O,M>(modelObjType,mockObjectsFactory,
															  		 crudAPI);
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
		
		// wait 10s for background jobs to complete
		try {
			System.out.println(".... give time for background jobs to complete before deleting created objects");
			Thread.sleep(10000);		
		} catch(Throwable th) {
			/*ignore*/
		}
		
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
