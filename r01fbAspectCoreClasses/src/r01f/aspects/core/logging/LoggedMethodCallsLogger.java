package r01f.aspects.core.logging;

import java.lang.reflect.Method;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Enums;

import r01f.aspects.interfaces.logging.LogLevel;
import r01f.aspects.interfaces.logging.LoggedMethodCalls;
import r01f.aspects.interfaces.logging.LoggedMethodCallsParamsFormatter;
import r01f.aspects.interfaces.logging.LoggedMethodCallsWhen;
import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;

/**
 * Clase utilizada en el aspecto LoggedMethodCallsAspect y que se encarga de todas
 * las tareas de logging
 */
public class LoggedMethodCallsLogger {
/////////////////////////////////////////////////////////////////////////////////////////
//  Clase que se construye en el aspecto LoggedMethodCallsAspect y que se encarga
//	del logging
/////////////////////////////////////////////////////////////////////////////////////////	
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	public static class LoggedMethodCallsLogs {
		private final Logger _logger;
		private final LogLevel _level;
		private final String _beginMethodCallLog;
		private final String _endMethodCallLog;
		
		public void beginMethodCall() {
			_doLog(_logger,_level,_beginMethodCallLog);
		}
		public void endMethodCall() {
			_doLog(_logger,_level,_endMethodCallLog);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS AUXILIARES PARA HACER EL LOG
/////////////////////////////////////////////////////////////////////////////////////////	
	public static LoggedMethodCallsLogs getLogger(final Method method,final Object... params) {
		LoggedMethodCallsLogs outLogs = null;
		
		Class<?> declaringClass = method.getDeclaringClass();
		LoggedMethodCalls loggedAnnot = ReflectionUtils.typeAnnotation(declaringClass,
																	   LoggedMethodCalls.class);
		// Obtener información de la anotación @R01MMethodCallsLogged para componer el mensaje de log
		Class<?> logType = declaringClass;
		LogLevel logLevel = loggedAnnot.level();
		Logger logger = LoggerFactory.getLogger(logType);
		
		// Componer el mensaje de log
		if (_isLogEnabled(logger,logLevel)) {
			String module = loggedAnnot.module();
			String start = loggedAnnot.start();
			String end = loggedAnnot.end();
			int indent = loggedAnnot.indent();
		
			
			// Formatear los parametros
			LoggedMethodCallsParamsFormatter paramsFormatter = ReflectionUtils.createInstanceOf(loggedAnnot.paramsFormatter());
			
			String paramsFormatted = paramsFormatter != null ? paramsFormatter.formatParams(params) 
															 : "";
			if (Strings.isNullOrEmpty(paramsFormatted)) paramsFormatted = "";
			
			// Componer el mensaje
			String indentStr = indent > 0 ? _tabs(indent) : ""; 
					
			String msgStart = loggedAnnot.when() == LoggedMethodCallsWhen.AROUND 
					       || loggedAnnot.when() == LoggedMethodCallsWhen.BEGIN ? Strings.of("{}{}{}: {} {}")
									 													 .customizeWith(indentStr,start,module,method.getName(),paramsFormatted)
									 													 .asString()
									 											: null;
			String msgEnd = loggedAnnot.when() == LoggedMethodCallsWhen.AROUND 
					     || loggedAnnot.when() == LoggedMethodCallsWhen.END ? Strings.of("{}{}{}: {} {}")
									 												 .customizeWith(indentStr,end,module,method.getName(),paramsFormatted)
									 												 .asString()
									 											: null;
			outLogs = new LoggedMethodCallsLogs(logger,logLevel,
							   				 	msgStart,msgEnd);
		}
		return outLogs;
	}
	/**
	 * Hace log a partir de los logs obtenidos en el metodo R01MMethodCallsLogger.composeLogs
	 * @param logger el logger
	 * @param logs los logs
	 */
	private static void _doLog(final Logger logger,final LogLevel level,
							   final String msg) {
		if (Strings.isNullOrEmpty(msg) || level == LogLevel.OFF) return;
		if (level == LogLevel.TRACE && logger.isTraceEnabled()) {
			logger.trace(msg);
		} else if (level == LogLevel.DEBUG && logger.isDebugEnabled()) {
			logger.debug(msg);
		} else if (level == LogLevel.INFO && logger.isInfoEnabled()) {
			logger.info(msg);
		} else if (level == LogLevel.WARN && logger.isWarnEnabled()) {
			logger.warn(msg);
		} else if (level == LogLevel.ERROR && logger.isErrorEnabled()) {
			logger.error(msg);
		}
	}
	/**
	 * Devuelve si están habilitadas las trazas en el logger
	 * @param logger el logger
	 * @param level el nivel de log
	 */
	private static boolean _isLogEnabled(final Logger logger,
								 		 final LogLevel level) {
		if (level == LogLevel.OFF) return false;
		if (level == LogLevel.TRACE && logger.isTraceEnabled()) return true;
		if (level == LogLevel.DEBUG && logger.isDebugEnabled()) return true;
		if (level == LogLevel.INFO && logger.isInfoEnabled()) return true;
		if (level == LogLevel.WARN && logger.isWarnEnabled()) return true;
		if (level == LogLevel.ERROR && logger.isErrorEnabled()) return true;
		return false;
	}
	/**
	 * Crea una cadena con el número de tabs que se indica
	 * @param indent el número de tabs
	 * @return la cadena con los tabs
	 */
	private static String _tabs(final int indent) {
		char[] outTabs = new char[indent];
		for (int i=0; i<indent; i++) outTabs[i] = '\t';
		return new String(outTabs);
	}
}
