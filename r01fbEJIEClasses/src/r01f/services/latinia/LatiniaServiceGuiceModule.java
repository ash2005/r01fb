package r01f.services.latinia;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.name.Names;

import lombok.RequiredArgsConstructor;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.marshalling.Marshaller;
import r01f.marshalling.Marshaller.MarshallerMappingsSearch;
import r01f.marshalling.simple.SimpleMarshallerBuilder;
import r01f.model.latinia.LatiniaObject;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@RequiredArgsConstructor
public class LatiniaServiceGuiceModule 
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
// 	The latinia properties can be located at any XMLProperties file (the <latinia>...</latinia>
// 	can be in any component XML file with other properties (there does NOT exists an exclusive
// 	XMLProperties file for latinia, the latinia config section <latinia>...</latinia> is embeded
// 	in any other XMLProperties file)
//
// 	BUT the latinia service provider (see below) expect a XMLProperties component
// 	named 'latinia' so this component MUST be created here
/////////////////////////////////////////////////////////////////////////////////////////
	private final AppCode _appCode;
	private final AppComponent _appComponent;
	private final String _latiniaPropsXPath;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		// Install the XMLProperties module
		//binder.install(new XMLPropertiesGuiceModule());
		
		// Bind a latinia objects marshaller instance 
		binder.bind(Marshaller.class)
			  .annotatedWith(Names.named("latiniaObjsMarshaller"))
			  .toInstance(SimpleMarshallerBuilder.createForPackages(MarshallerMappingsSearch.inPackages(LatiniaObject.class.getPackage().getName()))	// persistence objects
												 .getForMultipleUse());
		
		// The latinia service is created using the _provideLatiniaService() provider method
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Alternative to using a provider
     * binder.bind(XMLPropertiesForAppComponent.class)
     * 	  .annotatedWith(new XMLPropertiesComponent() {		// see [Binding annotations with attributes] at https://github.com/google/guice/wiki/BindingAnnotations
     * 								@Override
     * 								public Class<? extends Annotation> annotationType() {
     * 									return XMLPropertiesComponent.class;
     * 								}
     * 								@Override
     * 								public String value() {
     * 									return "latinia";
     * 								}
     * 	  				 })
     * 	  .toProvider(new Provider<XMLPropertiesForAppComponent>() {
     * 						@Override
     * 						public XMLPropertiesForAppComponent get() {
     * 							return XXXServicesBootstrapGuiceModule.this.servicesProperties();
     * 						}
     * 	  			  });
	 */
	@Provides @XMLPropertiesComponent("latinia")
	XMLPropertiesForAppComponent provideXMLPropertiesForLatinia(final XMLProperties props) {
		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_appCode),
																							 _appComponent);
		return outPropsForComponent;
	}	
	/**
	 * Provides a {@link LatiniaService} implementation
	 * @param props
	 * @return
	 */
	@Provides @Singleton	// beware the service is a singleton
	LatiniaService _provideLatiniaService(@XMLPropertiesComponent("latinia") final XMLPropertiesForAppComponent props) {
		// Provide a new latinia service api data using the provider
		LatiniaServiceApiDataProvider latiniaApiServiceProvider = new LatiniaServiceApiDataProvider(_appCode,
																									props,_latiniaPropsXPath);
		// Using the latinia service api data create the LatiniaService object
		LatiniaService outLatiniaService = new LatiniaService(latiniaApiServiceProvider.get());
		return outLatiniaService;
	}
}
