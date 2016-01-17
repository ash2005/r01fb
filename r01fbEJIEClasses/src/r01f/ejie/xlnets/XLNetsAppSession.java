/*
 * Abstrae una sesión de aplicación
 */
package r01f.ejie.xlnets;

import lombok.experimental.Accessors;

import org.w3c.dom.Node;

/**
 * Lee un documento xml con la informacion de XLNets de un usuario
 */
@Accessors(prefix="_")
public class XLNetsAppSession 
     extends XLNetsSessionBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  SENTENCIAS XPath
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
    protected String _UIDSESSION() {
    	return "/n38/elementos[@tipo='n38APISesionCrearToken']";
    }
///////////////////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////    
    public XLNetsAppSession(final Node sessionNode) {
    	super(sessionNode);
    }
}
