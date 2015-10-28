package r01f.persistence.internal;


import java.io.File;
import java.io.IOException;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.inject.HasMoreBindings;
import r01f.persistence.index.IndexManager;
import r01f.persistence.index.lucene.LuceneIndexManager;
import r01f.persistence.lucene.LuceneIndex;
import r01f.persistence.search.lucene.LuceneLanguageDependentAnalyzer;
import r01f.util.types.Strings;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.ProvisionException;
import com.google.inject.Singleton;

/**
 * Base {@link Guice} module for search engine (index / search) bindings
 */
@Slf4j
public abstract class LuceneSearchGuiceModuleBase 
              extends SearchGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
// DOCUMENT FIELDS CONFIG
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The document config type for every model object 
	 */
//	private final Set<SearchComponents> _searchComponents;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app divided into components
	 * In this case, the app is composed by a one or more components and the properties are going to be looked after at
	 * [appCode].[appComponent].persistence.properties.xml
	 * @param appCode
	 * @param appComponent
	 * @param searchComponents
	 */
	protected LuceneSearchGuiceModuleBase(final AppCode appCode,final AppComponent appComponent,
								    	  final Set<SearchComponents> searchComponents) {
		super(appCode,
			  AppComponent.forId(appComponent.asString()),
			  searchComponents);
	}
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app divided into components
	 * In this case, the app is composed by a one or more components and the properties are going to be looked after at
	 * [appCode].[appComponent].persistence.properties.xml
	 * @param appCode
	 * @param appComponent
	 * @param searchComponents
	 */
	protected LuceneSearchGuiceModuleBase(final AppCode appCode,final AppComponent appComponent,
								    	  final SearchComponents... searchComponents) {
		super(appCode,
			  AppComponent.forId(appComponent.asString()),
			  searchComponents);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * If many SearchGuiceModules are binded, avoid multiple lucene index binding
	 */
	private static boolean LUCENE_ANALYZER_BINDED = false;
	private static boolean LUCENE_DIRECTORY_BINDED = false;
	
	@Override 
	public void configure(final Binder binder) {
		// Basic indexers & searchers config
		super.configure(binder);
		
		// ... Lucene Index
		binder.bind(LuceneIndex.class)
			  .in(Singleton.class);
		if (!LUCENE_ANALYZER_BINDED) {
			binder.bind(Analyzer.class)
				  .toInstance(new LuceneLanguageDependentAnalyzer(_indexDocumentTypes(_searchComponents)));		// singleton binding...
			LUCENE_ANALYZER_BINDED = true;
		}
		if (!LUCENE_DIRECTORY_BINDED) {
			binder.bind(Directory.class)
				  .toInstance(_createLuceneDirectory(_indexFilesPath()));			// singleton binding
			LUCENE_DIRECTORY_BINDED = true;
		}
		
		// Bind the index manager
		binder.bind(IndexManager.class)
			  .to(LuceneIndexManager.class)
			  .in(Singleton.class);
	
		// Give chance to sub-types to do more bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(binder);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
////////////////////////////////////////////////////////////////////////////////////////
	private String _indexFilesPath() {
		String indexFilesPath = this.propertyAt("persistence/search/lucene/indexStore")
								    .asString(Strings.of("d:/temp_dev/{}/lucene")
								    				 .customizeWith(this.getAppCode())
								    				 .asString());
		return indexFilesPath;
	}
	private static Directory _createLuceneDirectory(final String indexFilesPath) {
		try {
			// Ensure the dir exists
			File indexFilesDir = new File(indexFilesPath);
			if (!indexFilesDir.exists()) {
				log.warn("The lucen index dir {} didn't existed so it's created",indexFilesPath);
				indexFilesDir.mkdirs();
			}
			// Create the lucene's FSDDirectory 
			return FSDirectory.open(new File(indexFilesPath));
		} catch(IOException ioEx) {
			throw new ProvisionException("Could not provide an instance of Lucene Directory at directory " + indexFilesPath + ": " + ioEx.getMessage(),
										 ioEx);
		}
	}
	@SuppressWarnings("unused")
	private boolean _isLuceneEnabled() {

		boolean outEnabled = super.propertyAt("persistence/search/lucene/@enabled").asBoolean();
		if (!outEnabled) log.warn("Lucene indexing is DISABLED!!");
		return outEnabled;
	}
}
