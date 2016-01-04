package r01f.persistence;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.util.types.Strings;

@XmlRootElement(name="crudOk")
@Accessors(prefix="_")
public class CRUDOK<T>
	 extends PersistenceOperationOnObjectOK<T>
  implements CRUDResult<T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOK() {
		/* nothing */
	}
	CRUDOK(final Class<T> entityType,
		   final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp) {
		super(entityType,
			  reqOp,performedOp);
	}
	CRUDOK(final Class<T> entityType,
		   final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp,
		   final T entity) {
		this(entityType,
			 reqOp,performedOp);
		_operationExecResult = entity;
	}
	CRUDOK(final Class<T> entityType,
		   final PersistenceRequestedOperation reqOp,
		   final T entity) {
		this(entityType,
			 reqOp,PersistencePerformedOperation.from(reqOp),
			 entity);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean hasBeenLoaded() {
		return _performedOperation == PersistencePerformedOperation.LOADED;
	}
	public boolean hasBeenCreated() {
		return _performedOperation == PersistencePerformedOperation.CREATED;
	}
	public boolean hasBeenUpdated() {
		return _performedOperation == PersistencePerformedOperation.UPDATED;
	}
	public boolean hasBeenDeleted() {
		return _performedOperation == PersistencePerformedOperation.DELETED;
	}
	public boolean hasBeenModified() {
		return this.hasBeenCreated() || this.hasBeenUpdated();
	}
	public boolean hasNotBeenModified() {
		return !this.hasBeenModified();
	}
	public boolean hasBeenFound() {
		return _performedOperation == PersistencePerformedOperation.FOUND;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDOK<T> asCRUDOK() {
		return this;
	}
	@Override
	public CRUDError<T> asCRUDError() {
		throw new ClassCastException();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		PersistencePerformedOperation supposedPerformed = PersistencePerformedOperation.from(_requestedOperation);
		return Strings.customized("{} persistence operation requested on entity of type {} {}",
								  _requestedOperation,_objectType,_performedOperation,
								  supposedPerformed != _performedOperation ? ("and performed " + _performedOperation + " persistence operation")
										  								   : "");
	}
}
