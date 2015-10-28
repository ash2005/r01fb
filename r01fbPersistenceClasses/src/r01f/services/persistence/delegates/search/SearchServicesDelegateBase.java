package r01f.services.persistence.delegates.search;

import java.util.Collection;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.model.search.SearchResults;
import r01f.model.search.SearchResultsProvider;
import r01f.persistence.search.SearchResultsLoaders.SearchResultsLoader;
import r01f.persistence.search.Searcher;
import r01f.usercontext.UserContext;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.eventbus.EventBus;

/**
 * Base for services of model objects with indexing and searching
 */
@Accessors(prefix="_")
public abstract class SearchServicesDelegateBase<F extends SearchFilter,I extends SearchResultItem> {
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTED STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * FullText Searcher implementation
	 */
	@Getter protected final Searcher<F,I> _searcher;
	/**
	 * {@link EventBus} used to span events to subscribed event handlers
	 */
	@Getter protected final EventBus _eventBus;
	/**
	 * Properties
	 */
	@Getter protected final XMLPropertiesForAppComponent _properties;
/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchServicesDelegateBase(final Searcher<F,I> searcher,
									  final EventBus eventBus,
									  final XMLPropertiesForAppComponent searchProps) {
		_searcher = searcher;
		_eventBus = eventBus;
		_properties = searchProps;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the number of items that verify the filter conditions
	 * @param userContext
	 * @param filter
	 * @return
	 */
	public int countRecords(final UserContext userContext,
							final F filter) {
		int outCount = _searcher.countRecords(userContext,
    										  filter);
		return outCount;
	}
	/**
	 * Returns a {@link SearchResults} structure that encapsulates the {@link SearchFilter} 
	 * and the {@link Collection} of {@link SearchResultItemForModelObject}s
	 * @param userContext
	 * @param filter
	 * @param firstRowNum
	 * @param numberOfRows
	 * @return
	 */
	public SearchResults<F,I> filterRecords(final UserContext userContext, 
							         	 	final F filter,
							         	 	final int firstRowNum,final int numberOfRows) {
		// beware of this!!!!
    	int effFirstRowNum = firstRowNum < 0 ? 0 : firstRowNum;
    	int effNumberOfRows = numberOfRows <= 0 ? SearchResults.defaultPageSize() 
    											: numberOfRows;
    	
    	// Filter
		int count = 0;
    	Collection<I> items = null;
		SearchResults<F,I> results = _searcher.filterRecords(userContext,
															 filter,
														     effFirstRowNum,effNumberOfRows);
		count = results != null ? results.getTotalItemsCount()
								: 0;
		items = results != null ? results.getPageItems()
								: null;
    	SearchResults<F,I> outSearchResults = new SearchResults<F,I>(filter,
    										   	    				 count,effFirstRowNum,
    										   	    				 items);
    	return outSearchResults;
	}
	/**
	 * Returns all oids for the records that verify the filter condition
	 * @param userContext
	 * @param filter
	 * @return
	 */
	public <O extends OID> Collection<O> filterRecordsOids(final UserContext userContext, 
														   final F filter) {
		// Collect all results
	 	SearchResultsProvider<F,I> resultsProvider = new SearchResultsProvider<F,I>(filter,10) {
										 						@Override
										 						public SearchResults<F,I> provide(final int startPosition) {
										 							// Retrieve the page results
										 							return SearchServicesDelegateBase.this.filterRecords(userContext,
										 																				 filter,
										 																				 startPosition,SearchResults.defaultPageSize());
										 						}
										 			 };
	    SearchResultsLoader<F,I> loader = SearchResultsLoader.create(resultsProvider);
	    Collection<I> allResults = loader.collectAll();
		
		// return 
		Collection<O> outRecords = null;
		if (CollectionUtils.hasData(allResults)) {
			outRecords = Collections2.transform(allResults,
												new Function<I,O>() {														
														@Override @SuppressWarnings("unchecked")
														public O apply(final I item) {
															SearchResultItemForModelObject<O,? extends PersistableModelObject<O>> itemForModelObject = (SearchResultItemForModelObject<O,? extends PersistableModelObject<O>>)item;
															return itemForModelObject.getOid();
														}
												});
		}
		return outRecords;
	}
}
