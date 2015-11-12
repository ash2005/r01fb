package r01f.persistence.internal;

import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.eclipse.persistence.config.PersistenceUnitProperties;
import org.w3c.dom.Node;

import com.google.inject.Binder;
import com.google.inject.name.Names;
import com.google.inject.persist.PersistService;
import com.google.inject.persist.jpa.JpaPersistModule;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.inject.GuiceModuleWithProperties;
import r01f.inject.ServiceHandler;
import r01f.services.ServicesPackages;
import r01f.services.core.DBPersistenceGuiceModule;
import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;
import r01f.util.types.Numbers;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Base type for DB guice modules
 */
@Slf4j
public abstract class DBGuiceModuleBase 
     		  extends GuiceModuleWithProperties
     	   implements DBPersistenceGuiceModule {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Sometimes the BDD AppCode does NOT match the application AppCode
	 */
	private final AppCode _bbddAppCode;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Constructor to be used when it's the db guice module for an app divided into components
	 * In this case, the app is composed by one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].persistence.properties.xml 
	 * @param coreBootstrapGuiceModuleType 
	 * @param bbddAppCode
	 */
	public DBGuiceModuleBase(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapGuiceModuleType,
							 final AppCode bbddAppCode) {
		super(ServicesPackages.appCodeFromCoreBootstrapModuleType(coreBootstrapGuiceModuleType),
			  AppComponent.forId(ServicesPackages.appComponentFromCoreBootstrapModuleTypeOrThrow(coreBootstrapGuiceModuleType).asString() + ".dbpersistence"));	// the persistence properties are going to be looked after as {appCode}.{appComponent}.persistence.xml
		_bbddAppCode = bbddAppCode != null ? bbddAppCode 
										   : this.getAppCode();
		log.info("...init the {} for appCode/component={}/{}; the persistence properties will be looked after as {}.{}.persistence.properties.xml",
				 this.getClass().getSimpleName(),this.getAppCode(),this.getAppComponent(),this.getAppCode(),this.getAppComponent());
	}
	/**
	 * Constructor to be used when it's the db guice module for an app divided into components
	 * In this case, the app is composed by one or more modules and the properties are going to be looked after at
	 * [appCode].[appComponent].persistence.properties.xml
	 * @param coreBootstrapGuiceModuleType 
	 */
	public DBGuiceModuleBase(final Class<? extends ServicesCoreBootstrapGuiceModule> coreBootstrapGuiceModuleType) {
		this(coreBootstrapGuiceModuleType,
			 null);
	} 
