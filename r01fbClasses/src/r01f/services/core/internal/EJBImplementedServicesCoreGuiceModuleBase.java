package r01f.services.core.internal;

import java.util.Collection;

import com.google.inject.Module;

public abstract class EJBImplementedServicesCoreGuiceModuleBase
	 	      extends ServicesCoreBootstrapGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public EJBImplementedServicesCoreGuiceModuleBase() {
		super();
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final Collection<? extends Module> modulesToInstall) {
		super(modulesToInstall);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final Module m1,
													    final Collection<? extends Module> otherModules) {
		super(m1,
			  otherModules);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final Module m1,final Module m2,
												   		final Collection<? extends Module> otherModules) {
		super(m1,m2,
			  otherModules);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final Module m1,final Module m2,final Module m3,
												   		final Collection<? extends Module> otherModules) {
		super(m1,m2,m3,
			  otherModules);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final Module m1,final Module m2,final Module m3,final Module m4,
												   		final Collection<? extends Module> otherModules) {
		super(m1,m2,m3,m4,
			  otherModules);
	}
}
