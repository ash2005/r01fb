package r01f.services.client;



import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.aopalliance.intercept.MethodInterceptor;

import r01f.services.ServicesImpl;
import r01f.services.client.internal.ServicesClientBootstrapGuiceModule;
import r01f.services.client.internal.ServicesClientProxyLazyLoaderGuiceMethodInterceptor;
import r01f.services.interfaces.ServiceInterface;

/**
 * A type that simply aggregates every fine-grained services and subservices (see {@link SubServiceInterface} and {@link ServiceInterface})
 * There are some flavors of this type:
 * <ul>
 * 		<li>{@link ServiceProxiesAggregatorForBeanImpls}: all services are proxied to it's BEAN implementation</li>
 * 		<li>{@link ServiceProxiesAggregatorForRESTImpls}: all services are proxied to it's REST implementation</li>
 * 		<li>{@link ServiceProxiesAggregatorForEJBImpls}: all services are proxied to it's EJB implementation</li>
 * 		<li>...</li>
 * </ul>
 * 
 * The proxy flavor to be used is injected at a corresponding {@link ClientAPI} sub-type:
 * <ul>
 * 		<li>{@link ClientAPIForBeanServices}: an instance of {@link ServiceProxiesAggregatorForBeanImpls} implementing type is injected</li>
 * 		<li>{@link ClientAPIForRESTServices}: an instance of {@link ServiceProxiesAggregatorForRESTImpls} implementing type is injected</li>
 * 		<li>{@link ClientAPIForEJBServices}: an instance of {@link ServiceProxiesAggregatorForEJBImpls} implementing type is injected</li>
 * 		<li>...</li> 
 * </ul> 
 * 
 * Finally the client that's using a {@link ClientAPI} can determine the {@link ClientAPI} flavor to use (the mode to be used to connect to the service: REST, Bean, EJB, etc)
 * annotating the {@link ClientAPI} field to be injected with:
 * <ul>
 *		<li>Bean: binding annotated with @ClientUsesBeanServices</li>
 *		<li>REST: binding annotated with @ClientUsesRESTServices</li>
 *		<li>EJB:  binding annotated with @ClientUsesEJBServices</li>
 *		<li>...</li>
 * </ul>
 * 
 * All the above described assumes that the client code has hard-coded the {@link ClientAPI} flavor to use (Bean, REST, EJB...), 
 * BUT usually is a better approach to use a config file to set the service implementation to use (Bean, REST, EJB...)
 * This config is be done at {clientAppCode}.client.properties.xml as:
 * <pre class='brush:java'>
 *		<proxies 
 *			<proxy appCode="{coreAppCode}" id="{coreModule}" impl="REST">{a module description}</proxy>
 *			...
 *		</proxies 
 * </pre>
 * 
 * When the configured proxy to the services implementation is to be used, annotate the {@link ClientAPI} field to be injected with @ClientUsesDefaultServices 
 * (or do NOT annotate it); this way a {@link ClientAPIForDefaultServices} sub-type will be injected which in turn will be injected with a {@link ServiceProxiesAggregatorForDefaultImpls}
 * sub-type that uses the configured proxies flavors
 * 
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
 * The method interception logic is at {@link ServicesClientProxyLazyLoaderGuiceMethodInterceptor} and is configured at {@link ServicesClientBootstrapGuiceModule} 
 */
@Accessors(prefix="_")
@RequiredArgsConstructor
public abstract class ServiceProxiesAggregator {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The api implementation
	 */
	@Getter protected final ServicesImpl _servicesImpl;
}
