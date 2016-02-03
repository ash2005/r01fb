package r01f.marshalling.simple;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.slf4j.Logger;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.collect.Maps;

import lombok.Cleanup;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.encoding.TextEncoder;
import r01f.exceptions.Throwables;
import r01f.locale.Language;
import r01f.locale.Languages;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.MarshallerMappings;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.marshalling.simple.DataTypes.DataTypeEnum;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Numbers;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;


/**
 * ObjsFromXMLBuilder.java
 * Se encarga de cargar una jerarquia de objetos a partir de
 * un XML utilizando SAX. SAX es un api para acceder a los eventos generados
 * por el parser a medida que va parseando el documento. Esto hace que NO
 * sea necesario tener todo el arbol DOM del documento almacenado
 * en memoria. Además es mas rapido.
 *
 * Para saber a qué objeto corresponde el XML se utiliza un documento XML que
 * define el mapeo entre el XML y los objetos (para mas información sobre este
 * documento ver la clase XOMap)
 */
@Slf4j
final class ObjsFromXMLBuilder<T> {
///////////////////////////////////////////////////////////////////////////////////////////
//  MIEMBROS
///////////////////////////////////////////////////////////////////////////////////////////
	private final MarshallerMappings _mappings; // Definición de las clases que se van a cargar
                 								// y como estas se estructuran en el XML
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
    public ObjsFromXMLBuilder(MarshallerMappings map) {
    	_mappings = map;
    }   

///////////////////////////////////////////////////////////////////////////////////////////
//  PUBLIC INTERFACE
/////////////////////////////////////////////////////////////////////////////////////////// 
    public final T beanFrom(final String xmlStr,
    						final Charset xmlCharset,
    						final TextEncoder encoder) throws MarshallerException {
        if (xmlStr == null) throw new MarshallerException("El XML es nulo; NO se puede obtener un objeto");
        return _parseXML(Strings.converterOf(xmlStr).asInputStream(),xmlCharset,encoder);
    }
    public final T beanFrom(final InputStream is,
    						final Charset xmlCharset,
    						final TextEncoder encoder) throws MarshallerException {
        if (is == null) throw new MarshallerException("El InputStream al XML es nulo; NO se puede obtener un objeto");
        return _parseXML(is,xmlCharset,encoder);
    }

