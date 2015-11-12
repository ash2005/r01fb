package r01f.services.core;

import r01f.services.core.internal.ServicesCoreBootstrapGuiceModule;


/**
 * Special kind of {@link ServicesCoreBootstrapGuiceModule} used to bootstap a servlet guice module
 * Note that this is NOT a full-fledged service as {@link RESTImplementedServicesCoreGuiceModuleBase}, {@link BeanImplementedServicesCoreBootstrapGuiceModuleBase} or {@link EJBImplementedServicesCoreGuiceModuleBase}
 * it's NOT used by a real client API: it's consumed by a web client like a browser so there's NO associated client-proxy
 */
public abstract class ServletImplementedServicesCoreGuiceModuleBase
		      extends ServicesCoreBootstrapGuiceModuleBase {
	/* just extend */
}
