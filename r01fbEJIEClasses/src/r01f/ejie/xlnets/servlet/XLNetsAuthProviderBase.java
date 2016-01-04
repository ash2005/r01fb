/*
 * Created on 28-jul-2004
 *
 * @author ie00165h
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import n38c.exe.N38API;

import org.w3c.dom.Document;

import r01f.ejie.xlnets.XLNetsAutorizacion;
import r01f.ejie.xlnets.XLNetsFuncion;
import r01f.ejie.xlnets.XLNetsItemSeguridad;
import r01f.ejie.xlnets.XLNetsTipoObjeto;
import r01f.ejie.xlnets.XLNetsUserSession;
import r01f.ejie.xlnets.servlet.XLNetsAppCfg.ProviderDef;
import r01f.ejie.xlnets.servlet.XLNetsTargetCfg.ResourceCfg;
import r01f.util.types.collections.CollectionUtils;
/**
 * Clase base para el provider de autenticación. Actualmente implementan esta interface tres clases que definen los distintos
 * sistemas de seguridad:
 * <pre>
 * 1) XLNetsFileAuthProvider : Seguridad implementada a través de ficheros físicos, se usa para simular el acceso a XL-Nets en entornos locales.
 * 2) EHULdapAuthProvider 	 : Seguridad implementada en la infraestructura de la UPV.
 * 3) XLNetsAuthProvider	 : Seguridad implementada mediante el sistema XL-Nets del Gobierno, utilizada para accesos vía web.
 * 4) XLNetsAppAuthProvider  : Seguridad implementada mediante el sistema XL-Nets del Gobierno, utilizada para accesos vía cliente.
 * </pre> 
 */
