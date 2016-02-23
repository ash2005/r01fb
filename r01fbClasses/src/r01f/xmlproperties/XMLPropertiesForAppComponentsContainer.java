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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Throwables;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.guids.CommonOIDs.Environment;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesReloadControl;
import r01f.resources.ResourcesReloadControlBuilder;
import r01f.resources.ResourcesReloadControlDef;
import r01f.types.Path;
import r01f.xml.XMLDocumentBuilder;


/**
 * Manages an app code's component's properties 
 * This type has a cache of the component's XML documents so the XML file does NOT have to be loaded and parsed 
 * again and again
 * <p>
 * The component's properties can be loaded from many sources as set at the component definition (see {@link XMLPropertiesComponentDef}).
 * </p>
 * <ul>
 * 		<li>A folder in the file system</li>
 * 		<li>A folder in the app classpath</li>
 * 		<li>A database table's row</li>
 * 		<li>...</li>
 * </ul>
 * It also sets how the properties are LOADED AND RELOADED (see {@link r01f.resources.ResourcesLoaderDef})
 * ie:
 * <ul>
 * 		<li>Reload periodically</li>
 * 		<li>Reload when a file is touched (modified)</li>
 * 		<li> etc.</li>
 * </ul>
 * see {@link r01f.resources.ResourcesReloadControlDef}.
 */
@Slf4j
class XMLPropertiesForAppComponentsContainer {
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Environment
	 */
	private Environment _environment;
	/**
	 * App code
	 */
	private AppCode _appCode;
    /**
     * Listener of component loaded envents
     */
    private XMLPropertiesComponentLoadedListener _componentLoadedListener;
///////////////////////////////////////////////////////////////////////////////////////////
//  COMPONENT CACHE
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Cache that stores the component's XML DOM that stores the properties
     */
    private Map<ComponentCacheKey,ComponentCacheXML> _componentsXMLCache;
    
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
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Cache size and debug mode based constructor
     * @param componentLoadedListener listener of component loaded events
     * @param environment environment
     * @param appCode app code
     * @param componentsNumberEstimation number of properties estimation
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
     * Removes the the cached properties forcing it's reloading
     * 	<ul>
     * 		<li>if <code>component != null</code> the given component is reloaded</li>
     * 		<li>if <code>component == null</code> ALL appcode's components are reloaded</li>
     * 	</ul>
     * @param appCode 
     * @param component 
     * @return the number of removed property entries
     */
    int clear(final AppComponent component) {
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
     * Reloads the config if necessary; to do so it checks the last reload time-stamp with the properties source modification time-stamp
     * (this properties source modification time-stamp is handed by the type set at the component definition)
     * ie:
     * <pre class='xml'>
	 *		<?xml version="1.0" encoding="UTF-8"?>
	 *		<componentDef>
	 *			<numberOfPropertiesEstimation>10</numberOfPropertiesEstimation>
	 *			<resourcesLoader type='CLASSPATH'/>	
	 *			<propertiesFileURI>...</propertiesFileURI>	<!-- BEWARE with ClassPathLoader: USE relative paths -->
	 *		</componentDef>
     * </pre>
     * @param component .
     * @return <code>true</code> if the component properties must be reloaded
     */
    boolean reloadIfNecessary(final AppComponent component) {
    	boolean outReload = false;

    	ComponentCacheXML comp = _retrieveComponent(component);
    	if (comp == null) return false;

    	ResourcesReloadControl reloadControlImpl = comp.getReloadControlImpl();
    	if (reloadControlImpl == null) return false;

    	// time between component reload checking
    	long checkInterval = comp.getCompDef().getLoaderDef().getReloadControlDef()
    										  				 .getCheckIntervalMilis();
    	if (checkInterval > 0) {
	    	long timeElapsed = System.currentTimeMillis() - comp.getLoadTimeStamp();
	    	if (timeElapsed > checkInterval) {
		    	outReload = reloadControlImpl.needsReload(component.asString());
		    	if (outReload) {
		    		log.debug("***** RELOAD component {}/{} ******",_appCode,component);
		    		this.clear(component);		// If a reload is needed, delete the component's definition
		    	}
	    	}
    	}
    	return outReload;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  PUBLIC METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the XML's DOM's {@link Node} obtained applying the given xpath expression to the app/component properties XML.
     * @param component
     * @param xPath 
     * @return The DOM {@link Node} or null if the node is NOT found
     */
    Node getPropertyNode(final AppComponent component,final Path xPath) {
    	return (Node)this.getPropertyNode(component,xPath,XPathConstants.NODE);
    }
    NodeList getPropertyNodeList(AppComponent component,Path xPath) {
    	String xPathStr = xPath.asString();
    	String effXPath = !xPathStr.endsWith("/child::*") ? xPathStr.concat("/child::*")
    													  : xPathStr;
    	return (NodeList)this.getPropertyNode(component,Path.from(effXPath),XPathConstants.NODESET);
    }
    /**
     * Returns the XML's DOM's {@link Node} obtained applying the given xpath expression to the app/component properties XML.
     * @param component 
     * @param propXPath 
     * @param returnType the {@link XPath} returned type (boolean, number, string, node o nodeSet).
     * @return The DOM {@link Node} or null if the node is NOT found
     */
    Object getPropertyNode(final AppComponent component,final Path propXPath,
    					   final QName returnType) {
        // [1]- Load the document's DOM 
        ComponentCacheXML comp = _retrieveComponent(component);
		if (comp == null) return null;		// NO se ha podido cargar el componente... devolver null

        // [2]- Exec the XPath expression
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
        return null;    // the property could NOT be loaded
    }
    /**
     * Returns a cached property or it gets it from the underlying properties XML if it was NOT loaded (execs the associated xpath expression)
     * @param component 
     * @return 
     */
    private ComponentCacheXML _retrieveComponent(final AppComponent component) {
    	ComponentCacheXML outComp = null;
    	try {
        	ComponentCacheKey key = new ComponentCacheKey(component);
        	outComp = _componentsXMLCache.get(key);		// Get the component from the cache if present
        	if (outComp == null) {
        		// Load the component definition
        		XMLPropertiesComponentDef compDef = XMLPropertiesComponentDef.load(_environment,
        																		   _appCode,component);
        		if (compDef != null) {
        			log.trace("Loading properties for {}/{} with component definition:{}",
        					 _appCode.asString(),component,compDef.debugInfo().toString());

        			// [0] -- Tell the caché that a new properties component has been loaded
        			//		  (at this point the cache is re-built to accomodate the new estimated property number)
        			_componentLoadedListener.newComponentLoaded(compDef);

        			// [1] -- Load the XML file
        			Document xmlDoc = _loadComponentXML(compDef);

	        		// [2] -- Load the reload control policy
        			ResourcesReloadControl reloadControlImpl = _loadReloadControlImpl(compDef);

	        		// [3] -- Cache
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
     * Loads a properties XML file for appCode/component as stated at the component definition
     * @param component 
     * @param compDef component definition
     * @return the xml {@link Document}
     * @throws XMLPropertiesException if the XML file cannot be loaded or it's malformed
     */
    private Document _loadComponentXML(final XMLPropertiesComponentDef compDef) throws XMLPropertiesException {
    	// [1] Get a resources loader
    	ResourcesLoader resLoader = ResourcesLoaderBuilder.createResourcesLoaderFor(compDef.getLoaderDef());
    	
    	// [2] Load the XML file using the configured resourcesLoader and parse it
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

}
