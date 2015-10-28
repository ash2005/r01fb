package r01f.services.core;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.services.interfaces.ServiceInterface;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import r01f.xmlproperties.XMLPropertiesGuiceModule;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Creates the core-side bindings for the service interfaces (api)
 * 
 * Usually the core (server) side implements more than one {@link ServiceInterface} and sometime exists the need to access a {@link ServiceInterface} logic
 * from another {@link ServiceInterface}. In such cases in order to avoid the use of the client API (and "leave" the core to return to it through the client)
 * a {@link CoreServicesAggregator} exists at the core (server) side
 * This {@link CoreServicesAggregator} can be injected at core-side to cross-use the {@link ServiceInterface} logic.
 * 
 * If many {@link CoreServicesAggregator} types exists at the core side, they MUST be annotated with a type annotated with {@link CoreServiceAggregatorQualifier}
 * in order to distinguish one another
 */
@Slf4j
public abstract class BeanImplementedServicesCoreGuiceModuleBase 
  		   implements ServicesCoreBootstrapGuiceModule {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * API appCode
	 */
	protected final AppCode _apiAppCode;
	/**
	 * CORE appCode
	 */
	protected final AppCode _coreAppCode;
	/**
	 * CORE appCode component
	 */
	protected final AppComponent _coreAppComponent;
	/**
	 * Api properties
	 */
	protected final XMLPropertiesForApp _apiProps;
	/**
	 * Core properties
	 */
	protected final XMLPropertiesForApp _coreProps;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public BeanImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
													  final AppCode coreAppCode) {
		this(apiAppCode,
		     coreAppCode,null);
	}
	public BeanImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
													  final AppCode coreAppCode,final AppComponent coreAppComponent) {
		_apiAppCode = apiAppCode;
		_coreAppCode = coreAppCode;
		_coreAppComponent = coreAppComponent;
		
		// Create a XMLPropertiesManager for the app
		_apiProps = Guice.createInjector(new XMLPropertiesGuiceModule())
				  	     .getInstance(XMLProperties.class)
				  	     .forApp(_apiAppCode);		
		_coreProps = Guice.createInjector(new XMLPropertiesGuiceModule())
					  	  .getInstance(XMLProperties.class)
					  	  .forApp(_coreAppCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROPERTIES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a services module property
	 * @return a {@link XMLPropertiesForAppComponent} that provides access to the properties
	 */
	public XMLPropertiesForAppComponent servicesProperties() {
		String componentId = _coreAppComponent != null ? _coreAppComponent.asString() + ".services"
													   : "services";
		XMLPropertiesForAppComponent props = _coreProps.forComponent(componentId);
		return props;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////	
	@Override
	public void configure(final Binder binder) {
		log.warn("START____________ {} CORE Bean Bootstraping _____________________________",_coreAppCode.asString().toUpperCase());
		
		// Give chance to subtypes to do particular bindings
		_configure(binder);
		
		log.warn("END_______________ {} CORE Bean Bootstraping _____________________________",_coreAppCode.asString().toUpperCase());
	}
	/**
	 * Module configurations: marshaller and other bindings
	 * @param binder
	 */
	protected abstract void _configure(final Binder binder);
	
/////////////////////////////////////////////////////////////////////////////////////////
//	XMLProperties PROVIDERS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static boolean XMLPROPERTIES_FOR_SERVICES_SET = false;
	protected static boolean XMLPROPERTIES_FOR_PERSISTENCE_SET = false;
	protected static boolean XMLPROPERTIES_FOR_SEARCH_SET = false;
	
//	@Provides @XMLPropertiesComponent("services")			// do not use this approach since sometimes it leads to the provider tried to being binded twice
	XMLPropertiesForAppComponent provideXMLPropertiesForServices(final XMLProperties props) {
		return _doProvideXMLPropertiesForServices(props,
										   		  _coreAppCode,_coreAppComponent);
	}
// 	@Provides @XMLPropertiesComponent("persistence")		// do not use this approach since sometimes it leads to the provider tried to being binded twice
 	XMLPropertiesForAppComponent provideXMLPropertiesForPersistence(final XMLProperties props) {
		return _doProvideXMLPropertiesForPersistence(props,
										   		     _coreAppCode,_coreAppComponent);
 	}
// 	@Provides @XMLPropertiesComponent("searchpersistence")	// do not use this approach since sometimes it leads to the provider tried to being binded twice
 	XMLPropertiesForAppComponent provideXMLPropertiesForSearchPersistence(final XMLProperties props) {
		return _doProvideXMLPropertiesForSearchPersistence(props,
								   		  		 		   _coreAppCode,_coreAppComponent);
 	}
/////////////////////////////////////////////////////////////////////////////////////////
//  XMLProperties PROVIDERS
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor
	protected static abstract class XMLPropertiesForXProviderBase 
				  		 implements Provider<XMLPropertiesForAppComponent> {
		@Inject protected XMLProperties _props;
		
				protected final AppCode _coreAppCode;
				protected final AppComponent _coreAppComponent;
	}
	
	protected static class XMLPropertiesForServicesProvider
				   extends XMLPropertiesForXProviderBase {
		
		
		public XMLPropertiesForServicesProvider(final AppCode coreAppCode,final AppComponent coreAppComponent) {
			super(coreAppCode,coreAppComponent);
		}
		@Override
		public XMLPropertiesForAppComponent get() {
			return _doProvideXMLPropertiesForServices(_props,
											   		  _coreAppCode,_coreAppComponent);
		}
		
	}
	protected static class XMLPropertiesForDBPersistenceProvider
				   extends XMLPropertiesForXProviderBase {
		
		public XMLPropertiesForDBPersistenceProvider(final AppCode coreAppCode,final AppComponent coreAppComponent) {
			super(coreAppCode,coreAppComponent);
		}
		@Override
		public XMLPropertiesForAppComponent get() {
			return _doProvideXMLPropertiesForPersistence(_props,
											   		     _coreAppCode,_coreAppComponent);
		}
 	}
	protected static class XMLPropertiesForSearchPersistenceProvider
				   extends XMLPropertiesForXProviderBase {
		
		public XMLPropertiesForSearchPersistenceProvider(final AppCode coreAppCode,final AppComponent coreAppComponent) {
			super(coreAppCode,coreAppComponent);
		}
		@Override
		public XMLPropertiesForAppComponent get() {
			return _doProvideXMLPropertiesForSearchPersistence(_props,
									   		  		 		   _coreAppCode,_coreAppComponent);
		}
 	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static XMLPropertiesForAppComponent _doProvideXMLPropertiesForServices(final XMLProperties props,
																				   final AppCode coreAppCode,final AppComponent coreAppComponent) {
		String componentId = coreAppComponent != null ? coreAppComponent.asString() + ".services"
													  : "services";
		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(coreAppCode),
																							 AppComponent.forId(componentId));
		return outPropsForComponent;
	}
 	private static XMLPropertiesForAppComponent _doProvideXMLPropertiesForPersistence(final XMLProperties props,
 																				      final AppCode coreAppCode,final AppComponent coreAppComponent) {
		String componentId = coreAppComponent != null ? coreAppComponent.asString() + ".dbpersistence"
													  : "dbpersistence";
 		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(coreAppCode),
 																							 AppComponent.forId(componentId));
 		return outPropsForComponent;
 	}
 	private static XMLPropertiesForAppComponent _doProvideXMLPropertiesForSearchPersistence(final XMLProperties props,
 																						    final AppCode coreAppCode,final AppComponent coreAppComponent) {
		String componentId = coreAppComponent != null ? coreAppComponent.asString() + ".searchpersistence"
													  : "serachpersistence";
 		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(coreAppCode),
 																							 AppComponent.forId(componentId));
 		return outPropsForComponent;
 	}
}
