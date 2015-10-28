package r01f.aspects.interfaces.logging;



/**
 * Implementación simple del formateo de los parámetros de llamada 
 * a un método
 * Es utilizado en la anotación @LoggedMethodCalls que se utiliza junto con el aspecto
 * LoggedMethodCallsAspect
 */
public class LoggedMethodCallsParamsVoidFormatter 
  implements LoggedMethodCallsParamsFormatter {

	@Override
	public String formatParams(Object... params) {
		return "";
	}

}
