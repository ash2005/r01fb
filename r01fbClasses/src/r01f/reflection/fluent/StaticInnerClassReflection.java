package r01f.reflection.fluent;

import r01f.util.types.Strings;

/**
 * Static Inner class
 * 
 * Let's assume we have the class <code>Jedi</code>, which contains two static inner classes: <code>Master</code> and <code>Padawan</code>.
 * 
 * <pre>
 * public class Jedi {
 * 		public static class Master {
 * 		}
 * 		public static class Padawan {
 * 		}
 * }
 * </pre>
 * 
 * The following example shows how to get a reference to the inner class <code>Master</code>:
 * 
 * <pre>
 * Class<?> masterClass = Reflection.staticInnerClass("Master").in(Jedi.class).get();
 * </pre>
 */
public final class StaticInnerClassReflection {
	private final String _innerClassName;
///////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	private StaticInnerClassReflection(final String name) {
		_innerClassName = name;
	}	
///////////////////////////////////////////////////////////////////////////////
// 	FLUENT-API
///////////////////////////////////////////////////////////////////////////////	
	/**
	 * Creates a new </code>{@link StaticInnerClassReflection}</code>.
	 * 
	 * @param name the name of the static inner class to obtain.
	 * @return the created <code>StaticInnerClassName</code>.
	 * @throws NullPointerException if the given name is <code>null</code>.
	 * @throws IllegalArgumentException if the given name is empty.
	 */
	static StaticInnerClassReflection startStaticInnerClassAccess(final String name) {
		_validateIsNotNullOrEmpty(name);
		return new StaticInnerClassReflection(name);
	}
///////////////////////////////////////////////////////////////////////////////
// 	INTERFAZ PUBLICA
///////////////////////////////////////////////////////////////////////////////	
	/**
	 * Specifies the declaring class of the static inner class to obtain.
	 * 
	 * @param declaringClass the declaring class.
	 * @return an object responsible for obtaining a reference to a static inner class.
	 * @throws NullPointerException if the given declaring class is <code>null</code>.
	 */
	public StaticInnerClassInvoker in(final Class<?> declaringClass) {
		return StaticInnerClassInvoker.newInvoker(declaringClass,_innerClassName);
	}
///////////////////////////////////////////////////////////////////////////////
// 	METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////	
	private static void _validateIsNotNullOrEmpty(final String innerClassName) {
		if (innerClassName == null) throw new NullPointerException("The name of the static inner class to access should not be null");
		if (Strings.isNullOrEmpty(innerClassName)) throw new IllegalArgumentException("The name of the static inner class to access should not be empty");
	}
}
