package r01f.services.core.internal;

import com.google.inject.PrivateBinder;
import com.google.inject.PrivateModule;

import r01f.services.ServicesMainGuiceBootstrap;

/**
 * Each {@link ServicesCoreBootstrapGuiceModule}s is installed in it's own guice's {@link PrivateModule} (see {@link ServicesMainGuiceBootstrap} and {@link ServicesCoreForAppModulePrivateGuiceModule})
 * ... so any binding inside the {@link ServicesCoreBootstrapGuiceModule} is NOT VISIBLE from the outside world.
 * During tests sometimes an internal binding is needed so a way to allow a {@link ServicesCoreBootstrapGuiceModule} expose some of it's internal bindings
 * is let the module implement this interface
 */
public interface ServicesCoreBootstrapGuiceModuleExposesBindings {
	/**
	 * Provides the {@link ServicesCoreBootstrapGuiceModule} with the {@link PrivateBinder} to give him
	 * the oportunity to expose something to the outside world
	 * @param privateBinder
	 */
	public void exposeBindings(final PrivateBinder privateBinder);
}
