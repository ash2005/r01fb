package r01f.persistence.internal;


import java.util.Collection;
import java.util.Set;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;
import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Singleton;
import com.google.inject.TypeLiteral;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.inject.GuiceModuleWithProperties;
import r01f.inject.HasMoreBindings;
import r01f.model.IndexableModelObject;
import r01f.model.search.SearchFilter;
import r01f.model.search.SearchResultItem;
import r01f.persistence.index.document.IndexDocumentFieldConfigSet;
import r01f.persistence.internal.SearchComponents.IndexerComponent;
import r01f.persistence.internal.SearchComponents.SearchComponent;
import r01f.services.ServicesPackages;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

/**
 * Base {@link Guice} module for search engine (index / search) bindings
 */
@Slf4j
public abstract class SearchGuiceModuleBase 
              extends GuiceModuleWithProperties {
/////////////////////////////////////////////////////////////////////////////////////////
// DOCUMENT FIELDS CONFIG
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The document config type for every model object 
	 */
	protected final Set<SearchComponents> _searchComponents;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app divided into components
	 * In this case, the app is composed by a one or more components and the properties are going to be looked after at
	 * [appCode].[appComponent].persistence.properties.xml
	 * @param coreBootstrapGuiceModuleType
	 * @param searchComponents
	 */
	protected SearchGuiceModuleBase(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapGuiceModuleType,
								    final Set<SearchComponents> searchComponents) {
		super(ServicesPackages.appCodeFromCoreBootstrapModuleType(coreBootstrapGuiceModuleType),
			  AppComponent.forId(ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(coreBootstrapGuiceModuleType).asString() + ".searchpersistence"));
		_searchComponents = searchComponents;
	}
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app divided into components
	 * In this case, the app is composed by a one or more components and the properties are going to be looked after at
	 * [appCode].[appComponent].persistence.properties.xml
	 * @param appCode
	 * @param appComponent
	 * @param searchComponents
	 */
	protected SearchGuiceModuleBase(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapGuiceModuleType,
								    final SearchComponents... searchComponents) {
		this(coreBootstrapGuiceModuleType,
			 Sets.newHashSet(searchComponents));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  dirty trick to return the real appcomponent
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public AppComponent getAppComponent() {
		AppComponent comp = super.getAppComponent();
		String compStr = comp.asString();
		return AppComponent.forId(compStr.substring(0,compStr.lastIndexOf(".searchpersistence")));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override 
	public void configure(final Binder binder) {	
		// Bind search components
		if (CollectionUtils.hasData(_searchComponents)) {
			log.warn("\t\t\t...binding search components for {}.{}",this.getAppCode(),this.getAppComponent());
			for (SearchComponents comp : _searchComponents) {
				if (CollectionUtils.hasData(comp.getIndexers())) {
					for (IndexerComponent<? extends IndexableModelObject<?>> indexer : comp.getIndexers()) {
						// Index document's fields config
						log.warn("\t\t\t\t-index document's fields config {} TO {}",indexer.getIndexDocumentFieldsConfigGuiceKey(),indexer.getIndexerType());
						binder.bind(indexer.getIndexDocumentFieldsConfigGuiceKey())
							  .toInstance(indexer.getIndexDocumentFieldsConfig());
						// Indexer
						log.warn("\t\t\t\t-indexer {} TO {}",indexer.getIndexerGuiceKey(),indexer.getIndexerType());
						binder.bind(indexer.getIndexerGuiceKey())   // binder.bind(new TypeLiteral<Indexer<R01MXX>>() {/* empty */})
							  .to(indexer.getIndexerType())			// 		 .to(R01XLuceneIndexerForXX.class)
							  .in(Singleton.class);					//		 .in(Singleton.class)	
					}
				}
				if (CollectionUtils.hasData(comp.getSearchers())) {
					for (SearchComponent<? extends SearchFilter,? extends SearchResultItem> searcher : comp.getSearchers()) {
						// Searcher
						log.warn("\t\t\t\t-searcher {} TO {}",searcher.getSearcherGuiceKey(),searcher.getSearcherType());
						binder.bind(searcher.getSearcherGuiceKey())  // binder.bind(new TypeLiteral<Searcher<R01MSearchFilterForXX,R01MSearchResultItemForXX>>() {/* empty */})
							  .to(searcher.getSearcherType())        // 	  .to(R01XLuceneSearcherForXX.class)                                                               
							  .in(Singleton.class);                  //		  .in(Singleton.class)
					}
				}
			}
		}
		// Give chance to sub-types to do more bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(binder);
		}
	}
	public Collection<TypeLiteral<?>> getServicesToExpose() {
		Collection<TypeLiteral<?>> outServicesToExpose = Lists.newArrayList();
		if (CollectionUtils.hasData(_searchComponents)) {
			for (SearchComponents comp : _searchComponents) {
				// indexers
				if (CollectionUtils.hasData(comp.getIndexers())) {
					for (IndexerComponent<? extends IndexableModelObject<?>> indexer : comp.getIndexers()) {
						outServicesToExpose.add(indexer.getIndexerGuiceKey());
					}
				}
				// searchers
				if (CollectionUtils.hasData(comp.getSearchers())) {
					for (SearchComponent<? extends SearchFilter,? extends SearchResultItem> searcher : comp.getSearchers()) {
						outServicesToExpose.add(searcher.getSearcherGuiceKey());
					}
				}
			}
		}
		return outServicesToExpose;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
////////////////////////////////////////////////////////////////////////////////////////
	protected static Set<IndexDocumentFieldConfigSet<?>> _indexDocumentTypes(final Set<SearchComponents> components) {
		Set<IndexDocumentFieldConfigSet<?>> outDocTypes = null;
		if (CollectionUtils.hasData(components))  {
			outDocTypes = Sets.newHashSet();
			for (SearchComponents comp : components) {
				if (CollectionUtils.isNullOrEmpty(comp.getIndexers())) continue;	// ignore this component
				
				outDocTypes.addAll(FluentIterable.from(comp.getIndexers())
									  		     .transform(new Function<IndexerComponent<? extends IndexableModelObject<?>>,
									  				   				     IndexDocumentFieldConfigSet<?>>() {
																	@Override 
																	public IndexDocumentFieldConfigSet<?> apply(final IndexerComponent<? extends IndexableModelObject<?>> indexerComponent) {
																		return indexerComponent.getIndexDocumentFieldsConfig();
																	}
									  			 		   })
									  			  .toSet());
			}
		}
		return outDocTypes;
	}
}
