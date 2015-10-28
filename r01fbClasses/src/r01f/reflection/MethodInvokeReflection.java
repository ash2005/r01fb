package r01f.reflection;

import java.lang.reflect.Method;
 
public class MethodInvokeReflection {
	private Class<?> _beanType;
	private Object _bean;
	private Method _method;
	
	public MethodInvokeReflection(Class<?> beanType,Object bean,Method method) {
		_beanType = beanType;
		_bean = bean;
		_method = method;
	}
///////////////////////////////////////////////////////////////////////////////
//	METODOS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Invoca un metodo sobre un objeto
	 * @return el objeto devuelto tras la invocación del metodo
	 * @throws ReflectionException
	 */
	@SuppressWarnings("unchecked")
	public <T> T invoke() {
		return (T)this.invoke(new Object[] {});
	}
    /**
     * Invoca un metodo sobre un objeto
     * @param argValues valores para los argumentos
     * @return El objeto devuelto tras la invocacion del metodo
     * @throws ReflectionException si ocurre algun error
     */
	@SuppressWarnings("unchecked")
    public <T> T invoke(Object... argValues) {
		T outValue = null;
    	if (_bean != null) {
    		// Invocar un metodo de instancia
	    	Object retValue = ReflectionUtils.invokeMethod(_bean,_method,argValues);
	    	outValue = (T)retValue;
    	} else {
    		// Invocar un método estatico
    		Object retValue = ReflectionUtils.invokeStaticMethod(_beanType,_method,argValues);
    		outValue = (T)retValue;
    	}
    	return outValue;
    }
}
