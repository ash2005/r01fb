package r01f.aspects.freezable;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import org.aspectj.lang.reflect.FieldSignature;

import r01f.aspects.core.freezable.FreezableCollection;
import r01f.aspects.core.freezable.FreezableMap;
import r01f.aspects.core.freezable.Freezer;
import r01f.aspects.interfaces.freezable.Freezable;
import r01f.reflection.ReflectionUtils;



/**
 * Implementación del interfaz {@link Freezable} en base a ASPECTJ
 * 
 * Para hacer que un tipo implemente el interfaz {@link Freezable} hay que:
 *		- PASO 1: Establecer que todas las clases anotadas con @ConvertToFreezable implementen
 *				  el interfaz FreezableInterface
 *						declare parents: @ConvertToFreezable * implements Freezable;
 *		- PASO 2: Crear el interfaz Freezable (ver aspecto {@link FreezableInterfaceAspect})
 *		- PASO 3: Implementar los pointcuts específicos para el interfaz {@link Freezable}
 * 
 * NOTA: 	Para mejorar la reutilización, el aspecto se divide en DOS
 * 				- La implementación del interfaz Freezable (este aspecto)
 * 				- Otro aspecto que "inyecta" el interfaz {@link Freezable} a aquellas clases 
 * 				  que se considere necesario, por ejemplo aquellas anotadas con @ConvertToFreezable
 * 				  (Ver {@link FreezableInterfaceAspect})
 * 			
 * 			De esta forma, es posible asociar el comportamiento {@link Freezable} a cualquier objeto;
 * 			simplemente basta con crear otro aspecto que haga que los tipos implementen el interfaz {@link Freezable}
 * 				declare parents: {pointcut} implements Freezable;
 * 			(es lo que se ha hecho en {@link ConvertToFreezableAnnotationAspect})
 * 
 * IMPORTANTE!!	En este aspecto se implementa el PASO 2 y el PASO 3 y en el aspecto {@link ConvertToFreezableAnnotationAspect}
 * 				se implementa el PASO 1
 */
