package r01f.internal;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Key;
import com.google.inject.servlet.GuiceServletContextListener;

import lombok.extern.slf4j.Slf4j;
import r01f.concurrent.ExecutorServiceManager;
import r01f.guids.CommonOIDs.AppCode;
import r01f.inject.ServiceHandler;
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
public abstract class ServletContextListenerBase 
	          extends GuiceServletContextListener {	
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static final String SERVLET_CONTEXT_ATTR_NAME = "R01_DAEMON_EXECUTOR_SERVICE";

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private final Collection<Key<? extends ServiceHandler>> _hasServiceHandlerTypes;
	
	private boolean _injectorCreated = false;
/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected ServletContextListenerBase() {
		_hasServiceHandlerTypes = null;
	}
	protected ServletContextListenerBase(final Key<? extends ServiceHandler>... hasServiceHandlerTypes) {
		_hasServiceHandlerTypes = CollectionUtils.hasData(hasServiceHandlerTypes) ? Arrays.asList(hasServiceHandlerTypes) : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Overridden methods of GuiceServletContextListener
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		log.warn("\n\n\n=============================================\n" + 
				       "Loading {} Servlet Context...\n" + 
				       "=============================================\n",
				 this.getClass().getSimpleName());
		
		super.contextInitialized(servletContextEvent);
		
		// Init JPA's Persistence Service, Lucene indexes and everything that has to be started
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		if (CollectionUtils.hasData(_hasServiceHandlerTypes)) {
			for (Key<? extends ServiceHandler> hasServiceHandlerType : _hasServiceHandlerTypes) {
				ServiceHandler serviceHandler = this.getInjector()
													.getInstance(hasServiceHandlerType);
				log.warn("\t--START SERVICE using {} type: {}",ServiceHandler.class.getSimpleName(),hasServiceHandlerType);
				serviceHandler.start();
			}
		}
	}
	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		log.warn("DESTROYING Servlet Context... closing search engine indexes if they are in use, release background jobs threads and so on...");
		
		// Close JPA's Persistence Service, Lucene indexes and everything that has to be closed
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		if (CollectionUtils.hasData(_hasServiceHandlerTypes)) {
			for (Key<? extends ServiceHandler> hasServiceHandlerType : _hasServiceHandlerTypes) {
				ServiceHandler serviceHandler = this.getInjector()
													.getInstance(hasServiceHandlerType);
				if (serviceHandler != null) {
					log.warn("\t--END SERVICE {} type: {}",ServiceHandler.class.getSimpleName(),hasServiceHandlerType);
					serviceHandler.stop();
				}
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
