package r01f.services.client.internal;

import com.google.inject.Module;

/**
 * Guice {@link Module} interface extension that marks a guice module type as a client bindings module
 * (a module that usually contains aditional client bindings -those not made at {@link ServicesClientAPIBootstrapGuiceModuleBase}-)
 */
public interface ServicesClientBindingsGuiceModule
		 extends ServicesClientGuiceModule {
	/* just a marker interface */
}
