package r01f.services;

import com.google.inject.Module;

import lombok.extern.slf4j.Slf4j;
import r01f.reflection.ReflectionException;
import r01f.reflection.ReflectionUtils;

@Slf4j
class ServiceBootstrapGuiceModuleUtils {
	/**
	 * Creates a guice module instance
	 * @param moduleType
	 * @return
	 */
	public static Module createGuiceModuleInstance(final Class<? extends Module> moduleType) {
		try {
			return ReflectionUtils.createInstanceOf(moduleType,
												    new Class<?>[] {},
													new Object[] {});
		} catch (ReflectionException refEx) {																					
			log.error("Could NOT create an instance of {} bootstrap guice module. The module MUST have a no-args constructor",moduleType);
			throw refEx;
		}
	}
}
