package r01f.persistence.search;

import org.apache.lucene.document.Document;

import r01f.guids.OID;
import r01f.model.ModelObject;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.persistence.index.document.IndexDocument;


/**
 * Interface to be implemented by {@link Searcher} subtypes that maps the indexed field values to search result item's fields
 */
public interface SearcherMapsIndexedFieldsToSearchResultItemFields<I extends SearchResultItemForModelObject<? extends OID,? extends ModelObject>>
		 extends SearcherSearchResultItemFromIndexDataTransformStrategy {
	/**
	 * Maps indexed fields in an {@link IndexDocument} that wraps the raw indexed data (such as a lucene's {@link Document})
	 * to a {@link SearchResultItemForModelObject}
	 * @param indexedDocument
	 * @param searchResultItem
	 */
	public void mapIndexedFieldsToSearchResultItemFields(final IndexDocument indexedDocument,
														 final I searchResultItem);
}
