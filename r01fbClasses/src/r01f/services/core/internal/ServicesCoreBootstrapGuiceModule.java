package r01f.services.core.internal;

import com.google.inject.Module;

/**
 * Guice {@link Module} interface extension that marks a guice module type as a CORE bootstraping module
 * This is used at application bootstraping-time at {@link ServicesCoreBootstrap} class to find the client
 * bootstrap module
 */
public interface ServicesCoreBootstrapGuiceModule
		 extends Module {
	/* just a marker interface */
}
