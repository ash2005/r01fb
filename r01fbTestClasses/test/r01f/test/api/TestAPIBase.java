package r01f.test.api;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.inject.ServiceHandler;
import r01f.services.client.ClientAPI;
import r01f.services.interfaces.IndexManagementServices;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * JVM arguments:
 * -javaagent:D:/tools_workspaces/eclipse/local_libs/aspectj/lib/aspectjweaver.jar -Daj.weaving.verbose=true
 */
@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class TestAPIBase<A extends ClientAPI> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS 
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter protected A _api;
	@Getter private Collection<String> _jpaPersistenceUnits;											
	@Getter private Collection<Class<? extends IndexManagementServices>> _indexMgmtSrvcsTypes;
	
	protected void setApi(final A api) {
		_api = api;
	}
	protected void setJpaPersistenceUnits(final Collection<String> units) {
		if (CollectionUtils.hasData(units)) _jpaPersistenceUnits = units;
	}
	protected void setJpaPersistenceUnits(final String... units) {
		if (CollectionUtils.hasData(units)) _jpaPersistenceUnits = Arrays.asList(units);
	}
	protected void setIndexMgmtSrvcsType(final Class<? extends IndexManagementServices>... indexMgmtSrvcsTypes) {
		_indexMgmtSrvcsTypes = Lists.newArrayList(indexMgmtSrvcsTypes);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  SETUP
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return an api instance
	 */
	protected abstract A _provideApiInstance();
	/**
	 * @return a GUICE {@link Injector} instance
	 */
	protected abstract Injector _getGuiceInjector();
	/**
	 * Setups api, starts jpa persistence service...
	 * @param instance
	 */
	protected void _setUp() {
		// Create the API
		_api = _provideApiInstance();
		
		// If stand-alone (no app-server is used), init the JPA servide
		// 		If the core is available at client classpath, start it
		// 		This is the case where there's no app-server
		// 		(usually the JPA's ServiceHandler is binded at the Guice module extending DBGuiceModuleBase at core side)
		if (CollectionUtils.hasData(_jpaPersistenceUnits)) {
			for (String jpaPersistenceUnit : _jpaPersistenceUnits) {
				ServiceHandler jpaServiceHandler = _getGuiceInjector().getInstance(Key.get(ServiceHandler.class,
																		  		   Names.named(jpaPersistenceUnit)));
				if (jpaServiceHandler != null) {
					log.warn("\t--Init JPA's PersistenceService....");
					jpaServiceHandler.start();
				} else {
					throw new IllegalStateException("No JPA persistence provider bound with name=" + jpaPersistenceUnit);
				}
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TEAR DONW
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Releases resources (jpa persistence service, lucene index, etc)
	 */
	protected void _tearDown() {
		// If stand-alone (no app-server is used):
		//		[1]: init the JPA servide
		// 				If the core is available at client classpath, start it
		// 				This is the case where there's no app-server
		// 				(usually the JPA's ServiceHandler is binded at the Guice module extending DBGuiceModuleBase at core side)
		//		[2]: Close search engine indexes
		if (CollectionUtils.hasData(_jpaPersistenceUnits)) {
			for (String jpaPersistenceUnit : _jpaPersistenceUnits) {
				ServiceHandler jpaServiceHandler = _getGuiceInjector().getInstance(Key.get(ServiceHandler.class,
																		  		   Names.named(Strings.customized(jpaPersistenceUnit))));
				if (jpaServiceHandler != null) {
					log.warn("\t--Closing JPA's PersistenceService....");
					jpaServiceHandler.stop();
				} else {
					throw new IllegalStateException("No JPA persistence provider bound with name=" + jpaPersistenceUnit);
				}
			}
			if (CollectionUtils.hasData(_indexMgmtSrvcsTypes)) {
				for (Class<? extends IndexManagementServices> indexMgmtSrvcType : _indexMgmtSrvcsTypes) {
					ServiceHandler indexMgr = _getGuiceInjector().getInstance(indexMgmtSrvcType);
					if (indexMgr != null) {
						log.warn("\t--closing indexer using manger: {}",indexMgmtSrvcType);
						indexMgr.stop();
					}
				}
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected void runTest(final int iterationNum) {
		try {
			// [1]-Set things up
			_setUp();
			
			Stopwatch stopWatch = Stopwatch.createStarted();
			
			for (int i=0; i < iterationNum; i++) {
				Stopwatch itStopWatch = Stopwatch.createStarted();
				System.out.println("\n\n\n\nSTART =========== Iteration " + i + " ===================\n\n\n\n");
				
				_doTest();		// Iteration test
				
				System.out.println("\n\n\n\nEND =========== Iteration " + i + " > " + itStopWatch.elapsed(TimeUnit.SECONDS) + "seconds ===================\n\n\n\n");
			}
			
			System.out.println("\n\n\n\n******* ELAPSED TIME: " + NumberFormat.getNumberInstance(Locale.getDefault()).format(stopWatch.elapsed(TimeUnit.SECONDS)) + " seconds");
			stopWatch.stop();
		} catch(Throwable th) {
			th.printStackTrace(System.out);
			
		} finally {
			// [99]-Tear things down
			_tearDown();
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected abstract void _doTest();
	
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _giveTimeForBackgroundJobsToFinish(final long milis) {
		// wait for background jobs to complete (if there's any background job that depends on DB data -like lucene indexing-
		// 										 if the DB data is deleted BEFORE the background job finish, it'll fail)
			System.out.println(".... give " + milis + " milis for background jobs (ie lucene index) to complete before deleting created DB records (lucene indexing will fail if the DB record is deleted)");
		try {
			Thread.sleep(milis);
		} catch(Throwable th) {
			th.printStackTrace(System.out);
		}
	}
}
