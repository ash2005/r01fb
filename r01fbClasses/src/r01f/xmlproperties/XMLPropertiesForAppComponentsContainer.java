package r01f.xmlproperties;
/**
 * Gestiona el acceso a los ficheros XML de propiedades.
 */
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.Environment;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesReloadControl;
import r01f.resources.ResourcesReloadControlBuilder;
import r01f.resources.ResourcesReloadControlDef;
import r01f.types.Path;
import r01f.xml.XMLDocumentBuilder;

import com.google.common.base.Throwables;


/**
 * Maneja las properties de UN COMPONENTE de una aplicación, manteniendo una caché de los Documentos XML de cada componente
 * de esa aplicación (recordar que una aplicación puede tener varios ficheros XML de propiedades -componentes-)
 * leídos desde su almacenamiento (ver {@link XMLPropertiesCache} para saber cómo funcionan las cachés).
 * <p>
 * Las propiedades de un componente se pueden cargar desde varias fuentes que se especifican en la
 * definición del componente (ver {@link XMLPropertiesComponentDef}).
 * </p>
 * <ul>
 * 		<li> Ruta física de un fichero en el sistema de ficheros.</li>
 * 		<li> Ruta en el classPath de la aplicación.</li>
 * 		<li> Ruta física de un fichero en el Gestor de Contenidos.</li>
 * 		<li> Base de Datos.</li>
 * </ul>
 * También se indica cómo se CARGAN (y RE-CARGAN) las propiedades (ver {@link r01f.resources.ResourcesLoaderDef})
 * por ejemplo:
 * <ul>
 * 		<li> Recargar cada cierto tiempo.</li>
 * 		<li> Recargar cuando se modifique un fichero.</li>
 * 		<li> etc.</li>
 * </ul>
 * Ver {@link r01f.resources.ResourcesReloadControlDef}.
 */
