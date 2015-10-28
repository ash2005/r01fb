package r01f.xmlproperties;

import java.util.HashMap;
import java.util.Map;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.AppComponent;
import r01f.guids.CommonOIDs.AppCode;
import r01f.xmlproperties.XMLPropertiesForAppCache.DefaultXMLPropertiesForAppCacheFactory;
import r01f.xmlproperties.XMLPropertiesForAppCache.XMLPropertiesForAppCacheFactory;

import com.google.inject.Guice;
import com.google.inject.Inject;

/**
 * {@link XMLProperties} is the main component of the R01's properties systems
 * Properties have two levels
 * <ol>
 * 		<li>By application code</li>
 * 		<li>By component (module) within an application</li>
 * </ol>
 * Properties are defined in XML format by app code / module that means that for a given
 * application code many modules may be defined
 * Example of an XML properties file:
 * <pre class="brush:xml">
 *          <misProps>
 *              <miProp>el valor</miProp>
 *              <miOtraProp value='a'></miOtraProp>
 *              <miOtraLista>
 *              	<prop1>value1</prop1>
 *              	<prop2>value2</prop2>
 *             	</miOtraLista>
 *          </misProps>
 * </pre>
 * 
 * Example of how application properties may be distributed in multiple XML files by module
 * <pre>
 * 		- 'main' 			-- general properties
 * 		- 'contentServer' 	-- content server related properties
 * 		- etc
 * </pre>
 * 
 * Each of the properties XML might be stored in a different place_
 * ie:  
 * <pre>
 * 		- 'main' 			-- stored in a file within the classpath 
 * 		- 'contentServer' 	-- stored in the BBDD
 * 		- etc
 * </pre>
 * 
 * <h2>COMPONENT LOADING</h2>
 * <hr/>
 * Para cargar los componentes es necesario:
 * <ol>
 * 		<li>Definir cómo se ha de cargar el componente (ver {@link XMLPropertiesComponentDef}</li>
 * 		<li>Definir el componente</li>
 * </ol>
 * Lo más IMPORTANTE es el punto [1]: Definir la carga del componente<br/>
 * Para eso:
 * <ol>
 * 	 	<li>El el CLASSPATH debe existir un directorio llamado <b>components</b></li>
 * 		<li>Dentro del directorio <b>components</b> debe haber un XML de definición de la carga del componentes para CADA componente<br/>
 * 			<pre>
 * 				Ej: Si la aplicación r01 tiene DOS componentes (ej: main y contentServer), en el directorio components del CLASSPATH deben existir
 * 				    DOS ficheros:
 * 						- /components/r01.main.xml
 * 						- /components/r01.contentServer.xml
 * 			</pre>
 * 			En cada uno de los ficheros se define <b>cómo se cargan las propiedades de cada componente</b>, por ejemplo, del CLASSPATH, de la BBDD, etc
 * 		</li>
 * </ol>
 * <h3>Carga de componentes en función del entorno</h3>
 * Es posible definir una variable del sistema llamada r01Env (al arrancar la máquina virtual -Dr01Env=loc o mediante System.setProperty("r01Env","loc") que
 * permite indicar un ENTORNO de donde coger la configuración de carga de un componente del properties.
 * Si la variable r01Env existe, la carga del properties sigue el patrón /components/[r01Env]/[appCode].[component].xml (ver que se ha incluido un directorio
 * adicional a la hora de buscar el componente (/components/[r01Env])
 * 
 * 
 * <h2>USO DE LOS XMLProperties</h2>
 * <hr/>
 * A la hora de utilizar las propiedades se consultan utilizando sentencias XPath en un objeto 
 * {@link XMLPropertiesForApp} que es el que "gestiona" los properties de una aplicación:<br/>
 * Por ejemplo: <br/>
 * <pre class="brush:java">
 * 		XMLPropertiesForApp props = ...
 * 		props.of("componente").at("misProps/miProp").asString();
 * 		props.of("componente").at("miOtraProp/@value").asString();
 * </pre>
 * 
 * Para obtener un objeto {@link XMLPropertiesForApp} hay diferentes opciones
 * 
 * <pre>
 * OPCION 1: NO utilizar GUICE: Crear el Manager a mano 
 * ------------------------------------------------------------------
 * </pre>
 * <pre class="brush:java">
 * 		// [1] Crear 
 * 		XMLProperties props = XMLProperties.create();
 * 		// [2] Obtener las propiedades de la aplicacion
 * 		XMLPropertiesForApp appProps = props.forApp(AppCode.forId("xx"),1000);
 * 		// Acceder a las propiedades
 * 		String prop = props.of(component).propertyAt(xPath).asString()
 * 
 * 		// Or even simpler
 * 		String prop = XMLProperties.createForAppComponent(appCode,component)
 * 								   .notUsingCache()
 * 								   .propertyAt(xPath).asString();
 * </pre>
 * <b>IMPORTANTE</b>El objeto XMLProperties tiene una cache de propiedades para CADA aplicacion, así que es recomendable tener 
 * 					UNA ÚNICA INSTANCIA del objeto XMLProperties creado, por ejemplo haciendo que sea estático
 * 
 * <pre>
 * OPCION 2: Utilizar GUICE
 * ------------------------------------------------------------------
 * </pre>
 * Hay que mantener una UNICA INSTANCIA DE LA CLASE {@link XMLPropertiesForApp} para toda la JVM.<br />
 * Para conseguirlo, lo óptimo es utilizar {@link Guice} y la clase {@link GuiceInjector} que mantiene un SINGLETON del INYECTOR
 * de GUICE:
 * <pre class="brush:java">
 * 		XMLPropertiesManager props = Guice.createInjector(new BootstrapGuiceModule())
 * 										  .getInstance(XMLProperties.class).forApp(appCode);
 * 		String prop = props.of(component).propertyAt(xPath).asString()
 * </pre>
 *
 * Lo ideal sería dejar que GUICE inyecte los properties en cualquier tipo: 
 * <pre class='brush:java'>
 * 		public class MyType {
 * 			@Inject @XMLPropertiesComponent("myComponent") 
 * 			XMLPropertiesForAppComponent _props;
 * 			// ...
 * 			public void myMethod(..) {
 * 				String myProp = _props.propertyAt(xPath);
 * 			}
 * 		}
 * </pre>
 * Como se ve lo único que ha sido necesario es inyectar un tipo {@link XMLPropertiesForAppComponent}
 * en cualquier clase.
 * Para que el sistema "sepa" qué componente hay que inyectar, es necesario anotar el objeto {@link XMLPropertiesForAppComponent}
 * con <pre>@XMLPropertiesComponent("myComponent")</pre>
 * 
 * Para hacer esto, basta con configurar en el módulo GUICE de la aplicación:
 * <pre class='brush:java'>
 *		@Override
 *		public void configure(Binder binder) {
 *			// ... what ever
 *		}
 *		@Provides @XMLPropertiesComponent("myComponent")
 *		XMLPropertiesForAppComponent provideXMLPropertiesForAppComponent(final XMLProperties props) {
 *			XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(AppCode.forId("xx")),
 *																								 AppComponent.forId("myComponent"));
 *			return outPropsForComponent;
 *		}
 * </pre>
 * ... or directly using the binder:
 * <pre class='brush:java'>
 *	binder.bind(XMLPropertiesForAppComponent.class)
 *		  .annotatedWith(new XMLPropertiesComponent() {		// see [Binding annotations with attributes] at https://github.com/google/guice/wiki/BindingAnnotations
 *									@Override
 *									public Class<? extends Annotation> annotationType() {
 *										return XMLPropertiesComponent.class;
 *									}
 *									@Override
 *									public String value() {
 *										return "myComponent";
 *									}
 *		  				 })
 *		  .toProvider(new Provider<XMLPropertiesForAppComponent>() {
 *							@Override
 *							public XMLPropertiesForAppComponent get() {
 *								XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(AppCode.forId("xx")),
 *																								 					 AppComponent.forId("myComponent"));
 *								return outPropsForComponent;
 *							}
 *		  			  });
 * </pre>
 * ... even simpler:
 *	binder.bind(XMLPropertiesForAppComponent.class)
 *		  .annotatedWith(new XMLPropertiesComponentImpl("myComponent"))
 *		  .toProvider(new Provider<XMLPropertiesForAppComponent>() {
 *							@Override
 *							public XMLPropertiesForAppComponent get() {
 *								XMLPropertiesForAppComponent outPropsForComponent = new XMLPropertiesForAppComponent(props.forApp(AppCode.forId("xx")),
 *																								 					 AppComponent.forId("myComponent"));
 *								return outPropsForComponent;
 *							}
 *		  			  });
 * 
 * 
 * A nivel interno, una visión de alto nivel es la siguiente:
 * <pre>
 * 		XMLProperties
 * 			|_Map<AppCode,XMLPropertiesForApp>  : para cada código de aplicación se asocia
 * 								|					     un manager de sus properties
 * 								|
 * 								|_XMLPropertiesForAppCache   : cache de propiedades para cada componente 
 * 	</pre>
 * <b>CARGA DE PROPIEDADES</b> ver clase {@link r01f.xmlproperties.XMLPropertiesForComponentContainer}.<br />
 * <b>----------------------------------------------------------------------------------------------------</b>
 * @see XMLPropertiesForComponentContainer
 */
