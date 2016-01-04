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

import r01f.util.types.Strings;



/**
 * Modela un tipo de objeto xlnets.
 */
@Accessors(prefix="_")
public class XLNetsFuncion 
     extends XLNetsObjectBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  SENTENCIAS XPath
/////////////////////////////////////////////////////////////////////////////////////////
    private static transient final String ITEM_SEGURIDAD = "/n38/elementos[@tipo='n38ItemObtenerAutorizacion']/elemento[@subtipo='n38itemSeguridad']";
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final XLNetsItemSeguridad _itemSeguridad;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
    public XLNetsFuncion(Node nodeFuncion) {
        super();
        _itemSeguridad = new XLNetsItemSeguridad(_executeXPathForNode(nodeFuncion,ITEM_SEGURIDAD));
    }   
/////////////////////////////////////////////////////////////////////////////////////////
//  OVERRIDE
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String toString() {
        String dbg = Strings.create()
        					.addCustomizedIfParamNotNull(">>>Funcion______________________________\n{}\n",this.getItemSeguridad())
        					.asString();
        return dbg;
    }
}
