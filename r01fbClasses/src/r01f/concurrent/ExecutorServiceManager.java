package r01f.concurrent;

import java.util.concurrent.ExecutorService;

import r01f.inject.ServiceHandler;

/**
 * Interface for types storing an {@link ExecutorService}
 */
public interface ExecutorServiceManager 
		 extends ServiceHandler {		// see ServletContextListenerBase
	/**
	 * Returns the {@link ExecutorService} if it has been created, that's AFTER {@link ServletContext}
	 * initialization
	 * @return the executor service
	 */
	public ExecutorService getExecutorService();
}
