package r01f.types.dirtytrack.interfaces;

import java.util.Set;

public interface ChangesTrackableLazyCollection<V> 
         extends ChangesTrackableCollection<V> {
	/**
	 * Devuelve una coleción con las entradas que NO son nuevas ni se han eliminado
	 * de la colección original, es decir, los elementos originales, pero SOLO de entre 
	 * aquellos que están CARGADAS en la colección debido a que se han accedido en algún 
	 * momento
	 * @return las entradas NO nuevas ni eliminadas (las originales)
	 */
	public Set<V> notNewOrRemovedEntries(boolean onlyLoaded);

}
