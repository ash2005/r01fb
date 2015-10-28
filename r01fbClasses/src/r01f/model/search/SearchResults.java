package r01f.model.search;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

/**
 * Wraps search result items alongside with info about the filter that originates those search results and other infor as the
 * total number of results and the start/end positions (paging info)
 */
@XmlRootElement(name="searchResults")
@Accessors(prefix="_")
@NoArgsConstructor
public class SearchResults<F extends SearchFilter,
						   I extends SearchResultItem> 
  implements SearchModelObject {

	private static final long serialVersionUID = -3911184210171016395L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static final int DEFAULT_PAGE_SIZE = 10;
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Total number of results 
	 */
	@XmlAttribute(name="totalItemsCount")
	@Getter @Setter private int _totalItemsCount;
	/**
	 * This results page initial row number
	 */
	@XmlAttribute(name="startPosition")
	@Getter @Setter private int _startPosition = 0;
	/**
	 * This results page final row number
	 */
	@XmlAttribute(name="endPosition")
	@Getter @Setter private int _endPosition = 10;
	/**
	 * The filter that was executed to get this results page
	 */
	@XmlElement
	@Getter @Setter private F _filter;
	/**
	 * This page results
	 */
	@XmlElementWrapper(name = "pageItems") 
	@Getter	@Setter private Collection<I> _pageItems;
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param newFilter 
	 * @param newPageItems this page items
	 * @param newTotalItemsCount total number of elements for the search results
	 * @param newStartPosition row number of this page results
	 * @param newEndPosition row number of the next page results
	 */
	public SearchResults(final F newFilter,
						 final int newTotalItemsCount,final int newStartPosition,
						 final Collection<I> newPageItems) {
		_filter = newFilter;
		if (CollectionUtils.hasData(newPageItems)) _pageItems = newPageItems; 
		_totalItemsCount = newTotalItemsCount;
		_startPosition = newStartPosition;
		if (CollectionUtils.hasData(newPageItems)) {
			_endPosition = newStartPosition + newPageItems.size();
		} else {
			_endPosition = newStartPosition;
		}
	}
	/**
	 * Constructor
	 * @param newFilter
	 * @param newTotalItemsCount
	 * @param newStartPosition
	 * @param pageItems
	 * @param conversionFunction
	 */
	public <T> SearchResults(final F newFilter,
						 	 final int newTotalItemsCount,final int newStartPosition,
						 	 final Collection<T> pageItems,
						 	 final Function<T,I> conversionFunction) {
		this(newFilter,
			 newTotalItemsCount,newStartPosition,
			 FluentIterable.from(pageItems)
						   .transform(conversionFunction)
						   .toList());
	}
	public static <F extends SearchFilter,
				   I extends SearchResultItem> SearchResults<F,I> create() {
		SearchResults<F,I> outSearchResults = new SearchResults<F,I>();
		return outSearchResults;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ADD METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchResults<F,I> addItem(final I item) {
		if (_pageItems == null) _pageItems = Sets.newHashSet();
		_pageItems.add(item);
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if this page has data
	 */
	public boolean hasData() {
		return CollectionUtils.hasData(_pageItems);
	}
	/**
	 * @return true if there are more pages to load
	 */
	public boolean hasMorePages() {
		return _endPosition < _totalItemsCount;
	}
	/**
	 * @return the start position of the next page
	 */
	public int getNextPageStartPosition() {
		if (!this.hasMorePages()) throw new IllegalStateException("This is the last search results page; cannot go to the next page!!");
		return _endPosition; 
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  DEFAULT	
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the configured default search results page size
	 */
	public static int defaultPageSize() {
		return DEFAULT_PAGE_SIZE;
	}
}
