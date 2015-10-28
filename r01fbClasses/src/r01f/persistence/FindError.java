package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

@XmlRootElement(name="errorFindingEntities")
@Accessors(prefix="_")
public class FindError<M extends PersistableModelObject<? extends OID>>
	 extends PersistenceOperationOnModelObjectError<Collection<M>>
  implements FindResult<M>,
  			 PersistenceOperationError {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindError() {
		// nothing
	}
	FindError(final Class<M> entityType,
			  final Throwable th) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,
			  th);
	}
	FindError(final Class<M> entityType,
			  final String errMsg,final PersistenceErrorType errorCode) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,
			  errMsg,errorCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOK<M> asOK() {
		throw new ClassCastException();
	}
	@Override
	public FindError<M> asError() {
		return this;
	}
}
