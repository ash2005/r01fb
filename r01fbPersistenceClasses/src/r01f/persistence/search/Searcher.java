package r01f.persistence.search;

import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.usercontext.UserContext;

/**
 * Interface to be implemented by types in charge to execute searches
 * @param <F>
 * @param <I>
 */
public interface Searcher<F extends SearchFilter,
		   				  I extends SearchResultItem> {
    /**
     * Returns the total number of results that verifies the filter
     * @param userContext
     * @param the filter
     * @return the total number of results
     */
	public int countRecords(final UserContext userContext,
							final F filter);
	/**
	 * Searches records using a provided filter
	 * @param userContext
	 * @param filter 
	 * @param firstRowNum order number of the first row to be returned
	 * @param numberOfRows number of rows to be returned
	 * @return the result items
	 */
	public SearchResults<F,I> filterRecords(final UserContext userContext,
									   		final F filter,
									   		final int firstRowNum,final int numberOfRows);

}
