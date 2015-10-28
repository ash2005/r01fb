package r01f.reflection;



/**
 * Fluent API
 */
public class Reflection {	
	public static  BeanReflection type(Class<?> type) {
		return new BeanReflection(type);
	}
    /**
     * Obtiene la definición de una clase (Class) a partir del nombre completo
     * (incluido paquete) de la clase
     * @param className El nombre completo de la clase
     * @return La definicion de la clase (Class) (NO UNA INSTANCIA)
     * @throws ReflectionException si NO se encuentra la clase 
     */
    public static BeanReflection type(String className) {
        Class<?> type = ReflectionUtils.typeFromClassName(className);
        return new BeanReflection(type);
    }	
    /**
     * Wrapper para reflection sobre una instancia de un objeto
     * @param bean el bean sobre el que se quiere hacer reflection
     * @return un wrapper para hacer reflection sobre la instanci
     */
    public static BeanInstanceReflection of(Object bean) {
    	BeanInstanceReflection beanInstanceRef = new BeanInstanceReflection(bean.getClass(),bean);
    	return beanInstanceRef;
    }
}
