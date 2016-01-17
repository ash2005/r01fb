package r01f.services.pif;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import javax.inject.Singleton;

import com.ejie.y31.exception.Y31JanoServiceGenericException;
import com.ejie.y31.factory.Y31JanoServiceAbstractFactory;
import com.ejie.y31.service.Y31JanoService;
import com.ejie.y31.vo.Y31AttachmentBean;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.file.FileName;
import r01f.mime.MimeType;
import r01f.model.pif.PifFile;
import r01f.model.pif.PifFileInfo;
import r01f.services.pif.PifServiceApiDataProvider.PifServiceAPIData;
import r01f.types.Factory;
import r01f.types.Path;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Encapsulates pif services
 * IMPORTANT!!!!!
 * 		Graphical user interface URL (ui console): http://svc.integracion.jakina.ejiedes.net/y31dBoxWAR/appbox
 * 
 * 
 * To build a {@link PifService} instance one of {@link XMLPropertiesForAppComponent} or {@link PifServiceAPIData} are needed
 * ... those can be built by hand but the normal usage is to have them binded using guice:
 * <pre class='brush:java'>
 *	    public static void main(String[] args) {
 *	    	Injector injector = Guice.createInjector(new PifServiceGuiceModule(AppCode.forId("xxx"),
 *																			   AppComponent.forId("myComp"),
 *																			   "signature"),
 *													 new XMLPropertiesGuiceModule());
 *      
 *	    	PifService pifService = injector.getInstance(PifService.class);
 *	    	pifService.getFile(path);
 *	    }
 * </pre>
 * ... or without using the PifServiceGuiceModule
 * <pre class='brush:java'>
 * 		// create a provider method for the XMLProperties file that contains the pif properties
 *		@Provides @XMLPropertiesComponent("pif")
 *		XMLPropertiesForAppComponent provideXMLPropertiesForServices(final XMLProperties props) {
 *			XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_appCode),
 *																								 AppComponent.forId("pif"));
 *			return outPropsForComponent;
 *		}
 *		// create a provider method for the PifService
 *		@Provides @Singleton	// provides a single instance of the pif service
 *		PifService _providePifService(@XMLPropertiesComponent("notifier") final XMLPropertiesForAppComponent props) {
 *			// Provide a new pif service api data using the provider
 *			PifServiceApiDataProvider pifApiServiceProvider = new PifServiceApiDataProvider(_appCode,
 *																										props,"notifier");
 *			// Using the pif service api data create the PifService object
 *			PifService outPifService = new PifService(pifApiServiceProvider.get());
 *			return outPifService;
 *		}
 * </pre>
 * 
 * Sample usage in a not injected app:
 * <pre class='brush:java'>
 *		PifServiceApiDataProvider PifServiceApiProvider = new PifServiceApiDataProvider(props);		// props must be loaded by hand
 *		PifService pifService = new PifService(PifServiceApiProvider.get());
 *	    pifService.downloadFile(path);
 * </pre>
 * 
 * For all this to work a properties file with the following config MUST be provided:
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
 * 
 * IMPORTANT!!!
 * ============
 * A y31.properties file MUST be present at the classpath with the following content (for dev environment):
 * 
 *			#establece si se valida el xml contra xlnets o solo se inenta extraer la info
 *			STRICT_SECURITY_CHECKER = false
 *			
 *			
 *			Y31_JANO_REST_END_POINT=http://svc.intra.integracion.jakina.ejiedes.net/ctxapp
 *			
 *			DEFAULT_HADOOP_HOST=hdfs://ejld071:8150
 *			
 *			# Base de datos que se esta utilizando (true: MongoDB, false: Oracle)
 *			Y31_JANO_MONGODB=true
 *			
 *			MONGO_SERVER = ejld070
 *			MONGO_PORT = 8155
 *			MONGO_DB = y31
 *			
 *			#ORACLE_JNDI_NAME= y31.y31DataSource
 *			
 *			QUEUE_EVENTS_PROVIDER_URL = t3://wl11vf0013.ejiedes.net:7002,wl11vf0015.ejiedes.net:7002
 *			QUEUE_EVENTS_SECURITY_PRINCIPAL = weblogic11
 *			QUEUE_EVENTS_SECURITY_CREDENTIALS = DESAweblogic11
 *			
 *			QUEUE_TRACE_PROVIDER_URL = t3://wl11vf0021.ejiedes.net:7001,wl11vf0023.ejiedes.net:7001
 *			QUEUE_TRACE_SECURITY_PRINCIPAL = weblogic11
 *			QUEUE_TRACE_SECURITY_CREDENTIALS = DESAweblogic11
 *			
 *			
 *			# parametros que solo deberian estar en softbase, aqui estan de forma provisional hasta que tengamos pif en aplic
 *			JANO_OPERATION_SUFIX=
 *			EVENT_WHAT_VALUE=WRITE
 *			
 *			###########################
 *			# pif2
 *			###########################
 *			
 *			# superadmin mode; permite navegar por AppBOX y UserBOX
 *			Y31_BOX_SUPERADMIN_ENABLED=false
 *			Y31_EDIT_ON_LINE_BASE_URL=svc.integracion.jakina.ejiedes.net/y31dWebDavUserWAR
 *			
 *			# explorer enabled; permite acceder a AppBOX (entornos de pruebas)
 *			Y31_BOX_EXPLORER_ENABLED=true
 *			
 *			# si Y31_BOX_EXPLORER está activo, el usuario adminsitrador con el que realizar operaciones
 *			Y31_BOX_ADMIN_APP_USER=Y31
 *			
 *			# Y31JanoServiceSecurityTool, carta blanca de acceso 'path;usuario' separado por ','
 *			Y31_WHITELIST=/;t17i,/;y31
 */
