package r01f.persistence.internal;

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

import com.google.inject.TypeLiteral;
import com.google.inject.util.Types;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.model.IndexableModelObject;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.persistence.index.Indexer;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.search.Searcher;

/**
 * Encapsulates the type of all components of a model object-related search
 * <ul>
 * 		<li>The indexed document definition: the document's fields: {@link IndexDocumentFieldConfigSet}</li>
 * 		<li>The indexer: the type that stores data at the index: {@link Indexer}</li>
 * 		<li>The searcher: the type that retrieves data from the index: {@link Searcher}</li>
 * </ul>
 */
@Accessors(prefix="_")
public class SearchComponents {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class IndexerComponent<M extends IndexableModelObject> {
		@Getter @Setter(AccessLevel.PRIVATE) private Class<M> _modelObjType;
		@Getter @Setter(AccessLevel.PRIVATE) private IndexDocumentFieldConfigSet<?> _indexDocumentFieldsConfig;
		@Getter @Setter(AccessLevel.PRIVATE) private Class<? extends Indexer<M>> _indexerType;
		
		
		@SuppressWarnings("unchecked")
		public TypeLiteral<Indexer<?>> getIndexerGuiceKey() {
			ParameterizedType pt = Types.newParameterizedType(Indexer.class, 
											    			  _modelObjType);
			return (TypeLiteral<Indexer<?>>)TypeLiteral.get(pt);
		}
		@SuppressWarnings("unchecked")
		public TypeLiteral<IndexDocumentFieldConfigSet<?>> getIndexDocumentFieldsConfigGuiceKey() {
			ParameterizedType pt = Types.newParameterizedType(IndexDocumentFieldConfigSet.class, 
											    			  _modelObjType);
			return (TypeLiteral<IndexDocumentFieldConfigSet<?>>)TypeLiteral.get(pt);
		}
	}
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class SearchComponent<F extends SearchFilter,I extends SearchResultItem> {
		@Getter @Setter(AccessLevel.PRIVATE) private Class<? extends Searcher<F,I>> _searcherType;
		@Getter @Setter(AccessLevel.PRIVATE) private Class<? extends F> _searchFilterType;
		@Getter @Setter(AccessLevel.PRIVATE) private Class<? extends I> _searchResultItemType;
		
		@SuppressWarnings("unchecked")
		public TypeLiteral<Searcher<?,?>> getSearcherGuiceKey() {
			ParameterizedType pt = Types.newParameterizedType(Searcher.class, 
															  _searchFilterType,_searchResultItemType);
			return (TypeLiteral<Searcher<?,?>>)TypeLiteral.get(pt);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter @Setter(AccessLevel.PRIVATE) private Set<IndexerComponent<? extends IndexableModelObject>> _indexers;
	@Getter @Setter(AccessLevel.PRIVATE) private Set<SearchComponent<? extends SearchFilter,? extends SearchResultItem>> _searchers;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public static SearchComponents create() {
		return new SearchComponents();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INDEXERS
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchComponentsIndexersStep indexers() {
		return new SearchComponentsIndexersStep();
	}
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class SearchComponentsIndexersStep {
		private Set<IndexerComponent<? extends IndexableModelObject>> _theIndexers = new HashSet<IndexerComponent<? extends IndexableModelObject>>();
		
		public SearchComponents build() {
			SearchComponents.this.setIndexers(_theIndexers);
			return SearchComponents.this;
		}
		
		private <M extends IndexableModelObject> void _addIndexerComponent(final IndexerComponent<M> indexerComponent) {
			_theIndexers.add(indexerComponent);
		}
		
		public <M extends IndexableModelObject> SearchComponentsIndexerDocumentStep<M> modelObject(final Class<M> modelObjType) {
			IndexerComponent<M> indexComponent = new IndexerComponent<M>();
			indexComponent.setModelObjType(modelObjType);
			return new SearchComponentsIndexerDocumentStep<M>(indexComponent);
		}
		
		@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
		public class SearchComponentsIndexerDocumentStep<M extends IndexableModelObject> {
			private final IndexerComponent<M> _indexComponent;
			
			public SearchComponentsIndexersIndexerStep<M> withIndexDocumentFieldsConfig(final IndexDocumentFieldConfigSet<?> indexDocFieldsCfg) {
				_indexComponent.setIndexDocumentFieldsConfig(indexDocFieldsCfg);
				
				return new SearchComponentsIndexersIndexerStep<M>(_indexComponent);
			}
		}
		@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
		public class SearchComponentsIndexersIndexerStep<M extends IndexableModelObject> {
			private final IndexerComponent<M> _indexComponent;
			
			public SearchComponentsIndexersStep isIndexedBy(final Class<? extends Indexer<M>> indexerType) {
				_indexComponent.setIndexerType(indexerType);
				
				SearchComponentsIndexersStep.this._addIndexerComponent(_indexComponent);	// add the created indexer to the parent
				return SearchComponentsIndexersStep.this;	// return the parent!
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SEARCHERS
/////////////////////////////////////////////////////////////////////////////////////////
	public SearchComponentsSearchersStep searchers() {
		return new SearchComponentsSearchersStep();
	}
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class SearchComponentsSearchersStep {
		private final Set<SearchComponent<? extends SearchFilter,? extends SearchResultItem>> _theSearchers = new HashSet<SearchComponent<? extends SearchFilter,? extends SearchResultItem>>();
		
		public SearchComponents build() {
			SearchComponents.this.setSearchers(_theSearchers);
			return SearchComponents.this;
		}
		
		private <F extends SearchFilter,I extends SearchResultItem> void _addSearchComponent(final SearchComponent<F,I> searchComponent) {
			_theSearchers.add(searchComponent);
		}
		
		public <F extends SearchFilter,I extends SearchResultItem> SearchComponentsSearcherFilterStep<F,I> searcherType(final Class<? extends Searcher<F,I>> searcherType) {
			SearchComponent<F,I> searchComponent = new SearchComponent<F,I>();
			searchComponent.setSearcherType(searcherType);
			return new SearchComponentsSearcherFilterStep<F,I>(searchComponent);
		}
		
		@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
		public class SearchComponentsSearcherFilterStep<F extends SearchFilter,I extends SearchResultItem> {
			private final SearchComponent<F,I> _searchComponent;
			
			public SearchComponentsSearcherResultItemStep<F,I> usesFilter(final Class<F> filterType) {
				_searchComponent.setSearchFilterType(filterType);
				return new SearchComponentsSearcherResultItemStep<F,I>(_searchComponent);
			}
		}
		@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
		public class SearchComponentsSearcherResultItemStep<F extends SearchFilter,I extends SearchResultItem> {
			private final SearchComponent<F,I> _searchComponent;
			
			public SearchComponentsSearchersStep returningItemsOfType(final Class<I> resultItemType) {
				_searchComponent.setSearchResultItemType(resultItemType);
				
				SearchComponentsSearchersStep.this._addSearchComponent(_searchComponent);	// add the created searcher to the parent
				return SearchComponentsSearchersStep.this;	// return the parent!
			}
		}
	}	
}
