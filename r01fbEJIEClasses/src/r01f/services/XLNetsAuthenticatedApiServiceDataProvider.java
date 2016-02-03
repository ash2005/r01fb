package r01f.services;

import org.w3c.dom.Document;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import n38a.exe.N38APISesion;
import n38c.exe.N38API;
import r01f.enums.EnumWithCode;
import r01f.enums.EnumWithCodeWrapper;
import r01f.exceptions.Throwables;
import r01f.httpclient.HttpClient;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.types.url.Url;
import r01f.types.url.UrlQueryStringParam;
import r01f.types.url.Urls;
import r01f.util.types.Strings;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Base type for XLNets authenticated services (pif, signature, etc)
 * @param <A>  
 */
@Slf4j
public abstract class XLNetsAuthenticatedApiServiceDataProvider<A extends XLNetsAuthenticatedServiceApiData> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates an XLNets token
	 */
	protected static Document _createXLNetsAuthToken(final XMLPropertiesForAppComponent props,
												  	 final String propsRootNode) {
		Document outAuthToken = null;
		try {
			log.debug("[XLNetsAuthenticatedService] > Creating authentication token .........");
	
			String loginAppId = props.propertyAt(propsRootNode + "/xlnets/@loginAppCode").asString();
			
			log.info("[XLNetsAuthenticatedService] > sessionLoginAppId={}",loginAppId);
			
			XLNetsAuthTokenType tokenType = props.propertyAt(propsRootNode + "/xlnets/@token").asEnumFromCode(XLNetsAuthTokenType.class,
																											  XLNetsAuthTokenType.LOGIN_APP);
			log.warn("[XLNetsAuthenticatedService]: {} XLNets token",tokenType);
			
			if (tokenType == XLNetsAuthTokenType.LOGIN_APP) {
				// login App
				N38APISesion myAPISesion;
				N38API n38API;
				
				myAPISesion = new N38APISesion();
				n38API = new N38API(myAPISesion.n38APISesionCrearApp(loginAppId));
				outAuthToken =  n38API.n38ItemSesion();
			} 
			else if (tokenType == XLNetsAuthTokenType.FILE) {
				// File
				String xlnetsMockTokenPath = props.propertyAt(propsRootNode + "/xlnets/sessionToken").asString();
				if (Strings.isNullOrEmpty(xlnetsMockTokenPath)) throw new IllegalStateException(Throwables.message("There's NO path for a mock xlnets token at {}",
																											       propsRootNode + "/xlnets/sessionToken"));
				log.warn("Geting a xlnets auth token for appCode={} from file={}",loginAppId,xlnetsMockTokenPath);
				
				outAuthToken = XMLUtils.parse(ResourcesLoaderBuilder.DEFAULT_RESOURCES_LOADER
																	.getInputStream(xlnetsMockTokenPath));
			} 
			else if (tokenType == XLNetsAuthTokenType.HTTP_PROVIDED) {
				// http provided (ie: http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/Y31JanoServiceXlnetsTokenCreatorServlet?login_app=X42T)
				Url xlnetsProviderUrl = Urls.join(props.propertyAt(propsRootNode + "/xlnets/sessionToken")
													   .asUrl(),
												  new UrlQueryStringParam("login_app",loginAppId));
				log.warn("Geting a xlnets auth token for appCode={} from url={}",loginAppId,xlnetsProviderUrl);
				
				outAuthToken = XMLUtils.parse(HttpClient.forUrl(xlnetsProviderUrl)
														.GET()
														.loadAsStream());
			}
			if (log.isDebugEnabled()) log.warn("[XLNetsAuthenticatedService] token={}",XMLUtils.asStringLinearized(outAuthToken));
		} catch (Throwable th) {
			log.error("[XLNetsAuthenticatedService] > Error while creating the Signature Service auth token: {}",th.getMessage(),th);
		}
		return outAuthToken;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@RequiredArgsConstructor
		 enum XLNetsAuthTokenType
	implements EnumWithCode<String,XLNetsAuthTokenType> {
			LOGIN_APP("loginApp"),
			HTTP_PROVIDED("httpProvided"),
			FILE("file");

			@Getter private final String _code;
			@Getter private final Class<String> _codeType = String.class;

			private static EnumWithCodeWrapper<String,XLNetsAuthTokenType> WRAPPER = EnumWithCodeWrapper.create(XLNetsAuthTokenType.class);
			
			@Override
			public boolean isIn(final XLNetsAuthTokenType... els) {
				return WRAPPER.isIn(this,els);
			}
			@Override
			public boolean is(final XLNetsAuthTokenType el) {
				return WRAPPER.is(this,el);
			}

			 
			
	}
}
