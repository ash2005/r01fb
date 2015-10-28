package r01f.reflection;

import java.lang.reflect.Field;


public class FieldReflection {
	private Class<?> _beanType;
	private Object _bean;
	private Field _field;
	private boolean _useAccessors = true;
	
	public FieldReflection(Class<?> beanType,Object bean,Field field) {
		_beanType = beanType;
		_bean = bean;
		_field = field;
	}
///////////////////////////////////////////////////////////////////////////////
//	METODOS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Devuelve el valor del miembro
	 * @return el valor del miembro
	 * @throws ReflectionException si hay algún error para obtener el valor
	 */
	@SuppressWarnings("unchecked")
	public <T> T get() {
		T outFieldValue = null;
		if (_bean != null) {
			outFieldValue = (T)ReflectionUtils.fieldValue(_bean,_field,_useAccessors);
		} else {
			// se trata de un field estatico
			outFieldValue = (T)ReflectionUtils.getStaticFieldValue(_beanType,_field.getName());
		}
		return outFieldValue;
	}
	/**
	 * Establece el valor de un miembro
	 * @param newValue el nuevo valor del miembro
	 * @throws ReflectionException si hay algún error para establecer el valor
	 */
	public <T> void set(T newValue) {
		if (_bean != null) {
			ReflectionUtils.setFieldValue(_bean,_field,newValue,_useAccessors);
		} else {
			// se trata de un field estatico
			ReflectionUtils.setStaticFieldValue(_beanType,_field.getName(),newValue);
		}
	}
	/**
	 * Establece que SI hay que utilizar los metodos get/set para acceder
	 * al miembro
	 */
	public FieldReflection usingAccessors()  {
		_useAccessors = true;
		return this;
	}
	/**
	 * Establece que NO hay que utilizar los metodos get/set para acceder
	 * al miembr
	 */
	public FieldReflection withoutUsingAccessors()  {
		_useAccessors = false;
		return this;
	}	

}
