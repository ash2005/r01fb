/*
 * Created on 25-ene-2010
 */
package r01f.ejie.xlnets.servlet;

import java.util.HashMap;
import java.util.Map;

import n38a.exe.N38APISesion;

import org.w3c.dom.Document;

import r01f.ejie.xlnets.XLNetsUserSession;
import r01f.util.types.Strings;


/**
 * Provider de seguridad basado en XLNets
 * @author Pedro Marquínez (co01350c)
 */
  class XLNetsAuthProviderForAppLogin 
extends XLNetsAuthProviderBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Constructor
	 */
	public XLNetsAuthProviderForAppLogin() {
		super();
	}
	
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE XLNetsAuthProviderBase
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public XLNetsUserSession getXLNetsSession() {
        // extraer el código de aplicación
        String appCode = _providerDef.getInitParams().getProperty(XLNetsAuthProviderBase.APP_CODE_PROPERTY);
        if (Strings.isNullOrEmpty(appCode)) return null;
    	
    	// [1] - Obtener la información de sesión
        Document appSesion= new N38APISesion().n38APISesionCrearApp(appCode);
    	XLNetsUserSession session = new XLNetsUserSession(appSesion.getDocumentElement());
    	
    	// [2] - Añadir información de usuario
    	//Document docUser = n38API.n38ItemObtenerPersonas("uid=" + session.getUser().getPersona());
    	//session.setUserInfo(docUser);
    	
        // [3] - Atributos de la sesion
        Map<String,String> authAttrs = new HashMap<String,String>();
        authAttrs.put("provider","appProvider");	// Por poner algo... no es necesario
    	
    	return session;
    }
}
