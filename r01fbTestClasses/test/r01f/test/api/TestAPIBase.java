package r01f.test.api;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Stopwatch;
import com.google.inject.Injector;
import com.google.inject.Key;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.Threads;
import r01f.inject.ServiceHandler;
import r01f.services.client.ClientAPI;
import r01f.types.TimeLapse;
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
//  STATIC FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static Injector GUICE_INJECTOR;
	protected static ClientAPI CLIENT_API;
	protected static Collection<Key<? extends ServiceHandler>> _hasServiceHandlerTypes;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public A getClientApi() {
		return (A)CLIENT_API;
	}
	@SuppressWarnings("static-method")
	public Injector getGuiceInjector() {
		return GUICE_INJECTOR;
	}
///////////////////////////////////////////////////////////////////////////////////////////	
//  RUN EXACTLY ONCE AT THE VERY BEGINNING OF THE TEST AS A WHOLE
//  (in fact they're run even before the type is constructed -that's why they're static)
///////////////////////////////////////////////////////////////////////////////////////////
	protected static void _setUpBeforeClass(final Injector guiceInjector,
											final Class<? extends ClientAPI> apiType,
										    final Key<? extends ServiceHandler>... hasServiceHandlerTypes) throws Exception {
		// Store the guice injector
		GUICE_INJECTOR = guiceInjector;
		
		// Store the service handler types
		_hasServiceHandlerTypes = CollectionUtils.hasData(hasServiceHandlerTypes) ? Arrays.asList(hasServiceHandlerTypes) : null;
		
		// Create the API
		CLIENT_API = GUICE_INJECTOR.getInstance(apiType);
		
		// If stand-alone (no app-server is used), init the JPA service or any service that needs to be started
		// like the search engine index
		// 		If the core is available at client classpath, start it
		// 		This is the case where there's no app-server
		// 		(usually the JPA's ServiceHandler is binded at the Guice module extending DBGuiceModuleBase at core side)
		if (CollectionUtils.hasData(_hasServiceHandlerTypes)) {
			for (Key<? extends ServiceHandler> hasServiceHandlerType : _hasServiceHandlerTypes) {
				ServiceHandler serviceHandler = GUICE_INJECTOR.getInstance(hasServiceHandlerType);
				log.warn("\t--START SERVICE using {} type: {}",ServiceHandler.class.getSimpleName(),hasServiceHandlerType);
				serviceHandler.start();
			}
		}
	}
	protected static void _tearDownAfterClass() throws Exception {
		// If stand-alone (no app-server is used), close the JPA service or any service that needs to be started
		// like the search engine index
		if (CollectionUtils.hasData(_hasServiceHandlerTypes)) {
			for (Key<? extends ServiceHandler> hasServiceHandlerType : _hasServiceHandlerTypes) {
				ServiceHandler serviceHandler = GUICE_INJECTOR.getInstance(hasServiceHandlerType);
				if (serviceHandler != null) {
					log.warn("\t--END SERVICE {} type: {}",ServiceHandler.class.getSimpleName(),hasServiceHandlerType);
					serviceHandler.stop();
				}
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected void runTest(final int iterationNum) {
		try {			
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
			
		} 
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _doTest() {
		log.warn("MUST implement this!");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	protected static void _giveTimeForBackgroundJobsToFinish(final long milis) {
		_giveTimeForBackgroundJobsToFinish(milis,
										   null);	// default msg
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final TimeLapse lapse) {
		_giveTimeForBackgroundJobsToFinish(lapse.asMilis(),
										   null);	// default msg
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final long milis,
															 final String msg,final Object... msgParams) {
		// wait for background jobs to complete (if there's any background job that depends on DB data -like lucene indexing-
		// 										 if the DB data is deleted BEFORE the background job finish, it'll fail)
		if (Strings.isNullOrEmpty(msg)) {
			log.warn("... give {} milis for background jobs (ie lucene index) to complete before deleting created DB records (ie lucene indexing will fail if the DB record is deleted)",milis);
		} else {
			log.warn("... give {} milis for {}",milis,Strings.customized(msg,msgParams));
		}
		Threads.safeSleep(milis);
	}
	protected static void _giveTimeForBackgroundJobsToFinish(final TimeLapse lapse,
															 final String msg,final Object... msgParams) {
		_giveTimeForBackgroundJobsToFinish(lapse.asMilis(),
										   msg,msgParams);
	}
}
