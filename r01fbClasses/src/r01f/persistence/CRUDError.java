package r01f.persistence;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

@XmlRootElement(name="persistenceCRUDError")
@Accessors(prefix="_")
public class CRUDError<M extends PersistableModelObject<? extends OID>>
	 extends PersistenceOperationOnModelObjectError<M>
  implements CRUDResult<M>,
  			 PersistenceOperationError {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * When the un-successful operation is a create or update operation, this
	 * field contains the client-sent data
	 */
	@XmlElement
	@Getter @Setter private M _targetEntity;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDError() {
		// nothing
	}
	CRUDError(final Class<M> entityType,
			  final PersistenceRequestedOperation requestedOp,
			  final Throwable th) {
		super(entityType,
			  requestedOp,
			  th);	
	}
	CRUDError(final Class<M> entityType,
			  final PersistenceRequestedOperation requestedOp,
			  final PersistenceErrorType errCode) {
		super(entityType,
			  requestedOp,
			  errCode);
	}
	CRUDError(final Class<M> entityType,
			  final PersistenceRequestedOperation requestedOp,
			  final String errMsg,final PersistenceErrorType errCode) {
		super(entityType,
			  requestedOp,
			  errMsg,errCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDOK<M> asOK() {
		throw new ClassCastException();
	}
	@Override
	public CRUDError<M> asError() {
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDError<M> withExtendedErrorCode(final int extErrorCode) {
		this.setExtendedErrorCode(extErrorCode);
		return this;
	}
}
