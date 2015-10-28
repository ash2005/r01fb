package r01f.internal;

import java.util.Collection;

import javax.inject.Singleton;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.ExecutorServiceManager;
import r01f.events.PersistenceOperationEventListeners.PersistenceOperationErrorEventListener;
import r01f.events.PersistenceOperationEventListeners.PersistenceOperationOKEventListener;
import r01f.events.crud.CRUDOperationErrorEventListener;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.inject.HasMoreBindings;
import r01f.inject.Matchers;
import r01f.persistence.internal.DBGuiceModuleBase;
import r01f.persistence.internal.SearchGuiceModuleBase;
import r01f.persistence.jobs.EventBusProvider;
import r01f.persistence.jobs.ExecutorServiceManagerProvider;
import r01f.services.core.BeanImplementedServicesCoreGuiceModuleBase;
import r01f.types.ExecutionMode;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLPropertiesComponentImpl;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.common.eventbus.EventBus;
import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;

/**
 * Mappings internal to services core implementation
 * IMPORTANT!!!!
 * =============
 * If this type is refactored and move to another package, it's VERY IMPORTANT to 
 * change the ServicesCoreBootstrap _findCoreGuiceModuleOrNull() method!!!!!
 */
@Slf4j
@EqualsAndHashCode(callSuper=true)				// This is important for guice modules
public abstract class ServicesBootstrapGuiceModuleBase
              extends BeanImplementedServicesCoreGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
