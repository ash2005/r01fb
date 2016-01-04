package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@XmlRootElement(name="crudOnMultipleError")
@Accessors(prefix="_")
public class CRUDOnMultipleError<M>
     extends PersistenceOperationOnObjectError<Collection<CRUDResult<M>>>
  implements CRUDOnMultipleResult<M> {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object type
	 * (beware that {@link PersistenceOperationOnObjectOK} wraps a {@link Collection} 
	 *  of this objects)
	 */
	@XmlAttribute(name="modelObjType")
	@Getter @Setter protected Class<M> _modelObjectType;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public CRUDOnMultipleError() {
		// default no args constructor
	}
	CRUDOnMultipleError(final Class<M> entityType,
		  		  	  	final PersistenceRequestedOperation reqOp,
		  		  	  	final Throwable th) {
		super(Collection.class,
			  reqOp,
			  th);
		_modelObjectType = entityType;
	}
	CRUDOnMultipleError(final Class<M> entityType,
						final PersistenceRequestedOperation requestedOp,
						final PersistenceErrorType errCode) {
		super(Collection.class,
			  requestedOp,
			  errCode);
		_modelObjectType = entityType;
	}
	CRUDOnMultipleError(final Class<M> entityType,
						final PersistenceRequestedOperation requestedOp,
						final String errMsg,final PersistenceErrorType errCode) {
		super(Collection.class,
			  requestedOp,
			  errMsg,errCode);
		_modelObjectType = entityType;
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
	public CRUDOnMultipleOK<M> asCRUDOnMultipleOK() {
		throw new ClassCastException();
	}
	@Override
	public CRUDOnMultipleError<M> asCRUDOnMultipleError() {
		return this;
	}
}
