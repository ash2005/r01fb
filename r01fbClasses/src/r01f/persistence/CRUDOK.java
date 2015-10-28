package r01f.persistence;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.util.types.Strings;

@XmlRootElement(name="persistedEntity")
@Accessors(prefix="_")
public class CRUDOK<M extends PersistableModelObject<? extends OID>>
	 extends PersistenceOperationOnModelObjectOK<M>
  implements CRUDResult<M>,
			 PersistenceOperationOK {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOK() {
		/* nothing */
	}
	CRUDOK(final Class<M> entityType,
		   final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp) {
		super(entityType,
			  reqOp,performedOp);
	}
	CRUDOK(final Class<M> entityType,
		   final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp,
		   final M entity) {
		super(entityType,
			  reqOp,performedOp);
		_operationExecResult = entity;
	}
	CRUDOK(final Class<M> entityType,
		   final PersistenceRequestedOperation reqOp,
		   final M entity) {
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
	public CRUDOK<M> asOK() {
		return this;
	}
	@Override
	public CRUDError<M> asError() {
		throw new ClassCastException();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		PersistencePerformedOperation supposedPerformed = PersistencePerformedOperation.from(_requestedOperation);
		return Strings.customized("{} persistence operation requested on entity of type {} with oid={} {}",
								  _requestedOperation,_modelObjectType,_operationExecResult.getOid(),_performedOperation,
								  supposedPerformed != _performedOperation ? ("and performed " + _performedOperation + " persistence operation")
										  								   : "");
	}
}
