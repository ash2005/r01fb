package r01f.aspects.interfaces.logging;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**
 * Anotación que se pone a los tipos para los que se quiere mostrar una traza justo antes 
 * de invocar a cualquier método público y justo después de terminar la invocación
 * Ej:
 * <pre class='brush:java'>
 * 		@LoggedMethodCalls(level=LogLevel.DEBUG,when=LoggedMethodCallsWhen.AROUND,
 * 						   module="[CLIENT API]",start="[START]",end="[END]",
 * 						   paramsFormatter=MyMethodsParamsFormatter.class)
 * 		public class ClientAPI {
 * 			public void myPublicMethod() {
 * 				...
 * 			}
 * 		}
 * </pre>
 * Es equivalente a:
 * <pre class='brush:java'>
 * 		@Sl4fj
 * 		public class ClientAPI {
 * 			public void myPublicMethod() {
 * 				log.debug("[CLIENT API][START]: myPublicMethod");
 * 				...
 * 				log.debug("  [CLIENT API][END]: myPublicMethod");
 * 			}
 * 		}
 * </pre>
 * es decir, "inyecta" trazas al principio de la llamada a cada método y al final de la invocación del mismo
 * de forma transparente para el desarrollador.
 * Esta anotación se utiliza en el aspecto LoggedMethodCallsAspect
 * 
 * Hay diferentes formateadores para los parámetros:
 * 		LoggedMethodCallsParamsDefaultFormatter --> Devuelve inforación sobre número y tipo de parámetros
 * 		LoggedMethodCallsParamsVoidFormatter	--> NO devuelve información sobre los parámetros
 * 		custom --> basta con proporcionar una clase que implemente el interfaz {@link LoggedMethodCallsParamsFormatter}
 * 
 * NOTA:
 * Si NO se quiere que un método sea loggeado, basta con anotarlo con @DoNotLog
 * Ej:
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
@Target(ElementType.TYPE)
public @interface LoggedMethodCalls {
	/**
	 * Nivel de la traza
	 */
	LogLevel level() default LogLevel.DEBUG;
	/**
	 * Cuándo se hace log
	 */
	LoggedMethodCallsWhen when() default LoggedMethodCallsWhen.AROUND;
	/**
	 * Módulo (aparece al principo del mensaje de log)
	 */
	String module() default "";
	/**
	 * Indentado del log (número de tabs que se insertan al principio de la traza)
	 */
	int indent() default 0;
	/**
	 * Indicador de comienzo del método
	 */
	String start() default "[START]";
	/**
	 * Indicador de fin del método
	 */
	String end() default "[END]";
	/**
	 * Formateador de los parámetros
	 */
	Class<? extends LoggedMethodCallsParamsFormatter> paramsFormatter() default LoggedMethodCallsParamsVoidFormatter.class;
}