privileged public aspect FreezableAspect { // perthis(freezableAnnotatedObj() || freezableImplementingInterfaceObj()) {
/////////////////////////////////////////////////////////////////////////////////////////
//	INTERFAZ Freezable (definida en un fichero .java propio)
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean Freezable._frozen = false;
	
	public boolean Freezable.isFrozen() {
		return _frozen;
	}
	public void Freezable.setFrozen(boolean value) {
		_frozen = value;
	}
	public void Freezable.freeze() {
		Freezer.freeze(this);
	}
	public void Freezable.unFreeze() {
		Freezer.unFreeze(this);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	Clases que implementan el interfaz Freezable
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * pointcut que designa un joinpoint en el que el objeto es una instancia de Freezable o una subclase
	 */
	pointcut freezableInterfaceImplementingObj() : within(Freezable+);
	
/////////////////////////////////////////////////////////////////////////////////////////
//	CAPTURA DE LAS MODIFICACIONES SOBRE LOS OBJETOS QUE IMPLEMENTAN Freezable
/////////////////////////////////////////////////////////////////////////////////////////
	pointcut fieldSetInFreezableClass(Freezable fz) : 
					(set(!static !final * Freezable+.*) 			// cualquier miembro...
						&& !set(boolean Freezable._frozen)) &&		// ... que no sea el miembro _frozen
					//within(Freezable+) && 	// Cualquier miembro en cualquier clase Freezable
					target(fz);		// objeto que recibe el advice (el que contiene el miembro)
	
	/**
	 * Advice before: se ejecuta ANTES de establecer el valor de un miembro en un objeto Freezable
	 */
	before(Freezable fz) : fieldSetInFreezableClass(fz) {
		if (fz.isFrozen()) throw new IllegalStateException("The object " + fz.getClass().getName() + " state is FROZEN so it cannot be changed!!");
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CAPTURA DE LAS MODIFICACIONES SOBRE COLECCIONES (arrays, maps y collections)
//	QUE SON MIEMBROS (field) DE OBJETOS QUE IMPLEMENTAN Freezable 
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Devolver un mapa envuelto en una clase que da acceso también al objeto
     * que contiene el mapa 
     */
    Map around() : get(Map+ Freezable+.*) {	// cualquier miembro de tipo Map de un Freezable
    	// Obtener información del joinPoint
    	Object container = thisJoinPoint.getTarget();
		FieldSignature fs = (FieldSignature)thisJoinPoint.getSignature();
		Field f = fs.getField();					// Field que se está accediendo
		Class<?> c = f.getDeclaringClass();			// Clase que contiene el field
    	
		// Obtener el mapa del miembro
    	Map theMap = proceed();		// Obtener el mapa subyacente
    	
    	// Si se dan las siguientes condiciones:
    	//		a.- El objeto padre es freezable y está congelado 
    	//		b.- el mapa NO es una instancia de FreezableMap
    	// dar el cambiazo por uno freezable
    	boolean containerIsFrozen = (container instanceof Freezable) && ((Freezable)container).isFrozen();
    	if (containerIsFrozen && theMap != null && !(theMap instanceof FreezableMap)) {
    		// Asegurarse primero de que el field es de tipo Map (el interface y NO un tipo concreto)
    		if (!f.getType().isAssignableFrom(Map.class)) throw new IllegalArgumentException("The Map field " + c.getName() + "." + f.getName() + " (" + f.getType() + ") type is NOT a java.util.Map (the interface), so it cannot be converted to a FreezableMap.");
    		
    		// Cambiar la instancia de Map por una FreezableMap
    		theMap = new FreezableMap(theMap,true);
    		ReflectionUtils.setFieldValue(thisJoinPoint.getTarget(),f,theMap,false);
    	} else {
    		// Por aqui pasa cuando YA se había dado el cambiazo al mapa por uno FreezableMap
    		// ... o bien el objeto padre NO está congelado
    	}
    	return theMap;
    }
    /**
     * Devolver un mapa envuelto en una clase que da acceso también al objeto
     * que contiene el mapa 
     */
    Collection around() : get(Collection+ Freezable+.*) {	// cualquier miembro de tipo Collection de un Freezable
    	// Obtener información del joinPoint
    	Object container = thisJoinPoint.getTarget();
		FieldSignature fs = (FieldSignature)thisJoinPoint.getSignature();
		Field f = fs.getField();					// Field que se está accediendo
		Class<?> c = f.getDeclaringClass();			// Clase que contiene el field
    	
		// Obtener el mapa del miembro
    	Collection theCol = proceed();		// Obtener la colección subyacente
    	
    	// Si se dan las siguientes condiciones:
    	//		a.- El objeto padre es freezable y está congelado 
    	//		b.- la colección NO es una instancia de FreezableCollection
    	// dar el cambiazo por uno freezable
    	boolean containerIsFrozen = (container instanceof Freezable) && ((Freezable)container).isFrozen();
    	if (containerIsFrozen && theCol != null && !(theCol instanceof FreezableCollection)) {
    		// Asegurarse primero de que el field es de tipo Collection (el interface y NO un tipo concreto)
    		if (!f.getType().isAssignableFrom(Collection.class)) throw new IllegalArgumentException("The Collection field " + c.getName() + "." + f.getName() + " (" + f.getType() + ") type is NOT a java.util.Collection (the interface), so it cannot be converted to a FreezableCollection.");
    		
    		// Cambiar la instancia de Collection por una FreezableCollection
    		theCol = new FreezableCollection(theCol,true);
    		ReflectionUtils.setFieldValue(thisJoinPoint.getTarget(),f,theCol,false);
    	} else {
    		// Por aqui pasa cuando YA se había dado el cambiazo al mapa por uno FreezableMap
    		// ... o bien el objeto padre NO está congelado
    	}
    	return theCol;
    }
	/**
	 * IMPORTANTE!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
	 * Hay que tener en cuenta que hay dos formas de invocar un método mutator de un mapa o colección 
	 * que es un miembro (field) de un objeto Container:
	 * 		public class Container {
	 * 			public Map _mapField;
	 * 		}
	 * CASO 1: Desde FUERA del objeto que contiene el miembro colección (map o collection):
	 * 			public class OtherObj {
	 * 				public void method() { 
	 * 					container.getMapField().put(..) <-- el pointcut está en el objeto/método que hace la llamada a put(..) en el Map,
	 * 				}										es decir en el objeto OtherObj y NO en el objeto container que contiene el Map 
	 * 			}											
	 * CASO 2: Desde FUERA del objeto que contiene el miembro colección (map o collection):
	 * 			public class OtherObj {
	 * 				public void method() {
	 * 					container.putIntoMapField(..) <-- el método putIntoMapField del objeto container es el que 
	 * 				}								 	  hace la llamada al método put(..) del Map así que ahí está el pointcut
	 * 			} 
	 * Esto implica que en un solo pointcut NO se puede tener acceso al objeto que contiene el objeto colección (map / collection), 
	 * unicamente se puede obtener el objeto colección y los argumentos del método mutator
	 * 
	 * IMPORTANTE!!	Para evitar que este pointcut se aplique a TODOS los métodos mutator de todas las colecciones, se ha de implementar el 
	 * 				pointcut collectionsMutatorMethodsCallingTypes, por ejemplo para capturar las llamadas a put() en el CASO 1 en 
	 * 				cualquier clase de r01f:
	 * 					pointcut collectionsMutatorMethodsCallingTypes() : within(r01f..*);
	 * 
	 * Esta implementación es compleja, así que se ha optado por capturar los accesos a los miembros tipo coleccion y devolver un wrapper
	 * del tipo concreto (map/collection) que "lleva" el control de si está congelado o no 
     */
	
	// Este POINTCUT NO SIRVE para todos los casos:
	//		Funciona para el caso 2: freezableObj.putIntoMap(...)
	//		NO funciona para el caso 1: freezableObj.getMapField().put(...)
//	pointcut mutatorMethodCallOnFieldImplementingMap(Freezable theObj,Map theMap) : 
//					(call(* Map+.remove(*)) || call(* Map+.clear()) ||
//					call(* Map+.put(*,*))  || call(* Map+.putAll(*))) &&	// metodo mutator de un mapa	 
//					freezableInterfaceImplementingObj() && 					// en un objeto Freezable
//					!within(FreezableInterfaceAspect) &&					// pero NO dentro de un aspecto FreezableInterfaceAspect
//					this(theObj) && target(theMap);		// this = objeto que hace la llamada al advice / target = objeto que recibe el advice
//	/**
//	 * Advice before: se ejecuta ANTES de establecer el valor de un miembro tipo Map en un objeto Freezable
//	 */
//	before(Freezable theObj,Map theMap): mutatorMethodCallOnFieldImplementingMap(theObj,theMap) {
//		if (theObj.isFrozen()) throw new IllegalStateException("The object " + theObj.getClass().getName() + " state is FROZEN so you cannot put an entry into a Map member!!");
//	}
	
// >> COLECCIONES TIPO Collection ---------------------------------
	// Este POINTCUT NO SIRVE para todos los casos:
	//		Funciona para el caso 2: freezableObj.addIntoCollection(...)
	//		NO funciona para el caso 1: freezableObj.getColField().put(...)
//	pointcut mutatorMethodCallOnFieldImplementingCollection(Freezable theObj,Collection theCol) : 
//					(call(* Collection+.remove(*)) || call(* Collection+.removeAll(*)) || call(* Collection+.retainAll(*)) || call(* Collection+.clear()) ||  
//					call(* Collection+.add(*)) || call(* Collection+.addAll(*))) &&		// método mutator de un mapa
//					freezableInterfaceImplementingObj() &&								// en un objeto Freezable
//					!within(FreezableInterfaceAspect) &&								// pero NO dentro de un aspecto FreezableInterfaceAspect
//					this(theObj) && target(theCol);		// this = objeto que hace la llamada al advice / target = objeto que recibe el advice
//	/**
//	 * Advice before: se ejecuta ANTES de establecer el valor de un miembro tipo Collection en un objeto Freezable
//	 */
//	before(Freezable theObj,java.util.Collection theCol): mutatorMethodCallOnFieldImplementingCollection(theObj,theCol) {
//		if (theObj.isFrozen()) throw new IllegalStateException("The object " + theObj.getClass().getName() + " state is FROZEN so you cannot put a value into a Collection member!!");
//	}
// >> COLECCIONES TIPO ARRAY ---------------------------------------
	/*
	 * ---- NO es posible establecer un joinpoint en el establecimiento de un 
	 *		elemento de un array (hay un bug reportado sobre esto)
//	 * pointcut llamado setOnArrayField que designa un joinpoint al cambiar miembro de tipo array de objetos 
//	 * Freezable
//	 */
//	pointcut setOnArrayField(Freezable theObj,Freezable[] theArray) : 
//					set(Freezable+[] *.*) &&
//					this(theObj) && 
//					args(theArray);
//  /**
//	 * Advice before: se ejecuta ANTES de establecer el valor de un miembro tipo array en un objeto Freezable
//	 */
//	before(Freezable theObj,Freezable[] theArray) : setOnArrayField(theObj,theArray) {
//		if (theObj.isFrozen()) throw new IllegalStateException("The object " + theObj.getClass().getName() + " state is FROZEN so you cannot change an element of an Array member!!");
//    }
}
