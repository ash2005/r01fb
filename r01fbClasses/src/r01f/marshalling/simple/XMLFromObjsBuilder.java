package r01f.marshalling.simple;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;

import r01f.encoding.TextEncoder;
import r01f.exceptions.Throwables;
import r01f.locale.Language;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.MarshallerMappings;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.marshalling.simple.XMLBuilder.XMLElement;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.googlecode.gentyref.GenericTypeReflector;



/**
 * Se encarga de obtener XML a partir de una jerarquía de objetos.
 * Para saber el formato del XML a obtener (su DTD en definitiva), se 
 * utiliza un documento XML que define el mapeo entre el XML y los objetos
 * (ver para ello el objeto XOMap)
 */
class XMLFromObjsBuilder {
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
	private final SimpleMarshallerMappings _beanMappings; 	// Definición de las clases que se van a cargar
                 											// y como estas se estructuran en el XML
    
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////        
    public XMLFromObjsBuilder(MarshallerMappings map) {
    	_beanMappings = (SimpleMarshallerMappings)map;
    }     
///////////////////////////////////////////////////////////////////////////////////////////
//  PUBLIC INTERFACE
///////////////////////////////////////////////////////////////////////////////////////////            
    /**
     * Devuelve la representación XML del objeto que se pasa.
     * Anteriormente se ha debido establecer el mapeo entre objetos y XML,
     * utilizando el constructor o el método setORMap
     * @param obj El objeto cuya representacion XML hay que obtener
     * @param xmlCharset el charset del xml de salida
     * @return el xml 
     * @throws MarshallerException si no puede convertir
     */
    public String xmlFrom(final Object obj,
    					  final Charset xmlCharset,
    					  final TextEncoder encoder) throws MarshallerException {
        // [1] - Obtener la definición de la clase del objeto que se pasa    
        BeanMap beanMap = _beanMappings.getBeanMapFromClassName( obj.getClass().getName() );
        XMLElement xml = null;
        if (obj instanceof Map) {
        	String tagName = null;
        	if (beanMap != null) {
        		tagName = beanMap.getXmlMap().getNodeName();	// El objeto extiende de Map y está anotado con @XmlRootElement cuyo nombre se toma para el nodo XML Wrapper de la colección
        	} else {	
        		tagName = "map";								// El objeto es un java.util.Map, NO es un objeto que extiende de Map y por lo tanto NO está anotado							
        	}
        	xml = new XMLElement();		// Wrapper node
        	xml.setTag(tagName);
        	for (Map.Entry<?,?> me : ((Map<?,?>)obj).entrySet()) {
        		if (me.getValue() == null) continue;
        		BeanMap childBeanMap = _beanMappings.getBeanMapFromClassName( me.getValue().getClass().getName() );
        		XMLElement childEl = _generateXML(null,me.getValue(),null,childBeanMap);
        		xml.newChildElement(childEl);
        	}
        } else if (obj instanceof Collection) {
        	String tagName = null;
        	if (beanMap != null) {
        		tagName = beanMap.getXmlMap().getNodeName();	// El objeto extiende de Map y está anotado con @XmlRootElement cuyo nombre se toma para el nodo XML Wrapper de la colección
        	} else if (obj instanceof List) {
        		tagName = "list";								// El objeto es un java.util.List, NO es un objeto que extiende de List y por lo tanto NO está anotado
        	} else if (obj instanceof Set) {
        		tagName = "set";								// El objeto es un java.util.Set, NO es un objeto que extiende de Set y por lo tanto NO está anotado
        	}
        	xml = new XMLElement();		// Wrapper node
        	xml.setTag(tagName);
        	for (Object o : (Collection<?>)obj) {
        		if (o == null) continue;
        		BeanMap childBeanMap = _beanMappings.getBeanMapFromClassName( o.getClass().getName() );
        		XMLElement childEl = _generateXML(null,o,null,childBeanMap);
        		xml.newChildElement(childEl);
        	}
        } else {
        	// Caso mas habitual: se trata de un objeto que NO es una colección o mapa
        	xml = _generateXML(null,obj,null,beanMap);
        }
               
        // [2] - Obtener el xml
        Charset theCharset = xmlCharset != null ? xmlCharset 
        										: Charset.defaultCharset();
        return Strings.of(xml.asText(encoder).toString())
        			  .encode(theCharset)
        			  .asString();       
    }
        
    
///////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////////////////////       
    /**
     * Funcion recursiva que obtiene un nodo XML que representa al 
     * objeto que se pasa
     * @param xmlElTag El nombre del tag para el nuevo objeto.
     *                 Si es null, se toma el dato de la definición del objeto (inObjClassDef._tagName)
     * @param inObj El objeto del que se quiere obtener la representacion XML
     * @param fieldMap mapeo del campo en el bean padre
     * @param beanMap mapeo del objeto
     */
    private XMLElement _generateXML(final String xmlElTag,
    								final Object inObj,
    								final FieldMap fieldMap,final BeanMap beanMap) throws MarshallerException {
        if (beanMap == null) throw new MarshallerException(Throwables.message("Mapping for type {} was not found",inObj.getClass().getName()));
        
        XMLElement outXmlEl = null;
        if (beanMap.isCustomXmlTransformed()) {
        	String rawXmlEl = beanMap.getCustomXMLTransformers().getXmlWriteTransformer()
        														.xmlFromBean(fieldMap.getXmlMap().isAttribute(),
        																	 inObj);
        	outXmlEl = XMLElement.createRaw(rawXmlEl);
        } else {
        	outXmlEl = _xmlElement(xmlElTag,
        						   inObj,
        						   fieldMap,beanMap);
        }
        return outXmlEl;
    }
    private XMLElement _xmlElement(final String xmlElTag,
    							   final Object inObj,
    							   final FieldMap fieldMap,final BeanMap beanMap) throws MarshallerException {        
        String fieldName = null;
        Object fieldValue = null;
        
        // Crear el elemnto XML para el objeto
        // (si se pasa un nombre para el tag, utilizar ese; en otro caso, especificar el especificado en el mapeo del bean)
        XMLElement xmlEl = XMLElement.create( Strings.valueOrDefault(xmlElTag,
        															 beanMap.getXmlMap().getNodeName()) );

        // Si el tipo NO es instanciable (ej es un interfaz o clase abstracta) 
        if (fieldMap != null && !fieldMap.getDataType().isInstanciable()) {
        	_includeTypeDiscriminatorAttributeInTag(xmlEl,
												    beanMap,fieldMap);
        }
        if (CollectionUtils.isNullOrEmpty(beanMap.getFields())) return xmlEl;
        try {
        	// Procesar los miembros del objeto
        	for (FieldMap currFieldMap : beanMap.getFields().values()) {	
        		if (currFieldMap.isTranzient()) {
        			continue;
        			
        		} else if (currFieldMap.getDataType().isJavaType()) {
        			// El miembro es una definición de un objeto java
        			// Ej: 	private Class<?> _myJavaType
        			Class<?> typeDef = ReflectionUtils.fieldValue(inObj,currFieldMap.getName(),
        														  beanMap.isUseAccessors());
        			if (typeDef != null) {
        				String typeName = GenericTypeReflector.getTypeName(typeDef);
        				String typeNameStr = typeName != null ? ("JavaType(" + typeName + ")") : null;	// SIEMPRE se serializa como JavaType(-nombre tipo java-)
        				if (typeNameStr != null && currFieldMap.getXmlMap().isAttribute()) {	
        					// Atributo
        					xmlEl.addAttribute(currFieldMap.getXmlMap().getNodeName(),
        									   typeNameStr);
        				} else if (typeNameStr != null && currFieldMap.getXmlMap().getNodeName().equals(beanMap.getXmlMap().getNodeName())) {
        					// Body del tag xml
	        				xmlEl.withText(typeNameStr);
        				} else if (typeName != null) {
        					// Nuevo elemento del tag xml
	        				XMLElement beanXmlEl = XMLElement.create(currFieldMap.getXmlMap().getNodeName())
	        												 .withText(typeNameStr);
	        				xmlEl.newChildElement(beanXmlEl);
        				}
        			}
        			
        		} else if (currFieldMap.getXmlMap().isAttribute()) {
        			// ::::::::::::: Atributos
	                fieldName = currFieldMap.getName(); 		// Nombre del miembro 
	            	fieldValue = MappingReflectionUtils.getFieldValue(inObj,fieldName,beanMap.isUseAccessors(),
	            													  currFieldMap.getDataType().getType());
        			
	            	// Si el objeto no tiene valor no serializar
	            	if (fieldValue == null) continue;
	            	
        			if (!currFieldMap.getXmlMap().isExpandableAsAttributes()) {
        				// simple text-conversible fields
        				//     @XmlRootElement(name="myContainerType")								
        				//     public class MyContainerType {
        				// 	   	    @XmlAttribute(name="attr")
        				// 	   	    @Getter @Setter private String _attrField;
        				//     }
        				//	   Will generate the following XML
        				//			<myContainerType attr="..." />
		            	// Ignorar fields de tipo simple cuando su valor coincide con el establecido para ignorar
		            	if (!Strings.isNullOrEmpty(currFieldMap.getXmlMap().getValueToIgnoreWhenWritingXML())) {
		            		if (fieldValue.toString().equals(currFieldMap.getXmlMap().getValueToIgnoreWhenWritingXML())) fieldValue = null;
		            	}
		            	
		            	// Serializar
		            	String fieldValueStr = null;
		            	if (currFieldMap.getDataType().getBeanMap() != null 
		            	 && currFieldMap.getDataType().getBeanMap().getCustomXMLTransformers() != null) {
		            		fieldValueStr = currFieldMap.getDataType().getBeanMap().getCustomXMLTransformers()
		            															   .getXmlWriteTransformer()
		            															   .xmlFromBean(true,
		            																	   		fieldValue);
		            	} else {
			                fieldValueStr = MappingReflectionUtils.formatAsString(currFieldMap,
			                													  fieldValue);
		            	}
		                if (fieldValueStr == null && currFieldMap.getDataType() != null && currFieldMap.getDataType().isObject()) fieldValueStr = fieldValue.toString();		// par aqui pasa para objetos inmutables (ej oid)
		                
		                if (fieldValueStr != null) xmlEl.addAttribute(currFieldMap.getXmlMap().getNodeName(),fieldValueStr);
        			} else {
        				// complex fields that are "expanded" as attributes:
        				//     @XmlRootElement(name="myContainerType")								
        				//     public class MyContainerType {
        				// 	   	    @XmlAttribute(name="whatEver")
        				// 	   	    @Getter @Setter private MyExpandableType _expandableField;
        				//     }
        				//     public class MyExpandableType {										
        				// 	   	    @XmlAttribute(name="attr1")
        				// 	   	    @Getter @Setter private String _attrField1;
        				//     
        				// 	   	    @XmlAttribute(name="attr1")
        				// 	   	    @Getter @Setter private String _attrField2;
        				//     }
        				//	   Will generate the following XML
        				//			<myContainerType attr1="..."	<-- comes from _attrField1 of MyExpandableType
        				//							 attr2="..."/>	<-- comes from _attrField2 of MyExpandableType
        				BeanMap expandableObjBeanMap = currFieldMap.getDataType().getBeanMap(); 
        				for (FieldMap expandableFieldMap : expandableObjBeanMap.getFields().values()) {
			                String expandableFieldName = expandableFieldMap.getName(); 		// Nombre del miembro 
			            	Object expandableFieldValue = MappingReflectionUtils.getFieldValue(fieldValue,expandableFieldName,expandableObjBeanMap.isUseAccessors(),
			            													  				   expandableFieldMap.getDataType().getType());
			            	// Ignorar fields de tipo simple cuando su valor coincide con el establecido para ignorar
			            	if (!Strings.isNullOrEmpty(currFieldMap.getXmlMap().getValueToIgnoreWhenWritingXML())) {
			            		if (expandableFieldValue.toString().equals(currFieldMap.getXmlMap().getValueToIgnoreWhenWritingXML())) fieldValue = null;
			            	}
			            	// Si el objeto no tiene valor no serializar
			            	if (fieldValue == null) continue;
			            	
			            	// Serializar
			                String fieldValueStr = MappingReflectionUtils.formatAsString(expandableFieldMap,
			                															 expandableFieldValue);
			                if (fieldValueStr == null && expandableFieldMap.getDataType() != null && expandableFieldMap.getDataType().isObject()) fieldValueStr = expandableFieldValue.toString();		// par aqui pasa para objetos inmutables (ej oid)
			                
			                if (fieldValueStr != null) xmlEl.addAttribute(expandableFieldMap.getXmlMap().getNodeName(),fieldValueStr);
	        			}
        			}
	                
        		} else {
        			// ::::::::::::: Elemento
	        		fieldName = currFieldMap.getName(); 		// Nombre del miembro
	            	fieldValue = MappingReflectionUtils.getFieldValue(inObj,fieldName,beanMap.isUseAccessors(),
	            													  currFieldMap.getDataType().getType());

	            	// Ignorar fields de tipo simple cuando su valor coincide con el establecido para ignorar
	            	if (!Strings.isNullOrEmpty(currFieldMap.getXmlMap().getValueToIgnoreWhenWritingXML())) {
	            		if (fieldValue.toString().equals(currFieldMap.getXmlMap().getValueToIgnoreWhenWritingXML())) fieldValue = null;
	            	}
	            	// Si el objeto no tiene valor no serializar
	            	if (fieldValue == null) continue;
	            	 
	            	// [[[[[[--- Colecciones (mapas, listas y arrays...)
	            	if (currFieldMap.getDataType().isCollection() || currFieldMap.getDataType().isMap()) {
	            		// [0] - Obtener los items de la colección
	            		//			- Si es una lista o un array se devuelven directamente los objetos en una colección
	            		//			- Si es un mapa se devuelve una colección de Map.Entry
	            		Collection<?> colItems = MappingReflectionUtils.getCollectionElements(fieldValue);
	            		if (CollectionUtils.isNullOrEmpty(colItems)) continue;
	            		
	            		// [1] - Tag que "engloba" los elementos de la colección (lista o mapa)
		            	// 		 Determinar si el elemento "padre" es un tag que "engloba" todos los elementos de la colección
		            	// 		 o si el elemento "padre" es el propio tag que "engloba" al objeto
		            	XMLElement colEnclosingXmlEl = null;
		            	if ( currFieldMap.getXmlMap().getNodeName().equals(beanMap.getXmlMap().getNodeName()) ) {	// el nombre del tag del elemento de la colección coincide con el nombre del tag del padre
		            		colEnclosingXmlEl = xmlEl;
		            	} else {																					// los elementos de la colección se "envuelven" en un tag que se indica
		            		colEnclosingXmlEl = XMLElement.create(currFieldMap.getXmlMap().getNodeName());
		            		xmlEl.newChildElement(colEnclosingXmlEl);
		            	}            		            		
	            		// [2] - Pasar a XML cada uno de los objetos de la colección y ponerlos dentro del elemento XML que los "engloba" creado en [1]  
		            	int i=0;
	            		for (Object colItem : colItems) {
	            			XMLElement colXmlEl = null;
	            			
            				// Objeto real del item (ver lo que devuelve MappingReflectionUtils.getCollectionElements(fieldValue))
            				Object colItemObj = currFieldMap.getDataType().isMap() ? ((Map.Entry<?,?>)colItem).getValue()	// - si es un map.entry es el value
            																	   : colItem;								// - si es una lista o un array es directamente el objeto
            				if (colItemObj == null) continue;	// ignore items with no value
            				
            				// ... y su mapeo
            				DataType colItemObjDataType = currFieldMap.getDataType().isCollection() ? currFieldMap.getDataType().asCollection().getValueElementsDataType()
            																					    : currFieldMap.getDataType().asMap().getValueElementsDataType();
	            			
	            			// 2.1 - elementos simples y enums
	            			if (MappingReflectionUtils.isSimple(colItemObj)) {
	            				// Si NO es posible saber el tipo del objeto (ej: Number)
	            				// En ocasiones NO es posible saber el tipo de objeto que va a contener una colección en tiempo de carga del mapeo
	            				// y el tipo de los items es Object, sin embargo, AQUI, ya se sabe el tipo concreto a partir del tipo del objeto
	            				// que se está pasando a XML, es necesario "dejar" algo de infomración que permita posteriormente pasar de xml a java
	            				DataType actualColItemObjDataType = colItemObjDataType.isObject() ? DataType.create(colItemObj.getClass().getName())
	            																				  : colItemObjDataType;
	            				
	            				// Formatear como string y crear el tag
		            			String fieldValueStr = MappingReflectionUtils.formatAsString(actualColItemObjDataType,colItemObj);	// OJO!! pasar a String el objeto no el colItem (puesto que puede ser un Map.Entry)
		            			if (fieldValueStr != null) {
		            				// Obtener el nombre del tag para cada uno de los elementos del mapa
		            				String tagName = currFieldMap.getXmlMap().getColElsNodeName();
		            				if (currFieldMap.getDataType().isMap()) {
		            					if (tagName == null) {
		            						// Situación más habitual... el nombre del tag se toma como el oid del objeto
	            							tagName = ((Map.Entry<?,?>)colItem).getKey().toString();	// se indica un miembro OID en el mapeo
	            							
		            					} else if (((Map.Entry<?,?>)colItem).getKey() instanceof Language) {
		            						// Es un mapa de textos Map<Language,String> --> la clave del mapa es el lenguage
		            						tagName = ((Map.Entry<?,?>)colItem).getKey().toString();
		            						
		            					} else {
		            						// NO es lo habitual, pero cuando se trata de un mapa y se indica el tag
		            						// se añade un correlativo al nombre del tag... es lo mas que se puede hacer
		            						tagName = tagName + (i+1);
		            					}
		            				} else {	// 
		            					if (tagName == null) tagName = "item" + (currFieldMap.getDataType().isMap() ? i : "");		// por defecto...	            					
		            				}            								
		            				// Crear el nodo
		            				colXmlEl = XMLElement.create(tagName)
		            									 .cdata(currFieldMap.getXmlMap().isCdata())
		            									 .withText(fieldValueStr);
		            				
		            				// Si NO es posible saber el tipo de objeto, hay que "dejar" información que permita pasar de xml a java
		            				// la única opción es añadir un atributo que indique el tipo para lo que es necesario utilizar el valor de
		            				// la anotación XmlTypeDiscriminatorAttribute
		            				if (colItemObjDataType.isObject() && !colItemObjDataType.is(Language.class)  
		            				 || !colItemObjDataType.isInstanciable()) {
		            					if (currFieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable() == null) throw new MarshallerException("El miembro '" + currFieldMap.getName() + "' del tipo " + beanMap.getDataType() + " es una colección cuyos tipos son tipos simples DESCONOCIDOS, así que hay que indicar el tipo en un atributo del XML. Es necesario anotar el miembro '" + currFieldMap.getName() + "' con @XmlTypeDiscriminatorAttribute(name=\"nombre_atributo\")");
		            					_includeTypeDiscriminatorAttributeInTag(colXmlEl,
		            														    colItemObj.getClass(),currFieldMap);
		            				}
		            			}
		            		// 2.2 - elementos complejos (objetos)
	            			} else if (colItemObjDataType.isObject()) {
	            				// Si el mapa está indexado por lenguaje, el objeto está dentro del tag language:
	            				//		<SPANISH>
	            				//			[objeto]
	            				//		</SPANISH>
	            				XMLElement langWrapperNode = null;
	            				if (currFieldMap.getDataType().isMap() && ((Map.Entry<?,?>)colItem).getKey() instanceof Language) {
	            					String langNodeName = ((Map.Entry<?,?>)colItem).getKey().toString();
	            					langWrapperNode = XMLElement.create(langNodeName);
	            				} 
	            				
	            				// Obtener el mapeo del objeto de la colección
	            				BeanMap colItemObjBeanMap = colItemObjDataType.getBeanMap();		            				
	            				if (colItemObjBeanMap == null) colItemObjBeanMap = _beanMappings.getBeanMapFromClassName(colItemObj.getClass().getName());
	            				if (colItemObjBeanMap == null) throw new MarshallerException("NO se encuentra el mapeo de la clase " + colItemObj.getClass().getName() + " especificado como elemento de la colección " + currFieldMap.getName() + " en la clase " + currFieldMap.getDeclaringBeanMap().getTypeName() + ". Revisa el documento de mapeo");
	            				
	            				// Nombre del nodo que envuelve el objeto complejo
	            				String nodeName = null;
	            				String typeDiscriminatorAttr = null;
	            				BeanMap actualColItemBeanMap = colItemObjDataType.isInstanciable() ? colItemObjBeanMap
	            																				   : _beanMappings.getBeanMapFromClassName(colItemObj.getClass().getName());
           						if (actualColItemBeanMap == null) throw new MarshallerException("El miembro '" + currFieldMap.getName() + "' del tipo " + beanMap.getTypeName() + " es una colección cuyos elementos son de un tipo desconocido o no instanciable: " + colItemObjDataType.getName() + ". Para dejar informacion en el xml que permita pasar a java de nuevo es necesario establecer la anotacion @XmlRootElement en el tipo " + colItemObj.getClass().getName());
	            				
           						if (!colItemObjDataType.isInstanciable()) {
		            				// En ocasiones NO es posible saber el tipo de objeto que va a contener una colección en tiempo de carga del mapeo,
		            				// sin embargo, AQUI, ya se sabe el tipo concreto a partir del tipo del objeto que se está pasando a XML
		            				// Hay dos opciones para "dejar" información en el XML que permita posteriormente pasar a java de nuevo
		            				//		1.- Utilizar el tag del objeto actual (el valor de la anotación @XmlRootElement del objeto actual
		            				//		2.- Añadir un atributo que especifique el tipo; el nombre del atributo se toma de la anotación @XmlTypeDiscriminatorAttribute
	            					typeDiscriminatorAttr = currFieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable();
	            					if (!Strings.isNullOrEmpty(typeDiscriminatorAttr)) {
	            						nodeName = currFieldMap.getXmlMap().getColElsNodeName();
	            					} else {
	            						nodeName = actualColItemBeanMap.getXmlMap().getNodeName();
	            					} 
	            				} else {
	            					// Caso más normal (el tipo es instanciable)
	            					nodeName = currFieldMap.getXmlMap().isExplicitNodeName() ? currFieldMap.getXmlMap().getColElsNodeName()
	            																			 : actualColItemBeanMap.getXmlMap().getNodeName();
	            				}           						
           						if (Strings.isNullOrEmpty(nodeName)) nodeName = actualColItemBeanMap.getXmlMap().getNodeName();	// last resort
           						
	            				// llamar recursivamente a este método
	            				colXmlEl = _generateXML(nodeName,
	            										colItemObj,			
	            										currFieldMap,actualColItemBeanMap); 
	            				// añadir el discriminador de tipo si es necesario
	            				if (typeDiscriminatorAttr != null) colXmlEl.addAttribute(typeDiscriminatorAttr,
	            																		 actualColItemBeanMap.getXmlMap().getNodeName());
	            				// si se trata de un mapa indexado por lenguaje, el nodo a añadir es realmente el nodo del idioma
	            				if (langWrapperNode != null) {
	            					langWrapperNode.newChildElement(colXmlEl);
	            					colXmlEl = langWrapperNode;
	            				}
	            			}            			
	            			if (colXmlEl != null && !colXmlEl.isEmpty()) {
	            				colEnclosingXmlEl.newChildElement(colXmlEl);
	            			}
	            			i++;
	            		}            	            	
	                    
	            	// [[[[[[--- Objetos normales (NO colecciones) 
	                } else {
	        			String fieldValueStr = MappingReflectionUtils.formatAsString(currFieldMap,
	        																		 fieldValue);
	        			if (fieldValueStr != null) { 
	        				// [1] el miembro es un tipo "simple" (String, Long, Date, etc)        				
			            	// Determinar si el elemento "padre" es un tag que "engloba" todos los elementos de la colección
			            	// o si el elemento "padre" es el propio tag que "engloba" al objeto
	        				if (currFieldMap.getXmlMap().getNodeName() != null 
	        				 && currFieldMap.getXmlMap().getNodeName().equals(beanMap.getXmlMap().getNodeName())) {
	        					xmlEl.withText(fieldValueStr);
	        					if (currFieldMap.getXmlMap().isCdata()) xmlEl.setCDATA(true);

	        				} else {
	        					String nodeName = currFieldMap.getXmlMap().getNodeName() != null ? currFieldMap.getXmlMap().getNodeName()
	        																					 : _beanMappings.getBeanMapFromClassName(fieldValue.getClass().getName()).getXmlMap().getNodeName();
		        				XMLElement beanXmlEl = XMLElement.create(nodeName)
		        												 .cdata(currFieldMap.getXmlMap().isCdata())
		        												 .withText(fieldValueStr);
		        				xmlEl.newChildElement(beanXmlEl);
		        				
		        				// Si el tipo NO es instanciable (ej es un interfaz o clase abstracta)
		        				if ((currFieldMap.getDataType().getType() == Object.class && currFieldMap.getXmlMap().getNodeName() != null && currFieldMap.getXmlMap().isExplicitNodeName())
		        					||
		        				    (!currFieldMap.getDataType().isInstanciable())) {
		        					if (ReflectionUtils.isTypeWithin(fieldValue.getClass(),
		        													 String.class,Long.class,Double.class,Integer.class,Float.class,Short.class,java.util.Date.class,java.sql.Date.class)) {
		        						_includeTypeDiscriminatorAttributeInTag(beanXmlEl,
		        															    fieldValue.getClass(),currFieldMap);
		        					} else {
			        					BeanMap currFieldBeanMap = _beanMappings.getBeanMapFromClassName(fieldValue.getClass().getName());
			        					if (currFieldBeanMap == null) throw new MarshallerException(Throwables.message("The type {} is NOT annotated with @{}",
			        																								   fieldValue.getClass().getName(),XmlRootElement.class));
			        					_includeTypeDiscriminatorAttributeInTag(beanXmlEl,
			        															currFieldBeanMap,currFieldMap);
		        					}
		        				}
	        				}
	        				
	        			} else {  
	        				// [2] El miembro es a su vez un bean "complejo"
	        				// Obtener el mapeo del bean hijo
	        				BeanMap elBeanMap = _beanMappings.getBeanMapFromClassName(fieldValue.getClass().getName());
	        				
	        				// If the mapping is NOT found... the type could be a Collection or a Map
		        			if (elBeanMap == null) {		        			
		        				if (ReflectionUtils.isImplementing(fieldValue.getClass(),Collection.class)) {
		        					XMLElement xml = new XMLElement();		// Wrapper node
		        					String tagName = "list";
		        					if (fieldValue instanceof Set) tagName = "set";
						        	xml.setTag(tagName);
						        	for (Object o : (Collection<?>)fieldValue) {
						        		if (o == null) continue;
						        		BeanMap childBeanMap = _beanMappings.getBeanMapFromClassName( o.getClass().getName() );
						        		XMLElement childEl = _generateXML(null,o,null,childBeanMap);		// recurse
						        		xml.newChildElement(childEl);
						        	}
						        	xmlEl.newChildElement(xml);
		        				} else if (ReflectionUtils.isImplementing(fieldValue.getClass(),Map.class)) {
		        					XMLElement xml = new XMLElement();		// Wrapper node
		        					xml.setTag("map");
						        	for (Map.Entry<?,?> me : ((Map<?,?>)fieldValue).entrySet()) {
						        		if (me.getValue() == null) continue;
						        		BeanMap childBeanMap = _beanMappings.getBeanMapFromClassName( me.getValue().getClass().getName() );
						        		XMLElement childEl = _generateXML(null,me.getValue(),null,childBeanMap);
						        		xml.newChildElement(childEl);
						        	}
						        	xmlEl.newChildElement(xml);
		        				} else {
		        					throw new MarshallerException("NO se encuentra el mapeo de la clase " + fieldValue.getClass().getName() + " miembro " + currFieldMap.getName() + " de la clase " + beanMap.getDataType().getName() + ". Revisa el documento de mapeo");
		        				}
		        			}
		        			// It's a "normal" mapped object
		        			else {        					
	            				String nodeName = currFieldMap.getXmlMap().isExplicitNodeName() ? currFieldMap.getXmlMap().getNodeName()
	            																				: elBeanMap.getXmlMap().getNodeName();
			        			XMLElement beanXmlEl = _generateXML(nodeName,
			        										 		fieldValue,
			        										 		currFieldMap,elBeanMap); 	// recurse	
			        			if (!beanXmlEl.isEmpty()) xmlEl.newChildElement(beanXmlEl);
		        			}
	        			}                
	                }        			
        		}
            }	// for
        } catch (ReflectionException refEx) {
            throw new MarshallerException("Error al acceder al valor de la variable miembro '" + fieldName + "' del bean '" + beanMap.getTypeName() + "'",refEx); 
        } 
        return xmlEl;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS AUXILIARES
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Si el tipo NO es instanciable (ej es un interfaz o clase abstracta) se pueden dar dos casos:
     * A.- Si el nombre del tag se ha proporcionado EXPLICITAMENTE en el field,
     *			Ej: 	@XmlElement(name="field") private MyType _field;	<-- el nombre del tag se da explicitamente (@XmlElement(name="field"))
     *	   es necesario añadir en el elemento XML un atributo que permita posteriormente discriminar el tipo concreto 
     *	   al pasar de XML a objetos
     * 	   El nombre del atributo xml se indica en la anotacion @XmlDiscriminator
     * B.- Si el nombre del tag NO se proporciona EXPLICITAMENTE en el field
     *			Ej: 	@XmlElement private MyType _field;		<-- el nombre del tag NO se da explicitamente (@XmlElement -sin atributo name-)
     */
    private static void _includeTypeDiscriminatorAttributeInTag(final XMLElement xmlEl,
    													 		final BeanMap beanMap,final FieldMap fieldMap) {
    	if (fieldMap.getXmlMap().isExplicitNodeName()) {
    		xmlEl.addAttribute(fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable(),
    						   beanMap.getXmlMap().getNodeName());
    	}
    }
    private static void _includeTypeDiscriminatorAttributeInTag(final XMLElement xmlEl,
    													 		final Class<?> objType,final FieldMap fieldMap) {
		xmlEl.addAttribute(fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable(),
						   objType.getName());
    }
    
}
