package r01f.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import r01f.reflection.ReflectionUtils.FieldAnnotated;

public class BeanInstanceReflection {
	private Class<?> _beanType;
	private Object _bean;
	
	public BeanInstanceReflection(Class<?> beanType,Object bean) {
		_beanType = beanType;
		_bean = bean;
	}
///////////////////////////////////////////////////////////////////////////////
//	METODOS
///////////////////////////////////////////////////////////////////////////////
    /**
     * Busca el metodo que se pasa como parameto, recorriendo toda la jerarquia de
     * herencia
     * PROBLEMA:    class.getMethods()          devuelve solo metodos PUBLICOS
     *              class.getDeclaredMethods()  devuelve metodos publicos y privados declarados
     * @param methodName El nombre del metodo
     * @return un wrapper para la invocación del metodo
     * @throws ReflectionException NoSuchMethodException si no se encuentra el metodo
     */
    public MethodInvokeReflection method(String methodName) {
    	Method method = ReflectionUtils.method(_beanType,methodName,new Class[] {});
    	return new MethodInvokeReflection(_beanType,_bean,method);
    }		
    /**
     * Busca el metodo que se pasa como parameto, recorriendo toda la jerarquia de
     * herencia
     * PROBLEMA:    class.getMethods()          devuelve solo metodos PUBLICOS
     *              class.getDeclaredMethods()  devuelve metodos publicos y privados declarados
     * @param methodName El nombre del metodo
     * @param paramTypes Los tipos de los parametros
     * @return un wrapper para la invocación del metodo
     * @throws ReflectionException NoSuchMethodException si no se encuentra el metodo
     */
    public MethodInvokeReflection method(String methodName,Class<?>... paramTypes) {
    	Method method = ReflectionUtils.method(_beanType,methodName,paramTypes);
    	return new MethodInvokeReflection(_beanType,_bean,method);
    }
    /**
     * Obtiene un wrapper para la invocación del método
     */
    public MethodInvokeReflection method(Method method) {
    	return new MethodInvokeReflection(_beanType,_bean,method);
    }
    /**
     * Devuelve el bean instanciado
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T instance() {
    	return (T)_bean;
    }
///////////////////////////////////////////////////////////////////////////////
//	MIEMBROS
///////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene un campo (miembro) del bean
     */
    public FieldReflection field(String fieldName) {
    	Field field = ReflectionUtils.field(_beanType,fieldName,true);
    	return new FieldReflection(_beanType,_bean,field);
    } 
    /**
     * Obtiene un campo (miembro) del bean
     */
    public FieldReflection field(Field field) {
    	return new FieldReflection(_beanType,_bean,field);
    }
    /**
     * Devuelve todos los campos (miembro) del bean que son de un determinado tipo
     * @param fieldType tipo de los miembros deseados
     */
    public FieldReflection[] fieldsOfType(Class<?> fieldType) {
    	Field[] fields = ReflectionUtils.fieldsOfType(_beanType,fieldType);
    	FieldReflection[] outFields = new FieldReflection[fields.length];
    	int i = 0;
    	for (Field f : fields) {
    		FieldReflection fr = new FieldReflection(_beanType,_bean,f);
    		outFields[i] = fr;
    		i++;
    	}
    	return outFields;
    }
    /**
     * Obtiene un campo (miembro) del bean anotado con una determinada anotacion
     * @param annotationType anotación
     */
    @SuppressWarnings("unchecked")
	public FieldAnnotatedReflection<? extends Annotation>[] fieldsAnnotatedWith(Class<? extends Annotation> annotationType) {
    	FieldAnnotated<? extends Annotation>[] fieldsAnnotated = ReflectionUtils.fieldsAnnotated(_beanType,annotationType);
    	
		FieldAnnotatedReflection<? extends Annotation>[] outFields = null;
    	if (fieldsAnnotated != null && fieldsAnnotated.length > 0) {
    		outFields = new FieldAnnotatedReflection[fieldsAnnotated.length];
    		int i=0;
    		for (FieldAnnotated<? extends Annotation> fieldAnnotated : fieldsAnnotated) {
    			FieldReflection fr = new FieldReflection(_beanType,_bean,fieldAnnotated.getField());
    			Annotation an = fieldAnnotated.getAnnotation();
    			outFields[i] = new FieldAnnotatedReflection<Annotation>(fr,an);
    			i++;
    		}
    	}
    	return outFields;
    }
}
