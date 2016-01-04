package r01f.services.pif;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import lombok.RequiredArgsConstructor;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

@RequiredArgsConstructor
public class PifServiceGuiceModule 
  implements Module {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
// 	The signature properties can be located at any XMLProperties file (the <signature>...</signature>
// 	can be in any component XML file with other properties (there does NOT exists an exclusive
// 	XMLProperties file for signature, the signature config section <signature>...</signature> is embeded
// 	in any other XMLProperties file)
//
// 	BUT the signature service provider (see below) expect a XMLProperties component
// 	named 'signature' so this component MUST be created here
/////////////////////////////////////////////////////////////////////////////////////////
	private final AppCode _appCode;
	private final AppComponent _appComponent;
	private final String _signaturePropsXPath;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		// Install the XMLProperties module
		//binder.install(new XMLPropertiesGuiceModule());
		
		// the service is created at the _providePifService() provider method below
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
     * 									return "pif";
     * 								}
     * 	  				 })
     * 	  .toProvider(new Provider<XMLPropertiesForAppComponent>() {
     * 						@Override
     * 						public XMLPropertiesForAppComponent get() {
     * 							return XXXServicesBootstrapGuiceModule.this.servicesProperties();
     * 						}
     * 	  			  });
	 */
	@Provides @XMLPropertiesComponent("pif")
	XMLPropertiesForAppComponent provideXMLPropertiesForPifService(final XMLProperties props) {
		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_appCode),
																							 _appComponent);
		return outPropsForComponent;
	}	
	/**
	 * Provides a {@link PifService} implementation
	 * @param props
	 * @return
	 */
	@Provides @Singleton	// beware the service is a singleton
	PifService _providePifService(@XMLPropertiesComponent("pif") final XMLPropertiesForAppComponent props) {
		// Provide a new pif service api data using the provider
		PifServiceApiDataProvider pifApiServiceProvider = new PifServiceApiDataProvider(_appCode,
																						props,_signaturePropsXPath);
		
		// Using the pif service api data create the PifService object
		PifService outPifService = new PifService(pifApiServiceProvider.get());
		return outPifService;
	}
}
