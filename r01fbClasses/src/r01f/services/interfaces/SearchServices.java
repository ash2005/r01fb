package r01f.services.interfaces;

import java.util.Collection;

import r01f.guids.OID;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.usercontext.UserContext;

public interface SearchServices<F extends SearchFilter,
								I extends SearchResultItem> 
		 extends ServiceInterface {
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the total number of results that verifies the filter
     * @param userContext
     * @param the filter
     * @return the total number of results
     */
	public int countRecords(final UserContext userContext,
							final F filter);
    /**
     * Returns the OIDs of the records verifying the search filter
	 * <pre>
	 * IMPORTANT!!	Normally this method always filter at BBDD (never uses Lucene) 
	 * 				because it's purpose is normally re-generate the lucene index
	 * 				from BBDD data
	 * </pre>
     * @param filter the filter
     * @return the oids
     */
	public <O extends OID> Collection<O> filterRecordsOids(final UserContext userContext,
														   final F filter);
	/**
	 * Searches records using a provided filter
	 * @param userContext the user auth data & context info
	 * @param filter the filter
	 * @param firstRowNum order number of the first row to be returned
	 * @param numberOfRows number of rows to be returned
	 * @return the result items
	 */
	public SearchResults<F,I> filterRecords(final UserContext userContext,
									        final F filter,
									     	final int firstRowNum,final int numberOfRows);
}
