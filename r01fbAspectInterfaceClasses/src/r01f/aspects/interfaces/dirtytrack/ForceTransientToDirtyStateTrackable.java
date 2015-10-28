package r01f.aspects.interfaces.dirtytrack;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Anotación que hace que un miembro transient de una clase anotada con {@link ConvertToDirtyStateTrackable}
 * SI sea tenido en cuenta durante el proceso de comprobar si se ha cambiado el estado
 * <pre>
 * NOTA: Normalmente, los miembros transient NO son tenidos en cuenta en el proceso de 
 * 		 comprobar el cambio de estado
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface ForceTransientToDirtyStateTrackable {
	/* just an interface */
}
