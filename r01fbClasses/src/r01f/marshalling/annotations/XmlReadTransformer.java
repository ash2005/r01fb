package r01f.marshalling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlReadCustomTransformer;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface XmlReadTransformer {
	/**
	 * Type in charge of transforming the xml into a java object
	 * The type MUST implement {@link XmlReadCustomTransformer} interface
	 */
	@SuppressWarnings("rawtypes")
	Class<? extends XmlReadCustomTransformer> using();
}
