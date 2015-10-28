package r01f.marshalling.simple;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import lombok.Getter;
import lombok.experimental.Accessors;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import r01f.marshalling.MarshallerException;
import r01f.marshalling.simple.DataTypes.DataType;
import r01f.marshalling.simple.DataTypes.DataTypeEnum;
import r01f.marshalling.simple.FieldMap.Relation;
import r01f.reflection.ReflectionUtils;

/**
 * Clase auxiliar que se encarga de cargar los mapeos de beans a partir de 
 * un XML de mapeo
 * Obtiene una representación en memoria de la definición en XML de 
 * una jerarquia de clases.
 * Un ejemplo del XML es:
 *   <classes encoding='iso-8859-1'>
 *           <class name='com.ejie.r01f.xmlbeans.Proyecto' fromElement='proyecto' useAccessors='true'>
 *                   <member name='oid' dataType='String' isOID='true' fromAttribute='oid' isCDATA='true'/>
 *                   <member name='nombre' dataType='String' fromElement='nombre'/>
 *                   <member name='presupuestos' dataType='List(Presupuesto)' fromElement='presupuestos'/>
 *                   <member name='destinatarios' dataType='Map(String,Destinatario)' fromElement='destinatarios'/>
 *                   <member name='interlocutor' dataType='Interlocutor' fromElement='interlocutor'/>
 *           </class>
 *           <class name='com.ejie.r01f.xmlbeans.Presupuesto' fromElement='presupuesto' useAccessors='true'>
 *                   <member name='oid' dataType='String' isOID='true' fromAttribute='oid' isCDATA='true'/>
 *                   <member name='periodo' dataType='String' fromElement='periodo'/>
 *                   <member name='valor' dataType='String' fromElement='presupuesto'/>
 *           </class>
 *           <class name='com.ejie.r01f.xmlbeans.Destinatario' fromElement='destinatario' useAccessors='true'>
 *                   <member name='oid' dataType='String' isOID='true' fromAttribute='oid' isCDATA='true'/>
 *                   <member name='nombre' dataType='String' fromElement='nombre'/>
 *           </class>
 *           <class name='com.ejie.r01f.xmlbeans.Interlocutor' fromElement='interlocutor' useAccessors='true'>
 *                   <member name='oid' dataType='String' isOID='true' fromAttribute='oid' isCDATA='true'/>
 *                   <member name='nombre' dataType='String' fromElement='nombre'/>
 *                   <member name='telefono' dataType='String' fromElement='telefono'/>
 *           </class>
 *   </classes>
 * Trukos de definición de mapeos:
 * [TRUKO 0]: Bases
 * -----------------------------------------------------------------------------------------------------------------------------
 * 		- Para utilizar metodos get/set en una clase establecer la propiedad useAccesors='true' en la definicion de la clase
 * 		- Los tipos de java.lang (String, Object, Integer, Long, Double, etc) se pueden nombrar sin utilizar el paquete o con el (indistintamente)
 * 		- Las colecciones (Mapas y listas) se pueden nombrar como java.util.[tipoColecion] o simplemente como [tipoColeccion] 
 * 
 * [TRUKO 1]: Lista / mapa de objetos complejos:
 * --------------------------------------------
 * Seguir los siguientes pasos:
 * 1.- En el objeto padre que tiene como miembro la lista / mapa incluir un miembro:
 *    	<member name='miembroListaMapa' dataType='List(ElementoHijoClass)' fromelement='hijos' />
 *      o bien:
 *      <member name='miembroListaMapa' dataType='Map(String,ElementoHijoClass)' fromelement='hijos' />
 *      ... aunque tambien se puede "dejar" al marshaller que "adivine" el tipo de los elementos de la coleccion
 *      <member name='miembroListaMapa' dataType='List' fromelement='hijos' />
 *      o bien:
 *      <member name='miembroListaMapa' dataType='Map' fromelement='hijos' />
 *          
 *      Hay dos posibilidades:
 *      	a.- Los elementos de la colección estan "englobados" en un tag especifico dentro del objeto "padre"
 *      		<padre>
 *      			<miembro>..</miembro>
 *      			<colección>
 *      				<colObj>...
 *      				<colObj>...
 *      			</coleccion>
 *      		</padre>
 *      		En este caso la definicion sera 
 *      		<member name='miembroListaMapa' dataType='tipo coleccion' 
 *      				fromelement='coleccion' />		-- fromElement es el tag que engloba los elementos de la coleccion
 *      	b.- Los elementos de la colección "cuelgan" directamente del tag que engloba el objeto padre
 *      		 <padre>
 *      			<miembro>..</miembro>
 *      			<colección>
 *      				<colObj>...
 *      				<colObj>...
 *      			</coleccion>
 *      		</padre>
 *      		En este caso la definicion sera 
 *      		<member name='miembroListaMapa' dataType='tipo coleccion' 
 *      				fromelement='padre' />	-- fromElement es el tag que engloba los elementos de la coleccion,
 *      										   que en este caso coincide con el tag que engloba el objeto padre
 *      
 * 2.- Incluir la definición del objeto hijo (ElementoHijoClass):
 *     <class name='ElementoHijoClass' package='com.ejie' fromElement='hijo'>
 *     ... todos los miembros ...
 *     </class>
 *     
 * Si se trata de un MAPA uno de los miembros del objeto hijo debe:
 * 	- Opcion 1: tener el atributo isOID='true'
 * 	- Opcion 2: incluir el atributo oidAccessorMethod en la definición de la clase hijo, 
 * 				que se utiliza para indicar cual es el método a llamar para obtener la clave para indexar cada 
 * 				instancia de la clase en un mapa (ej: oidAccessorMethod='getKey')
 * 					Esta propiedad es util cuando el oid no se compone a partir de una un unico miembro 
 * 					(en cuyo caso se pondría <member name='oid' dataType='String' isOID='true' fromAttribute='oid'/>)
 * 					sino que se compone por ejemplo concatenando los valores de varios miembros
 * lo que va a hacer que se indexe en el mapa utilizando el valor de este miembro
 *              
 * [TRUKO 2]: Un mapa de Strings o XMLs en el que la clave del mapa es el propio tag:
 * ---------------------------------------------------------------------------------
 * <member name='miembro' dataType='Map(tipoKey,tipoValue)' 
 * 		   fromElement='properties' 
 * 		   isCDATA='true/false'/>
 * (en este caso además cada elemento será CDATA o no segun el valor del atributo)
 * Ejemplo:      <properties>
 *                    <prop1>val1</properties>
 *                    <prop2>val2</properties>
 *               </properties>
 *      
 * [TRUKO 3]: Lista de Strings, enteros, fechas etc
 * ------------------------------------------------
 *        		<member name='miembro' dataType='String' collection='List/Array(tipo)' 
 *        				fromElement='valores' 
 *        				ofElements='valor'/>
 *       	 Ejemplo:      <valores>
 *                          <valor>1</valor>
 *                          <valor>2</valor>
 *                          ...
 *                      <valores>
 *                      
 * [TRUKO 4]: Mapas / Listas de objetos
 * ------------------------------------
 * Si se tiene un Mapa/Lista en el que se pueden mezclar diferentes tipos de objetos, basta con definirlo como:
 * 		<member name='mapOfUnknownObjsField' dataType='Map(String,Object)' fromElement='mapOfUnknownObjects'/>")
 *                      
 * [TRUKO 5]: Un miembro tipo fecha con formateo al serializar a XML
 * -----------------------------------------------------------------
 *        		<member name='miembroFecha' dataType='Date(dd/MM/yyyy-hh:mm:ss)' fromElement='fecha'/>
 *        		<member name='otroMiembroFecha' dataType='SQLDate(dd-MM-yy)' fromElement='fechaSQL'/>
 *        
 * [TRUKO 6]: Reutilización de clases con distinto tag
 * -----------------------------------------------------------------
 * Las clases:
 * 		class TestObj {
 * 			@Getter @Setter private ChildObj _childObj;
 * 			@Getter @Setter priaver List<ChildObj> _listOfChildObjs;
 * 		}
 *   	class ChildObj {
 *   		@Getter @Setter private String _key;
 *   		@Getter @Setter private String _value;
 *   	}
 * normalmente se mapearían así:
 * 		<testObj>
 * 			<childObj key="theKey" value="theValue"/>
 *      	<listOfChildObjs>
 *   			<childObj key="theKey00" value="theValue00">
 *   			<childObj key="theKey00" value="theValue00">
 *   		</listOfChildObjs>
 *   	</testObj>
 * con esta definición de clases:
 * 		<class name='r01f.marshalling.TestObj' fromElement='testObj' useAccessors='true'>
 * 			<member name='childObj' dataType='r01f.marshalling.TestChildObj' 
 * 					fromElement='childObj'/>")
 * 			<member name='listOfChildObj' dataType='List(r01f.marshalling.TestChildObj)' 
 * 					fromElement='listOfChildObjs'/>")
 * 		</class> 
 * 		<class name='r01f.marshalling.TestChildObj' fromElement='childObj' useAccessors='true'>
 * 			<member name='key' dataType='String' fromAttribute='key' isOID='true'/>
 * 			<member name='value' dataType='String' fromAttribute='value'/>")
 * 		</class>
 * 
 * pero si se quiere se puede cambiar el tag de la clase en función del contexto para 
 * tener un XML algo diferente SIN cambiar mucho el mapeo:
 * 		class TestObj {
 * 			@Getter @Setter private ChildObj _childObj;
 * 			@Getter @Setter private ChildObj _otherChildObj;
 * 			@Getter @Setter priaver List<ChildObj> _listOfChildObjs;
 * 		}
 *   	class ChildObj {
 *   		@Getter @Setter private String _key;
 *   		@Getter @Setter private String _value;
 *   	}
 * 		<testObj>
 * 			<childObjFake key="theKey" value="theValue"/>
 *      	<listOfChildObjs>
 *   			<childObjFake key="theKey00" value="theValue00">
 *   			<childObjFake key="theKey00" value="theValue00">
 *   		</listOfChildObjs>
 *   	</testObj>
 *   	>>>>> Fijarse en que:
 *   			- TestObj tiene DOS miembros TestChildObj; la única forma de diferenciarlos es que 
 *   			  tengan un tag diferente
 *   			- los hijos de la lista NO tienen el tag childObj sino childObjFake
 *   
 * 		<class name='r01f.marshalling.TestObj' fromElement='testObj' useAccessors='true'>
 * 			<member name='childObj' dataType='r01f.marshalling.TestChildObj' 
 * 					fromElement='childObj'/>")
 * 			<member name='otherChildObj' dataType='r01f.marshalling.TestChildObj' 
 * 					fromElement='childObjFake'/>")								// <-- sobre-escribir el tag del objeto!!
 * 			<member name='listOfChildObj' dataType='List(r01f.marshalling.TestChildObj)' 
 * 					fromElement='listOfChildObjs' ofElements='childObjFake'/>")	// <-- sobre-escribir el tag de cada elemento
 * 		</class>
 *  
 * [TRUKO 7]: Soporte de Enums
 * -----------------------------------------------------------------
 * Basta con indicar en el fichero de mapeo que el miembro es un enum y se puede mapear en un atributo o en un elemento:
 * Ej: <member name='enum' dataType='Enum(r01f.marshalling.TestEnum)' fromAttribute='enum'/>"
 * 
 * [TRUKO 8]: Clases inmutables y miembros finales
 * -----------------------------------------------------------------
 * Cuando una clase tiene miembros finales que hay que establecer en el constructor:
 * 		public class MyType {
 * 			final String _myFinalMember;
 * 			public MyClass(String finalMember) {
 * 				_myFinalMember = finalMember;
 * 			}
 * 		}
 * si TODOS los miembros finales se mapean a ATRIBUTOS del tag que engloba la clase en el XML, 
 * es posible invocar al constructor con los valores de los miembros finales:
 * Es importante que los miembros en el XML se definan en el MISMO ORDEN que luego tienen en el constructor
 * y se incluya el atributo isFinal=true
 * 		<class name='r01f.marshalling.MyType' fromElement='MyType' useAccessors='true'>
 * 			<member name='myFinalMember' dataType='java.lang.String' isFinal='true' fromAttribute='myFinalMember'/>")
 * 		</class>
 */
