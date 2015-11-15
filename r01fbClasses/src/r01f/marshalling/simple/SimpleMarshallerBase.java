package r01f.marshalling.simple;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.w3c.dom.Node;

import r01f.encoding.TextEncoder;
import r01f.marshalling.Marshaller;
import r01f.marshalling.MarshallerException;
import r01f.marshalling.MarshallerMappings;
import r01f.util.types.Strings;

/**
 * Base del marshalling de XML<->Java
 */
abstract class SimpleMarshallerBase 
    implements Marshaller {
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS (es inyectado en el constructor de las super-clases)
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Contiene la cache de mapeo utilizada en los procesos de marshalling/unmarshalling
	 * El mapeo se puede obtener de un fichero de definición en XML o de anotaciones en las
	 * propias clases Java
	 * IMPORTANTE!!
	 * 		- Si se van a hacer muchas operaciones de marshalling/unmarshalling, conviene
	 * 		  CACHEAR este objeto, es decir, que este objeto sea UNICO, puesto que se 
	 * 		  reutiliza una y otra vez
	 * 		  Ej: persistencia de objetos en BBDD en formato XML: se está continuamente transformando
	 * 			  objetos a XML... es NECESARIO cachear el mapeo
	 * 
	 * 		- Si únicamente se va a hacer UNA OPERACIÓN de marshalling/unmarshalling sobre
	 * 		  un determinado tipo de objeto, NO tiene sentido mantener una cache de la definición
	 * 		  del mapeo.
	 * 		  Ej: carga de configuración de un XML: se pasa el XML a objetos y lo que se cachean
	 * 			  con los objetos... NO es necesario volver a pasar de XML a objetos
	 */
	MarshallerMappings _mappings;
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
	private Charset _charSet;
	private TextEncoder _textEncoder;
	
	
/////////////////////////////////////////////////////////////////////////////////////////
//  ACCESO A LOS MAPPINGS
/////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public MarshallerMappings getMappings() {
    	return _mappings;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INICIALIZACION A PARTIR DE FICHEROS DE MAPEO EN XML
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
	public Marshaller addTypes(String mapFilesPath) throws MarshallerException {
    	_mappings.loadFromMappingDefFile(mapFilesPath);
    	return this;
    }    
    @Override
	public Marshaller addTypes(InputStream mapsIS) throws MarshallerException {
    	_mappings.loadFromMappingDef(mapsIS);
    	return this;
    }
    @Override
    public String debugInfo() {
    	return _mappings.debugInfo();
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INICIALIZACION A PARTIR DE BEANS ANOTADOS
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public Marshaller addTypes(Class<?>... annotatedTypes) throws MarshallerException {
    	_mappings.loadFromAnnotatedTypes(annotatedTypes);
    	return this;
    }
    @Override
    public Marshaller addTypes(Package... packages) throws MarshallerException {
    	_mappings.loadFromAnnotatedTypesScanningPackages(packages);
    	return this;
    }
    @Override
    public Marshaller addTypes(Object... searchSpecs) throws MarshallerException {
    	_mappings.loadFromAnnotatedTypes(searchSpecs);
    	return this;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE CONFIGURACION
///////////////////////////////////////////////////////////////////////////////////////////    
    @Override
	public Marshaller usingEncoder(TextEncoder encoder) {
    	_textEncoder = encoder;
    	return this;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE CONVERSION
///////////////////////////////////////////////////////////////////////////////////////////    
	@Override
	public <T> T beanFromXml(final String beanXml) throws MarshallerException {
		ObjsFromXMLBuilder<T> objBuilder = new ObjsFromXMLBuilder<T>(_mappings);
		T outBean = objBuilder.beanFrom(beanXml,_charSet,_textEncoder);	
		return outBean;
	}
	@Override
	public <T> T beanFromXml(final CharSequence beanXml) throws MarshallerException {
		return this.<T>beanFromXml(beanXml.toString());
	}
	@Override
	public <T> T beanFromXml(final byte[] beanXmlBytes) throws MarshallerException {
		InputStream beanXmlIS = new ByteArrayInputStream(beanXmlBytes);
		return this.<T>beanFromXml(beanXmlIS);
	}
	@Override
	public <T> T beanFromXml(final InputStream beanXmlIS) throws MarshallerException {
		ObjsFromXMLBuilder<T> objBuilder = new ObjsFromXMLBuilder<T>(_mappings);
		T outBean = objBuilder.beanFrom(beanXmlIS,_charSet,_textEncoder);	
		return outBean;
	}
	@Override
	public <T> T beanFromXml(final Node beanXmlNode) throws MarshallerException {
		ObjsFromXMLBuilder<T> objBuilder = new ObjsFromXMLBuilder<T>(_mappings);
		T outBean = objBuilder.beanFrom(beanXmlNode,_charSet,_textEncoder);	
		return outBean;		
	}
	@Override
	public <T> String xmlFromBean(final T bean) throws MarshallerException {
		XMLFromObjsBuilder xmlBuilder = new XMLFromObjsBuilder(_mappings);
		return xmlBuilder.xmlFrom(bean,_charSet,_textEncoder);
	} 
	@Override
	public <T> String xmlFromBean(final T bean,
								  final Charset charset) {
		_charSet = charset != null ? charset
								   : Charset.defaultCharset();
		String xml = this.xmlFromBean(bean);
		String xmlHeader = Strings.of("<?xml version=\"1.0\" encoding=\"{}\" ?>\n")
								  .customizeWith(_charSet.name())
								  .asString();
		StringBuilder outSb = new StringBuilder(xml.length() + xmlHeader.length() + 1);
		outSb.append(xmlHeader)
			 .append(xml);
		return outSb.toString();
	}
}
