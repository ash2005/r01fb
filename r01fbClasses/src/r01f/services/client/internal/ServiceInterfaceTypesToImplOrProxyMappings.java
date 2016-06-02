package r01f.services.client.internal;

import r01f.services.core.internal.ServicesCoreForAppModulePrivateGuiceModule;

/**
 * Service interface type to service impl or proxy matching (binding): 
 * see {@link ServicesCoreForAppModulePrivateGuiceModule}
 * 		- if the service bean implementation is available, the service interface is binded to the bean impl directly
 *		- otherwise, the best suitable proxy to the service implementation is binded
 */
public interface ServiceInterfaceTypesToImplOrProxyMappings {
	// just a marker interface
}
