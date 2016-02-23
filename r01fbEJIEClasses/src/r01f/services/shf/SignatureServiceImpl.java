package r01f.services.shf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;
import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;

import com.ejie.nshf.client.X43FNSHF;
import com.ejie.nshf.client.X43FNSHF_Impl;
import com.ejie.nshf.client.X43FNSHF_PortType;
import com.google.common.io.ByteStreams;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.ejie.model.shf.SignatureRequestOutputData;
import r01f.ejie.model.shf.SignatureVerifyOutputData;
import r01f.exceptions.Throwables;
import r01f.guids.CommonOIDs.AppCode;
import r01f.model.pif.PifFileInfo;
import r01f.services.EJIESoapMessageHandler;
import r01f.services.pif.PifService;
import r01f.services.pif.PifServiceApiDataProvider.PifServiceAPIData;
import r01f.services.shf.SignatureServiceApiDataProvider.SignatureServiceAPIData;
import r01f.types.Factory;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
import x43f.ejie.com.X43FNSHF.Body;
import x43f.ejie.com.X43FNSHF.CreateAdESSignature;
import x43f.ejie.com.X43FNSHF.CreateAdESSignatureLocation;
import x43f.ejie.com.X43FNSHF.CreateAdESSignatureLocationResponse;
import x43f.ejie.com.X43FNSHF.CreateAdESSignatureResponse;
import x43f.ejie.com.X43FNSHF.EjgvDocument;
import x43f.ejie.com.X43FNSHF.EjgvDocumentType;
import x43f.ejie.com.X43FNSHF.Header;
import x43f.ejie.com.X43FNSHF.SignaturePlacementAnonType4;
import x43f.ejie.com.X43FNSHF.SignaturePlacementAnonType5;
import x43f.ejie.com.X43FNSHF.VerifyAdESSignature;
import x43f.ejie.com.X43FNSHF.VerifyAdESSignatureResponse;

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
@Singleton
@Slf4j
public class SignatureServiceImpl 
  implements SignatureService {
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * If file signature is to be done, the SHF expects the file at a PIF location
	 */
	private final PifService _pifService;
	
	private final SignatureServiceAPIData _apiData;
	
	private final Factory<X43FNSHF> _wsClientFactory = 
						new Factory<X43FNSHF>() {
								@Override 
								public X43FNSHF create() {
									log.debug("[SignatureService] > creating the X43FNSHF ws client to URL {}",
											  _apiData.getWebServiceUrl());
									X43FNSHF nshfService = null;
									try {
										if (_apiData.getXLNetsAuthToken() != null) {
											// [1] - Create the auth token
											String xlnetsTokenLinearized = XMLUtils.asStringLinearized(_apiData.getXLNetsAuthToken());	// Linearize xml, strip white spaces and newlines
											if (log.isTraceEnabled()) log.trace("XLNetsToken: {}",xlnetsTokenLinearized);
											
											Map<String,String> authTokenMap = new HashMap<String,String>();
											authTokenMap.put("sessionToken",xlnetsTokenLinearized); 
							
											// [2] - Create the client
											nshfService = new X43FNSHF_Impl(_apiData.getWebServiceUrl().asString());
											Object port = nshfService.getPorts().next();
											HandlerRegistry registry = nshfService.getHandlerRegistry();
											
										
											List<HandlerInfo> handlerList = new ArrayList<HandlerInfo>();
											handlerList.add(new HandlerInfo(EJIESoapMessageHandler.class, 
																			authTokenMap, 
																			null));
											registry.setHandlerChain((QName)port,
																	 handlerList);
										} else {
											throw new IllegalStateException("The XLNets session token is NOT present; check that the xlnets config properties are present");
										}
									} catch (Throwable th) {
										log.error("[SignatureService] > Error while creating the {} service: {}",X43FNSHF.class,th.getMessage(),th);
									}
									return nshfService;
								}
							};
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SignatureServiceImpl(final SignatureServiceAPIData signatureServiceData,
							final PifServiceAPIData pifServiceApiData) {
		_apiData = signatureServiceData;
		_pifService = new PifService(pifServiceApiData);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	API
/////////////////////////////////////////////////////////////////////////////////////////
	public SignatureServiceForApp requiredBy(final AppCode appCode) {
		return new SignatureServiceForAppImpl(appCode);
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class SignatureServiceForAppImpl implements  SignatureServiceForApp{
		private final AppCode _appCode;
		
		public SignatureRequestOutputData createXAdESSignatureOf(final String dataToBeSigned) {		
			return this.createXAdESSignatureOf(dataToBeSigned.getBytes());
		}
		public SignatureRequestOutputData createXAdESSignatureOf(final InputStream dataToBeSigned) throws IOException {		
			return this.createXAdESSignatureOf(ByteStreams.toByteArray(dataToBeSigned));
		}
		public SignatureRequestOutputData createXAdESSignatureOf(final byte[] dataToBeSigned) {		
			return _creatXAdESSignature(_appCode,
										dataToBeSigned);
		}	
		public SignatureRequestOutputData createXAdESSignatureOf(final File fileToBeSigned) throws IOException {
			FileInputStream inputStreamToBeSigned = new FileInputStream(fileToBeSigned);
			return _createXAdESSignatureUsingPif(_appCode,
												 inputStreamToBeSigned);
		}
		public SignatureRequestOutputData createXAdESSignatureOf(final URL urlToBeSigned) throws IOException {
			return _createXAdESSignatureUsingPif(_appCode,
												 urlToBeSigned.openStream());
		}
		public SignatureVerifyOutputData verifyXAdESSignature(final InputStream signedData,
										 final InputStream signature) throws IOException {
			return _verifyXAdESSignature(_appCode,
								  ByteStreams.toByteArray(signedData),ByteStreams.toByteArray(signature));		
		}
		public SignatureVerifyOutputData verifyXAdESSignature(final InputStream signedData,
										 final Document signature) throws IOException {
			return _verifyXAdESSignature(_appCode,
								  ByteStreams.toByteArray(signedData),XMLUtils.asString(signature).getBytes());
		}
		public SignatureVerifyOutputData verifyXAdESSignature(final String signedData,final Document signature) {
			return _verifyXAdESSignature(_appCode,
								  signedData.getBytes(),XMLUtils.asString(signature).getBytes());
		}
		public SignatureVerifyOutputData verifyXAdESSignature(final String signedData,final String signature) {
			return _verifyXAdESSignature(_appCode,
								  signedData.getBytes(),signature.getBytes());
		}
		public SignatureVerifyOutputData verifyXAdESSignature(final byte[] signedData,final byte[] signature) {
			return _verifyXAdESSignature(_appCode,
								  signedData,signature);			
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unused")
	private SignatureRequestOutputData _creatXAdESSignature(final AppCode appCode,
															final byte[] dataToBeSigned) {
			log.debug("[SignatureService] > create XAdES Signature");
			
			SignatureRequestOutputData outputData = null;
			// [1] - Create a ws client using the factory
			X43FNSHF nshf = _wsClientFactory.create();
			if (nshf == null) throw new IllegalStateException(Throwables.message("Could NOT create a {} instance!",X43FNSHF.class));
			try {
				CreateAdESSignature cr = new CreateAdESSignature();
				cr.setCertificateId(_apiData.getCertificateId()); 		// "0035"
				cr.setDocument_B64(Base64.encodeBase64(dataToBeSigned));
				cr.setSignaturePlacement(SignaturePlacementAnonType4.detached);
				X43FNSHF_PortType port = nshf.getX43FNSHF_PortType();
				
				CreateAdESSignatureResponse resp = port.createAdESSignature(cr);
				
				outputData = new SignatureRequestOutputData(resp.getCreateAdESSignatureResult()
																.getEjgvDocument());
			} catch(Throwable th) {
				log.error("[SignatureService] > Error while calling ws at {}: {}",_apiData.getWebServiceUrl(),th.getMessage(),th);
				throw new IllegalStateException(Throwables.message("[SignatureService] > Error while calling ws at {}: {}",_apiData.getWebServiceUrl(),th.getMessage(),th));
			}
			return outputData;
	}
	private SignatureRequestOutputData _createXAdESSignatureUsingPif(final AppCode appCode,
																	 final InputStream inputStremToBeSigned) {
		log.debug("[SignatureService] > create Signature");
		SignatureRequestOutputData outputData = null;
		try {
			// [1] - Create a ws client using the factory
			X43FNSHF nshf = _wsClientFactory.create();
			if (nshf == null) throw new IllegalStateException(Throwables.message("Could NOT create a {} instance!",X43FNSHF.class));
			
			// [2] - Upload the data to PIF at a SHF(x43f) location
			PifFileInfo fileInfo = _pifService.uploadFile(inputStremToBeSigned, 
														   Path.from(Strings.customized("/r02g/{}/{}_{}_toBeSigned.sgn",					// ie: /x43f/xxx/xxx_1214212_toBeSigned"
																   					  appCode,appCode,System.currentTimeMillis())),	
														   false,					// preserve name
														   1L,TimeUnit.HOURS);		// the file is removed from the pif location after 1h
			
			X43FNSHF_PortType port = nshf.getX43FNSHF_PortType();
			CreateAdESSignatureLocation cr = new CreateAdESSignatureLocation();
			cr.setCertificateId(_apiData.getCertificateId()); // "0035"
			cr.setSignaturePlacement(SignaturePlacementAnonType5.detached);
			cr.setDocumentLocation(fileInfo.getFilePath().asAbsoluteString());
			
			CreateAdESSignatureLocationResponse resp = port.createAdESSignatureLocation(cr);
			
			outputData = new SignatureRequestOutputData(resp.getCreateAdESSignatureLocationResult().getEjgvDocument());
		} catch(Throwable th) {
			log.error("[SignatureService] > Error while calling ws at {}: {}",_apiData.getWebServiceUrl(),th.getMessage(),th);
			throw new IllegalStateException(Throwables.message("[SignatureService] > Error while calling ws at {}: {}",_apiData.getWebServiceUrl(),th.getMessage(),th));
		}
		return outputData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private SignatureVerifyOutputData _verifyXAdESSignature(final AppCode appCode,
															final byte[] signedData,final byte[] signature) {
		log.debug("[SignatureService] > verify Signature");
		
		SignatureVerifyOutputData verifyResult = null;
		EjgvDocumentType ejgvDocumentSignature = new EjgvDocumentType();
		EjgvDocument ejgvDocument = new EjgvDocument();
		ejgvDocument.setHeader(new Header());
		ejgvDocument.setBody(new Body());
		
		
		ejgvDocument.getHeader().setType("xades");
		ejgvDocument.getHeader().setPlacement("detached");
		ejgvDocument.getHeader().setFormat("ES-T");
		ejgvDocument.getHeader().setVersion("2.0");
		ejgvDocument.getHeader().setFlags("0");
		ejgvDocument.getHeader().setIsConservable("1");
		ejgvDocument.getHeader().setDocumentIsRequired("0");
		ejgvDocument.getBody().setSign(Base64.encodeBase64String(signature));
		
		ejgvDocumentSignature.setEjgvDocument(ejgvDocument);
		
		VerifyAdESSignature cr = new VerifyAdESSignature();
		cr.setSignature(ejgvDocumentSignature);
		cr.setDocument_B64(Base64.encodeBase64String(signedData).getBytes());
		// [1] - Create a ws client using the factory
		X43FNSHF nshf = _wsClientFactory.create();
		if (nshf == null) throw new IllegalStateException(Throwables.message("Could NOT create a {} instance!",X43FNSHF.class));
		
		try {
			X43FNSHF_PortType port = nshf.getX43FNSHF_PortType();
			VerifyAdESSignatureResponse resp = port.verifyAdESSignature(cr);
			verifyResult = new SignatureVerifyOutputData(resp.getVerifyAdESSignatureResult().getVerificationResult());
		} catch(Throwable th) {
			log.error("[SignatureService] > Error while calling ws at {}: {}",_apiData.getWebServiceUrl(),th.getMessage(),th);
		}
		return verifyResult;
	}

}
