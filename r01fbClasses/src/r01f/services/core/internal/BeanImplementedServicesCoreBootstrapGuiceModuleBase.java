package r01f.services.core.internal;



import java.util.Collection;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.inject.HasMoreBindings;
import r01f.services.ServicesPackages;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponentImpl;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

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
@Accessors(prefix="_")
public abstract class BeanImplementedServicesCoreBootstrapGuiceModuleBase
  		      extends ServicesCoreBootstrapGuiceModuleBase {
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
	 * Core properties
	 */
	protected final XMLPropertiesForApp _coreProps;
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULES
/////////////////////////////////////////////////////////////////////////////////////////
	private final Collection<Module> _installedModules;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private BeanImplementedServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode) {
		_apiAppCode = apiAppCode;
		
		// get tye appCode from the bootstrap module type's package and @ServicesCore annotation
		_coreAppCode = ServicesPackages.appCodeFromCoreBootstrapModuleType(this.getClass());						// the appCode is extracted from the package
		_coreAppComponent = ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(this.getClass());		// the component is extracted from the @ServiceCore annotation
		
		// Create a XMLPropertiesManager for the app
		_coreProps = XMLProperties.createForApp(_coreAppCode)
								  .notUsingCache();
		
		// Crete the list of installed modules
		_installedModules = Lists.newArrayList();
	}
	protected BeanImplementedServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
													  	 		  final Collection<? extends Module> modulesToInstall) {
		this(apiAppCode);
		
		// modules to install
		if (CollectionUtils.hasData(modulesToInstall)) {
			for (Module m : modulesToInstall) {
				_installedModules.add(m);
				_installedModuleTypes.add(m.getClass());		// store the installed module type for later use
			}
		}
	}
	protected BeanImplementedServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
													  	 		  final Module m1,
													  	 		  final Collection<? extends Module> otherModules) {
		this(apiAppCode);
		
		// modules to install
		if (m1 != null) {
			_installedModules.add(m1);
			_installedModuleTypes.add(m1.getClass());
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		    	 _installedModuleTypes.add(m.getClass());
		     }
		}
	}
	protected BeanImplementedServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
													  	 		  final Module m1,final Module m2,
													  	 		  final Collection<? extends Module> otherModules) {
		this(apiAppCode);
		
		// modules to install
		if (m1 != null) {
			_installedModules.add(m1);
			_installedModuleTypes.add(m1.getClass());
		}
		if (m2 != null) {
			_installedModules.add(m2);
			_installedModuleTypes.add(m2.getClass());
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		    	 _installedModuleTypes.add(m.getClass());
		     }
		}
	}
	protected BeanImplementedServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
													  	 		  final Module m1,final Module m2,final Module m3,
													  	 		  final Collection<? extends Module> otherModules) {
		this(apiAppCode);
		
		// modules to install
		if (m1 != null) {
			_installedModules.add(m1);
			_installedModuleTypes.add(m1.getClass());
		}
		if (m2 != null) {
			_installedModules.add(m2);
			_installedModuleTypes.add(m2.getClass());
		}
		if (m3 != null) {
			_installedModules.add(m3);
			_installedModuleTypes.add(m3.getClass());
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		    	 _installedModuleTypes.add(m.getClass());
		     }
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROPERTIES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a services module property
	 * @return a {@link XMLPropertiesForAppComponent} that provides access to the properties
	 */
	public XMLPropertiesForAppComponent servicesProperties() {
		AppComponent appComp = AppComponent.forId(_coreAppComponent.asString() + ".services");
		XMLPropertiesForAppComponent props = _coreProps.forComponent(appComp);
		return props;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean XMLPROPERTIES_FOR_SERVICES_SET = false;
	
	@Override
	public void configure(final Binder binder) {
		log.warn("START____________ {} CORE Bean Bootstraping _____________________________",_coreAppCode.asString().toUpperCase());
		
		Binder theBinder = binder; 
		
		// [1]: Install Modules 
		if (CollectionUtils.hasData(_installedModules)) {
			for (Module m : _installedModules) {
				log.warn("\t\t-{} > install {} guice module",_coreAppCode,m.getClass());
				theBinder.install(m);
			}
		}
		
		
		// Other bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(theBinder);
		}
		
		// [2]: Bind services properties
		if (!XMLPROPERTIES_FOR_SERVICES_SET) {	
			String servicesPropertiesBindName = "services";
			//log.warn("...binded services properties as {}",servicesPropertiesBindName);
			theBinder.bind(XMLPropertiesForAppComponent.class)
				  	 .annotatedWith(new XMLPropertiesComponentImpl(servicesPropertiesBindName))
				  	 .toProvider(new XMLPropertiesForServicesProvider(_coreAppCode,_coreAppComponent))
				  	 .in(Singleton.class);
			XMLPROPERTIES_FOR_SERVICES_SET = true;
		}
		
		// [3]: Give chance to subtypes to do particular bindings
		_configure(theBinder);
		
		log.warn("END_______________ {} CORE Bean Bootstraping _____________________________",_coreAppCode.asString().toUpperCase());
	}
	/**
	 * Module configurations: marshaller and other bindings
	 * @param binder
	 */
	protected abstract void _configure(final Binder binder);
	
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