@Accessors(prefix="_")
class SimpleMarshallerMappingsFromXMLLoader extends DefaultHandler {
    // Constantes del documento XML
    private static final String CLASSES = "classes";
    private static final String CLASS = "class";
    private static final String USEACCESSORS = "useAccessors";
    private static final String OIDACCESSORMETHOD = "oidAccessorMethod";
    private static final String CUSTOM_XMLTRANSFORMERS = "customXmlTransformers";
    private static final String CUSTOM_XMLREADTRANSFORMER = "xmlRead";
    private static final String CUSTOM_XMLWRITETRANSFORMER = "xmlWrite";
    private static final String NAME = "name";
    private static final String MEMBER = "member";
    private static final String DATATYPE = "dataType";
    private static final String ISFINAL = "isFinal";
    private static final String RELATION = "relation";        
    private static final String CREATEMETHOD = "createMethod";
    private static final String ISOID = "isOID";
    private static final String FROMATTRIBUTE = "fromAttribute";
    private static final String FROMELEMENT = "fromElement";
    private static final String OFELEMENTS = "ofElements";
    private static final String ISCDATA = "isCDATA";
    private static final String ISTRANSIENT = "isTransient";
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////    
    @Getter private Map<String,BeanMap> _loadedBeans;    	// Mapa que contiene las clases relacionadas con su nombre
///////////////////////////////////////////////////////////////////////////////////////////
//  VARIABLES
///////////////////////////////////////////////////////////////////////////////////////////
    private Stack<BeanMap> _beansStack = null;  	// Pila de objetos en carga
    private SimpleMarshallerCustomXmlTransformers.XmlReadCustomTransformer<?> _xmlReadTransformer;
    private SimpleMarshallerCustomXmlTransformers.XmlWriteCustomTransformer _xmlWriteTransformer;
///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ SAX
/////////////////////////////////////////////////////////////////////////////////////////// 
    /** Principio del documento XML */
    @Override
    public void startDocument() {
        _beansStack = new Stack<BeanMap>();  		// Nueva pila para objetos en proceso
    }

