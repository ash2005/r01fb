package r01f.aspects.interfaces.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Anotación que se asocia a un método para que NO se haga log de la llamada a dicho método
 * <pre class='brush:java'>
 * 		@LoggedMethodCalls(level=LogLevel.DEBUG,when=LoggedMethodCallsWhen.AROUND,
 * 						   module="[CLIENT API]",start="[START]",end="[END]",
 * 						   paramsFormatter=MyMethodsParamsFormatter.class)
 * 		public class ClientAPI {
 * 			public void myPublicMethod() {
 * 				...
 * 				@DoNotLog
 * 				public void notLoggedMethod() {
 * 					...
 * 				}
 * 			}
 * 		}
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface DoNotLog {
	/* just an interface */
}
