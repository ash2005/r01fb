package r01f.marshalling.simple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.simple.BeanMap.BeanXMLMap;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.marshalling.simple.DataTypes.DataTypeEnum;
import r01f.reflection.ReflectionException;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
       
/**
 * Clase que modela una instancia de una clase durante el proceso de 
 * conversión de XML a objetos
 */
@Accessors(prefix="_")
class BeanInstance {  	
///////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////        
    @Getter 		private final BeanMap _mapping;						// Definicion de la clase
    @Getter 		private final Map<String,FieldInstance> _fields;	// Miembros de la clase indexados por el nombre del TAG (o nodo)
    @Getter @Setter private String _effectiveNodeName;					// tag que envuelve el objeto
    				private Object _instance;							// Datos concretos    
///////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor sin especificar el mapeo del bean
     * Se utiliza en los siguientes casos:
     * 	- un tipo primitivo (String, integer, etc) que NO se declaran en el fichero de mapeo
     * 	- una colección de tipos primitivos (String, integer, etc)
     */
    public BeanInstance() throws MarshallerException {
    	this(null,null,null,null);
    }
    public BeanInstance(final BeanMap newBeanMap,final String effectiveNodeName) {
    	_effectiveNodeName = effectiveNodeName;
        _mapping = newBeanMap;
        _fields = newBeanMap != null && CollectionUtils.hasData(newBeanMap.getFields()) ? new HashMap<String,FieldInstance>(newBeanMap.getFields().size())
        																			    : null;
    }
    /** 
     * {@link BeanInstance} builder from the {@link BeanMap} 
     * @param newBeanMap 
     * @param constructorArgsTypes the type's constructor argument types
     * @param constructorArgs the type's constructor argument values
     * @param effectiveNodeName the type's tag to be used
     */
    public BeanInstance(final BeanMap newBeanMap,
    					final Class<?>[] constructorArgsTypes,final Object[] constructorArgs,
    					final String effectiveNodeName) throws MarshallerException {
    	this(newBeanMap,effectiveNodeName);
    	
        if (_mapping != null && _mapping.getCustomXMLTransformers() != null) return;	// CustomXMLTransformers are used... nothing to do
        																				// ... the bean is created at ObjsFromXMLBuilder
        try {
	        if (_mapping != null) {
	        	// Get a type instance...
	        	if (_mapping.getDataType().isEnum()) {
	        		// an enum
	        		_instance = new StringBuilder();
	        	} else if (_mapping.getDataType().isObject() && _mapping.getFields() == null) {
	        		// an object with NO mapped fields (ie all fields are transient or annotated with @XmlTransient)
	        		// (for example r01f.types.Path)
	        		// This kind of objects can only be created using a single String-param constructor or an static valueOf(String) builder method
	        		_instance = new StringBuilder();
	        		
	        	} else {
	        		// a "usual" java type
	        		_instance = MappingReflectionUtils.createObjectInstance(_mapping,
	        																constructorArgsTypes,constructorArgs);
	        	}
	        } else {
	            // Virtual instance: there's NO definition for the type
	            // Usually the flow enter this block if it's:
	        	//		- A simple type (String, Integer, int, Long, boolean, etc)
	        	//		- A Collection of simple types (String, integer, etc)
	        	//				- List: the text is directly the field value
	        	//				- Maps:the tag name is the key and the text is the value
	        	// 				ie:
	        	//	  			- List:				 				- Map
	            //					<parameters>                       <parameters>
	            //						<param>valor1</param>                 <param_1>valor1</param_1>
	            //						<param>valor2</param>				  <param_2>valor2</param_2>
	            //					</parameters>					   </parameters>
	            _instance = new StringBuilder();
	        }
        } catch(ReflectionException refEx) {
            throw new MarshallerException("Error al crear una instancia de la clase " + (_mapping != null ? _mapping.getTypeName() : "null") + ". Posiblemente NO tenga un constructor SIN argumentos o NO se encuentra la clase: " + refEx.getMessage(),refEx);
        }        
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene la instancia concreta del bean
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
    public <T> void set(final T obj) {
    	_instance = obj;
    }
    /**
     * Devuelve el mapeo xml del bean
     * @return
     */
    public BeanXMLMap getXmlMap() {
    	return _mapping.getXmlMap();
    }
    /** 
     * Añade una instancia de un miembro a una instancia de una clase
     * @param fieldMap mapeo del miembro
     * @return Un objeto FieldInstance que encapsula el mapeo con la instancia ya creada
     * @throws MarshallerException Si se produce algún error en el proceso
     */
	public FieldInstance getFieldInstance(final FieldMap fieldMap) throws MarshallerException {
    	if (fieldMap == null) return null;
    	FieldInstance fieldInstance = null;
    	// Si la instancia del miembro YA existe, devolver esta instancia, en otro caso crearla
    	if (!_fields.isEmpty()) {
    		fieldInstance = _fields.get(fieldMap.getXmlMap().getNodeName());
    	}
    	// en otro caso... crearla (posiblemente la instancia concreta aún NO se ha creado)
    	if (fieldInstance == null) {
    		fieldInstance = new FieldInstance(fieldMap);
    		_fields.put(fieldMap.getXmlMap().getNodeName(),fieldInstance);
    	} 
    	return fieldInstance;
	}
    /**
     * Construye la instancia del objeto estableciendo cada una de sus propiedades
     */
    public Object build() {
    	if (_instance == null) return null;
    	
    	if (_mapping == null) {
    		// Instancia "virtual" para tipos simples (String, date, etc) en colecciones 
    		// ... se construye el tipo real al construir la colección en la clase MappingReflectionUtils
    		return _instance;
    	}
    	if (_mapping.isCustomXmlTransformed()) {
    		StringBuilder xml = (StringBuilder)_instance;
    		Object builtObj = _mapping.getCustomXMLTransformers().getXmlReadTransformer()
    												  			 .beanFromXml(false,xml);
    		_instance = builtObj;
    		return _instance;
    	}
    	if (_mapping.getDataType().isEnum()) {
    		Object builtObj = MappingReflectionUtils.simpleObjFromString(_mapping.getDataType(),
    															   		 (StringBuilder)_instance);
    		_instance = builtObj;
    		return _instance;
    	}
    	// Construir el objeto a partir de sus miembros
    	// (al metodo build se llama al CERRAR el objeto, por lo que TODOS sus miembros
    	//  se supone que YA se habrán construido)
    	if (CollectionUtils.hasData(_fields)) {
	    	for (FieldInstance fieldInstance : _fields.values()) {
	    		
	    		if (fieldInstance.get() == null) continue;
	    		
    			Object value = null;
    			DataType fieldDataType = fieldInstance.getMapping().getDataType();
    			
    			if (fieldDataType.isXML()) {
    				StringBuilder valueStr = fieldInstance.get();
    				if (valueStr.toString().startsWith("<" + fieldInstance.getMapping().getXmlMap().getNodeName() + ">")) {
    					// si el XML se mapea con el propio tag, eliminar el tag
    					valueStr = _extractXMLBetweenTag(valueStr);
    					fieldInstance.set(valueStr);
    				}
    			} 
    			if (fieldDataType.isCollection() || fieldDataType.isMap()) {
		        	// Colección 
		        	List<BeanInstance> instances = fieldInstance.get();
		        	if ( !CollectionUtils.isNullOrEmpty(instances) ) value = instances;			        	
		        	
		        	// colección / mapa de xmls... extraer xml que hay entre el tag de inicio y fin (no se puede hacer en otro sitio que aquí)
		        	if ((fieldDataType.isCollection() && DataTypeEnum.XML.canBeFromTypeName(fieldDataType.asCollection().getValueElementsType().getName())
                        ||
                        (fieldDataType.isMap() && DataTypeEnum.XML.canBeFromTypeName(fieldDataType.asMap().getValueElementsType().getName())))) {
		        		for (BeanInstance colElBean : instances) {
		        			StringBuilder valueStr = colElBean.get();
		        			valueStr = _extractXMLBetweenTag(valueStr);
		        			colElBean.set(valueStr);
		        		}
		        	}
		        	
		        } else if (fieldDataType.isSimple()) {
		        	// Tipo simple
		            StringBuilder valueStr = fieldInstance.get();
		            if (!Strings.isNullOrEmpty(valueStr)) value = valueStr;
		        
		        } else if (fieldDataType.isObject()
		        		&& (fieldDataType.getBeanMap() == null
		        		    || !fieldDataType.getBeanMap().isCustomXmlTransformed())) {
		        	// Bean normal: en la instancia viene el propio bean
		        	BeanInstance instance = fieldInstance.get();
		        	value = instance.get();
		        	
		        } else if (fieldDataType.isObject() 
		        		&& fieldDataType.getBeanMap().isCustomXmlTransformed()) {
		        	// Bean customXmlTransformed: en la instancia viene el xml
		        	BeanInstance instance = fieldInstance.get();
		        	value = instance.get();
		        	//StringBuilder xml = instance.get();
		        	//value = fieldDataType.getBeanMap().getCustomXMLTransformers().getXmlReadTransformer().beanFromXml(xml);
		        } 
    			
		        // Set the value by reflection
		        if (value != null) {
		        	MappingReflectionUtils.setFieldValue(_instance,
		        										 fieldInstance.getMapping(),
		        										 value);
		        }
	    	}
    	} 
    	return _instance;
    }
    
    // Extrae el xml que hay entre dos tags: (<xmlTag>...texto a extraer...</xmlTag>
    private static final Pattern XMLBETWEENTAGS_PATTERN = Pattern.compile("<([^>]+)>(.*)</\\1>");
    private static StringBuilder _extractXMLBetweenTag(final StringBuilder xml) {
    	StringBuilder outXML = null;
		Matcher m = XMLBETWEENTAGS_PATTERN.matcher(xml);
		if (m.find()) outXML = new StringBuilder(m.group(2));
		return outXML != null ? outXML
							  : xml;
    }
}