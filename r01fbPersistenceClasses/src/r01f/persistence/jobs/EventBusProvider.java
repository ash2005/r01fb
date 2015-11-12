package r01f.persistence.jobs;

import java.util.concurrent.ExecutorService;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.Provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.ExecutorServiceManager;
import r01f.types.ExecutionMode;

/**
 * Provides event buses
 * (a provider is used since it's injected and can be used while guice bootstraping)
 */
@Slf4j
@RequiredArgsConstructor
public class EventBusProvider
  implements Provider<EventBus> {
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTED FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Execution mode
	 */
	@Getter private final ExecutionMode _execMode;
	/**
	 * The jobs executor holder: manages a thread pool in charge of dispatching events
	 * In a web application environment (ie Tomcat), this thread pool MUST be destroyed
	 * when the servlet context is destroyed; to do so, the executor service manager is
	 * used in a {@link ServletContextListener}
	 * 
	 * ... so in a web app environment:
	 * 		This executor service manager MUST be binded at guice module with access to 
	 * 		the {@link ServletContext} (ie RESTJerseyServletGuiceModuleBase)
	 * 		
	 * 		This executor service manager is USED at a {@link ServletContextListener}'s destroy()
	 * 		method to kill the worker threads (ie R01VServletContextListener)
	 */
	@Getter private final ExecutorServiceManager _executorServiceManager;

/////////////////////////////////////////////////////////////////////////////////////////
//  NOT INJECTED FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private EventBus _eventBusInstance = null;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public EventBus get() {
		if (_eventBusInstance != null) return _eventBusInstance;
		
		log.warn("Creating a {} event bus",_execMode); 
		switch(_execMode) {
		case ASYNC:
			ExecutorService execService = _executorServiceManager.getExecutorService();
			if (execService == null) {
				log.error("CRUD events are configured to be consumed ASYNCHRONOUSLY but no ExecutorService could be obtained... the CRUD events will be consumed SYNCHRONOUSLY!!!!");
				_eventBusInstance = new EventBus("R01E EventBus");	// sync event bus 
			} else {
				_eventBusInstance = new AsyncEventBus("R01 ASYNC EventBus",
										 		  	  execService);
			}
			break;
		case SYNC:
			_eventBusInstance = new EventBus("R01 SYNC EventBus");
			break;
		default:
			throw new IllegalStateException();
		}
		return _eventBusInstance;
	}
}