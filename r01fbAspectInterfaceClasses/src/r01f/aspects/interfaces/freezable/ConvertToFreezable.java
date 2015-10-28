package r01f.aspects.interfaces.freezable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
/**
 * Anotación que indica que un objeto es "congelable" (Freezable).
 * 
 * IMPORTANTE! Ver FreezableAspect
 * 		El funcionamiento se basa en ASPECT-J que "intercepta" todas las modificaciones a los 
 * 		miembros de la clase y ANTES de establecer un nuevo valor en el miembro se comprueba si
 * 		el estado del objeto está congelado
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ConvertToFreezable {
	/* just an interface */
}
