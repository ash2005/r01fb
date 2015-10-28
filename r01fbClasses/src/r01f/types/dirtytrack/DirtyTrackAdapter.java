package r01f.types.dirtytrack;

import java.util.Collection;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.interfaces.ChangesTrackableCollection;
import r01f.types.dirtytrack.interfaces.ChangesTrackableMap;


/**
 * Clase adaptadora que transforma un objeto anotado con {@link r01f.aspects.dirtytrack.ConvertToDirtyStateTrackable}
 * a una clase que implementa un interfaz para acceder a los métodos de DirtyTracking
 * <pre>
 * IMPORTANTE:	El objeto a adaptar ha de implementar el interfaz {@link DirtyStateTrackable}
 * 				Lo normal es que para implementar este interfaz:
 * 						1.- El objeto se anote con {@link r01f.aspects.dirtytrack.ConvertToDirtyStateTrackable}
 * 						2.- Se aplique el aspecto DirtyStateTrackable: hay que recubrir el objeto 
 * 							con el AspectJ weaver
 * </pre>
 * El uso habitual es:
 * <pre class='brush:java'>
 * 		MyTrackableObj myObj = new MyTrackableObj();
 * 		myObj.setField("aaa");
 * 		
 * 		DirtyStateTrackable myObjTrackable = DirtyTrackAdapter.adapt(myObj);
 * 		myObjTrackable.startTrackingChanges(true);
 * 
 * 		myObj.addMyEntry("a",new MyChildObj("ccc"));
 * 
 * 		System.out.println("Dirty? " + myObjTrackable.isDirty());
 * </pre>
 */
@Slf4j
public class DirtyTrackAdapter {
	/**
	 * Adapta un objeto a {@link DirtyStateTrackable}
	 * @param object el objeto a adaptar
	 * @return el acceso al interfaz {@link DirtyStateTrackable} del objeto
	 */
	public static <T> DirtyStateTrackable adapt(final T object) {
		try {
			return (DirtyStateTrackable)object;
		} catch(ClassCastException ccEx) {
			log.error("{} canot be cast-ed to {}: maybe it's not annotated as @{} or maybe weaving is not in use, add -javaagent:aspectjweaver.jar to the VM start command",
					  object.getClass(),DirtyStateTrackable.class,ConvertToDirtyStateTrackable.class.getName());
			throw ccEx;
		}
	}
	/**
	 * Adapta un mapa a {@link ChangesTrackableMap} que permite saber si un mapa
	 * tiene cambios
	 * @param map el mapa a adaptar
	 * @return el acceso al interfaz {@link ChangesTrackableMap}
	 */
	@SuppressWarnings("unchecked")
	public static <K,V> ChangesTrackableMap<K,V> adapt(final Map<K,V> map) {
		return (ChangesTrackableMap<K,V>)map;
	}
	/**
	 * Adapta una colección (List o Set) a {@link ChangesTrackableCollection} que permite saber
	 * si una colección tiene cambios
	 * @param col la colección a adaptar
	 * @return el acceso al interfaz {@link ChangesTrackableCollection}
	 */
	@SuppressWarnings("unchecked")
	public static <V> ChangesTrackableCollection<V> adapt(final Collection<V> col) {
		return (ChangesTrackableCollection<V>)col;
	}
}
