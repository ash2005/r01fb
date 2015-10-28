package r01f.concurrent;

import java.io.Serializable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Manages the life cycle of an {@link ExecutorService} for background (daemon) jobs
 * It uses a {@link DaemonThreadFactory} to create daemon threads<
 * 
 * USAGE NOTES INSIDE A SERVLER CONTAINER (ie Tomcat)
 * ==================================================
 * The key is that the {@link ExecutorService} is created when the {@link ServletContext} is
 * initialized and destroyed when {@link ServletContext} is destroyed
 * 
 * This type MUST be used at a {@link ServletContextListener} type as:
 * <pre class='brush:java'>
 * 		public class MyContextListener 
 * 		  implements ServletContextListener {
 * 
 * 			private DaemonExecutorServiceLifeCycleManager _lifeCycleManager;
 * 
 * 			public void contextInitialized(ServletContextEvent servletContextEvent) {
 * 				// Create and initialize the daemon ExecutorService to run background jobs in tomcat
 *				_lifeCycleManager = new DaemonExecutorServiceLifeCycleManager(5);
 *				_lifeCycleManager.initialize();
 * 			}
 * 			public void contextDestroyed(ServletContextEvent servletContextEvent) {
 * 				_lifeCycleManager.destroy(); 
 * 			}
 * 		}
 * </pre>
 * 
 * http://stackoverflow.com/questions/4907502/running-a-background-java-program-in-tomcat
 */
@Slf4j
@RequiredArgsConstructor
public class DaemonExecutorServiceLifeCycleManager 
  implements ExecutorServiceManager,
  			 Serializable {

	private static final long serialVersionUID = -775988426740172684L;
/////////////////////////////////////////////////////////////////////////////////////////
//  FINAL FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Config
	 */
	private final int _numberOfThreadsInPool;
/////////////////////////////////////////////////////////////////////////////////////////
//  NON FINAL FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The thread pool
	 */
	private ExecutorService _executor;
/////////////////////////////////////////////////////////////////////////////////////////
//  ServiceHandler
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void start() {
		if (_executor == null) {
	        ThreadFactory daemonFactory = new DaemonThreadFactory();
	
	        int theNumExecutors = _numberOfThreadsInPool <= 0 ? 1 : _numberOfThreadsInPool;
	        log.warn("Creating a background jobs executor pool with size={}",theNumExecutors);
	        if (theNumExecutors <= 1) {
	        	_executor = Executors.newSingleThreadExecutor(daemonFactory);
	        } else {
	        	_executor = Executors.newFixedThreadPool(theNumExecutors,
	        											 daemonFactory);
	        }
		}
	}
	@Override
	public void stop() {
		// Shutdown the thread pool or process/wait until all pending jobs are done
        _executor.shutdownNow(); 	// this DO NOT close the executor service... simply tells it not to accept more tasks
									// see: http://java.dzone.com/articles/executorservice-10-tips-and
									//		http://java.dzone.com/articles/interrupting-executor-tasks
		try {
			// wait for running tasks to finalize... 10 seconds/cycle x 10 cycles = 100 seconds
			int cyclesAwaited = 1;
			int secondsPerCycle = 10;
			boolean done = _executor.awaitTermination(secondsPerCycle,TimeUnit.SECONDS);
			while(!done && cyclesAwaited < 10) {
				log.warn("\t--still pending jobs executing....");
				done = _executor.awaitTermination(secondsPerCycle,TimeUnit.SECONDS);
				cyclesAwaited++;
			}
		} catch(InterruptedException intEx) {
			intEx.printStackTrace();
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public ExecutorService getExecutorService() {
		if (_executor == null) this.start();	// Ensure the executor is initialized!
		return _executor;
	}
}
