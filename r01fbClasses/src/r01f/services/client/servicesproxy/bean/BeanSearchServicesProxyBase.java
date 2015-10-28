package r01f.services.client.servicesproxy.bean;


import java.util.Collection;

import lombok.RequiredArgsConstructor;
import r01f.guids.OID;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.services.interfaces.ProxyForBeanImplementedService;
import r01f.services.interfaces.SearchServices;
import r01f.usercontext.UserContext;

/**
 * Service proxy that just delegates to the service impl bean
 */
@RequiredArgsConstructor
public abstract class BeanSearchServicesProxyBase<F extends SearchFilter,I extends SearchResultItem>
           implements SearchServices<F,I>,
  			   		  ProxyForBeanImplementedService {		// it's a bean implementation of the R01MServiceInterface

/////////////////////////////////////////////////////////////////////////////////////////
// 	DELEGATE
/////////////////////////////////////////////////////////////////////////////////////////
	protected final SearchServices<F,I> _searchServices;
/////////////////////////////////////////////////////////////////////////////////////////
//  UTILITY METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	protected <T extends SearchServices<F,I>> T searchServicesAs(@SuppressWarnings("unused") final Class<T> type) {
		return (T)_searchServices;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCH
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public int countRecords(final UserContext userContext,
							final F filter) {
		return _searchServices.countRecords(userContext,
										    filter);
	}
	@Override
	public <ID extends OID> Collection<ID> filterRecordsOids(final UserContext userContext,
													   	     final F filter) {
		return _searchServices.filterRecordsOids(userContext,
											     filter);
	}
	@Override
	public SearchResults<F,I> filterRecords(final UserContext userContext,
											final F filter,
											final int firstRowNum,final int numberOfRows) {
		return _searchServices.filterRecords(userContext,
										     filter,
										     firstRowNum,numberOfRows);
	}
}