	public final T beanFrom(final File file,
    						final Charset xmlCharset,
    						final TextEncoder encoder) throws MarshallerException {
        if (file == null) throw new MarshallerException("XML File is null; NO se puede obtener un objeto");
        try {
            @Cleanup FileInputStream fis = new FileInputStream(file);
            return _parseXML(fis,xmlCharset,encoder);
        } catch (FileNotFoundException fnfEx) {
            throw new MarshallerException(fnfEx.getMessage(),fnfEx);
        } catch (IOException ioEx) {
        	throw new MarshallerException(ioEx.getMessage(),ioEx);
        }
    }
    public final T beanFrom(final Node xmlNode,
    						final Charset xmlCharset,
    						final TextEncoder encoder) throws MarshallerException {
        if (xmlNode == null) throw new MarshallerException("El Node XML es nulo; NO se puede obtener un objeto");
        // Utilizar una transformación (sin hacer NADA) para obtener un inputStream
        try {
        	// Origen de la transformación
	        DOMSource domSource = new DOMSource(xmlNode);
	        // Destino de la transformación
	        ByteArrayOutputStream transformedXML = new ByteArrayOutputStream();
	        // Transformar (realmente NO hace nada)
	        TransformerFactory.newInstance().newTransformer().transform(domSource,
	        															new StreamResult(transformedXML));
	        // Obtener un InputStream del XML
	        InputStream transformedXMLIS = new ByteArrayInputStream(transformedXML.toByteArray());
	        return _parseXML(transformedXMLIS,xmlCharset,encoder);
	        
        } catch(TransformerException trEx) {
        	throw new MarshallerException(trEx.getMessage(),trEx);
        }
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  PARSEO
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Parsea el xml de entrada y obtiene su representación en objetos
     * @param xmlIS Stream de entrada con los datos xml
     * @param charset el juego de caracteres del xml que contiene los objetos
     * @param textEncoder un codificador/decodificador del texto del xml
     */

	private final T _parseXML(final InputStream xmlIS,
    						  final Charset charset,
    						  final TextEncoder textEncoder) throws MarshallerException {
    	try {
	        // Encoding del xml
	        Charset theCharset = charset != null ? charset : Charset.defaultCharset();
	        @Cleanup InputStreamReader isr = new InputStreamReader(xmlIS,theCharset);
	        InputSource is = new InputSource(isr);
	        return _parseXML(is,textEncoder);
	        
        } catch (IOException ioEx) {
            throw new MarshallerException(ioEx.getMessage(),ioEx);
        }
    }
    /**
     * Parsea el xml de entrada y obtiene su representación en objetos
     * @param xmlIS Stream de entrada con los datos xml
     * @param textEncoder un codificador/decodificador del texto del xml
     */
    private final T _parseXML(final InputSource xmlIS,
    						  final TextEncoder textEncoder) throws MarshallerException {
        if (_mappings == null) throw new MarshallerException("No se ha establecido el documento de mapeo de beans a XML: Llamar a setBeanMap, o establecerlo en el constructor");
        if (xmlIS == null) return null;
        try {
            // Default (non-validating) parser
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(false);	// no validar el xml

            // Parsear la entrada pasandose a sí mismo como handler de los eventos generados en el parseo.
            SAXParser saxParser = factory.newSAXParser();
            ObjsFromXMLLoader loader = new ObjsFromXMLLoader((SimpleMarshallerMappings)_mappings,
            												 textEncoder,
            												 log); 	// implementa DefaultHandler + LexicalHandler (para los CDATA)
            saxParser.setProperty("http://xml.org/sax/properties/lexical-handler",loader);  // Imprescindible para el LexicalHandler
            saxParser.parse(xmlIS,loader);            

            @SuppressWarnings("unchecked")
            T outObj = (T)loader.getBuiltObj();
            return outObj;
        } catch (SAXException saxEx) {
        	Throwable cause = saxEx.getCause();
        	if (cause instanceof MarshallerException) throw (MarshallerException)cause;
            throw new MarshallerException(saxEx.getMessage(),saxEx);
        } catch (IOException ioEx) {
            throw new MarshallerException(ioEx.getMessage(),ioEx);
        } catch (Exception ex) {
            throw new MarshallerException("Error desconocido en el proceso de marshalling:\r\n" + ex.getMessage(),ex);
        }
    }
///////////////////////////////////////////////////////////////////////////////
//  CLASE AUXILIAR QUE PARSEA EL XML Y SE ENCARGA DE GENERAR EL OBJETO
//	Funcionamiento interno:
//	En la pila _beanAndFieldStack se van "dejando" el bean y el miembro en
//	proceso en un momento dado y que se "inicializan" cuando el parser SAX
//	lanza el evento startElement para un nuevo TAG
//	IMPORTANTE:
//	-	Para los miembros "simples" (los de tipo String, long, 
//		date, etc), o los "complejos" (aquellos que también se definen en 
//		el fichero de mapeo), en la pila _beanAndFieldStack hay un objeto:
//			bean = bean que contiene el miembro
//			field = null
//		si el miembro es "complejo", una vez comienza el bean hijo, 
//		en la pila _beanAndFieldStack se introduce otro objeto beanAndField 
//		para el hijo
//
//	- 	Para los miembros tipo Colección (Mapas, arrays y listas), en la
//		pila _beanAndFieldStack hay un objeto en el que
//			bean = bean que contiene la colección
//			field = una Lista donde se van "metiendo" cada uno de los beans
//					de la colección
//		una vez comienza a procesarse un bean de la colección, en la pila
//		_beanAndFieldStack se introduce otro objeto beanAndField para el 
//		bean.
//		NOTA: 	Si la colección es de algún tipo simple (ej XML, String, long...)
//				para cada elemento de la colección, en la pila se introduce
//				un bean "virtual" para cada elemento en el que
//				el mapping es nulo (un tipo simple NO se define en el fichero
//				de mapeo
///////////////////////////////////////////////////////////////////////////////
/**
 * Clase auxiliar que parsea el XML y que genera el objeto
 */
@RequiredArgsConstructor	// Constructor con los miembros final
private class ObjsFromXMLLoader 
      extends DefaultHandler 
   implements LexicalHandler  {
	
	private final SimpleMarshallerMappings _beanMappings;
	private final TextEncoder _textEncoder;
	private final Logger _log;

    @Getter private Object builtObj = null;
    		private Deque<BeanAndFieldWrapper> _beanAndFieldStack = new ArrayDeque<BeanAndFieldWrapper>();
    		private Deque<String> _beanElementTagNames = new ArrayDeque<String>();	// NO es estrictamente necesario (bastaría con el último tag)...   
    		private Deque<String> _rawXMLTags = new ArrayDeque<String>();   	// Si se está cargando un miembro que contiene XML "a pelo" (raw), el contenido
    																			// del tag que "engloba" al miembro NO se parsea sino que se trata como una cadena
    																			//		Puede ocurrir que el tag que "engloba" al miembro tipo XML aparezca tambien en el
    																			// 		propio xml contenido
																				// Ejemplo:     <value>     <-- TAG XML (se mapea con un String)
																				//                  <props>
													    						//                      <value>1</value>    <-- aparece el tag padre!
													    						//                      <value>2</value>
													    						//                  </props>
													    						//              </value>    <-- Los subsiguientes tags son "normales"
    		boolean _loadingChars = true;	// evita que se compute una y otra vez el método characters() si en la 
    										// primera pasada por characters() se detecta que NO se mapea a ningún miembro del objeto
    		
    		private String _mapElementWrapperKey = null;
    		private Class<?> _realTypeToBeLoadedAtCharacters = null;
    		
    // -------: METODOS DEL INTERFAZ DocumentHandler SAX
    @Override
    public void comment(char[] arg0, int arg1, int arg2) throws SAXException {
    	/* nothing to do here */
    }
    @Override
    public void endDTD() throws SAXException {
    	/* nothing to do here */
    }
    @Override
    public void endEntity(final String arg0) throws SAXException {
    	/* nothing to do here */
    }
    @Override
    public void startDTD(final String arg0,final String arg1,final String arg2) throws SAXException {
    	/* nothing to do here */
    }
    @Override
    public void startEntity(final String arg0) throws SAXException {
    	/* nothing to do here */
    }
	@Override
    public void startDocument() throws SAXException  {
    	/* nothing to do here */
    }
	@SuppressWarnings("unchecked")
	@Override
    public void startElement(final String namespaceURI,
                             final String lName, // local name
                             final String qName, // qualified name
                             final Attributes attrs) throws SAXException {
        // Obtener el nombre del tag y meterlo en la pila de tags
        String eName = lName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false  

        StringBuilder dbg = _log.isTraceEnabled() ? new StringBuilder(100) 
        										  : null;
        
        if (_log.isTraceEnabled()) dbg.append("[START]: ").append(eName);
		try {
	        _loadingChars = true;	// NO mover... 
	        
	        if (_rawXMLTags.isEmpty()) _beanElementTagNames.push(eName);  	// solo poner en la pila el tag si NO se está cargando un XML en un miembro
        	
	        if (_beanAndFieldStack.isEmpty()) {
	        	_startingMainObject(eName,attrs);	// Crear el objeto principal 
	            return;
	        } 
	                                
	        // Obtener una instancia del bean y el miembro en el que se mapean los datos	
	        BeanAndFieldWrapper beanAndField = _loadingAField(_beanElementTagNames.peek());
	        
	        // si se está cargando un xml "a pelo", simplemente poner el XML en el miembro
	        if (!_rawXMLTags.isEmpty()) {
	        	_rawXMLStartElement(beanAndField,
	        						eName,attrs);
	        	return;
	        }
	        // En este momento TIENE que haber una instancia y un field de la instancia...
	        if (beanAndField == null || beanAndField.getBeanInstance() == null || beanAndField.getFieldInstance() == null) throw new SAXException("Error desconocido en el proceso de convesion de XML a objetos; probablemente la clase que mapea el tag " + eName + " NO esta anotada con @XmlRootElement(name=" + eName + ")");
	        
	        // Obtener el fieldMap y DataType para ahorrar código...
	        FieldMap fieldMap = beanAndField.getFieldMap();
	        DataType fieldDataType = fieldMap.getDataType();
	        
	        // Comprobación de seguridad (no mover de aqui)
	        if (!beanAndField.isValid()) throw new SAXException("NO se ha podido encontrar una instancia para el tag " + eName + " en el bean " + (beanAndField.getBeanInstance() != null ? beanAndField.getBeanMap().getTypeName() : "NULL") + ": El XML que se esta parseando NO se corresponde con la configuracion del mapeo;\r\nLas causas mas frecuentes del error son:\r\n\t1.- en la definición de mapeo se ha especificado como ATRIBUTO, pero en el XML llega como ELEMENTO.\r\n\t2.- en la definición de mapeo NO se ha incluido el elemento");	        	        
	        
	        if ( fieldDataType.isCollectionOrMap() ) {	
	        	// [[[[ COLECCION / MAPA ]]]] -----------------------	        	
	        	if (fieldMap.getXmlMap().getNodeName().equals(eName)) {
	        		if (_log.isTraceEnabled()) dbg.append(" (Collection)");
	        		// Comienza la colección poner en la pila de bean/field el bean/campo que CONTIENE la colección
	        		_beanAndFieldStack.push(beanAndField);
	        		
	        	} else {
	        		if (_log.isTraceEnabled()) dbg.append(" (Collection Item)");
	        		
	        		// Normally Map objects are mapped like
	        		//		<mapWrapper>
	        		//			<mapElement oid='xxx'>...</mapElement>
	        		//			<mapElement oid='yyy'>...</mapElement>
	        		//		</mapWrapper>
	        		// the elements are NOT wrapped and the map key is the @OidField annotated field of the map element's type
	        		// BUT sometimes the map is like
	        		//		<mapWrapper>
	        		//			<SPANISH>
	        		//				<mapElement...>
	        		//			</SPANISH>
	        		//			<BASQUE>
	        		//				<mapElement...>
	        		//			</BASQUE>
	        		//		</mapWrapper>
	        		// this time the map elements are wrapped and the map key is the wrapper tag 
	        		// ... so the map elements wrappers must be ignored until the endElement() method
	        		// to do so, the wrapper key value must be stored at _mapElementWrapperKey and released at endElement() method
	        		if (fieldMap.getDataType().isMap() 
	        		 && fieldMap.getDataType().asMap().getKeyElementsDataType().is(Language.class)		// TODO maybe the same can be applied to Map<String,String> or LanguageTexts
	        		 && !fieldMap.getDataType().asMap().getValueElementsDataType().is(String.class)
	        		 && Languages.canBe(eName)) {
	        			_mapElementWrapperKey = eName;
	        			return;
	        		}
	        			        	
		            // Un objeto DENTRO de una colección
	        		BeanMap colElBeanMap = _beanMapForField(fieldMap,
	        												eName,attrs);
	                BeanInstance colElBeanInstance = _createBeanInstance(colElBeanMap,
	                												     eName,attrs);	                

	                
	                // Añadir el bean a la colección del bean padre
	                List<BeanInstance> instances = beanAndField.getFieldInstance().get();
	                if (instances == null) {
	                	// se trata de un field tipo [Object] (y por lo tanto NO inicializado en el método _creatBeanInstance al inicializar el objeto padre)
	                	// y que realmente contiene una colección: mapa, lista o set
	                	instances = (List<BeanInstance>)FieldInstance.createInstance(fieldMap);
	                	beanAndField.getFieldInstance().set(instances);
	                }
	                instances.add(colElBeanInstance); 
	                                  
	                // En la pila de bean/fields en proceso se inserta el nuevo bean
	                BeanAndFieldWrapper colElBeanAndField = new BeanAndFieldWrapper(colElBeanInstance,	// nuevo bean dentro de una colección / mapa
	                															  	null);				// ... el "padre" es la colección... NO otro bean
	                _beanAndFieldStack.push(colElBeanAndField);
	                
	                // Si se trata de una colección de XMLs, iniciar la carga del xml
	                // NOTA: se detecta que es una coleccion de XMLs, por que:
	                //			1.- El nuevo bean creado es de un tipo simple (un bean "virtual" en el que colElBeanInstance.getMapping() == null) 
	                //			2.- El tipo de dato de la colección es XML
	                //			3.- El nuevo bean tieen un custom Marshaller
	                if (colElBeanInstance.getMapping() == null 		// tipo simple (integer, long, date, etc)
	                					&& 
	                			(fieldDataType.isCollection() && DataTypeEnum.XML.canBeFromTypeName(fieldDataType.asCollection().getValueElementsType().getName())
	                					||
	                			(fieldDataType.isMap() && DataTypeEnum.XML.canBeFromTypeName(fieldDataType.asMap().getValueElementsType().getName())))
	                   ||
	                   (colElBeanInstance.getMapping() != null && colElBeanInstance.getMapping().isCustomXmlTransformed())) {
	                	
	                	_rawXMLStartElement(colElBeanAndField,
	                						eName,attrs);
	                }
	        	} 
	        	
	        } else if (fieldDataType.isObject()) { 		// it's an object
	        	// [[[[ OBJET ]]]] ---------------------------------
        		if (_log.isTraceEnabled()) dbg.append(" (Object)");
        		
        		// [1] - Obtener el mapeo del bean al que se refiere el objeto
        		BeanMap fieldBeanMap = _beanMapForField(fieldMap,
        											    eName,attrs);
	        	
	        	// [2] - Crear beanInstance y fieldInstance en la pila
		    	if (fieldBeanMap != null) {
		    		// [2.1] - Create the bean instance
		    		BeanInstance objBeanInstance = _createBeanInstance(fieldMap,fieldBeanMap,
		    														   eName,attrs);
		    		
		    		// [2.2] - Poner en la pila
		            // Referenciar el bean en el campo del bean padre
		            beanAndField.getFieldInstance().set(objBeanInstance);
	
		            // Si al crear la instancia se ha detectado que es un tipo que se transforma xml<->java de forma customizada, 
		            // hay que pasar a modo leer xml raw
		            if (objBeanInstance.getMapping().isCustomXmlTransformed()) _rawXMLStartElement(beanAndField,
		                					  													   eName,attrs);
		            // Poner en la pila de beanAndField el bean/field "padre" y el nuevo bean/field
		            _beanAndFieldStack.push(beanAndField);                
		            _beanAndFieldStack.push(new BeanAndFieldWrapper(objBeanInstance,null));
		            
	        	} else {	        		
	        		// The field is an object BUT it's not mapped (ej: it's a Boolean, Long, or so)
	        		String typeDiscriminator = fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable();
	        		if (Strings.isNOTNullOrEmpty(typeDiscriminator)) {
		        		// This is the case for:
		        		//		public class RecordPersistenceOperationResult<R> {
		        		//			@XmlElement(name="record") @XmlTypeDiscriminatorAttribute(name="type")
		        		//			@Getter @Setter private R _record;
		        		//      }
		        		// and a RecordPersistenceOperationResult<Boolean> is mapped: _record field is detected as an Object 
		        		// ... BUT there is no mapping for Boolean
			        	if (Strings.isNOTNullOrEmpty(typeDiscriminator)) {
			        		// set the real type to be loaded at characters method: WTF!
			        		_realTypeToBeLoadedAtCharacters = Class.forName(attrs.getValue(typeDiscriminator));
			        	}
	        		} else {
		        		// This is the case for:
		        		//		public class RecordPersistenceOperationResult<R> {
		        		//			@XmlElement
		        		//			@Getter @Setter private R _record;
		        		//      }
		        		// and a RecordPersistenceOperationResult<Boolean> is mapped: _record field is detected as an Object 
		        		// ... BUT there is no mapping for Boolean
		        		// All the loading is done at characters() method
	        		}
	        	}
	        } else if (fieldDataType.isXML()																			// es xml
	        	    || (fieldDataType.getBeanMap() != null && fieldDataType.getBeanMap().isCustomXmlTransformed())) {	// es un objeto que se transforma con un customXmlTransformer
        		if (_log.isTraceEnabled()) dbg.append(" (Object custom transformed or XML)");
        		
	        	// [[[[ XML ]]]] ------------------------------------	        	
	            // Miembros que cargan el XML como String... es decir, el field simplemente almacena el XML sin parsearlo
	        	_rawXMLStartElement(beanAndField,
	        					    eName,attrs);
	        	
	        } else if (fieldDataType.isSimple()) {
        		if (_log.isTraceEnabled()) dbg.append(" (simple)");
	        	// [[[[ Tipo Simple ]]]] ----------------------------
	        	// Tipo simple (String, Integer, Long, etc)	
	    		// No hacer nada... el dato se carga en el metodo characters()
	        }
		} catch (Exception ex) {
			_log.error("[START]: {} > ERROR",eName,ex);
			throw new SAXException(ex);
		} finally {
	        if (_log.isTraceEnabled()) {
	        	dbg.append(" > OK");
	        	_log.trace(dbg.toString());
	        }
		}
    }
	
	
	@Override
    public void characters(final char buff[],final int offset,final int len) throws SAXException {
    	if (buff == null || buff.length == 0) return;		// NO meter nada si solo hay espacios en blanco
    	
    	StringBuilder dbg = _log.isTraceEnabled() ? new StringBuilder(100) : null;
    	
    	if (_log.isTraceEnabled()) dbg.append("[CHARACTERS]: ").append(_beanElementTagNames.peek());
    	
    	
    	if (!_loadingChars) return;		// no volver a computar si ya se ha determinado que los caracteres del tag NO van a ningun miembro
    	try {
	       	BeanAndFieldWrapper beanAndField = _loadingAField(_beanElementTagNames.peek());
	        
	        // Pueden darse multiples llamadas a characters(), asi que la instancia del miembro es un 
	    	// StringBuffer donde se va almacenando el contenido
	       	if (beanAndField != null) {
	       		StringBuilder sb = _textBuffer(beanAndField);
		        if (sb != null) {
		        	StringBuilder text = Strings.create(len + sb.length())	// crear un buffer del tamaño adecuado = texto existente + texto nuevo
		        								.add(buff,offset,len)		// añadir el texto que llega en el método characters
		        								.decodeUsing(_textEncoder)	// decodificarlo
		        								.convert().asStringBuilder();			// devolver como un stringBuilder
		    		sb.append(text);	// añadir al buffer existente el nuevo texto
		    		
		        } else if (beanAndField.getFieldInstance() != null 
		        		&& beanAndField.getFieldInstance().get() == null 
		        		&& beanAndField.getFieldInstance().getMapping().getDataType().getType() == Object.class) {
		        	
		        	String value = new String(buff,offset,len);
		        	Object o = null;
		        	if (_realTypeToBeLoadedAtCharacters != null) {
		        		// This is the case for:
		        		//		public class RecordPersistenceOperationResult<R> {
		        		//			@XmlElement(name="record") @XmlTypeDiscriminatorAttribute(name="type")
		        		//			@Getter @Setter private R _record;
		        		//      }
		        		// and a RecordPersistenceOperationResult<Boolean> is mapped: _record field is detected as an Object 
		        		// ... BUT there is no mapping for Boolean
		        		if (_realTypeToBeLoadedAtCharacters == String.class) {
		        			o = value;
		        		} else if (_realTypeToBeLoadedAtCharacters == Integer.class) {
		        			o = new Integer(value);
		        		} else if (_realTypeToBeLoadedAtCharacters == Long.class) {
		        			o = new Long(value);
		        		} else if (_realTypeToBeLoadedAtCharacters == Short.class) {
		        			o = new Short(value);
		        		} else if (_realTypeToBeLoadedAtCharacters == Double.class) {
		        			o = new Double(value);
 		        		} else if (_realTypeToBeLoadedAtCharacters == Float.class) {
 		        			o = new Float(value);
 		        		}
		        	} else {
		        		// The field is an object BUT it's not mapped (ej: it's a Boolean, Long, or so)
		        		// This is the case for:
		        		//		public class RecordPersistenceOperationResult {
		        		//			@XmlElement
		        		//			@Getter @Setter private R _record;
		        		//      }
		        		// and a RecordPersistenceOperationResult<Boolean> is mapped: _record field is detected as an Object 
		        		// ... BUT there is no mapping for Boolean
			        	if (value.equals("true") || value.equals("false")) {
			        		o = new Boolean(value);
			        	} else if (Numbers.isInteger(value)) {
			        		o = new Integer(value);
			        	} else if (Numbers.isLong(value)) {
			        		o = new Long(value);
			        	} else if (Numbers.isShort(value)) {
			        		o = new Short(value);
			        	} else if (Numbers.isDouble(value)) {
			        		o = new Double(value);
			        	} else if (Numbers.isFloat(value)) {
			        		o = new Float(value);
			        	} else {
			        		o = value;
			        	}
		        	}
		        	// Set the value
		        	BeanInstance bi = new BeanInstance();
		        	bi.set(o);
		        	beanAndField.getFieldInstance().set(bi);
		        	_loadingChars = false;
		        	
		        } else if (beanAndField.getFieldInstance() != null 
		        		&& beanAndField.getFieldInstance().get() == null
		        		&& beanAndField.getFieldInstance().getMapping().getDataType().isObject()
		        		&& beanAndField.getFieldInstance().getMapping().getDataType().getBeanMap() != null
		        		&& (beanAndField.getFieldInstance().getMapping().getDataType().isCanBeCreatedFromString() 
		        			|| !beanAndField.getFieldInstance().getMapping().getDataType().asObject().hasFields())) {	
		        	// The field is an object BUT any of it's fields are mapped (they are transient or annotated with @XmlTransient
		        	// This is the case for r01f.types.Path object
		        	// The only way to create these type of objects is using a single String param constructor or a static valueOf(String) method
		        	// ... so the value should be an String
		        	String value = new String(buff,offset,len);
		        	BeanInstance bi = new BeanInstance();
		        	bi.set(new StringBuilder(value));
		        	beanAndField.getFieldInstance().set(bi);
		        	
		        } else if (beanAndField.getFieldInstance() == null
		        	    && beanAndField.getBeanInstance().getMapping().getDataType().isCanBeCreatedFromString()) {
		        	// A object that can be created from a String is being loaded (ie a OID object that have a valueOf(String) or fromString(String) method)
		        	// ie	<myObj>
		        	//			<oid>myOid</oid>
		        	//		</myObj>
		        	String value = new String(buff,offset,len);
		        	beanAndField.getBeanInstance().set(ReflectionUtils.createInstanceFromString(beanAndField.getBeanInstance().get().getClass(),value));
		        	
		        } else {
		        	// The characters are NOT persisted in any bean member... ignore any characters() call from now on
		        	_loadingChars = false;
		        }
	       	} else {	       		
//	        	StringBuilder text2 = Strings.create()					// crear un buffer del tamaño adecuado = texto existente + texto nuevo
//	        								.add(buff,offset,len)		// añadir el texto que llega en el método characters
//	        								.decodeUsing(_textEncoder)	// decodificarlo
//	        								.asStringBuilder();			// devolver como un stringBuilder
//	        	System.out.println("--->" + text2);
	       	}
    	} catch(Exception ex) {
    		_log.error("[CHARACTERS]: {} > ERROR",_beanElementTagNames.peek(),ex);
    		throw new SAXException(ex);
    	} finally {
    		if (_log.isTraceEnabled()) {
    			dbg.append(" > OK");
    			_log.trace(dbg.toString());
    		}
    	}
    }
	
	
	@Override
    public void startCDATA() throws SAXException {
    	if (_rawXMLTags.isEmpty()) return;	// si no se está cargando un xml... no hacer nada
    	try {
	    	BeanAndFieldWrapper beanAndField = _loadingAField(_beanElementTagNames.peek());
	        StringBuilder xmlSb = null;
	        if (beanAndField.getFieldInstance() != null) {
	        	xmlSb = beanAndField.getFieldInstance().get();
	        } else {
	        	xmlSb = beanAndField.getBeanInstance().get();
	        }            	            	
	        xmlSb.append("<![CDATA[");
    	} catch(Exception ex) {
    		_log.error("[START_CDATA]: {} > ERROR",_beanElementTagNames.peek(),ex);
    		throw new SAXException(ex);
    	}	        
    }
    
	@Override
    public void endCDATA() throws SAXException {
    	if (_rawXMLTags.isEmpty()) return;	// si no se está cargando un xml... no hacer nada
    	try {
	    	BeanAndFieldWrapper beanAndField = _loadingAField(_beanElementTagNames.peek());        	
	    	StringBuilder xmlSb = null;            	
	        if (beanAndField.getFieldInstance() != null) {
	        	xmlSb = beanAndField.getFieldInstance().get();
	        } else {
	        	xmlSb = beanAndField.getBeanInstance().get();
	        }            	
	        xmlSb.append("]]>");
    	} catch(Exception ex) {
    		_log.error("[END_CDATA]: {} > ERROR",_beanElementTagNames.peek(),ex);
    		throw new SAXException(ex);
    	}	        
    }
    
	@Override @SuppressWarnings({ "unchecked" })
    public void endElement(final String namespaceURI,
                           final String sName, // simple name
                           final String qName  // qualified name
                           ) throws SAXException {
        String eName = sName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false
        
        StringBuilder dbg = _log.isTraceEnabled() ? new StringBuilder(100) : null;
        
        if (_log.isTraceEnabled()) dbg.append("[END]: ").append(eName);
        
        // Check wether a key-wrapped map element is ending 
        if (_mapElementWrapperKey != null && eName.equals(_mapElementWrapperKey)) {
        	 List<BeanInstance> colEls = _beanAndFieldStack.peek().getFieldInstance().get();
        	 colEls.get(colEls.size()-1).setEffectiveNodeName(eName);
        	_mapElementWrapperKey = null;
        	_beanElementTagNames.pop();
        	return;
        }
        
        try {
	        BeanAndFieldWrapper beanAndField = _beanAndFieldStack.peek();
	        
	        if (_beanAndFieldStack.isEmpty()) return; 	// the root object is being finished and the root object is a collection
	        
	        // :::: raw xml is being loaded?
	        if (!_rawXMLTags.isEmpty()) { 
				beanAndField = _loadingAField(_beanElementTagNames.peek());
				_rawXmlEndElement(eName,beanAndField);
				if (!_rawXMLTags.isEmpty()) return;
	        }
	        
	        // :::: otherwise it can be finishing:
	        //		- a collection's element
	        //		- an object
	        //		- a member	
	        if (beanAndField.getBeanMap() == null) {
	        	// se está finalizando un bean "virtual" correspondiente a un tipo "simple" (string, date, etc) 
	    		// y englobado en una colección
	        	_beanAndFieldStack.pop();
	        	
	        } else if (beanAndField.getFieldInstance() != null
	        		&& beanAndField.getFieldMap().getDataType().isCollectionOrMap()) {
	        	// a collection is being finished
	            _beanAndFieldStack.pop();		// The bean/field that CONTAINS the collection
	        	
	        } else if (beanAndField.getFieldInstance() == null 
	        		&& beanAndField.getBeanInstance() != null            			
	        		&& beanAndField.getBeanInstance().getEffectiveNodeName().equals(eName)) {
	        	// se está finalizando un bean que es un miembro de otro bean (beanAndField tiene field = null)
	        	beanAndField.getBeanInstance()
	        				.build();				// <-- IMPORTANT!!! here the bean is built <-----------
	        	_beanAndFieldStack.pop();
	        	
	        	if ( !_beanAndFieldStack.isEmpty() ) {
	        		// Si el bean/field "padre" NO es una colección, hay que sacarlo tambien de la pila
	            	BeanAndFieldWrapper parentBeanAndField = _beanAndFieldStack.peek();
	            	if ( parentBeanAndField.getFieldInstance() != null 
	            	 && !parentBeanAndField.getFieldMap().getDataType().isCollection()
	            	 && !parentBeanAndField.getFieldMap().getDataType().isMap()) {
	            		_beanAndFieldStack.pop();	// El bean/field que CONTIENE el objeto "hijo" 
	            	}
	        	}
	        } else {
	        	/* empty */
	        }	        
	    	// ... se termina el objeto
			_beanElementTagNames.pop();		// sacar el tag de la pila
			
			_loadingChars = true;
			// Ver si se ha terminado con el último objeto
	    	if (_beanAndFieldStack.isEmpty()) {
	    		// Obtener el último objeto construido
	    		Object lastBuiltObj = beanAndField.getBeanInstance().get();
	    		
	    		// Hay 2 casos:
	    		//		- El objeto construido es el objeto traíz
	    		//		- El objeto construido es un elemento del objeto raíz y este es una colección o mapa
	    		
	    		// El objeto raíz es un mapa
	    		if (this.builtObj != null && CollectionUtils.isMap(this.builtObj.getClass())) {
	    			// Buscar la clave y valor para poner en el mapa
	    			Object key = null;
	    			Object value = lastBuiltObj;
	    			BeanMap rootBeanMap = _beanMappings.getBeanMapFromXmlTag(eName);
	    			FieldMap oidFieldMap = rootBeanMap.getOidField();
	    			if (oidFieldMap != null) {
	    				key = ReflectionUtils.fieldValue(lastBuiltObj,oidFieldMap.getName(),rootBeanMap.isUseAccessors());
	    			} else {
	    				throw new MarshallerException("NO se puede poner en el mapa " + this.builtObj.getClass().getName() + " un objeto de tipo " + lastBuiltObj.getClass().getName() + " si en este tipo NO se especifica cual es el miembro OID (ej anotar con @OidField)");
	    			}
	    			// Poner el objeto en el mapa
	    			@SuppressWarnings("rawtypes")
					Map builtMap = (Map)this.builtObj;
	    			builtMap.put(key,value);
	    		}
	    		// El objeto raíz es una colección
	    		else if (this.builtObj != null && CollectionUtils.isCollection(this.builtObj.getClass())) {
	    			@SuppressWarnings("rawtypes")
	    			Collection builtCol = (Collection)this.builtObj;
	    			builtCol.add(lastBuiltObj);
	    		}
	    		// El objeto raíz está transformado java<->xml de forma customizada
	    		else if (beanAndField.getBeanMap().isCustomXmlTransformed()) {
	    			this.builtObj = beanAndField.getBeanInstance().get();
//	    			String nodeName = beanAndField.getBeanMap().getXmlMap().getNodeName();
//	    			StringBuilder xml = (StringBuilder)beanAndField.getBeanInstance().get();	// llega SIN el tag de apertura y cierre
//	    			xml.insert(0,"<" + nodeName + ">")		// tag de apertura
//	    			   .append("</" + nodeName + ">");		// tag de cierre
//	    			this.builtObj = beanAndField.getBeanMap().getCustomXMLTransformers().getXmlReadTransformer()
//	    																				.beanFromXml(xml);
	    		}
	    		// Situación más habitual, el objeto raíz es un objeto "normal"
	    		else {
    				this.builtObj = lastBuiltObj;		// el objeto construido
	    		}
	    	}     		
    	} catch(Exception ex) {
    		_log.error("[END]: {} > ERROR",eName,ex);
    		throw new SAXException(ex);
    	} finally {
    		if(_log.isTraceEnabled()) {
    			dbg.append(" > END");
    			_log.trace(dbg.toString());
    		}
    	}
    }

	@Override
    public void endDocument() {
    	assert _beanAndFieldStack.isEmpty() && _beanElementTagNames.isEmpty();
    }
///////////////////////////////////////////////////////////////////////////////
//  METODOS AUXILIARES
///////////////////////////////////////////////////////////////////////////////  
	private void _startingMainObject(final String eName,
									 final Attributes attrs) throws MarshallerException {
        // Ver si se está empezando con el objeto raiz; hay DOS posibilidades
        //		- El objeto raíz es un objeto normal (caso más habitual)
        //		- El objeto raíz se una colección o mapa
    	BeanMap rootBeanMap = _beanMappings.getBeanMapFromXmlTag(eName);
    	if (rootBeanMap == null) {
    		// Si el xml es <map>...</map> o bien <collection>...</collection> se crea un bean aqui
    		if (eName.equals("map")) {
    			rootBeanMap = new BeanMap("Map:" + LinkedHashMap.class.getCanonicalName());
    		} else if (eName.equals("list")) {
    			rootBeanMap = new BeanMap("Collection:" + LinkedList.class.getCanonicalName());
    		} else if (eName.equals("set")) {
    			rootBeanMap = new BeanMap("Collection:" + LinkedHashSet.class.getCanonicalName());
    		} else {
    			throw new MarshallerException("NO se ha podido encontrar un mapeo para el tag " + eName);
    		}
    	}
    	
        if (rootBeanMap.isCustomXmlTransformed()) {
        	// [CASO 0] - El objeto principal está transformado de forma customizada
        	this.builtObj = null;	// importante!!
        	BeanInstance beanInstance = new BeanInstance(rootBeanMap,eName);
        	beanInstance.set(new StringBuilder());
        	BeanAndFieldWrapper beanAndField = new BeanAndFieldWrapper(beanInstance,null);
        	 _beanAndFieldStack.push(beanAndField);
        	_rawXMLStartElement(beanAndField,
        						eName,attrs);
        } else {
            BeanInstance rootBeanInstance = _createBeanInstance(rootBeanMap,
            													eName,attrs);
            
        	// [CASO 1] - Se está comenzando el objeto principal y este es un mapa o colección;
            //			  en this.builtObj se deja una instancia de la colección/Mapa que se va 
            //			  completando en el método endElement()
            if (rootBeanMap.getDataType().isMap() || rootBeanMap.getDataType().isCollection()) {
            	_beanElementTagNames.pop();
            	this.builtObj = rootBeanInstance.get();
            } 
            // [CASO 2,3]- El objeto principal es un mapa o colección y se está empezando un item de dicho mapa/colección
            //			   se trata como si se estubiera construyendo un bean "normal" que más tarde en endElement() se 
            //			   pasará al mapa / colección que está en this.builtObj
            else if (this.builtObj != null && CollectionUtils.isMap(this.builtObj.getClass())) {
	            _beanAndFieldStack.push( new BeanAndFieldWrapper(rootBeanInstance,null) );
            }
            else if (this.builtObj != null && CollectionUtils.isCollection(this.builtObj.getClass())) {
	            _beanAndFieldStack.push( new BeanAndFieldWrapper(rootBeanInstance,null) );
            }
            // [CASO 4] - El objeto principal es un objeto normal (es el caso más habitual)
            else {
            	this.builtObj = null;	// importante!!
	            _beanAndFieldStack.push( new BeanAndFieldWrapper(rootBeanInstance,null) );
            }
        }
	}
    private BeanAndFieldWrapper _loadingAField(final String currNodeName) throws MarshallerException {
    	// IMPORTANT!!! When loading a type's field, beanAndField.getFieldInstance() is ALLWAYS Null
    	BeanAndFieldWrapper beanAndField = _beanAndFieldStack.peek();
		BeanInstance beanInstance = beanAndField.getBeanInstance(); 
		
		BeanAndFieldWrapper outBeanAndField = null;
		if (!_rawXMLTags.isEmpty()) {
			// se está cargando un miembro xml o custom marshalled
			String fieldTag = _rawXMLTags.peekFirst(); 	// el tag que "engloba" el miembro está en la base de la pila
			FieldMap fieldMap = null;
			if (beanInstance.getMapping() == null) {
				// El xml forma parte de un bean "virtual" de una colección de objetos "simples" (en este caso XMLs)
				outBeanAndField = _beanAndFieldStack.peek();	// El xml va en el propio bean "virtual" (que contiene un StringBuilder para almacenar el texto)
			} else {
				// El xml forma parte de un miembro de un bean "normal"
				//		- caso 1: el tag que "engloba" el miembro está en la base de la pila (miembro anotado con @XmlElement(name="tag")
				//		- caso 2: el tag que "engloba" el miembro es el propio tag del tipo del miembro (miembro anotado con @XmlValue)
				fieldMap = beanInstance.getMapping().getFieldFromXmlNode(fieldTag,false);
				if (fieldMap == null) {
					// caso 2
					fieldMap = beanInstance.getMapping().getFieldFromXmlNode(beanInstance.getEffectiveNodeName(),false);	
				} else {
					// caso 1
				}
				outBeanAndField =  new BeanAndFieldWrapper(beanInstance,
											    		   beanInstance.getFieldInstance(fieldMap));
			}
			
		} 
		// Cuando se carga un miembro de un bean, beanAndField.getFieldInstance() es SIEMBRE Null
		else if (beanAndField.getFieldInstance() == null) {	
			if (beanInstance.getMapping() == null) {
				// tipo "simple" (string, long, date, etc) dentro de una colección... se devuelve un bean "virtual" en el que
				//		- beanAndField.getBean().getMapping() == null (es un tipo "simple" que NO se refleja en el fichero de mapeo
				//		- beanAndField.getField() == null (como cualquier bean)
				outBeanAndField = beanAndField;	
				
			} else {
				// miembro "normal" dentro de un bean o nuevo objeto dentro de una colección/mapa
				
				// Averiguar el miembro del bean padre en el que se mapea el tag
				// ... si se trata de un nuevo objeto de una colección fieldMap = null
				FieldMap fieldMap = beanInstance.getMapping()
												.getFieldFromXmlNode(currNodeName,false);	// lo habitual es que se devuelva un valor aqui
				// Si el método anterior NO ha devuelto un field... es un caso raro
				if (fieldMap == null && currNodeName.equals(beanInstance.getEffectiveNodeName())) {
					// Caso (poco habitual) en el que un objeto puede estar englobado por otro tag cuando se encuentra como miembro de otro objeto
					// Ej: 	El tipo MyType se engloba "normalmente" en el tag <myTypeTag>
					//			@XmlRootElement(name="myTypeTag")
					//			public class MyType {
					//				...
					//			}
					//		PERO si en OTRO tipo MyOTHERType hay un miembro MyType y se CAMBIA el tag que engloba al objeto MyType
					//			@XmlRootElement(name="myOtherTypeTag")
					//			public class MyOtherType {
					//				@XmlElement(name="myTypeFakeTag")		<-- en ESTE objeto MyOtherType, el objeto de tipo MyType está englobado en el tag <MyTypeFakeTag>
					//				@Getter @Setter private MyType _myType;		en lugar de estar englobado en <MyType>
					//			}
					//	   en este caso, cuando llega el tag myTypeFakeTag (<myTypeFakeTag>...</myTypeFakeTag>) al intentar buscar en el registro
					//	   tipos<->tags un tipo englobado por <myTypeFake> NO se encuentra nada 
					// 	   Hay que "engañar" al flujo y buscar por <myType> en lugar de por <myTypeFakeTag>
					fieldMap = beanInstance.getMapping()
										   .getFieldFromXmlNode(beanInstance.getXmlMap().getNodeName(),false);
				}
				if (fieldMap == null) {
					// Caso en el que NO se define el tag en el que se mapea el miembro:
					// ej:		@XmlElement 
					//			private MyType myField;		<-- La anotación @XmlElement no lleva el atributo name
					// Normalmente esta caso se da cuando el field está definido con un interfaz o clase abstracta que puede ser 
					// implementada por diferentes tipos y cada un de ellos se "engloba" en un tag XML distinto especificado 
					// en la anotación @XmlRootElement
					BeanMap candidateBeanMap = _beanMappings.getBeanMapFromXmlTag(currNodeName);
					if (candidateBeanMap != null) {
						// El tag engloba un tipo existente en el mapeo... ver si hay algún field en el bean actual de este tipo 
						fieldMap = beanInstance.getMapping()
											   .getFieldForType(candidateBeanMap.getDataType().getType());
					}
				}
				if (fieldMap == null) {
					// Caso en el que el tag es un XML que se engloba en el propio tag del bean
					// ej: 		@XmlRootElement(name="myTag")					<myTag>
					//			public class MyType {								<someXml>...</someXml>
					//				@XmlValue @XmlInline						</myTag>
					//				@Getter @Setter private String _xml;
					//			}
					FieldMap candidateFieldMap = beanInstance.getMapping().getFieldFromXmlNode(beanInstance.getEffectiveNodeName(),false);
					if (candidateFieldMap != null && candidateFieldMap.getDataType().isXML()) {
						fieldMap = candidateFieldMap;
					}
				}
				if (fieldMap == null) {
					// Caso en el que se trata de una colección o mapa
					fieldMap = _fieldForACollection(currNodeName,
												    beanInstance);
				}
				
				// >>> Devolver....
				if (fieldMap != null) { //&& !fieldMap.getDataType().isCollectionOrMap()) {
					// ... caso normal; se devuelve el bean y el miembro donde se va a cargar el valor					
					outBeanAndField =  new BeanAndFieldWrapper(beanInstance,
	    										    		   beanInstance.getFieldInstance(fieldMap));
				} else {
					// ...caso en el que el texto (characters) del tag que "engloba" al bean NO se mapea en ningún miembro
					outBeanAndField = new BeanAndFieldWrapper(beanInstance,null);
				}
			} 
		} 
		// Cuando se carga un elemento de una colección o mapa, beanAndField.getFieldInstance() es SIEMBRE una colección donde
		// se van "guardando" los elementos de la colección o mapa
		else if (beanAndField.getFieldInstance() != null && beanAndField.getFieldInstance().getMapping().getDataType().isCollectionOrMap()) {
			outBeanAndField = beanAndField;
		}
		return outBeanAndField;
    }
    private FieldMap _fieldForACollection(final String currNodeName,
    									  final BeanInstance beanInstance) throws MarshallerException {
    	// En el caso se las colecciones hay DOS casos.
    	//		- Colecciones cuyos elementos se "engloban" en un tag
    	//		- Colecciones cuyos elementos NO se "engloban" en un tag, sino que los elementos están directamente "colgando" del tag del bean
    	//
    	// 		Coleccion NO englobada en un tag              	Colección englobada en un tag 
		//		(el propio tag del objeto "padre" contiene
		//		 los elementos de la colección)
    	//		<obj>											<obj>
    	//			<myField>...</myField>							<myField>...</myField>
    	//			|<myOj>											\<myObjs>  <---------------------------wrapper tag
    	//			|	<myObjField>...</myObjField>				\	|<myObj>
    	//			|<myObj>										\	|		<myObjField>...</myObjField>
    	//			*<myOj>											\	|</myObj>
    	//			*	<myObjField>...</myObjField>				\	*<myObj>
    	//			*<myObj>										\	*		<myObjField>...</myObjField>
    	//		</obj>												\	*</myObj>
    	//															\</myObjs> <----------------------------wrapper tag
    	//														</obj>
		FieldMap outColFieldMap = null;
		
		// (1) Ver si llega el tag que "engloba" a la colección 
		outColFieldMap = beanInstance.getMapping().getFieldFromXmlNode(currNodeName,false);	// hay un miembro tipo colección que se mapea en el tag?
		if (outColFieldMap == null || !outColFieldMap.getDataType().isCollectionOrMap()) {
			// (2) Los elementos de la colección NO se engloban en un wrapper tag				
			// 	   ...ver si los elementos de la colección se "engloban" directamente en el tag del bean
			// Hay dos casos:
			//		CASO 1: todos los elementos de la colección tienen el mismo tag
			// 				Ej:	<myType>
			//						<myColItem>..</myColItem>
			//						<myColItem>..</myColItem>
			//					</myType>
			// 		CASO 2: los elementos de la colección NO tienen todos el mismo tag
			// 				Ej:	<myType>
			//						<myColItemTypeA>..</myColItemTypeA>
			//						<myColItemTypeB>..</myColItemTypeB>
			//					</myType>
			FieldMap fieldMapCandidate = beanInstance.getMapping().getFieldFromXmlNode(beanInstance.getMapping().getXmlMap().getNodeName(),false);	// Field que se mapea con el nodo que engloba el bean
			if ( fieldMapCandidate != null
			 && (fieldMapCandidate.getDataType().isCollection() || fieldMapCandidate.getDataType().isMap()) ) {
				outColFieldMap = fieldMapCandidate;
			} 
		} 
		// Caso en el que se trata de un elemento de una colección que se mapea directamente con el tag del bean padre
		// SOLO puede haber 
		// ej: 		@XmlRootElement(name="myTag")					<myTag>
		//			public class MyType {								<myColElement>...</myColElement>
		//				@XmlValue										<myColElement>...</myColElement>
		//				private Collection<MyOtherType> _myCol;		</myTag>
		//			}
		// Buscer colecciones en el bean que se mapeen al propio tag
		if (outColFieldMap == null) {
			Collection<FieldMap> colFields = beanInstance.getMapping()
														 .getCollectionOrMapFields();
			if (CollectionUtils.hasData(colFields)) {
				//FieldMap fm = CollectionUtils.of(colFields).pickOneAndOnlyElement();	// solo puede haber un elemento anotado con @XmlValue
				FieldMap fm = CollectionUtils.of(colFields).pickOneElement();	// solo puede haber un elemento anotado con @XmlValue				
				if (fm.getXmlMap().getNodeName().equals(beanInstance.getMapping().getXmlMap().getNodeName())) {
					outColFieldMap = fm;
				}
			}
		}
		// Caso particular del anterior en el que un field es una coleccion o mapa PERO no se puede anotar con @XmlElementWrapper debido al uso de 
		// genericos:
		// ej: 		@XmlRootElement(name="myType")
		// 			public class MyType
		//				 extends MyTypeBase<Collection<String>> {
		//				...
		//			}
		//			public abstract class MyTypeBase<T> {
		//				@XmlElement
		//				private T _value;	<-- this finally is a Collection<String> BUT cannot be annotated with @XmlElementWrapper
		//			}
		if (outColFieldMap == null 
		&& (currNodeName.equals("list") || currNodeName.equals("set") || currNodeName.equals("map"))) {
	        for (FieldMap fm : beanInstance.getMapping().getFields().values()) {
	        	DataType dataType = fm.getDataType();
	        	if (dataType.isObject()) {
	        		// The field is mapped as an Object BUT it's really a collection...
	        		outColFieldMap = new FieldMap(fm);
	        		outColFieldMap.getXmlMap().setNodeName(currNodeName);		// list / set / map
		    		if (currNodeName.equals("map")) {
		    			outColFieldMap.setDataType(DataType.create("Map:" + LinkedHashMap.class.getCanonicalName()));
		    		} else if (currNodeName.equals("list")) {
		    			outColFieldMap.setDataType(DataType.create("Collection:" + LinkedList.class.getCanonicalName()));
		    		} else if (currNodeName.equals("set")) {
		    			outColFieldMap.setDataType(DataType.create("Collection:" + LinkedHashSet.class.getCanonicalName()));
		    		}
	        		break;
	        	}
	        }
		}		
		return outColFieldMap;    				
    }
    /**
     * Guess the {@link BeanMap} for a certain field given it's {@link FieldMap}, XML node name and attributes
     * @param fieldMap
     * @param effectiveNodeName
     * @param attrs
     * @return
     * @throws MarshallerException
     */
    private final BeanMap _beanMapForField(final FieldMap fieldMap,		// FieldMap del miembro al que se va a asignar el bean creado
    								  	   final String effectiveNodeName,final Attributes attrs) throws MarshallerException {
    	// [1] Obtener el tipo de datos a crear y a partir del tipo de datos el BeanMap
        BeanMap theBeanMap = null;
    	DataType actualDataType = null;
    	if (fieldMap.getDataType().isCollection()) {
    		// The dataType IS a collection
    		actualDataType = fieldMap.getDataType().asCollection().getValueElementsDataType();
    		
    	} else if (fieldMap.getDataType().isMap()) {
    		// The dataType IS a Map
    		actualDataType = fieldMap.getDataType().asMap().getValueElementsDataType();
    		
    	} else {
    		// The dataType is an object
    		actualDataType = fieldMap.getDataType();
    	}

		// [2] Get the BeanMap for the DataType
    	if (actualDataType.isInstanciable() && !actualDataType.getName().equals("java.lang.Object")) {
    		// The dataType IS mapped type
    		theBeanMap = actualDataType.getBeanMap();
    		if (theBeanMap == null) {
				// Caso en el que NO se define el tag en el que se mapea el miembro:
				// ej:		@XmlElement 
				//			private MyType myField;		<-- La anotación @XmlElement no lleva el atributo name 
    			theBeanMap = _beanMappings.getBeanMapFromXmlTag(effectiveNodeName);
    		}
    		
    	} else { 		
    		// 2.1 - See if the object's enclosing node is mapped to some type (the node's name must match some type's @XmlRootElement annotation value)
    		theBeanMap = _beanMappings.getBeanMapFromXmlTag(effectiveNodeName);
    		
    		// 2.2 - If NOT 2.1: see if it's an XML attribute that gives some "clues" about the type to be created (the name of this XML attribute is set by the @XmlTypeDiscriminatorAttribute annotation)
    		if (theBeanMap == null) {
	    		// The datatype is either:
	    		//		a) An interface
	    		//		b) A generic Object
	    		// In either case, the concrete type to be instanced is needed.
	    		// In order to know this concrete type two approaches can be used:
	    		// 		1.- See if the object's enclosing node is mapped to some type (the node's name must match some type's @XmlRootElement annotation value)
	    		//		2.- See if it's an XML attribute that gives some "clues" about the type to be created (the name of this XML attribute is set by the @XmlTypeDiscriminatorAttribute annotation)
	    		//			The @XmlTypeDiscriminatorAttribute's value can be:
	    		//				i) the tag name that "encloses" the real type (the one at the type's @XmlRootElement's annotation)
	    		//			   ii) the java class like r01f.types.MyConcreteType 
    			if (fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable() != null) {
    				String discriminator = attrs.getValue(fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable());
    				if (discriminator == null) throw new MarshallerException("The XML node " + effectiveNodeName + " is supposed to have an attribute named " + fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable() + " used to know the concrete type to be instanciated");
    				
    				//  i) try to see if the discriminator's value is the tag that encloses the concrete type
    				theBeanMap = _beanMappings.getBeanMapFromXmlTag(discriminator);
    				
    				// ii) if not... the discriminator's value is the java datatype 
    				if (theBeanMap == null) {
		        		DataType dataType = DataType.create(discriminator);
		        		if (dataType != null) {
			        		theBeanMap = dataType.getBeanMap(); 	// simple types (long, String, etc) are NOT mapped so dataType.getBeanMap() = null
		        		} else {
		        			throw new MarshallerException("El miembro '" + fieldMap.getName() + "' del tipo " + fieldMap.getDeclaringBeanMap().getTypeName() + " es de un tipo NO instanciable (" + actualDataType.getName() + "); para conocer el tipo concreto se ha intentado buscar el mapeo para el tipo indicado en el atributo " + fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable() + "=" + discriminator + " PERO NO se ha encontrado el mapeo de este tipo"); 
		        		}
    				}
    			}
    		}	
    	}
    	return theBeanMap;
    }
	/**
	 * Creates a bean instance to be asigned to another bean's field
	 * @param fieldMap the container bean's field
	 * @param beanMap the bean to be created {@link BeanMap}
	 * @param effectiveNodeName the xml node name
	 * @param attrs the xml node attributes
	 * @return
	 * @throws MarshallerException
	 */
	private final BeanInstance _createBeanInstance(final FieldMap fieldMap,final BeanMap beanMap,	
    											   final String effectiveNodeName,final Attributes attrs) throws MarshallerException {
		BeanInstance outBeanInstance = null;
		
		// [2.1] - Crear el beanInstance
		if (beanMap.isCustomXmlTransformed()) {
    		// El bean está transformado a XML de forma customizada...
    		outBeanInstance = new BeanInstance(beanMap,effectiveNodeName);
    		outBeanInstance.set(new StringBuilder());
    		
		} else {																				// Objeto que NO se transforma con un customXmlTransformer
        	// Objeto complejo que NO tiene asociados customXMLTransformers
        	// El bean puede estar "englobado" en el tag que se indica en el atributo fromElement del miembro o
        	// estarlo en el que se indica en la definición del bean
        	
        	// Crear el nuevo bean que puede ser UNICAMENTE 
    		//		- El bean correspondiente a otro objeto complejo definido en el fichero de mapeo
    		//		  ---> en este caso newBeanInstance.getMapping() != null 
            outBeanInstance = _createBeanInstance(beanMap,
											      effectiveNodeName,attrs,
 			    							      fieldMap.getXmlMap().getDiscriminatorWhenNotInstanciable());		// ignored attrs
		}
    	return outBeanInstance;
    }
    /**
     * Creates a bean instance
     * @param beanMap
     * @param effectiveNodeName
     * @param attrs
     * @param ignoredAttrs
     * @return
     * @throws MarshallerException
     */
	@SuppressWarnings("null")
	private final BeanInstance _createBeanInstance(final BeanMap beanMap,		// BeanMap del bean a crear
    											   final String effectiveNodeName,final Attributes attrs,
    											   final String... ignoredAttrs) throws MarshallerException {
    	if (beanMap != null && beanMap.isCustomXmlTransformed()) {
    		// El bean está transformado a XML de forma customizada...
    		BeanInstance outBeanInstance = new BeanInstance(beanMap,effectiveNodeName);
    		outBeanInstance.set(new StringBuilder());		
    		return outBeanInstance;
    	}
    	
    	// [1] Diferenciar entre los fields finales y los que no lo son
    	//	   Crear tres mapas que asocian el nombre del miembro con el valor del atributo correspondiente
    	//			- Un mapa para los miembros finales (que hay que instanciar en [2] y pasar en el constructor del bean en [3])
    	//			- Otro mapa para los miembros no finales (que se establecen una vez creado el bean en [4])
    	//			- Un último mapa para los atributos NO mapeados y que puedan proceder de objetos complejos expandidos    	
    	Map<String,CharSequence> finalFieldsFromAttrs = Maps.newLinkedHashMap();
    	Map<String,CharSequence> nonFinalFieldsFromAttrs = Maps.newLinkedHashMap();
    	Map<String,CharSequence> attrsFromExpandedObjs = Maps.newLinkedHashMap();
        for (int i=0; i < attrs.getLength(); i++) {
        	String attrName = attrs.getQName(i);
        	if ( _isIgnoredAttribute(attrName,ignoredAttrs) ) continue;	// saltar si el attribute es un type discriminator
        	
        	CharSequence encodedAttrTxt = _textEncoder != null ? _textEncoder.decode(attrs.getValue(i)) 
        													   : attrs.getValue(i); 	// Texto del atributo (decodificar si es necesario)
        	FieldMap attrFieldMap = beanMap.getFieldFromXmlNode(attrName,true);
        	if (attrFieldMap != null && !attrFieldMap.getXmlMap().isExpandableAsAttributes()) {	
        		// usually this is the case: it's a normal attribute (not a one that comes from an expanded complex field)
	        	if (attrFieldMap.isFinal()) {       		
	        		finalFieldsFromAttrs.put(attrFieldMap.getName(),encodedAttrTxt);
	        	} else {
	        		nonFinalFieldsFromAttrs.put(attrFieldMap.getName(),encodedAttrTxt);
	        	}
        	} else {																					
        		// not found attribute... could be an expanded complex object
        		attrsFromExpandedObjs.put(attrName,encodedAttrTxt);
        	}
        }                       
        
        
        // [2] Instance the final fields whoose values come from xml element attributes
        //	   This final fields values MUST be provided at the bean constructor
        Class<?>[] constructorArgsTypes = null;
        Object[] constructorArgs = null;
        if (CollectionUtils.hasData(finalFieldsFromAttrs)) {         	
        	constructorArgsTypes = new Class<?>[finalFieldsFromAttrs.size()];
        	constructorArgs = new Object[finalFieldsFromAttrs.size()];
        	int i=0;
        	for (Map.Entry<String,CharSequence> me : finalFieldsFromAttrs.entrySet()) {
        		// en [1] se ha creado un mapa que asocia el nombre del miembro con el valor del atributo 
	        	String fieldName = me.getKey();				// nombre del field						
	        	CharSequence fieldValue = me.getValue();	// valor del field (valor del atributo)
	        	
				FieldMap attrFieldMap = beanMap.getField(fieldName);	// normalmente serán tipos simples (String, Long, etc)
	        	Object constructorArgInstance = MappingReflectionUtils.simpleObjFromString(attrFieldMap.getDataType(),fieldValue);
	        	Class<?> constructorArgType = attrFieldMap.getDataType().getType();
	        	constructorArgs[i] = constructorArgInstance;
	        	constructorArgsTypes[i] = constructorArgType;
	        	i++;
        	}
        }
    	// [3] Crear el bean (IMPORTANTE: Si se trata de un tipo simple, devuelve un BeanInstance con un StringBuilder)
        BeanInstance beanInstance = new BeanInstance(beanMap,
        											 constructorArgsTypes,constructorArgs,		// Argumentos de la creación del bean...
        											 effectiveNodeName);    
        
        
        // [4] If there's any attribute that comes from an expanded complex object try to map it
        if (CollectionUtils.hasData(attrsFromExpandedObjs)) {
        	
	    	Collection<FieldMap> fieldMapsOfExpandedFields = beanMap.getXmlMap().getFieldsExpandedAsXmlAttributes();
	    	if (!CollectionUtils.hasData(fieldMapsOfExpandedFields)) throw new MarshallerException("Some of the attributes " + attrsFromExpandedObjs.keySet() + " is NOT mapped at " + beanMap.getTypeName()); 
	    		
	    	for (FieldMap expandedField : fieldMapsOfExpandedFields) {
	    		BeanMap expandedBeanMap = expandedField.getDataType().getBeanMap();
	        	Object expandedBeanInstance = null;
		        	
	    		// Try to instance the expanded bean 
	    		Map<String,FieldMap> expandedBeanFinalFields = expandedBeanMap.getFinalFields();
	    		if (CollectionUtils.hasData(expandedBeanFinalFields)) {
		        	constructorArgsTypes = new Class<?>[expandedBeanFinalFields.size()];
		        	constructorArgs = new Object[expandedBeanFinalFields.size()];
		        	int i=0;
		        	for (Map.Entry<String,FieldMap> me : expandedBeanFinalFields.entrySet()) {		        		
			        	FieldMap expandedFieldMap = me.getValue();			// mapeo del field
			        	CharSequence expandedFieldValue = attrsFromExpandedObjs.get(expandedFieldMap.getXmlMap().getNodeName());
			        	Object constructorArgInstance = MappingReflectionUtils.simpleObjFromString(expandedFieldMap.getDataType(),
			        																			   expandedFieldValue);
			        	Class<?> constructorArgType = expandedFieldMap.getDataType().getType();
			        	constructorArgs[i] = constructorArgInstance;
			        	constructorArgsTypes[i] = constructorArgType;
			        	i++;
		        	}
			        Collection<Constructor<?>> constructors = ReflectionUtils.findSuitableConstructors(expandedBeanMap.getDataType().getType(),
						        														 			   constructorArgsTypes);
			        if (CollectionUtils.hasData(constructors)) {
			        	if (constructors.size() > 1) throw new MarshallerException(Throwables.message("{} has more than a single constructor suitable for {}",
			        																				  expandedBeanMap.getDataType().getType(),CollectionUtils.of(constructorArgsTypes).toStringCommaSeparated()));
			        	// Order the args acording to the constructor order
			        	Constructor<?> constructor = CollectionUtils.of(constructors)
			        												.pickOneAndOnlyElement();
			        	Object[] constructorArgsOrdered = new Object[constructorArgs.length];
			        	int j=0;
			        	for (Class<?> constructorArgType : constructor.getParameterTypes()) {
			        		for (Object constructorArg : constructorArgs) {
			        			if (constructorArgType.isAssignableFrom(constructorArg.getClass())) {
			        				constructorArgsOrdered[j] = constructorArg;
			        				j++;
			        				break;
			        			}
			        		}
			        	}
			        	try {
			        		expandedBeanInstance = constructor.newInstance(constructorArgsOrdered);
			        	} catch(Throwable th) {
			        		throw new MarshallerException(th);
			        	}
			        } else {
			        	throw new MarshallerException(Throwables.message("{} does NOT have a constructor suitable for {}",
			        													 expandedBeanMap.getDataType().getType(),CollectionUtils.of(constructorArgsTypes).toStringCommaSeparated()));
			        }
	    		} else {
	    			expandedBeanInstance = ReflectionUtils.createInstanceOf(expandedBeanMap.getDataType().getType());
	    		}
	    		
				// ... now set the non final fields on the recently created instance
	    		Map<String,FieldMap> expandedBeanNonFinalFields = expandedBeanMap.getNonFinalFields();
	    		if (CollectionUtils.hasData(expandedBeanNonFinalFields)) {
	    			for (Map.Entry<String,FieldMap> me : expandedBeanNonFinalFields.entrySet()) {
	    				FieldMap expandedFieldMap = me.getValue();			// mapeo del field
	    				CharSequence expandedFieldValueAsString = attrsFromExpandedObjs.get(expandedFieldMap.getXmlMap().getNodeName());
	    				Object expandedFieldValue = MappingReflectionUtils.simpleObjFromString(expandedFieldMap.getDataType(),
	    																					   expandedFieldValueAsString);
	    				ReflectionUtils.setFieldValue(expandedBeanInstance,
	    											  expandedFieldMap.getName(),
	    											  expandedFieldValue);
	    			}
	    		}
	    		// Set the expanded object instance at the bean's field
	    		beanInstance.getFieldInstance(expandedField)
        					.createInstance(expandedBeanInstance);	
	    	}
        }       
        
	    // [5] Establecer los miembros que vienen de atributos NO finales
        if (CollectionUtils.hasData(nonFinalFieldsFromAttrs)) {
	        for (Map.Entry<String,CharSequence> me : nonFinalFieldsFromAttrs.entrySet()) {	        	
	        	String fieldName = me.getKey();
	        	CharSequence fieldValue = me.getValue();	        	
	        	FieldMap fieldMap = beanMap.getField(fieldName);
	        	
	        	// Instance the fields
	        	// [A] - It's an object with a single simple (long, String, int, etc) field that can be
	        	//		 built using a single-arg constructor
	        	//		 (ej mutable oids which must have a no-arg constructor to be serializable to GWT)
	        	if (fieldMap.getDataType().isObject()
	        	 && fieldMap.getDataType().asObject().hasOnlyOneSimpleField()) {
	        		Object fieldInstance = MappingReflectionUtils.simpleObjFromString(fieldMap.getDataType(),
	        																  	 	  fieldValue);
	        		beanInstance.getFieldInstance(fieldMap)
	        					.createInstance(fieldInstance);
	        	}
//        		// [B] - It's an inmmutable object with a single simple (long, String, int, etc) field
//	        	//		 it can be constructed using a single arg constructor
//	        	//		 (ej inmutable oids)
//	        	if (fieldMap.getDataType().isObject()
//	        		  && fieldMap.getDataType().asObject().isInmutable()							// inmutable object
//	        		  && fieldMap.getDataType().asObject().hasOnlyOneFinalSimpleField()) {			// field to hold the id
//	        		DataType argDataType = fieldMap.getDataType().asObject()
//	        													 .getSingleFinalSimpleField();
//	        		Class<?> objType = argDataType.getType();
//	        		Object obj = MappingReflectionUtils.simpleObjFromString(argDataType,fieldValue);
//	        		beanInstance.getFieldInstance(fieldMap)
//	        					.createInstance(new Class<?>[] {objType},new Object[] {obj});
//	        		
//	        	}
//	        	// [C] - It's NOT an inmutable objet, BUT it has a single simple (long, String, int, etc) field
//	        	//		 AND can be constructed using a single arg constuctor
//	        	//		 (ej mutable oids which must have a no-arg constructor to be serializable to GWT)
//	        	else if (fieldMap.getDataType().isObject() 
//	        		  && fieldMap.getDataType().asObject().hasOnlyOneSimpleField()) {
//	        		DataType argDataType = fieldMap.getDataType().asObject()
//	        													 .getSingleSimpleField();
//	        		Class<?> objType = argDataType.getType();
//	        		Object obj = MappingReflectionUtils.simpleObjFromString(argDataType,fieldValue);
//	        		beanInstance.getFieldInstance(fieldMap)
//	        					.createInstance(new Class<?>[] {objType},new Object[] {obj});
//	        		
//	        	}
        		// [C] - It's NOT an inmutable object, use the default no-arg constructor and
	        	//		 set each of the fields
	        	else { 
		        	Object instance = beanInstance.getFieldInstance(fieldMap)
		        								  .createInstance();
		        	
		        	if (instance instanceof StringBuilder) {
		        		// La instancia del miembro es un tipo simple (String, Long, etc) que se crea a partir del valor del atributo
			        	StringBuilder sb = (StringBuilder)instance;
			            sb.append(fieldValue); 	
			            
		        	} else if (instance instanceof BeanInstance) {
		        		// la instancia del miembro probablemente es un tipo complejo que se crea con un CustomTransformer a partir del valor del atributo
		        		Object attrObj = null;
		        		BeanInstance attrBeanInstance = (BeanInstance)instance;
		        		if (attrBeanInstance.getMapping().isCustomXmlTransformed()) {
		        			attrObj = attrBeanInstance.getMapping().getCustomXMLTransformers()
        														   .getXmlReadTransformer()
        														   .beanFromXml(fieldMap.getXmlMap().isAttribute(),
        																   		fieldValue);
		        		} else {
		        			attrObj = ReflectionUtils.createInstanceFromString(attrBeanInstance.getMapping().getDataType().getType(),
		        													   		   fieldValue.toString());
		        		}
		        		attrBeanInstance.set(attrObj);
		        	}
	        	}
	        }
        }
        // [6] Crear una instancia para TODOS los miembros que NO sean un objeto complejo
        //	   (los objetos complejos se crean llamando de nuevo a este método desde el tag START del bean
        if (beanMap != null && beanMap.getFields() != null) {
	        for (FieldMap fieldMap : beanMap.getFields().values()) {
	        	FieldInstance fieldInstance = beanInstance.getFieldInstance(fieldMap);
	        	
	        	// los objetos complejos se crean en el método START del bean
	        	if (fieldInstance.getMapping().getXmlMap().isAttribute() || fieldInstance.getMapping().getDataType().isObject()) continue;
	        	
	        	// se crea una instancia para tipos simples y colecciones... (no objetos complejos)
	        	if (fieldInstance.get() == null) {
	        		fieldInstance.createInstance();
	        	}
	        }
        }
        return beanInstance;
    } 
    private boolean _isIgnoredAttribute(final String attr,final String... ignoredAttrs) {
		boolean ignoreAttr = false;
    	if (ignoredAttrs != null) {
    		for (String ignoredAttr : ignoredAttrs) {
    			if (ignoredAttr == null) continue;
    			if (ignoredAttr.equals(attr)) {
    				ignoreAttr = true;
    				break;
    			}
    		}
    	}
    	return ignoreAttr;
    }
    private void _rawXMLStartElement(final BeanAndFieldWrapper beanAndField,
    								 final String tagName,
    								 final Attributes attrs) {
//    	log.debug("[--- START RAWXML]: {}",tagName);
    	boolean isFirstTagInRawXML = _rawXMLTags.isEmpty();
    	String rawXmlEnclosingTag = isFirstTagInRawXML ? tagName 
    												   : _rawXMLTags.peekFirst(); 	// el tag que "engloba" el xml es el primero de la pila
        if (tagName.equals(rawXmlEnclosingTag)) _rawXMLTags.push(tagName);			// ...por si el tag raw xml aparece varias veces en el raw xml (ej: <rawXML>...<rawXML>adsf</rawXML>...</rawXML>)
        
    	StringBuilder xmlSb = _textBuffer(beanAndField);
        xmlSb.append("<").append(tagName);
        for (int i=0; i<attrs.getLength(); i++) xmlSb.append(" ")
        										     .append(attrs.getQName(i)).append("='")
        											 .append(attrs.getValue(i))
        											 .append("'");
        xmlSb.append(">");
    }
    private void _rawXmlEndElement(final String tagName,
    							   final BeanAndFieldWrapper beanAndField) {
//    	log.debug("--- [END RAWXML]: {}",tagName);
    	boolean isFirstTagInRawXML = _rawXMLTags.isEmpty();
    	String rawXmlEnclosingTag = isFirstTagInRawXML ? tagName
    												   : _rawXMLTags.peekFirst(); 		// el tag que "engloba" el xml es el primero de la pila        	
    	if (tagName.equals(rawXmlEnclosingTag)) _rawXMLTags.pop(); 	// ...por si el tag raw xml aparece varias veces en el raw xml (ej: <rawXML>...<rawXML>adsf</rawXML>...</rawXML>)
    	isFirstTagInRawXML = _rawXMLTags.isEmpty();
    	
		StringBuilder xmlSb = _textBuffer(beanAndField);
        xmlSb.append("</").append(tagName).append(">");
    }
    
    
    
    /**
     * Obtiene el buffer donde poner el valor de los miembros de un tipo simple o XML-raw
     */
    private StringBuilder _textBuffer(final BeanAndFieldWrapper beanAndField) { 
    	StringBuilder sb = null;
    	// [1]: the xml is mapped to a field
    	if (beanAndField.getFieldInstance() != null 
        		&& beanAndField.getFieldInstance().get() instanceof StringBuilder) {	// el xml se mapea en un miembro
        	sb = beanAndField.getFieldInstance().get();									// ... se toma la instancia del field        	
        }
    	// [2]: the xml is mapped through a customXmlTransformer
    	else if (beanAndField.getFieldInstance() != null 
        	    && beanAndField.getFieldInstance().get() instanceof BeanInstance) {		// el xml se mapea con un customXmlTransformer
        	BeanInstance customXmlTransformedInstance = beanAndField.getFieldInstance().get();
        	sb = customXmlTransformedInstance.get();
        	if (sb == null) {
        		sb = new StringBuilder();
        		customXmlTransformedInstance.set(sb);
        	}
        }
    	// [3]: simple types (String, boolean, long, XML, etc)
    	else if (beanAndField.getBeanInstance() != null 
        	    && beanAndField.getBeanInstance().get() instanceof StringBuilder) { // beanAndField.getBean().getMapping() == null)
        	// objeto "virtual" para elementos de tipo "simple" (String, long, XML, etc) en colecciones
        	sb = beanAndField.getBeanInstance().get();
        } 
        return sb;
    }

    
}   // Fin de la clase cargadora..

///////////////////////////////////////////////////////////////////////////////////////////
//  AUX INNER TYPE
///////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
    @RequiredArgsConstructor 
    private class BeanAndFieldWrapper {
    	@Getter private final BeanInstance _beanInstance;
    	@Getter private final FieldInstance _fieldInstance;
    	
    	public boolean isValid() {
    		return _beanInstance != null && _fieldInstance != null;
    	}
    	public BeanMap getBeanMap() {
    		return _beanInstance != null ? _beanInstance.getMapping() : null;
    	}
    	public FieldMap getFieldMap() {
    		return _fieldInstance != null ? _fieldInstance.getMapping() : null;
    	}
    }

}   // it's over!
