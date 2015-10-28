package r01f.aspects.core.dirtytrack;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;
import r01f.aspects.core.dirtytrack.DirtyStateTrackingUtils.DirtyStatusModifier;
import r01f.aspects.core.dirtytrack.DirtyStateTrackingUtils.DirtyTrackingStatusModifier;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyTrackingStatus;
import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.locale.LanguageTexts;
import r01f.locale.LanguageTextsMapBacked;
import r01f.reflection.ReflectionUtils;
import r01f.types.dirtytrack.interfaces.ChangesTrackableCollection;
import r01f.types.dirtytrack.interfaces.ChangesTrackableMap;

/**
 * Tracking status container
 * @see {@link DirtyStateTrackable}
 */
@Slf4j
public class DirtyTrackingStatusImpl 
  implements DirtyTrackingStatus {

	private static final long serialVersionUID = -4833831535002335824L;
/////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
/////////////////////////////////////////////////////////////////////////////////////////
	private boolean _thisNew = false;					// Indica si el objeto es nuevo
	private boolean _thisDirty = false;					// Indica si se ha cambiado el estado de ESTE objeto INDEPENDIENTEMENTE de que se haya cambiado el estado de sus dependientes
	private boolean _thisDirtyTracking = false;			// Indica si se están monitorizando cambios en el estado
	private boolean _thisCheckIfValueChanges = false;	// Establece cómo se comprueba si un miembro ha cambiado
													 	// - _trckCheckIfValueChanges=true --> al cambiar un field, se comprueba si el nuevo valor es igual o no al valor anterior
														//									  (el joinpoint es en before fieldSet) 
														// - _trckCheckIfValueChanges=false -> al cambiar un field, NO se comprueba si el nuevo valor es igual o no al anterior
														//									   simplemente al hacer un set de un campo se considera un cambio, independientemente
														//									   de que el nuevo valor sea igual o no al anterior
														//									   (el joinpiont es en after fieldSet)
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR
/////////////////////////////////////////////////////////////////////////////////////////
	public DirtyTrackingStatusImpl() {
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Control de las modificaciones en el estado en ESTE objeto
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void setThisNew(boolean newObj) {
		_thisNew = newObj;
	}
	@Override
	public boolean isThisNew() {
		return _thisNew;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Control del estado de modificación en ESTE objeto
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void setThisDirty(boolean thisDirty) {
		_thisDirty = thisDirty;
	}
	@Override
	public boolean isThisDirty() {
		return _thisDirty;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Control del estado de tracking en ESTE objeto
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void setThisDirtyTracking(boolean dirtyTrack) {
		_thisDirtyTracking = dirtyTrack;
	}
	@Override
	public boolean isThisDirtyTracking() {
		return _thisDirtyTracking;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Control de cómo se comprueba el estado de modificación de ESTE objeto
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void setThisCheckIfValueChanges(boolean check) {
		_thisCheckIfValueChanges = check;
	}
	@Override
	public boolean isThisCheckIfValueChanges() {
		return _thisCheckIfValueChanges;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS A LOS QUE SE DELEGAN LOS MÉTODOS DEL INTERFAZ DirtyStateTrackable
//  EN LOS ASPECTOS
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void _resetDirty(final DirtyStateTrackable trck) {
		_thisDirty = false;
		_thisNew = false;
		DirtyStatusModifier.resetDirtyStatus(trck,true);
	}
	@Override
	public boolean _isThisDirty(DirtyStateTrackable trck) {
		// If changes are not being tracked... do not check anything else, return false
		if (!_thisDirtyTracking) return false;
		boolean outDirty = false;
		if (_thisDirty) {
			// if this object is dirty... it worth nothing to check anything else
			outDirty = true;
 		} else {
 			// if this object is NOT dirty... one of it's COMPOSITION related objects could be
 			outDirty = DirtyStateTrackingUtils.isThisObjectDirty(trck);
 		}
		return outDirty;
	}
	@Override
	public boolean _isDirty(final DirtyStateTrackable trck) {
		// Si NO se está haciendo trackig... NO mirar nada, devolver false directamente
		if (!_thisDirtyTracking) return false;
		
		boolean outDirty = false;
		if (_thisDirty) {
			// if this object is dirty... it worth nothing to check anything else
			outDirty = true;
		} else {
			// if this object is NOT dirty... check related objects (all of them -opposed to _isThisDirty)
			outDirty = DirtyStateTrackingUtils.isObjectDirty(trck);		
		}
		return outDirty;
	}
	@Override
	public void _startTrackingChangesInState(final DirtyStateTrackable trck) {
		DirtyTrackingStatusModifier.startTrackingChangesInState(trck,
																false,
																true);
	}
	@Override
	public void _stopTrackingChangesInState(final DirtyStateTrackable trck) {
		DirtyTrackingStatusModifier.stopTrackingChangesInState(trck,
															   true);
	}
	@Override
	public void _startTrackingChangesInState(final DirtyStateTrackable trck,
											 final boolean startTrackingInChilds) {
		DirtyTrackingStatusModifier.startTrackingChangesInState(trck,
																false,
																startTrackingInChilds);
	}
	@Override
	public void _startTrackingChangesInState(final DirtyStateTrackable trck,
											 final boolean startTrackingInChilds,
											 final boolean checkIfOldValueChanges) {
		_thisCheckIfValueChanges = checkIfOldValueChanges;
		DirtyTrackingStatusModifier.startTrackingChangesInState(trck,
																checkIfOldValueChanges,
																startTrackingInChilds);
	}
	@Override
	public void _stopTrackingChangesInState(final DirtyStateTrackable trck,
											final boolean stopTrackingInChilds) {
		DirtyTrackingStatusModifier.stopTrackingChangesInState(trck,
															   stopTrackingInChilds);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METODOS QUE SE EJECUTAN EN LOS ADVICES
/////////////////////////////////////////////////////////////////////////////////////////
	public static void _beforeSetMember(final DirtyStateTrackable trck,final Field field,final Object newValue) { 
		// Do not track transient fields
		if (!_dirtyTrackableField(field)) return;
		
		// When the old value is compared with the new value to check if a change has taken place
		// 		trck.getTrackingStatus().isThisCheckIfValueChanges() = true
		// If the old and new values are NOT compared the _afterSetMember() method is where the dirty status is set
		if (trck.getTrackingStatus().isThisDirtyTracking() && trck.getTrackingStatus().isThisCheckIfValueChanges()) {		// se está haciendo tracking comprobando si cambia el valor			
			// Obtener el valor anterior del campo utilizando Reflection
			Object prevValue = ReflectionUtils.fieldValue(trck,field,false);
			
			// Ver si ha cambiado respecto al valor anterior
			if (prevValue == null && newValue == null) {
				// NO ha cambiado
			} if (prevValue != null && newValue == null) {
				log.trace("[DirtyTracking]: change detected at field {} of {}",field.getName(),trck.getWrappedObject().getClass());
				trck.getTrackingStatus().setThisDirty(true);
			} else if (prevValue == null && newValue != null) {
				log.trace("[DirtyTracking]: change detected at field {} of {}",field.getName(),trck.getWrappedObject().getClass());
				trck.getTrackingStatus().setThisDirty(true);
			} else if (prevValue != null && newValue != null) {		// Se mira a ver si son iguales
				if (!trck.getTrackingStatus().isThisDirty()) {		// ... pero SOLO se cambia si NO era ya dirty
					log.trace("[DirtyTracking]: change detected at field {} of {}",field.getName(),trck.getWrappedObject().getClass());
					trck.getTrackingStatus().setThisDirty( !prevValue.equals(newValue) );																							
				}
																																	
			}			
		} 
	}
	/**
	 * Advice after: se ejecuta DESPUES de establecer el valor de un miembro en un objeto DirtyStateTrackable
	 */
	public static void _afterSetMember(final DirtyStateTrackable trck,final Field field,final Object newValue) {
		// Do not track transient fields
		if (!_dirtyTrackableField(field)) return;
		
		// Tener en cuenta que la nueva instancia puede ser una instancia de DirtyStateTrackable creada "fuera" del objeto padre
		// DESPUES de haber llamado a startTrackingChanges y por lo tanto NO se han establecido los atributos tracking a true
		if (newValue != null && newValue instanceof DirtyStateTrackable) {
			DirtyStateTrackable newValueAsTrackable = ((DirtyStateTrackable)newValue);			
			if (newValueAsTrackable.getTrackingStatus().isThisDirtyTracking() != trck.getTrackingStatus().isThisDirtyTracking()
			 || newValueAsTrackable.getTrackingStatus().isThisCheckIfValueChanges() != trck.getTrackingStatus().isThisCheckIfValueChanges()) {
				DirtyTrackingStatusModifier.startTrackingChangesInState(newValueAsTrackable,
																		trck.getTrackingStatus().isThisDirtyTracking(),
																		trck.getTrackingStatus().isThisCheckIfValueChanges());
			}
		} 
		// The field probabilly has changed (a setter method has been called)... a comparision between the old and new field value is not done
		// so we assume that the value has changed...
		//		trck.getTrackingStatus().isThisCheckIfValueChanges() = false
		// if the new value has to be compared with the old value to determine if a change has occur the _beforeSetMember() method 
		// should be called
		if (trck.getTrackingStatus().isThisDirtyTracking() && !trck.getTrackingStatus().isThisCheckIfValueChanges()) {		// se está haciendo tracking ignorando si cambia el valor
			log.trace("[DirtyTracking]: change detected at field {} of {}",field.getName(),trck.getWrappedObject().getClass());			
			trck.getTrackingStatus().setThisDirty(true);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	ASIGNACIÓN DE COLECCIONES (Map / Collection)
// 	Cuando se intenta asignar un miembro tipo Map o Collection, se "pega" el cambiazo
// 	por otro que "envuelve" al original y que lleva la cuenta de las modificaciones
// 	y además es capaz de "informar" al objeto que contiene el Map o Collection de que se
//  ha producido una modificación en el mapa para que establezca su estado a dirty
//	NOTA: Los advices se podrían definir como 
// 				Map around() : get(Map+ DirtyStateTrackable+.*) 
// 		  pero da un ERROR cuando la colección es un objeto que extiende de una colección
// 		  Ej: 
// 				public class MyObj extends HashMap.
/////////////////////////////////////////////////////////////////////////////////////////
	public static LanguageTexts _arroundLanguageTextsFieldGet(final DirtyStateTrackable trck,final Field field,final LanguageTexts theLangTexts) {
		if (theLangTexts == null) return null;
		
		// if it's NOT a map-backed LanguageTexts instance... do nothing
		if (!(theLangTexts instanceof LanguageTextsMapBacked)) return theLangTexts;
		
		// do not track transient fields
		if (!_dirtyTrackableField(field)) return theLangTexts;
    	
    	return DirtyStateTrackingUtils.wrapLanguageTextsToTrackable(trck,field,theLangTexts);
    }
	public static <K,V> Object _arroundMapFieldGet(final DirtyStateTrackable trck,final Field field,final Map<K,V> theMap) {
		if (theMap == null) return null;
		
		// Si ya es ChangesTrackableMap no hacer nada
		if (theMap instanceof ChangesTrackableMap) return theMap;
		
		// do not track transient fields
		if (!_dirtyTrackableField(field)) return theMap;

    	return DirtyStateTrackingUtils.wrapMapToTrackable(trck,field,theMap);
    }
	public static <V> Object _arroundCollectionFieldGet(final DirtyStateTrackable trck,final Field field,final Collection<V> theCol) {
		if (theCol == null) return null;
		
		// Si ya es ChangesTrackableCollection no hacer nada
		if (theCol instanceof ChangesTrackableCollection) return theCol;
		
		// NO hacer tracking de fields transient
		if (!_dirtyTrackableField(field)) return theCol;
    	Object outWrappedCol = DirtyStateTrackingUtils.wrapCollectionToTrackable(trck,field,theCol);
    	return outWrappedCol;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Devuelve true si el field es un field trackable, es decir, no es ni estático ni transient
     * @param field el field
     * @return flase si el field NO es tracakble
     */
    private static boolean _dirtyTrackableField(final Field field) {
		boolean tranzient = Modifier.isTransient(field.getModifiers());
		boolean ztatic = Modifier.isStatic(field.getModifiers());
		boolean notDirtyStateTrackable = field.isAnnotationPresent(NotDirtyStateTrackable.class);
		// Devolver true si NO se cumple NINGUNA de las condiciones anteriores
		return !tranzient && !ztatic && !notDirtyStateTrackable;
    }

}
