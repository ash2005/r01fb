package r01f.persistence.search;

import org.apache.lucene.document.Document;

import r01f.guids.OID;
import r01f.model.IndexableModelObject;
import r01f.usercontext.UserContext;

/**
 * Interface to be implemented by {@link Searcher} subtypes that creates a model object instance
 * from the indexed data
 * For example if the search index is a Lucene index, a {@link Searcher} implementing this interface can create model objects
 * from lucene's {@link Document}
 * @param <INDEX_DATA> the index data (ie Lucene's {@link Document}s or a BBDD record set
 * @param <P>
 */
public interface SearcherCreatesResultItemFromIndexData<INDEX_DATA,
														P extends IndexableModelObject<? extends OID>>
		 extends SearcherSearchResultItemFromIndexDataTransformStrategy {
	/**
	 * Creates a model object from the search index stored data
	 * @param userContext
	 * @param indexData
	 * @return
	 */
	public P createModelObjectFrom(final UserContext userContext,
								   final INDEX_DATA indexData);
}