@Singleton
@Slf4j
public class PifService {
	
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private final PifServiceAPIData _apiData;
	
	private final Factory<Y31JanoService> _clientFactory = new Factory<Y31JanoService>() {
																	@Override 
																	public Y31JanoService  create() {
																		log.debug("[PifService] > creating the Y31JanoService client");
																		Y31JanoService janoService = null;
																		try {
																			janoService = Y31JanoServiceAbstractFactory.getInstance();
																		} catch (Y31JanoServiceGenericException jEx) {
																			log.error("[PifService] > Error while creating the {} service: {}",Y31JanoService.class,jEx.getMessage(),jEx);
																		}
																		if (janoService == null) throw new IllegalStateException(Throwables.message("Could NOT create a {} instance!",Y31JanoService.class));
																		return janoService;
																	}
																};
/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public PifService(final PifServiceAPIData apiData) {
		_apiData = apiData;
	}
	
/////////////////////////////////////////////////////////////////////////////////////////
// 	API
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Uploads a file to PIF
	 * @param filetoUpdload
	 * @param pathTo
	 * @param preserveName
	 * @param timeTolive
	 * @param timeUnit
	 * @return
	 */
	public PifFileInfo uploadFile(final InputStream filetoUpdload,
							   	  final Path pathTo,
							   	  final boolean preserveName,final Long timeTolive,final TimeUnit timeUnit) {
		log.debug("[PifService] > put file in PIF");

		Y31JanoService service = _clientFactory.create();

		PifFileInfo outputData = null;
		try {
			Y31AttachmentBean result = service.put(_apiData.getXLNetsAuthToken(),
												   filetoUpdload,
												   pathTo.asAbsoluteString(),
												   preserveName,timeUnit.toMillis(timeTolive));
			outputData = new PifFileInfo(MimeType.forName(result.getContentType()),
										 FileName.of(result.getFileName()), 
										 Path.of(result.getFilePath()), 
										 result.getSize());
		} catch(Throwable th) {
			log.error("[PifService] > Error while calling PIF service: {}",th.getMessage(),th);
			throw new IllegalStateException(Throwables.message("[PifService] > Error while calling PIF service: {}",th.getMessage(),th));
		}
		return outputData;
	}
	/**
	 * Downloads a file from PIF	
	 * @param pathFrom
	 * @return
	 */
	public PifFile downloadFile(final Path pathFrom) {
		log.debug("[PifService] > get file from PIF");
		PifFile outputData = null;
		Y31JanoService service = _clientFactory.create();
		if(service == null){
			 throw new IllegalStateException(Throwables.message("Could NOT create a {} instance!",Y31JanoService.class));
		}
		try {
			InputStream result = service.get(_apiData.getXLNetsAuthToken(),
											 pathFrom.asAbsoluteString());
			outputData = new PifFile(pathFrom,
									 result);
		} catch(Throwable th) {
			log.error("[PifService] > Error while calling PIF service: {}",th.getMessage(),th);
			throw new IllegalStateException(Throwables.message("[PifService] > Error while calling PIF service: {}",th.getMessage(),th));
		}
		
		return outputData;
	}
	

}
