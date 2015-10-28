package r01f.marshalling.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to express the marshall/unmarshall format for Dates
 * The normal use is:
 * <pre class='brush:java'>
 * 		@XmlRootElement(name="myType")
 * 		public class MyType {
 * 			@XmlElement(name="myDateField") @XmlDateFormat("dd/MM/yyyy")
 * 			@Getter @Setter private Date _myDateField;
 * 		}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface XmlDateFormat {
	String value();
}
