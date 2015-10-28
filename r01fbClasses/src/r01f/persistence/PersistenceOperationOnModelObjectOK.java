package r01f.persistence;

import javax.xml.bind.annotation.XmlAttribute;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;

@Accessors(prefix="_")
abstract class PersistenceOperationOnModelObjectOK<T>
	   extends PersistenceOperationExecOK<T>
    implements PersistenceOperationOK,
    		   PersistenceOperationOnModelObjectResult<T>,
    		   Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  SERIALIZABLE DATA
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The type of the entity subject of the requested operation 
	 */
	@XmlAttribute(name="type")
	@Getter @Setter protected Class<? extends PersistableModelObject<? extends OID>> _modelObjectType;
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
	public PersistenceOperationOnModelObjectOK() {
		/* nothing */
	}
	PersistenceOperationOnModelObjectOK(final Class<? extends PersistableModelObject<? extends OID>> entityType,
					  					final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp) {
		_modelObjectType = entityType;
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
