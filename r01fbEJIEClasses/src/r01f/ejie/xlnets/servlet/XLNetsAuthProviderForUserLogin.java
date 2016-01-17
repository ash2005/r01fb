/*
 * Created on 03-dic-2004
 *
 * @author Juan José Villa (EJIE)
 */
package r01f.ejie.xlnets.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import n38c.exe.N38API;

import org.w3c.dom.Document;

import r01f.ejie.xlnets.XLNetsUserSession;


/**
 * Provider de seguridad basado en XLNets
 */
@NoArgsConstructor
@Slf4j
  class XLNetsAuthProviderForUserLogin 
extends XLNetsAuthProviderBase {
///////////////////////////////////////////////////////////////////////////////////////////
//  Constructores
///////////////////////////////////////////////////////////////////////////////////////////
	/**
     * Constructor en base a la request
     */
    public XLNetsAuthProviderForUserLogin(final ServletRequest newReq) {
        super(newReq);
    }

///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE XLNetsAuthProviderBase
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public XLNetsUserSession getXLNetsSession() {
        // Ver si existe session (aqui se comprueban las cookies, etc)
        // Instanciar la n38ItemSesion para obtener la informacion de la
    	// sesion XLNets del usuario
    	N38API n38API = new N38API((HttpServletRequest)_request);
    	
    	// [1] - Obtener la información de sesión
    	Document docItemSesion = n38API.n38ItemSesion();
    	XLNetsUserSession session = new XLNetsUserSession(docItemSesion.getDocumentElement());
    	
    	// [2] - Añadir información de usuario
    	Document docUser = n38API.n38ItemObtenerPersonas("uid=" + session.getUser().getPersona());
    	session.setUserInfo(docUser);
    	
        // [3] - Atributos de la sesion
        Map<String,String> authAttrs = new HashMap<String,String>();
        authAttrs.put("provider","xlNetsProvider");	// Por poner algo... no es necesario
    	
    	return session;
    }
}
