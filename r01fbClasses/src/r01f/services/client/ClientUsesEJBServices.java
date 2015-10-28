package r01f.services.client;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Annotation that tells guice to inject the EJB-services-backed API
 * It's used when injecting the API like: 
 * <pre class='brush:java'>
 * 		public class MyType {
 * 			@Inject @ClientUseEJBServices
 * 			private R01MClientAPI _api;
 * 		}
 * </pre>
 */
@BindingAnnotation 
@Target({ ElementType.FIELD,ElementType.PARAMETER}) 
@Retention(RetentionPolicy.RUNTIME)
public @interface ClientUsesEJBServices {
	/* nothing to do */
}
