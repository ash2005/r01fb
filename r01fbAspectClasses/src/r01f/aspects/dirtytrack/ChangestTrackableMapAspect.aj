package r01f.aspects.dirtytrack;

import java.util.Map;
import java.util.Set;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.interfaces.ChangesTrackableMap;
import r01f.types.dirtytrack.internal.ChangesTrackedMapMethods;
import r01f.types.dirtytrack.internal.CollectionChangesTracker;

/**
 * Aspecto que convierte una clase que extiende de Map en un mapa trackable (ChangesTrackableMap)
 */
privileged public aspect ChangestTrackableMapAspect 
	  			 extends DirtyStateTrackableAspectBase<ChangesTrackableMap> {
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE para inyectar un miembro CollectionChangesTracker que lleva el control
// 	de los cambios en el mapa
/////////////////////////////////////////////////////////////////////////////////////////
	private CollectionChangesTracker<K> ChangesTrackableMap._trackingMapChangesTracker = new CollectionChangesTracker<K>();
	
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE para inyectar un miembro DirtyTrackingStatusImpl
//	NO es necesario inyectar el miembro _trackingStatus ya que al inyectarse el interfaz
//	ChangesTrackableMap se inyecta el interfaz DirtyStateTrackable ya que 
//		ChangesTrackableMap extends DirtyStateTrackable
// 	y por lo tanto se aplica el aspecto DirtyStateTrackableAspect que inyecta el field 
// 	_trackingStatus
/////////////////////////////////////////////////////////////////////////////////////////
//	DirtyTrackingStatus DirtyStateTrackable._trackingStatus = new DirtyTrackingStatusImpl();
	
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE: interfaz DirtyStateTrackable -> METODOS SOBRE ESCRITOS DEL INTERFAZ 
//												DirtyStateTrackable
//	NOTA: 	Se sobre escriben métodos del interfaz DirtyStateTrackable ya que para comprobar
//			el estado dirty hay que mirar:
//				- los miembros que pueda incluir el tipo que extiende de Map
//				- el propio mapa (adición, borrado, modificación) de elementos en el mapa
// 	NOTA:	NO es necesario incluir los métodos del interfaz DirtyStateTrackable que 
//			NO sea necesario sobre-escribir (ej: getTrackingStatus())
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings({ "unchecked","rawtypes" })
	public boolean ChangesTrackableMap.isDirty() {
		boolean someMemberDirty = false;		// estado dirty de los miembros del tipo que extiende Map
		boolean mapDirty = false;				// estado dirty del Map
		
		someMemberDirty = this.getTrackingStatus()._isDirty(this);
		if (!someMemberDirty) mapDirty = ChangesTrackedMapMethods.isDirty((Map)this,
																		  _trackingMapChangesTracker);
		
		return someMemberDirty | mapDirty;
	}
	public DirtyStateTrackable ChangesTrackableMap.resetDirty() {
		// Resetear el estado dirty de los miembros del tipo que extiende Map
		this.getTrackingStatus()._resetDirty(this);
		// Resetear el estado dirty del Map
		_trackingMapChangesTracker.resetDirty();
		
		return this;
	}
	public DirtyStateTrackable ChangesTrackableMap.startTrackingChangesInState() {
		_trackingMapChangesTracker.startTrackingChangesInState();
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  true);
		return this;
	}
	public DirtyStateTrackable ChangesTrackableMap.stopTrackingChangesInState() {
		_trackingMapChangesTracker.stopTrackingChangesInState();
		this.getTrackingStatus()._stopTrackingChangesInState(this,
												 	 		 true);
		return this;
	}
	public DirtyStateTrackable ChangesTrackableMap.startTrackingChangesInState(final boolean startTrackingInChilds) {
		_trackingMapChangesTracker.startTrackingChangesInState(startTrackingInChilds);
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  startTrackingInChilds);
		return this;
	}
	public DirtyStateTrackable ChangesTrackableMap.startTrackingChangesInState(final boolean startTrackingInChilds,
																			   final boolean checkIfOldValueChanges) {
		_trackingMapChangesTracker.startTrackingChangesInState(startTrackingInChilds,
															   checkIfOldValueChanges);
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  startTrackingInChilds,
												 	 		  checkIfOldValueChanges);
		return this;
	}
	public DirtyStateTrackable ChangesTrackableMap.stopTrackingChangesInState(final boolean stopTrackingInChilds) {
		_trackingMapChangesTracker.stopTrackingChangesInState(stopTrackingInChilds);
		this.getTrackingStatus()._stopTrackingChangesInState(this,
															 stopTrackingInChilds);
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE interfaz ChangesTrackableMap: METODOS ESPECIFICOS
//  (ver implementación del tipo ChangesTrackedMap)
/////////////////////////////////////////////////////////////////////////////////////////
	public CollectionChangesTracker<K> ChangesTrackableMap.getChangesTracker() {
		return _trackingMapChangesTracker;
	}
	@SuppressWarnings({ "unchecked","rawtypes" })
	public Set<K> ChangesTrackableMap.newKeys() {
		return ChangesTrackedMapMethods.newKeys((Map)this,_trackingMapChangesTracker);
	}
	@SuppressWarnings({ "unchecked","rawtypes" })
	public Set<K> ChangesTrackableMap.removedKeys() {
		return ChangesTrackedMapMethods.removedKeys((Map)this,_trackingMapChangesTracker);
	}
	@SuppressWarnings({ "unchecked","rawtypes" })
	public Set<K> ChangesTrackableMap.notNewOrRemovedKeys() {
		return ChangesTrackedMapMethods.notNewOrRemovedKeys((Map)this,_trackingMapChangesTracker);
	}
	@SuppressWarnings({ "unchecked","rawtypes" })
	public Map<K,V> ChangesTrackableMap.newEntries() {
		return ChangesTrackedMapMethods.newEntries((Map)this,_trackingMapChangesTracker);
	}
	@SuppressWarnings({ "unchecked","rawtypes" })
	public Map<K,V> ChangesTrackableMap.notNewOrRemovedEntries() {
		return ChangesTrackedMapMethods.notNewOrRemovedEntries((Map)this,_trackingMapChangesTracker);
	}
	@SuppressWarnings({ "unchecked","rawtypes" })
	public String ChangesTrackableMap.debugInfo() {
		return ChangesTrackedMapMethods.debugInfo((Map)this,_trackingMapChangesTracker);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CAPTURA DE METODOS MUTATOR DEL MAPA
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressAjWarnings
    Object around(Map map,Object key,Object value) : 
    	        call(public * (@ConvertToDirtyStateTrackable Map+).put(*,*))		// llamar al método put de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
    		 && args(key,value)														// argumentos del metodo put
    		 && target(map) {														// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
    	Object outVal = null; 
    	ChangesTrackableMap trck = (ChangesTrackableMap)map;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		outVal = ChangesTrackedMapMethods.put(key,value,
										 		  map,trck.getChangesTracker());
    	} else {
    		outVal = proceed(map,key,value);
    	}
    	return outVal;
    }
    @SuppressAjWarnings
	Object around(Map map,Object key) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Map+).remove(*))		// llamar al método remove de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
    		 && args(key)															// argumentos del metodo put
    		 && target(map) {														// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
		Object outVal = null; 
    	ChangesTrackableMap trck = (ChangesTrackableMap)map;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		outVal = ChangesTrackedMapMethods.remove(key,
													 map,trck.getChangesTracker());
    	} else {
    		outVal = proceed(map,key);
    	}
    	return outVal;
	}
	@SuppressAjWarnings
	void around(Map map,java.util.Map otherMap) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Map+).putAll(java.util.Map))		// llamar al método putAll de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
    		 && args(otherMap)																	// argumentos del metodo put
    		 && target(map) {																	// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
    	ChangesTrackableMap trck = (ChangesTrackableMap)map;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
			ChangesTrackedMapMethods.putAll(otherMap,
											map,trck.getChangesTracker());
    	}
	}
	@SuppressAjWarnings
	void around(Map map) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Map+).clear())	// llamar al método clear de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
    		 && target(map) {												// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de map
    	ChangesTrackableMap trck = (ChangesTrackableMap)map;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		ChangesTrackedMapMethods.clear(map,trck.getChangesTracker());
    	}
	}
}