    /** Principio de un elemento XML */
    @Override
    public void startElement(String namespaceURI,
                             String lName, // local name
                             String qName, // qualified name
                             Attributes attrs) throws SAXException {                
        // Obtener el nombre del tag
        String eName = lName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false

        if ( eName.equalsIgnoreCase(CLASSES) ) {
            _loadedBeans = new HashMap<String,BeanMap>();   	// Mapa que relaciona la definición de las clases con su nombre de clase
            
        } else if ( eName.equalsIgnoreCase(CLASS) ) {
            // Crear un nuevo objeto beanMap y obtener su nombre y paquete
            BeanMap newBean = new BeanMap();                
            if (attrs != null) {
                if ( attrs.getValue(NAME) != null ) 				newBean.setTypeName( attrs.getValue(NAME).trim() );
                if ( attrs.getValue(FROMELEMENT) != null )  		newBean.getXmlMap().setNodeName( attrs.getValue(FROMELEMENT).trim() );
                if ( attrs.getValue(USEACCESSORS) != null ) 		newBean.setUseAccessors( attrs.getValue(USEACCESSORS).trim().equalsIgnoreCase("true") ? true:false );
                if ( attrs.getValue(OIDACCESSORMETHOD) != null )	newBean.setOidAccessorMethod( attrs.getValue(OIDACCESSORMETHOD).trim() );
            } 
            try {
            	_checkBean(newBean);			// Comprobaciones de seguridad
            } catch (MarshallerException xoEx) {
            	throw new SAXException(xoEx); 
            }
            newBean.setDataType(DataType.create(newBean.getTypeName()));		// Obtener información del tipo de dato
            
            _beansStack.push(newBean);			// Introducir el nuevo objeto en la pila de objetos en proceso
	
        } else if ( eName.equalsIgnoreCase(CUSTOM_XMLREADTRANSFORMER) || eName.equalsIgnoreCase(CUSTOM_XMLWRITETRANSFORMER) ) {
            if (attrs != null) {
            	String clsName = attrs.getValue(CLASS);
            	if (clsName != null && eName.equalsIgnoreCase(CUSTOM_XMLREADTRANSFORMER)) {
            		_xmlReadTransformer = ReflectionUtils.createInstanceOf(clsName);
            	} else if (clsName != null && eName.equalsIgnoreCase(CUSTOM_XMLWRITETRANSFORMER)) {
            		_xmlWriteTransformer = ReflectionUtils.createInstanceOf(clsName);
            	}
            }
            
        } else if ( eName.equalsIgnoreCase(MEMBER) ) {
            // Crear un nuevo objeto FieldMap y asociarlo a la última clase en proceso
            FieldMap newField = new FieldMap();
            BeanMap currBean = _beansStack.peek();
            if (currBean == null) throw new SAXException( new MarshallerException("Un member debe ir siempre asociado a un bean: Revisa el xml de mapeo") ); 
            if (attrs != null) {                
                // ---: Nombre del miembro
            	String name = null;                	
                if ( (name = attrs.getValue(NAME)) != null ) newField.setName( name.trim() );
                
                // ---: Es final?
                String isFinal = null;
                if ( (isFinal = attrs.getValue(ISFINAL)) != null )	newField.setFinal(isFinal.trim().equals("true") ? true:false);
                
                // ---: Tipo de dato
                String dataType = null;
                if ( (dataType = attrs.getValue(DATATYPE)) != null ) {
                	DataType theDataType = DataType.create(dataType.trim()); 	// ([a-zA-Z]+(\\([^)]+\\))?                   
                    newField.setDataType(theDataType);
                } 
                // ---: ¿Es CDATA?
                String cdata = null;                    
                if ( (cdata = attrs.getValue(ISCDATA)) != null ) newField.getXmlMap().setCdata(cdata.trim().equals("true") ? true:false);
                
                // ---: Tipo de Relacion
                String relation = null;                    
                if ( (relation = attrs.getValue(RELATION)) != null ) {
                	FieldMap.Relation theRel = Relation.create( relation.trim() );
                    if (theRel == null) throw new SAXException( new MarshallerException("El tipo de relacion '" + relation + "' indicado en el miembro " + newField.getName() + " de la clase " + currBean.getTypeName() + " NO esta soportado. Revisa el xml de mapeo") );
                    newField.setRelation(theRel);
                }                                        
                // ---: Nombre del metodo create
                String createMethod = null;                    
                if ( (createMethod = attrs.getValue(CREATEMETHOD)) != null ) { 
                    newField.setCreateMethod( createMethod.trim() );             
                }                                 
                // ---: ¿Es transient ?
                String isTransient = null;                    
                if ( (isTransient = attrs.getValue(ISTRANSIENT)) != null ) {
                	newField.setTranzient(isTransient.trim().equals("true") ? true:false);
                }                    
                // ---: ¿Es oid ?
                String isOid = null;                    
                if ( (isOid = attrs.getValue(ISOID)) != null ) {
                	newField.setOid(isOid.trim().equals("true") ? true : false);
                }                   
                // ---: Tag XML (NO MOVER DE AQUÍ!!!!)
                String xmlNodeName = null;     
                if ( (xmlNodeName = attrs.getValue(FROMATTRIBUTE)) != null ) {
                	newField.getXmlMap().setAttribute(true);                    	
                } else if ( (xmlNodeName = attrs.getValue(FROMELEMENT)) != null ) {                    	
                	newField.getXmlMap().setAttribute(false);
                }
                newField.getXmlMap().setNodeName(xmlNodeName); 
                
                // Si se trata de un tipo coleccion, se puede especificar el tag de los elementos de la lista
                if (newField.getDataType().isCollection()) {
                	String colElsNodeName = null;
                	if ( (colElsNodeName = attrs.getValue(OFELEMENTS)) != null ) {
                		newField.getXmlMap().setColElsNodeName(colElsNodeName);
                	}
                }
            }  
            
            // ---: Añadir el field al bean (NO mover de aqui)
            try {
            	currBean.addField(newField);
            	_checkField(newField);				// Comprobaciones
            } catch (MarshallerException xoEx) {
            	throw new SAXException(xoEx);
            }
        } else {
            throw new SAXException( new MarshallerException("Error en la configuracion del documento de mapeo: Hay un elemento que no es 'CLASS' o 'MEMBER'") );
        }
    }

