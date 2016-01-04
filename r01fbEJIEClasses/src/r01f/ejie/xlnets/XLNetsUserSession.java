/*
 * Created on 11-ago-2004
 * 
 * 
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets;

import lombok.experimental.Accessors;

import org.w3c.dom.Node;



/**
 * Lee un documento xml con la informacion de XLNets de sesion
 */
@Accessors(prefix="_")
public class XLNetsUserSession 
     extends XLNetsSessionBase {  

/////////////////////////////////////////////////////////////////////////////////////////
//  SENTENCIAS XPath
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
    protected String _UIDSESSION() {
    	return "/n38/elementos[@tipo='n38APISesionValida']";
    }
///////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////    
    public XLNetsUserSession(final Node sessionNode) {
    	super(sessionNode);
    }
}
