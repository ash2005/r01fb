package r01f.test.persistence;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.patterns.CommandOn;
import r01f.services.client.ClientAPI;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectFindServices;
import r01f.types.Factory;

/**
 * JVM arguments:
 * -javaagent:D:/tools_workspaces/eclipse/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 */
public abstract class TestEntityBase<API extends ClientAPI,
							  		 O extends OID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Class<M> _entityType;
	protected final API _api;
	protected final TestPersistableModelObjectFactory<O,M> _factory;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	protected TestEntityBase(final Class<M> entityType,long milisToWaitForBackgroundJobs,
							 final Factory<M> mockObjsFactory,
							 final API api) {
		_entityType = entityType;
		_api = api;
		_factory = TestPersistableModelObjectFactory.create(_entityType,
															mockObjsFactory,
															_clientCRUDApi(),
															milisToWaitForBackgroundJobs);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public void doTest() {
		System.out.println("===========================================================");
		System.out.println("TEST: " + _entityType.getSimpleName());
		System.out.println("===========================================================");
		
		// [1]: Test Persistence (create, update, load and delete)
		this.doCRUDTest();
		System.out.println("--------------------------------------------------------------------\n\n\n\n");
		
		// [2]: Test Find 
		this.doFindTest();
		System.out.println("--------------------------------------------------------------------\n\n\n\n");	
		
		// [3]: Test other methods
		this.testOtherMethods();
		
		// [4]: Ensure created records are removed
		_factory.tearDownCreatedMockModelObjs();
	}
	protected abstract void testOtherCRUDMethods();
	protected abstract void testOtherFindMethods();
	protected abstract void testOtherMethods();
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	public void doCRUDTest() {
		// [1]: Basic persistence tests
		TestPersistableModelObjectCRUD<O,M> crudTest = TestPersistableModelObjectCRUD.create(// crud api
																							 _clientCRUDApi(),
																							 // model objects factory
																							 _factory);		
		crudTest.testPersistence(_modelObjectStateUpdateCommand());
		
		
		// [3]: Test other CRUD methods
		this.testOtherCRUDMethods();
		
	}	
	/**
	 * @return a {@link CommandOn} that changes the model object's state (simulate a user update action)
	 */
	protected abstract CommandOn<M> _modelObjectStateUpdateCommand();
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND
/////////////////////////////////////////////////////////////////////////////////////////
	public void doFindTest() {
		// [1]: Basic find tests
		TestPersistableModelObjectFind<O,M> findTest = TestPersistableModelObjectFind.create(// find api
																							 _clientFindApi(),
																							 // mock objects factory
																							 _factory);
		findTest.testFind();
		
		// [2]: Test extended methods
		System.out.println("[Test other FIND methods]");
		this.testOtherFindMethods();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CRUD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a {@link ClientAPIDelegateForModelObjectCRUDServices} instance depending on the model object type
	 */
	protected abstract ClientAPIDelegateForModelObjectCRUDServices<O,M> _clientCRUDApi();
	
	@SuppressWarnings({ "unchecked","unused" })
	protected <A extends ClientAPIDelegateForModelObjectCRUDServices<O,M>> A _clientCRUDApiAs(final Class<A> apiType) {
		return (A)_clientCRUDApi();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIND API
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a {@link ClientAPIDelegateForModelObjectFindServices} instance depending on the model object type
	 */
	protected abstract ClientAPIDelegateForModelObjectFindServices<O,M> _clientFindApi();
	
	@SuppressWarnings({ "unchecked","unused" })
	protected <A extends ClientAPIDelegateForModelObjectFindServices<O,M>> A _clientFindApiAs(final Class<A> apiType) {
		return (A)_clientFindApi();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
}
