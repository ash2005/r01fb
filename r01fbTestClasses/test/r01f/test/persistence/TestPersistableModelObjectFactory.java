package r01f.test.persistence;

import java.util.Collection;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectCRUDServices;
import r01f.types.Factory;


public interface TestPersistableModelObjectFactory<O extends OID,M extends PersistableModelObject<O>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the model object type
	 */
	public Class<M> getModelObjType();
	/**
	 * @return the factory of mock objects
	 */
	public Factory<M> getMockObjectsFactory();
	/**
	 * @return the CRUD API
	 */
	public ClientAPIDelegateForModelObjectCRUDServices<O,M> getCRUDApi();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a number of model objects using the provided factory
	 * @param numOfObjectsToCreate
	 * @return the created objects
	 */
	public void setUpMockModelObjs(final int numOfObjsToCreate);
	/**
	 * Deletes a {@link Collection} of previously created objects
	 * @param createdObjs
	 */
	public void tearDownCreatedMockModelObjs();
	/**
	 * Reset the state removing the stored created mock objs oids
	 */
	public void reset();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns {@link Collection} of the oids of the created model objects after calling {@link #setUpMockModelObjs(int)}
	 * @return
	 */
	public Collection<O> getCreatedMockModelObjectsOids();
	/**
	 * @return a {@link Collection} of the created model objects after calling {@link #setUpMockModelObjs(int)}
	 */
	public Collection<M> getCreatedModelObjects();
	/**
	 * @return the oid of any of the created model objects after calling after calling {@link #setUpMockModelObjs(int)}
	 */
	public O getAnyCreatedModelObjectOid();
	/**
	 * @return any of the created model objects after calling {@link #setUpMockModelObjs(int)}
	 */
	public M getAnyCreatedModelObject();
}
