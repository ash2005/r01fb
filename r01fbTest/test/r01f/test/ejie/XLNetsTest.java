package r01f.test.ejie;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;

import lombok.extern.slf4j.Slf4j;
import r01f.ejie.xlnets.XLNetsFuncion;
import r01f.ejie.xlnets.XLNetsItemSeguridad;
import r01f.ejie.xlnets.XLNetsSession;
import r01f.ejie.xlnets.login.XLNetsAuthenticatedApiServiceDataProvider;
import r01f.ejie.xlnets.servlet.XLNetsAppCfg;
import r01f.ejie.xlnets.servlet.XLNetsAuthCtx;
import r01f.ejie.xlnets.servlet.XLNetsResourceAuthorization;
import r01f.ejie.xlnets.servlet.XLNetsResourceCtx;
import r01f.ejie.xlnets.servlet.XLNetsTargetCtx;
import r01f.ejie.xlnets.servlet.XLNetsOIDs.XLNetsOrgDivisionOID;
import r01f.ejie.xlnets.servlet.XLNetsOIDs.XLNetsOrgDivisionServiceOID;
import r01f.ejie.xlnets.servlet.XLNetsOIDs.XLNetsOrganizationOID;
import r01f.ejie.xlnets.servlet.XLNetsTargetCfg.ResourceItemType;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.guids.CommonOIDs.UserCode;
import r01f.httpclient.HttpClient;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xml.XMLUtils;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForAppComponent;
/**
 * ERPI Consoles can help you to check process success:
 * DESARROLLO: svc.integracion.jakina.ejiedes.net/w43saConsolaWAR/
 * PRUEBAS: svc.integracion.jakina.ejiepru.net/w43saConsolaWAR/
 * PRODUCCION: svc.integracion.jakina.ejgvdns/w43saConsolaWAR/
 */
@Slf4j
public class XLNetsTest {
/////////////////////////////////////////////////////////////////////////////////////////
//
/////////////////////////////////////////////////////////////////////////////////////////
	@Test @SuppressWarnings("static-method")
	public void testConfig() {
		XMLPropertiesForAppComponent props = XMLProperties.createForAppComponent(AppCode.forId("r01fb"),
																			     AppComponent.forId("xlnets.fileUserToken"))
														  .notUsingCache();
		XLNetsAppCfg cfg = new XLNetsAppCfg(props);
		log.warn(cfg.toString());
	}
	@Test @SuppressWarnings("static-method")
	public void testXLNetsSessionHttpProvided() {
		XMLPropertiesForAppComponent props = XMLProperties.createForAppComponent(AppCode.forId("r01fb"),
																			     AppComponent.forId("xlnets.loginAppHttpProvided"))
														  .notUsingCache();
		XLNetsAuthenticatedApiServiceDataProvider provider = new XLNetsAuthenticatedApiServiceDataProvider(props);
		Document xlnetsSessionTokenDoc = provider.getXLNetsSessionTokenDoc();
		Assert.assertNotNull(xlnetsSessionTokenDoc);
		
		System.out.println(XMLUtils.asString(xlnetsSessionTokenDoc));
		
		XLNetsSession xlnetsSessionToken = new XLNetsSession(xlnetsSessionTokenDoc);
	}
	@Test @SuppressWarnings("static-method")
	public void testXLNetsSessionFileProvided() {
		XMLPropertiesForAppComponent props = XMLProperties.createForAppComponent(AppCode.forId("r01fb"),
																			     AppComponent.forId("xlnets.fileUserToken"))
														  .notUsingCache();
		XLNetsAuthenticatedApiServiceDataProvider provider = new XLNetsAuthenticatedApiServiceDataProvider(props);
		Document xlnetsSessionTokenDoc = provider.getXLNetsSessionTokenDoc();
		Assert.assertNotNull(xlnetsSessionTokenDoc);
		
		System.out.println(XMLUtils.asString(xlnetsSessionTokenDoc));
		
		XLNetsSession xlnetsSessionToken = new XLNetsSession(xlnetsSessionTokenDoc);
		
    	// add the user name: issues another n38 api call to get the user info
    	Document xlnetsUserInfoDoc = provider.getXLNetsUserDoc(null,
    													   	   xlnetsSessionTokenDoc);
    	Assert.assertNotNull(xlnetsUserInfoDoc);
    	xlnetsSessionToken.setUserInfo(xlnetsUserInfoDoc.getDocumentElement());

    	Assert.assertNotNull(xlnetsSessionToken.getUser());
    	Assert.assertNotNull(xlnetsSessionToken.getUser().getOid());
    	
    	Document xlnetsAuthDoc = provider.getAuthorization(null,
		                								   "AA14A-FN-0001",ResourceItemType.FUNCTION);
    	Assert.assertNotNull(xlnetsAuthDoc);
    	XLNetsFuncion func = new XLNetsFuncion(xlnetsAuthDoc.getDocumentElement());
    	Assert.assertEquals(func.getItemSeguridad().getCommonName(),"AA14A-FN-0001");
    	
    	
    	// Build the auth ctx
        XLNetsAuthCtx xlnetsAuthCtx = new XLNetsAuthCtx(xlnetsSessionToken);		// targets.... de momento nada... se va rellenando conforme se llama a authorize
		XLNetsResourceCtx resourceCtx = new XLNetsResourceCtx(func);

		Map<String,XLNetsResourceAuthorization> resAuth = resourceCtx.getAuthorizations();
		if (CollectionUtils.hasData(resAuth)) {
			for (Map.Entry<String,XLNetsResourceAuthorization> me : resAuth.entrySet()) {
				System.out.println("===>" + me.getKey() + " > " + me.getValue().getProfileOid());
			}
		}
	}
	@Test @SuppressWarnings("static-method")
	public void testSessionTokenProvider() {
		try {
			String xlnetsSessionToken = HttpClient.forUrl("http://svc.intra.integracion.jakina.ejiedes.net/ctxapp/Y31JanoServiceXlnetsTokenCreatorServlet?login_app=X42T")
												  .GET()
												  .loadAsString();
			log.info("XLNetsSessionToken: {}",
					 xlnetsSessionToken);
			
			Document xlnetsSessionTokenDoc = XMLUtils.parse(xlnetsSessionToken);
			
			String xlnetsSessionTokenLinearized = XMLUtils.asStringLinearized(xlnetsSessionTokenDoc);
			
			log.info("XLNetsSessionTokenLinearized: {}",
					 xlnetsSessionTokenLinearized);
		} catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
