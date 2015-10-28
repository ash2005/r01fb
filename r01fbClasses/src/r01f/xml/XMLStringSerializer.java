/*
 * Created on 20-feb-2005
 * 
 * @author IE00165H
 * (c) 2005 EJIE: Eusko Jaurlaritzako Informatika Elkartea
 */
package r01f.xml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import lombok.NoArgsConstructor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Formatea una cadena XML
 */
@NoArgsConstructor
public class XMLStringSerializer {
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Imprime toda una estructura XML
     * @param doc el documento a escribir como cadena
     * @param outEncoding codificación de la cadena con el xml generado
     *                    (null para utilizar la codificación por defecto)
     * @return una cadena con el xml
     */
    public static String writeDocument(final Document doc,final Charset outEncoding) {
        if (doc == null) return null;
        return XMLStringSerializer.writeNode(doc.getDocumentElement(),outEncoding);
    }
    
    /**
     * Imprime una estructura XML a partir de un nodo
     * @param beginNode el nodo de inicio
     * @param outEncoding codificación de la cadena con el xml generado
     *                    (null para utilizar la codificación por defecto)
     * @return una cadena con el xml
     */
    public static String writeNode(final Node beginNode,final Charset outEncoding) {
        if (beginNode == null) return null;
        try {
            return XMLStringSerializer.writeOuterXML(beginNode,outEncoding);
        } catch (TransformerException tEx) {
            return ("Error al pasar el nodo DOM a String XML: " + tEx.toString() );
        }        
    }    
    /**
     * Serializa el contenido del nodo que se pasa como parametro
     * @param node El nodo a serializar
     * @param outEncoding encoding de la cadena con el xml generado
     *                    (null para utilizar la codificación por defecto)
     * @return Una cadena con el contenido del nodo serializado
     * @throws TransformerException
     */
    public static String writeInnerXML(final Node node,final Charset outEncoding) throws TransformerException {
        StringBuilder innerXml = new StringBuilder();
        if (node.hasChildNodes()) {
            NodeList childNodes = node.getChildNodes();
            int i = childNodes.getLength();
            for (int c = 0; c < i; c++) {
                innerXml.append(XMLStringSerializer.writeOuterXML(childNodes.item(c),outEncoding));
            }
            return innerXml.toString();
        }
        return "";
    }    
    /**
     * Serializa nodo que se pasa como parametro 
     * @param node nodo DOM a serializar
     * @param outEncoding codificacion de la cadena con el XML generado
     *                    (null para utilizar la codificación por defecto)
     * @return una cadena formateada con el dcr
     * @throws TransformerException si se produce un error en el proceso
     */
    public static String writeOuterXML(final Node node,final Charset outEncoding) throws TransformerException {
        if (node == null) return null;
        try {
            TransformerFactory fac = TransformerFactory.newInstance();
            Transformer tf = fac.newTransformer();
            Properties tfProps = new Properties();
            if (outEncoding != null) tfProps.setProperty(OutputKeys.ENCODING,outEncoding.name());
            tfProps.setProperty(OutputKeys.INDENT,"yes");  // indentar
            tf.setOutputProperties(tfProps);
            tf.setOutputProperty("{http://xml.apache.org/xslt}indent-amount","2");  // 2 espacios de tabulacion
            DOMSource src = new DOMSource(node);  // origen de la transformacion
            StringWriter w = new StringWriter();  // destino del xm serializado
            StreamResult rslt = new StreamResult(w);
            tf.transform(src,rslt);
            return w.toString();
        } catch (TransformerFactoryConfigurationError tfCfgEx) {
            throw new TransformerException(tfCfgEx);
        } catch (TransformerConfigurationException tfCfgEx) {
            throw new TransformerException(tfCfgEx);
        } catch (IllegalArgumentException illArgEx) {
            throw new TransformerException(illArgEx);
        }      
    }
    /**
     * Parsea un "churro" xml todo seguido y lo pone tag a tag- 
     * IMPORTANTE!!	Utiliza el encoding por defecto tanto para el String de entrada como para el de salida
     * @param notFormatedXMLStringodificación por defecto)
     * @return una cadena con el xml formateado
     * @throws TransformerException
     */    
    public static String beautifyXMLString(final String notFormatedXMLString) throws TransformerException {
    	return XMLStringSerializer.beautifyXMLString(notFormatedXMLString,Charset.defaultCharset(),Charset.defaultCharset());
    }
    /**
     * Parsea un "churro" xml todo seguido y lo pone tag a tag- 
     * @param notFormatedXMLString
     * @param inputEncoding codificación de la cadena de entrada
     * @param outEncoding codificacion de la cadena de salida
     *                    (null para utilizar la codificación por defecto)
     * @return una cadena con el xml formateado
     * @throws TransformerException
     */    
    public static String beautifyXMLString(final String notFormatedXMLString,final Charset inputEncoding,
    									   final Charset outEncoding) throws TransformerException {
        Document doc = null;
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setIgnoringComments(true);  // Pasar de los comentarios
        dfactory.setNamespaceAware(true);
        try {
            InputSource is = new InputSource( new ByteArrayInputStream(notFormatedXMLString.getBytes(inputEncoding)));
            is.setEncoding(inputEncoding.name());
            doc = dfactory.newDocumentBuilder().parse(is);
        } catch (ParserConfigurationException pcEx) {
            throw new TransformerException("Error en la configuracin del parser XML: " + pcEx.toString());
        } catch (SAXException saxEx) {
            throw new TransformerException("Error en el parseo SAX: " + saxEx.toString());
        } catch (IOException ioEx) {
            throw new TransformerException("Error en el parseo SAX: " + ioEx.toString());
        }
        
        String salida = XMLStringSerializer.writeDocument(doc,outEncoding);
        return salida;
    }
}