@Accessors(prefix="_")
@NoArgsConstructor
@Slf4j
abstract class XLNetsAuthProviderBase implements XLNetsAuthProvider {
// /////////////////////////////////////////////////////////////////////////////////////////
// CONSTANTES
/////////////////////////////////////////////////////////////////////////////////////////// 
    /**
     * Clave de la propiedad que aloja el código de aplicación en el login de aplicación.
     */
    public static final String APP_CODE_PROPERTY = "appCode";
    
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
    @Getter @Setter ServletRequest _request = null; // Request
	@Getter	@Setter	ProviderDef _providerDef;	// Configuración de la aplicación
    @Getter @Setter String _providerId = null;		// Identificador del provider

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor en base a datos de la request
     * @param theReq la request
     */
    public XLNetsAuthProviderBase(final ServletRequest theReq) {
        _request = theReq;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS ABSTRACTOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * @param appCfg configuración de la aplicacion
     * @return la sesión XLNets
     */
    @Override
	public abstract XLNetsUserSession getXLNetsSession();
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Devuelve un contexto de seguridad que construye el filtro de autorización
     * Puede hacer login de Aplicación o de usuario en XLNets, en caso de hacer de usuario:
     * <ul>
     *      <li>Si el usuario se ha autenticado, devuelve un objeto con el contexto
     *        de la sesión</li>
     *      <li>Si el usuario no se ha autenticado devuelve null</li>
     * @param appCfg la configuración de la aplicación
     * @return Un objeto con el contexto o null si el usuario no se ha autenticado
     */
    @Override
	public XLNetsAuthCtx getAuthContext() {
        try {
        	XLNetsUserSession session = this.getXLNetsSession();
        	
	        // Comprobar si la sesión es válida.
	        if ( session.getSessionUID() != null ) {
		        // Crear el contexto de autorizacion sin recursos autorizados ya que estos se irán
		        // cargando a medida que el filtro llama a la funcion authorize
		        return new XLNetsAuthCtx();		// targets.... de momento nada... se va rellenando conforme se llama a authorize
			}
            return null;
        } catch (Exception ex) {
             log.error("[XLNetsAuth]-Error desconocido!!: {}",ex.getMessage(),ex);
        }
        return null;	// No se ha podido autenticar
    }
    /**
     * Redirige al usuario a la página de login
     * @param res la response
     * @param returnURL La url a la que ha de devolver al usuario la aplicación de login una vez
     *                   que este ha hecho login
     */
    @Override
	public void redirectToLogin(final ServletResponse res,final String returnURL) {
        try {
            // Redirigir a la pagina de login, pasando como parametro la url a la que hay que
            // volver después de hacer login
            ((HttpServletResponse)res).sendRedirect(returnURL);
        } catch (IOException ioEx) {
            ioEx.printStackTrace(System.out);
            log.warn("[XLNetsAuth]-No se ha podido redirigir a la pagina de login");
        }
    }
    /**
     * Consulta los datos de autorización del destino cuya configuracion
     * se pasa como parametro
     * @param authCtx contexto de autorización
     * @param targetCfg La configuracion del target
     * @return un objeto {@link XLNetsTargetCtx} con el contexto de autorizacion para el destino
     */
    @Override
	public XLNetsTargetCtx authorize(final XLNetsAuthCtx authCtx,
    								 final XLNetsTargetCfg targetCfg,
    								 final boolean override) {
        if (targetCfg == null || CollectionUtils.isNullOrEmpty(targetCfg.getResources())) return null;
        
        // Obtener del proveedor la autorizacion para cada item
        XLNetsTargetCtx targetCtx = null;							// Contexto del destino
        Map<String,XLNetsResourceCtx> authorizedResources = null;	// Recursos autorizados en el destino

        try {
	        for (ResourceCfg currResCfg : targetCfg.getResources().values()) {
	        	XLNetsResourceCtx resourceCtx = null;				// Recurso actual
	        	
	        	if (!override) {
		            // Obtener la informacion de autorizacion del recurso a partir del oid de la funcion
		            N38API miN38API = new N38API((HttpServletRequest)_request);
		            Document doc = null;

		            if (currResCfg.getType().equals("function")) {
		                doc = miN38API.n38ItemObtenerAutorizacion(currResCfg.getOid());
		                XLNetsFuncion func = new XLNetsFuncion(doc.getDocumentElement());
		                XLNetsItemSeguridad itemSeg = func.getItemSeguridad();

		                // Contexto del recurso
		                resourceCtx = new XLNetsResourceCtx(null,									// parent oid
		                									itemSeg.getUID(),						// oid
								  						    itemSeg.getDescripcion(),				// name
								  						    itemSeg.getTipo(),itemSeg.getSubTipo(),	// tipo / subtipo
								  						    _obtainItemAuths(authCtx,itemSeg));		// autorizaciones

			            // Meter el recurso autorizado en el contexto
			            if (authorizedResources == null) authorizedResources = new HashMap<String,XLNetsResourceCtx>();
			            authorizedResources.put(resourceCtx.getOid(),resourceCtx);

		            } else if (currResCfg.getType().equals("object")) {
		            	doc = miN38API.n38ItemSeguridad(currResCfg.getOid());

		                XLNetsTipoObjeto tipoObj = new XLNetsTipoObjeto(doc.getDocumentElement());
		                XLNetsItemSeguridad itemSeg = tipoObj.getItemSeguridad();
		                // Contexto del recurso
		                resourceCtx = new XLNetsResourceCtx(null,									// parent oid
		                									itemSeg.getUID(),						// oid
								  						    itemSeg.getDescripcion(),				// name
								  						    itemSeg.getTipo(),itemSeg.getSubTipo(),	// tipo / subtipo
								  						    _obtainItemAuths(authCtx,itemSeg));		// autorizaciones

			            // Meter el recurso autorizado en el contexto
			            if (authorizedResources == null) authorizedResources = new HashMap<String,XLNetsResourceCtx>();
			            authorizedResources.put(resourceCtx.getOid(),resourceCtx);

		                // A diferencia de una funcion, un tipo de objeto tiene instancias que tambien hay
		                // que meter como recursos
		                if (CollectionUtils.hasData(tipoObj.getInstances())) {
		                    for (int i=0; i<tipoObj.getInstances().length; i++) {
		                        itemSeg = tipoObj.getInstances()[i];
		    	                // Contexto del recurso
		    	                resourceCtx = new XLNetsResourceCtx(itemSeg.getUID(),
	                                                              tipoObj.getItemSeguridad().getUID(),
		    							  						  itemSeg.getDescripcion(),
		    							  						  itemSeg.getTipo(),itemSeg.getSubTipo(),
		    							  						  _obtainItemAuths(authCtx,itemSeg));
		        	            // Meter el recurso autorizado en el contexto
		        	            authorizedResources = new HashMap<String,XLNetsResourceCtx>();
		        	            authorizedResources.put(resourceCtx.getOid(),resourceCtx);
		                    }
		                }
		            }
	        	} else {
	        	    // Se hace override...
		            if (currResCfg.getType().equals("function")) {
		                // Contexto del recurso
		                resourceCtx = new XLNetsResourceCtx(null,						// parent oid
		                									currResCfg.getOid(),		// oid
								  						  	currResCfg.getName(),		// name
								  						  	currResCfg.getType(),null,	// tipo / subtipo
								  						  	null);						// autorizaciones

			            // Meter el recurso autorizado en el contexto
			            if (authorizedResources == null) authorizedResources = new HashMap<String,XLNetsResourceCtx>();
			            authorizedResources.put(resourceCtx.getOid(),resourceCtx);

		            } else if (currResCfg.getType().equals("object")) {
		                // Contexto del recurso
                        resourceCtx = new XLNetsResourceCtx(null,						// parent oid
                        									currResCfg.getOid(),		// oid
                        									currResCfg.getName(),		// name
                        									currResCfg.getType(),null,	// tipo / subtipo
                        									null);						// autorizaciones

			            // Meter el recurso autorizado en el contexto
			            if (authorizedResources == null) authorizedResources = new HashMap<String,XLNetsResourceCtx>();
			            authorizedResources.put(resourceCtx.getOid(),resourceCtx);
		            }
	        	}
                if (resourceCtx != null) log.trace("[XLNetsAuth]-Nuevo recurso: {}",resourceCtx.getOid());
	        } // del for...
        } catch (Exception saxEx) {
            log.error("[XLNetsAuth]-Error al parsear el documento xml de informacion de xlnets: {}" + saxEx.getMessage(),saxEx);
            return null;
        }
        // Devolver la autorizacion del recurso
        targetCtx = new XLNetsTargetCtx(targetCfg,authorizedResources);		// Contexto de autorizacion del recurso
        return targetCtx;
    }


///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene las autorizaciones del recurso
     * @param session contexto de autenticación
     * @param itemSeg item de seguridad
     * @return un mapa con objetos {@link R01FResourceAuthorization}
     */
    private static Map<String,XLNetsResourceAuthorization> _obtainItemAuths(final XLNetsAuthCtx authCtx,
    																		final XLNetsItemSeguridad itemSeg) {
        Map<String,XLNetsResourceAuthorization> outAuths = null;
        
        // Cada una de las autorizaciones del recurso
        if (CollectionUtils.hasData(itemSeg.getAutorizaciones())) {
            outAuths = new HashMap<String,XLNetsResourceAuthorization>(itemSeg.getAutorizaciones().length);
            for (XLNetsAutorizacion auth : itemSeg.getAutorizaciones()) {
            	XLNetsResourceAuthorization currAuth = new XLNetsResourceAuthorization(auth.getCommonName(),
                        								 							   auth.getDescripcion(),
                        								 							   auth.getAcciones(),
                        								 							   auth.getProfileOid());
                outAuths.put(currAuth.getOid(),currAuth);
            }
        }
        return outAuths;
    }

}
