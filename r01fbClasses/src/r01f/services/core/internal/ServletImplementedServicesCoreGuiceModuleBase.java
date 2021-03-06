package r01f.services.core.internal;

import java.util.Collection;

import com.google.inject.Module;

/**
 * Special kind of {@link ServicesCoreBootstrapGuiceModule} used to bootstap a servlet guice module
 * Note that this is NOT a full-fledged service as {@link RESTImplementedServicesCoreGuiceModuleBase}, {@link BeanImplementedServicesCoreBootstrapGuiceModuleBase} or {@link EJBImplementedServicesCoreGuiceModuleBase}
 * it's NOT used by a real client API: it's consumed by a web client like a browser so there's NO associated client-proxy
 */
public abstract class ServletImplementedServicesCoreGuiceModuleBase
		      extends ServicesCoreBootstrapGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServletImplementedServicesCoreGuiceModuleBase() {
		super();
	}
	protected ServletImplementedServicesCoreGuiceModuleBase(final Collection<? extends Module> modulesToInstall) {
		super(modulesToInstall);
	}
	protected ServletImplementedServicesCoreGuiceModuleBase(final Module m1,
															final Collection<? extends Module> otherModules) {
		super(m1,
			  otherModules);
	}
	protected ServletImplementedServicesCoreGuiceModuleBase(final Module m1,final Module m2,
												   			final Collection<? extends Module> otherModules) {
		super(m1,m2,
			  otherModules);
	}
	protected ServletImplementedServicesCoreGuiceModuleBase(final Module m1,final Module m2,final Module m3,
												   			final Collection<? extends Module> otherModules) {
		super(m1,m2,m3,
			  otherModules);
	}
	protected ServletImplementedServicesCoreGuiceModuleBase(final Module m1,final Module m2,final Module m3,final Module m4,
												   			final Collection<? extends Module> otherModules) {
		super(m1,m2,m3,m4,
			  otherModules);
	}
}
