package r01f.persistence.index;

import r01f.usercontext.UserContext;

/**
 * Interface to be implemented by types in charge to manage indexes
 * @param <M>
 */
public interface IndexManager {
	/**
	 * Closes the index
	 * @param userContext
	 */
	public void open(final UserContext userContext);
	/**
	 * Closes the index
	 * @param userContext
	 */
	public void close(final UserContext userContext);
	/**
	 * Optimizes the index
	 * @param userContext
	 */
	public void optimize(final UserContext userContext);
	
	/**
	 * Truncates the index (removes all documents)
	 * @param userContext
	 */
	public void truncate(final UserContext userContext);
}
