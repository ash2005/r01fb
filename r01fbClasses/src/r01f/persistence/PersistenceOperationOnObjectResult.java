package r01f.persistence;

public interface PersistenceOperationOnObjectResult<T>
		 extends PersistenceOperationResult {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the model object type 
	 */
	public Class<T> getObjectType();
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