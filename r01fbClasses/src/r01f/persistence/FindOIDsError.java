package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

@XmlRootElement(name="errorFindingEntitiesOids")
@Accessors(prefix="_")
public class FindOIDsError<O extends OID>
	 extends PersistenceOperationOnModelObjectError<Collection<O>>
  implements FindOIDsResult<O>,
  			 PersistenceOperationError {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindOIDsError() {
		// nothing
	}
	FindOIDsError(final Class<? extends PersistableModelObject<? extends OID>> entityType,
			  	  final Throwable th) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,
			  th);
	}
	FindOIDsError(final Class<? extends PersistableModelObject<? extends OID>> entityType,
			  	  final String errMsg,final PersistenceErrorType errorCode) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,
			  errMsg,errorCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsError<O> asError() {
		return this;
	}
	@Override
	public FindOIDsOK<O> asOK() {
		throw new ClassCastException();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsError<O> withExtendedErrorCode(final int extErrorCode) {
		this.setExtendedErrorCode(extErrorCode);
		return this;
	}
}
