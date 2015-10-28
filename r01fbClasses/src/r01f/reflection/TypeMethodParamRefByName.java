package r01f.reflection;

import java.io.Serializable;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;

/**
 * Referencia a un parámetro de un método
 */
@Inmutable
@Accessors(prefix="_")
@RequiredArgsConstructor
public class TypeMethodParamRefByName implements Serializable {
	private static final long serialVersionUID = -9058364756169461226L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Tipo del parámetro
	 */
	@Getter private final TypeRefByName _type;
	/**
	 * Valor del parámetro
	 */
	@Getter private final Object _paramValue;
}
