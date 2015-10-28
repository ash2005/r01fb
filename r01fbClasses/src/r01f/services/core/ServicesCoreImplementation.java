package r01f.services.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation that tells guice that some bean is the implementation of some services
 * It's used when injecting the API with the bean implementation: 
 * <pre class='brush:java'>
 * 		public class MyType {
 * 			@Inject @ServicesCoreImplementation	// inject the api bean implementation
 * 			private R01MSomeServicesAPI _api;
 * 		}
 * </pre>
 */
@BindingAnnotation 
@Target({ ElementType.FIELD,ElementType.PARAMETER}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ServicesCoreImplementation {
	/* nothing to do */
}
