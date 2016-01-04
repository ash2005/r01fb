/*
 * Created on 09-jul-2004
 *
 * @author IE00165H (Alex Lara Garatxana)
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
import r01f.util.types.collections.CollectionUtils;


/**
 * Contiene todo el contexto de seguridad que construye el filtro de autorización
 * y que se pasa al recurso protegido.
 * En este objeto se almacenan los siguientes datos:
 * <pre>
 * 		- Atributos del contexto de autorizacion
 * 		- Perfiles del usuario
 * 		- Información de autorización al recurso
 * </pre>
 */
@Accessors(prefix="_")
@NoArgsConstructor @AllArgsConstructor
    class XLNetsAuthCtx 
implements Serializable {
	
	private static final long serialVersionUID = 5697699783433808308L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
    @Getter @Setter private Map<String,XLNetsTargetCtx> _authorizedTargets = null;	// Targets a los que se ha comprobado el acceso
    																				// indexados por el patron de la URI

///////////////////////////////////////////////////////////////////////////////////////////
//  GET & SET
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Obtiene la configuracion de seguridad del destino en base a la URI
	 * que se está solicitando, para lo cual intenta "casar" esta URI con las
	 * configuraciones de seguridad del usuario y que se han obtenido del fichero
	 * de propiedades y cargado en el filtro de seguridad
	 * @param uriPattern El partón de la uri para el destino
	 * @return La configuración de seguridad si la uri verifica alguno de los
	 * 		   patrones configurados en el fichero properties.
	 * 		   Null si la uri no "casa" con ninguno de los patrones del
	 *         fichero properties
	 */
	public XLNetsTargetCtx getTargetAuth(final String uriPattern) {
	    return CollectionUtils.hasData(_authorizedTargets) ? _authorizedTargets.get(uriPattern)
	    												   : null;
	}

///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE COMPROBACIÓN DE LA VALIDEZ
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Comprueba si el objeto es valido
     * @return true si el objeto es valido y false si no es asín
     */
    public boolean isValid() {
        // Cuidado!!! Si override = true es posible que no haya profiles ni recursos
        if (CollectionUtils.isNullOrEmpty(_authorizedTargets)) return false;
        for (XLNetsTargetCtx target : _authorizedTargets.values()) {
            if ( !target.isValid() ) return false;
        }
        return true;
    }
}
