package r01f.persistence;

import r01f.guids.OID;
import r01f.model.PersistableModelObject;


@SuppressWarnings("unused")
public interface PersistenceOperationOnModelObjectResult<T>
		 extends PersistenceOperationResult {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the model object type 
	 */
	public Class<? extends PersistableModelObject<? extends OID>> getModelObjectType();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the requested operation
	 */
	public PersistenceRequestedOperation getRequestedOperation();
	/**
	 * @return the performed operation
	 */
	public PersistencePerformedOperation getPerformedOperation();
}
