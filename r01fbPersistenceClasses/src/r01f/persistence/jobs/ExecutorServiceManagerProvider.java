package r01f.persistence.jobs;

import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import r01f.concurrent.DaemonExecutorServiceLifeCycleManager;
import r01f.concurrent.ExecutorServiceManager;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.inject.Provider;

/**
 * Provides an {@link ExecutorServiceManager} in charge of the life cycle of the
 * {@link ExecutorService} that handle crud events in the background
 */
public class ExecutorServiceManagerProvider
  implements Provider<ExecutorServiceManager> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject @XMLPropertiesComponent("persistence") 
	private XMLPropertiesForAppComponent _props;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ExecutorServiceManager get() {
		int numberOfBackgroundThreads = _props.propertyAt("persistence/crudEventsHandling/numberOfThreadsInPool")
											  .asInteger(1); 	// single threaded by default
		
		// Create a daemon executor service life cycle manager
		ExecutorServiceManager execServiceManager = new DaemonExecutorServiceLifeCycleManager(numberOfBackgroundThreads);
		execServiceManager.start();

		return execServiceManager;
	}

}
