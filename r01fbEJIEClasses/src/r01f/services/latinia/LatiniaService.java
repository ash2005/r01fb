package r01f.services.latinia;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.rpc.handler.HandlerInfo;
import javax.xml.rpc.handler.HandlerRegistry;

import lombok.extern.slf4j.Slf4j;
import r01f.exceptions.Throwables;
import r01f.marshalling.Marshaller;
import r01f.model.latinia.LatiniaRequest;
import r01f.model.latinia.LatiniaRequestMessage;
import r01f.model.latinia.LatiniaResponse;
import r01f.services.EJIESoapMessageHandler;
import r01f.services.ServiceCanBeDisabled;
import r01f.services.latinia.LatiniaServiceApiDataProvider.LatiniaServiceAPIData;
import r01f.types.Factory;
import r01f.util.types.Strings;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

import com.ejie.w91d.client.W91DSendSms;
import com.ejie.w91d.client.W91DSendSmsWebServiceImplService_Impl;
import com.google.inject.Singleton;

/**
 * Encapsulates latinia message sending
 * To build a {@link LatiniaService} instance one of {@link XMLPropertiesForAppComponent} / {@link Marshaller} or {@link LatiniaServiceAPIData} are needed
 * ... those can be built by hand but the normal usage is to have them binded using guice:
 * <pre class='brush:java'>
 *	    public static void main(String[] args) {
 *	    	Injector injector = Guice.createInjector(new LatiniaServiceGuiceModule(AppCode.forId("xxx"),
 *																				   AppComponent.forId("notifier"),
 *																				   "notifier"),
 *													 new XMLPropertiesGuiceModule());
 *      
 *	    	LatiniaService latiniaService = injector.getInstance(LatiniaService.class);
 *	    	latiniaService.sendNotification(_createMockMessage());
 *	    }
 * </pre>
 * ... or without using the LatiniaServiceGuiceModule
 * <pre class='brush:java'>
 * 		// create a provider method for the XMLProperties file that contains the latinia properties
 *		@Provides @XMLPropertiesComponent("notifier")
 *		XMLPropertiesForAppComponent provideXMLPropertiesForServices(final XMLProperties props) {
 *			XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(_appCode),
 *																								 AppComponent.forId("notifier"));
 *			return outPropsForComponent;
 *		}
 *		// create a provider method for the LatiniaService
 *		@Provides @Singleton	// provides a single instance of the latinia service
 *		LatiniaService _provideLatiniaService(@XMLPropertiesComponent("notifier") final XMLPropertiesForAppComponent props) {
 *			// Provide a new latinia service api data using the provider
 *			LatiniaServiceApiDataProvider latiniaApiServiceProvider = new LatiniaServiceApiDataProvider(_appCode,
 *																										props,"notifier");
 *			// Using the latinia service api data create the LatiniaService object
 *			LatiniaService outLatiniaService = new LatiniaService(latiniaApiServiceProvider.get());
 *			return outLatiniaService;
 *		}
 * </pre>
 * 
 * Sample usage in a not injected app:
 * <pre class='brush:java'>
 *		LatiniaServiceApiDataProvider latiniaServiceApiProvider = new LatiniaServiceApiDataProvider(props);		// props must be loaded by hand
 *		LatiniaService latiniaService = new LatiniaService(latiniaServiceApiProvider.get());
 *	    latiniaService.sendNotification(_createMockMessage());
 * </pre>
 * 
 * To build a message: 
 * <pre class='brush:java'>
 *	    private static LatiniaRequestMessage _createMockMessage() {
 *	    	LatiniaRequestMessage latiniaMsg = new LatiniaRequestMessage();
 *	    	latiniaMsg.setAcknowledge("S");
 *	    	latiniaMsg.setMessageContent("TEST MESSAGE!!!");
 *	    	latiniaMsg.setReceiverNumbers("688671967");
 *	    	return latiniaMsg;
 *	    }
 * </pre>
 * 
 * For all this to work a properties file with the following config MUST be provided:
 * <pre class='xml'>
 * 	<latinia>
 *		<wsURL>http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/W91dSendSms?WSDL</wsURL>
 *		<authentication>
 *		  <enterprise>
 *		    		<login>INNOVUS</login>
 *		    		<user>innovus.superusuario</user>
 *		    		<password>MARKSTAT</password>
 *		  </enterprise>
 *		  <clientApp>
 *		    		<productId>X47B</productId>
 *		    		<contractId>2066</contractId>
 *		    		<password>X47N</password>
 *		  </clientApp>
 *		</authentication>
 *	</latinia>
 * </pre>
 * 
 * NOTE:
 * Latinia session Token:
 * 		<authenticationLatinia>
 * 			<loginEnterprise>INNOVUS</loginEnterprise>
 * 		    <userLatinia>innovus.superusuario</userLatinia>
 * 		    <passwordLatinia>MARKSTAT</passwordLatinia>
 * 		    <refProduct>X47B</refProduct>
 * 		    <idContract>xxxx</idContract>
 * 		    <password>X47B</password>
 * 		</authenticationLatinia>
 *
 * SOAP Message example:
 * <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:w91d="http://w91d">
 * 	<soapenv:Header>
 *		<authenticationLatinia>
 *           <userLatinia>innovus.superusuario</userLatinia>
 *           <passwordLatinia>MARKSTAT</passwordLatinia>
 *           <refProduct>X47B</refProduct>
 *           <loginEnterprise>INNOVUS</loginEnterprise>
 *           <idContract>2066</idContract>
 *           <password>X47B</password>
 *		</authenticationLatinia>
 * 	</soapenv:Header>
 * 	<soapenv:Body>
 * 		<StringInput xmlns="http://w91d">
 *		<![CDATA[<?xml version="1.0" encoding="UTF-8" standalone="no"?>
 *		<PETICION>
 *		<LATINIA>
 *		<MENSAJES>
 *		<MENSAJE_INFO ACUSE="S">
 *		<TEXTO>Hola con prioridad BAJA pero BAJA</TEXTO>
 *		<GSM_DEST>616178858</GSM_DEST>
 *		</MENSAJE_INFO>
 *		</MENSAJES>
 *		</LATINIA>
 *		</PETICION>]]>
 *		</StringInput>
 *	</soapenv:Body>
 * </soapenv:Envelope>
 *
 */
