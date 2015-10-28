package r01f.concurrent;

public class TimeOutController {
///////////////////////////////////////////////////////////////////////////////
// CONSTRUCTOR
///////////////////////////////////////////////////////////////////////////////
	/**
	 * No instanciar métodos de esta clase.
	 */
	private TimeOutController() {
		super();
	}
///////////////////////////////////////////////////////////////////////////////
//	METODOS
///////////////////////////////////////////////////////////////////////////////
    /**
     * Ejecuta la tarea y espera los milisegundos especificados en timeout para devolver
     * Si la tarea no retorna en el número de milisegundos especificado, el thread se interrumpe
     * y se lanza una excepción
     * El llamante debe sobrecarga el método Thread.interrupt() para hacer algo 
     * que mate el thread o bien utilice el método Thread.isInterrupted();
     *
     * @param task: La tarea a ejecutar
     * @param timeout: El timeout a esperar a que el thread retorne. 0 significa esperar para siempre
     * @throws TimeoutException si se cumple el timeout y el thread no ha retornado.
     */
    public static void execute(final Thread task,final long timeout) throws TimeoutException {
        task.start();
        try {
            task.join(timeout);
        } catch (InterruptedException e) {
            /* Si alguien interrumpe, el sabrá que está haciendo */
        }
        if (task.isAlive()) {
            task.interrupt();
            throw new TimeoutException();
        }
    }
    /**
     * Ejecuta la tarea en un nuevo deamon Thread y espera el tiempo especificado
     * en el timeout.
     * @param task: La tarea a ejecutar
     * @param timeout: El timeout en milisegundos. 0 significa esperar para siempre
     * @throws TimeoutException: si pasa el tiempo especificado y la tarea no ha retornado
     */
    public static void execute(final Runnable task,final long timeout) throws TimeoutException {
        Thread t = new Thread(task,"Timeout guard");	// Convertir en thread
        t.setDaemon(true);
        execute(t,timeout);	// Ejecutarla igual que el metodo anterior
    }
    /**
     * Signals that the task timed out.
     */
    public static class TimeoutException 
    			extends Exception {
        private static final long serialVersionUID = 273515211929706600L;

        /** Create an instance */
        public TimeoutException() {
        }
    }
}
