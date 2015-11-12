package r01f.xmlproperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.generics.TypeRef;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.guids.CommonOIDs.Environment;
import r01f.marshalling.Marshaller;
import r01f.marshalling.MarshallerException;
import r01f.types.Path;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;
import r01f.xml.XMLStringSerializer;
import r01f.xml.XMLUtils;

/**
 * Caché de propiedades para un código de aplicación.<br/>
 * Hay DOS cachés:
 * 	<ul>
 * 	<li>Caché de valores de <b>las propiedades</b>:<p>Se va completando a medida que se accede a las propiedades,
 * 										     		  es decir, cuando se recupera una propiedad del XML se almacena
 * 										     		  su valor en esta caché y NO se vuelve a obtener del XML.
 * 										     		  Esta caché está gestionada en la clase {@link XMLPropertiesCache}.
 * 										     		  Esta caché se utiliza en la clase {@link XMLPropertiesManager}.</li>
 *
 * 	<li>Caché de XMLs de <b>los componentes</b>:<p>Cuando se accede por primera vez a una propiedad cualquiera de un appCode/contentType
 * 										   		   se carga el XML de propiedades desde su almacenamiento, en este momento el Document XML
 * 										   		   se cachea en la clase {@link XMLPropertiesForComponentContainer}, de forma que NO es necesario
 * 										   		   volver a leer el XML.
 * 										   		   A medida que se van leyendo propiedades del XML, sus valores se almacenan en la
 * 										   		   clase {@link XMLPropertiesCache}.
 * 										   		   Esta caché está en la clase {@link XMLPropertiesForComponentContainer} y se
 * 										   		   instancia en ESTA clase utilizando la factoría.</li>
 * </ul>
 */
@Slf4j
     class XMLPropertiesForAppCache 
