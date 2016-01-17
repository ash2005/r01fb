package r01f.model.latinia;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

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
@XmlRootElement(name="MENSAJE")
@Accessors(prefix="_")
public class LatiniaResponseMessage
  implements LatiniaObject {

	private static final long serialVersionUID = 4262827727118295752L;

/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////

	@XmlAttribute(name="NUM")
	@Getter @Setter private String _receiverNumber;

	@XmlValue
	@Getter @Setter private List<LatiniaResponsePhone> _responsePhoneResults;
}
