package r01f.persistence;






public interface PersistenceOperationResult {
/////////////////////////////////////////////////////////////////////////////////////////
//  OPERATION NAME
/////////////////////////////////////////////////////////////////////////////////////////
	/**
 	 * @return the requested operation in a human-friendly format
	 */
	public String getRequestedOperationName();
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR CONDITIONS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if the persistence operation has failed
	 */
	public boolean hasFailed();
	/**
	 * @return true if the persistence operation has succeeded
	 */
	public boolean hasSucceeded();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the operation result message, normally if the operation has failed, it contains an error description and if the 
	 * 		   operation has succeed it contains some logging info
	 */
	public String getDetailedMessage();
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the {@link PersistenceOperationResult} as another {@link PersistenceOperationResult} subtype
	 * @param type
	 * @return
	 */
	public <R extends PersistenceOperationResult> R as(final Class<R> type);
//	/**
//	 * Returns the {@link PersistenceOperationResult} as a {@link PersistenceOperationError} subtype
//	 * it'll throw a {@link ClassCastException} if the instance is not a {@link PersistenceOperationError} subtype
//	 * @return
//	 */
//	public <R extends PersistenceOperationError> R asError(final Class<R> type);
//	/**
//	 * Returns the {@link PersistenceOperationResult} as a {@link PersistenceOperationOK} subtype
//	 * it'll throw a {@link ClassCastException} if the instance is not a {@link PersistenceOperationOK} subtype
//	 * @return
//	 */
//	public <R extends PersistenceOperationOK> R asOK(final Class<R> type);
}
