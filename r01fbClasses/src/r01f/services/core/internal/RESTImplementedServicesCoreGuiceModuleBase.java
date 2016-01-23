package r01f.services.core.internal;

import java.util.Collection;

import com.google.inject.Module;

import r01f.guids.CommonOIDs.AppCode;

public abstract class RESTImplementedServicesCoreGuiceModuleBase
		      extends ServicesCoreBootstrapGuiceModuleBase {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public RESTImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode) {
		super(apiAppCode);
	}
	protected RESTImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
												   		 final Collection<? extends Module> modulesToInstall) {
		super(apiAppCode,
			  modulesToInstall);
	}
	protected RESTImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
													     final Module m1,
													     final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,
			  otherModules);
	}
	protected RESTImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
									   				  	 final Module m1,final Module m2,
									   				  	 final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,m2,
			  otherModules);
	}
	protected RESTImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
										   				 final Module m1,final Module m2,final Module m3,
										   				 final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,m2,m3,
			  otherModules);
	}
	protected RESTImplementedServicesCoreGuiceModuleBase(final AppCode apiAppCode,
										   				 final Module m1,final Module m2,final Module m3,final Module m4,
										   				 final Collection<? extends Module> otherModules) {
		super(apiAppCode,
			  m1,m2,m3,m4,
			  otherModules);
	}
}
