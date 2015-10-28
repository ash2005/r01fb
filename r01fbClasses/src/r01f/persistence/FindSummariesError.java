package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;

@XmlRootElement(name="errorFindingSummarizedEntities")
@Accessors(prefix="_")
@SuppressWarnings("unchecked")
public class FindSummariesError<M extends PersistableModelObject<? extends OID>>
	 extends PersistenceOperationOnModelObjectError<Collection<? extends SummarizedModelObject<M>>>
  implements FindSummariesResult<M>,
  			 PersistenceOperationError {

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindSummariesError() {
		// nothing
	}
	FindSummariesError(final Class<M> entityType,
			  		 final Throwable th) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,
			  th);
	}
	FindSummariesError(final Class<M> entityType,
			  		 final String errMsg,final PersistenceErrorType errorCode) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,
			  errMsg,errorCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindSummariesOK<M> asOK() {
		throw new ClassCastException();
	}
	@Override
	public FindSummariesError<M> asError() {
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public FindSummariesError<M> withExtendedErrorCode(final int extErrorCode) {
		this.setExtendedErrorCode(extErrorCode);
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <S extends SummarizedModelObject<M>> Collection<S> getOrThrow() throws PersistenceException {
		return (Collection<S>)super.getOrThrow();
	}
}
