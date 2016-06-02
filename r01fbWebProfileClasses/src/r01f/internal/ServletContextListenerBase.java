package r01f.internal;

import java.util.Arrays;
import java.util.Collection;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.servlet.GuiceServletContextListener;

import lombok.extern.slf4j.Slf4j;
import r01f.services.ServicesInitData;
import r01f.services.ServicesLifeCycleUtil;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

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
	private final Collection<ServicesInitData> _servicesInitData;
	private final Collection<Module> _commonClientBindingModules;
	
	private Injector _injector;
/////////////////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	protected ServletContextListenerBase(final ServicesInitData initData,
										 final Module... commonClientBindingModules ) {
		this(Lists.newArrayList(initData),
			 commonClientBindingModules);
	}
	protected ServletContextListenerBase(final Collection<ServicesInitData> initData,
										 final Module... commonClientBindingModules ) {
		this(initData,
			 CollectionUtils.hasData(commonClientBindingModules) ? Arrays.asList(commonClientBindingModules) : null);
	}
	protected ServletContextListenerBase(final Collection<ServicesInitData> initData,
										 final Collection<Module> commonClientBindingModules ) {
		if (CollectionUtils.isNullOrEmpty(initData)) throw new IllegalArgumentException();
		_servicesInitData = initData;
		_commonClientBindingModules = commonClientBindingModules;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Overridden methods of GuiceServletContextListener
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	protected Injector getInjector() {
		if (_injector == null) {
			_injector = ServicesLifeCycleUtil.createGuiceInjector(_servicesInitData)
											 .withCommonBindingModules(_commonClientBindingModules);
		} else {
			log.warn("The Guice Injector is already created!!!");
		}
		return _injector;
	}
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		log.warn("============================================="); 
		log.warn("Loading {} Servlet Context with {}...",
				 servletContextEvent.getServletContext().getContextPath(),
				 this.getClass().getSimpleName());
		log.warn("=============================================");
				 
		super.contextInitialized(servletContextEvent);
		
		// Init JPA's Persistence Service, Lucene indexes and everything that has to be started
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesLifeCycleUtil.startServices(_injector);
	}
	@Override
	public void contextDestroyed(final ServletContextEvent servletContextEvent) {
		log.warn("=============================================");
		log.warn("DESTROYING {} Servlet Context with {} > closing search engine indexes if they are in use, release background jobs threads and so on...",
				 servletContextEvent.getServletContext().getContextPath(),
				 this.getClass().getSimpleName());
		log.warn("=============================================");
		
		// Close JPA's Persistence Service, Lucene indexes and everything that has to be closed
		// (see https://github.com/google/guice/wiki/ModulesShouldBeFastAndSideEffectFree)
		ServicesLifeCycleUtil.stopServices(_injector);
		
		// finalize
		super.contextDestroyed(servletContextEvent); 
		
		log.warn("============================================="); 
		log.warn("{} Servlet Context DESTROYED!!...",
				 servletContextEvent.getServletContext().getContextPath());
		log.warn("=============================================");
	}
}
