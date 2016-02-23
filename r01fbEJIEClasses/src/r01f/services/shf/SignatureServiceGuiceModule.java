package r01f.services.shf;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppAndComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.services.pif.PifServiceApiDataProvider;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesComponent;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * This guice module is to be used when using the SignatureService in a standalone way (ie testing)
 * something like:
 * <pre class='brush:java'>
 *		Injector injector = Guice.createInjector(new SignatureServiceGuiceModule());
 *	
 *		SignatureService signService = injector.getInstance(SignatureService.class);
 *		signService.createXAdESSignature("sign this text");
 * </pre>
 * It's important to bind the XMLPropertiesGuiceModule:
 * <pre class='brush:java'>
 * 		binder.install(new XMLPropertiesGuiceModule());
 * </pre>
 */
@Slf4j
public class SignatureServiceGuiceModule 
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
	private final String _propsXPath;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SignatureServiceGuiceModule(final AppCode appCode,final AppComponent appComponent,
									   final String propsXPath) {
		_appCode = appCode;
		_appComponent = appComponent;
		_propsXPath = propsXPath;
	}
	public SignatureServiceGuiceModule(final AppAndComponent appAndComponent,
									   final String propsXPath) {
		_appCode = appAndComponent.getAppCode();
		_appComponent = appAndComponent.getAppComponent();
		_propsXPath = propsXPath;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void configure(final Binder binder) {
		// the service is created at the _provideSignatureService() provider method below
	}
	/**
	 * Alternative to using a provider
     * binder.bind(XMLPropertiesForAppComponent.class)
     * 	     .annotatedWith(new XMLPropertiesComponent() {		// see [Binding annotations with attributes] at https://github.com/google/guice/wiki/BindingAnnotations
     * 								@Override
     * 								public Class<? extends Annotation> annotationType() {
     * 									return XMLPropertiesComponent.class;
     * 								}
     * 								@Override
     * 								public String value() {
     * 									return "signature";
     * 								}
     * 	  				 })
     * 	     .toProvider(new Provider<XMLPropertiesForAppComponent>() {
     * 								@Override
     * 								public XMLPropertiesForAppComponent get() {
     * 									return XXXServicesBootstrapGuiceModule.this.servicesProperties();
     * 								}
     * 	  			  	 });
	 */
	@Provides @XMLPropertiesComponent("signature")
	XMLPropertiesForAppComponent provideXMLPropertiesForSignatureService(final XMLProperties props) {
		XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_appCode),
																							 _appComponent);
		return outPropsForComponent;
	}
	/**
	 * Provides a {@link SignatureService} implementation
	 * @param props
	 * @return
	 */
	@Provides @Singleton	// beware the service is a singleton
	SignatureService _provideSignatureService(@XMLPropertiesComponent("signature") final XMLPropertiesForAppComponent props) {
		boolean mock = props.propertyAt(_propsXPath + "/signatureService/@mock")
							.asBoolean(false);
		if (mock) log.warn("Using a MOCK signature service. Change the {} property to enable real SHF impl",
						   _propsXPath + "/signatureService/@mock");
		if (!mock) {
			// Provide a new signature service api data and pif service api data using their providers
			SignatureServiceApiDataProvider signatureApiDataServiceProvider = new SignatureServiceApiDataProvider(_appCode,
																												  props,_propsXPath);
			PifServiceApiDataProvider pifApiDataServiceProvider = new PifServiceApiDataProvider(_appCode,
																								props,_propsXPath);
			// Using the signature service api data create the SignatureService object
			SignatureService outSignatureService = new SignatureServiceImpl(signatureApiDataServiceProvider.get(),
																			pifApiDataServiceProvider.get());
			return outSignatureService;
		}
		// mock impl
		SignatureService outSignatureService = new SignatureServiceMockImpl();
		return outSignatureService;
	}
}
