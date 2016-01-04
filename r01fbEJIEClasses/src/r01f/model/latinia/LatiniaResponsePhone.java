package r01f.model.latinia;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <?xml version="1.0" encoding="UTF-8"?>
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
 * <MENSAJE NUM="2">
 * ........
 * </MENSAJE>
 * </PETICION>
 */
@XmlRootElement(name="TELEFONO")
@Accessors(prefix="_")
public class LatiniaResponsePhone
  implements LatiniaObject {

	private static final long serialVersionUID = 350623609498625826L;

/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////

	@XmlAttribute(name="NUM")
	@Getter @Setter private String _receiverNumber; // If delivery notification is requested (S/N).

	@XmlElement(name="RESULTADO")
	@Getter @Setter private String _result;      // Message response of state (OK/ERROR)

	@XmlElement(name="IDENTIFICADOR")
	@Getter @Setter private String _messageId;      // Identifier (qwerty...)

	@XmlElement(name="CODIGO_ERROR")
	@Getter @Setter private String _errorCode;      // Error code (301)

	@XmlElement(name="MENSAJE_ERROR")
	@Getter @Setter private String _errorMessage;   // Error message (for example "El mensaje ha expirado")

}
