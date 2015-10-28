package r01f.services.client.api.delegates;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResults;
import r01f.services.interfaces.SearchServices;
import r01f.usercontext.UserContext;

/**
 * Adapts Search API method invocations to the service proxy that performs the core method invocations
 * @param <F>
 * @param <I>
 */
@Accessors(prefix="_")
public abstract class ClientAPIDelegateForModelObjectSearchServices<F extends SearchFilter,
																	I extends SearchResultItem> 
	 		  extends ClientAPIServiceDelegateBase<SearchServices<F,I>> {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	private static final int SEARCH_RESULT_PAGE_SIZE = 10;		// TODO parameterize the search result page size
	
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final Class<F> _filterType;
	@Getter private final Class<I> _resultItemType;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public ClientAPIDelegateForModelObjectSearchServices(final UserContext userContext,
														 final SearchServices<F,I> services,
														 final Class<F> filterType,final Class<I> resultItemType) {
		super(userContext,
			  services);
		_filterType = filterType;
		_resultItemType = resultItemType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Searches returning only the first page results
	 * @param filter
	 * @return
	 */
	public ClientAPIDelegateForModelObjectSearchServicesPageStep1 search(final F filter) {
		return new ClientAPIDelegateForModelObjectSearchServicesPageStep1(filter);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ClientAPIDelegateForModelObjectSearchServicesPageStep1 {
		private final F _filter;
		
		public ClientAPIDelegateForModelObjectSearchServicesPageStep2 fromItemAt(final int firstItemNum) {
			return new ClientAPIDelegateForModelObjectSearchServicesPageStep2(_filter,
																			  firstItemNum);
		}
		public SearchResults<F,I> firstPageOfSize(final int numberOfItems) {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getUserContext(),
																			 		_filter,
																			 		0,numberOfItems);
		}
		public SearchResults<F,I> firstPage() {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getUserContext(),
																			 		_filter,
																			 		0,SEARCH_RESULT_PAGE_SIZE);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class ClientAPIDelegateForModelObjectSearchServicesPageStep2 {		
		private final F _filter;
		private final int _firstItemNum;
		
		public SearchResults<F,I> returning(final int numberOfItems) {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getUserContext(),
																			 		_filter,
																			 		_firstItemNum,numberOfItems);
		}
		public SearchResults<F,I> returningTheDefaultNumberOfItems() {
			return ClientAPIDelegateForModelObjectSearchServices.this.getServiceProxy()
																	 .filterRecords(ClientAPIDelegateForModelObjectSearchServices.this.getUserContext(),
																			 		_filter,
																			 		_firstItemNum,SEARCH_RESULT_PAGE_SIZE);
		}
	}
}
