package r01f.xmlproperties;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.guids.CommonOIDs.Environment;
import r01f.patterns.Memoized;
import r01f.services.ServiceIDs.ClientApiAppCode;
import r01f.services.ServiceIDs.CoreAppCode;
import r01f.xmlproperties.XMLPropertiesForAppCache.XMLPropertiesForAppCacheFactory;

/**
 * {@link XMLProperties} is the main component of the R01's properties systems
 * Properties have two levels
 * <ol>
 * 		<li>By application code</li>
 * 		<li>By component (module) within an application</li>
 * </ol>
 * Properties are defined in XML format by app code / module; this fact means that for a given
 * application code any number of modules may be defined
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
 * In order to a component to be loaded, the load method must be set before
 * (see {@link XMLPropertiesComponentDef})
 * <ol>
 * 	 	<li>The classpath MUST contain a folder named <b>{appCode}/components</b></li>
 * 		<li>Inside the <b>{appCode}/components</b> folder there MUST be a XML file for each component<br/>
 * 			<pre>
 * 				ie: If the appCode is xxx and it have two components (foo and bar) a xxx/components folder MUST exists in the classpath containing
 * 				    TWO xml files:
 * 						- xxx/components/xxx.foo.xml
 * 						- xxx/components/xxx.bar.xml
 * 			</pre>
 * 			Think about these files as an index or a definition of how / from where to load the real XML properties file
 * 			(ie: if the xml properties are real filesystem files, the component file contains the real path of the file
 * 				 if the xml properties are stored at a database table row, the component file contains how to connect to de bd and the table / row selection
 * 		</li>
 * </ol>
 * <h3>Environment dependent component loading</h3>
 * A system var named r01Env can be set (as a jvm argument -Dr01Env=loc or by means of System.setProperty("r01Env","loc"))
 * This var can set an environment where the components are loaded: it the r01Env var exists, the component loading will not be done from /{appCode}/components,
 * the components will be searched at: /{appCode}/components/{r01Env}/
 * 
 * <h2>XMLProperties usage</h2>
 * <hr/>
 * properties stored at XMLProperties files are retrieved using xpath sentences issued at a {@link XMLPropertiesForApp} object 
 * <pre class="brush:java">
 * 		XMLPropertiesForApp props = ...
 * 		props.of("componente").at("misProps/miProp").asString("defaultValue");
 * 		props.of("componente").at("miOtraProp/@value").asString();
 * </pre>
 * 
 * <h2>Property Caching</h2>
 * A high level vision of the cache system is:
 * <pre>
 * 		XMLProperties
 * 			|_Map<AppCode,XMLPropertiesForApp>  
 * 								|				
 * 								|
 * 								|_XMLPropertiesForAppCache  
 * 	</pre>
 * 
 * <h2>Property loading</h2>
 * In order to get a {@link XMLPropertiesForApp} object, there are many options:
 * <pre>
 * OPTION 1: Create the {@link XMLPropertiesForApp} by hand 
 * ------------------------------------------------------------------
 * </pre>
 * <pre class="brush:java">
 * 		// [1] Create an XMLProperties object
 * 		XMLProperties props = XMLProperties.create();
 * 		// [2] Get the properties for an application
 * 		XMLPropertiesForApp appProps = props.forApp(AppCode.forId("xx"),1000);
 * 		// [3] Access the properties of a component
 * 		String prop = props.of(component).propertyAt(xPath).asString()
 * 
 * 		// Or even simpler
 * 		String prop = XMLProperties.createForAppComponent(appCode,component)
 * 								   .notUsingCache()
 * 								   .propertyAt(xPath).asString();
 * </pre>
 * <b>IMPORTANTE</b>The {@link XMLPropertiesForApp} object maintains a cache of properties for each managed application code, so it's advisable to have 
 * 					a single instance of the {@link XMLProperties} object (ie: manage it as a guice singleton or make it static)
 * 
 * <pre>
 * OPCION 2: Use guice
 * ------------------------------------------------------------------
 * </pre>
 * It's advisable to have a SINGLE instance of {@link XMLPropertiesForApp} since it maintains a cache of properties<br />
 * A guice singleton can be used to achieve this pourpose:
 * <pre class="brush:java">
 * 		XMLPropertiesForApp appProps = Guice.createInjector(new XMLPropertiesGuiceModule())
 * 										  	.getInstance(XMLProperties.class).forApp(appCode);
 * 		String prop = props.of(component)
 * 						   .propertyAt(xPath).asString(defaultValue)
 * </pre>
 *
 * To let guice inject app component's properties, the @XmlPropertiesComponent annotation can be used: 
 * <pre class='brush:java'>
 * 		public class MyType {
 * 			@Inject @XMLPropertiesComponent("myComponent") 
 * 			XMLPropertiesForAppComponent _props;	<-- guice will inject an annotated XMLPropertiesForAppComponent instance
 * 			// ...
 * 			public void myMethod(..) {
 * 				String myProp = _props.propertyAt(xPath);
 * 			}
 * 		}
 * </pre>
 * ... to do so, there MUST exists a {@link XMLPropertiesForAppComponent} instance binded to a @XMLPropertiesComponent("myComponent") annotation
 * A guice's provider can be used: 
 * <pre class='brush:java'>
 *		@Override
 *		public void configure(final Binder binder) {
 *			// ... what ever other bindings
 *		}
 *		@Provides @XMLPropertiesComponent("myComponent")
 *		XMLPropertiesForAppComponent provideXMLPropertiesForAppComponent(final XMLProperties props) {
 *			// note that XMLProperties singleton will be injected to the provider by guice
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
 */
