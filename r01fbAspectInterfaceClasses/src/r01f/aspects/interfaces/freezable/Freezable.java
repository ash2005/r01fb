package r01f.aspects.interfaces.freezable;



/**
 * Interfaz que indica que un objeto es "congelable" (Freezable).
 * El uso habitual es:
 * PASO 1: Crear una clase que implementa el interfaz Freezable
 * 		public class MyFreezableObj implements Freezable {
 * 		}
 * 
 * PASO 2: Establecer el estado del objeto y congelar
 * 		MyFreezableObj obj = new MyFreezableObj();
 * 		obj.setXX
 * 		obj.setYY
 * 		obj.freeze();	<-- congelar: NO se puede cambiar el estado del objeto
 * 		obj.setXX 		<-- IllegalStateException!!!
 * 					  		Una vez que el estado de un objeto se congela, NO puede ser modificado.
 * 
 * IMPORTANTE! Ver FreezableAspect
 * 		El funcionamiento se basa en ASPECT-J que "intercepta" todas las modificaciones a los 
 * 		miembros de la clase y ANTES de establecer un nuevo valor en el miembro se comprueba si
 * 		el estado del objeto está congelado
 */
public interface Freezable {
	/**
	 * Devuelve si el objeto está congelado o no
	 * @return true si está congelado
	 */
	public boolean isFrozen();
	/**
	 * Establece el estado de congelación
	 * @param value true congela / false descongela
	 */
	public void setFrozen(boolean value);
	/**
	 * Congela el estado
	 */
	public void freeze();
	/**
	 * Descongela el estado
	 */
	public void unFreeze();
}
