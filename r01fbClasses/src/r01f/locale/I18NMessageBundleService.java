package r01f.locale;



import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import r01f.bundles.ResourceBundleMissingKeyBehaviour;

/**
 * Anotación que sirve para inyectar un objteo {@link I18NService} en cualquier clase de forma que se facilita el
 * acceso a los mensajes.
 * ver {@link I18NService} para saber más sobre el funcionamiento
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface I18NMessageBundleService {
    /**
     * Cadena de bundles en los que buscar una clave
     * Cuando se pide un mensaje por su clave, se busca en los bundles indicados en orden hasta que se 
     * encuentra dicha clave
     */
    String[] chain() default {"default"};	// si no se indica una cadena devuelve un bundle llamado "default"
    /**
     * Comportamiento en caso de que no se encuentre la clave en ninguno de los bundles
     * de la cadena
     */
    ResourceBundleMissingKeyBehaviour missingKeyBehaviour() ;//default ResourceBundleMissingKeyBehaviour.THROW_EXCEPTION;
}
