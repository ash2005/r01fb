package r01f.reflection;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.types.annotations.Inmutable;

/**
 * Encapsula la refencia a un tipo por su nombre
 */
@Inmutable
@Accessors(prefix="_")
@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
public class TypeRefByName implements Serializable {
	private static final long serialVersionUID = -2891958240264085388L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	@Getter private final String _name;
/////////////////////////////////////////////////////////////////////////////////////////
//  CREACIÓN
/////////////////////////////////////////////////////////////////////////////////////////
	public static TypeRefByName forTypeName(final String fullTypeName) {
		return new TypeRefByName(fullTypeName);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return el tipo a partir del nombre de la clase encapsulado
	 */
	public Class<?> type() {
		return ReflectionUtils.typeFromClassName(_name);
	}
	/**
	 * @return una nueva instancia del tipo encapsulado en este objeto
	 */
	public <T> T createInstance() {
		return ReflectionUtils.<T>createInstanceOf(this.type());
	}
	/**
	 * @return un acceso via reflection al tipo
	 */
	public BeanReflection reflectionAccess() {
		return Reflection.type(this.type());
	}
}
