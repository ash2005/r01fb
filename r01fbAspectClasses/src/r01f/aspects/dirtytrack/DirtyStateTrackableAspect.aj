package r01f.aspects.dirtytrack;

import r01f.aspects.core.dirtytrack.DirtyTrackingStatusImpl;
import r01f.aspects.interfaces.dirtytrack.DirtyStateTrackable;
import r01f.aspects.interfaces.dirtytrack.DirtyTrackingStatus;


/**
 * Aspecto hace que se pueda detectar si el estado de una clase ha cambiado, es decir,
 * se MONITORIZAN los cambios en los miembros y se "anota" cualquier cambio
 * El uso habitual es:
 * PASO 1: Crear una clase anotada con @ConvertToDirtyStateTrackable
 * 				@ConvertToDirtyStateTrackable
 * 				public class MyTrackableObj {
 * 				}
 * PASO 2: Establecer el estado del objeto 
 * 				MyTrackableObj obj = new MyTrackableObj();
 * 				obj.setXX
 * 				obj.setYY
 * 				assertFalse(obj.isDirty());
 * 				obj.startTrackingChanges();	<-- empezar a monitorizar cambios en el estado
 * 				obj.setXX 		<-- el estado cambia
 * 				assertTrue(obj.isDirty());	<-- El estado ha cambiado!!
 */
privileged public aspect DirtyStateTrackableAspect  
                 extends DirtyStateTrackableAspectBase<DirtyStateTrackable>  {
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE para inyectar un miembro DirtyTrackingStatusImpl
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Dirty status control
	 */
	private transient DirtyTrackingStatus DirtyStateTrackable._trackingStatus = new DirtyTrackingStatusImpl();
	
/////////////////////////////////////////////////////////////////////////////////////////
//  INTER-TYPE inject DirtyStateTrackable interface
/////////////////////////////////////////////////////////////////////////////////////////
	public DirtyTrackingStatus DirtyStateTrackable.getTrackingStatus() {
		return _trackingStatus;
	}
	public boolean DirtyStateTrackable.isThisDirty() {
		return this.getTrackingStatus()._isThisDirty(this);
	}
	public boolean DirtyStateTrackable.isDirty() {
		return this.getTrackingStatus()._isDirty(this);
	}
	public DirtyStateTrackable DirtyStateTrackable.touch() {
		this.getTrackingStatus().setThisDirty(true);
		return this;
	}
	public DirtyStateTrackable DirtyStateTrackable.resetDirty() {
		this.getTrackingStatus()._resetDirty(this);
		return this;
	}
	public DirtyStateTrackable DirtyStateTrackable.stopTrackingChangesInState() {
		this.getTrackingStatus()._stopTrackingChangesInState(this,
												 	 		 true);	
		return this;
	}
	public DirtyStateTrackable DirtyStateTrackable.startTrackingChangesInState() {
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  true);
		return this;
	}
	public DirtyStateTrackable DirtyStateTrackable.startTrackingChangesInState(final boolean startTrackingInChilds) {
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  startTrackingInChilds);
		return this;
	}
	public DirtyStateTrackable DirtyStateTrackable.startTrackingChangesInState(final boolean startTrackingInChilds,
																			   final boolean checkIfOldValueChanges) {
		this.getTrackingStatus()._startTrackingChangesInState(this,
												 	 		  startTrackingInChilds,
												 	 		  checkIfOldValueChanges);
		return this;
	}
	public DirtyStateTrackable DirtyStateTrackable.stopTrackingChangesInState(final boolean stopTrackingInChilds) {
		this.getTrackingStatus()._stopTrackingChangesInState(this,
															 stopTrackingInChilds);
		return this;
	}
	@SuppressWarnings("unchecked")
	public <T> T DirtyStateTrackable.getWrappedObject() {
		return (T)this;
	}
}