@Slf4j
public class XMLProperties {
/////////////////////////////////////////////////////////////////////////////////////////
//	ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Cache de objetos {@link XMLPropertiesForApp} por aplicación que contiene un objeto
	 * {@link XMLPropertiesCache} para dicha aplicación
	 */
	private Map<AppCode,XMLPropertiesForApp> _propertiesManagerForAppCache;
	/**
	 * True si se utiliza la cache
	 */
	private boolean _useCache = true;
/////////////////////////////////////////////////////////////////////////////////////////
//	MIEMBROS INYECTADOS (ver constructor)
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Factoría de cache de propiedades de una aplicación
	 */
	private XMLPropertiesForAppCacheFactory _cacheFactory;

/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTORES
/////////////////////////////////////////////////////////////////////////////////////////
	public XMLProperties() {
		log.trace("XMLProperties BootStraping!!!!");
		_cacheFactory = new DefaultXMLPropertiesForAppCacheFactory();
	}
	/**
	 * Constructor
	 * @param cacheFactory Factoría de cache de propiedades.
     * 					   <p><b>NOTA:</b> se utiliza en la clase {@link XMLPropertiesManager} para cachear las propiedades de un componente.
     * 							 Dado que en ESTA clase se cachea un objeto {@link XMLPropertiesManager} POR APLICACIÓN,
     * 							 hay una instancia de {@link XMLPropertiesCache} por {@link XMLPropertiesManager} y por lo tanto
     * 							 por APLICACION.
	 */
	@Inject
	public XMLProperties(final XMLPropertiesForAppCacheFactory cacheFactory) {
		log.trace("XMLProperties BootStraping!!!!");
		_cacheFactory = cacheFactory;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static XMLPropertiesCacheUsageStep create() {
		return new XMLPropertiesCacheUsageStep();
	}
	public static XMLPropertiesForAppCacheUsageStep createForApp(final AppCode appCode) {
		return new XMLPropertiesForAppCacheUsageStep(appCode);
	}
	public static XMLPropertiesForAppCacheUsageStep createForApp(final String appCode) {
		return XMLProperties.createForApp(AppCode.forId(appCode));
	}
	public static XMLPropertiesForAppComponentCacheUsageStep createForAppComponent(final AppCode appCode,final AppComponent component) {
		return new XMLPropertiesForAppComponentCacheUsageStep(appCode,component);
	}
	public static XMLPropertiesForAppComponentCacheUsageStep createForAppComponent(final String appCode,final String component) {
		return XMLProperties.createForAppComponent(AppCode.forId(appCode),AppComponent.forId(component));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public static class XMLPropertiesCacheUsageStep {
		@SuppressWarnings("static-method")
		public XMLProperties notUsingCache() {
			XMLProperties outProps = new XMLProperties();
			outProps._notCaching();
			return outProps;
		}
		@SuppressWarnings("static-method")
		public XMLProperties usingCache() {
			XMLProperties outProps = new XMLProperties();
			return outProps;
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class XMLPropertiesForAppCacheUsageStep {
		private final AppCode _appCode;
		public XMLPropertiesForApp notUsingCache() {
			XMLProperties outProps = new XMLProperties();
			outProps._notCaching();
			return outProps.forApp(_appCode);
		}
		public XMLPropertiesForApp usingCache() {
			XMLProperties outProps = new XMLProperties();
			return outProps.forApp(_appCode);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class XMLPropertiesForAppComponentCacheUsageStep {
		private final AppCode _appCode;
		private final AppComponent _appComponent;
		public XMLPropertiesForAppComponent notUsingCache() {
			XMLProperties outProps = new XMLProperties();
			outProps._notCaching();
			return outProps.forAppComponent(_appCode,_appComponent);
		}
		public XMLPropertiesForAppComponent usingCache() {
			XMLProperties outProps = new XMLProperties();
			return outProps.forAppComponent(_appCode,_appComponent);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METODOS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return desactiva el cacheo de propiedades
	 */
	private XMLProperties _notCaching() {
		_useCache = false;
		return this;
	}
	public XMLPropertiesForApp forApp(final AppCode appCode,final int componentsNumberEstimation) {
		XMLPropertiesForApp manager = _propertiesManagerForAppCache != null ? _propertiesManagerForAppCache.get(appCode)
																			: null;
		if (manager == null) {
			log.trace("The properties for application {} are not present in the cache: they must be loaded NOW",appCode);
			XMLPropertiesForAppCache cache = _cacheFactory.createFor(appCode,componentsNumberEstimation,
																	 _useCache);
			manager = new XMLPropertiesForApp(cache,
											  appCode,	
										  	  componentsNumberEstimation);		
			_propertiesManagerForAppCache = new HashMap<AppCode,XMLPropertiesForApp>(15,0.5F);
			_propertiesManagerForAppCache.put(appCode,manager);
		}
		return manager;		
	}
	public XMLPropertiesForApp forApp(final AppCode appCode) {
		return this.forApp(appCode,10); // Component number estimation for the app
	}
	/**
	 * Obtiene el manager de propiedades {@link XMLPropertiesManager} para una aplicación.
	 * @param appCode Código de aplicación.
	 * @return El manager que permite acceder a las propiedades.
	 */
	public XMLPropertiesForApp forApp(final String appCode) {
		return this.forApp(AppCode.forId(appCode));
	}
	/**
	 * Returns the app component manager
	 * @param appCode
	 * @param component
	 * @return
	 */
	public XMLPropertiesForAppComponent forAppComponent(final AppCode appCode,final AppComponent component) {
		XMLPropertiesForApp propsForApp = this.forApp(appCode);
		return propsForApp.forComponent(component);
	}
	/**
	 * Returns the app component manager
	 * @param appCode
	 * @param component
	 * @return
	 */
	public XMLPropertiesForAppComponent forAppComponent(final String appCode,final String component) {
		return this.forAppComponent(AppCode.forId(appCode),AppComponent.forId(component));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * This method is used in guice binding to:
	 * <pre>
	 * 		Annotate an instance of {@link XMLPropertiesForApp} or {@link XMLPropertiesForAppComponent}
	 * 		to be injected by Guice
	 * </pre>
	 * Ej:
	 * <pre class='brush:java'>
	 * 		public class MyType {
	 * 			@Inject @XMLPropertiesComponent("r01m") XMLPropertiesForApp _manager;
	 * 			...
	 * 		}
	 * </pre>
	 * or
	 * <pre class='brush:java'>
	 * 		public class MyType {
	 * 			@Inject @XMLPropertiesComponent("default") XMLPropertiesForAppComponent _component;
	 * 			...
	 * 		}
	 * </pre> 
	 * The guice bindings are:
	 * <pre class='brush:java'>
	 * 		@Override
	 *		public void configure(Binder binder) {
	 *			binder.bind(XMLPropertiesForApp.class).annotatedWith(XMLProperties.named("r01m")
	 *				  .toProvider(new XMLPropertiesForAppGuiceProvider("r01m");
	 *			binder.bind(XMLPropertiesForAppComponent.class).annotatedWith(XMLProperties.named("default"))
	 *				  .toInstance(new XMLPropertiesForAppComponent("default")
	 *				  .in(Singleton.class);
	 *		}
	 * </pre>
	 * Returns a {@link XMLPropertiesComponent}
	 */
	public static XMLPropertiesComponent named(final String name) {
		return new XMLPropertiesComponentImpl(name);
	}
}
