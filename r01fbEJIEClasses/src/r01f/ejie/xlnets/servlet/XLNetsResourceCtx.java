/*
 * Created on 26-jul-2004
 *
 * @author IE00165H
 * (c) 2004 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.ejie.xlnets.servlet;

import java.io.Serializable;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


/**
 * Item perteneciente al recurso autorizado
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
     class XLNetsResourceCtx 
implements Serializable {
    private static final long serialVersionUID = -1471890948119134239L;
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
    @Getter @Setter private String _parentOid = "rctx-unknown"; // oid del recurso
    @Getter @Setter private String _oid = "rctx-unknown";       // oid del recurso
    @Getter @Setter private LanguageTexts _name = null;			// descripcion
    @Getter @Setter private String _type = "rctx-unknown";      // tipo de recurso (objeto, funcion)
    @Getter @Setter private String _subtype = "rctx-unknown"; 	// subtipo. En el caso  de ser instancias de Objetos Portal indica el oid del Portal.
    @Getter @Setter private Map<String,XLNetsResourceAuthorization> _authorizations = null;		// autorizaciones

///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
//    public String toXML() throws XOMarshallerException {
//        return XOManager.getXML(XMLProperties.get("r01f","authMapPath"),this);
//    }
///////////////////////////////////////////////////////////////////////////////////////////
//  VALIDEZ
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Comprueba si el objeto es valido
     * @return true si el objeto es valido y false si no es asín
     */
    public boolean isValid() {
        if (Strings.isNullOrEmpty(_oid)) return false;
        if (_name == null || _name.getFor(Language.SPANISH) == null || _name.getFor(Language.BASQUE) == null) return false;
        if (CollectionUtils.isNullOrEmpty(_authorizations)) return false;
        for (XLNetsResourceAuthorization auth : _authorizations.values()) {
            if ( !auth.isValid() ) return false;
        }
        return true;
    }


}
