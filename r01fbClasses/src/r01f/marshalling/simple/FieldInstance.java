package r01f.marshalling.simple;

import java.util.List;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.marshalling.MarshallerException;

import com.google.common.collect.Lists;

/**
 * Clase que modela un miembro dentro de una intancia de
 * una clase durante el proceso de conversión de XML a objetos
 */
@Accessors(prefix="_")
class FieldInstance {
///////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////    
    @Getter			private final FieldMap _mapping;					// Definicion del miembro		
    				private Object _instance;							// Instancia del miembro    	   
///////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor
     * @param newFieldMap definición del miembro 
     */
    public FieldInstance(final FieldMap newFieldMap) {
	    _mapping = newFieldMap;
    }
    /** 
     * Crea una instancia utilizando el constructor por defecto
     */
    public Object createInstance() throws MarshallerException {
    	return this.createInstance(null,
    							   null);
    }
    /** 
     * Crea una instancia utilizando un constructor dado
     * @param constructorArgsTypes tipos de los argumentos del constructor del field
     * @param constructorArgs argumentos del constructor del field
     */
    public Object createInstance(final Class<?>[] constructorArgsTypes,
    						     final Object[] constructorArgs) throws MarshallerException {
        _instance = _createInstance(_mapping,
        							constructorArgsTypes,constructorArgs);
        return _instance;
    }
    public Object createInstance(final Object instance) {
    	String nodeName = _mapping.getDataType().getBeanMap().getXmlMap().getNodeName() != null ? _mapping.getDataType().getBeanMap().getXmlMap().getNodeName()
    																						    : _mapping.getName();
    	BeanInstance outInstance = new BeanInstance(_mapping.getDataType().getBeanMap(),
    												nodeName);
    	outInstance.set(instance);
    	_instance = outInstance;
    	return outInstance;
    }
    public static Object createInstance(final FieldMap mapping) {
    	return _createInstance(mapping,
    						   null,null);
    }
    private static Object _createInstance(final FieldMap mapping,
    									  final Class<?>[] constructorArgsTypes,final Object[] constructorArgs) {
    	Object outInstance = null;
        // Crear el objeto asociado dependiendo el tipo de miembro
        // Se pueden dar dos casos:
        //		1. El tipo de dato del miembro SI se ha explicitado en el mapeo, en cuyo caso se tiene información de tipo
        //		2. El tipo de dato del miembro NO se ha explicitado en el fichero de mapeo (ej tipos simples); en este caso se crea un StringBuilder
        //		   que más adelante se convierte en el tipo concreto
        if (mapping != null) {
        	// [1]: Hay información de tipo en el mapeo para el miembro (tipo complejo o colección)
	        if (mapping.getDataType().isCollection() || mapping.getDataType().isMap()) {
	        	List<BeanInstance> instances = Lists.newArrayList();
	        	outInstance = instances;
	        	
	        } else if (mapping.getDataType().isSimple()) {
	            outInstance = new StringBuilder();
	            
	        } else if (mapping.getDataType().isObject()) {
	        	String nodeName = mapping.getDataType().getBeanMap().getXmlMap().getNodeName() != null ? mapping.getDataType().getBeanMap().getXmlMap().getNodeName()
	        																						   : mapping.getName();
	            outInstance = new BeanInstance(mapping.getDataType().getBeanMap(),
	            							   constructorArgsTypes,constructorArgs,
	            							   nodeName);
	        } else {
	        	throw new MarshallerException("El tipo de dato " + mapping.getDataType() + " del miembro " + mapping.getName() + " del bean " + mapping.getDeclaringBeanMap().getTypeName() + " NO es correcto. Revisa el documento de mapeo");
	        }
        } else {
        	// [2] NO hay información de tipo en el mapeo para miembro (tipo simple)
        	outInstance = new StringBuilder();
        }
        return outInstance;
    }
///////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////     
    /**
     * Obtiene la instancia concreta del miembro
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T get() {
    	return (T)_instance;
    }
    /**
     * Establece la instancia concreta del miembro
     * @param obj la instancia
     */
    public <T> void set(T obj) {
    	_instance = obj;
    }
}  
