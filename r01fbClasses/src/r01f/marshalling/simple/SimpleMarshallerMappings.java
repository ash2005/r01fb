package r01f.marshalling.simple;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import lombok.Cleanup;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import r01f.marshalling.MarshallerException;
import r01f.marshalling.MarshallerMappings;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.marshalling.simple.DataTypes.DataTypeEnum;
import r01f.marshalling.simple.SimpleMarshallerBuilder.SimpleMarshallerReusableImpl;
import r01f.marshalling.simple.SimpleMarshallerBuilder.SimpleMarshallerSingleUseImpl;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Se encarga de construir un modelo de objetos en memoria que tiene la información para realizar el marshalling / unmarshalling de 
 * XML a objetos y viceversa
 * Esta información de mapeo se puede obtener de dos formas:
 * 		- Con un XML de definición del mapeo (ver {@link SimpleMarshallerMappingsFromXMLLoader})
 * 		- Desde anotaciones en las clases java (ver {@link MarshallerMappingsFromAnnotationsLoader})
 * 
 * IMPORTANTE!! (ver {@link SimpleMarshallerBase}, {@link SimpleMarshallerSingleUseImpl} y {@link SimpleMarshallerReusableImpl}
 * 	- Si se van a hacer muchas operaciones de marshalling/unmarshalling, conviene
 * 	  CACHEAR este objeto, es decir, que este objeto sea UNICO, puesto que se reutiliza una y otra vez
 * 	  Ej: persistencia de objetos en BBDD en formato XML: se está continuamente transformando
 * 		  objetos a XML... es NECESARIO cachear el mapeo
 * 
 * 	- Si únicamente se va a hacer UNA OPERACIÓN de marshalling/unmarshalling sobre
 * 	  un determinado tipo de objeto, NO tiene sentido mantener una cache de la definición
 * 	  del mapeo.
 * 	  Ej: carga de configuración de un XML: se pasa el XML a objetos y lo que se cachean
 * 		  con los objetos... NO es necesario volver a pasar de XML a objetos
 * 
 * Internamente se utilizan dos clases auxiliares:
 *      - BeanMap:  Modela una clase (nombre, paquete, miembros, tag por defecto de la que procede, etc)
 *      - FieldMap: Modela un miembro de una clase (nombre, tag del que procede, etc)
 * Al final en memoria se tiene una estructura como la siguiente:
 *
 * Clase1
 *   |_ Miembro 1 (String)
 *   |_ Miembro 2 (Clase 3) --------|
 *   |_ Miembro 3 (Clase 2) ----|   |
 *                              |   |
 *                              |   |
 *   |--------------------------|   |
 * Clase 2                          |
 *   |_ Miembro 1 (String)          |
 *   |_ Miembro 2 (Clase 3) ----|   |
 *                              |   |
 *                              |   |
 *   |--------------------------|   |
 *   |------------------------------|
 *   |
 * Clase 3
 *   |_ Miembro 1 (Long)
 *   |_ Miembro 2 (String)
 */
@Slf4j
@NoArgsConstructor
public class SimpleMarshallerMappings 
  implements MarshallerMappings {
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
// 	El loadFactor de un mapa es una medida de cómo de llena puede estar una tabla ANTES de que                          
// 	se incremente su capacidad: cuando numEntradas > loadFactor*capacidadActual la tabla interna es                     
// 	re-construida y se dobla el número de buckets (capacidad)                                                           
// 	NOTA:                                                                                                               
//		- Un HashMap almacena entradas por hash de la clave en una serie de posiciones (buckets)                       
//		  El posible que en una misma posición (bucket) se almacenen MAS DE UNA ENTRADA por dos razones:               
//			1.- Cuando se inserta una nueva entrada tiene el mismo hashCode que una existente en el bucket,            
//					En este caso se llama a equals() para saber si en realidad es la misma entrada (se sustituye)      
//					o son diferentes (en el bucket hay dos entradas)                                                   
// 			2.- Se ha excedido la capacidad de la tabla y obviamente en cada posición (bucket) hay que almacenar       
//				mas de una entrada; en este caso cuando se introduce una nueva entrada hay que comparar el hashCode    
//				con el de las entradas ya existentes en bucket:                                                        
//					- si el hash coincide hay que llamar a equals()                                                    
//					- si el hash no coincide simplemente se añade una nueva entrada en el bucket                       
// 	El segundo tipo de colisión esta afectado por la capacidad (número de buckets) del mapa, que en realidad            
// 	está afectado por el loadFactor (entre 0 y 1)                                                                       
//		Cuanto MENOR es el loadFactor, MENOS probable es una colisión (el número de buckets de la tabla                
//		se va a doblar más rápidamente con lo que es menos probable que se agote el espacio de la tabla                
//		y mas de una entrada acabe en el mismo bucket)                                                                 

	/**
	 * Mapa que contiene las clases relacionadas con su nombre
	 * (se va rellenando a medida que se cargan los mapeos: métodos loadFrom...)
	 */
	private Map<String,BeanMap> _beanMappingsByType = new HashMap<String,BeanMap>(200,0.3F);
	/**
	 * Mapa que contiene las clases relacionadas con el tag XML
	 * (se va rellenando a medida que se necesita saber que bean está "englobado" por un tag: método getBeanMapFromXmlTag)
	 */
	private Map<String,BeanMap> _beanMappingsByEnclosingXmlElement = new HashMap<String,BeanMap>(200,0.3F);	
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static SimpleMarshallerMappings createFrom(final Class<?>... annotatedTypes) {
		SimpleMarshallerMappings outMappings = new SimpleMarshallerMappings();
		if (CollectionUtils.hasData(annotatedTypes)) outMappings.loadFromAnnotatedTypes(annotatedTypes);
		return outMappings;
	}
	public static SimpleMarshallerMappings createFrom(final Package... packages) {
		SimpleMarshallerMappings outMappings = new SimpleMarshallerMappings();
		if (CollectionUtils.hasData(packages)) outMappings.loadFromAnnotatedTypesScanningPackages(packages);
		return outMappings;
	}
	public static SimpleMarshallerMappings createFrom(final Object... searchSpecs) {
		SimpleMarshallerMappings outMappings = new SimpleMarshallerMappings();
		if (CollectionUtils.hasData(searchSpecs)) outMappings.loadFromAnnotatedTypes(searchSpecs);
		return outMappings;
	}
	public static SimpleMarshallerMappings createFrom(final String filePath) {
		SimpleMarshallerMappings outMappings = new SimpleMarshallerMappings();
		if (Strings.isNOTNullOrEmpty(filePath)) outMappings.loadFromMappingDefFile(filePath);
		return outMappings;
	}
	public static SimpleMarshallerMappings createFrom(final File mapFile) {
		SimpleMarshallerMappings outMappings = new SimpleMarshallerMappings();
		if (mapFile != null) outMappings.loadFromMappingDefFile(mapFile);
		return outMappings;
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  ACCESO A LOS MAPPINGS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene la definición de una clase a partir del nombre de la clase
     * @param beanName Nombre de la clase
     */
    public BeanMap getBeanMapFromClassName(final String beanName) {
        if (_beanMappingsByType == null) return null;
        return _beanMappingsByType.get(beanName);
    }
    /**
     * Obtiene la definición de una clase a partir del tag xml
     * @param tagName Tag XML en el que se define la clase
     */
    public BeanMap getBeanMapFromXmlTag(final String tagName) {
        BeanMap outBean = _beanMappingsByEnclosingXmlElement.get(tagName);
        if (outBean != null) return outBean;
        // Buscar el bean que se asigna a este tag XML y cachear 
        for (BeanMap currBean : _beanMappingsByType.values()) {
        	if (currBean.getXmlMap() == null || currBean.getXmlMap().getNodeName() == null) continue;		// las clases con CustomXmlTransformers no tienen xmlMap
        	if (currBean.getXmlMap().getNodeName().equals(tagName)) {
        		outBean = currBean;
        		break;
        	}
        }
        if (outBean != null) _beanMappingsByEnclosingXmlElement.put(tagName,outBean);
        return outBean;
    } 
    
///////////////////////////////////////////////////////////////////////////////////////////
//  CARGA DE MAPPINGS
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public boolean containsType(Class<?> type) throws MarshallerException {
    	BeanMap bm = this.getBeanMapFromClassName(type.getClass().getName());
    	return bm != null;
    }
    @Override
	public void loadFromAnnotatedTypes(final Class<?>... annotatedTypes) throws MarshallerException {
		SimpleMarshallerMappingsFromAnnotationsLoader annotLoader = new SimpleMarshallerMappingsFromAnnotationsLoader(annotatedTypes);
        Map<String,BeanMap> mappings = annotLoader.getLoadedBeans();
    	_cacheBeansMappings(mappings);
    }
    @Override
    public void loadFromAnnotatedTypesScanningPackages(final Package... packages) throws MarshallerException {
		SimpleMarshallerMappingsFromAnnotationsLoader annotLoader = new SimpleMarshallerMappingsFromAnnotationsLoader(packages);
        Map<String,BeanMap> mappings = annotLoader.getLoadedBeans();
    	_cacheBeansMappings(mappings);
    }
    @Override
    public void loadFromAnnotatedTypes(Object... searchSpecs) throws MarshallerException {
		SimpleMarshallerMappingsFromAnnotationsLoader annotLoader = new SimpleMarshallerMappingsFromAnnotationsLoader(searchSpecs);
        Map<String,BeanMap> mappings = annotLoader.getLoadedBeans();
    	_cacheBeansMappings(mappings);    	
    }
    @Override
	public void loadFromMappingDefFile(final String filePath) throws MarshallerException {
    	File mapFile = new File(filePath);
    	this.loadFromMappingDefFile(mapFile);
    }
    @Override
    public void loadFromMappingDef(final InputStream mapIS) throws MarshallerException {
    	Map<String,BeanMap> mappings = _loadFromXML(mapIS,null);		// Cargar el xml de mapeo utilizando el encoding por defecto
    	_cacheBeansMappings(mappings);
    }
    /**
     * Añade los mapeos del fichero que se pasa como parametro
     * @param mapFile fichero con los mapeos
     * @throws MarshallerException si el fichero de mapeos es incorrecto
     */
    @SuppressWarnings("resource")
    public void loadFromMappingDefFile(final File mapFile) throws MarshallerException {
    	if (mapFile != null) {
	    	try {
				@Cleanup FileInputStream fis = new FileInputStream(mapFile);
		    	Map<String,BeanMap> mappings = _loadFromXML(fis,null);		// Cargar el xml de mapeo utilizando el encoding por defecto
		    	_cacheBeansMappings(mappings);
	    	} catch (FileNotFoundException fnfEx) {
	    		throw new MarshallerException(fnfEx.getMessage(),fnfEx);
	    	} catch (IOException ioEx) {
	    		throw new MarshallerException(ioEx);
	    	}
    	}
    }  
    /**
     * Carga el mapa de clases desde su definición en XML
     * @param mapXmlIS InputStream al XML de mapeo
     * @param charset el charset del fichero de mapeo
     * @throws SAXException si no se puede cargar el mapeo
     */
    private static Map<String,BeanMap> _loadFromXML(InputStream mapXmlIS,Charset charset) throws MarshallerException {
    	Map<String,BeanMap> outBeansMappingDef = null;
        try {
            // Default (non-validating) parser
            SAXParserFactory factory = SAXParserFactory.newInstance();

            // Parsear la entrada pasandose a sí mismo como handler
            // de los eventos generados en el parseo.
            SAXParser saxParser = factory.newSAXParser();
            SimpleMarshallerMappingsFromXMLLoader loader = new SimpleMarshallerMappingsFromXMLLoader();
            
            // Encoding del xml
            Charset theCharset = charset != null ? charset : Charset.defaultCharset();
            @Cleanup InputStreamReader isr = new InputStreamReader(mapXmlIS,theCharset);
            InputSource is = new InputSource(isr);            
            
            // Parsear y cargar los beans
            saxParser.parse(is,loader);
            outBeansMappingDef = loader.getLoadedBeans();
            
        } catch (SAXException saxEx) { 
        	Throwable cause = saxEx.getCause();
        	if (cause instanceof MarshallerException) throw (MarshallerException)cause;
        	throw new MarshallerException("Error en el parseo del fichero de mapeo: " + saxEx.toString(),saxEx );        	
        } catch (ParserConfigurationException pcEx) {
        	throw new MarshallerException("Error en la configuración del parser: " + pcEx.toString(),pcEx );
        } catch (IOException ioEx) {
        	throw new MarshallerException("Error de IO al cargar el xml de mapeo: " + ioEx.toString(),ioEx );        	
        } 
        return outBeansMappingDef;
    }

///////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
///////////////////////////////////////////////////////////////////////////////////////////    
    @Override   
    public String debugInfo() {
    	if (_beanMappingsByType == null) return "NO beans in XOMap";
    	StringBuilder sb = new StringBuilder(_beanMappingsByType.size() * 200);
    	sb.append("----[Bean Mappings in XML]----");
    	for (BeanMap currBean : _beanMappingsByType.values()) {
    		sb.append("\r\n");
    		sb.append(currBean.debugInfo());
    	}
    	return sb.toString();
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Pone los beans cargados en la cache
     * @param loadedBeans los beans cargados
     */
    private void _cacheBeansMappings(final Map<String,BeanMap> loadedBeans) {
        // Poner los beans cargados en el mapa de salida
        if (loadedBeans != null && loadedBeans.size() > 0) {
        	_beanMappingsByType.putAll(loadedBeans);
        	
        	// debug...
        	if (log.isTraceEnabled()) {
        		log.trace("Definición de mapeo para el proceso de marshalling/unmarshalling: {}\r\n",
        				  this.debugInfo());
        	}
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE UTILIDAD
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Conecta el objeto {@link BeanMap} en los miembros {@link FieldMap} de tipo objeto o colección
     * Este proceso hay que hacerlo DESPUES de cargar todos los beans (tanto en {@link MarshallerMappingsFromAnnotationsLoader} 
     * como en {@link SimpleMarshallerMappingsFromXMLLoader})
     * @param beansMappings los mapeos de beans
     * @throws MarshallerException si hay algún error al hacer las conexiones
     */
    static void connectBeanMappings(final Map<String,BeanMap> beansMappings) throws MarshallerException {
    	for (BeanMap beanMap : beansMappings.values()) {
    		if (CollectionUtils.isNullOrEmpty(beanMap.getFields())) continue;
    		
            // Comprobar que dos clases distintas no se mapeen en el mismo tag
            int numMatches = 0;
            for (BeanMap anotherBean : beansMappings.values()) {
            	if (anotherBean.getXmlMap().getNodeName() == null || beanMap.getXmlMap().getNodeName() == null) continue;
            	if (anotherBean.getXmlMap().getNodeName().equals(beanMap.getXmlMap().getNodeName())) {
            		numMatches++;
            		if (numMatches > 1) {
            			String msg = Strings.of("There are two types that are mapped to the same XML tag (they're annotated with the same @XmlRootElement(name=\"{}\"): {}.")
            								.customizeWith(beanMap.getXmlMap().getNodeName(),beanMap.getTypeName())
            								.asString();
            			throw new MarshallerException(msg);
            		}
            	}
            }
            
            // Connect the beanMap's DataType with itself
            beanMap.getDataType().setBeanMap(beanMap);
    		
			// Establecer relaciones entre beans        		
    		for (FieldMap fieldMap : beanMap.getFields().values()) {
    			if (fieldMap.getDataType().isObject() && fieldMap.getDataType().asObject().isKnownType()) {
    				// Buscar el mapeo del objeto referenciado...
    				BeanMap fieldObjBeanMap = beansMappings.get(fieldMap.getDataType().getName());
    				if (fieldObjBeanMap == null && ReflectionUtils.isInstanciable(fieldMap.getDataType().getType())) {
    					String msg = Strings.of("El miembro {} de la clase {} es un tipo complejo ({}) sin embargo NO se ha podido cargar su definición")
    										.customizeWith(fieldMap.getName(),beanMap.getTypeName(),fieldMap.getDataType().getName())
    										.asString();
    					log.warn(msg);	//throw new MarshallerException( new MarshallerException(msg) );
    				}
    				fieldMap.getDataType().setBeanMap(fieldObjBeanMap);
    				
    			} else if (fieldMap.getDataType().isCollection()) {
    				// Buscar el mapeo de los objetos contenidos en la colección (en estos momentos SOLO se tiene el nombre, pero hay que crear el tipo)
    				DataType colElsType = fieldMap.getDataType().asCollection().getValueElementsDataType();			// Tipo de los objetos
    				if ( !colElsType.isSimple() ) {
    					BeanMap colElsBeanMap = _beanMapForObjectInMapOrCollection(beansMappings,fieldMap,colElsType);
   						colElsType.setBeanMap(colElsBeanMap);
    				}
    				// Chequeo de seguridad
	            	if (colElsType.getTypeDef() != DataTypeEnum.STRING && fieldMap.getXmlMap().isCdata()) throw new MarshallerException("Si un miembro es una coleccion y sus elementos NO son de tipo String, el miembro NO puede ser CDATA. Revisa el miembro " + fieldMap.getName() + " del bean " + fieldMap.getDeclaringBeanMap().getTypeName());
	            	
    			} else if (fieldMap.getDataType().isMap()) {
    				// Buscar el mapeo de los objetos contenidos en el mapa
    				DataType keyElsType = fieldMap.getDataType().asMap().getKeyElementsDataType();
    				if ( !keyElsType.isSimple() ) {
    					BeanMap keyElsBeanMap = _beanMapForObjectInMapOrCollection(beansMappings,fieldMap,keyElsType);
   						keyElsType.setBeanMap(keyElsBeanMap);
    				}
    				DataType valueElsType = fieldMap.getDataType().asMap().getValueElementsDataType();
    				if ( !valueElsType.isSimple() ) {
    					BeanMap valueElsBeanMap = _beanMapForObjectInMapOrCollection(beansMappings,fieldMap,valueElsType);
   						valueElsType.setBeanMap(valueElsBeanMap);
    				}
    				
		            // Comprobar que si es un MAPA de tipos simples (String, long, etc), la clave TIENE que ser String
		            if (fieldMap.getDataType().asMap().getValueElementsType() == null) {				
						Pattern p = Pattern.compile("[a-zA-Z]+(?:\\(([^,]+,[^,]+)\\))?");
						Matcher m = p.matcher(fieldMap.getDataType().getName());
						if (m.find()) {					
							// en la definición se indica el tipo (ej Map<String,OtherType>)
							String keyType = Strings.of(m.group(1)).splitter(",").group(2).asString();
							if (keyType != null) {
								DataType dataType = DataType.create(keyType);
								if (dataType.getTypeDef() != DataTypeEnum.STRING) throw new MarshallerException( new MarshallerException("Si un miembro es de tipo Map y los values del map son tipos SIMPLES (ej: Map<String,Long> o Map<String,String>), la clave TIENE que ser de tipo String (y sera SIEMPRE el nombre del tag que 'engloba' el value). Revisa el miembro " + fieldMap.getName() + " del bean " + fieldMap.getDeclaringBeanMap().getTypeName()) );
							}
						}            	
		            }		            	
    			}
    			
    			// If the field is an attribute AND it's a complex type NOT instanciable from a String, it can 
    			// be mapped if ALL it's fields are mapped as attributes
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
				if (fieldMap.getXmlMap().isAttribute() && fieldMap.getDataType().isObject()) {
					// Check that the object
					//		a) has default no arg constructor
					//		b) has a static valueOf method
					// ... if not, check that all fields are mapped as attributes
					boolean canBeCreatedFromString = fieldMap.getDataType().isCanBeCreatedFromString(); 	//ReflectionUtils.canBeCreatedFromString(fieldMap.getDataType().getType());
					if (!canBeCreatedFromString && fieldMap.getDataType().getBeanMap() == null) {
						log.warn("The {}'s {}-type field with name '{}' is mapped as attribute BUT it's a complex object whose mapping info is NOT available",
								 beanMap.getDataType().getType().getSimpleName(),
								 fieldMap.getDataType().getType().getSimpleName(),fieldMap.getName());
					} else if (!canBeCreatedFromString && fieldMap.getDataType().getBeanMap().getXmlMap().areAllFieldsMappedAsXmlAttributes()) {
						log.info("The {}'s {} field of type {} is a complex type NOT instanciable from a String whose fields are ALL mapped as attributes so " + 
								 "it's fields will be expanded as xml attributes",
								 beanMap.getDataType().getType(),fieldMap.getName(),fieldMap.getDataType().getType());
						
						fieldMap.getXmlMap().setExpandableAsAttributes(true);
						
					} else if (!canBeCreatedFromString) {
						log.debug("The {}'s {}-type field with name '{}' is mapped as attribute BUT it's a complex object that neither does have a way to be built from a String either all it's fields are mapped as attributes",
								 beanMap.getDataType().getType().getSimpleName(),
								 fieldMap.getDataType().getType().getSimpleName(),fieldMap.getName());
					}
				} 
    			
    			
    		} // fields
    		
    		// Inicializar caches en los beans
    		beanMap.initIndexes();        		
    	} // beans
    }
    private static BeanMap _beanMapForObjectInMapOrCollection(final Map<String,BeanMap> beansMappings,
    								 		 				  final FieldMap fieldMap,
    								 		 				  final DataType type) {
    	BeanMap outBeanMap = null;
		if (type.getTypeDef() == DataTypeEnum.OBJECT && !DataTypeEnum.OBJECT.canBeFromTypeName(type.getName())) {
			outBeanMap = beansMappings.get(type.getName());
			if (outBeanMap == null) {
				String msg = Strings.of("El miembro {} es una colección/mapa con elementos de tipo {} sin embargo NO se ha definido este tipo")
									.customizeWith(fieldMap.getName(),type.getName())
									.asString();
				throw new MarshallerException(msg);
			}
		}
		return outBeanMap;
    }
    
     
}

