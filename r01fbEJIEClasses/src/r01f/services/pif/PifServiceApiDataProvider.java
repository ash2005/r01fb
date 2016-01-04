package r01f.services.pif;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.w3c.dom.Document;

import r01f.guids.CommonOIDs.AppCode;
import r01f.services.XLNetsAuthenticatedApiServiceDataProvider;
import r01f.services.XLNetsAuthenticatedServiceApiData;
import r01f.services.pif.PifServiceApiDataProvider.PifServiceAPIData;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.google.inject.Provider;

/**
 * Provides a {@link PifService} using a properties file info
 * For this provider to work, a properties file with the following config MUST be provided:
 * <pre class='xml'>
 * 		<pifService>
 * 			<uiConsoleUrl>http://svc.integracion.jakina.ejiedes.net/y31dBoxWAR/appbox</uiConsoleUrl>
 *		</pifService>
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
public class PifServiceApiDataProvider
	 extends XLNetsAuthenticatedApiServiceDataProvider<XLNetsAuthenticatedServiceApiData>
  implements Provider<PifServiceAPIData> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private PifServiceAPIData _apiData;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public PifServiceApiDataProvider(final AppCode appCode,
									 final XMLPropertiesForAppComponent props,final String propsRootNode)  {
		Document pifAuthToken = _createXLNetsAuthToken(props,propsRootNode);
		_apiData = new PifServiceAPIData(pifAuthToken);
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
//  PROVIDER
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public PifServiceAPIData get() {	
		return _apiData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	Service API data
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public class PifServiceAPIData 
	  implements XLNetsAuthenticatedServiceApiData {
		@Getter private final Document _XLNetsAuthToken;
	}
}
