package r01f.persistence;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Accessors(prefix="_")
abstract class PersistenceOperationOnObjectOK<T>
	   extends PersistenceOperationExecOK<T>
    implements PersistenceOperationOnObjectResult<T> {
/////////////////////////////////////////////////////////////////////////////////////////
//  SERIALIZABLE DATA
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The type of the entity subject of the requested operation 
	 */
	@XmlAttribute(name="type")
	@Getter @Setter protected Class<T> _objectType;
	/**
	 * The requested operation
	 */
	@XmlAttribute(name="requestedOperation")
	@Getter @Setter protected PersistenceRequestedOperation _requestedOperation;
	/**
	 * The performed operation
	 * Sometimes the requested operation is NOT the same as the requested operation since
	 * for example, the client requests a create operation BUT an update operation is really 
	 * performed because the record already exists at the persistence store
	 */
	@XmlAttribute(name="performedOperation")
	@Getter @Setter protected PersistencePerformedOperation _performedOperation;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceOperationOnObjectOK() {
		/* nothing */
	}
	@SuppressWarnings("unchecked")
	PersistenceOperationOnObjectOK(final Class<?> entityType,
					  			   final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp) {
		_objectType = (Class<T>)entityType;
		_requestedOperation = reqOp;
		_performedOperation = performedOp;
		_requestedOperationName = reqOp.name();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getRequestedOperationName() {
		return _requestedOperation != null ? _requestedOperation.name() 
										   : "unknown persistence operation";
	}
}
