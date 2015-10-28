package r01f.services.interfaces;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation that tells the system (see R01MInjector type) that a type is a service interface (see {@link ServiceInterface})
 * {@link ServiceInterface}s in turn can be composed by (aggregates) {@link SubServiceInterface}s that are "portions"
 * of the aggregated {@link ServiceInterface}
 * Those {@link SubServiceInterface}s MUST be annotated with {@link SubServiceInterfaceFor} annotation
 */
@BindingAnnotation 
@Target({ ElementType.TYPE }) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ServiceInterfaceFor {
	/**
	 * The appCode
	 */
	String appCode();
	/**
	 * The app module
	 */
	String module();
}
