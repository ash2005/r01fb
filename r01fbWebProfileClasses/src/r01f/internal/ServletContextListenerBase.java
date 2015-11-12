package r01f.internal;

import java.util.Collection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Key;
import com.google.inject.name.Names;
import com.google.inject.servlet.GuiceServletContextListener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.ExecutorServiceManager;
import r01f.guids.CommonOIDs.AppCode;
import r01f.inject.ServiceHandler;
import r01f.services.interfaces.IndexManagementServices;
import r01f.util.types.collections.CollectionUtils;

/**
 * Extends {@link GuiceServletContextListener} (that in turn extends {@link ServletContextListener})
 * to have the opportunity to:
 * <ul>
 * 	<li>When starting the web app: start JPA service</li>
 * 	<li>When closing the web app: stop JPA service and free lucene resources (the index writer)</li>
 * </ul>
 * If this is NOT done, an error is raised when re-deploying the application because lucene index
 * are still opened by lucene threads
 * This {@link ServletContextListener} MUST be configured at web.xml removing the default {@link ServletContextListener}
 * (if it exists)
 * <pre class='brush:xml'>
 *		<listener>
 *			<listener-class>r01e.rest.R01VRESTGuiceServletContextListener</listener-class>
 *		</listener>
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
public abstract class ServletContextListenerBase 
	          extends GuiceServletContextListener {	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static final String SERVLET_CONTEXT_ATTR_NAME = "R01_DAEMON_EXECUTOR_SERVICE";

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final Class<? extends IndexManagementServices> _indexManagementType;
	private final Collection<String> _jpaPersistenceUnits;	// the names of the JPA Service to be started & stopped (see persistence.xml)
	
	private boolean _injectorCreated = false;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  Overridden methods of GuiceServletContextListener
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		log.warn("\n=============================================\n" + 
				   "Servlet Context LOADED!!...\n" + 
				   "=============================================\n\n\n\n");
		
		super.contextInitialized(servletContextEvent);
		
		// Init the jpa's Persistence service
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		if (CollectionUtils.hasData(_jpaPersistenceUnits)) {
			for (String jpaPersistenceUnit : _jpaPersistenceUnits) {
				log.warn("\t--Init JPA's PersistenceService....");
				ServiceHandler jpaServiceHandler = this.getInjector()
															.getInstance(Key.get(ServiceHandler.class,
																		  		 Names.named(jpaPersistenceUnit)));
				jpaServiceHandler.start();
			}
		}
	}
	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		log.warn("DESTROYING Servlet Context... closing search engine indexes if they are in use, release background jobs threads and so on...");
		
		// Close JPA's Persistence Service
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		if (CollectionUtils.hasData(_jpaPersistenceUnits)) {
			for (String jpaPersistenceUnit : _jpaPersistenceUnits) {
				log.warn("\t--Closing JPA's PersistenceService....");
				ServiceHandler jpaServiceHandler = this.getInjector()
															.getInstance(Key.get(ServiceHandler.class,
																				 Names.named(jpaPersistenceUnit)));
				jpaServiceHandler.stop();
				log.warn("\t--Closed OK Persistence Servive name {} !!!",jpaPersistenceUnit);
			}
		} else {
			log.warn("\t--NO JPA PersistenceService to close!!");
		}
		
		// Close index
		if (_indexManagementType != null) {
			log.warn("\t--Closing indexer");
			ServiceHandler indexMgr = this.getInjector()
												.getInstance(_indexManagementType);
			if (indexMgr != null) {
				log.warn("\t--Closing indexers....");
				indexMgr.stop();
			} else {
				log.warn("\t--NO indexers to close!!");
			}
		}
		
		// Stop background jobs
		log.warn("\t--Release background threads");
		ServiceHandler execSrvMgr = this.getInjector().getInstance(ExecutorServiceManager.class);	// binded at BeanServicesBootstrapGuiceModuleBase
		if (execSrvMgr != null) {
			execSrvMgr.stop();	
		} else {
			log.warn("\t--NO executor services to close!!");
		}
		
		// finalize
		super.contextDestroyed(servletContextEvent); 
		
		log.warn("\n=============================================\n" + 
				   "Servlet Context DESTROYED!!...\n" + 
				   "=============================================\n\n\n\n");
	}
	/**
	 * Simply logs the injector creation
	 * @param appCode
	 */
	protected void _logIfInjectorDidntExist(final AppCode apiAppCode,
											final AppCode coreAppCode,
											final String appName) {
		if (!_injectorCreated) {
			log.warn("\n\n\n\n==============================================================\n"
						   + "========== [{}: {} BootStrapping]\n"
						   + "==============================================================\n",
						   coreAppCode,appName);
			log.warn("CREATING {} CLIENT GUICE Injector.............",apiAppCode);
			_injectorCreated = true;
		}
	}
}
