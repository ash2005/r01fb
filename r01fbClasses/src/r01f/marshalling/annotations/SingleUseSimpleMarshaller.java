package r01f.marshalling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import r01f.marshalling.simple.SimpleMarshallerBuilder;

import com.google.inject.BindingAnnotation;

/**
 * Anotación que permite especificar a {@link com.google.inject.Guice} que el {@link Marshaller}
 * a inyectar es el {@link SimpleMarshallerBuilder}
 * <code>
 * 		@SimpleMarshaller Marshaller myMarshaller
 * </code>
 */
@BindingAnnotation 
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD }) 
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleUseSimpleMarshaller {
	/* empty */
}