implements XMLPropertiesComponentLoadedListener {
/////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ DE LA FACTORIA DE XMLPropertiesForAppCache UTILIZADA 
// 	EN GUICE POR AssistedInject (ver móudlo guice)
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Factoría de las clases responsables de cargar los xmls de propiedades.
	 * (utilizada por GUICE AssistedInject)
	 */
	static interface XMLPropertiesForAppCacheFactory {
		/**
		 * Crea la caché de propiedades.
		 * @param appCode Código de aplicación.
		 * @param componentsNumberEstimation Estimación de componentes.
		 * @param useCache <code>true</code> si se utiliza la caché, <code>false</code> en caso contrario.
		 * @return La caché de propiedades.
		 */
		public XMLPropertiesForAppCache createFor(AppCode appCode,int componentsNumberEstimation,
							  					  boolean useCache);
		/**
		 * Crea la caché de propiedades.
		 * @param appCode Código de aplicación.
		 * @param environment entorno
		 * @param componentsNumberEstimation Estimación de componentes.
		 * @param useCache <code>true</code> si se utiliza la caché, <code>false</code> en caso contrario.
		 * @return La caché de propiedades.
		 */
		public XMLPropertiesForAppCache createFor(final Environment env,final AppCode appCode,
												  final int componentsNumberEstimation,final boolean useCache);
	}
	/**
	 * Implementación por defecto de {@link XMLPropertiesForAppCacheFactory} para ser usada fuera de GUICE
	 */
	static class DefaultXMLPropertiesForAppCacheFactory
	  implements XMLPropertiesForAppCacheFactory {
		@Override
		public XMLPropertiesForAppCache createFor(final AppCode appCode,
												  final int componentsNumberEstimation,final boolean useCache) {
			return new XMLPropertiesForAppCache(appCode,
											    componentsNumberEstimation,useCache);
		}
		@Override
		public XMLPropertiesForAppCache createFor(final Environment env,final AppCode appCode,
												  final int componentsNumberEstimation,final boolean useCache) {
			return new XMLPropertiesForAppCache(env,appCode,
												componentsNumberEstimation,useCache);
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTES
///////////////////////////////////////////////////////////////////////////////////////////
	static int DEFAULT_COMPONENTS_NUMBER = 30;
	static int DEFAULT_PROPERTIES_PER_COMPONENT = 1000;
///////////////////////////////////////////////////////////////////////////////////////////
//  CACHE DE XMLs POR COMPONENTE: Se consulta cuando una propiedad NO está en la cache
///////////////////////////////////////////////////////////////////////////////////////////
	// Caché de ficheros XML
	private XMLPropertiesForAppComponentsContainer _componentXMLManager;
///////////////////////////////////////////////////////////////////////////////////////////
//  CACHE DE PROPIEDADES
///////////////////////////////////////////////////////////////////////////////////////////
    // Cache de propiedades: relaciona el XPath de la propiedad con el nodo del DOM
    //						 donde se define la propiedad
	private Map<CacheKey,CacheValue> _cache;
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
	// Código de aplicación al que se refiere esta caché
	private AppCode _appCode;

	// Indica si utiliza la cache o consulta de nuevo el XML del manager
	private boolean _useCache = true;


///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Constructor en base al tamaño del cache y el modo de depuración.<br>
	 * <b>NOTA:</b>	Se utiliza {@link com.google.inject.assistedinject.AssistedInject} ya que los parametros appCode, componentsNumberEstimation y
	 * 		useCache NO se conocen hasta el momento de la ejecución por lo que NO se puede crear un binding estatico para XMLPropertiesCache
	 * 		Para utilizar {@link com.google.inject.assistedinject.AssistedInject}:
	 * 		<ul>
	 * 		<ol>Crear un interfaz para la factoría de {@link XMLPropertiesForAppCache}, por ejemplo {@link XMLPropertiesForAppCacheFactory}
	 * 				<pre class="brush:java">
	 * 					public XMLPropertiesForAppCache createFor(String appCode,int componentsNumberEstimation,
	 *				  									    	  boolean useCache);
	 *				</pre>
	 *			(esta interfaz NO se implementa... lo hace Guice automáticamente).
	 *		</ol>
	 *		<ol>En el CONSTRUCTOR de la clase que implementa XMLPropertiesForAppCache (esta clase), anotar los parámetros que
	 *			NO se pueden inyectar con &#64;Assisted:
	 *				<pre class="brush:java">
	 *				&#64;Inject
	 *				public XMLPropertiesForAppCacheImpl(&#64;Assisted final String appCode,&#64;Assisted final int componentsNumberEstimation,
	 *					      					  		&#64;Assisted final boolean useCache)
	 *				</pre>
	 *		</ol>
	 *		<ol>En el módulo de Guice, indicar que para construir una instancia de XMLPropertiesForAppCache, se utilice una
	 *			una factoría que implementa {@link XMLPropertiesForAppCacheFactory} y que Guice construye automáticamente por detrás:
	 *				<pre class="brush:java">
	 *				Module assistedModuleForPropertiesCacheFactory = new FactoryModuleBuilder().implement(XMLPropertiesForAppCache.class,
	 *																									  XMLPropertiesForAppCache.class)
	 *																						   .build(XMLPropertiesForAppCacheFactory.class);
	 *				binder.install(assistedModuleForPropertiesCacheFactory);
	 *				</pre>
	 *		</ol>
	 *		</ul>
	 * @param appCode Código de aplicación.
	 * @param componentsNumberEstimation Tamaño de la cache de apps/components.
	 * @param useCache <code>true</code> indica si se utiliza la cache de objetos recuperados del properties.
	 */
    @Inject
	public XMLPropertiesForAppCache(@XMLPropertiesEnvironment final Environment env,
							      	@Assisted final AppCode appCode,@Assisted final int componentsNumberEstimation,
							      	@Assisted final boolean useCache) {
		_appCode = appCode;
		_useCache = useCache;
		_componentXMLManager = new XMLPropertiesForAppComponentsContainer(this,							// listener de carga de componentes
																	      env,							// entorno (si está definido en una variable de sistema -ver modulo guice-
																	      _appCode,componentsNumberEstimation);	// codigo de aplicación / estimación de componentes para la aplicación
	}
	public XMLPropertiesForAppCache(final AppCode appCode,final int componentsNumberEstimation,
							      	final boolean useCache) {
    	this(null,
    		 appCode,componentsNumberEstimation,
    		 useCache);
	}
	public XMLPropertiesForAppCache(final Environment env,
							      	final AppCode appCode,final int componentsNumberEstimation) {
    	this(env,
    		 appCode,componentsNumberEstimation,
    		 true);
	}
	public XMLPropertiesForAppCache(final AppCode appCode,final int componentsNumberEstimation) {
    	this(null,
    		 appCode,componentsNumberEstimation,
    		 true);
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////

///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ XMLPropertiesComponentLoadedListener
///////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void newComponentLoaded(final XMLPropertiesComponentDef def) {
		// Asegurarse de que hay espacio en la caché para las propiedades estimadas del componente
		_ensureCapacity(def.getNumberOfPropertiesEstimation());
	}
	/**
	 * Se asegura cierta capacidad del caché.<br>
	 * A esta función se llama desde la clase {@link XMLPropertiesForComponentContainer} cuando se carga
	 * un nuevo XML de propiedades para un componente:
	 * 		<ul>
	 *      <li>En el XML de definición del componente, se indica una estimación del número de propiedades.</li>
	 * 		<li>Cuando se carga el XML de definición del componente se llama a este método para forzar el redimensionamiento de la caché.</li>
	 * 		</ul>
	 * de esta forma, la caché estará normalmente bien dimensionada.
	 * @param propertiesPerComponentEstimation Estimación de número de propiedades para el componente.
	 */
	private void _ensureCapacity(final int propertiesPerComponentEstimation) {
		final float cacheMapLoadFactor = 0.5F;	// El loadFactor de un mapa es una medida de cómo de llena puede estar una tabla ANTES de que
												// se incremente su capacidad: cuando numEntradas > loadFactor*capacidadActual la tabla interna es
												// re-construida y se dobla el número de buckets (capacidad)
												// NOTA:
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
												// El segundo tipo de colisión esta afectado por la capacidad (número de buckets) del mapa, que en realidad
												// está afectado por el loadFactor (entre 0 y 1)
												//		Cuanto MENOR es el loadFactor, MENOS probable es una colisión (el número de buckets de la tabla
												//		se va a doblar más rápidamente con lo que es menos probable que se agote el espacio de la tabla
												//		y mas de una entrada acabe en el mismo bucket)
		if (_cache == null) {
			// se crea una nueva cache
			int cacheSize = propertiesPerComponentEstimation + 100;
			log.trace("Creating a {} positions cache for every component of {}",Integer.toString(cacheSize),_appCode);
			_cache = new HashMap<CacheKey,CacheValue>(cacheSize,cacheMapLoadFactor);	// El segundo parametro es el factor de carga del mapa
																						// cuanto menor sea...
		} else {
			// se redimensiona la cache
			int cacheSize = _cache.size() + propertiesPerComponentEstimation + 100;
			log.trace("Resizing cache to add {} positions; final size: {}",Integer.toString(propertiesPerComponentEstimation),Integer.toString(cacheSize));
			Map<CacheKey,CacheValue> tempCache = new HashMap<CacheKey,CacheValue>(cacheSize,cacheMapLoadFactor);		// El segundo parametro es el factor de carga del mapa
																														// cuanto menor sea...
			tempCache.putAll(_cache);
			_cache = tempCache;
		}
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS SOBRE EL CONTENIDO
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Inicializa la caché de todos los componentes de todas las aplicaciones.
	 * @return el número de elementos re-iniciados.
	 */
	public int clear() {
		return this.clear(null);
	}
    /**
     * Recarga las propiedades de un componente o de todos.
     * @param component El componente puede ser:
     * <ul>
     * 		<li> component != null se recarga el componente señalado.</li>
     * 		<li> component == null se recargan TODOS los componentes.</li>
     * </ul>
     * @return el número de elementos re-iniciados
     */
	public int clear(final AppComponent component) {
		log.trace("clearing cached properties for component {} of {}",component,_appCode);
		if (_cache == null) return 0;
		int numMatches = 0;
		if (component == null) {
			// Se resetea TODA la cache
			numMatches = _cache.size();
			_cache.clear();
		} else {
			// Se resetea únicamente la cache de UN COMPONENTE de la aplicacion
			List<CacheKey> keysToRemove = new ArrayList<CacheKey>();
			for (CacheKey key : _cache.keySet()) {
				if (key.isSameAs(component)) {
					keysToRemove.add(key);	// Añadir la clave a la lista de claves a eliminar
					numMatches++;
				}
			}
			if (!keysToRemove.isEmpty()) {
				for (CacheKey key : keysToRemove) {
					Object removedProperty = _cache.remove(key);					// Eliminar la clave
					if (removedProperty != null) numMatches++;
				}
			}
		}
		return numMatches;
	}
	/**
	 * Obtiene las estadísticas de la cache a partir de los datos de uso almacenados en cada
	 * entrada de la cache.
	 * @return un objeto CacheStatistics
	 */
	public CacheStatistics usageStats() {
		CacheStatistics outStats = new CacheStatistics();
		for (CacheValue val : _cache.values()) {
			if (val.getPropValue() != null) {
				if (val.isDefaultValue()) {
					outStats.setDefaultCount( outStats.getDefaultCount() + val.getAccessCount() );
				} else {
					outStats.setHitCount( outStats.getHitCount() + val.getAccessCount() - 1);	// el primer acceso NO es por la cache
					outStats.setNonHitCount( outStats.getNonHitCount() + 1 );
				}
			} else {
				outStats.setInvalidCount( outStats.getInvalidCount() + val.getAccessCount());
			}
		}
		return outStats;
	}
///////////////////////////////////////////////////////////////////////////////
//	METODOS DE NEGOCIO
///////////////////////////////////////////////////////////////////////////////
    /**
     * Comprueba si existe una propiedad.
     * @param component componente de la aplicación.
     * @param propXPath ruta xpath para obtener la propiedad.
     * @return true si la propiedad existe (no es <code>null</code>).
     */
    public boolean existProperty(final AppComponent component,final Path propXPath) {
        // Verificar el cache de propiedades
        Object propValue = _retrieve(component,propXPath,null,null);	// si se pasa el tipo a null SOLO mira la cache (no intenta la carga desde el XML)
        if (propValue != null) return true;
        // si es false, puede que la propiedad NO exista realmente o puede que SI, pero NO se haya cargado,
        // para comprobarlo, simplemente buscar el nodo xml y ver si existe
        Node node = _componentXMLManager.getPropertyNode(component,propXPath);
        return node != null;
    }
	/**
	 * Devuelve una propiedad.
	 * @param component el componente de la aplicación.
	 * @param propXPath la ruta XPath de la propiedad.
	 * @param defaultValue el valor por defecto de la propiedad.
	 * @param type el tipo de dato en el que se quiere la propiedad.
	 * @return La propiedad o <code>null</code> si la propiedad no existe.
	 */
    public <T> T getProperty(final AppComponent component,final Path propXPath,
    						 final T defaultValue,
    						 final Class<T> type) {
    	T outObj = this.getProperty(component,propXPath,
    								defaultValue,
    								type,
    								null);		// sin utilizar marshaller
    	return outObj;
    }
	/**
	 * Devuelve una propiedad.
	 * @param component el componente de la aplicación.
	 * @param propXPath la ruta XPath de la propiedad.
	 * @param defaultValue el valor por defecto de la propiedad.
	 * @param typeRef el tipo de dato en el que se quiere la propiedad.
	 * @return La propiedad o <code>null</code> si la propiedad no existe.
	 */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(final AppComponent component,final Path propXPath,
    						 final T defaultValue,
    						 final TypeRef<T> typeRef) {
    	T outObj = this.getProperty(component,propXPath,
    								defaultValue,
    								(Class<T>)typeRef.rawType());
    	return outObj;
    }
	/**
	 * Devuelve una propiedad.
	 * @param component el componente de la aplicación.
	 * @param propXPath la ruta XPath de la propiedad.
	 * @param defaultValue el valor por defecto de la propiedad.
	 * @param type el tipo de dato en el que se quiere la propiedad.
	 * @param marshaller el marshaller para pasar de XML a objetos el xml de las propiedades.
	 * @return La propiedad o <code>null</code> si la propiedad no existe.
	 */
    @SuppressWarnings("unchecked")
    public <T> T getProperty(final AppComponent component,final Path propXPath,
    						 final T defaultValue,
    						 final Class<T> type,
    						 final Marshaller marshaller) {
    	CacheValue value = _retrieve(component,propXPath,
    								 type,marshaller);
    	T outObj = (T)value.getPropValue();
    	if (outObj == null && defaultValue != null) {
    		// La propiedad NO existe, pero se pasa un valor por defecto; almacenar el valor por defecto
    		outObj = defaultValue;
    		value.setValue(defaultValue,true);
    	} else if (outObj == null) {
    		// La propiedad NO existe y NO se pasa un valor por defecto
    	}
    	return outObj;
    }
	/**
	 * Devuelve el nodo xml que contiene la propiedad
	 * @param component el componente de la aplicación
	 * @param propXPath ruta xPath de la propiedad
	 * @return el nodo xml 
	 */
	public Node getPropertyNode(final AppComponent component,final Path propXPath) {
		Node node = _componentXMLManager.getPropertyNode(component,propXPath);
		return node;
	}
	/**
	 * Devuelve una lista de nodos resultado de aplicar el xPath
	 * @param component el componente de la aplicación
	 * @param propXPath ruta xPath de la propiedad
	 * @return la lista de nodos xml
	 */
	public NodeList getPropertyNodeList(final AppComponent component,final Path propXPath) {
		NodeList nodeList = _componentXMLManager.getPropertyNodeList(component,propXPath);
		return nodeList;
	}
///////////////////////////////////////////////////////////////////////////////
// 	METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////
	private String _retrieveStringProperty(final AppComponent component,final Path propXPath) {
		Node node = _componentXMLManager.getPropertyNode(component,propXPath);
    	return node != null ? XMLUtils.nodeTextContent(node)
    						: null;
	}
  	private Map<String,List<String>> _retrieveMapOfStringsProperty(final AppComponent component,final Path propXPath) {
    	Map<String,List<String>> outMap = null;
		NodeList nodeList = _componentXMLManager.getPropertyNodeList(component,propXPath);
		if (nodeList != null && nodeList.getLength() > 0) {
			outMap = new HashMap<String,List<String>>(nodeList.getLength());
			for (int i=0; i<nodeList.getLength(); i++) {
				Node node = nodeList.item(i);

				// Obtener el nombre del item
				String nodeName = node.getLocalName();
				// Obtener el valor del item
				String nodeStrValue = null;
				Node contentNode = node.getFirstChild();
    			if (XMLUtils.isTextNode(contentNode)) {
    				nodeStrValue = contentNode.getTextContent();
    			} else {
    				nodeStrValue = XMLStringSerializer.writeNode(contentNode,null);
    			}
    			// Poner el item en el mapa
				if (nodeStrValue != null) {
					List<String> currItemValue = outMap.get(nodeName);
					if (currItemValue == null) {
						currItemValue = new ArrayList<String>();
						outMap.put(nodeName,currItemValue);
					}
					currItemValue.add(nodeStrValue);
					outMap.put(nodeName,currItemValue);
				}
			}
		}
    	return outMap;
	}
	/**
	 * @param component
	 * @param propXPath
	 * @param type
	 * @param marshaller
	 * @return
	 */
	private <T> T _retrieveBeanPropertyUsingMarshaller(final AppComponent component,final Path propXPath,
						 	       				 	   final Class<?> type,final Marshaller marshaller) {
		T outObj = null;
		if (marshaller == null) {
			log.warn("Error transforming property {} from {}/{} to an object using r01 Marshaller: the provided marshaller is null",
					 propXPath,_appCode.asString(),component);
		} else {
			Node node = _componentXMLManager.getPropertyNode(component,propXPath);
			if (node != null) {
		        try {
			        outObj = marshaller.<T>beanFromXml(node);
		        } catch (MarshallerException msEx) {
		        	String err = Strings.of("Error transforming property {} from {}/{} to an object using r01 Marshaller: {}")
		        						.customizeWith(propXPath,_appCode,component,msEx.getMessage()).asString();
		        	log.error(err,msEx);
		        }
			}
		}
		return outObj;
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PARA GUARDAR Y RECUPERAR DE LA CACHE DE PROPIEDADES DE UN COMPONENTE DE UNA APP
// 	Los objetos se indexan en una cache en la que la clave se compone a partir de
//	appCode, component y la ruta XPath de la aplicación
// 		appCode1 / component1A --> xPath_prop1 - obj1
// 								   xPath_prop2 - obj2
// 								   ...
// 		appCode2 / Component2A --> xPath_prop1 - obj1
//								   ...
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Recupera la referencia a un objeto extraido de las propiedades.<br>
	 * <b>NOTA:</b><p>Si el parámetro <code>type==null</code>, SOLO se mira la caché, NO hace la carga desde el XML
	 * 			en caso de NO estar la propiedad en la caché.
	 * @param component Componente.
	 * @param xPath El xPath a la propiedad.
	 * @param type El tipo del objeto devuelto.
	 * @param marshaller El marshaller utilizado para pasar el xml a objetos.
	 * @return El objeto almacenado para este appCode/component.
	 */
	@SuppressWarnings("unchecked")
	private <T> CacheValue _retrieve(final AppComponent component,final Path xPath,
									 final Class<T> type,final Marshaller marshaller) {
		// [0] Comprobar si es necesario re-cargar las propiedades
		boolean hasToClear = _componentXMLManager.reloadIfNecessary(component);
		if (hasToClear) this.clear(component);		// Borrar todas las entradas del componente en la caché

		// [1] Cargar la propiedad
		CacheKey key = new CacheKey(component,xPath);
		CacheValue value = null;
		if (_useCache) {
			// Intentar recuperar el valor de la cache
			value = _cache.get(key);
			if (value != null) value.anotherHit();
		}
		if (value == null && type != null) {
			// Es la primera vez que se accede a la propiedad o NO se usa la cache
			// ... se "tira" directamente del xml
			T obj = null;
    		if (type.equals(String.class)) {
    			// String
    			obj = (T)_retrieveStringProperty(component,xPath);

    		} else if (type.equals(Number.class)) {
    			// Numero (int, long, double, etc)
    			String numStr = _retrieveStringProperty(component,xPath);
		    	try {
		    		Number num = NumberUtils.createNumber(numStr);
		    		obj = (T)num;
		    	} catch (NumberFormatException nfEx) {
		    		log.warn("Property {} from appCode/component={}/{}: {} cannot be converted to a Number!",
		    			     xPath,_appCode.asString(),component,numStr);
		    	}

    		} else if (type.equals(Boolean.class)) {
    			// Booleano
    			String bolStr = _retrieveStringProperty(component,xPath);
		    	Boolean bool = BooleanUtils.toBooleanObject(bolStr);
		    	if (bolStr != null && bool == null) {
		    		log.debug("Property {} from appCode/component={}/{}: {} cannot be converted to a boolean!",
		    				  xPath,_appCode.asString(),component,bolStr);
		    	}
		    	obj = (T)bool;

    		} else if (CollectionUtils.isMap(type)) {
    			// Mapa
    			obj = (T)_retrieveMapOfStringsProperty(component,xPath);

    		} else if (marshaller != null) {
    			obj = (T)_retrieveBeanPropertyUsingMarshaller(component,xPath,
    														  type,
    														  marshaller);
    		} else {
    			log.warn("{} type is not a supported for a property. Property {} from appCode/component={}/{}: cannot be converted",
    					 type.getName(),xPath,_appCode.asString(),component);
    		}
	    	// Guardar el objeto recién creado en la cache
    		value = _store(component,xPath,obj,false);		// IMPORTANTE!! obj puede ser null si la propiedad NO existe!!!!
		}
		// Devolver
		return value;
	}
	/**
	 * Almacena en la caché un objeto extraido de las propiedades.
	 * @param component Componente.
	 * @param xPath El xpath de la propiedad.
	 * @param obj El objeto a almacenar.
	 * @param isDefaultVal <code>true</code> si se está almacenando el valor por defecto para la propiedad.
	 * @return El anterior objeto almacenado para este appCode/component.
	 */
	private CacheValue _store(final AppComponent component,final Path xPath,
							  final Object obj,final boolean isDefaultVal) {
		CacheKey key = new CacheKey(component,xPath);
		CacheValue value = new CacheValue(1,System.currentTimeMillis(),obj,isDefaultVal);
		_cache.put(key,value);
		return value;
	}
///////////////////////////////////////////////////////////////////////////////////////////
//  CLAVE DE CADA ELEMENTO DE LA CACHE DE PROPIEDADES
///////////////////////////////////////////////////////////////////////////////////////////
  	@Accessors(prefix="_")
  	@EqualsAndHashCode @ToString
  	@AllArgsConstructor
  	private class CacheKey {
  		@Getter private AppComponent _component;
  		@Getter private Path _propXPath;

		boolean isSameAs(final AppComponent... keyComponent) {
			boolean isSame = false;
			if (keyComponent.length == 2) {	// component / xPath
				isSame = this.composeKey(_component).equals(this.composeKey(keyComponent[0],keyComponent[1]));
			} else if (keyComponent.length == 1) {	// appCode
				isSame = this.composeKey(_component).equals(this.composeKey(keyComponent[0]));
			}
			return isSame;
		}
		AppComponent composeKey(final AppComponent... keyComponent) {
			AppComponent outKey = null;
			if (keyComponent.length == 2) {	// component / xPath
				outKey = AppComponent.forId(keyComponent[0].asString() + "." + keyComponent[1].asString());
			} else if (keyComponent.length == 1) {	// component
				outKey = keyComponent[0];
			}
			return outKey;
		}
  	}
    /**
     * Información que se guarda en la caché cuando una entrada NO se encuentra en el properties,
     * de esta forma se evita tener que consultar de nuevo el árbol DOM.<br>
     * Simplemente almacena información sobre el número de veces que se ha preguntado por la propiedad
     * y el último acceso a la misma.
     */
  	@Accessors(prefix="_")
    @NoArgsConstructor @AllArgsConstructor
    private class CacheValue {
    	@Getter private long _accessCount;
    	@Getter private long _lastAcessTimeStamp;
    	@Getter private Object _propValue;
    	@Getter private boolean _defaultValue;
    	public void anotherHit() {
    		_accessCount++;
    		_lastAcessTimeStamp = System.currentTimeMillis();
    	}
    	public void setValue(final Object val,final boolean defaultVal) {
    		_propValue = val;
    		_defaultValue = defaultVal;
    	}
    }
///////////////////////////////////////////////////////////////////////////////
//	InnerClass que encapsula las estadisticas de la cache
///////////////////////////////////////////////////////////////////////////////
    @Accessors(prefix="_")
	@NoArgsConstructor
    class CacheStatistics {
    	@Getter @Setter private long _hitCount;		// Número total de hits en la cache
    	@Getter @Setter private long _nonHitCount;	// Número total de hits fuera de la cache (entradas existentes)
    	@Getter @Setter private long _invalidCount;	// Número total de invalidos en la cache (entradas NO existentes)
    	@Getter @Setter private long _defaultCount;	// Número total de propiedades devueltas a partir del valor por defecto
    	public String debugInfo() {
    		StringBuilder invalidPropsStr = new StringBuilder("");
	    	return Strings.create().add("Estadisticas de uso de la cache de XMLProperties:")
								   .add("\r\n     Hits: ").add(Long.toString(_hitCount))
								   .add("\r\n  NO-Hits: ").add(Long.toString(_nonHitCount))
								   .add("\r\n Defaults: ").add(Long.toString(_defaultCount))
								   .add("\r\n Invalids: ").add(Long.toString(_invalidCount))
								   .add("\r\n").add(invalidPropsStr).asString();
	    }
    }
}
