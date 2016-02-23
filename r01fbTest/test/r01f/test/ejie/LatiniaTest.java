package r01f.test.ejie;

import com.google.inject.Guice;
import com.google.inject.Injector;

import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.model.latinia.LatiniaRequestMessage;
import r01f.services.latinia.LatiniaService;
import r01f.services.latinia.LatiniaServiceGuiceModule;
import r01f.xmlproperties.XMLPropertiesGuiceModule;
/**
 * ERPI Consoles can help you to check process success:
 * DESARROLLO: svc.integracion.jakina.ejiedes.net/w43saConsolaWAR/
 * PRUEBAS: svc.integracion.jakina.ejiepru.net/w43saConsolaWAR/
 * PRODUCCION: svc.integracion.jakina.ejgvdns/w43saConsolaWAR/
 */
public class LatiniaTest {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	public static void main(String[] args) {
		Injector injector = Guice.createInjector(new XMLPropertiesGuiceModule(),
												 new LatiniaServiceGuiceModule(AppCode.forId("r01fb"),
														 					   AppComponent.forId("test"),
														 					   "test"));


		LatiniaService latiniaService = injector.getInstance(LatiniaService.class);
		
		LatiniaRequestMessage msg = _createMockMessage();
		System.out.println("=====> " + latiniaService.getLatiniaRequestMessageAsXml(msg));
		latiniaService.sendNotification(msg);
	}
	private static LatiniaRequestMessage _createMockMessage() {
		LatiniaRequestMessage latiniaMsg = new LatiniaRequestMessage();
		latiniaMsg.setAcknowledge("S");
		latiniaMsg.setMessageContent("TEST MESSAGE x47b intento 1");
		latiniaMsg.setReceiverNumbers("688671967");
		return latiniaMsg;
	}
}
