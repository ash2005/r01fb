package r01f.marshalling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/**
 * Anotación que permite especificar a {@link com.google.inject.Guice} que el {@link Marshaller}
 * a inyectar es el {@link JaxbMarshaller}
 * <pre class='brush:java'>
 * 		public class MyService {
 * 			@Inject @SingleUseJaxbMarshaller Marshaller myMarshaller
 * 		}
 * </pre>
 */
@BindingAnnotation 
@Target({ ElementType.FIELD, ElementType.PARAMETER }) 
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleUseJaxbMarshaller {
	/* empty */
}
