package r01f.internal;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Singleton;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.ExecutorServiceManager;
import r01f.events.PersistenceOperationEventListeners.PersistenceOperationErrorEventListener;
import r01f.events.PersistenceOperationEventListeners.PersistenceOperationOKEventListener;
import r01f.events.crud.CRUDOperationErrorEventListener;
import r01f.inject.Matchers;
import r01f.inject.ServiceHandler;
import r01f.persistence.internal.DBGuiceModuleBase;
import r01f.persistence.internal.SearchGuiceModuleBase;
import r01f.persistence.jobs.AsyncEventBusProvider;
import r01f.persistence.jobs.ExecutorServiceManagerProvider;
import r01f.persistence.jobs.SyncEventBusProvider;
import r01f.services.ServiceIDs.CoreAppCode;
import r01f.services.ServiceIDs.CoreModule;
import r01f.services.ServicesPackages;
import r01f.services.core.internal.BeanImplementedServicesCoreBootstrapGuiceModuleBase;
import r01f.types.ExecutionMode;
import r01f.util.types.Strings;

/**
 * Mappings internal to services core implementation
 * IMPORTANT!!!!
 * =============
 * If this type is refactored and move to another package, it's VERY IMPORTANT to 
 * change the ServicesCoreBootstrap _findCoreGuiceModuleOrNull() method!!!!!
 */
