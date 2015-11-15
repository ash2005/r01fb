package r01f.services.core.internal;

import java.util.Map;

import com.google.inject.Binder;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.name.Names;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.inject.ServiceHandler;
import r01f.services.ServiceBootstrapDef;
import r01f.services.client.internal.ServicesClientInterfaceToImplOrProxyBinder;
import r01f.services.interfaces.ServiceInterface;
import r01f.util.types.collections.CollectionUtils;

/**
 * When more than a single coreAppCode / module is found in the classpath there's a big chance for a collision of 
 * binded resources like JPA's EntityManager that MUST be binded at guice's {@link PrivateModule}s 
 * (see guice multiple persist modules at https://github.com/google/guice/wiki/GuicePersistMultiModules)
 * 
 * The solution is isolate core bindings for every coreAppCode / module at a separate private module and expose only 
 * the public service interface implementations.
 */
@Slf4j
@RequiredArgsConstructor
public class ServicesCoreForAppModulePrivateGuiceModule 
     extends PrivateModule {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The core app / module definition
	 */
	private final ServiceBootstrapDef _coreBootstrapDef;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings({"rawtypes"})
	protected void configure() {
		Binder theBinder = this.binder();
		
		// Bind the CORE guice modules					
		log.warn("[START]-Binding CORE modules====================================================================================");
		boolean dbModulePresent = false;
		if (CollectionUtils.hasData(_coreBootstrapDef.getPrivateBootstrapGuiceModuleInstances())) {
			// do NOT install the REST core buide modules (they're binded at ServicesMainGuiceBootstrap, otherwise they're not visible
			// to the outside world and Guice Servlet filter cannot see REST resources)
			for (ServicesCoreBootstrapGuiceModule coreGuiceModule : _coreBootstrapDef.getPrivateBootstrapGuiceModuleInstances()) {
				// install the core bootstrap guice module
				theBinder.install(coreGuiceModule);
				
				// if the core bootstrap guice module has db persistence bindings, the jpa service handler must be exposed (see below)
				if (!dbModulePresent && coreGuiceModule.isModuleInstalled(DBPersistenceGuiceModule.class)) dbModulePresent = true;
			}
		}
		log.warn("  [END]-Binding CORE modules====================================================================================");
		
		
		log.warn("[START]-Binding serviceInterface to proxy ======================================================================");
		// Bind service interface types to bean impl or proxy types and get a Map of the bindings
		// 		- if the service bean implementation is available, the service interface is binded to the bean impl directly
		//		- otherwise, the best suitable proxy to the service implementation is binded
		ServicesClientInterfaceToImplOrProxyBinder ifaceToProxyOrImplBinder = new ServicesClientInterfaceToImplOrProxyBinder();
		Map<Class<? extends ServiceInterface>,
		    Class<? extends ServiceInterface>> serviceIfaceTypeToImplOrProxyType = ifaceToProxyOrImplBinder.bindServiceInterfacesToProxiesOrImpls(theBinder,
																	   																			  _coreBootstrapDef.getServiceInterfacesToImplAndProxiesDefs());
		
		// Expose the service interface types to bean impl or proxy types as:
		//		[1] - A MapBinder that binds the service interface type to the bean impl or proxy instance
		//			  This MapBinder is used at the API's proxy aggregator to inject the correct service interface bean impl or proxy 
		//			  (see ServicesClientProxyLazyLoaderGuiceMethodInterceptor)
		//		[2] - A direct bind of the service intereface type to the bean impl or proxy type
		MapBinder<Class,ServiceInterface> serviceIfaceTypeToImplOrProxyBinder = MapBinder.newMapBinder(theBinder,
																				 				 	   Class.class,ServiceInterface.class,
																				 				 	   Names.named(_coreBootstrapDef.getCoreAppCodeAndModule().asString()));
		int numBindings = 0;
		if (CollectionUtils.hasData(serviceIfaceTypeToImplOrProxyType)) {
			for (Map.Entry<Class<? extends ServiceInterface>,Class<? extends ServiceInterface>> me : serviceIfaceTypeToImplOrProxyType.entrySet()) {
				// a an interface to impl / proxy binding to the Map used at ServicesClientProxyLazyLoaderGuiceMethodInterceptor
				serviceIfaceTypeToImplOrProxyBinder.addBinding(me.getKey())
								 			 	   .to(me.getValue());
				// expose the service interface binding
				this.expose(me.getKey());
				
				numBindings++;
			}
		}			
		// expose the MapBinder to be injected and used at ServicesClientProxyLazyLoaderGuiceMethodInterceptor
		this.expose(new TypeLiteral<Map<Class,ServiceInterface>>() { /* nothing */ })
			.annotatedWith(Names.named(_coreBootstrapDef.getCoreAppCodeAndModule().asString()));
		log.warn("\t{} service interface to bean impl or proxy bindings exposed as {}",
				 numBindings,_coreBootstrapDef.getCoreAppCodeAndModule().asString());
		
		
		
		// expose the JPA's service handler (if available)
		if (dbModulePresent) {
			this.expose(ServiceHandler.class)
				.annotatedWith(Names.named(_coreBootstrapDef.getCoreAppCodeAndModule().asString())); 
		}
		log.warn("  [END]-Binding serviceInterface to proxy ======================================================================");
	}
}
