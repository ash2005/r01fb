package r01f.persistence;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

@XmlRootElement(name="crudOnModelObjectError")
@Accessors(prefix="_")
public class CRUDOnModelObjectError<M extends PersistableModelObject<? extends OID>>
	 extends CRUDError<M>
  implements CRUDOnModelObjectResult<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOnModelObjectError() {
		// nothing
	}
	CRUDOnModelObjectError(final Class<M> entityType,
			  final PersistenceRequestedOperation requestedOp,
			  final Throwable th) {
		super(entityType,
			  requestedOp,
			  th);	
	}
	CRUDOnModelObjectError(final Class<M> entityType,
			  final PersistenceRequestedOperation requestedOp,
			  final PersistenceErrorType errCode) {
		super(entityType,
			  requestedOp,
			  errCode);
	}
	CRUDOnModelObjectError(final Class<M> entityType,
			  final PersistenceRequestedOperation requestedOp,
			  final String errMsg,final PersistenceErrorType errCode) {
		super(entityType,
			  requestedOp,
			  errMsg,errCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("cast")
	public Class<M> getModelObjectType() {
		return (Class<M>)_objectType;
	}
	@Override
	public void setModelObjectType(final Class<M> type) {
		_objectType = type;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDOnModelObjectOK<M> asCRUDOnModelObjectOK() {
		throw new ClassCastException();
	}
	@Override
	public CRUDOnModelObjectError<M> asCRUDOnModelObjectError() {
		return this;
	}

}
