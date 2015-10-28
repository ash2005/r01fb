package r01f.services.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation that tells guice to inject the default services API
 * This could be BEAN services, REST services or whatever it's configured
 * (see {@link R01MInjector})
 * It's used when injecting the API like: 
 * <pre class='brush:java'>
 * 		public class MyType {
 * 			@Inject @ClientUsesDefaultServices
 * 			private R01MClientAPI _api;
 * 		}
 * </pre>
 */
@BindingAnnotation 
@Target({ ElementType.FIELD,ElementType.PARAMETER}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientUsesDefaultServices {
	/* nothing to do */
}
