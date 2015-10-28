package r01f.aspects.core.logging;

import r01f.aspects.interfaces.logging.LoggedMethodCallsParamsFormatter;
import r01f.util.types.collections.CollectionUtils;


/**
 * Implementación por defecto del formateo de los parámetros de llamada 
 * a un método
 * Es utilizado en la anotación @LoggedMethodCalls que se utiliza junto con el aspecto
 * LoggedMethodCallsAspect
 */
public class LoggedMethodCallsParamsDefaultFormatter 
  implements LoggedMethodCallsParamsFormatter {

	@Override
	public String formatParams(Object... params) {
		// Devuelve una cadena con información sobre los parámetros de la llamada a un método
		String outLog = null;
		if (CollectionUtils.hasData(params)) {
			StringBuffer sb = new StringBuffer(params.length * 10);
			sb.append("> ").append(params.length).append(" params: ");
			int i=0;
			for(Object o : params) {
				if (o != null) {
					sb.append(o.getClass().getName());
				} else {
					sb.append("null");
				}
				if (i > 0 && i < params.length-1) sb.append(", ");
				i++;
			}
			outLog = sb.toString();
		}
		return outLog;
	}

}
