package r01f.services.core.internal;

import java.util.Collection;

import javax.inject.Singleton;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.PrivateBinder;
import com.google.inject.Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.inject.HasMoreBindings;
import r01f.reflection.ReflectionUtils;
import r01f.services.ServiceIDs.CoreAppCode;
import r01f.services.ServiceIDs.CoreModule;
import r01f.services.ServicesPackages;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponentImpl;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@Slf4j
abstract class ServicesCoreBootstrapGuiceModuleBase
    implements ServicesCoreBootstrapGuiceModule {
/////////////////////////////////////////////////////////////////////////////////////////
// 	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 *  MODULES
	 *		 a collection of the installed module types (ie dbmodule, search module, notification module, etc)
	 *		 (it's used to detect whether a certain guice module type is present)
	 */
	protected final Collection<Module> _installedModules;
	/**
	 * Core services properties (to be used while binding)
	 */
	protected final XMLPropertiesForAppComponent _servicesCoreProps;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreBootstrapGuiceModuleBase() {
		// Create the list of installed modules
		_installedModules = Lists.newArrayList();
		
		// instance a properties to be used here
		final CoreAppCode coreAppCode = ServicesPackages.appCodeFromCoreBootstrapModuleType(this.getClass());				// the appCode is extracted from the package
		final CoreModule coreModule = ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(this.getClass());		// the component is extracted from the @ServiceCore annotation
		_servicesCoreProps = XMLProperties.createForAppComponent(coreAppCode.asAppCode(),
																 AppComponent.forId(_componentPropertiesIdFor(coreModule,"services")))	// beware!!
							 			  .notUsingCache();
	}
	protected ServicesCoreBootstrapGuiceModuleBase(final Collection<? extends Module> modulesToInstall) {
		this();
		
		// modules to install
		if (CollectionUtils.hasData(modulesToInstall)) {
			for (Module m : modulesToInstall) {
				_installedModules.add(m);
			}
		}
	}
	protected ServicesCoreBootstrapGuiceModuleBase(final Module m1,
												   final Collection<? extends Module> otherModules) {
		this();
		
		// modules to install
		if (m1 != null) {
			_installedModules.add(m1);
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		     }
		}
	}
	protected ServicesCoreBootstrapGuiceModuleBase(final Module m1,final Module m2,
												   final Collection<? extends Module> otherModules) {
		this();
		
		// modules to install
		if (m1 != null) {
			_installedModules.add(m1);
		}
		if (m2 != null) {
			_installedModules.add(m2);
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		     }
		}
	}
	protected ServicesCoreBootstrapGuiceModuleBase(final Module m1,final Module m2,final Module m3,
												   final Collection<? extends Module> otherModules) {
		this();
		
		// modules to install
		if (m1 != null) {
			_installedModules.add(m1);
		}
		if (m2 != null) {
			_installedModules.add(m2);
		}
		if (m3 != null) {
			_installedModules.add(m3);
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		     }
		}
	}
	protected ServicesCoreBootstrapGuiceModuleBase(final Module m1,final Module m2,final Module m3,final Module m4,
												   final Collection<? extends Module> otherModules) {
		this();
		
		// modules to install
		if (m1 != null) {
			_installedModules.add(m1);
		}
		if (m2 != null) {
			_installedModules.add(m2);
		}
		if (m3 != null) {
			_installedModules.add(m3);
		}
		if (m4 != null) {
			_installedModules.add(m4);
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		     }
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Avoid multiple bindings 
	 */
	private boolean XMLPROPERTIES_FOR_SERVICES_SET = false;
	
	@Override
	public void configure(final Binder binder) {
		final CoreAppCode coreAppCode = ServicesPackages.appCodeFromCoreBootstrapModuleType(this.getClass());				// the appCode is extracted from the package
		final CoreModule coreModule = ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(this.getClass());		// the component is extracted from the @ServiceCore annotation
		
		log.warn("____________START [{}.{}] CORE Bean Bootstraping with {}",
				 coreAppCode,coreModule,this.getClass());
		
		Binder theBinder = binder; 
		
		// [1]: Bind services properties
		if (!XMLPROPERTIES_FOR_SERVICES_SET) {
			_bindXMLPropertiesComponentProviderFor(coreAppCode,coreModule,
												  "services",
												   theBinder);
			XMLPROPERTIES_FOR_SERVICES_SET = true;
		}
		
		// [2]: Install Modules & bind specific properties
		if (CollectionUtils.hasData(_installedModules)) {
			for (Module m : _installedModules) {
				log.warn("\t\t-{} > install {} guice module",coreAppCode,m.getClass());
				
				// Install the module
				theBinder.install(m);
			}
		}
		
		
		// Other bindings
		if (this instanceof HasMoreBindings) {
			((HasMoreBindings)this).configureMoreBindings(theBinder);
		}
		
		log.warn("______________END [{}.{}] CORE Bean Bootstraping with {}",
				 coreAppCode,coreModule,this.getClass());
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  XMLProperties PROVIDERS
/////////////////////////////////////////////////////////////////////////////////////////
	protected static void _bindXMLPropertiesComponentProviderFor(final CoreAppCode coreAppCode,final CoreModule coreModule,
														  		 final String subComponent,
														  		 final Binder binder) {
		_doBindXMLPropertiesComponentProviderAs(coreAppCode,coreModule,
												subComponent,subComponent,
												binder,false);	// bind BUT do NOT expose
		_doBindXMLPropertiesComponentProviderAs(coreAppCode,coreModule,
												_componentPropertiesIdFor(coreModule,subComponent),subComponent,
											    binder,true);	// bind and expose
	}
	private static void _doBindXMLPropertiesComponentProviderAs(final CoreAppCode coreAppCode,final CoreModule coreAppComponent,
														 		final String bindingName,final String subComponent,
														 		final Binder binder,final boolean expose) {
		log.debug("{}.{}.properties.xml properties are available for injection as a {} object annotated with @XMLPropertiesComponent(\"{}\") INTERNALLY and @XMLPropertiesComponent(\"{}\") EXTERNALLY",
				  coreAppCode,_componentPropertiesIdFor(coreAppComponent,subComponent),
				  XMLPropertiesForAppComponent.class.getSimpleName(),
				  subComponent,
				  _componentPropertiesIdFor(coreAppComponent,subComponent));
		
		// do the binding
		binder.bind(XMLPropertiesForAppComponent.class)
			  .annotatedWith(new XMLPropertiesComponentImpl(bindingName))
			  .toProvider(new XMLPropertiesForXProvider(coreAppCode,coreAppComponent,subComponent))
			  .in(Singleton.class);
		
		// Expose xml properties binding
		if (expose && (binder instanceof PrivateBinder)) {
			log.warn("{}.{}.properties.xml properties are available for injection as a {} object annotated with @XMLPropertiesComponent(\"{}\")",
					 coreAppCode,_componentPropertiesIdFor(coreAppComponent,subComponent),
					 XMLPropertiesForAppComponent.class.getSimpleName(),
					 _componentPropertiesIdFor(coreAppComponent,subComponent));
			PrivateBinder pb = (PrivateBinder)binder;
			pb.expose(Key.get(XMLPropertiesForAppComponent.class,new XMLPropertiesComponentImpl(bindingName)));
		}
	}

	protected static String _componentPropertiesIdFor(final CoreModule coreModule,final String subComponent) {
		String componentId = coreModule != null ? coreModule.asString() + "." + subComponent
												: subComponent;
		return componentId;
	}
	@RequiredArgsConstructor
	protected static class XMLPropertiesForXProvider 
				implements Provider<XMLPropertiesForAppComponent> {
		
		@Inject protected XMLProperties _props;
		
				protected final CoreAppCode _coreAppCode;
				protected final CoreModule _coreModule;
				protected final String _subComponent;
				
		@Override
		public XMLPropertiesForAppComponent get() {
			String componentId = ServicesCoreBootstrapGuiceModuleBase._componentPropertiesIdFor(_coreModule,_subComponent);
			XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(_props.forApp(_coreAppCode.asAppCode()),
																								 AppComponent.forId(componentId));
			return outPropsForComponent;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ServicesCoreBootstrapGuiceModule
/////////////////////////////////////////////////////////////////////////////////////////
//	@Override
	public Collection<Class<? extends Module>> getInstalledModuleTypes() {
		return FluentIterable.from(_installedModules)
							 .transform(new Function<Module,Class<? extends Module>>() {
												@Override
												public Class<? extends Module> apply(final Module mod) {
													return mod.getClass();
												}
							 			})
							 .toList();
	}
//	@Override
	public boolean isModuleInstalled(final Class<? extends Module> modType) {
		if (CollectionUtils.isNullOrEmpty(_installedModules)) return false;
		
		return FluentIterable.from(this.getInstalledModuleTypes())
							 .anyMatch(new Predicate<Class<? extends Module>>() {
											@Override
											public boolean apply(final Class<? extends Module> mT) {
												return ReflectionUtils.isSubClassOf(mT,modType);
											}
							 		   });
	}
}
