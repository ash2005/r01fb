package r01f.aspects.interfaces.logging;

/**
 * Interfaz que han de cumplir los tipos responsables de "pintar" log sobre los parámetros
 * de las llamadas a métodos
 * Implementaciones concretas de este interfaz se utilizan en la anotación @LoggedMethodCalls
 * Ej:
 * <pre class='brush:java'>
 * 		@LoggedMethodCalls(level=LogLevel.DEBUG,
 * 						   module="[CLIENT API]",start="[START]",end="[END]",
 * 						   paramsFormatter=MyMethodParamsFormatter.class)
 * 		public class R01MClientAPI {
 * 			public void myPublicMethod() {
 * 				...
 * 			}
 * 		}
 * </pre>
 */
public interface LoggedMethodCallsParamsFormatter {
	/**
	 * Formatea los parámetros de un método
	 * @param params los parámetros
	 * @return el log
	 */
	public String formatParams(Object... params);
}