    /** Caracteres del elemento XML */
    @Override
    public void characters(char buf[], int offset, int len) {
        //String s = new String(buf, offset, len);
        //String currentTag = (String)_tagStack.peek();
    }        

    /** Fin de un elemento XML */
    @Override
    public void endElement(String namespaceURI,
                           String sName, // simple name
                           String qName  // qualified name
                           ) throws SAXException {
        String eName = sName; // element name
        if ("".equals(eName)) eName = qName; // namespaceAware = false   

        if (eName.equalsIgnoreCase(CLASS)) {
            // Meter la nueva clase en el mapa de clases
            BeanMap currBean = _beansStack.pop();
            BeanMap otherBean =  _loadedBeans.put(currBean.getTypeName(),currBean);
            if ( otherBean != null ) throw new SAXException( new MarshallerException("La clase " + currBean.getTypeName() + " está duplicada en el xml de mapeo, revisalo") );
            
            // importante!! reinicializar a null los transformers para que no se mezclen con los de otro bean
            _xmlReadTransformer = null;
            _xmlWriteTransformer = null;
            
        } else if ( eName.equalsIgnoreCase(CUSTOM_XMLTRANSFORMERS) ) {
        	// Crear un nuevo objeto XmlCustomTransformers
        	BeanMap currBean = _beansStack.peek();
            if (currBean == null) throw new SAXException( new MarshallerException("Los xml custom transformer deben ir siempre asociados a un bean: Revisa el xml de mapeo") );
            if (_xmlReadTransformer == null || _xmlWriteTransformer == null) throw new SAXException( new MarshallerException("Siempre que en un tipo se utilizan CustomXmlTransformers, hay que indicar ambos: XmlReadTransformer y XmlWriteTransformer; revisa el xml de mapeo"));
            currBean.setCustomXMLTransformers(new SimpleMarshallerCustomXmlTransformers(_xmlReadTransformer,_xmlWriteTransformer));
        }
    }

