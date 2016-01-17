/*
 * Created on 11-ago-2004
 * 
 * @author ie00165h
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.locale.LanguageTextsBuilder;
import r01f.util.types.Strings;



/**
 * Modela un item de seguridad.
 */
@Accessors(prefix="_")
public class XLNetsItemSeguridad
     extends XLNetsObjectBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  SENTENCIAS XPath
/////////////////////////////////////////////////////////////////////////////////////////
    private static final String UID = "parametro[@id='n38uidobjseguridad']/valor[1]/text()";
    private static final String CN = "parametro[@id='cn']/valor[1]/text()";
    private static final String PATH_EJECUTABLE = "parametro[@id='n38path-ejecutable']/valor[1]/text()";
    private static final String DESCRIPCION_ES = "parametro[@id='n38cadescripcion']/valor[1]/text()";
    private static final String DESCRIPCION_EU = "parametro[@id='n38eudescripcion']/valor[1]/text()";    
    private static final String URL = "parametro[@id='url']/valor[1]/text()";
    private static final String TIPO = "parametro[@id='n38tipo']/valor[1]/text()";
    private static final String SUBTIPO = "parametro[@id='n38subtipo']/valor[1]/text()";
    private static final String AUTORIZACIONES = "elemento[@subtipo='n38autorizacion']";
    
    private static final String ACCIONES = "parametro[@id='n38acciones']/valor"; 
    private static final String PERFILES = "parametro[@id='n38perfiles']/valor";
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final String _UID;
    @Getter private final String _commonName;
    @Getter private final String _pathEjecutable;
    @Getter private final LanguageTexts _descripcion;
    @Getter private final String _tipo;
    @Getter private final String _subTipo;
    @Getter private final String[] _acciones;
    @Getter private final String[] _perfiles;
    @Getter private final XLNetsAutorizacion[] _autorizaciones;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
    public XLNetsItemSeguridad(Node itemSeguridadNode) {
        super();
        _UID = _extractValue(itemSeguridadNode,UID); 
        _commonName = _extractValue(itemSeguridadNode,CN); 
    	_pathEjecutable = _extractValue(itemSeguridadNode,PATH_EJECUTABLE); 
    	_descripcion = LanguageTextsBuilder.createMapBacked()
    									   .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
		    							   .addForLang(Language.SPANISH,_extractValue(itemSeguridadNode,DESCRIPCION_ES))
		    							   .addForLang(Language.BASQUE,_extractValue(itemSeguridadNode,DESCRIPCION_EU))
    									   .build();
    	_tipo = _extractValue(itemSeguridadNode,TIPO);
    	_subTipo = _extractValue(itemSeguridadNode,SUBTIPO);
    	_acciones = _extractMultipleValue(itemSeguridadNode,ACCIONES); 
    	_perfiles = _extractMultipleValue(itemSeguridadNode,PERFILES);
    	_autorizaciones = _autorizaciones(itemSeguridadNode);
    }
    /** 
     * Devuelve las autorizaciones asociadas con este tipo de objeto
     */
    private static XLNetsAutorizacion[] _autorizaciones(final Node node) {
        XLNetsAutorizacion[] outAuths = null;        
        NodeList nl = _executeXPathForNodeList(node,AUTORIZACIONES);
        if (nl != null) {
            outAuths = new XLNetsAutorizacion[nl.getLength()]; 
            for (int i=0; i<nl.getLength(); i++) outAuths[i] = new XLNetsAutorizacion(nl.item(i));
        }
        return outAuths;        
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  OVERRIDE
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
    	String dbg = Strings.create()
    						.addCustomizedIfParamNotNull("            UID: {}\n",this.getUID())
    						.addCustomizedIfParamNotNull("             CN: {}\n",this.getCommonName())
    						.addCustomizedIfParamNotNull("    Descripcion: {}\n",this.getDescripcion())
    						.addCustomizedIfParamNotNull("           Tipo: {}\n",this.getTipo())
    						.addCustomizedIfParamNotNull("        SubTipo: {}\n",this.getSubTipo())
    						.addCustomizedIfParamNotNull("Path-Ejecutable: {}\n",this.getPathEjecutable())
    						.addCustomizedIfParamNotNull("       Acciones: {}\n",this.getAcciones())
    						.addCustomizedIfParamNotNull("       Perfiles: {}\n",this.getPerfiles())
    						.addCustomizedIfParamNotNull(" Autorizaciones:\n{}\n",this.getAutorizaciones())
    						.asString();
        return dbg;
    }
}