@Slf4j
class XMLPropertiesForAppComponentsContainer {
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Entorno
	 */
	private Environment _environment;
	/**
	 * Código de aplicación para los XMLs de componentes almacenados en esta caché.
	 */
	private AppCode _appCode;
    /**
     * Mapa donde se guarda un DOM con el XML de cada componente de propiedades.
     */
    private Map<ComponentCacheKey,ComponentCacheXML> _componentsXMLCache;
    /**
     * Listener de los eventos que indican que se ha cargado un componente.
     */
    private XMLPropertiesComponentLoadedListener _componentLoadedListener;
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor en base al tamaño de la caché y el modo de depuración.
     * @param componentLoadedListener Listener de los eventos que indican que se ha cargado un componente.
     * @param environment entorno
     * @param appCode Código de aplicación.
     * @param componentsNumberEstimation Tamaño de la caché de componentes.
     */
    XMLPropertiesForAppComponentsContainer(final XMLPropertiesComponentLoadedListener componentLoadedListener,
    								   	   final Environment environment,
    								   	   final AppCode appCode,
    								   	   final int componentsNumberEstimation) {
    	_environment = environment;
    	_appCode = appCode;
    	_componentLoadedListener = componentLoadedListener;
    	_componentsXMLCache = new HashMap<ComponentCacheKey,ComponentCacheXML>(componentsNumberEstimation,0.5F);
    }
    /**
     * Elimina las propiedades de la caché forzando su recarga.
     * 		<ul>
     * 		<li>Si <code>component != null</code> se recarga el componente señalado.</li>
     * 		<li>Si <code>component == null</code> se recargan TODOS los componentes de la aplicación.</li>
     * 		</ul>
     * @param appCode Código de aplicación.
     * @param component Componente.
     * @return El número de entradas eliminadas.
     */
    int clear(AppComponent component) {
    	log.trace("Clearing XML documents cache for {}/{}",_appCode,component);
    	int numMatches = 0;
        if (component == null) {
        	numMatches = _componentsXMLCache.size();
        	_componentsXMLCache.clear();
        } else {
        	List<ComponentCacheKey> keysToRemove = new ArrayList<ComponentCacheKey>();
        	for (ComponentCacheKey key : _componentsXMLCache.keySet()) {
        		if (key.isSameAs(component)) {
        			keysToRemove.add(key);
        			numMatches++;
        		}
        	}
        	if (!keysToRemove.isEmpty()) {
        		for (ComponentCacheKey key : keysToRemove) {
        			ComponentCacheXML removedComp = _componentsXMLCache.remove(key);	// Eliminar la clave del cache de DOMs por componente
        			if (removedComp != null) numMatches++;
        		}
        	}
        }
        return numMatches;
    }
    /**
     * Recarga la configuración si es necesario, para lo que compara el timeStamp
     * de la última recarga y el timeStamp de modificación del properties que es
     * proporcionado por la clase especificada en la configuración del componente.
     * @param component Componente.
     * @return <code>true</code> si es necesario recargar la configuración.
     */
    boolean reloadIfNecessary(final AppComponent component) {
    	boolean outReload = false;

    	ComponentCacheXML comp = _retrieveComponent(component);
    	if (comp == null) return false;

    	ResourcesReloadControl reloadControlImpl = comp.getReloadControlImpl();
    	if (reloadControlImpl == null) return false;

    	// Tiempo entre comprobaciones para ver si hay que recargar el recurso.
    	long checkInterval = comp.getCompDef().getLoaderDef().getReloadControlDef()
    										  				 .getCheckIntervalMilis();
    	if (checkInterval > 0) {
	    	long timeElapsed = System.currentTimeMillis() - comp.getLoadTimeStamp();
	    	if (timeElapsed > checkInterval) {
		    	outReload = reloadControlImpl.needsReload(component.asString());
		    	if (outReload) {
		    		log.debug("***** RELOAD component {}/{} ******",_appCode,component);
		    		this.clear(component);		// Si hay que recargar, borrar la definición del componente de la aplicación
		    	}
	    	}
    	}
    	return outReload;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PUBLICOS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene el Node del DOM que corresponde con el xpath aplicado sobre el XML de la app/componente que se pasan.
     * @param component Componente de la aplicación.
     * @param xPath La ruta XPath.
     * @return El nodo dentro del arbol DOM o <code>null</code> si no se encuentra.
     */
    Node getPropertyNode(final AppComponent component,final Path xPath) {
    	return (Node)this.getPropertyNode(component,xPath,XPathConstants.NODE);
    }
    NodeList getPropertyNodeList(AppComponent component,Path xPath) {
    	String xPathStr = xPath.asString();
    	String effXPath = !xPathStr.endsWith("/child::*") ? xPathStr.concat("/child::*")
    													  : xPathStr;
    	return (NodeList)this.getPropertyNode(component,Path.of(effXPath),XPathConstants.NODESET);
    }
    /**
     * Obtiene el Node del DOM que corresponde con el xpath aplicado sobre el XML de la app/componente que se pasan.
     * @param component Componente de la aplicación.
     * @param propXPath La ruta XPath.
     * @param returnType El tipo de objeto devuelto por XPath (boolean, number, string, node o nodeSet).
     * @return El nodo dentro del arbol DOM o <code>null</code> si no se encuentra.
     */
    Object getPropertyNode(final AppComponent component,final Path propXPath,
    					   final QName returnType) {
        // [1]- Cargar el DOM del documento que contiene el nodo
        ComponentCacheXML comp = _retrieveComponent(component);
		if (comp == null) return null;		// NO se ha podido cargar el componente... devolver null

        // [2]- Hacer la busqueda xPath del nodo
		String thePropXPath = null;
        try {
            Object outObj = null;
            thePropXPath = propXPath.asString().trim();
            if (thePropXPath.startsWith("/")) thePropXPath = thePropXPath.substring(1);

            XPathFactory xPathFactory = XPathFactory.newInstance();
            XPath xPath = xPathFactory.newXPath();
            XPathExpression xPathExpr = xPath.compile(thePropXPath);
            if (returnType == XPathConstants.BOOLEAN) {
            	outObj = xPathExpr.evaluate(comp.getXml(),XPathConstants.BOOLEAN);
            } else if (returnType == XPathConstants.NUMBER) {
            	outObj = xPathExpr.evaluate(comp.getXml(),XPathConstants.NUMBER);
            } else if (returnType == XPathConstants.STRING) {
            	outObj = xPathExpr.evaluate(comp.getXml(),XPathConstants.STRING);
            } else if (returnType == XPathConstants.NODE) {
            	outObj = xPathExpr.evaluate(comp.getXml(),XPathConstants.NODE);
            } else if (returnType == XPathConstants.NODESET) {
            	outObj = xPathExpr.evaluate(comp.getXml(),XPathConstants.NODESET);
            }
            return outObj;
        } catch (XPathExpressionException xPathEx) {
        	log.warn("Error retrieving property at {} for {}/{}",
        			 thePropXPath,_appCode.asString(),component);
        	xPathEx.printStackTrace(System.out);
        }
        return null;    // No se ha podido cargar la propiedad
    }
    /**
     * Obtiene un componente de la cache o bien lo carga si es que NO estaba cargado.
     * @param component Componente.
     * @return El componente.
     */
    private ComponentCacheXML _retrieveComponent(final AppComponent component) {
    	ComponentCacheXML outComp = null;
    	try {
        	ComponentCacheKey key = new ComponentCacheKey(component);
        	outComp = _componentsXMLCache.get(key);		// Obtener el componente de la cache... si está presente
        	if (outComp == null) {
        		// Cargar la definición del componente
        		XMLPropertiesComponentDef compDef = XMLPropertiesComponentDef.load(_environment,_appCode,component);
        		if (compDef != null) {
        			log.trace("Loading properties for {}/{} with component definition:{}",
        					 _appCode.asString(),component,compDef.debugInfo().toString());

        			// [0] -- Informar a la caché de que se cargan nuevas propiedades
        			//		  (en este punto la caché se re-dimensiona para acomodar el número de propiedades estimadas para el componente)
        			_componentLoadedListener.newComponentLoaded(compDef);

        			// [1] -- Cargar el XML de Propiedades
        			Document xmlDoc = _loadComponentXML(compDef);

	        		// [2] -- Cargar la implementación de la política de control de recarga
        			ResourcesReloadControl reloadControlImpl = _loadReloadControlImpl(compDef);

	        		// [3] -- Cachear
	        		outComp = new ComponentCacheXML(compDef,System.currentTimeMillis(),reloadControlImpl,
	        									    xmlDoc);
	        		_componentsXMLCache.put(key,outComp);
        		}
        	}
        } catch (XMLPropertiesException xmlPropsEx) {
        	xmlPropsEx.printStackTrace(System.out);
        }
    	return outComp;
    }
    /**
     * Carga el XML de propiedades de appCode/component según lo indicado en la definición del componente.
     * @param component Componente.
     * @param compDef Definición del componente.
     * @return El XML.
     * @throws XMLPropertiesException Si no se puede cargar el XML o este está mal formado.
     */
    private Document _loadComponentXML(final XMLPropertiesComponentDef compDef) throws XMLPropertiesException {
    	// [1] Obtener un ResourcesLoader
    	ResourcesLoader resLoader = ResourcesLoaderBuilder.createResourcesLoaderFor(compDef.getLoaderDef());
    	
    	// [2] Cargar el XML utilizando el resourcesLoader
		XMLDocumentBuilder domBuilder = new XMLDocumentBuilder(resLoader);
		Document xmlDoc = null;
		try {
			xmlDoc = domBuilder.buildXMLDOM(compDef.getPropertiesFileURI());
		} catch(SAXException saxEx) {
			throw Throwables.getRootCause(saxEx) instanceof FileNotFoundException ? XMLPropertiesException.propertiesLoadError(_environment,_appCode,compDef.getName())
																				  : XMLPropertiesException.propertiesXMLError(_environment,_appCode,compDef.getName());
		}
		return xmlDoc;
    }
    private static ResourcesReloadControl _loadReloadControlImpl(final XMLPropertiesComponentDef compDef) {
		ResourcesReloadControlDef reloadControlDef = compDef.getLoaderDef()
															.getReloadControlDef();
		if (reloadControlDef == null) return null;

		ResourcesReloadControl outReloadControl = ResourcesReloadControlBuilder.createFor(reloadControlDef);
		return outReloadControl;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CLAVE PARA EL MAPA DE DOCUMENTOS DOM CON LAS PROPIEDADES
///////////////////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
  	@EqualsAndHashCode @ToString
  	@AllArgsConstructor
  	private class ComponentCacheKey {
  		@Getter private final AppComponent _component;

		boolean isSameAs(final AppComponent... keyComponent) {
			boolean isSame = false;
			if (keyComponent.length == 1) {
				isSame = this.composeKey(_component).equals(this.composeKey(keyComponent[0]));
			}
			return isSame;
		}
		AppComponent composeKey(AppComponent... keyComponent) {
			AppComponent outKey = null;
			if (keyComponent.length == 1) {
				outKey = keyComponent[0];
			}
			return outKey;
		}
  	}
  	@Accessors(prefix="_")
  	@AllArgsConstructor
  	private class ComponentCacheXML {
  		@Getter private XMLPropertiesComponentDef _compDef;			// definición del componente
  		@Getter private long _loadTimeStamp;						// timeStamp del momento de la carga
  		@Getter private ResourcesReloadControl _reloadControlImpl;	// Implementación del control de recarga de propiedades
  		@Getter private Document _xml;								// documento XML
  	}
}
