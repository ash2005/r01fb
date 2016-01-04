package r01f.services.shf;

import javax.inject.Singleton;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;

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
	private final boolean _mock;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SignatureServiceGuiceModule(final AppCode appCode,final AppComponent appComponent,
									   final String propsXPath) {
		this(appCode,appComponent,
			 propsXPath,
			 false);	// not mock
	}
	public SignatureServiceGuiceModule(final AppCode appCode,final AppComponent appComponent,
									   final String propsXPath,
									   final boolean mock) {
		_appCode = appCode;
		_appComponent = appComponent;
		_propsXPath = propsXPath;
		_mock = mock;
	}
	public SignatureServiceGuiceModule(final AppAndComponent appAndComponent,
									   final String propsXPath) {
		this(appAndComponent,
			 propsXPath,
			 false);	// not mock
	}
	public SignatureServiceGuiceModule(final AppAndComponent appAndComponent,
									   final String propsXPath,
									   final boolean mock) {
		_appCode = appAndComponent.getAppCode();
		_appComponent = appAndComponent.getAppComponent();
		_propsXPath = propsXPath;
		_mock = mock;
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
		if (!_mock) {
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