/////////////////////////////////////////////////////////////////////////////////////////
//  dirty trick to return the real appcomponent
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public AppComponent getAppComponent() {
		AppComponent comp = super.getAppComponent();
		String compStr = comp.asString();
		return AppComponent.forId(compStr.substring(0,compStr.lastIndexOf(".dbpersistence")));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GUICE MODULE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * 
	 */
	@Override
	public void configure(final Binder binder) {
		Binder theBinder = binder;
		
		// [0] - JPA
		// The JPA persistence unit name is composed by:
		//		- The appCode/component
		//		- The PersistenceUnitType: driverManager (connection) / dataSource
		// So the persistence unit name to use at the persistence.xml file is composed like:
		//		<persistence-unit name="persistenceUnit.{appCode}.{appComponent}.{persistenceUnitType}">
		PersistenceUnitType persistenceUnitType = this.propertyAt("persistence/@unitType")
													  .asEnumElement(PersistenceUnitType.class,
															  		 PersistenceUnitType.DRIVER_MANAGER);	// use driverManager by default
		
		// Sometimes it's an app component (ie: the urlAlias component for the r01t app)
		// On these cases:
		//	- the persistence properties are going to be looked after as  {appCode}.{appComponent}.persistence.properties.xml
		//	- the persistence unit is going to be looked after as persistenceUnit.{appCode}.{appComponent}
		//	  otherwise (no appComponent is set):
		//	- the persistence properties are going to be looked after as  {appCode}.persistence.properties.xml
		//	- the persistence unit is going to be looked after as persistenceUnit.{appCode} 
		String jpaServiceName = this.getAppComponent() == null ? _bbddAppCode.asString()
													  		   : Strings.customized("{}.{}",_bbddAppCode,this.getAppComponent());
		final String persistenceUnitName = Strings.customized("persistenceUnit.{}.{}",
													 	 	  jpaServiceName,			// ie: "r01t.urlAlias"
													 	 	  persistenceUnitType);		// ie: "persistenceUnit.r01n.myComponent.dataSource"
		
		// Load properties
		DBSpec bbddSpec = _loadDataBaseSpec(persistenceUnitType);
		Properties conxProps = persistenceUnitType.is(PersistenceUnitType.DRIVER_MANAGER) ? _loadBBDDConnectionPropertiesForPool(persistenceUnitType,
																																 persistenceUnitName,
																																 bbddSpec)
																						  : null;
		Properties appServerVendorProps = _loadAppServerVendorProperties(bbddSpec);
		Properties bbddVendorProps = _loadBBDDVendorProperties(bbddSpec);
		Properties schemaCreateProps = _loadSchemaCreateProperties(bbddSpec);
		Properties logProps = _loadLogProperties(bbddSpec);
		Properties otherProps = _loadOtherProperties(persistenceUnitType,
													 persistenceUnitName,
													 bbddSpec);
		
		final Properties props = new Properties();
		if (conxProps != null) 				props.putAll(conxProps);
		if (appServerVendorProps != null) 	props.putAll(appServerVendorProps);
		if (bbddVendorProps != null) 		props.putAll(bbddVendorProps);
		if (schemaCreateProps != null) 		props.putAll(schemaCreateProps);
		if (logProps != null) 				props.putAll(logProps);
		if (otherProps != null) 			props.putAll(otherProps);
		
		// Create the module	
		JpaPersistModule jpaModule = new JpaPersistModule(persistenceUnitName);	// for an alternative way see http://stackoverflow.com/questions/18101488/does-guice-persist-provide-transaction-scoped-or-application-managed-entitymanag
		jpaModule.properties(props);
		theBinder.install(jpaModule);
		
//		Provider<EntityManager> emProvider = theBinder.getProvider(EntityManager.class);
//		theBinder.bind(EntityManager.class)
//				 .annotatedWith(Names.named(jpaServiceName))
//				 .toProvider(emProvider);
//		this.expose(EntityManager.class)
//			.annotatedWith(Names.named(jpaServiceName));
		
		
		// Service handler used to control (start/stop) the Persistence Service (see ServletContextListenerBase)
		theBinder.bind(ServiceHandler.class)					
			  	 .annotatedWith(Names.named(jpaServiceName))
			  	 .to(JPAPersistenceServiceControl.class)
			  	 .in(Singleton.class);	//.asEagerSingleton();
//		this.expose(ServiceHandler.class)
//			.annotatedWith(Names.named(jpaServiceName));
		
		
		log.warn("... binded jpa persistence unit {} whose entity manager is handled by ServiceHandler with name {}",persistenceUnitName,jpaServiceName);
		
		// [1] - Custom bindings
		_doCustomBindings(theBinder);
	}
	/**
	 * Hook for the concrete implementations of this abstract type to do custom bindings
	 * @param binder
	 */
	protected abstract void _doCustomBindings(final Binder binder);
/////////////////////////////////////////////////////////////////////////////////////////
//  PersistenceService control
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree
	 * The {@link ServiceHandler} interface is used to start & stop the JPA's PersistenceService
	 * at ServletContextListenerBase type
	 */
	static class JPAPersistenceServiceControl 
	  implements ServiceHandler {
		
		private final PersistService _service;
		
		@Inject
		public JPAPersistenceServiceControl(final PersistService service) {
			_service = service;
		}
		@Override
		public void start() {
			if (_service == null) throw new IllegalStateException("NO persistence service available!");
			_service.start();
		}
		@Override
		public void stop() {
			if (_service == null) throw new IllegalStateException("NO persistence service available!");
			try {
				_service.stop();
			} catch (Throwable th) {/* just in the case where PersistenceService were NOT started */ }
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private DBSpec _loadDataBaseSpec(final PersistenceUnitType unitType) {
		String targetBBDD = this.propertyAt("persistence/unit[@type='" + unitType + "']/@targetBBDD")
								.asString();
		if (Strings.isNullOrEmpty(targetBBDD)) {
			log.error("Could NO load the BBDD vendor and version from persistence/unit[@type='{}']/@targetBBDD at {} properties file",
					  unitType,super.getAppCode());
			targetBBDD = "MySql 5.6";
		}
		DBSpec spec = DBSpec.valueOf(targetBBDD);
		return spec;
	}
	private Properties _loadBBDDConnectionPropertiesForPool(final PersistenceUnitType unitType,
															final String persistenceUnitName,
															final DBSpec spec) {
		// IMPORTANT
		//		The BBDD connection cannot be retrieved as usually: 
		//			[AppCode].bbddProperties().
		//					 .propertyAt("bbdd/connection")
		//		because the [AppCode] type is using the injector that it's being configured in this module: the injector is NOT configured
		//		in this moment
		// 		In order to overcome this inconvenient, this module extends GuiceModuleWithProperties that access properties outside GUICE
		
		// [1] - Driver User / password
		String xPath = "persistence/unit[@type='" + unitType + "']/connection[@vendor='" + spec.getVendor() + "']";
		Properties props = this.propertyAt(xPath)
							   .asProperties();
		if (CollectionUtils.isNullOrEmpty(props)) {
			log.error("Could NO load the BBDD connection properties at {} at properties file {}: does it contains a connection section?",
					  xPath,super.getAppCode());
			props = _defaultBBDDConnectionPropertiesForLocalPool();
		}
		
		// [2] - Pool size
		ConnectionPoolSize poolSize = _loadBBDDConnectionPoolSize(unitType);
		if (poolSize != null) {
			if (poolSize.initial != null) props.put("eclipselink.jdbc.connection_pool.default." + PersistenceUnitProperties.CONNECTION_POOL_INITIAL,poolSize.initial);
			if (poolSize.min != null)	  props.put("eclipselink.jdbc.connection_pool.default." + PersistenceUnitProperties.CONNECTION_POOL_MIN,poolSize.min);
			if (poolSize.max != null)	  props.put("eclipselink.jdbc.connection_pool.default." + PersistenceUnitProperties.CONNECTION_POOL_MAX,poolSize.max);
			String dbg = Strings.of("BBDD connection pool size for persistence unit with name {}: initial={}, min={}, max={}")
								.customizeWith(persistenceUnitName,
											   poolSize.initial,
											   poolSize.min,
											   poolSize.max)
								.asString();
			log.warn(dbg);
		}
		
		
		// Debug
		String dbg = Strings.of("BBDD connection properties for persistence unit with name {} \n" +
								"       javax.persistence.jdbc.user: {}\n" + 
								"   javax.persistence.jdbc.password: {}\n" + 
								"     javax.persistence.jdbc.driver: {}\n" + 
								"        javax.persistence.jdbc.url: {}") 
							.customizeWith(persistenceUnitName,
										   props.get(PersistenceUnitProperties.JDBC_USER),
										   props.get(PersistenceUnitProperties.JDBC_PASSWORD),
										   props.get(PersistenceUnitProperties.JDBC_DRIVER),
										   props.get(PersistenceUnitProperties.JDBC_URL))
							.asString();
		log.warn(dbg);
		return props;
	}
	private Properties _defaultBBDDConnectionPropertiesForLocalPool() {
		Properties props = new Properties();
		
		String dbUser = this.getAppCode().asString();
		String dbPassword = this.getAppCode().asString();
		
		props.put(PersistenceUnitProperties.JDBC_USER,dbUser);
		props.put(PersistenceUnitProperties.JDBC_PASSWORD,dbPassword);
		props.put(PersistenceUnitProperties.JDBC_DRIVER,"com.mysql.jdbc.Driver");		// BEWARE to copy mySql jconector at $CATALINA_HOME/lib  
		props.put(PersistenceUnitProperties.JDBC_URL,"jdbc:mysql://localhost:3306/r01n");
		
		return props;
	}	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private class ConnectionPoolSize {
		public final String initial;
		public final String min;
		public final String max;
	}
	private ConnectionPoolSize _loadBBDDConnectionPoolSize(final PersistenceUnitType unitType) {
		String xPath = "persistence/unit[@type='" + unitType + "']/connectionPool";
		Node node = this.propertyAt(xPath).node();
		if (node != null && node.getAttributes() != null) {
			String initial = node.getAttributes().getNamedItem("initial") != null ? node.getAttributes().getNamedItem("initial").getNodeValue() : null;
			String min = node.getAttributes().getNamedItem("min") != null ? node.getAttributes().getNamedItem("min").getNodeValue() : null;
			String max = node.getAttributes().getNamedItem("max") != null ? node.getAttributes().getNamedItem("max").getNodeValue() : null;
			
			if (!Numbers.isInteger(initial)) {
				log.error("The connection pool initial size at {} is NOT valid: {}",xPath,initial);
				initial = null;
			}
			if (!Numbers.isInteger(min)) {
				log.error("The connection pool min size at {} is NOT valid: {}",xPath,initial);
				min = null;
			}
			if (!Numbers.isInteger(max)) {
				log.error("The connection pool max size at {} is NOT valid: {}",xPath,initial);
				max = null;
			}
			return new ConnectionPoolSize(initial,min,max);
		}
		return null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @param spec
	 * @return
	 */
	private static Properties _loadAppServerVendorProperties(final DBSpec spec) {
		Properties props = new Properties();
		//props.put(PersistenceUnitProperties.TARGET_SERVER,TargetServer.WebLogic_10);
		return props;
	} 
	private static Properties _loadBBDDVendorProperties(final DBSpec spec) {
		Properties props = new Properties();
		
		props.put(PersistenceUnitProperties.TARGET_DATABASE,spec.getVendor().getCode());	// MySQLPlatformExtension.class.getCanonicalName()		
		
		// used when generating schema 
		props.put(PersistenceUnitProperties.SCHEMA_DATABASE_PRODUCT_NAME,spec.getVendor().getCode());
		props.put(PersistenceUnitProperties.SCHEMA_DATABASE_MAJOR_VERSION,spec.getMajorVersion());
		props.put(PersistenceUnitProperties.SCHEMA_DATABASE_MINOR_VERSION,spec.getMinorVersion());
		
		// enable innoDB tables in MySql (needed for full-text searching)
		if (spec.getVendor().is(DBVendor.MySQL)) {
			props.put("eclipselink.ddl.default-table-suffix","engine=InnoDB");
		}
		
		return props;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Properties for creating the schema
	 * @param spec
	 * @return
	 */
	private Properties _loadSchemaCreateProperties(final DBSpec spec) {
		Properties props = new Properties();
		
		// --- Generate schema
		// see 
		//		https://wiki.eclipse.org/EclipseLink/Release/2.5/JPA21
		//		http://www.eclipse.org/eclipselink/documentation/2.5/jpa/extensions/p_ddl_generation_output_mode.htm
		//		http://wiki.eclipse.org/EclipseLink/DesignDocs/368365)
		BBDDSchemaTablesDDLAction ddlAction = this.propertyAt("persistence/schema/generationMode")
							   					  .asEnumElement(BBDDSchemaTablesDDLAction.class,
							   							  		 BBDDSchemaTablesDDLAction.NONE);
		if (ddlAction == BBDDSchemaTablesDDLAction.NONE) return props;
		
		// props.put(PersistenceUnitProperties.SCHEMA_GENERATION_CONNECTION,"");
		props.put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_DATABASE_SCHEMAS,"false");						// do NOT generate the schema
		props.put(PersistenceUnitProperties.DDL_GENERATION_MODE,PersistenceUnitProperties.DDL_BOTH_GENERATION);		// database / sql-script / both
		
		// specifies whether the creation of database artifacts is to occur on the basis of
		//		(1) the object/relational mapping metadata, 
		//		(2) DDL script, 
		//		(3) a combination (1) and (2)
       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SOURCE,"metadata");			// metadata / script / metadata-then-script / script-then-metadata
       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SOURCE,"metadata");				// metadata / script / metadata-then-script / script-then-metadata
		
		// [1] == Schema generation from the object/relational mapping metadata 
		// specifies the action to be taken by the persistence provider with regard to the database artifacts
       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_DATABASE_ACTION,ddlAction.getJpaAction());	// none / create / create-or-extend-tables / drop-and-create / drop
       	props.put(PersistenceUnitProperties.DDL_GENERATION,ddlAction.getEclipseLinkAction());				// none / create-tables / create-or-extend-tables / drop-and-create-tables

		// Generate indexes for foreign keys: http://java-persistence-performance.blogspot.com.es/2013/06/cool-performance-features-of.html
		props.put(PersistenceUnitProperties.DDL_GENERATION_INDEX_FOREIGN_KEYS,"true"); 					
		       
		// [2] == Schema generation from DDL scripts
		// specify the file system directory in which EclipseLink writes (outputs) DDL files. - See more at: http://www.eclipse.org/eclipselink/documentation/2.5/jpa/extensions/p_application_location.htm#CACHGDEJ
		String ddlScriptPath = this.propertyAt("persistence/schema/writeDDLScriptTo")
								   .asString();
		if (Strings.isNOTNullOrEmpty(ddlScriptPath)) {
			String theDDLScriptPath = Strings.of(ddlScriptPath)		// the properties specified path can have placeholders for the appCode
											 .replaceAll("\\{APPCODE\\}",this.getAppCode().asString())
											 .asString();
			props.put(PersistenceUnitProperties.APP_LOCATION,theDDLScriptPath);
			
			// specifies which scripts are to be generated by the persistence provider		
	       	props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_ACTION,"drop-and-create");		// none / create / drop-and-create / drop
			
	       	// props.put("PersistenceUnitProperties.SCHEMA_GENERATION_CREATE_SCRIPT_SOURCE","META-INF/create.sql");       	
			// props.put("PersistenceUnitProperties.SCHEMA_GENERATION_DROP_SCRIPT_SOURCE,"META-INF/drop.jdbc");
			
			
			// If JPA GENERATES the schema generation scripts to be executed later
			props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_CREATE_TARGET,this.getAppCode().asString() + "Create.sql");
			props.put(PersistenceUnitProperties.CREATE_JDBC_DDL_FILE,this.getAppCode().asString() + "Create.sql");
			
			props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SCRIPTS_DROP_TARGET,this.getAppCode().asString() + "Drop.sql");
			props.put(PersistenceUnitProperties.DROP_JDBC_DDL_FILE,this.getAppCode().asString() + "Drop.sql");
		}
		
		// [3] == Initial load script
		// If the database has to be loaded with initial data this property specifies the
		// SQL load script for database initialization
		String loadScriptPath = this.propertyAt("persistence/schema/loadScriptPath")
								    .asString();
		if (Strings.isNOTNullOrEmpty(loadScriptPath)) {
			props.put(PersistenceUnitProperties.SCHEMA_GENERATION_SQL_LOAD_SCRIPT_SOURCE,loadScriptPath);
		}
		
		// [4] == Other properties
		Properties otherProps = this.propertyAt("persistence/schema/properties[@vendor='" + spec.getVendor().getCode() + "']")
									.asProperties();
		if (otherProps != null) {
			props.putAll(otherProps);
		}
		
		
		return props;
		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unused")
	private static Properties _loadCachingProperties(final DBSpec spec) {
		// CACHING http://wiki.eclipse.org/EclipseLink/Examples/JPA/Caching -->
		// Es importante DESHABILITAR el cache en AWS ya que hay multiples instancias del servidor de apps -->
		// En caso de HABILITAR el cache en AWS hay que coordinar las caches: http://wiki.eclipse.org/EclipseLink/UserGuide/JPA/sandbox/caching/Cache_Coordination -->
		// <property name="eclipselink.cache.shared.default" value="false"/>
		return null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * see Logging: ver http://wiki.eclipse.org/EclipseLink/Examples/JPA/Logging
	 * @param spec
	 * @return
	 */
	private Properties _loadLogProperties(final DBSpec spec) {
		String xPath = "persistence/debugSQL";
		
		String logLevelName = Level.INFO.getName();
		
		// the debugSQL property might contain true/false or a log Level name (off, warn, error, etc)
		String debugSQLValue = this.propertyAt(xPath).asString("false");
		if (debugSQLValue.equalsIgnoreCase("false")) {
			logLevelName = Level.INFO.getName();
		} else if (debugSQLValue.equalsIgnoreCase("true")) {
			logLevelName = Level.FINEST.getName();
		} else {
			// the debugSQL property might contain a Level name
			try {
				logLevelName = Level.parse(debugSQLValue).getName();
			} catch(Throwable th) {
				th.printStackTrace(System.out);
				logLevelName = Level.FINEST.getName();
			}
		}
		
		// Set the log level
		Properties props = new Properties();
		
		props.put(PersistenceUnitProperties.LOGGING_LOGGER,"ServerLogger");
		props.put(PersistenceUnitProperties.LOGGING_LEVEL,logLevelName);	
		props.put(PersistenceUnitProperties.LOGGING_TIMESTAMP,"false");
		props.put(PersistenceUnitProperties.LOGGING_THREAD,"true");
		props.put(PersistenceUnitProperties.LOGGING_SESSION,"true");
		props.put(PersistenceUnitProperties.LOGGING_CONNECTION,"true");
		props.put(PersistenceUnitProperties.LOGGING_EXCEPTIONS,"true");
		props.put(PersistenceUnitProperties.LOGGING_PARAMETERS,"true");
		
		return props;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * see Logging: ver http://wiki.eclipse.org/EclipseLink/Examples/JPA/Logging
	 * @param spec
	 * @return
	 */
	private Properties _loadOtherProperties(final PersistenceUnitType unitType,
											final String persistenceUnitName,
											final DBSpec spec) {
		String xPath = "persistence/unit[@type='" + unitType + "']/properties";
		Properties props = this.propertyAt(xPath)
							   .asProperties();
		if (CollectionUtils.isNullOrEmpty(props)) {
			log.error("There're NO aditional JPA properties at {} at properties file {}",
					   xPath,super.getAppCode());
		} else {
			// Debug
			StringBuilder dbg = new StringBuilder("Aditional properties for persistence unit with name").append(persistenceUnitName).append("\n");
			for (Map.Entry<Object,Object> me : props.entrySet()) {
				dbg.append(Strings.customized("\t-{}={}\n",me.getKey(),me.getValue()));
			}
			log.warn(dbg.toString());
		}
		return props;
	}
}
