package r01f.persistence;

/**
 * An interface for a persistence operation that could be completed successfully
 */
public interface PersistenceOperationOK 
		 extends PersistenceOperationResult {
	public boolean isCRUDOK();
	public boolean isCRUDOnMultipleOK();
	public boolean isFindOK();
	public boolean isFindSummariesOK();
}
