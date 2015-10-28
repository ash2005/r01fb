package r01f.rest.resources.delegates;

import java.net.URI;

import javax.ws.rs.core.Response;

import lombok.experimental.Accessors;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.rest.RESTOperationsResponseBuilder;
import r01f.services.interfaces.SearchServices;
import r01f.usercontext.UserContext;

/**
 * Base type for REST services that encapsulates the common search index ops: indexing, searching
 */
@Accessors(prefix="_")
public abstract class RESTSearchDelegateBase<F extends SearchFilter,I extends SearchResultItem> 
           implements RESTDelegate {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final SearchServices<F,I> _searchServices;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <T extends SearchServices<F,I>> T searchServicesAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_searchServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTSearchDelegateBase(final SearchServices<F,I> searchServices) {
		_searchServices = searchServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Search using the provided filter
	 * @param userContext 
	 * @param resourcePath
	 * @param filter
	 * @param startingRow
	 * @param itemsToReturn
	 * @return
	 */
	public Response search(final UserContext userContext,final String resourcePath,
						   final F filter,
						   final int startingRow,final int itemsToReturn) {
		SearchResults<F,I> searchResults = null;
		searchResults = _searchServices.filterRecords(userContext,
									   		    	  filter,
									   		    	  startingRow,itemsToReturn);
		Response outResponse = RESTOperationsResponseBuilder.searchIndex()
															.at(URI.create(resourcePath))
															.build(searchResults);
		return outResponse;
	}
}
