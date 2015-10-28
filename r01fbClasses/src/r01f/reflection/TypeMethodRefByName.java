package r01f.reflection;

import java.io.Serializable;
import java.util.Set;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;

@Inmutable
@Accessors(prefix="_")
public class TypeMethodRefByName implements Serializable {
	private static final long serialVersionUID = 2502317609370430460L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final TypeRefByName _type;
	/**
	 * Nombre del método
	 */
	@Getter private final String _name;
	/**
	 * Tipos de los parámetros del método
	 */
	@Getter private final TypeMethodParamRefByName[] _params;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
/////////////////////////////////////////////////////////////////////////////////////////
	public TypeMethodRefByName(final TypeRefByName type,
							   final String name,final TypeMethodParamRefByName... params) {
		_type = type;
		_name = name;
		_params = params;
	}
	public TypeMethodRefByName(final TypeRefByName type,
							   final String name,final Set<TypeMethodParamRefByName> params) {
		_type = type;
		_name = name;
		_params = params != null ? params.toArray(new TypeMethodParamRefByName[params.size()])
								 : null;
	}
}
