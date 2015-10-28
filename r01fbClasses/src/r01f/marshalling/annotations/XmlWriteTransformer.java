package r01f.marshalling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlWriteCustomTransformer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlWriteTransformer {
	/**
	 * Clase que se encarga de la transformación de objeto java a xml
	 * y que implementa el interfaz XmlCustomTransformers.XmlWriteTransformer
	 */
	Class<? extends XmlWriteCustomTransformer> using();
}
