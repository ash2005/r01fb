package r01f.marshalling.simple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.extern.slf4j.Slf4j;
import r01f.locale.Language;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTextsMapBacked;
import r01f.locale.Languages;
import r01f.locale.LanguageTexts.LangTextNotFoundBehabior;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.marshalling.simple.DataTypes.DataTypeEnum;
import r01f.reflection.Reflection;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Dates;
import r01f.util.types.Numbers;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.collect.Sets;


/**
 * Reflection utils for the xml to objs marshaller
 */
@Slf4j
class MappingReflectionUtils {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Collection types that have a size constructor (ie: new ArrayList(size))
	 */
	@SuppressWarnings("unused")
	private static final Collection<Class<?>> COLLECTION_TYPES_WITH_SIZE_CONSTRUCTOR = Sets.<Class<?>>newHashSet(ArrayList.class,
																										  		 HashMap.class,
																										  		 LinkedHashMap.class,
																										  		 HashSet.class,
																										  		 LinkedHashSet.class);
	/**
	 * Collection types that have a size constructor (ie do not have a constructor like new ArrayList(size))
	 */
	private static final Collection<Class<?>> COLLECTION_TYPES_WITHOUT_SIZE_CONSTRUCTOR = Sets.<Class<?>>newHashSet(LinkedList.class);
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Instancia una clase utilizando reflection a partir de la definición de la clase
	 * @param newBeanMap la definición de mapeo xml-obj del nuevo objeto
	 * @param constructorArgsTypes tipos de los argumentos del constructor
	 * @param constructorArgs argumentos del constructor del bean
	 * @return la instancia de la clase creada
     * @throws ReflectionException si ocurre algún error al obtener la instancia
	 */
	static <T> T createObjectInstance(final BeanMap newBeanMap,
									  final Class<?>[] constructorArgsTypes,final Object[] constructorArgs) throws ReflectionException {
		T outObj = null;
		try {
			outObj = Reflection.type(newBeanMap.getDataType().getType())
							   .withConstructor(constructorArgsTypes)
							   .load(constructorArgs)
							   .<T>instance();
		} catch(Throwable th) {
			throw ReflectionException.of(th); 
		}
		if (outObj == null) throw ReflectionException.instantiationException(newBeanMap.getDataType().getType(),constructorArgsTypes); 
		return outObj;
	}
	/**
	 * Obtiene el valor de un miembro de un objeto
	 * @param obj el objeto
	 * @param fieldName el nombre del miembro
	 * @param fieldType el tipo del miembro
	 * @param useAccessors true si hay que utilizar métodos get/set
	 * @return el valor del miembro
	 */
	static <T> T getFieldValue(final Object obj,final String fieldName,final boolean useAccessors,
							   final Class<?> fieldType) {
		T outFieldValue = ReflectionUtils.<T>fieldValue(obj,fieldName,useAccessors,
													    fieldType);     // Valor del miembro
		return outFieldValue;
	}
	/**
	 * Obtiene los elementos de una colección, independientemente de que la 
	 * colección sea un mapa, una lista o un array
	 * 		- Si es una lista o un array se devuelve una colección con los objetos
	 * 		- Si es un mapa se devuelve una colección de Map.Entry
	 * @param colObj la colección (mapa, lista o array)
	 * @return una colección con los elementos
	 */
	static Collection<?> getCollectionElements(final Object colObj) {
		if (colObj == null) return null;
		Collection<?> outEls = null;
		
		if (CollectionUtils.isCollection(colObj.getClass())) {
			outEls = (Collection<?>)colObj;
			
		} else if (CollectionUtils.isMap(colObj.getClass())) {
			outEls = ((Map<?,?>)colObj).entrySet();		// values()
			
		} else if (colObj.getClass().isArray()) {
			//Class<?> elType = colObj.getClass().getComponentType();
			//int length = Array.getLength(colObj);
			outEls = Arrays.asList((Object[])colObj);
		}
		return outEls;
	}
	/**
	 * Establece el valor de una variable miembro de una clase
	 * Para ello utiliza reflection y antes de nada convierte al tipo de dato
	 * correcto.
	 * @param obj El objeto cuya variable hay que establecer
	 * @param fieldMap La definicion del miembro
	 * @param value El valor del miembro
     * @throws ReflectionException si ocurre algún error al obtener la instancia
	 */
	static void setFieldValue(final Object obj,final FieldMap fieldMap,
							  final Object value) throws ReflectionException {		
		if (value == null) return;

	    // Utilizar reflection para establecer el valor de la variable member en el objeto tarjetObj.  
        // Hay 2 casos:
        //      CASO 1: El miembro es un objeto simple (String, Integer, int, etc)
        //      CASO 2: El miembro es una colección/mapa de objetos complejos
        //      CASO 3: El miembro es un objeto complejo
		Object valueObj = null;
					
		if (value instanceof StringBuilder) {	
        	// [CASO 1]:  objeto simple (String, Integer, int, etc)
        	valueObj = MappingReflectionUtils.simpleObjFromString(fieldMap.getDataType(),
        														  value.toString());
		} else if (value instanceof ArrayList) {
			// [CASO 2]: Se trata de una colección 
			DataType dataType = fieldMap.getDataType();				
			
			@SuppressWarnings("unchecked")
			List<BeanInstance> instances = (List<BeanInstance>)value;
			
			// [Collections] ------
			if (dataType.isCollection()) {
				Collection<Object> listInstance = null;
				if (dataType.getTypeDef() == DataTypeEnum.ARRAY) {
					listInstance = new ArrayList<Object>(instances.size());
					
				} else {
					// Create a collection instance with the correct size if possible
					if (!COLLECTION_TYPES_WITHOUT_SIZE_CONSTRUCTOR.contains(dataType.getType())) {
						listInstance = ReflectionUtils.createInstanceOf(dataType.getType(),							 // tipo de lista
										  							    new Class[] {int.class},					 // tamaño de la lista
																		new Object[] {new Integer(instances.size())},// 
																		true);										 // forzar la creación
					} else {
						listInstance = ReflectionUtils.createInstanceOf(dataType.getType());	// tipo de lista	
					}
				}
				// Every bean instance
				for (BeanInstance beanInstance : instances) {
					Object colElInstance = null;
					DataType colElsType = dataType.asCollection().getValueElementsDataType();
					if (colElsType.isSimple()) {
						StringBuilder sb = beanInstance.get();
						colElInstance = MappingReflectionUtils.simpleObjFromString(colElsType,sb.toString());
					} else {	
						colElInstance = beanInstance.get();
					}
					if (colElInstance != null) listInstance.add(colElInstance);
				} // for collection elements
				valueObj = dataType.getTypeDef() == DataTypeEnum.ARRAY ? CollectionUtils.toArray(listInstance,dataType.getType())
														   			   : listInstance;
			}
			// [Maps] -------
			else if (dataType.isMap()) {	
				Map<Object,Object> mapInstance = ReflectionUtils.createInstanceOf(dataType.getType(),							// tipo de mapa
                                                								  new Class[] {int.class},						// tamaño del mapa
                                                								  new Object[] {new Integer(instances.size())},	// 
                                                								  true);										// forzar la creación
				int i = 1;
				for (BeanInstance beanInstance : instances) {
					Object mapEntryKeyInstance = null;
					Object mapEntryValueInstance = null;
					
					DataType mapKeysType = dataType.asMap().getKeyElementsDataType();
					DataType mapValuesType = dataType.asMap().getValueElementsDataType();
					
					// -- Mapa de tipos simples (String, long, etc)
					if (mapValuesType.isSimple()) {
						// [Key]
						if (mapKeysType.getType() == Language.class) {
							// Se trata de un mapa de Language,String (LangText)
							if (beanInstance.getEffectiveNodeName().length() == 2) {
								// es, eu, en
								mapEntryKeyInstance = Languages.fromContentLangVersionFolder(beanInstance.getEffectiveNodeName());
							} else {
								// SPANISH, BASQUE, etc
								mapEntryKeyInstance = Language.fromName(beanInstance.getEffectiveNodeName());
							}
						} else {
							// Si el objeto del mapa es un objeto simple (long, string, etc), la clave por la que se indexa
							// es el nombre del tag (no hay otra cosa por la que indexar)
							mapEntryKeyInstance = MappingReflectionUtils.simpleObjFromString(mapKeysType,beanInstance.getEffectiveNodeName());
						}
						// [Value]
						StringBuilder sb = (StringBuilder)beanInstance.get();
						mapEntryValueInstance = simpleObjFromString(mapValuesType,sb.toString());
						
					}
					// -- Mapa de tipos complejo
					else {	
						// [Key]
						if (mapKeysType.getType() == Language.class) {
							// Mapa indexado por Language
							if (beanInstance.getEffectiveNodeName().length() == 2) {
								// es, eu, en
								mapEntryKeyInstance = Languages.fromCountryCode(beanInstance.getEffectiveNodeName());
							} else {
								// SPANISH, BASQUE, etc
								mapEntryKeyInstance = Language.fromName(beanInstance.getEffectiveNodeName());
							}
						} else {
							// Mapa indexado por el oid del tipo complejo 
							FieldMap oidFieldMap = beanInstance.getMapping().getOidField();
							if (oidFieldMap != null) {
								mapEntryKeyInstance = ReflectionUtils.fieldValue(beanInstance.get(),oidFieldMap.getName(),
																				 fieldMap.getDeclaringBeanMap().isUseAccessors());
							} else {
								log.warn("Se está indexando el objeto {} en el miembro {} tipo mapa del objeto {}, pero NO se ha indicado cual el el miembro que hace de OID; es recomendable hacerlo ya que de otra forma se indexa por el nombre del tag mas un número",
										 beanInstance.getMapping().getTypeName(),fieldMap.getName(),obj.getClass());
								mapEntryKeyInstance = beanInstance.getEffectiveNodeName() + i;		// por defecto se utiliza el nombre del tag para indexar
							}
						}
						// [Value]
						mapEntryValueInstance = beanInstance.get();
					}
					// Add the entry to the map
					if (mapEntryValueInstance != null) {
						if (mapEntryKeyInstance == null) throw new IllegalArgumentException("Null keys are NOT allowed!");
						mapInstance.put(mapEntryKeyInstance,mapEntryValueInstance);
					}
					i++;
				} // for map elements
				
				// Si se trata de un Map<Language,String> el miembro PUEDE ser un LanguageTexts
				// ... en otro caso es un simple Mapa
				if (dataType.asMap().getKeyElementsType().equals(Language.class) 
				 && dataType.asMap().getValueElementsType().equals(String.class)) {
					LanguageTexts langTexts = new LanguageTextsMapBacked(mapInstance.size(),
																		 LangTextNotFoundBehabior.RETURN_NULL);
					for(Map.Entry<Object,Object> me : mapInstance.entrySet()) {					
						langTexts.add((Language)me.getKey(),
									  (String)me.getValue());
					}
					valueObj = langTexts;
				}
				// Mapa "normal"
				else {
					valueObj = mapInstance;
				}
			}		
			
		}
		// [Objects] -----
		else {
            // Se trata de otro objeto el valor ya está en el objeto que se pasa como parámetro
			valueObj = value;			
		}
				
        // Establecer el valor utilizando reflection
		if (valueObj != null) {
	        ReflectionUtils.setFieldValue(obj,fieldMap.getName(),
	        							  valueObj,
	        							  fieldMap.getDeclaringBeanMap().isUseAccessors());
		}
	}
	/**
	 * Formatea el valor de un miembro como un String
	 * @param fieldMap el miembro
	 * @param value valor del miembro
	 * @return el miembro formateado como un String o null si NO el miembro no es un tipo simple
	 */
	static String formatAsString(final FieldMap fieldMap,
								 final Object value) {
		if (value == null) return null;
		
		String outStrValue = null;
        if (!fieldMap.getDataType().isObject()) {
        	// it's a simple (primitive) type
        	outStrValue = MappingReflectionUtils.formatAsString(fieldMap.getDataType(),value);
        	
        } else if (fieldMap.getDataType() != null && fieldMap.getDataType().getBeanMap() != null
        		&& CollectionUtils.isNullOrEmpty(fieldMap.getDataType().getBeanMap().getFields())) {
        	// if the field's bean has NO fields (ie a path)...
        	outStrValue = value.toString();
        	
        } else if (fieldMap.getDataType().isObject()) {
        	// it's an object
        	// CustomXMLTransformers
        	if (fieldMap.getDataType().getBeanMap() != null 
        	 && fieldMap.getDataType().getBeanMap().isCustomXmlTransformed()) {
        		outStrValue = fieldMap.getDataType().getBeanMap().getCustomXMLTransformers()
        														 .getXmlWriteTransformer()
        														 .xmlFromBean(fieldMap.getXmlMap().isAttribute(),
        																 	  value);
        	} else if (MappingReflectionUtils.isSimple(value)) {
        		outStrValue = MappingReflectionUtils.formatAsString(fieldMap.getDataType(),value);
        	} 
        }
        return outStrValue;		
	}
	/**
	 * Formatea el valor de un miembro como un String, PERO SOLO en el caso de que
	 * el objeto sea un tipo "simple" (String, Long, StringBuilder, Date, etc)
	 * @param dataType el tipo de dato 
	 * @param value el valor del miembro
	 * @return el miembro formateado como un String o null si NO el miembro no es un tipo simple
	 */
	static String formatAsString(final DataType dataType,
								 final Object value) {
		if (value == null) return null;
		
		String outStrValue = null;
        if (value instanceof Integer) {
            outStrValue = ((Integer)value).toString();
            
        } else if (value instanceof Long) {
            outStrValue = ((Long)value).toString();
            
        } else if (value instanceof Double) {
            outStrValue = ((Double)value).toString();

        } else if (value instanceof Float) {
            outStrValue = ((Float)value).toString();
            
        } else if (value instanceof Boolean) {
            outStrValue = ((Boolean)value).toString();
            
        } else if (value instanceof java.util.Date || value instanceof java.sql.Date) {
        	String dateFormat = dataType.asDate().getDateFormat();
			outStrValue = Dates.format((java.util.Date)value,dateFormat);
			
        } else if (ReflectionUtils.isImplementing(value.getClass(),CharSequence.class)) {
            outStrValue = value.toString();		// String, StringBuffer, StringBuilder, etc
            
        } else if (value instanceof Enum) {
        	outStrValue = ((Enum<?>)value).name();
        	
        } else if (value instanceof Class) {
        	/* nothing */
        }
        return outStrValue;		
	}
	/**
	 * Devuelve si un objeto es simple o no viendo si es una instancia simple
	 * @param value el objeto
	 * @return true si el objeto es simple
	 */
	static boolean isSimple(final Object value) {
		boolean outSimple = false;
        if (value instanceof Integer || value instanceof Long || value instanceof Double || value instanceof Float || value instanceof Boolean 
         || value instanceof java.util.Date || value instanceof java.sql.Date 
         || ReflectionUtils.isImplementing(value.getClass(),CharSequence.class) 
         || value instanceof Enum) {
        	outSimple = true;
        }
        return outSimple;		
	}
	/**
	 * @return el tipo del dataType representado por esta clase
	 */
	static Class<?> typeOf(final DataType dataType) {
		Class<?> outType = null;
        if (dataType.getTypeDef() == DataTypeEnum.STRING || dataType.getTypeDef() == DataTypeEnum.XML) {
        	outType = String.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.STRINGBUFFER) {
        	outType = StringBuffer.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.STRINGBUILDER) {
        	outType = StringBuilder.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.INTEGER || dataType.getTypeDef() == DataTypeEnum.INTEGER_P) {
        	outType = Integer.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.LONG || dataType.getTypeDef() == DataTypeEnum.LONG_P) {
        	outType = Long.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.DOUBLE || dataType.getTypeDef() == DataTypeEnum.DOUBLE_P) {
        	outType = Double.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.FLOAT || dataType.getTypeDef() == DataTypeEnum.FLOAT_P) {
        	outType = Float.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.NUMBER) {
        	outType = Number.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.BOOLEAN || dataType.getTypeDef() == DataTypeEnum.BOOLEAN_P) {
        	
        	outType = Boolean.class;
        } else if (dataType.isDate()) {   
        	outType = Date.class;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.ENUM) {
			outType = Reflection.type(dataType.asEnum().getEnumTypeName()).getType();
			
        } else if (dataType.isObject()) {
        	outType = Reflection.type(dataType.asObject().getName()).getType();
        	
        } else if (dataType.isCollection()) {
        	String colTypeName = dataType.getName();
        	if (colTypeName.equals("List")) colTypeName = LinkedList.class.getCanonicalName();
			outType = Reflection.type(colTypeName).getType();
			if (ReflectionUtils.isInterface(outType)) outType = outType.isAssignableFrom(Set.class) ? LinkedHashSet.class 
																									: LinkedList.class;
		} else if (dataType.isMap()) {
        	String mapTypeName = dataType.getName();
        	if (mapTypeName.equals("Map")) mapTypeName = LinkedHashMap.class.getCanonicalName();
			outType = Reflection.type(mapTypeName).getType();
			if (ReflectionUtils.isInterface(outType)) outType = LinkedHashMap.class;	// Por defecto cuando no se indica el tipo concreto, se instancia un Map
			
		} else if (dataType.isJavaType()) {
			outType = Class.class;
		}
        return outType;
	}
	/**
	 * Obtiene un objeto del tipo correcto a partir de la cadena
	 * @param dataType el tipo de dato a obtener
	 * @param valueStr la cadena con los datos
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static Object simpleObjFromString(final DataType dataType,
									  final CharSequence valueStr) {
		boolean ignoreWhiteSpace = true;	

		Object outValueObj = null;
		String theValueStr = ignoreWhiteSpace ? Strings.of(valueStr).trim().asString()
											  : valueStr.toString();
        // Dependiendo del tipo de dato, hacer las transformaciones correspondientes..
        if (dataType.getTypeDef() == DataTypeEnum.STRING || dataType.getTypeDef() == DataTypeEnum.XML) {
        	outValueObj = theValueStr;	
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.STRINGBUFFER) {
        	outValueObj = new StringBuffer(theValueStr);
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.STRINGBUILDER) {
        	outValueObj = new StringBuilder(theValueStr);
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.INTEGER || dataType.getTypeDef() == DataTypeEnum.INTEGER_P) {
        	outValueObj = new Integer(theValueStr);
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.LONG || dataType.getTypeDef() == DataTypeEnum.LONG_P) {
        	outValueObj = new Long(theValueStr);
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.DOUBLE || dataType.getTypeDef() == DataTypeEnum.DOUBLE_P) {
        	outValueObj = new Double(theValueStr);
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.FLOAT || dataType.getTypeDef() == DataTypeEnum.FLOAT_P) {
        	outValueObj = new Float(theValueStr);
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.NUMBER) {
        	if (Numbers.isInteger(theValueStr)) {
        		outValueObj = new Integer(theValueStr);
        	} else if (Numbers.isLong(theValueStr)) {
        		outValueObj = new Long(theValueStr);
        	} else if (Numbers.isDouble(theValueStr)) {
        		outValueObj = new Double(theValueStr);
        	} else if (Numbers.isFloat(theValueStr)) {
        		outValueObj = new Float(theValueStr);
        	}
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.BOOLEAN || dataType.getTypeDef() == DataTypeEnum.BOOLEAN_P) {
            if (theValueStr.equalsIgnoreCase("1") || theValueStr.equalsIgnoreCase("true")) {
                theValueStr = "true";
            } else if (theValueStr.equalsIgnoreCase("0") || theValueStr.equalsIgnoreCase("false")) {
                theValueStr = "false";
            }
        	outValueObj = new Boolean(theValueStr); 
        	
        } else if (dataType.isDate()) {   
        	// Obtener la mascara de formateo de fechas
        	String format = dataType.asDate().getDateFormat();
        	Date date = Dates.fromFormatedString(theValueStr,format);
        	outValueObj = date;
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.ENUM || dataType.getType().isEnum()) {
        	// Obtener un enum a partir del valor
			Class<? extends Enum> enumType = (Class<? extends Enum>)dataType.getType();
        	outValueObj = Enum.valueOf(enumType,theValueStr);
        	
        } else if (dataType.getTypeDef() == DataTypeEnum.JAVACLASS) {
        	// Se trata de una definición de un tipo java
        	// ej	private Class<?> _myJavaType
        	// En el XML SIEMPRE llega como JavaType(-tipo java-)
        	Pattern p = Pattern.compile("JavaType\\(([^)]+)\\)");
        	Matcher m = p.matcher(valueStr);
        	if (m.find()) {
        		outValueObj = ReflectionUtils.typeFromClassName(m.group(1));
        	}
        } else if (dataType.isCanBeCreatedFromString()) { //(ReflectionUtils.canBeCreatedFromString(dataType.getType()))
        	// Try to create the object using a single String param constructor or a valueOf(String) static method
        	outValueObj = ReflectionUtils.createInstanceFromString(dataType.getType(),
        														   theValueStr); 
        } else if (dataType.isObject()
        	    && dataType.asObject().isInmutable()							// inmutable object
	        	&& dataType.asObject().hasOnlyOneFinalSimpleField()) {			// field to hold the id
        	// An object with a single final field
        	// - Create the field
    		DataType argDataType = dataType.asObject()
    									   .getSingleFinalSimpleField();
    		Class<?> finalFieldType = argDataType.getType();
    		Object finalFieldInstance = MappingReflectionUtils.simpleObjFromString(argDataType,
    																			   theValueStr);    		
    		// - Create the out object
    		outValueObj = ReflectionUtils.createInstanceOf(dataType.getType(),
    													   new Class<?>[] {finalFieldType},new Object[] {finalFieldInstance});
        } else if (dataType.isObject() 
	        	&& dataType.asObject().hasOnlyOneSimpleField()) {
        	// An object with a single field
        	// - Create the field
    		DataType argDataType = dataType.asObject()
    									   .getSingleSimpleField();
    		Class<?> fieldType = argDataType.getType();
    		Object fieldInstance = MappingReflectionUtils.simpleObjFromString(argDataType,
    																		  theValueStr);
    		// - Create the out object and set the field value
    		outValueObj = ReflectionUtils.createInstanceOf(dataType.getType(),
    													   new Class<?>[] {fieldType},new Object[] {fieldInstance});
		} else {
        	log.error("An object of type {} was tried to be created from a String {} but it was NOT possilbe",
        			  dataType.getType(),valueStr.toString());
        }
        return outValueObj;        				
	}
}
