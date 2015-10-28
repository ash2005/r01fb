package r01f.xmlproperties;
/**

 * @author  Alex Lara
 * @version
 */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import r01f.generics.TypeRef;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.internal.BuiltInObjectsMarshaller;
import r01f.marshalling.Marshaller;
import r01f.resources.ResourcesLoaderDef;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Maneja las properties para un código de aplicación.<br/>
 * Simplemente facilita el acceso a las propiedades de los diferentes componentes 
 * de las propiedades de una aplicación
 * 
 * Esta clase puede ser utilizada para acceder a las propiedades de una aplicación,
 * aunque NO se debe utilizar directamente sino a través de {@link XMLProperties}
 * (ver la {@link XMLProperties} para saber cómo se utiliza el sistema)
 */
@Accessors(prefix="_")
@Slf4j
public final class XMLPropertiesForApp {
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
    XMLPropertiesForAppCache _cache;  // Cache de properties (es construido por la factoría de XMLPropertiesCache que
    								  //						se pasa como parámetro en el constructor)
    @Getter private AppCode _appCode;		  // Codigo de aplicación
    
	@SuppressWarnings("unused")
	private boolean _useCache = true;

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor
     * @param cache
     * @param appCode
	 * @param componentsNumberEstimation  
	 */
    public XMLPropertiesForApp(final XMLPropertiesForAppCache cache,
    						   final AppCode appCode,
    						   final int componentsNumberEstimation) {
    	_appCode = appCode;
    	_cache = cache;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS DE RECARGA DE CACHES Y DEBUG
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Devuelve la información de uso de la cache en formato imprimible.
     * @param stats La informacion de uso de la cache.
     * @return Un String con información de depuración.
     */
    public String cacheStatsDebugInfo() {
    	return _cache.usageStats().debugInfo();
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT API
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Encapsula los métodos que devuelven las propiedades.
     * @param component Componente de la aplicacion.
     * @return Un componente que encapsula el acceso a las propiedades.
     */
    public ComponentProperties of(final AppComponent component) {
    	return new ComponentProperties(_appCode,component);
    }
    /**
     * Encapsula los métodos que devuelven las propiedades.
     * @param component Componente de la aplicacion.
     * @return Un componente que encapsula el acceso a las propiedades.
     */
    public ComponentProperties of(final String component) {
    	return new ComponentProperties(_appCode,AppComponent.forId(component));
    }
    /**
     * Encapsulates the component properties
     * @param component the component
     * @return the {@link XMLPropertiesForAppComponent}
     */
    public XMLPropertiesForAppComponent forComponent(final AppComponent component) {
    	return new XMLPropertiesForAppComponent(this,component);
    }
    /**
     * Encapsulates the component properties
     * @param component the component
     * @return the {@link XMLPropertiesForAppComponent}
     */
    public XMLPropertiesForAppComponent forComponent(final String component) {
    	return new XMLPropertiesForAppComponent(this,AppComponent.forId(component));
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ PUBLICA DE UN COMPONENTE
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Encapsula el acceso a las propiedades de un componente.
     */
    public class ComponentProperties {
    	private AppCode _theAppCode;
    	private AppComponent _component;

    	/**
    	 * Constructor en base al código de aplicación y componente.
    	 * @param component Componente de la aplicación.
    	 */
    	ComponentProperties(final AppCode appCode,final AppComponent component) {
    		_theAppCode = appCode;
    		_component = component;
    	}
    	/**
    	 * @return el nodo xml que conteniene la proiedade
    	 */
    	public Node node(final String propXPath) {
    		return _cache.getPropertyNode(_component,Path.of(propXPath));
    	}
    	/**
    	 * @return la lista de nodos que verifican el xPath
    	 */
    	public NodeList nodeList(final String propXPath) {
    		return _cache.getPropertyNodeList(_component,Path.of(propXPath));
    	}
		/**
		 * Transform the child nodes into a collection of objects
		 * @param propXPath
		 * @param transformFunction
		 * @return
		 */
		public <T> Collection<T> getObjectList(final String propXPath,
											   final Function<Node,T> transformFunction) {
			Collection<T> outObjs = null;
			NodeList nodes = this.nodeList(propXPath);
			if (nodes != null && nodes.getLength() > 0) {
				outObjs = Lists.newArrayListWithExpectedSize(nodes.getLength());
				for (int i=0; i < nodes.getLength(); i++) {
					outObjs.add(transformFunction.apply(nodes.item(i)));	// transform the node
				}
			}
			return outObjs;
		}
	    /**
	     * Comprueba si una propiedad está definida en el fichero de propiedades.
	     * @param propXPath Ruta xpth de la propiedad.
	     * @return <code>true</code> si la propiedad está definida en el fichero, <code>false</code> en otro caso.
	     */
	    public boolean existProperty(final String propXPath) {
	    	boolean outExists = _cache.existProperty(_component,Path.of(propXPath));
	    	return outExists;
	    }
	    /**
	     * Devuelve una propiedad intentando evaluar su tipo
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un string con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public <T> T get(final String propXPath) {
	    	return this.<T>get(propXPath,null);
	    }
	    /**
	     * Devuelve una propiedad intentando evaluar su tipo
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue el valor por defecto de la propiedad
	     * @return Un string con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public <T> T get(final String propXPath,final T defaultValue) {
	    	return _cache.getProperty(_component,Path.of(propXPath),
	    							  defaultValue,
	    							  new TypeRef<T>() {/* empty */});
	    }
	    /**
	     * Devuelve una propiedad como un String.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un string con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public String getString(final String propXPath) {
	    	return this.getString(propXPath,null);
	    }
	    /**
	     * Devuelve una propiedad como String o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     * @param defaultValue Valor por defecto para la propiedad.
	     * @return El valor de la propiedad o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     */
	    public String getString(final String propXPath,final String defaultValue) {
	        String outStr = _cache.getProperty(_component,Path.of(propXPath),
	        								   defaultValue,
	        								   String.class);
	        return outStr;
	    }
	    /**
	     * Devuelve la propiedad en forma de cadena envuelta en forma de fluent API para realizar operaciones
	     * posteriores con el valor de la propiedad.<br>
	     * (ver http://download.oracle.com/javase/1.5.0/docs/api/index.html?java/util/Formatter.html)
	     * <pre class="brush:java">
	     * 		Calendar cal = new GregorianCalendar(1995, MAY, 23)
	     * 		String theProp = props.of("xxx","comp").getStringWrapped("xPath").format(cal).asString();
	     * </pre>
	     * 		Si la propiedad es por ejemplo "Duke's Birthday: %1$tm %1$te,%1$tY",<br>
	     * 		el valor de theProp tras la llamada anterior sera: Duke's Birthday: May 23, 1995
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return La propiedad en forma de cadena.
	     */
	    public StringExtended getStringWrapped(final String propXPath) {
	    	return Strings.of(this.getString(propXPath));
	    }
	    /**
	     * Devuelve la propiedad en forma de cadena envuelta en forma de fluent API para realizar operaciones
	     * posteriores con el valor de la propiedad, si no está definida el valor especificado por defecto.<br>
	     * (ver http://download.oracle.com/javase/1.5.0/docs/api/index.html?java/util/Formatter.html)
	     * <pre class="brush:java">
	     * 		Calendar cal = new GregorianCalendar(1995, MAY, 23)
	     * 		String theProp = props.of("xxx","comp").getStringWrapped("xPath").format(cal).asString();
	     * </pre>
	     * 		Si la propiedad es por ejemplo "Duke's Birthday: %1$tm %1$te,%1$tY",<br>
	     * 		el valor de theProp tras la llamada anterior sera: Duke's Birthday: May 23, 1995
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue Valor por defecto en caso de no existir.
	     * @return La propiedad en forma de cadena.
	     */
	    public StringExtended getStringWrapped(final String propXPath,final String defaultValue) {
	    	return Strings.of(this.getString(propXPath,defaultValue));
	    }
	    /**
	     * Devuelve una propiedad como un Number.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un string con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public Number getNumber(final String propXPath) {
	    	return this.getNumber(propXPath,null);
	    }
	    /**
	     * Devuelve una propiedad como un Number o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue Valor por defecto para la propiedad.
	     * @return El valor de la propiedad o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     */
	    public Number getNumber(final String propXPath,final Number defaultValue) {
	    	Number outNum = _cache.getProperty(_component,Path.of(propXPath),
	    									   defaultValue,
	    									   Number.class);
	        return outNum;
	    }
	    /**
	     * Devuelve una propiedad como un int.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un string con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public int getInteger(final String propXPath) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.intValue() : Integer.MIN_VALUE;
	    }
	    /**
	     * Devuelve una propiedad como un int o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue Valor por defecto para la propiedad.
	     * @return El valor de la propiedad o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     */
	    public int getInteger(final String propXPath,final int defaultValue) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.intValue() : defaultValue;
	    }
	    /**
	     * Devuelve una propiedad como un long.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un string con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public long getLong(final String propXPath) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.longValue() : Long.MIN_VALUE;
	    }
	    /**
	     * Devuelve una propiedad como un long o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue Valor por defecto para la propiedad.
	     * @return El valor de la propiedad o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     */
	    public long getLong(final String propXPath,final long defaultValue) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.longValue() : defaultValue;
	    }
	    /**
	     * Devuelve una propiedad como un double.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un double con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public double getDouble(final String propXPath) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.doubleValue() : Double.MIN_VALUE;
	    }
	    /**
	     * Devuelve una propiedad como un double o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue Valor por defecto para la propiedad.
	     * @return El valor de la propiedad o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     */
	    public double getDouble(final String propXPath,final double defaultValue) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.doubleValue() : defaultValue;
	    }
	    /**
	     * Devuelve una propiedad como un float.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un string con la propiedad o <code>null</code> si la propiedad no existe.
	     */
	    public float getFloat(final String propXPath) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.floatValue() : Float.MIN_VALUE;
	    }
	    /**
	     * Devuelve una propiedad como un float o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue Valor por defecto para la propiedad.
	     * @return El valor de la propiedad o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     */
	    public float getFloat(final String propXPath,final float defaultValue) {
	    	Number num = this.getNumber(propXPath);
	    	return num != null ? num.floatValue() : defaultValue;
	    }
	    /**
	     * Devuelve una propiedad como un Boolean.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @return Un string con la propiedad o <code>false</code> si la propiedad no existe.
	     */
	    public boolean getBoolean(final String propXPath) {
	    	return this.getBoolean(propXPath,false);
	    }
	    /**
	     * Devuelve una propiedad como un boolean o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     * @param propXPath La ruta XPath de la propiedad.
	     * @param defaultValue Valor por defecto para la propiedad.
	     * @return El valor de la propiedad o el valor por defecto que se pasa si la propiedad es <code>null</code>.
	     */
	    public boolean getBoolean(final String propXPath,final boolean defaultValue) {
	    	Boolean bool = _cache.getProperty(_component,Path.of(propXPath),
	    									  defaultValue,
	    									  Boolean.class);
	        return bool != null ? bool : false;
	    }
	    /**
	     * Devuelve una propiedad como un Properties de los valores que están por debajo del tag que se
	     * pasa en la sentencia xPath.<br>
	     * Ej: si el xPath llega a
	     * {@code
	     * 	<myProperties>
	     * 		<itemName1>value1</itemName1>
	     * 		<itemName2>value2</itemName2>
	     * 			...
	     * 		</myProperties>
	     * }
	     * <br>devuelve un objeto properties:{[itemName1,value1],[itemName2,value2]...}.
	     * @param propXPath Ruta XPath al nodo padre de los elementos a poner el objeto Properties.
	     * @return Un objeto Properties.
	     */
	    @SuppressWarnings("unchecked")
	    public Properties getProperties(final String propXPath) {
	    	Map<String,List<String>> map = _cache.getProperty(_component,Path.of(propXPath),
	    													  null,
	    													  Map.class);
	    	// Pasar a un properties
	    	Properties outProps = null;
	    	if (map != null) {
	    		outProps = new Properties();
		    	for (Map.Entry<String,List<String>> me : map.entrySet()) {
		    		if (me.getValue() != null && me.getValue().size() == 1) {
		    			outProps.put(me.getKey(),me.getValue().get(0));
		    		} else {
		    			StringBuilder sb = new StringBuilder();
		    			for (Iterator<String> it=me.getValue().iterator(); it.hasNext(); ) {
		    				sb.append('[');
		    				sb.append(it.next());
		    				sb.append(']');
		    				if (it.hasNext()) {
		    					sb.append(',');
		    				}
		    			}
		    			outProps.put(me.getKey(),sb.toString());
		    		}
		    	}
	    	}
	    	return outProps;
	    }
	    /**
	     * Devuelve una propiedad como un Properties de los valores que están por debajo del tag que se
	     * pasa en la sentencia xPath.<br>
	     * Ej: si el xPath llega a
	     * {@code
	     * 	<myProperties>
	     * 		<itemName1>value1</itemName1>
	     * 		<itemName2>value2</itemName2>
	     * 			...
	     * 	</myProperties>
	     * }
	     * <br>devuelve un objeto properties:{[itemName1,value1],[itemName2,value2]...}.
	     * @param propXPath Ruta XPath al nodo padre de los elementos a poner el objeto Properties.
	     * @param defaultValue Properties por defecto
	     * @return Un objeto Properties.
	     */
	    public Properties getProperties(final String propXPath,final Properties defaultValue) {
	    	Properties outProps = this.getProperties(propXPath);
	    	if (outProps == null) outProps = defaultValue;
	    	return outProps;
	    }
	    /**
	     * Devuelve una propiedad como una lista de Strings con los valores que están por debajo del tag que se
	     * pasa en la sentencia xPath.<br>
	     * Ej: si el xPath llega a
	     * {@code
	     * <myList>
	     * 	<item>value1</item>
	     * 	<item>value2</item>
	     * 		...
	     * 	</myList>
	     * }
	     * <br>devuelve un objeto List{value1,value2...}.
	     * @param propXPath Ruta XPath al nodo padre de los elementos a poner el objeto List.
	     * @return Un objeto List<String>.
	     */
	    @SuppressWarnings("unchecked")
	    public List<String> getListOfStrings(final String propXPath) {
	    	String effXPath = (propXPath != null && !propXPath.endsWith("/child::*")) ? propXPath.concat("/child::*")
	    																			  : propXPath;
	    	Map<String,List<String>> map = _cache.getProperty(_component,Path.of(effXPath),
	    													  null,
	    													  Map.class);
	    	// Pasar a un properties
	    	List<String> outList = null;
	    	if (map != null) {
	    		outList = new ArrayList<String>(map.size());
		    	for (Map.Entry<String,List<String>> me : map.entrySet()) {
		    		if (me.getValue() != null && me.getValue().size() == 1) {
		    			outList.add(me.getValue().get(0));
		    		} else {
		    			for (Iterator<String> it=me.getValue().iterator(); it.hasNext(); ) {
		    				outList.add(it.next());
		    			}
		    		}
		    	}
	    	}
	    	return outList;
	    }
	    /**
	     * Devuelve una propiedad como una lista de Strings con los valores que están por debajo del tag que se
	     * pasa en la sentencia xPath.<br>
	     * Ej: si el xPath llega a
	     * {@code
	     * <myList>
	     * 	<item>value1</item>
	     * 	<item>value2</item>
	     * 		...
	     * </myList>
	     * }
	     * <br>devuelve un objeto List{value1,value2...}.
	     * @param propXPath Ruta XPath al nodo padre de los elementos a poner el objeto List.
	     * @param defaultValue Lista de Strings por defecto.
	     * @return Un objeto List<String>.
	     */
	    public List<String> getListOfStrings(final String propXPath,final List<String> defaultValue) {
	    	List<String> outList = this.getListOfStrings(propXPath);
	    	if (outList == null) outList = defaultValue;
	    	return outList;
	    }
	    /**
	     * Devuelve un objeto a partir de una porción del XML de propiedades.<br>
	     * La transformación de XML a objetos se hace utilizando el Marshaller de R01.
	     * @param propXPath La ruta al tag que engloba el objeto.
	     * @param objType El tipo de objeto.
	     * @return El objeto obtenido del XML.
	     */
	    public <T> T getObject(final String propXPath,Class<T> objType,final Marshaller marshaller) {
	    	T outObj = _cache.getProperty(_component,Path.of(propXPath),
	    								  null,
	    								  objType,marshaller);
	    	if (outObj == null) {
	    		log.warn("The property {} for appCode/componente={}/{} can not be converted to object {}",
	    				 propXPath,_theAppCode.asString(),_component.asString(),objType.getName());
	    	}
	    	return outObj;
	    }
		/**
		 * Returns the property as an object transforming the node to an object
		 * using a {@link Function}
		 * @param propXPath 
		 * @param transformFuncion
		 * @return
		 */
		public <T> T getObject(final String propXPath,
							   final Function<Node,T> transformFuncion) {
			T outObj = null;
			Node node = _cache.getPropertyNode(_component,Path.of(propXPath));
			if (node != null) outObj = transformFuncion.apply(node);
			return outObj;
		}
	    /**
	     * Devuelve la propiedad como un objeto de definición de carga de recursos {@link ResourcesLoaderDef}
	     * (obviamente el XML tiene que tener la estrucutra impuesta por {@link ResourcesLoaderDef}).
	     * @return Un objeto {@link ResourcesLoaderDef}.
	     * @see ResourcesLoaderDef
	     */
	    public ResourcesLoaderDef getResourcesLoaderDef(final String propXPath) {
	    	ResourcesLoaderDef outResDef = this.getObject(propXPath,ResourcesLoaderDef.class,
	    												  BuiltInObjectsMarshaller.instance());		// the marshaller "built-in" object marshaller is provided

	    	return outResDef;
	    }
    }
}
