package r01f.persistence.db;

import r01f.model.search.SearchResultItem;
import r01f.usercontext.UserContext;

/**
 * Interface for types that transforms a {@link DBEntity} into a {@link SearchResultItem}
 * @param <DB>
 * @param <M>
 */
public interface TransformsDBEntityToSearchResultItem<DB extends DBEntity,
												 	  I extends SearchResultItem> {
	/**
	 * Builds a {@link SearchResultItem} from this {@link DBEntity} data
	 * @param userContext
	 * @param dbEntity
	 * @return a search result item
	 */
	public abstract I dbEntityToSearchResultItem(final UserContext userContext,
										    	 final DB dbEntity);
}