@Slf4j
public class XMLProperties {
/////////////////////////////////////////////////////////////////////////////////////////
//	FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Cache of {@link XMLPropertiesForApp} for every managed app
	 * the {@link XMLPropertiesForApp} also have another cache (an instance of {@link XMLPropertiesCache})
	 */
	private Map<AppCode,XMLPropertiesForApp> _propsForAppCache;
	/**
	 * Is the cache used?
	 */
	private boolean _useCache = true;
	/**
	 * App properties factory
	 */
	private XMLPropertiesForAppCacheFactory _propsForAppCacheFactory;

/////////////////////////////////////////////////////////////////////////////////////////
//	CONSTRUCTORS
/////////////////////////////////////////////////////////////////////////////////////////
	public XMLProperties() {
		log.trace("XMLProperties BootStraping!!!!");
		_propsForAppCacheFactory = new XMLPropertiesForAppCacheFactory() {
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
								   };
	}
	/**
	 * Constructor (used by guice)
	 * @param cacheFactory properties cache factory.
	 */
	@Inject
	public XMLProperties(final XMLPropertiesForAppCacheFactory cacheFactory) {
		log.trace("XMLProperties BootStraping!!!!");
		_propsForAppCacheFactory = cacheFactory;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	public static XMLPropertiesCacheUsageStep create() {
		return new XMLProperties().new XMLPropertiesCacheUsageStep();
	}
	public static XMLPropertiesForAppCacheUsageStep createForApp(final AppCode appCode) {
		return new XMLProperties().new XMLPropertiesForAppCacheUsageStep(appCode);
	}
	public static XMLPropertiesForAppCacheUsageStep createForApp(final ClientApiAppCode appCode) {
		return XMLProperties.createForApp(appCode.asAppCode());
	}
	public static XMLPropertiesForAppCacheUsageStep createForApp(final CoreAppCode appCode) {
		return XMLProperties.createForApp(appCode.asAppCode());
	}
	public static XMLPropertiesForAppCacheUsageStep createForApp(final String appCode) {
		return XMLProperties.createForApp(AppCode.forId(appCode));
	}
	public static XMLPropertiesForAppComponentCacheUsageStep createForAppComponent(final AppCode appCode,final AppComponent component) {
		return new XMLProperties().new XMLPropertiesForAppComponentCacheUsageStep(appCode,component);
	}
	public static XMLPropertiesForAppComponentCacheUsageStep createForAppComponent(final String appCode,final String component) {
		return XMLProperties.createForAppComponent(AppCode.forId(appCode),AppComponent.forId(component));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@NoArgsConstructor(access=AccessLevel.PRIVATE)
	public class XMLPropertiesCacheUsageStep {
		public XMLProperties notUsingCache() {
			_useCache = false;
			return XMLProperties.this;
		}
		public XMLProperties usingCache() {
			_useCache = true;
			return XMLProperties.this;
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class XMLPropertiesForAppCacheUsageStep {
		private final AppCode _appCode;
		public XMLPropertiesForApp notUsingCache() {
			_useCache = false;
			return XMLProperties.this.forApp(_appCode);
		}
		public XMLPropertiesForApp usingCache() {
			_useCache = true;
			return XMLProperties.this.forApp(_appCode);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class XMLPropertiesForAppComponentCacheUsageStep {
		private final AppCode _appCode;
		private final AppComponent _appComponent;
		public XMLPropertiesForAppComponent notUsingCache() {
			_useCache = false;
			return XMLProperties.this.forAppComponent(_appCode,_appComponent);
		}
		public XMLPropertiesForAppComponent usingCache() {
			_useCache = false;
			return XMLProperties.this.forAppComponent(_appCode,_appComponent);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public XMLPropertiesForApp forApp(final ClientApiAppCode appCode,final int componentsNumberEstimation) {
		return this.forApp(appCode.asAppCode(),componentsNumberEstimation);
	}
	public XMLPropertiesForApp forApp(final CoreAppCode appCode,final int componentsNumberEstimation) {
		return this.forApp(appCode.asAppCode(),componentsNumberEstimation);
	}
	public XMLPropertiesForApp forApp(final AppCode appCode,final int componentsNumberEstimation) {
		XMLPropertiesForApp propsForApp = _propsForAppCache != null ? _propsForAppCache.get(appCode)
																	: null;
		if (propsForApp == null) {
			log.trace("The properties for application {} are not present in the cache: they must be loaded NOW",appCode);
			XMLPropertiesForAppCache propsForAppCache = _propsForAppCacheFactory.createFor(appCode,componentsNumberEstimation,
																	 					   _useCache);
			propsForApp = new XMLPropertiesForApp(propsForAppCache,
											      appCode,	
										  	      componentsNumberEstimation);		
			_propsForAppCache = new HashMap<AppCode,XMLPropertiesForApp>(15,0.5F);
			_propsForAppCache.put(appCode,propsForApp);
		}
		return propsForApp;		
	}
	public XMLPropertiesForApp forApp(final AppCode appCode) {
		return this.forApp(appCode,10); 	// Component number estimation for the app
	}
	public XMLPropertiesForApp forApp(final ClientApiAppCode appCode) {
		return this.forApp(appCode.asAppCode());
	}
	public XMLPropertiesForApp forApp(final CoreAppCode appCode) {
		return this.forApp(appCode.asAppCode());
	}
	/**
	 * Gets an app properties manager {@link XMLPropertiesForApp} 
	 * @param appCode app code
	 * @return the manager that provides access to components and from there to properties
	 */
	public XMLPropertiesForApp forApp(final String appCode) {
		return this.forApp(AppCode.forId(appCode));
	}
	/**
	 * Gets an app properties manager (a {@link XMLPropertiesForApp} instance) and from it
	 * an manager of component properties (a {@link XMLPropertiesForAppComponent}) that provides
	 * access to properties
	 * @param appCode
	 * @param component
	 * @return
	 */
	public XMLPropertiesForAppComponent forAppComponent(final AppCode appCode,final AppComponent component) {
		XMLPropertiesForApp propsForApp = this.forApp(appCode);
		return propsForApp.forComponent(component);
	}
	/**
	 * Gets an app properties manager (a {@link XMLPropertiesForApp} instance) and from it
	 * an manager of component properties (a {@link XMLPropertiesForAppComponent}) that provides
	 * access to properties
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
	 * 			@Inject @XMLPropertiesComponent("default") 
	 * 			XMLPropertiesForAppComponent _component;
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
/////////////////////////////////////////////////////////////////////////////////////////
//  OS DETECTION
/////////////////////////////////////////////////////////////////////////////////////////
	public enum OSType {
		WINDOWS,
		MacOS,
		Linux,
		NIX;
	}
	private static String OS_PROP = System.getProperty("os.name").toLowerCase();
	public static Memoized<OSType> OS = new Memoized<OSType>() {
												@Override
												protected OSType supply() {
													if (OS_PROP.indexOf("win") >= 0) return OSType.WINDOWS;
													if (OS_PROP.indexOf("mac") >= 0 || OS_PROP.indexOf("darwin") >= 0) return OSType.MacOS;
													if (OS_PROP.indexOf("nux") >= 0) return OSType.Linux;
													if (OS_PROP.indexOf("nix") >= 0 || OS_PROP.indexOf("aix") > 0 || OS_PROP.indexOf("sunos") >= 0) return OSType.NIX;
													throw new IllegalStateException("Unknown os.name property: " + OS_PROP + " cannot detect the OS");
												}
									};
	public static OSType getOS() {
		return OS.get();
	}
}
