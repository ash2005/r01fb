/*
 * Created on 09-jul-2004
 *
 * @author IE00165H
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets.servlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import r01f.ejie.xlnets.servlet.XLNetsTargetCfg.ResourceCfg;
import r01f.guids.CommonOIDs.AppCode;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.locale.LanguageTextsBuilder;
import r01f.locale.Languages;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;
import r01f.util.types.collections.CollectionUtils;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForApp;
import r01f.xmlproperties.XMLPropertyWrapper;

/**
 * Modela la informacion de autorizacion de una aplicacion
 */
@Accessors(prefix="_")
@Slf4j
     class XLNetsAppCfg 
implements Serializable{
    private static final long serialVersionUID = -4853237050490550353L;

///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
    @Getter private AppCode _appCode = AppCode.forId("r01f");		// Código de la aplicacion
    @Getter private boolean _override = false;		// Indica si hay que permitir por narices...
    @Getter private boolean _useSession = true;		// Indica si la información de autorizacion se guarda en session
    @Getter private Map<String,ProviderDef> _providers = null;		// Configuracion de los provider de autorizacion
    @Getter private Map<String,XLNetsTargetCfg> _targets = null;	// Recursos que se protegen.
    																// Cada recurso se indexa por una expresion regular que ha de machear la
    																// uri que se solicita
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR A PARTIR DEL CÓDIGO DE APLICACION
/////////////////////////////////////////////////////////////////////////////////////////
    public XLNetsAppCfg(final AppCode appCode) {
    	this.loadConfig(appCode);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  GET & SET
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Añade un nuevo provider
     * @param theProvider
     */
    public void addNewProvider(final ProviderDef theProvider) {
        if(_providers == null) {
            Map<String,ProviderDef> newProviders = new HashMap<String,ProviderDef>();
            newProviders.put(theProvider.getIdProvider(), theProvider);
            _providers = newProviders;
        } else {
            _providers.put(theProvider.getIdProvider(),theProvider);
        }
    }
    /**
     * Devuelve el provider cuyo id se pasa como parámetro
     * @param providerId
     * @return
     */
    public ProviderDef getProvider(final String providerId) {
    	return CollectionUtils.hasData(_providers) ? _providers.get(providerId)
    											   : null;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  CARGA DE CONFIGURACIÓN
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Carga la configuración de seguridad de una aplicación. Para ello lee en el
     * fichero [codApp].properties.xml la sección <auth>
     * @param theAppCode Código de aplicación
     */
    public void loadConfig(final AppCode theAppCode) {
        log.debug("[XLNetsAuth]-Cargando la informacion de autorizacion del fichero {}.properties.xml",theAppCode);

        Node appAuthDefNode = (new XLNetsAuthProperties()).propertyAt(theAppCode,"/authCfg")
        										  		  .node();

        if (appAuthDefNode == null) {
            log.warn("[XLNetsAuth]-NO se puede cargar la información de seguridad de la aplicación {} ya que no existe la configuración en el fichero {}.xlnets.properties.xml",
            		 _appCode,_appCode);
            return;
        } 
        log.trace("[XLNetsAuth]-Cargando parámetros de seguridad de la aplicacion {}: {}",
	       		  _appCode,appAuthDefNode);

        _appCode = theAppCode;

        //_______Interpretar los datos de las propiedades________
        NamedNodeMap attrs = appAuthDefNode.getAttributes();
        Node currAttrNode = null;
        String currAttrValue = null;

        // Atributo override que indica si se ignora la configuración de XLNets
        if (attrs != null && (currAttrNode = attrs.getNamedItem("override")) != null) {
            currAttrValue = currAttrNode.getNodeValue().trim();
            if (currAttrValue.length() > 0) {
                if ("true".equals(currAttrValue)) {
                    _override = true;
                } else if ("false".equals(currAttrValue)) {
                    _override = false;
                }
            }
        }
        // Atributo useSession que indica si se utiliza la session http para guardar la informacion
        // de autorizacion una vez obtenida
        if (attrs != null && (currAttrNode = attrs.getNamedItem("useSession")) != null) {
            currAttrValue = currAttrNode.getNodeValue().trim();
            if ("true".equals(currAttrValue)) {
                _useSession = true;
            } else if ("false".equals(currAttrValue)) {
                _useSession = false;
            }
        }

        log.trace("[XLNetsAuth]-Cargando configuracion de seguridad de la aplicacion '{}': override={}",
        		  _appCode,_override);

        // Provider de autorizacion y Targets a los que se aplica la autorización
        NodeList rl = appAuthDefNode.getChildNodes();
        if (rl != null) {
            Node node = null;
            for (int i=0; i<rl.getLength(); i++) {
                node = rl.item(i);
                // ________________ PROVIDER _______________
                // Provider: className y Parametros de inicializacion: clase que se encarga de conectar con el
                // provider de autorización para obtener las autorizaciones
                if (node.getNodeName().equals("provider")) {
                    ProviderDef currProvider = this.new ProviderDef();
                    //Leemos el id del provider
                    if (node.getAttributes() != null && (currAttrNode = node.getAttributes().getNamedItem("id")) != null) {
                        currProvider.setIdProvider( node.getAttributes().getNamedItem("id").getNodeValue() );
                    }else {
                        // Si no tiene Id se considera que es un provider de usuario
                        currProvider.setIdProvider("userProvider");
                    }
                    NodeList nl = node.getChildNodes();
                    if (nl != null) {
                        Node currNode = null;
                        for (int j=0; j<nl.getLength(); j++) {
                            currNode = nl.item(j);
                            if (currNode.getNodeName().equals("className") && currNode.getFirstChild() != null) {
                                currProvider.setTypeName( currNode.getFirstChild().getNodeValue() );
                            } else if (currNode.getNodeName().equals("initParam")
                                    && currNode.getAttributes() != null
                                    && currNode.getAttributes().getNamedItem("name") != null
                                    && currNode.getFirstChild() != null) {
                                if (CollectionUtils.isNullOrEmpty(currProvider.getInitParams())) currProvider.setInitParams(new Properties());
                                currProvider.getInitParams().setProperty(currNode.getAttributes().getNamedItem("name").getNodeValue(),
                                                                         currNode.getFirstChild().getNodeValue());
                            } else if (currNode.getNodeName().equals("loginPage") 
                            	    && currNode.getFirstChild() != null) {
                                currProvider.setLoginPage(currNode.getFirstChild().getNodeValue());
                            }
                        }
                    }
                    this.addNewProvider(currProvider);
                }
                // ________________ TARGET _________________
                if (node.getNodeName().equals("target")) {
                    log.trace("[XLNetsAuth]-Nueva configuracion de seguridad para un target");
                    XLNetsTargetCfg currTargetCfg = new XLNetsTargetCfg();  // Configuración de seguridad del target actual

                    attrs = node.getAttributes();
                    // Atributo id
                    //if (attrs != null && (currAttrNode = attrs.getNamedItem("id")) != null)
                    //    currTargetCfg.id = currAttrNode.getNodeValue().trim();

                    // Atributo kind
                    if (attrs != null && (currAttrNode = attrs.getNamedItem("kind")) != null) {
                        String kindStr = currAttrNode.getNodeValue();
                        log.trace("\n\n\n\n$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$ kind={}\n\n\n\n",
                        		  kindStr);
                        currTargetCfg.setKind(XLNetsTargetCfg.ResourceAccess.fromName(kindStr));
                        if (currTargetCfg.getKind() == null) log.warn("[XLNetsAuth]-El valor '{}' del atributo kind de la configuración del target de acceso NO es valido.\r\nLos únicos valores válidos son {}",
                            		 								  kindStr,XLNetsTargetCfg.ResourceAccess.values());
                    }

                    NodeList nl = node.getChildNodes();
                    if (nl != null) {
                        Node currNode = null;
                        for (int j=0; j<nl.getLength(); j++) {
                            currNode = nl.item(j);

                            if (currNode.getNodeName().equals("uri")) {
                                currTargetCfg.setUriPattern(currNode.getFirstChild().getNodeValue());
                            } else if (currNode.getNodeName().equals("resources")) {
                                // Los "hijos" de <resources> son los nombres de los recursos que hay que comprobar
                                NodeList il = currNode.getChildNodes();
                                if (il != null) {
                                    Node currResNode = null;
                                    NamedNodeMap currResAttrs = null;
                                    NodeList bl = null;
                                    String itemOID = null;
                                    String itemMandatory = "false";
                                    String itemType = null;
                                    LanguageTexts itemName = null;
                                    
                                    for (int k=0; k<il.getLength(); k++) {
                                        currResNode = il.item(k);
                                        currResAttrs = currResNode.getAttributes();
                                        bl = currResNode.getChildNodes();
                                        if (currResNode.getNodeName().equals("resource")
                                         &&  bl != null && bl.getLength() > 0 && currResAttrs != null
                                         &&  currResAttrs.getNamedItem("oid") != null
                                         &&  currResAttrs.getNamedItem("type") != null) {
                                            // El oid, tipo y si el item es obligatorio o no
                                            itemOID = currResAttrs.getNamedItem("oid").getNodeValue();
                                            itemType = currResAttrs.getNamedItem("type").getNodeValue();
                                            itemMandatory = (currResAttrs.getNamedItem("mandatory") == null ? "false"
                                            																: currResAttrs.getNamedItem("mandatory").getNodeValue());
                                            itemName = LanguageTextsBuilder.createMapBacked()
                                            							   .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
                                            							   .build();
                                            // El nombre del item en diferentes idiomas
                                            for (int c=0; c<bl.getLength(); c++) {
                                            	Language lang = Languages.of(bl.item(c).getNodeName().toLowerCase());
                                            	if (lang != null &&  bl.item(c).getFirstChild() != null) {
                                                    itemName.add(lang,
                                                    			 bl.item(c).getFirstChild().getNodeValue().trim());
                                            	}                                            	
                                            }
                                            // Añadir la configuración del recurso a la configuracion del target
                                            if (itemType != null && itemName != null) {
                                                if(currTargetCfg.getResources() == null) currTargetCfg.setResources(new HashMap<String,ResourceCfg>());
                                                XLNetsTargetCfg.ResourceCfg resCfg = currTargetCfg.new ResourceCfg(itemOID.trim(),itemType.trim(),
                                                																 (itemMandatory.equals("true") ? true:false),
                                                																 itemName);
                                                currTargetCfg.getResources().put(resCfg.getOid(),resCfg);
                                            }
                                        } else {
                                            //log.warn("Un item de seguridad no esta correctamente configurado!!!");
                                        }
                                    }
                                }
                            }
                        }
                    } // Fin del propiedades del target

                    // Meter en nuevo target en el mapa de configuración de targets de la aplicación,
                    // identificado por el patrón de la url
                    if (_targets == null) _targets = new HashMap<String,XLNetsTargetCfg>();
                    _targets.put(currTargetCfg.getUriPattern(),currTargetCfg);

                } // Fin del recurso
            }
        } // Fin de la lista de recursos
        log.trace("[XLNetsAuth]-Fin de la carga de configuracion de seguridad");
    }
    @Override
    public String toString() {
        StringExtended sb = Strings.create(77);
        sb.addCustomized("[XLNetsAuth]-Configuracion de seguridad de la aplicacion {}; useSession={}; override={}",
        				 _appCode,_useSession,_override);
        if (CollectionUtils.hasData(_providers)) {
            for (ProviderDef p : _providers.values()) sb.add( p.toString() );
        }
        if (CollectionUtils.hasData(_targets)) {
            for (Iterator<XLNetsTargetCfg> it = _targets.values().iterator(); it.hasNext(); ) {
                sb.add( it.next().toString() );
            }
        }
        return sb.toString();
    }

///////////////////////////////////////////////////////////////////////////////////////////
//  INNER CLASS QUE REPRESENTA UN PROVIDER DE AUTORIZACION
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	* Item perteneciente al recurso
	*/
    @Accessors(prefix="_")
    @NoArgsConstructor
	public class ProviderDef {
	    @Getter @Setter private String _typeName = null;		// Nombre de la clase que hace de provider
        @Getter @Setter private String _loginPage = null;       // Página de login
	    @Getter @Setter private Properties _initParams = null;	// Parametros de inicializacion del provider
	    @Getter @Setter private String _idProvider = null;    	//Identificador del provider
		/**
		 * Devuelve un parámetro de inicio del provider
		 * @param paramName: El nombre del parametro
		 * @return: El valor del parametro
		 */
		public String getInitParameter(final String paramName) {
		    if (_initParams == null) return null;
		    return (String)_initParams.get(paramName);
		}
		@Override
		public String toString() {
		    StringExtended sb = Strings.create(63);
		    sb.add("\tProvider:");
		    sb.addCustomizedIfParamNotNull("\n\t\tidProvider={}",_idProvider);
		    sb.addCustomizedIfParamNotNull("\n\t\t     class={}",_typeName);
		    sb.addCustomizedIfParamNotNull("\n\t\t LoginPage={}",(_loginPage != null ? _loginPage
		    																		 : "not configured...it's asumed the r01f login page"));
		    if (_initParams != null) {
		        sb.add("\t\tInitParams:\r\n");
		        Map.Entry<Object,Object> me = null;
		        for (Iterator<Map.Entry<Object,Object>> it = _initParams.entrySet().iterator(); it.hasNext(); ) {
		            me = it.next();
		            sb.addCustomized("\t\t\t{}={}",me.getKey(),me.getValue());
		        }
		    }
		    return sb.toString();
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
// ACCESO A PROPIEDADES
/////////////////////////////////////////////////////////////////////////////////////////
	class XLNetsAuthProperties {
		/**
		 * Develve una propiedad de seguridad en un punto XPath
		 * @param appCode código de aplicación
		 * @param xPath la ruta xPath
		 * @return un wrapper a la propiedad que permite recuperarla en diferentes formatos
		 */
		XMLPropertyWrapper propertyAt(final AppCode appCode,final String xPath) {
			XMLPropertiesForApp props = XMLProperties.createForApp(appCode)
													 .notUsingCache();
			XMLPropertyWrapper prop = new XMLPropertyWrapper(props.of("xlnets"),	// componente xlNets
															 xPath);				// ruta de la propiedad deseada
			return prop;
		}
		/**
		 * Devuelve una propiedad en un punto XPath
		 * @param xPathWithPlaceHolders una sentencia XPath con "placeholders" que se sustituyen por variables
		 * @param vars las variables
		 * @return un wrapper a la propiedad que permite recuperarla en diferentes formatos
		 */
		XMLPropertyWrapper propertyAt(final AppCode appCode,final String xPathWithPlaceHolders,final String... vars) {
			String theXPath = Strings.of(xPathWithPlaceHolders).customizeWith(vars).asString();
			return this.propertyAt(appCode,theXPath);
		}
	}
}