    /** Fin del documento XML */
    @Override
    public void endDocument() throws SAXException {
    	try {
    		// Conecta el objeto {@link BeanMap} en los miembros {@link FieldMap} de tipo objeto o colección
    		// Este proceso hay que hacerlo DESPUES de cargar todos los beans 
    		SimpleMarshallerMappings.connectBeanMappings(_loadedBeans);
    	} catch(MarshallerException msEx) {
    		throw new SAXException(msEx);
    	}
    } 
    /**
     * Comprueba la validez ce la definición de un bean
     * @param newBean el bean
     * @throws SAXException si el bean NO esta bien definido
     */
    private static void _checkBean(BeanMap newBean) throws MarshallerException {
	    if (newBean.getTypeName() == null) throw new MarshallerException("Hay una clase para la que NO se define nombre. Revisa el xml de mapeo");
        if (newBean.getXmlMap().getNodeName() == null) throw new MarshallerException("La clase " + newBean.getTypeName() + " NO define el nodo XML en la que se mapea. Revisa el xml de mapeo");
    }
    /**
     * Comprueba la validez de la definición de un miembro
     * @param newField el miembro
     * @throws SAXException si el bean NO está bien definido
     */
    private static void _checkField(FieldMap newField) throws MarshallerException {
    	if (newField.getName() == null) throw new MarshallerException("El nombre de un miembro de la clase " + newField.getDeclaringBeanMap().getTypeName() + " NO se ha definido. Revisa el xml de mapeo");
    	if (newField.getDataType() == null) throw new MarshallerException("El tipo de datos del miembro " + newField.getName() + " de la clase " + newField.getDeclaringBeanMap().getTypeName() + " NO se ha definido o es incorrecto. Revisa el xml de mapeo");
    	
    	// Comprobar que si es un atributo NO puede ser un objeto, una coleccion o mapearse como CDATA
        if (newField.getXmlMap().isAttribute()) {
        	if (newField.getDataType().isCollection()) throw new MarshallerException("Si un miembro se mapea como atributo NO puede ser una coleccion. Revisa el miembro " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName());
        	if (newField.getDataType().getTypeDef() == DataTypeEnum.OBJECT) throw new MarshallerException("Si un miembro se mapea como atributo NO puede ser un objeto 'complejo'. Revisa el miembro " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName());
        	if (newField.getXmlMap().isCdata()) throw new MarshallerException("Si un miembro se mapea como atributo NO puede ser CDATA. Revisa el miembro " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName());            	
        }
        // Comprobar si el atributo CDATA es correcto
        // (solo permitido para tipos String o List<String> o Map<String,String>)
        if (newField.getDataType().isObject()) {
        	if (newField.getXmlMap().isCdata()) throw new MarshallerException("Si un miembro es un objeto NO pude ser CDATA. Revisa el miembro " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName());         
        } else if (newField.getDataType().isSimple()) {
        	if (newField.getDataType().getTypeDef() != DataTypeEnum.STRING && newField.getXmlMap().isCdata()) throw new MarshallerException("Si un miembro es un tipo primitivo solo puede ser CDATA si es de tipo String. Revisa el miembro " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName());
        }
        // Comprobar que NO hay dos miembros que se mapean al mismo nodo xml
        if (newField.getXmlMap().isAttribute()) {
        	int matchCount = 0;
        	for (FieldMap currField : newField.getDeclaringBeanMap().getFields().values()) {
        		if (currField.getXmlMap().isAttribute() 
        		 && currField.getXmlMap().getNodeName().equals(newField.getXmlMap().getNodeName())) {
        			matchCount++;
        			if (matchCount > 1) throw new MarshallerException("El miembro " + newField.getName() + " de la clase " + newField.getDeclaringBeanMap().getTypeName() + " se mapea en el atributo " + newField.getXmlMap().getNodeName() + " pero hay otro miembro de la misma clase que tambien se mapea a ese mismo atributo!");
        		}
        	}
        } else {
        	int matchCount = 0;
        	for (FieldMap currField : newField.getDeclaringBeanMap().getFields().values()) {
        		if (!currField.isTranzient() && !currField.getXmlMap().isAttribute() 
        		 && currField.getXmlMap().getNodeName() != null
        		 && currField.getXmlMap().getNodeName().equals(newField.getXmlMap().getNodeName())) {
        			matchCount++;
        			if (matchCount > 1) throw new MarshallerException("El miembro " + newField.getName() + " de la clase " + newField.getDeclaringBeanMap().getTypeName() + " se mapea en el elemento " + newField.getXmlMap().getNodeName() + " pero hay otro miembro de la misma clase que tambien se mapea a ese mismo elemento!");
        		}
        	}
        }
        // Comprobar que si se trata de un enum, se indica el nombre del tipo del enum
        if (newField.getDataType().isEnum() && newField.getDataType().asEnum().getEnumTypeName() == null) {
        	throw new MarshallerException("Si un miembro es Enum, hay que indicar el tipo de enum (ej. Enum(r01f.enums.MyEnum). revisa el nimebro " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName());
        }
        
        // Comprobar que los campos finales se mapean a atributos y que son de tipo simple
        if (newField.isFinal()) {
        	if (!newField.getXmlMap().isAttribute()) throw new MarshallerException("El field " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName() + " se ha declarado como final, sin embargo NO se mapea a un atributo XML; solo los atributos XML pueden mapearse a fields finales del bean!!");
        	if (!newField.getDataType().isSimple()) throw new MarshallerException("El field " + newField.getName() + " del bean " + newField.getDeclaringBeanMap().getTypeName() + " se ha declarado como field, sin embargo NO es de un tipo simple (String, long, etc)");
        }
    }
    
}   // fin de clase auxiliar cargadora
