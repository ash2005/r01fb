package r01f.services.core.internal;

import java.util.Collection;

import com.google.inject.Module;

import r01f.reflection.ReflectionUtils;
import r01f.util.types.collections.CollectionUtils;
import r01f.util.types.collections.Lists;

abstract class ServicesCoreBootstrapGuiceModuleBase
    implements ServicesCoreBootstrapGuiceModule {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return a collection of the installed module types (ie dbmodule, search module, notification module, etc)
	 * 		   (it's used to detect whether a certain guice module type is present)
	 */
	protected final Collection<Class<? extends Module>> _installedModuleTypes;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public ServicesCoreBootstrapGuiceModuleBase() {
		_installedModuleTypes = Lists.newArrayList();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ServicesCoreBootstrapGuiceModule
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Collection<Class<? extends Module>> getInstalledModuleTypes() {
		return _installedModuleTypes;
	}
	@Override
	public boolean isModuleInstalled(final Class<? extends Module> modType) {
		if (CollectionUtils.isNullOrEmpty(_installedModuleTypes)) return false;
		
		boolean outInstalled = false;
		for (Class<? extends Module> mType : _installedModuleTypes) {
			if (ReflectionUtils.isSubClassOf(mType,modType)) {
				outInstalled = true;
				break;
			}
		}
		return outInstalled;
	}
}
