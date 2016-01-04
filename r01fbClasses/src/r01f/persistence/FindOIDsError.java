package r01f.persistence;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;

@XmlRootElement(name="oidFindError")
@Accessors(prefix="_")
public class FindOIDsError<O extends OID>
	 extends FindError<O>
  implements FindOIDsResult<O> {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The model object type
	 * Beware that {@link FindOIDsOK} extends {@link FindOK} parameterized with 
	 * the oid type NOT the model object type 
	 */
	@XmlAttribute(name="modelObjType")
	@Getter @Setter private Class<? extends PersistableModelObject<? extends OID>> _modelObjectType;

/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindOIDsError() {
		// nothing
	}
	@SuppressWarnings("unchecked")
	FindOIDsError(final Class<? extends PersistableModelObject<? extends OID>> entityType,
			  	  final Throwable th) {
		super((Class<O>)ModelObjectTypeMetaDataBuilder.createFor(entityType)
													  .getOIDFieldMetaData()
													  .getDataType(),
			  th);
		_modelObjectType = entityType;
	}
	@SuppressWarnings("unchecked")
	FindOIDsError(final Class<? extends PersistableModelObject<? extends OID>> entityType,
			  	  final String errMsg,final PersistenceErrorType errorCode) {
		super((Class<O>)ModelObjectTypeMetaDataBuilder.createFor(entityType)
													  .getOIDFieldMetaData()
													  .getDataType(),
			  errMsg,errorCode);
		_modelObjectType = entityType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsError<O> asCRUDError() {
		return this;
	}
	@Override
	public FindOIDsOK<O> asCRUDOK() {
		throw new ClassCastException();
	}
}
