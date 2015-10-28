package r01f.xmlproperties;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.google.inject.BindingAnnotation;

/** 
 * Anotación que permite inyectar a la clase {@link XMLPropertiesForAppCacheImpl} el entorno donde hay que cargar las propiedades
 * Este entorno se define en una propiedad de la máquina virtual (r01Env) y se carga en el módulo guice {@link XMLPropertiesGuiceModule}
 */
@BindingAnnotation 	// this tells guice that this is an annotation used to know where to inject
@Target({ ElementType.FIELD, ElementType.PARAMETER, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@interface XMLPropertiesEnvironment {
	/* marker interface */
}
