package r01f.services.shf;

import r01f.guids.CommonOIDs.AppCode;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Encapsulates signature services
 * 
 * To build a {@link SignatureServiceImpl} instance the {@link XMLPropertiesForAppComponent} for SHF and optionally PIF -if a file signature is to be done- are needed
 * ... those can be built by hand but the normal usage is to have them binded using guice:
 * <pre class='brush:java'>
 *	    public static void main(String[] args) {
 *	    	Injector injector = Guice.createInjector(new SignatureServiceGuiceModule(AppCode.forId("xxx"),
 *																					 AppComponent.forId("myComp"),
 *																					 "testSignature",
 *													 new XMLPropertiesGuiceModule());
 *      
 *	    	SignatureService pifService = injector.getInstance(SignatureService.class);
 *	    	pifService.getFile(path);
 *	    }
 * </pre>
 * ... or without using the signatureServiceGuiceModule
 * <pre class='brush:java'>
 * 		// create a provider method for the XMLProperties file that contains the sign properties
 *		@Provides @XMLPropertiesComponent("notifier")
 *		XMLPropertiesForAppComponent provideXMLPropertiesForServices(final XMLProperties props) {
 *			XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_appCode),
 *																								 AppComponent.forId("notifier"));
 *			return outPropsForComponent;
 *		}
 *		// create a provider method for the signatureService
 *		@Provides @Singleton	// provides a single instance of the sign service
 *		signatureService _providesignatureService(@XMLPropertiesComponent("notifier") final XMLPropertiesForAppComponent props) {
 *			// Provide a new sign service api data using the provider
 *			signatureServiceApiDataProvider signApiServiceProvider = new signatureServiceApiDataProvider(_appCode,
 *																										props,"notifier");
 *			// Using the sign service api data create the signatureService object
 *			signatureService outsignatureService = new signatureService(signApiServiceProvider.get());
 *			return outsignatureService;
 *		}
 * </pre>
 * 
 * Sample usage in a not injected app:
 * <pre class='brush:java'>
 *		SignatureServiceApiDataProvider signServiceApiProvider = new SignatureServiceApiDataProvider(signProps);		// props must be loaded by hand
 *		// ... only if file signature is needed
 *		PifServiceApiDataProvider pifServiceApiProvider = new PifServiceApiDataProvider(pifProps);						// props must be loaded by hand		
 *
 *		SignatureService signService = new SignatureService(signServiceApiProvider.get(),
 *															pifServiceApiProvider.get());
 *	    signService.requiredBy(AppCode.of("myApp"))
 *				   .createXAdESSignatureOf("Sign this!!!");
 * </pre>
 * 
 * For this provider to work, a properties file with the following config MUST be provided:
 * <pre class='xml'>
 * 		<signatureService>
 *			<wsURL>http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/X43FNSHF2?WSDL</wsURL>
 *			<certificateId>0035</certificateId>
 *		</signatureService>
 *		... any other properties...
 *		<xlnets loginAppCode='theAppCode' token='httpProvided'>	<!-- token=file/httpProvided/loginApp -->
 *			<sessionToken>
 *				if token=file: 			...path to a mock xlnets token (use https://xlnets.servicios.jakina.ejiedes.net/xlnets/servicios.htm to generate one)
 *				if token=httpProvided:  ...url to the url that provides the token (ie: http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/Y31JanoServiceXlnetsTokenCreatorServlet?login_app=appId)
 *				if token=loginApp		...not used 
 *			</sessionToken>
 *		</xlnets>
 * </pre>
 */
public interface SignatureService {
	public SignatureServiceForApp requiredBy(final AppCode appCode);
}
