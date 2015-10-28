package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@XmlRootElement(name="foundEntityOids")
@Accessors(prefix="_")
public class FindOIDsOK<O extends OID>
	 extends PersistenceOperationOnModelObjectOK<Collection<O>>
  implements FindOIDsResult<O>,
  			 PersistenceOperationOK {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindOIDsOK() {
		/* nothing */
	}
	protected <M extends PersistableModelObject<O>>
			  FindOIDsOK(final Class<M> entityType) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,PersistencePerformedOperation.from(PersistenceRequestedOperation.FIND));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * When a single result is expected, this method returns this oid
	 * @return
	 */
	public O getSingleExpectedOrThrow() {
		O outOid = null;
		Collection<O> oids = this.getOrThrow();
		if (CollectionUtils.hasData(oids)) {
			outOid = CollectionUtils.of(oids).pickOneAndOnlyElement("A single instance of {} oid was expected to be found BUT {} were found",_modelObjectType,oids.size());
		} 
		return outOid;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOIDsError<O> asError() {
		throw new ClassCastException();
	}
	@Override
	public FindOIDsOK<O> asOK() {
		return this;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		return Strings.customized("{} persistence operation requested on entity of type {} and found {} results",
								  _requestedOperation,_modelObjectType,CollectionUtils.safeSize(_operationExecResult));
	}

}
