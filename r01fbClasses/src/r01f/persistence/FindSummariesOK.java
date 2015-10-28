package r01f.persistence;

import java.util.Collection;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.experimental.Accessors;
import r01f.exceptions.Throwables;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;
import r01f.model.facets.HasOID;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;

@XmlRootElement(name="foundSummarizedEntities")
@Accessors(prefix="_")
@SuppressWarnings("unchecked")
public class FindSummariesOK<M extends PersistableModelObject<? extends OID>>
	 extends PersistenceOperationOnModelObjectOK<Collection<? extends SummarizedModelObject<M>>>
  implements FindSummariesResult<M>,
  			 PersistenceOperationOK {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public FindSummariesOK() {
		/* nothing */
	}
	protected FindSummariesOK(final Class<M> entityType) {
		super(entityType,
			  PersistenceRequestedOperation.FIND,PersistencePerformedOperation.from(PersistenceRequestedOperation.FIND));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public <S extends SummarizedModelObject<M>> Collection<S> getOrThrow() throws PersistenceException {
		return (Collection<S>)super.getOrThrow();
	}
	/**
	 * @return the found entities' oids if the persistence find operation was successful or a PersistenteException if not
	 * @throws PersistenceException
	 */
	public <O extends OID> Collection<O> getOidsOrThrow() throws PersistenceException {
		if (CollectionUtils.isNullOrEmpty(_operationExecResult)) return Lists.newArrayList();
		return FluentIterable.from(_operationExecResult)
							 .transform(new Function<SummarizedModelObject<M>,O>() {
												@Override 
												public O apply(final SummarizedModelObject<M> entitySummary) {
													if (entitySummary instanceof HasOID) return ((HasOID<O>)entitySummary).getOid();
													throw new IllegalStateException(Throwables.message("The entity of type {} does NOT implements {}",
																									   entitySummary.getModelObjectType(),HasOID.class));
												}
								 			
							 			})
							 .toList();
	}
	/**
	 * When a single result is expected, this method returns this entity's oid
	 * @return
	 */
	public <O extends OID> O getSingleExpectedOidOrThrow() {
		SummarizedModelObject<M> outEntitySummary = this.getSingleExpectedOrThrow();
		if (outEntitySummary != null) {
			if (outEntitySummary instanceof HasOID) return ((HasOID<O>)outEntitySummary).getOid();
			throw new IllegalStateException(Throwables.message("The entity of type {} does NOT implements {}",
															   outEntitySummary.getModelObjectType(),HasOID.class));	
		}
		return null;
	}
	/**
	 * When a single result is expected, this method returns this entity
	 * @return
	 */
	public <S extends SummarizedModelObject<M>> S getSingleExpectedOrThrow() {
		S outEntitySummary = null;
		Collection<S> entities = this.<S>getOrThrow();
		if (CollectionUtils.hasData(entities)) {
			outEntitySummary = CollectionUtils.of(entities).pickOneAndOnlyElement("A single instance of {} was expected to be found BUT {} were found",SummarizedModelObject.class,entities.size());
		} 
		return outEntitySummary;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public FindSummariesOK<M> asOK() {
		return this;
	}
	@Override
	public FindSummariesError<M> asError() {
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
