package r01f.persistence.jobs;

import java.util.concurrent.ExecutorService;

import com.google.inject.Provider;

import r01f.concurrent.DaemonExecutorServiceLifeCycleManager;
import r01f.concurrent.ExecutorServiceManager;

/**
 * Provides an {@link ExecutorServiceManager} in charge of the life cycle of the
 * {@link ExecutorService} that handle crud events in the background
 */
public class ExecutorServiceManagerProvider
  implements Provider<ExecutorServiceManager> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final int _numberOfBackgroundThreads;
	
	public ExecutorServiceManagerProvider(final int numberOfBackgroundThreads) {
		_numberOfBackgroundThreads = numberOfBackgroundThreads;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ExecutorServiceManager get() {
		// Create a daemon executor service life cycle manager
		ExecutorServiceManager execServiceManager = new DaemonExecutorServiceLifeCycleManager(_numberOfBackgroundThreads);
		execServiceManager.start();

		return execServiceManager;
	}

}
