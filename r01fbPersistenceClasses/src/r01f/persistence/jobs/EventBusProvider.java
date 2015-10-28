package r01f.persistence.jobs;

import java.util.concurrent.ExecutorService;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.ExecutorServiceManager;
import r01f.types.ExecutionMode;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provides event buses
 * (a provider is used since it's injected and can be used while guice bootstraping)
 */
@Slf4j
public class EventBusProvider
  implements Provider<EventBus> {
/////////////////////////////////////////////////////////////////////////////////////////
//  INJECTED FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Inject @XMLPropertiesComponent("services")
	private XMLPropertiesForAppComponent _props;
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
	@Inject(optional=true)		// SYNC eventBus DO NOT need an ExecutorServiceManager
	@Getter private ExecutorServiceManager _executorServiceManager;

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
		
		// Get from the properties the way CRUD events are to be consumed: synchronously or asynchronously 
		ExecutionMode execMode = _props.propertyAt("services/crudEventsHandling/@mode").asEnumElement(ExecutionMode.class);
		if (execMode == null) {
			log.warn("CRUD Events Handling config could NOT be found at [appCode].[component].services.properties.xml, please ensure that the [appCode].[component].services.properties.xml" +
					 "contains a 'crudEventsHandling' section; meanwhile SYNC event handling is assumed");
			execMode = ExecutionMode.SYNC;
		}
		
		log.warn("Creating a {} event bus",execMode); 
		switch(execMode) {
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