@Singleton
@Slf4j
public class LatiniaService
  implements ServiceCanBeDisabled {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean _disabled;
	private final LatiniaServiceAPIData _apiData;
	
	private final Factory<W91DSendSms> _wsClientFactory = new Factory<W91DSendSms>() {
																	@Override @SuppressWarnings("deprecation")
																	public W91DSendSms create() {
																		log.debug("[Latinia] > creating the latinia ws client to URL {}",_apiData.getWebServiceUrl());
																		W91DSendSms sendSmsService = null;
																		try {
																			// [1] - Create the auth token
																			Map<String,String> authTokenMap = new HashMap<String,String>();
																			authTokenMap.put("sessionToken",XMLUtils.asStringLinearized(_apiData.getLatiniaAuthToken())); //Linarize xml, strip whitespaces and newlines from latinia auth token
		
																			// [2] - Create the client
																			W91DSendSmsWebServiceImplService_Impl ws = new W91DSendSmsWebServiceImplService_Impl(_apiData.getWebServiceUrl().asString());
																			sendSmsService = ws.getW91dSendSms();
																			HandlerRegistry registry = ws.getHandlerRegistry();
																			Object port = ws.getPorts().next();
		
																			List<HandlerInfo> handlerList = new ArrayList<HandlerInfo>();
																			handlerList.add(new HandlerInfo(EJIESoapMessageHandler.class,
																											authTokenMap,
																											null));	// ?
																			registry.setHandlerChain((QName)port,
																									 handlerList);
		
																		} catch (Throwable th) {
																			log.error("[Latinia] > Error while creating the {} service: {}",W91DSendSms.class,th.getMessage(),th);
																		}
																		return sendSmsService;
																	}
															  };
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public LatiniaService(final LatiniaServiceAPIData apiData) {
		_apiData = apiData;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ServiceCanBeDisabled
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean isEnabled() {
		return !_disabled;
	}
	@Override
	public boolean isDisabled() {
		return _disabled;
	}
	@Override
	public void setEnabled() {
		_disabled = false;
	}
	@Override
	public void setDisabled() {
		_disabled = true;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	API
/////////////////////////////////////////////////////////////////////////////////////////
	public LatiniaResponse sendNotification(final LatiniaRequestMessage msg) {
		log.debug("[Latinia] > Send message");

		// [1] - Create a ws client using the factory
		W91DSendSms sendSmsService = _wsClientFactory.create();
		if (sendSmsService == null) throw new IllegalStateException(Throwables.message("Could NOT create a {} instance!",W91DSendSms.class));

		// [2] - Compose the request
		LatiniaRequest request = new LatiniaRequest();
		request.addMessage(msg);

		// [3] - Send the request
		LatiniaResponse response = null;
		try {
			StringBuilder requestXml = new StringBuilder("<![CDATA[");
			requestXml.append(_apiData.getLatiniaObjsMarshaller().xmlFromBean(request));
			requestXml.append("]]>");
			log.info("[Latinia] > request XML: {} ",requestXml);

			final String responseXml = sendSmsService.sendSms(requestXml.toString());
			if (!Strings.isNullOrEmpty(responseXml)) {
				String theResponseXml = Strings.of(responseXml).replaceAll("PETICION","RESPUESTA")
											   .asString();
				log.info("[Latinia] > response XML: {}",responseXml);
				response = _apiData.getLatiniaObjsMarshaller().beanFromXml(theResponseXml);
			} else {
				throw new IllegalStateException("Latinia WS returned a null response!");
			}

		} catch (Throwable th) {
			log.error("[Latinia] > Error while calling ws at {}: {}",_apiData.getWebServiceUrl(),th.getMessage(),th);
		}
		return response;
	}
}
