package r01f.services.core.internal;

import java.util.Collection;

import com.google.inject.Module;

/**
 * Guice {@link Module} interface extension that marks a guice module type as a CORE bootstraping module
 * This is used at application bootstraping-time at {@link ServicesCoreBootstrapModulesFinder} class to find the client
 * bootstrap module
 */
public interface ServicesCoreBootstrapGuiceModule
		 extends Module {
	/**
	 * @return a collection of the installed module types (ie dbmodule, search module, notification module, etc)
	 */
	public Collection<Class<? extends Module>> getInstalledModuleTypes();
	/**
	 * Returns true if a module of the given type or any subtype was installed
	 * @param modType
	 * @return
	 */
	public boolean isModuleInstalled(final Class<? extends Module> modType);
}
