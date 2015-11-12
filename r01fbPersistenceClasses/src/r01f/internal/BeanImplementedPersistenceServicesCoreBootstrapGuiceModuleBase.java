package r01f.internal;

import java.util.Arrays;
import java.util.Collection;

import javax.inject.Singleton;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
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
import r01f.guids.CommonOIDs.AppCode;
import r01f.inject.Matchers;
import r01f.persistence.internal.DBGuiceModuleBase;
import r01f.persistence.internal.SearchGuiceModuleBase;
import r01f.persistence.jobs.EventBusProvider;
import r01f.persistence.jobs.ExecutorServiceManagerProvider;
import r01f.services.core.BeanImplementedServicesCoreBootstrapGuiceModuleBase;
import r01f.types.ExecutionMode;
import r01f.xmlproperties.XMLPropertiesComponentImpl;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

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
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
														   final Module... otherModules) {
		super(apiAppCode,
		      otherModules != null ? Arrays.asList(otherModules) : null);		
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
														   final DBGuiceModuleBase dbGuiceModule,
														   final Module... otherModules) {
		super(apiAppCode,
			  dbGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
														   final SearchGuiceModuleBase searchGuiceModule,
														   final Module... otherModules) {
		super(apiAppCode,
			  searchGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
														   final SearchGuiceModuleBase searchGuiceModule,
														   final Collection<Module> otherModules) {
		super(apiAppCode,
			  searchGuiceModule,
			  otherModules);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
														   final DBGuiceModuleBase dbGuiceModule,
														   final SearchGuiceModuleBase searchGuiceModule,
														   final Collection<Module> otherModules) {
		super(apiAppCode,
			  dbGuiceModule,
			  searchGuiceModule,
			  otherModules);
	}
	public BeanImplementedPersistenceServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
														   final DBGuiceModuleBase dbGuiceModule,
														   final SearchGuiceModuleBase searchGuiceModule,
														   final Module... otherModules) {
		super(apiAppCode,
			  dbGuiceModule,
			  searchGuiceModule,
			  otherModules != null ? Arrays.asList(otherModules) : null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Avoid multiple bindings 
	 */
	private boolean CRUD_OPERATION_ERROR_LISTENER_BINDED = false;
	private boolean XMLPROPERTIES_FOR_PERSISTENCE_SET = false;
	private boolean XMLPROPERTIES_FOR_SEARCH_SET = false;
	
	@Override 
	protected void _configure(final Binder binder) {
		log.warn("\tBootstraping services from: {} for {}",this.getClass().getName(),_coreAppCode);
		
		final Binder theBinder = binder;
		
		// [1]: Bind XMLProperties for persistence and search
		if (!XMLPROPERTIES_FOR_PERSISTENCE_SET && this.isModuleInstalled(DBGuiceModuleBase.class)) {
			String persistencePropertiesBindName = "persistence";
			//log.warn("...binded persitence properties as {}",persistencePropertiesBindName);
			theBinder.bind(XMLPropertiesForAppComponent.class)
				  	 .annotatedWith(new XMLPropertiesComponentImpl(persistencePropertiesBindName))
				  	 .toProvider(new XMLPropertiesForDBPersistenceProvider(_coreAppCode,_coreAppComponent))
				  	 .in(Singleton.class);
			XMLPROPERTIES_FOR_PERSISTENCE_SET = true;
		}
		if (!XMLPROPERTIES_FOR_SEARCH_SET && this.isModuleInstalled(SearchGuiceModuleBase.class)) {
			String searchPersistencePropertiesBindName = "searchpersistence";
			//log.warn("...binded search persistence properties as {}",searchPersistencePropertiesBindName);
			theBinder.bind(XMLPropertiesForAppComponent.class)
				  	 .annotatedWith(new XMLPropertiesComponentImpl(searchPersistencePropertiesBindName))
				  	 .toProvider(new XMLPropertiesForSearchPersistenceProvider(_coreAppCode,_coreAppComponent))
				  	 .in(Singleton.class);
			XMLPROPERTIES_FOR_SEARCH_SET = true;
		}	
		
		
		// [2]: Bind event listeners 
		// ==================================================
		// Event Bus & Background jobs
		if (this instanceof ServicesBootstrapGuiceModuleBindsCRUDEventListeners) {
			
			// Get from the properties the way CRUD events are to be consumed: synchronously or asynchronously
			ExecutionMode execMode = this.servicesProperties()
												.propertyAt("services/crudEventsHandling/@mode")
													.asEnumElement(ExecutionMode.class);
			if (execMode == null) {
				String servicesComp = super._coreAppCode + ".services";
				log.warn("CRUD Events Handling config could NOT be found at {}.{}.properties.xml, please ensure that the {}.{}.properties.xml" +
						 "contains a 'crudEventsHandling' section; meanwhile SYNC event handling is assumed",
						 servicesComp,servicesComp);
				execMode = ExecutionMode.SYNC;
			}
			// The EventBus needs an ExecutorService (a thread pool) to manage events in the background
			ExecutorServiceManager execServiceManager = null;
			if (execMode == ExecutionMode.ASYNC) {
				int numberOfBackgroundThreads = this.servicesProperties()
														.propertyAt("services/crudEventsHandling/numberOfThreadsInPool")
																.asInteger(1); 	// single threaded by default
				execServiceManager = new ExecutorServiceManagerProvider(numberOfBackgroundThreads).get();
			} 
			
			// do de bindings
			theBinder.bind(EventBus.class)
				 	 .toProvider(new EventBusProvider(execMode,execServiceManager))
				 	 .in(Singleton.class);
			
			
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
