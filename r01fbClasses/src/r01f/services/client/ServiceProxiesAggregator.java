package r01f.services.client;



import org.aopalliance.intercept.MethodInterceptor;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.services.client.internal.ServicesClientAPIBootstrapGuiceModuleBase;
import r01f.services.client.internal.ServicesClientProxyLazyLoaderGuiceMethodInterceptor;
import r01f.services.interfaces.ServiceInterface;

/**
 * A type that simply aggregates every fine-grained services and subservices (see {@link SubServiceInterface} and {@link ServiceInterface})
 * The proxy is injected at a {@link ClientAPI} sub-type:
 * 
 * IMPORTANT!
 * ==========
 * Types extending this base type provides fine-grained access to service proxies; to do so they hold service proxies field instances
 * (subtypes of {@link ServiceInterface} or {@link SubServiceInterface}) that are lazily loaded:
 * <pre class='brush:java'>
 * 		public class MyServicesClientProxy
 * 			 extends ServicesClientProxy {
 * 
 * 			@Getter private MyFineGrainedService _serviceProxy;	<-- an instance of {@link ServiceInterface} or {@link SubServiceInterface}
 *
 * 		} 
 * </pre>
 * This lazy-load initialization is avoided using a GUICE {@link MethodInterceptor} that interecepts fine-grained service proxy accessor method calls
 * and initializes the proxy instance
 * The method interception logic is at {@link ServicesClientProxyLazyLoaderGuiceMethodInterceptor} and is configured at {@link ServicesClientAPIBootstrapGuiceModuleBase} 
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class ServiceProxiesAggregator {
	// nothing
}
