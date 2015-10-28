package r01f.aspects.interfaces.logging;

/**
 * Enum que se utiliza en la anotación @LoggedMethodCalls para indicar dónde hay que hacer log:
 */
public enum LoggedMethodCallsWhen {
	BEGIN,		// principio del método
	END,		// final del método
	AROUND;		// al principio y al final del método
}
