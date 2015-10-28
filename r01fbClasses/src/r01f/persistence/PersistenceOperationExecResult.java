package r01f.persistence;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(prefix="_")
public abstract class PersistenceOperationExecResult<T> 
    	   implements PersistenceOperationResult {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@XmlAttribute(name="requestedOperationName")
	@Getter @Setter protected String _requestedOperationName;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceOperationExecResult() {
		/* nothing */
	} 
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets the operation execution returned object
	 * @return the persistence operation returned object or throw a {@link PersistenceException} if the 
	 *  	   operation execution was not successful
	 * @throws PersistenceException
	 */
	public T getOrThrow() throws PersistenceException {
		if (this.hasFailed()) this.asError()		//as(PersistenceOperationExecError.class)
								  .throwAsPersistenceException();
		return this.asOK()	//as(PersistenceOperationExecOK.class)
				   .getOrThrow();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean hasFailed() {
		return this instanceof PersistenceOperationError;
	}

	@Override
	public boolean hasSucceeded() {
		return this instanceof PersistenceOperationOK;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public abstract PersistenceOperationExecError<T> asError();
	public abstract PersistenceOperationExecOK<T> asOK();
/////////////////////////////////////////////////////////////////////////////////////////
//  CAST
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <R extends PersistenceOperationResult> R as(final Class<R> type) {
		return (R)this;
	}
//	@Override @SuppressWarnings("unchecked")
//	public <R extends PersistenceOperationError> R asError(final Class<R> type) {
//		return (R)this;
//	}
//	@Override @SuppressWarnings("unchecked")
//	public <R extends PersistenceOperationOK> R asOK(final Class<R> type) {
//		return (R)this;
//	}
}
