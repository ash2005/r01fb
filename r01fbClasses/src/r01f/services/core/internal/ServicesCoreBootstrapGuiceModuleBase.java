package r01f.services.core.internal;

import java.util.Collection;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.inject.HasMoreBindings;
import r01f.reflection.ReflectionUtils;
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
//  
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
	 * Properties for core
	 */
	protected final XMLPropertiesForAppComponent _coreProps;
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULES
//		 a collection of the installed module types (ie dbmodule, search module, notification module, etc)
//		 (it's used to detect whether a certain guice module type is present)
/////////////////////////////////////////////////////////////////////////////////////////
	protected final Collection<Module> _installedModules;

	protected final Collection<Class<? extends Module>> _installedModuleTypes;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode) {
		_apiAppCode = apiAppCode;
		
		// get tye appCode from the bootstrap module type's package and @ServicesCore annotation
		_coreAppCode = ServicesPackages.appCodeFromCoreBootstrapModuleType(this.getClass());						// the appCode is extracted from the package
		_coreAppComponent = ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(this.getClass());		// the component is extracted from the @ServiceCore annotation
		_coreProps = XMLProperties.createForApp(_coreAppCode)
			  						  .notUsingCache()
			  						  .forComponent(_coreAppComponent.asString() + ".services");
		
		// Crete the list of installed modules
		_installedModules = Lists.newArrayList();
		_installedModuleTypes = Lists.newArrayList();
	}
	protected ServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
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
	protected ServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
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
	protected ServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
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
	protected ServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
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
	protected ServicesCoreBootstrapGuiceModuleBase(final AppCode apiAppCode,
												   final Module m1,final Module m2,final Module m3,final Module m4,
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
		if (m4 != null) {
			_installedModules.add(m4);
			_installedModuleTypes.add(m4.getClass());
		}
		if (CollectionUtils.hasData(otherModules)) {		 
		     for (Module m : otherModules) {
		    	 _installedModules.add(m);
		    	 _installedModuleTypes.add(m.getClass());
		     }
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MODULE INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean XMLPROPERTIES_FOR_SERVICES_SET = false;
	
	@Override
	public void configure(final Binder binder) {
		log.warn("START____________ {}.{} CORE Bean Bootstraping with {}_____________________________",
				 _coreAppCode,_coreAppComponent,this.getClass());
		
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
			_bindXMLPropertiesComponentProviderFor("services",
												   theBinder);
			XMLPROPERTIES_FOR_SERVICES_SET = true;
		}
		
//		// [3]: Give chance to subtypes to do particular bindings
//		_configure(theBinder);
		
		log.warn("END_______________ {}.{} CORE Bean Bootstraping with {}_____________________________",
				 _coreAppCode,_coreAppComponent,this.getClass());
	}
//	/**
//	 * Module configurations: marshaller and other bindings
//	 * @param binder
//	 */
//	protected abstract void _configure(final Binder binder);
	
/////////////////////////////////////////////////////////////////////////////////////////
//  XMLProperties PROVIDERS
/////////////////////////////////////////////////////////////////////////////////////////
	protected void _bindXMLPropertiesComponentProviderFor(final String subComponent,
														  final Binder binder) {
		_doBindXMLPropertiesComponentProviderAs(subComponent,subComponent,
												binder);
		_doBindXMLPropertiesComponentProviderAs(_componentPropertiesIdFor(_coreAppComponent,subComponent),subComponent,
											    binder);	
		log.warn("{}.{}.properties.xml properties are available for injection as @XMLPropertiesComponent(\"{}\") and @XMLPropertiesComponent(\"{}\")",
				 _coreAppCode,_componentPropertiesIdFor(_coreAppComponent,subComponent),
				 subComponent,
				 _componentPropertiesIdFor(_coreAppComponent,subComponent));
	}
	private void _doBindXMLPropertiesComponentProviderAs(final String bindingName,final String subComponent,
														 final Binder binder) {
		binder.bind(XMLPropertiesForAppComponent.class)
			  .annotatedWith(new XMLPropertiesComponentImpl(bindingName))
			  .toProvider(new XMLPropertiesForXProvider(_coreAppCode,_coreAppComponent,subComponent))
			  .in(Singleton.class);
	}

	private static String _componentPropertiesIdFor(final AppComponent appComponent,final String subComponent) {
		String componentId = appComponent != null ? appComponent.asString() + "." + subComponent
												  : subComponent;
		return componentId;
	}
	@RequiredArgsConstructor
	protected static class XMLPropertiesForXProvider 
				implements Provider<XMLPropertiesForAppComponent> {
		@Inject protected XMLProperties _props;
		
				protected final AppCode _coreAppCode;
				protected final AppComponent _coreAppComponent;
				protected final String _subComponent;
				
		@Override
		public XMLPropertiesForAppComponent get() {
			String componentId = ServicesCoreBootstrapGuiceModuleBase._componentPropertiesIdFor(_coreAppComponent,_subComponent);
			XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(_props.forApp(_coreAppCode),
																								 AppComponent.forId(componentId));
			return outPropsForComponent;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ServicesCoreBootstrapGuiceModule
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Collection<Class<? extends Module>> getInstalledModuleTypes() {
		return _installedModuleTypes;
	}
	@Override
	public boolean isModuleInstalled(final Class<? extends Module> modType) {
		if (CollectionUtils.isNullOrEmpty(_installedModuleTypes)) return false;
		
		boolean outInstalled = false;
		for (Class<? extends Module> mType : _installedModuleTypes) {
			if (ReflectionUtils.isSubClassOf(mType,modType)) {
				outInstalled = true;
				break;
			}
		}
		return outInstalled;
	}
}
