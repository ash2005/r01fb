/*
 * Created on 28-jul-2004
 *
 * @author ie00165h
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets.servlet;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import lombok.NoArgsConstructor;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import r01f.ejie.xlnets.XLNetsUserSession;
import r01f.types.Path;
import r01f.xml.XMLUtils;
/**
 * Provider de seguridad en base a ficheros simulacion de xlnets...
 */
@NoArgsConstructor
public class XLNetsAuthProviderForFileTokenLogin 
     extends XLNetsAuthProviderBase {
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor en base a la request
     */
    public XLNetsAuthProviderForFileTokenLogin(final HttpServletRequest theReq) {
        super(theReq);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  IMPLEMENTACIÓN DE LOS METODOS ABSTRACTOS DE XLNetsAuthProviderBase
///////////////////////////////////////////////////////////////////////////////////////////
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE XLNetsAuthProviderBase
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public XLNetsUserSession getXLNetsSession() {
        XLNetsUserSession session = null;

        // Ver si existe session (aqui se comprueban las cookies, etc)
        // En este caso como se toma todo de un fichero, simplemente, abrir el
        // fichero xml con la sesion y cargar los datos
    	try {
	        Document doc = XMLUtils.parse(new Path(_providerDef.getInitParameter("xmlsDir"),
	        								  	   _providerDef.getInitParameter("sessionFileName")));
	
	    	
	    	// [1] - Obtener la información de sesión
	        session = new XLNetsUserSession(doc.getDocumentElement());
	    	
	    	// [2] - Añadir información de usuario
	    	//Document docUser = n38API.n38ItemObtenerPersonas("uid=" + session.getUser().getPersona());
	    	//session.setUserInfo(docUser);
	    	
	        // [3] - Atributos de la sesion
	        Map<String,String> authAttrs = new HashMap<String,String>();
	        authAttrs.put("provider","fileProvider");	// Por poner algo... no es necesario
    	} catch (SAXException saxEx) {
    		saxEx.printStackTrace(System.out);
    	}
    	return session;
    }
}
