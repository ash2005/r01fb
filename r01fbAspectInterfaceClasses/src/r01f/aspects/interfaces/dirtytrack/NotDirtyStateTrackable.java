package r01f.aspects.interfaces.dirtytrack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Anotación que hace que un miembro de una clase anotada con {@link ConvertToDirtyStateTrackable}
 * NO sea tenido en cuenta durante el proceso de comprobar si se ha cambiado el estado
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface NotDirtyStateTrackable {
	/* just an interface */
}
