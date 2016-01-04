package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;

@XmlRootElement(name="errorFindingSummarizedModelObjects")
@Accessors(prefix="_")
@SuppressWarnings("unchecked")
public class FindSummariesError<M extends PersistableModelObject<? extends OID>>
	 extends PersistenceOperationOnObjectError<Collection<? extends SummarizedModelObject<M>>>
  implements FindSummariesResult<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Info about the model object
	 * beware that the {@link PersistenceOperationOnObjectOK} wraps a {@link Collection}
	 * of model objects
	 */
	@XmlAttribute(name="modelObjectType")
	@Getter @Setter private Class<M> _modelObjectType;
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
	public FindSummariesOK<M> asCRUDOK() {
		throw new ClassCastException();
	}
	@Override
	public FindSummariesError<M> asCRUDError() {
		return this;
	}
	
	@Override
	public <S extends SummarizedModelObject<M>> Collection<S> getOrThrow() throws PersistenceException {
		if (this.hasFailed()) this.asOperationExecError()		
								  .throwAsPersistenceException();
		return (Collection<S>) this.asOperationExecOK()
				   .getOrThrow();
	}	
	
}



