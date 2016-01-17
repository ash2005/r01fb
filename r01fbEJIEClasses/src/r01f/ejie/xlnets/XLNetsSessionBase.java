/*
 * Abstrae una sesión de aplicación
 */
package r01f.ejie.xlnets;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.w3c.dom.Node;

import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTextsBuilder;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Lee un documento xml con la informacion de XLNets de sesion
 */
@Accessors(prefix="_")
public abstract class XLNetsSessionBase 
              extends XLNetsObjectBase 
           implements XLNetsSession {
/////////////////////////////////////////////////////////////////////////////////////////
//  SENTENCIAS XPath
/////////////////////////////////////////////////////////////////////////////////////////
	// XML de sesión
    private static transient final String UIDSESSION = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38uidsesion']/valor[1]/text()";
    private static transient final String DNI = "/elemento[@subtipo='N38Sesion']/parametro[@id='dni']/valor[1]/text()";
    private static transient final String LOGIN = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38login']/valor[1]/text()";
    private static transient final String PERSONA = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38personauid']/valor[1]/text()";
    private static transient final String PUESTO = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38puestouid']/valor[1]/text()";
    private static transient final String IDIOMA = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38idioma']/valor[1]/text()";    
    private static transient final String IP = "/elemento[@subtipo='N38Sesion']/parametro[@id='iphostnumber']/valor[1]/text()";
    private static transient final String LOGINAPP = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38loginasociado']/valor[1]/text()";
    private static transient final String HOME = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38urlfinal']/valor[1]/text()";    
    private static transient final String ORGANIZACION = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38organizacion']/valor[1]/text()";
    private static transient final String GRUPO = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38grupoorganicouid']/valor[1]/text()";
    private static transient final String UNIDAD = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38orgunituid']/valor[1]/text()";    
    private static transient final String PERFILES = "/elemento[@subtipo='N38Sesion']/parametro[@id='n38perfiles']/valor";
    
    // XML de usuario
    private static transient final String DISPLAYNAME = "/n38/elementos[@tipo='n38ItemObtenerPersonas']/elemento[@subtipo='n38persona']/parametro[@id='displayname']/valor[1]/text()";
    
    protected abstract String _UIDSESSION();
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final Date _loginDate = new Date();   // Fecha de login
    @Getter private final String _sessionUID;
    @Getter private final XLNetsUser _user;
    @Getter private final XLNetsOrgNode _organizacion;
    @Getter private final XLNetsOrgNode _grupo;
    @Getter private final XLNetsOrgNode _unidad;
    @Getter private final Map<String,XLNetsProfile> _perfiles;
    @Getter private 	  Map<String,String> _attributes;
///////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////    
    public XLNetsSessionBase(final Node sessionNode) {
        super();
        
        String xPathBase = _UIDSESSION();
        
        _sessionUID = _extractValue(sessionNode,xPathBase + UIDSESSION);
        
        // Datos del usuario
        String login = _extractValue(sessionNode,xPathBase + LOGIN);
        String loginApp = _extractValue(sessionNode,xPathBase + LOGINAPP);
        String persona = _extractValue(sessionNode,xPathBase + PERSONA);
        String puesto = _extractValue(sessionNode,xPathBase + PUESTO);
        String dni = _extractValue(sessionNode,xPathBase + DNI);
        String home = _extractValue(sessionNode,xPathBase + HOME);
        String idioma = _extractValue(sessionNode,xPathBase + IDIOMA);
        String ip = _extractValue(sessionNode,xPathBase + IP);
        
        Boolean isLoginApp = loginApp != null && loginApp.equalsIgnoreCase("TRUE") ? true : false;
        Language lang = idioma != null && idioma.equals("3") ? Language.SPANISH : Language.BASQUE;
        
        _user = new XLNetsUser(login,isLoginApp,
        					   login,persona,puesto,
        					   null,dni,home,lang,
        					   ip,
        					   null);
        
        // Organizacion del usuario
        String organizacion = _extractValue(sessionNode,xPathBase + ORGANIZACION); 
        String grupo = _extractValue(sessionNode,xPathBase + GRUPO);
        String unidad = _extractValue(sessionNode,xPathBase + UNIDAD);
        _organizacion = new XLNetsOrgNode(organizacion,
        								  organizacion,organizacion,
        								  null);
        _grupo = new XLNetsOrgNode(grupo,
        						   grupo,grupo,
        						   _organizacion);		// apunta al padre
        _unidad = new XLNetsOrgNode(unidad,
        							unidad,unidad,
        							_grupo);			// apunta al padre
        
        // Perfiles
        String[] perfiles = _extractMultipleValue(sessionNode,xPathBase + PERFILES);
        if (CollectionUtils.hasData(perfiles)) {
        	_perfiles = new HashMap<String,XLNetsProfile>(perfiles.length);
        	for (String perfil : perfiles) {
        		XLNetsProfile profile = new XLNetsProfile(perfil,
        												  LanguageTextsBuilder.createMapBacked()
        												  					  .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
	        												  			      .addForLang(Language.SPANISH,perfil)
	        												  			      .addForLang(Language.BASQUE,perfil)
        																	  .build());
        		_perfiles.put(perfil,profile);
        	}
        } else {
        	_perfiles = null;
        }
    }
    @Override
	public void setUserInfo(final Node userNode) {
    	String displayName = null;
        if (userNode != null) {
	    	String dispName = _extractValue(userNode,DISPLAYNAME);
    		displayName = Strings.isNullOrEmpty(dispName) ? "unknown" : dispName;
        } else {
        	displayName = "unknown";
        }
        _user.setName(displayName);
    }
    @Override
	public boolean userBelongsTo(final String orgOID) {
        if (_organizacion == null) return false;
        if (_organizacion.getOid().equals(orgOID)) return true;
        if (_organizacion.isSubOrgOf(orgOID)) return true;
        if (_organizacion.isSupraOrgOf(orgOID)) return true;
        return false; // No!
    }
    @Override
    public XLNetsProfile getPerfil(String profileOID) {
    	return CollectionUtils.hasData(_perfiles) ? _perfiles.get(profileOID)
    											  : null;
    }
    @Override
	public boolean hasProfile(final String profileOID) {
        return CollectionUtils.hasData(_perfiles) ? _perfiles.containsKey(profileOID) 
        										  : null;
    }
    @Override
	public String getAttribute(final String attrName) {
        return CollectionUtils.hasData(_attributes) ? _attributes.get(attrName)
        										    : null;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String toString() {
		String userDbg = Strings.create()
								  .addCustomizedIfParamNotNull(" UID Session: {}\n",this.getSessionUID())
								  .addCustomizedIfParamNotNull("{}",this.getUser())
								  .addCustomizedIfParamNotNull("Organizacion: {}\n",this.getOrganizacion())
								  .addCustomizedIfParamNotNull("       Grupo: {}\n",this.getGrupo())
								  .addCustomizedIfParamNotNull("      Unidad: {}\n",this.getUnidad())
								  .addCustomizedIfParamNotNull("    Perfiles: {}\n",this.getPerfiles())
								.asString();
	    return userDbg;
	}
}
