package r01f.persistence;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.metadata.ModelObjectTypeMetaDataBuilder;

@XmlRootElement(name="foudOids")
@Accessors(prefix="_")
public class FindOIDsOK<O extends OID>
	 extends FindOK<O>
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
	public FindOIDsOK() {
		/* nothing */
	}
	@SuppressWarnings("unchecked")
	protected <M extends PersistableModelObject<O>>
			  FindOIDsOK(final Class<M> entityType) {
		super((Class<O>)ModelObjectTypeMetaDataBuilder.createFor(entityType)
													  .getOIDFieldMetaData()
													  .getDataType());
		_modelObjectType = entityType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsError<O> asCRUDError() {
		throw new ClassCastException();
	}
	@Override
	public FindOIDsOK<O> asCRUDOK() {
		return this;
	}
}
