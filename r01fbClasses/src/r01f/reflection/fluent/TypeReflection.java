package r01f.reflection.fluent;

import r01f.util.types.Strings;

/**
 * Class Loading.
 * 
 * Usage example:
 * <pre>
 *   // Loads the class 'org.republic.Jedi'
 *   Class<?> jediType = Reflection.type("org.republic.Jedi").load();
 * 
 *   // Loads the class 'org.republic.Jedi' as 'org.republic.Person' (Jedi extends Person)
 *   Class<Person> jediType = Reflection.type("org.republic.Jedi").loadAs(Person.class);
 * 
 *   // Loads the class 'org.republic.Jedi' using a custom class loader
 *   Class<?> jediType = Reflection.type("org.republic.Jedi").withClassLoader(myClassLoader).load();
 * </pre>
 */
public final class TypeReflection {
	private final String _className;
///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor
	 * @param className
	 */
	private TypeReflection(final String className) {
		_className = className;
	}
///////////////////////////////////////////////////////////////////////////////
// 	FLUENT-API
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new <code>{@link TypeReflection}</code>: the starting point of the fluent interface for loading classes dynamically.
	 * 
	 * @param className the name of the class to load.
	 * @return the created <code>Type</code>.
	 * @throws NullPointerException if the given name is <code>null</code>.
	 * @throws IllegalArgumentException if the given name is empty.
	 */
	static TypeReflection startTypeAccess(final String className) {
		if (className == null) throw new NullPointerException("The name of the class to load should not be null");
		if (Strings.isNullOrEmpty(className)) throw new IllegalArgumentException("The name of the class to load should not be empty");
		return new TypeReflection(className);
	}
///////////////////////////////////////////////////////////////////////////////
// 	INTERFAZ PUBLICA
///////////////////////////////////////////////////////////////////////////////	
	/**
	 * Specifies the <code>{@link ClassLoader}</code> to use to load the class.
	 * Example:
	 * <pre>
	 *   Class<?> jediType = Reflection.type("org.republic.Jedi").withClassLoader(myClassLoader).load();
	 * </pre>
	 * 
	 * @param classLoader the given <code>ClassLoader</code>.
	 * @return an object responsible of loading a class with the given <code>ClassLoader</code>.
	 * @throws NullPointerException if the given <code>ClassLoader</code> is <code>null</code>.
	 */
	public TypeInvoker withClassLoader(final ClassLoader classLoader) {
		return TypeInvoker.newLoader(_className,classLoader);
	}	
	/**
	 * Loads the class with the name specified in this type, using this class' <code>ClassLoader</code>.
	 * 
	 * @return the loaded class.
	 * @throws ReflectionError wrapping any error that occurred during class loading.
	 */
	public Class<?> load() {
		return TypeInvoker.newLoader(_className,thisClassLoader()).load();
	}
	/**
	 * Loads the class with the name specified in this type, as the given type, using this class' <code>ClassLoader</code>.
	 * The following example shows how to use this method. Let's assume that we have the class <code>Jedi</code> that extends the class <code>Person</code>:
	 * <pre>
	 *   Class<Person> jediType = Reflection.type("org.republic.Jedi").loadAs(Person.class);
	 * </pre>
	 * 
	 * @param type the given type.
	 * @param <T> the generic type of the type.
	 * @return the loaded class.
	 * @throws NullPointerException if the given type is <code>null</code>.
	 * @throws ReflectionError wrapping any error that occurred during class loading.
	 */
	public <T> Class<? extends T> loadAs(final Class<T> type) {
		return TypeInvoker.newLoader(_className,thisClassLoader()).loadAs(type);
	}
///////////////////////////////////////////////////////////////////////////////
// 	METODOSO PRIVADOS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the local class loader
	 * @return
	 */
	private ClassLoader thisClassLoader() {
		return this.getClass().getClassLoader();
	}
	
}
