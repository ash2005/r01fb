package r01f.aspects.dirtytrack;

import java.util.Collection;
import java.util.Set;

import org.aspectj.lang.annotation.SuppressAjWarnings;

import r01f.aspects.interfaces.dirtytrack.ConvertToDirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.types.dirtytrack.interfaces.ChangesTrackableCollection;
import r01f.types.dirtytrack.internal.ChangesTrackedCollectionMethods;
import r01f.types.dirtytrack.internal.CollectionChangesTracker;

/**
 * Aspecto base que convierte una clase que extiende de una colección en una colección trackable (ChangesTrackableCollection)
 */
privileged public abstract aspect ChangestTrackableCollectionAspectBase<C extends ChangesTrackableCollection> 
	           			  extends DirtyStateTrackableAspectBase<C> {
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE para inyectar un miembro CollectionChangesTracker que lleva el control
// 	de los cambios en el mapa
/////////////////////////////////////////////////////////////////////////////////////////
	private CollectionChangesTracker<V> ChangesTrackableCollection._trackingCollectionChangesTracker = new CollectionChangesTracker<V>();
	
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE para inyectar un miembro DirtyTrackingStatusImpl
//	NO es necesario inyectar el miembro _trackingStatus ya que al inyectarse el interfaz
//	ChangesTrackableCollection se inyecta el interfaz DirtyStateTrackable ya que 
//		ChangesTrackableCollection extends DirtyStateTrackable
// 	y por lo tanto se aplica el aspecto DirtyStateTrackableAspect que inyecta el field 
// 	_trackingStatus
/////////////////////////////////////////////////////////////////////////////////////////
//	DirtyTrackingStatus DirtyStateTrackable._trackingStatus = new DirtyTrackingStatusImpl();
	
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE: interfaz DirtyStateTrackable -> METODOS SOBRE ESCRITOS DEL INTERFAZ 
//												DirtyStateTrackable
//	NOTA: 	Se sobre escriben métodos del interfaz DirtyStateTrackable ya que para comprobar
//			el estado dirty hay que mirar:
//				- los miembros que pueda incluir el tipo que extiende de Collection
//				- el propio mapa (adición, borrado, modificación) de elementos en el mapa
// 	NOTA:	NO es necesario incluir los métodos del interfaz DirtyStateTrackable que 
//			NO sea necesario sobre-escribir (ej: getTrackingStatus())
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean ChangesTrackableCollection.isDirty() {
		boolean someMemberDirty = false;		// estado dirty de los miembros del tipo que extiende Collection
		boolean mapDirty = false;				// estado dirty de la colección
		
		someMemberDirty = this.getTrackingStatus()._isDirty(this);
		if (!someMemberDirty) mapDirty = ChangesTrackedCollectionMethods.isDirty((Collection)this,
																				 _trackingCollectionChangesTracker);
		
		return someMemberDirty | mapDirty;
	}
	public DirtyStateTrackable ChangesTrackableCollection.resetDirty() {
		// Resetear el estado dirty de los miembros del tipo que extiende Collection
		this.getTrackingStatus()._resetDirty(this);
		// Resetear el estado dirty del Collection
		_trackingCollectionChangesTracker.resetDirty();
		
		return (DirtyStateTrackable)this;
	}
	public DirtyStateTrackable ChangesTrackableCollection.startTrackingChangesInState() {
		_trackingCollectionChangesTracker.startTrackingChangesInState();
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  true);
		return (DirtyStateTrackable)this;
	}
	public DirtyStateTrackable ChangesTrackableCollection.stopTrackingChangesInState() {
		_trackingCollectionChangesTracker.stopTrackingChangesInState();
		this.getTrackingStatus()._stopTrackingChangesInState(this,
												 	 		 true);
		return (DirtyStateTrackable)this;
	}
	public DirtyStateTrackable ChangesTrackableCollection.startTrackingChangesInState(final boolean startTrackingInChilds) {
		_trackingCollectionChangesTracker.startTrackingChangesInState(startTrackingInChilds);
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  startTrackingInChilds);
		return (DirtyStateTrackable)this;
	}
	public DirtyStateTrackable ChangesTrackableCollection.startTrackingChangesInState(final boolean startTrackingInChilds,
																	   				  final boolean checkIfOldValueChanges) {
		_trackingCollectionChangesTracker.startTrackingChangesInState(startTrackingInChilds,
															   		  checkIfOldValueChanges);
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  startTrackingInChilds,
												 	 		  checkIfOldValueChanges);
		return (DirtyStateTrackable)this;
	}
	public DirtyStateTrackable ChangesTrackableCollection.stopTrackingChangesInState(final boolean stopTrackingInChilds) {
		_trackingCollectionChangesTracker.stopTrackingChangesInState(stopTrackingInChilds);
		this.getTrackingStatus()._stopTrackingChangesInState(this,
															 stopTrackingInChilds);
		return (DirtyStateTrackable)this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE interfaz ChangesTrackableCollection: METODOS ESPECIFICOS
//  (ver implementación del tipo ChangesTrackedMap)
/////////////////////////////////////////////////////////////////////////////////////////
	public CollectionChangesTracker<V> ChangesTrackableCollection.getChangesTracker() {
		return _trackingCollectionChangesTracker;
	}
	public Set<V> ChangesTrackableCollection.newEntries() {
		return ChangesTrackedCollectionMethods.newEntries((Collection)this,_trackingCollectionChangesTracker);
	}
	public Set<V> ChangesTrackableCollection.removedEntries() {
		return ChangesTrackedCollectionMethods.removedEntries((Collection)this,_trackingCollectionChangesTracker);
	}
	public Set<V> ChangesTrackableCollection.notNewOrRemovedEntries() {
		return ChangesTrackedCollectionMethods.notNewOrRemovedEntries((Collection)this,_trackingCollectionChangesTracker);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//	CAPTURA DE METODOS MUTATOR DE LA COLECCIÓN
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressAjWarnings
    Object around(Collection col,Object value) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Collection+).add(*))	// llamar al método add de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    		 && args(value)															// argumentos del metodo add
    		 && target(col) {														// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    	Object outVal = null; 
    	ChangesTrackableCollection trck = (ChangesTrackableCollection)col;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		outVal = ChangesTrackedCollectionMethods.add(value,
										 		  		 col,trck.getChangesTracker());
    	} else {
    		outVal = proceed(col,value);
    	}
    	return outVal;
    }
    @SuppressAjWarnings
    boolean around(Collection col,Collection otherCol) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Collection+).addAll(Collection))	// llamar al método addAll de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    		 && args(otherCol)																	// argumentos del metodo addAll
    		 && target(col) {																	// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    	boolean outResult = true; 
    	ChangesTrackableCollection trck = (ChangesTrackableCollection)col;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		outResult = ChangesTrackedCollectionMethods.addAll(otherCol,
										 		  		       col,trck.getChangesTracker());
    	} else {
    		outResult = proceed(col,otherCol);
    	}
    	return outResult;
    }
    @SuppressAjWarnings
    boolean around(Collection col,Object value) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Collection+).remove(*))	// llamar al método remove de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    		 && args(value)																// argumentos del metodo remove
    		 && target(col) {															// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    	boolean outResult = true; 
    	ChangesTrackableCollection trck = (ChangesTrackableCollection)col;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		outResult = ChangesTrackedCollectionMethods.remove(value,
										 		  		       col,trck.getChangesTracker());
    	} else {
    		outResult = proceed(col,value);
    	}
    	return outResult;
    }
    @SuppressAjWarnings
    boolean around(Collection col,Collection otherCol) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Collection+).removeAll(Collection))// llamar al método removeAll de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    		 && args(otherCol)																	// argumentos del metodo removeAll
    		 && target(col) {																	// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    	boolean outResult = true; 
    	ChangesTrackableCollection trck = (ChangesTrackableCollection)col;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		outResult = ChangesTrackedCollectionMethods.removeAll(otherCol,
										 		  		       	  col,trck.getChangesTracker());
    	} else {
    		outResult = proceed(col,otherCol);
    	}
    	return outResult;
    }
    @SuppressAjWarnings
    boolean around(Collection col,Collection otherCol) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Collection+).retainAll(Collection))// llamar al método retainAll de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    		 && args(otherCol)																	// argumentos del metodo retainAll
    		 && target(col) {																	// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    	boolean outResult = true; 
    	ChangesTrackableCollection trck = (ChangesTrackableCollection)col;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		outResult = ChangesTrackedCollectionMethods.retainAll(otherCol,
										 		  		       	  col,trck.getChangesTracker());
    	} else {
    		outResult = proceed(col,otherCol);
    	}
    	return outResult;
    } 
    @SuppressAjWarnings
    void around(Collection col) : 
    		 	call(public * (@ConvertToDirtyStateTrackable Collection+).clear())	// llamar al método clear de un tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    		 && target(col) {														// el tipo anotado con @ConvertToDirtyStateTrackable y que extiende de Collection
    	ChangesTrackableCollection trck = (ChangesTrackableCollection)col;
    	if (trck.getTrackingStatus().isThisDirtyTracking()) {
    		ChangesTrackedCollectionMethods.clear(col,trck.getChangesTracker());
    	} else {
    		proceed(col);
    	}
    } 
}
