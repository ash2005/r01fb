package r01f.persistence;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

@XmlRootElement(name="crudOnModelObjectOK")
@Accessors(prefix="_")
public class CRUDOnModelObjectOK<M extends PersistableModelObject<? extends OID>>
	 extends CRUDOK<M> 
  implements CRUDOnModelObjectResult<M> {
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOnModelObjectOK() {
		/* nothing */
	}
	CRUDOnModelObjectOK(final Class<M> entityType,
		   final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp) {
		super(entityType,
			  reqOp,performedOp);
	}
	CRUDOnModelObjectOK(final Class<M> entityType,
		   final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp,
		   final M entity) {
		super(entityType,
			  reqOp,performedOp);
		_operationExecResult = entity;
	}
	CRUDOnModelObjectOK(final Class<M> entityType,
		   final PersistenceRequestedOperation reqOp,
		   final M entity) {
		this(entityType,
			 reqOp,PersistencePerformedOperation.from(reqOp),
			 entity);
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
		return this;
	}
	@Override
	public CRUDOnModelObjectError<M> asCRUDOnModelObjectError() {
		throw new ClassCastException();
	}

}
