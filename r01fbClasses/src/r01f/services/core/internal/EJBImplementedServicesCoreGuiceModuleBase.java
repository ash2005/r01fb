package r01f.services.core.internal;

import java.util.Collection;

import com.google.inject.Module;

import r01f.guids.CommonOIDs.AppCode;

public abstract class EJBImplementedServicesCoreGuiceModuleBase
	 	      extends ServicesCoreBootstrapGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public EJBImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode) {
		super(apiAppCode);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
												   	    final Collection<? extends Module> modulesToInstall) {
		super(apiAppCode,
			  modulesToInstall);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
													    final Module m1,
													    final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,
			  otherModules);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
												   		final Module m1,final Module m2,
												   		final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,m2,
			  otherModules);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
												   		final Module m1,final Module m2,final Module m3,
												   		final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,m2,m3,
			  otherModules);
	}
	protected EJBImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
												   	    final Module m1,final Module m2,final Module m3,final Module m4,
												   		final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,m2,m3,m4,
			  otherModules);
	}
}
