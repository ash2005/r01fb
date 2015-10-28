package r01f.aspects.interfaces.logging;

/**
 * sl4fj NO dispone de un enum para los niveles de traza.
 * es necesario (ej: anotación LoggedMethodCalls)
 */
public enum LogLevel {
	OFF,
	TRACE, 
	DEBUG, 
	INFO,
	WARN, 
	ERROR;
}
