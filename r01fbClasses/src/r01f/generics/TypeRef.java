package r01f.generics;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import r01f.reflection.ReflectionUtils;

/**
 * Reference to a generic type.
 * Based on Neal Gafter's <code><a href="http://gafter.blogspot.com/2006/12/super-type-tokens.html" target="_blank">TypeReference</a></code>.
 * Usage: When a {@link TypeRef} argument is needed:
 * <code>
 * 		new TypeRef<TyeType>() {}
 * <code>
 * @param <T> the generic type in this reference.
 */
public abstract class TypeRef<T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
/////////////////////////////////////////////////////////////////////////////////////////	
	private final Class<?> _rawType;
///////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new </code>{@link TypeRef}</code>.
	 * @throws IllegalArgumentException if the generic type of this reference is missing type parameter.
	 */
	public TypeRef() {
		Type superclass = this.getClass().getGenericSuperclass();
		if (superclass instanceof Class<?>) throw new IllegalArgumentException("Missing type parameter. Maybe you have used the generics 'mode' but the type is not generics");
		Type type = ((ParameterizedType)superclass).getActualTypeArguments()[0];
		_rawType = ReflectionUtils.classOfType(type);
		if (_rawType == null) throw new IllegalArgumentException("The rawType of type=" + type + " cannot be known!");
	}
///////////////////////////////////////////////////////////////////////////////
//	METODOS PUBLICOS
///////////////////////////////////////////////////////////////////////////////	
	/**
	 * Returns the raw type of the generic type in this reference.
	 * @return the raw type of the generic type in this reference.
	 */
	public final Class<?> rawType() {
		return _rawType;
	}
}
