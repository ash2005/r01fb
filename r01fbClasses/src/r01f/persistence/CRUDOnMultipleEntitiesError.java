package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

@XmlRootElement(name="persistenceCRUDErrorOnMultipleEntities")
@Accessors(prefix="_")
public class CRUDOnMultipleEntitiesError<M extends PersistableModelObject<? extends OID>>
     extends PersistenceOperationOnModelObjectError<Collection<CRUDResult<M>>>
  implements CRUDOnMultipleEntitiesResult<M>,
  			 PersistenceOperationError {	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOnMultipleEntitiesError() {
		// default no args constructor
	}
	CRUDOnMultipleEntitiesError(final Class<M> entityType,
				  		  	  	final PersistenceRequestedOperation reqOp,
				  		  	  	final Throwable th) {
		super(entityType,
			  reqOp,
			  th);
	}
	CRUDOnMultipleEntitiesError(final Class<M> entityType,
								final PersistenceRequestedOperation requestedOp,
								final PersistenceErrorType errCode) {
		super(entityType,
			  requestedOp,
			  errCode);
	}
	CRUDOnMultipleEntitiesError(final Class<M> entityType,
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
	public Collection<M> getSuccessfulOperationsOrThrow() throws PersistenceException {
		this.throwAsPersistenceException();
		return null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDOnMultipleEntitiesOK<M> asOK() {
		throw new ClassCastException();
	}
	@Override
	public CRUDOnMultipleEntitiesError<M> asError() {
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDOnMultipleEntitiesError<M> withExtendedErrorCode(final int extErrorCode) {
		this.setExtendedErrorCode(extErrorCode);
		return this;
	}
}
