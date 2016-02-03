package r01f.services.shf;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.w3c.dom.Document;

import r01f.guids.CommonOIDs.AppCode;
import r01f.services.XLNetsAuthenticatedApiServiceDataProvider;
import r01f.services.XLNetsAuthenticatedServiceApiData;
import r01f.services.shf.SignatureServiceApiDataProvider.SignatureServiceAPIData;
import r01f.types.url.Url;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.inject.Provider;

/**
 * Provides a {@link SignatureService} using a properties file info
 * <pre class='java'>
 *		SignatureServiceProvider signatureServiceProvider = new SignatureServiceProvider(props);
 *		SignatureService signatureService = signatureServiceProvider.get();
 * </pre>
 * For this provider to work, a properties file with the following config MUST be provided:
 * <pre class='xml'>
 * 	<signatureService>
 *		<wsURL>http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/X43FNSHF2?WSDL</wsURL>
 *		<certificateId>0035</certificateId>
 *	</signatureService>
 *		... any other properties...
 *	<xlnets loginAppCode='theAppCode' token='httpProvided'>	<!-- token=file/httpProvided/loginApp -->
 *		<sessionToken>
 *			if token=file: 			...path to a mock xlnets token (use https://xlnets.servicios.jakina.ejiedes.net/xlnets/servicios.htm to generate one)
 *			if token=httpProvided:  ...url to the url that provides the token (ie: http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/Y31JanoServiceXlnetsTokenCreatorServlet?login_app=appId)
 *			if token=loginApp		...not used 
 *		</sessionToken>
 *	</xlnets>
 * </pre>
 */
public class SignatureServiceApiDataProvider 
	 extends XLNetsAuthenticatedApiServiceDataProvider<SignatureServiceAPIData>
  implements Provider<SignatureServiceAPIData> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private SignatureServiceAPIData _apiData;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public SignatureServiceApiDataProvider(final AppCode appCode,
										   final XMLPropertiesForAppComponent props)  {
		this(appCode,
			 props,"signature");
	}
	@SuppressWarnings("unused")
	public SignatureServiceApiDataProvider(final AppCode appCode,
										   final XMLPropertiesForAppComponent props,final String propsRootNode)  {
		Url webServiceUrl = props.propertyAt(propsRootNode + "/signatureService/wsURL")
						      	 .asUrl("http://svc.intra.integracion.jakina.ejgvdns/ctxapp/X43FNSHF2?WSDL");
		Document authToken = _createXLNetsAuthToken(props,propsRootNode);
		String certificateId = props.propertyAt(propsRootNode + "/signatureService/certificateId").asString("0035");
		
		_apiData = new SignatureServiceAPIData(webServiceUrl,
											   authToken,
											   certificateId);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  API DATA
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public class SignatureServiceAPIData
	  implements XLNetsAuthenticatedServiceApiData {
		@Getter private final Url _webServiceUrl;
		@Getter private final Document _XLNetsAuthToken;
		@Getter private final String _certificateId;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public SignatureServiceAPIData get() {
		return _apiData;
	}
}