//	private final AppCode _apiAppCode;
//	private final AppCode _coreAppCode;
	private final DBGuiceModuleBase _dbGuiceModule;
	private final SearchGuiceModuleBase _searchGuiceModule;
	private final Module[] _otherModules;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app component
	 * In this case, the app is composed of one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].services.properties.xml or [appCode].[appComponent].persistence.properties.xml 
	 * @param apiAppCode
	 * @param coreAppCode
	 * @param coreAppComponent
	 * @param otherModules
	 */
	public ServicesBootstrapGuiceModuleBase(final AppCode apiAppCode,
											final AppCode coreAppCode,final AppComponent coreAppComponent,
											final Module... otherModules) {
		this(apiAppCode,
		     coreAppCode,coreAppComponent,
		     (DBGuiceModuleBase)null,		// db
		     (SearchGuiceModuleBase)null,	// search
		     otherModules);
	}
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app component
	 * In this case, the app is composed of one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].services.properties.xml or [appCode].[appComponent].persistence.properties.xml 
	 * @param apiAppCode
	 * @param coreAppCode
	 * @param coreAppComponent
	 * @param dbGuiceModule
	 * @param otherModules
	 */
	public ServicesBootstrapGuiceModuleBase(final AppCode apiAppCode,
											final AppCode coreAppCode,final AppComponent coreAppComponent,
											final DBGuiceModuleBase dbGuiceModule,
											final Module... otherModules) {
		this(apiAppCode,
			 coreAppCode,coreAppComponent,
			 dbGuiceModule,
			 null,					// search
			 otherModules);
	}
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app component
	 * In this case, the app is composed of one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].services.properties.xml or [appCode].[appComponent].persistence.properties.xml 
	 * @param apiAppCode
	 * @param coreAppCode
	 * @param coreAppComponent
	 * @param searchGuiceModule
	 * @param otherModules
	 */
	public ServicesBootstrapGuiceModuleBase(final AppCode apiAppCode,
											final AppCode coreAppCode,final AppComponent coreAppComponent,
											final SearchGuiceModuleBase searchGuiceModule,
											final Module... otherModules) {
		this(apiAppCode,
			 coreAppCode,coreAppComponent,
			 null,
			 searchGuiceModule,
			 otherModules);
	}
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app component
	 * In this case, the app is composed of one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].services.properties.xml or [appCode].[appComponent].persistence.properties.xml 
	 * @param apiAppCode
	 * @param coreAppCode
	 * @param coreAppComponent
	 * @param searchGuiceModule
	 * @param otherModules
	 */
	public ServicesBootstrapGuiceModuleBase(final AppCode apiAppCode,
											final AppCode coreAppCode,final AppComponent coreAppComponent,
											final SearchGuiceModuleBase searchGuiceModule,
											final Collection<Module> otherModules) {
		this(apiAppCode,
			 coreAppCode,coreAppComponent,
			 null,
			 searchGuiceModule,
			 otherModules);
	}
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app component
	 * In this case, the app is composed of one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].services.properties.xml or [appCode].[appComponent].persistence.properties.xml 
	 * @param apiAppCode
	 * @param coreAppCode
	 * @param coreAppComponent
	 * @param dbGuiceModule
	 * @param searchGuiceModule
	 * @param indexManagementModule
	 * @param otherModules
	 */
	public ServicesBootstrapGuiceModuleBase(final AppCode apiAppCode,
											final AppCode coreAppCode,final AppComponent coreAppComponent,
											final DBGuiceModuleBase dbGuiceModule,
											final SearchGuiceModuleBase searchGuiceModule,
											final Collection<Module> otherModules) {
		super(apiAppCode,
			  coreAppCode,coreAppComponent);
		_dbGuiceModule = dbGuiceModule;
		_searchGuiceModule = searchGuiceModule;
		_otherModules = otherModules != null ? otherModules.toArray(new Module[otherModules.size()]) : null;
	}
	/**
	 * Constructor to be used when it's the bean managed services bootstrap guice module for an app component
	 * In this case, the app is composed of one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].services.properties.xml or [appCode].[appComponent].persistence.properties.xml 
	 * @param apiAppCode
	 * @param coreAppCode
	 * @param coreAppComponent
	 * @param dbGuiceModule
	 * @param searchGuiceModule
	 * @param indexManagementModule
	 * @param otherModules
	 */
	public ServicesBootstrapGuiceModuleBase(final AppCode apiAppCode,
											final AppCode coreAppCode,final AppComponent coreAppComponent,
											final DBGuiceModuleBase dbGuiceModule,
											final SearchGuiceModuleBase searchGuiceModule,
											final Module... otherModules) {
		super(apiAppCode,
			  coreAppCode,coreAppComponent);
		_dbGuiceModule = dbGuiceModule;
		_searchGuiceModule = searchGuiceModule;
		_otherModules = otherModules;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * If many DBGuiceModules are binded, avoid multiple JPA Persistence manager binding
	 */
	private static boolean CRUD_OPERATION_ERROR_LISTENER_BINDED = false;
	
	@Override 
	protected void _configure(final Binder binder) {
		log.warn("\tBootstraping services from: {} for {}",this.getClass().getName(),_coreAppCode);
		
		// Bind XMLProperties
		//		Avoid properties component providers being binded twice if two ServicesBootstrapGuiceModuleBase extending
		//		types are binded (ie when both DBPersistenceServicesBootstrapGuiceModule and SearchIndexServicesBootstrapGuiceModule
		//							 are binded by the same binder)
		if (!XMLPROPERTIES_FOR_SERVICES_SET) {				
			binder.bind(XMLPropertiesForAppComponent.class)
				  .annotatedWith(new XMLPropertiesComponentImpl("services"))
				  .toProvider(new XMLPropertiesForServicesProvider(_coreAppCode,_coreAppComponent))
				  .in(Singleton.class);
			XMLPROPERTIES_FOR_SERVICES_SET = true;
		}
		if (!XMLPROPERTIES_FOR_PERSISTENCE_SET) {
			binder.bind(XMLPropertiesForAppComponent.class)
				  .annotatedWith(new XMLPropertiesComponentImpl("persistence"))
				  .toProvider(new XMLPropertiesForDBPersistenceProvider(_coreAppCode,_coreAppComponent))
				  .in(Singleton.class);
			XMLPROPERTIES_FOR_PERSISTENCE_SET = true;
		}		
		if (!XMLPROPERTIES_FOR_SEARCH_SET) {
			binder.bind(XMLPropertiesForAppComponent.class)
				  .annotatedWith(new XMLPropertiesComponentImpl("searchpersistence"))
				  .toProvider(new XMLPropertiesForSearchPersistenceProvider(_coreAppCode,_coreAppComponent))
				  .in(Singleton.class);
			XMLPROPERTIES_FOR_SEARCH_SET = true;
		}
		
		
		// [1]: Install Modules 
		// ==================================================
		// Search Engine
		if (_searchGuiceModule != null) {
			log.warn("\t\t-Search module for {}",_coreAppCode);
			binder.install(_searchGuiceModule);
		} else {
			log.warn("\t\t-NO search guice module was provided for {}",_coreAppCode);
		}		
		// DB
		if (_dbGuiceModule != null) {
			log.warn("\t\t-DB module for {}",_coreAppCode);
			binder.install(_dbGuiceModule);
		} else {
			log.warn("\t\t-NO DB guice module was provided for {}",_coreAppCode);
		}	
		// Other modules
		if (CollectionUtils.hasData(_otherModules)) {
			log.warn("\t\t-Other modules for {}",_coreAppCode);
			for (Module mod : _otherModules) {
				log.warn("\t\t\t-{}",mod.getClass().getName());
				binder.install(mod);
			}
		}
		
		// Other bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(binder);
		}
		
		// [2]: Bind event listeners 
		// ==================================================
		// Event Bus & Background jobs
		if (this instanceof ServicesBootstrapGuiceModuleBindsCRUDEventListeners) {
			binder.bind(EventBus.class)
				  .toProvider(EventBusProvider.class)
				  .in(Singleton.class);
			
			// The EventBus needs an ExecutorService (a thread pool) to manage events in the background
			if (ExecutionMode.ASYNC == this.servicesProperties()
												.propertyAt("services/crudEventsHandling/@mode")
													.asEnumElement(ExecutionMode.class)) {
				binder.bind(ExecutorServiceManager.class)
					  .toProvider(ExecutorServiceManagerProvider.class)
					  .in(Singleton.class);
			} 
			
			// Automatic registering of event listeners to the event bus avoiding the
			// manual registering of every listener; this simply listen for guice's binding events;
			// when an event listener gets binded, it's is automatically registered at the event bus
			// 		Listen to injection of CRUDOperationOKEventListener & CRUDOperationNOKEventListener subtypes (indexers are CRUD events listeners)
			// 		(when indexers are being injected)
			EventBusSubscriberTypeListener typeListener = new EventBusSubscriberTypeListener(binder.getProvider(EventBus.class));	// inject a Provider to get dependencies injected!!!
			binder.bindListener(Matchers.subclassesOf(PersistenceOperationOKEventListener.class,
													  PersistenceOperationErrorEventListener.class),
							    typeListener);	// registers the event listeners at the EventBus
			
			// These fires the creation of event listeners and thus them being registered at the event bus
			// by means of the EventBusSubscriberTypeListener bindListener (see below)
			if (!CRUD_OPERATION_ERROR_LISTENER_BINDED) {
				binder.bind(CRUDOperationErrorEventListener.class)
					  .toInstance(new CRUDOperationErrorEventListener());				// CRUDOperationNOKEvent for EVERY model object
				CRUD_OPERATION_ERROR_LISTENER_BINDED = true;
			}
			
			// Bind every listener
			((ServicesBootstrapGuiceModuleBindsCRUDEventListeners)this).bindCRUDEventListeners(binder);
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
