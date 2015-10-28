package r01f.aspects.core.util;


import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import r01f.aspects.interfaces.dirtytrack.ForceTransientToDirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.generics.TypeRef;
import r01f.reflection.ReflectionUtils;
import r01f.types.lazy.LazyCollection;
import r01f.types.lazy.LazyMap;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Predicate;

/**
 * Utilidad para cambiar una variable de estado en una jerarquía de objetos.
 * Utiliza reflection para recorrer la jerarquía de objetos y cambiar el estado en 
 * aquellos objetos que son del tipo deseado
 */
public class ObjectsHierarchyModifier {
///////////////////////////////////////////////////////////////////////////////////////////////////
// 	FUNCION
///////////////////////////////////////////////////////////////////////////////////////////////////
	public static interface StateModifierFunction<T> {
		public void changeState(T obj);
	}
///////////////////////////////////////////////////////////////////////////////////////////////////
// 	METODOS PRIVADOS
///////////////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public static <T> void changeObjectHierarchyState(final T obj,final TypeRef<T> typeRef,
													  final StateModifierFunction<T> modifierFunction,
													  final boolean changeChildsState,
													  final Predicate<Field> fieldAcceptCriteria) {
		if (obj == null) return;
		
		// Modificar el estado si se trata de un objeto que implementa el tipo typeRef
		if (ReflectionUtils.isImplementing(obj.getClass(),typeRef.rawType())) {
			modifierFunction.changeState(obj);
		}
		
		// Modificar el estado de los hijos
		Field[] fields = ReflectionUtils.allFields(obj.getClass());
		if (fields == null) return;
		
		for (Field f : fields) {
			// fields ignorados
			if (Modifier.isTransient(f.getModifiers()) && !f.isAnnotationPresent(ForceTransientToDirtyStateTrackable.class)) continue;	// pasar de los fields transient
			if (Modifier.isStatic(f.getModifiers())) continue;					// pasar de los fields static
			if (f.isAnnotationPresent(NotDirtyStateTrackable.class)) continue;	// pasar de los fields anotados con @NotDirtyStateTrackable (sino se mete en un bucle)
			if (fieldAcceptCriteria != null && !fieldAcceptCriteria.apply(f)) continue;
			
			// Obtener el valor del miembro... si es null NO hacer nada
			Object fValue = ReflectionUtils.fieldValue(obj,f,true);
			if (fValue == null) continue;
			
			// Tipos primitivos
			if (f.getType().isPrimitive() || ReflectionUtils.isFinalInmutable(f.getType())) continue;
			
			// System.out.println("::::::::>" + obj.getClass().getName() + "." + f.getName() + " (" + fValue.getClass() + ") ");
			
			// ---- Tipos complejos
			if (changeChildsState && ReflectionUtils.isImplementing(fValue.getClass(),typeRef.rawType())) {		// OJO!! ver si implementa la INSTANCIA no el tipo declarado
				T fzChild = (T)fValue;																			//		 en el miembro, ya que puede ser un interfaz
				ObjectsHierarchyModifier.changeObjectHierarchyState(fzChild,typeRef,			// Llamada recursiva
																	modifierFunction,changeChildsState,
																	fieldAcceptCriteria);
				
			}
			
			// ---- Colecciones (puede ser una colección que además es de tipo T y por lo tanto habrá entrado también por el punto anterior)
			// Mapas
			if (changeChildsState && CollectionUtils.isMap(f.getType())) {
				Map<?,?> theMap = (Map<?,?>)fValue;
				// Modificar el estado de cada uno de los elementos CARGADOS del mapa
				Collection<?> loadedValues = theMap instanceof LazyMap ? ((LazyMap<?,?>)theMap).loadedValues()	// SOLO convertir los cargados en un mapa Lazy
																 	   : theMap.values();					
				for (Object v : loadedValues) {
					if (v == null) continue;
					if (ReflectionUtils.isImplementing(v.getClass(),typeRef.rawType())) {	// Procesar SOLO los elementos que implementan T
						ObjectsHierarchyModifier.changeObjectHierarchyState((T)v,typeRef,	// Llamada recursiva
												   							modifierFunction,changeChildsState,
												   							fieldAcceptCriteria);
					}
				}
			}
			// Colecciones
			else if (changeChildsState && CollectionUtils.isCollection(f.getType())) {
				Collection<?> theCol = (Collection<?>)fValue;
				// Modificar el estado de cada uno de los elementos CARGADOS de la colección 
				Collection<?> loadedValues = theCol instanceof LazyCollection ? ((LazyCollection<?>)theCol).loadedValues()	// SOLO convertir los cargados en una colección lazy
																			  : theCol;
				// Modificar el estado de cada uno de los elementos de la colección
				for (Object v : loadedValues) {
					if (v == null) continue;
					if (ReflectionUtils.isImplementing(v.getClass(),typeRef.rawType())) {	// Procesar SOLO los elementos que implementan T
						ObjectsHierarchyModifier.changeObjectHierarchyState((T)v,typeRef,	// Llamada recursiva
												   							modifierFunction,changeChildsState,
												   							fieldAcceptCriteria);
					} 
				}
			}
			// Arrays de objetos
			else if (changeChildsState && CollectionUtils.isArray(f.getType())) {
				if (ReflectionUtils.isImplementing(f.getType().getComponentType(),typeRef.rawType())) {	// Procesar SOLO los elementos que implementan T
					int length = Array.getLength(fValue);
				    for (int i = 0; i < length; i ++) { 
				        Object v = Array.get(fValue,i);
				        if (v == null) continue; 
						ObjectsHierarchyModifier.changeObjectHierarchyState((T)v,typeRef,			// Llamada recursiva
								   			  	   							modifierFunction,changeChildsState,
								   			  	   							fieldAcceptCriteria);
				    }
				}
			} 
		} // for fields
	}
}
