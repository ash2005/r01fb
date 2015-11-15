package r01f.test.persistence;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import org.junit.Assert;

import r01f.guids.OID;
import r01f.model.ModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.search.SearchFilterForModelObject;
import r01f.model.search.SearchResultItemForModelObject;
import r01f.model.search.SearchResults;
import r01f.reflection.ReflectionUtils;
import r01f.services.client.api.delegates.ClientAPIDelegateForModelObjectSearchServices;

import com.google.common.base.Stopwatch;

@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class TestPersistableModelObjectSearch<F extends SearchFilterForModelObject,I extends SearchResultItemForModelObject<? extends OID,? extends ModelObject>> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////	
	private final ClientAPIDelegateForModelObjectSearchServices<F,I> _searchAPI;
	private final TestPersistableModelObjectFactory<? extends OID,? extends PersistableModelObject<? extends OID>> _modelObjFactory;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static <F extends SearchFilterForModelObject,I extends SearchResultItemForModelObject<? extends OID,? extends ModelObject>>
		   TestPersistableModelObjectSearch<F,I> create(final ClientAPIDelegateForModelObjectSearchServices<F,I> searchApi,
				   											final TestPersistableModelObjectFactory<? extends OID,? extends PersistableModelObject<? extends OID>> modelObjFactory) {
		return new TestPersistableModelObjectSearch<F,I>(searchApi,
															 modelObjFactory);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tests the search api
	 * @param modelObject
	 */
	@SuppressWarnings("unchecked")
	public <O extends OID,M extends PersistableModelObject<O>> void testSearch(final F filter) {		
		System.out.println("[init][TEST BASIC SEARCH]-----------------------------------------------------------------------");
		
		Stopwatch stopWatch = Stopwatch.createStarted();
		
		// [0]: SetUp: create some test objects
		TestPersistableModelObjectFactory<O,M> modelObjFactory = (TestPersistableModelObjectFactory<O,M>)_modelObjFactory;
		modelObjFactory.setUpMockModelObjs(5);
		
		// [1]: give time to the objects being indexed
		try {
			System.out.println("... giving time for the objects to be indexed");
			Thread.sleep(5000);
		} catch(InterruptedException intEx) {
			/* ignore */
		}
		
		// [2]: Run tests		
		System.out.println("SEARCH ENTITIES WITH THE FILTER: " + filter.toCriteriaString());
		SearchResults<F,I> results = _searchAPI.search(filter).firstPage();
		Assert.assertTrue(results.getTotalItemsCount() > 0);
				
		TestSearchUtil.debugSearchResults(results);
		
		// [99]: Delete previously created test objects to restore DB state
		_modelObjFactory.tearDownCreatedMockModelObjs();
		System.out.println("[end ][TEST BASIC SEARCH] (elapsed time: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)) + " milis) -------------------------");
		stopWatch.stop();
	}
	/**
	 * Test the index & unindex operations
	 * @param emptyFilter
	 */
	@SuppressWarnings("unchecked")
	public <O extends OID,M extends PersistableModelObject<O>> void testIndexAndUnIndex() {
		long totalItems = 0;
		
		System.out.println("[init][TEST INDEX & UNINDEX]--------------------------------------------------------------------");
		Stopwatch stopWatch = Stopwatch.createStarted();
		
		// [1]: Set-up 10 model objects
		System.out.println("Create 10 model objects....");
		TestPersistableModelObjectFactory<O,M> modelObjFactory = (TestPersistableModelObjectFactory<O,M>)_modelObjFactory;	
		modelObjFactory.setUpMockModelObjs(10);
		
		// [2]: Give time for the objects to be indexed
		System.out.println("... wait some time to give space for the indexer to index all objects");
		try {
			Thread.sleep(10000);		// give time
		} catch(Throwable th) {
			/* ignore */
		}
		
		// [3]: Ensure that there're 10 indexed objects
		F emptyFilter = ReflectionUtils.createInstanceOf(_searchAPI.getFilterType());		// empty filter
		totalItems = _searchAPI.search(emptyFilter)		// an empty filter
									.firstPage()
									.getTotalItemsCount();
		Assert.assertTrue(totalItems == 10);
		
		// [4]: Wipe previously created objects
		_modelObjFactory.tearDownCreatedMockModelObjs();
		
		// [5]: Give time for the objects to be unindexed
		System.out.println("... wait some time to give space for the indexer to unindex all objects");
		try {
			Thread.sleep(10000);		// give time
		} catch(Throwable th) {
			/* ignore */
		}
		
		// [6]: Ensure that there're 10 indexed objects
		totalItems = _searchAPI.search(emptyFilter)		// an empty filter
									.firstPage()
									.getTotalItemsCount();
		Assert.assertTrue(totalItems == 0);
		
		System.out.println("[end ][TEST BASIC INDEX & UNINDEX] (elapsed time: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.MILLISECONDS)) + " milis) -------------------------");
	}
}
