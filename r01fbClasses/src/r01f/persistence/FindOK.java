package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

@XmlRootElement(name="foundEntities")
@Accessors(prefix="_")
public class FindOK<M extends PersistableModelObject<? extends OID>>
	 extends PersistenceOperationOnModelObjectOK<Collection<M>>
  implements FindResult<M>,
  			 PersistenceOperationOK {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindOK() {
		/* nothing */
	}
	protected FindOK(final Class<M> entityType) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,PersistencePerformedOperation.from(PersistenceRequestedOperation.FIND));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the found entities' oids if the persistence find operation was successful or a PersistenteException if not
	 * @throws PersistenceException
	 */
	public <O extends OID> Collection<O> getOidsOrThrow() throws PersistenceException {
		if (CollectionUtils.isNullOrEmpty(_operationExecResult)) return Lists.newArrayList();
		return FluentIterable.from(_operationExecResult)
							 .transform(new Function<M,O>() {
												@Override @SuppressWarnings("unchecked")
												public O apply(final M entity) {
													return (O)entity.getOid();
												}
								 			
							 			})
							 .toList();
	}
	/**
	 * When a single result is expected, this method returns this entity's oid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <O extends OID> O getSingleExpectedOidOrThrow() {
		M outEntity = this.getSingleExpectedOrThrow();
		return (O)(outEntity != null ? outEntity.getOid()
								 	 : null);
	}
	/**
	 * When a single result is expected, this method returns this entity
	 * @return
	 */
	public M getSingleExpectedOrThrow() {
		M outEntity = null;
		Collection<M> entities = this.getOrThrow();
		if (CollectionUtils.hasData(entities)) {
			outEntity = CollectionUtils.of(entities).pickOneAndOnlyElement("A single instance of {} was expected to be found BUT {} were found",_modelObjectType,entities.size());
		} 
		return outEntity;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindOK<M> asOK() {
		return this;
	}
	@Override
	public FindError<M> asError() {
		throw new ClassCastException();
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
