package r01f.aspects.core.freezable;


import java.lang.reflect.Field;

import r01f.aspects.core.util.ObjectsHierarchyModifier;
import r01f.aspects.core.util.ObjectsHierarchyModifier.StateModifierFunction;
import r01f.aspects.interfaces.freezable.Freezable;
import r01f.generics.TypeRef;

import com.google.common.base.Predicate;

/**
 * Utilidad para "congelar" una jerarquía de objetos.
 * Utiliza reflection para recorrer la jerarquía de objetos y "congelar" aquellos que son Freezable
 */
public class Freezer {
	/**
	 * Predicado para excluir algunos fields en los métodos que utilizan changeObjectHierarchyState
	 * para por ejemplo comenzar a controlar cambios en el estado o ver si el objeto está sucio
	 */
	static final Predicate<Field> _fieldAcceptCriteria = new Predicate<Field>() {
																@Override
																public boolean apply(final Field f) {
																	if (f.getDeclaringClass().getPackage().getName().startsWith("java.lang")) return false;	
																	if (f.getDeclaringClass().getPackage().getName().startsWith("com.google")) return false;
																	if (f.getName().startsWith("ajc$")) return false; 
																	if (f.getName().startsWith("_frozen")) return false;
																	return true;
																}
														};
	/**
	 * Clase para modificar el estado de un objeto Freezable
	 */
	private static class FreezeStateModifierFunction implements StateModifierFunction<Freezable> {
		private final boolean _freeze;
		public FreezeStateModifierFunction(final boolean freeze) {
			_freeze = freeze;
		}
		@Override
		public void changeState(Freezable obj) {
			obj.setFrozen(_freeze);
		}
	}
	
	/**
	 * Congela un objeto y sus objetos dependientes
	 * @param freezableObj el objeto freezable
	 */
	public static void freeze(Freezable freezableObj) {
		ObjectsHierarchyModifier.<Freezable>changeObjectHierarchyState(freezableObj,new TypeRef<Freezable>() {},
																	   new FreezeStateModifierFunction(true),
																	   true,		// congelar toda la jerarquía de objetos
																	   _fieldAcceptCriteria);
	}
	/**
	 * Descongela un objeto y sus objetos dependientes
	 * @param freezableObj el objeto freezable
	 */
	public static void unFreeze(Freezable freezableObj) {
		ObjectsHierarchyModifier.<Freezable>changeObjectHierarchyState(freezableObj,new TypeRef<Freezable>() {},
																	   new FreezeStateModifierFunction(false),
																	   true,		// congelar toda la jerarquía de objetos
																	   _fieldAcceptCriteria);
	}
}
