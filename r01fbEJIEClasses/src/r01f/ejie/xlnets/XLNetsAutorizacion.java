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

import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTextsBuilder;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.util.types.Strings;


/**
 * Modela un item de seguridad.
 */
@Accessors(prefix="_")
public class XLNetsAutorizacion 
     extends XLNetsObjectBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
/////////////////////////////////////////////////////////////////////////////////////////
    private static transient final String CN = "parametro[@id='cn']/valor[1]/text()";
    private static transient final String DESCRIPCION_ES = "parametro[@id='n38cadescripcion']/valor[1]/text()";
    private static transient final String DESCRIPCION_EU = "parametro[@id='n38eudescripcion']/valor[1]/text()";    
    private static transient final String PERFIL = "parametro[@id='n38uidperfil']/valor[1]/text()";
    private static transient final String ACCIONES = "parametro[@id='n38acciones']/valor";
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final String _commonName; 
    @Getter private final LanguageTexts _descripcion;
    @Getter private final String _profileOid;
    @Getter private final String[] _acciones;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
    public XLNetsAutorizacion(final Node authNode) {
        super();
        // Obtener los valores del nodo
        _commonName = _extractValue(authNode,CN);
        _descripcion = LanguageTextsBuilder.createMapBacked()
        								   .withMissingLangTextBehavior(LangTextNotFoundBehabior.RETURN_NULL)
		        						   .addForLang(Language.SPANISH,_extractValue(authNode,DESCRIPCION_ES))
		    							   .addForLang(Language.BASQUE,_extractValue(authNode,DESCRIPCION_EU))
				    					   .build();
        _profileOid = _extractValue(authNode,PERFIL); 
        _acciones = _extractMultipleValue(authNode,ACCIONES); 
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  OVERRIDE
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
    	String dbg = Strings.of("\tAutorizacion:")
    						.addCustomizedIfParamNotNull("\t\t         CN: {}\n",this.getCommonName())
    						.addCustomizedIfParamNotNull("\t\tDescripcion: {}\n",this.getDescripcion())
    						.addCustomizedIfParamNotNull("\t\t     Perfil: {}\n",this.getProfileOid())
    						.addCustomizedIfParamNotNull("\t\t   Acciones: {}\n",this.getAcciones())
    						.asString();
        return dbg;
    }
}
