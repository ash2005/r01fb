package r01f.marshalling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotación utilizada para señalar que un elemento de un XML que se mapea con el Marshaller
 * es directamente un XML
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlInline {
	/* empty */
}
