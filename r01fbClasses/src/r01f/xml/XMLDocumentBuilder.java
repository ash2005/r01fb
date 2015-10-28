package r01f.xml;

import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import r01f.resources.ResourcesLoader;
import r01f.types.Path;

/**
 * Clase de utilidad para el parseo de XMLs
 */
public class XMLDocumentBuilder {
///////////////////////////////////////////////////////////////////////////////
// 	MIEMBROS
///////////////////////////////////////////////////////////////////////////////
	private ResourcesLoader _resourcesLoader;	// carga los ficheros
///////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	public XMLDocumentBuilder(ResourcesLoader resLoader) {
		_resourcesLoader = resLoader;
	}
///////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////
    /**
     * @see XMLDocumentBuilder#buildXMLDOM(String, String...)
     */
	public Document buildXMLDOM(final Path xmlFilePath,final String... ignoredEntities) throws SAXException {
		return this.buildXMLDOM(xmlFilePath.asString(),ignoredEntities);
	}
    /**
     * Obtiene un DOM a partir de un XML
     * @param xmlFilePath ruta al fichero con el XML
     * @param ignoredEntities entidades EXTERNAS cuya resolución hay que ignorar
     *          - Entidades internas: <!ENTITY entityname "replacement text">
     *          - Entidades externas: <!ENTITY entityname [PUBLIC "public-identifier"] SYSTEM "system-identifier"> 
     *        (por ejemplo para evitar la validacion de dtds se ignora la entidad dcr4.5.dtd 
     *         especificada en <!DOCTYPE record SYSTEM "dcr4.5.dtd">)
     *        Las entidades ignoradas se indican en un array publicId:systemId
     * @return el objeto DOM Documento
     * @throws SAXException si se produce algún error en el proceso
     */
    public Document buildXMLDOM(final String xmlFilePath,
    							final String... ignoredEntities) throws SAXException {
    	Document outDoc = null;
    	try {
			InputStream xmlIS = _resourcesLoader.getInputStream(xmlFilePath,true);	// true indica que NO hay que utilizar cachés (si existen)
	    	if (xmlIS != null) {
	    		outDoc = XMLDocumentBuilder.buildXMLDOM(xmlIS,ignoredEntities);
	    	} else {
	    		throw new FileNotFoundException("NO se ha podido cargar el xml desde " + xmlFilePath + ": el recurso NO existe");
	    	}
	    	xmlIS.close();	// do not forget!
    	} catch(IOException ioEx) {
    		throw new SAXException(ioEx);
    	}
    	return outDoc;
    }	
    /**
     * Obtiene un DOM a partir de un XML
     * @param xmlIs InputStream al XML
     * @param ignoredEntities entidades EXTERNAS cuya resolucion hay que ignorar
     *          - Entidades internas: <!ENTITY entityname "replacement text">
     *          - Entidades externas: <!ENTITY entityname [PUBLIC "public-identifier"] SYSTEM "system-identifier"> 
     *        (por ejemplo para evitar la validacion de dtds se ignora la entidad dcr4.5.dtd 
     *         especificada en <!DOCTYPE record SYSTEM "dcr4.5.dtd">)
     *        Las entidades ignoradas se indican en un array publicId:systemId
     * @return el objeto DOM Document
     * @throws SAXException si se produce algún error en el proceso
     */
    public static Document buildXMLDOM(final InputStream xmlIs,final String... ignoredEntities) throws SAXException {
        // Instanciar una factoria del parser (builder)
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);      // Pasar de los comentarios
        factory.setValidating(false);           // NO Validar los documentos
        factory.setIgnoringElementContentWhitespace(true);
        factory.setNamespaceAware(true);            
        DocumentBuilder builder;
        try {
            builder = factory.newDocumentBuilder(); 
            builder.setEntityResolver(
                    // Instancia de una clase EntityResolver se se utiliza para ignorar
                    // entidades externas (pe el DTD del DCR, para que no se valide el DTD)
                    new EntityResolver() {
                        @Override
						public InputSource resolveEntity(String publicId, String systemId) {
                            String key = publicId != null ? publicId + "." + systemId : systemId;                            
                            if  (_isIgnoredExternalEntity(key)) {
                                return new InputSource(new ByteArrayInputStream("".getBytes()));
                            } 
                            return null;
                        }
                        private boolean _isIgnoredExternalEntity(String key) {
                            if (ignoredEntities != null) {
                                for (int i=0; i<ignoredEntities.length; i++) {
                                    if (ignoredEntities[i].equals(key)) return true;
                                }
                            }
                            return false;
                        }
                    }
            ); 
            return builder.parse(xmlIs);    // parsear el xml y devolver el documento xml (DOM)
        } catch (ParserConfigurationException pcEx) {
            throw new SAXException(pcEx);
        } catch (IOException ioEx) {
            throw new SAXException(ioEx);
        }        
    }
}
