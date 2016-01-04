package r01f.model.latinia;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * Resultado de una petición LatiniaRequest, en la petición se han podido enviar múltiples mensajes, cada uno a X teléfonos.
 *
 * (un sms se ha podido enviar a n números de teléfono)
 *
 * Ejemplo (de documentación Latinia):
 * <PETICION>
 * 	<MENSAJE NUM="1">
 * 		<TELEFONO NUM="659000001">
 * 			<RESULTADO>OK</RESULTADO>
 * 			<IDENTIFICADOR>UGsiZ7E1naZX/Uey32A1hFUq</IDENTIFICADOR>
 * 		</TELEFONO>
 * 		<TELEFONO NUM="666000001">
 * 			<RESULTADO>OK</RESULTADO>
 * 			<IDENTIFICADOR>UGsiZ7E2efSshUey32A1mU7o</IDENTIFICADOR>
 * 		</TELEFONO>
 * 		<TELEFONO NUM="600123456">
 * 			<RESULTADO>ERROR</RESULTADO>
 * 			<CODIGO_ERROR>301</CODIGO_ERROR>
 * 			<MENSAJE_ERROR>El mensaje ha expirado</MENSAJE_ERROR>
 * 		</TELEFONO>
 * 	</MENSAJE>
 *  <MENSAJE NUM="1">
 *  ...........
 *  </MENSAJE>
 * </PETICION>
 *
 * MENSAJE: Si un mensaje es enviado a varios números de teléfono nos devuelve una lista con el resultado de cada uno de ellos.
 * NUM: número del mensaje que se ha enviado.
 * - TELEFONO: Mensaje que se ha enviado.
 * - NUM: Número de teléfono al que se ha enviado el mensaje.
 * - RESULTADO: OK o ERROR. Si el mensaje se ha entregado bien se devuelve un OK. Si ha sucedido algún error se devuelve ERROR.
 * - IDENTIFICADOR: Si el resultado ha sido OK, se devuelve el identificador único del mensaje, id interno del mensaje en la plataforma
 *                  (para la Q68) y en identificador externo, no único, en el caso de las aplicaciones no Q68. En este último caso
 *                  Identificador + Num de teléfono será la manera de identificar cada sms enviado.
 * - CODIGO_ERROR: Código asignado al error que se ha producido.
 * - MENSAJE_ERROR: Descripción del error que se ha generado.
 */
@XmlRootElement(name="RESPUESTA")
@Accessors(prefix="_")
@NoArgsConstructor
public class LatiniaResponse
	implements LatiniaObject {

	private static final long serialVersionUID = -3636899320335998954L;

/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlValue
	@Getter @Setter private List<LatiniaResponseMessage> _latiniaResponses;

}