@Slf4j
@EqualsAndHashCode(callSuper=true)				// This is important for guice modules
public abstract class BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase
              extends BeanImplementedServicesCoreBootstrapGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final Module... otherModules) {
		super(otherModules != null ? Arrays.asList(otherModules) : null);		
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final DBGuiceModuleBase dbGuiceModule) {
		super(dbGuiceModule,
			  null);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final DBGuiceModuleBase dbGuiceModule,
														   				  final Module... otherModules) {
		super(dbGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final DBGuiceModuleBase dbGuiceModule,
														   				  final Collection<Module> otherModules) {
		super(dbGuiceModule,
			  otherModules);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final DBGuiceModuleBase dbGuiceModule,
														   				  final SearchGuiceModuleBase searchGuiceModule,
														   				  final Collection<Module> otherModules) {
		super(dbGuiceModule,
			  searchGuiceModule,
			  otherModules);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final DBGuiceModuleBase dbGuiceModule,
														   				  final SearchGuiceModuleBase searchGuiceModule,
														   				  final Module... otherModules) {
		super(dbGuiceModule,
			  searchGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final SearchGuiceModuleBase searchGuiceModule,
														   				  final Module... otherModules) {
		super(searchGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final SearchGuiceModuleBase searchGuiceModule,
														   				  final Collection<Module> otherModules) {
		super(searchGuiceModule,
			  otherModules);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Avoid multiple bindings 
	 */
	private boolean CRUD_OPERATION_ERROR_LISTENER_BINDED = false;
	
	private boolean DBPERSISTENCE_BINDINGS_SET = false;
	private boolean SEARCH_BINDINGS_SET = false;
	
	@Override 
	public void configure(final Binder binder) {
		super.configure(binder);	// this is where all sub-modules are installed!!
		
		final CoreAppCode coreAppCode = ServicesPackages.appCodeFromCoreBootstrapModuleType(this.getClass());				// the appCode is extracted from the package
		final CoreModule coreModule = ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(this.getClass());		// the component is extracted from the @ServiceCore annotation
		
		
		final Binder theBinder = binder;
		
		// [1]: Bind XMLProperties for persistence and search
		if (!DBPERSISTENCE_BINDINGS_SET && this.isModuleInstalled(DBGuiceModuleBase.class)) {
			_bindXMLPropertiesComponentProviderFor(coreAppCode,coreModule,
												   "dbpersistence",
												   theBinder);
			DBPERSISTENCE_BINDINGS_SET = true;
			
		}
		if (!SEARCH_BINDINGS_SET && this.isModuleInstalled(SearchGuiceModuleBase.class)) {
			_bindXMLPropertiesComponentProviderFor(coreAppCode,coreModule,
												   "searchpersistence",
												   theBinder);
			SEARCH_BINDINGS_SET = true;
		}
		
		
		// [2]: Bind event listeners 
		// ==================================================
		// Event Bus & Background jobs
		if (this instanceof ServicesBootstrapGuiceModuleBindsCRUDEventListeners) {
			// Get from the properties the way CRUD events are to be consumed: synchronously or asynchronously
			ExecutionMode execMode = _servicesCoreProps.propertyAt("services/crudEventsHandling/@mode")
										  	   		   .asEnumElement(ExecutionMode.class);
			if (execMode == null) {
				log.warn("Events Handling config could NOT be found at {}.{}.properties.xml, please ensure that the {}.{}.properties.xml" +
						 "contains a 'crudEventsHandling' section; meanwhile SYNC event handling is assumed",
						 _servicesCoreProps.getAppCode(),_servicesCoreProps.getAppComponent(),_servicesCoreProps.getAppCode(),_servicesCoreProps.getAppComponent());
				execMode = ExecutionMode.SYNC;
			}
			log.warn("{}.{} events handling: {}; " + 
					 "the event handling mode at can be changed at property 'services/crudEventsHandling/@mode' at {}.{}.properties.xml",
					 _servicesCoreProps.getAppCode(),_servicesCoreProps.getAppComponent(),
					 execMode,
					 _servicesCoreProps.getAppCode(),_servicesCoreProps.getAppComponent());
			// The EventBus needs an ExecutorService (a thread pool) to manage events in the background
			ExecutorServiceManagerProvider execServiceManagerProvider = null;
			if (execMode == ExecutionMode.ASYNC) {
				// create the executer service provider
				int numberOfBackgroundThreads = _servicesCoreProps.propertyAt("services/crudEventsHandling/numberOfThreadsInPool")
													 	  		  .asInteger(1); 	// single threaded by default
				log.warn("Events handling will be ASYNCHRONOUSLY handled using a thread pool of {} threads; " +
						 "the size can be changed at 'services/crudEventsHandling/numberOfThreadsInPool' property at {}.{}.properties.xml",
						 numberOfBackgroundThreads,_servicesCoreProps.getAppCode(),_servicesCoreProps.getAppComponent());
				execServiceManagerProvider = new ExecutorServiceManagerProvider(numberOfBackgroundThreads);
				theBinder.bind(ExecutorServiceManager.class)
						 .toProvider(execServiceManagerProvider)
						 .in(Singleton.class);
				
				// Expose the ServiceHandler to stop the exec manager threads
				String bindingName = Strings.customized("{}.{}.backgroundTasksExecService",coreAppCode,coreModule);
				theBinder.bind(ServiceHandler.class)
						 .annotatedWith(Names.named(bindingName))
						 .to(ExecutorServiceManager.class);
				if (theBinder instanceof PrivateBinder) {
					PrivateBinder privateBinder = (PrivateBinder)binder;
					privateBinder.expose(Key.get(ServiceHandler.class,
												 Names.named(bindingName)));	// expose the binding
				}
				
				// create the event bus provider
				theBinder.bind(EventBus.class)
						 .toProvider(AsyncEventBusProvider.class)
						 .in(Singleton.class);
			} else {
				theBinder.bind(EventBus.class)
						 .toProvider(SyncEventBusProvider.class)
						 .in(Singleton.class);
			}
			
			
			// Automatic registering of event listeners to the event bus avoiding the
			// manual registering of every listener; this simply listen for guice's binding events;
			// when an event listener gets binded, it's is automatically registered at the event bus
			// 		Listen to injection of CRUDOperationOKEventListener & CRUDOperationNOKEventListener subtypes (indexers are CRUD events listeners)
			// 		(when indexers are being injected)
			EventBusSubscriberTypeListener typeListener = new EventBusSubscriberTypeListener(theBinder.getProvider(EventBus.class));	// inject a Provider to get dependencies injected!!!
			theBinder.bindListener(Matchers.subclassesOf(PersistenceOperationOKEventListener.class,
													     PersistenceOperationErrorEventListener.class),
							       typeListener);	// registers the event listeners at the EventBus
			
			// These fires the creation of event listeners and thus them being registered at the event bus
			// by means of the EventBusSubscriberTypeListener bindListener (see below)
			if (!CRUD_OPERATION_ERROR_LISTENER_BINDED) {
				theBinder.bind(CRUDOperationErrorEventListener.class)
					  	 .toInstance(new CRUDOperationErrorEventListener());				// CRUDOperationNOKEvent for EVERY model object
				CRUD_OPERATION_ERROR_LISTENER_BINDED = true;
			}
			
			// Bind every listener
			((ServicesBootstrapGuiceModuleBindsCRUDEventListeners)this).bindCRUDEventListeners(theBinder);
		}
	}
	/**
	 * Guice {@link TypeListener} that gets called when a {@link PersistenceOperationOKEventListener} subtype (the indexer is a CRUD events listener)
	 * is injected (or created) (this is called ONCE per type)
	 * AFTER the {@link PersistenceOperationOKEventListener} subtype is injected (or created), it MUST be registered at the {@link EventBus} 
	 */
	@RequiredArgsConstructor
	private class EventBusSubscriberTypeListener
	   implements TypeListener {
		
		// The EventBus cannot be injected because it cannot be created inside a module
		// however an EventBus provider can be injected and in turn it's injected with 
		// it's dependencies
		// see r01f.persistence.jobs.EventBusProvider
		private final Provider<EventBus> _eventBusProvider;
		
		@Override
		public <I> void hear(final TypeLiteral<I> type,
							 final TypeEncounter<I> encounter) {
			encounter.register(// AFTER the type is injected it MUST be registered at the EventBus
							   new InjectionListener<I>() {
										@Override
										public void afterInjection(final I injecteeEventListener) {
											log.warn("\tRegistering {} event listener at event bus {}",
													 injecteeEventListener.getClass(),
													 _eventBusProvider.get());
											_eventBusProvider.get()
													 		 .register(injecteeEventListener);	// register the indexer (the indexer is an event listener)
										}
							   });
		}
	}
}
