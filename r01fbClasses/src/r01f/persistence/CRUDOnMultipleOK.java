package r01f.persistence;

import java.util.Collection;
import java.util.Set;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Sets;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.model.facets.HasOID;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

@XmlRootElement(name="crudOnMultipleOk")
@Accessors(prefix="_")
public class CRUDOnMultipleOK<M>
     extends PersistenceOperationOnObjectOK<Collection<CRUDResult<M>>>
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
	public CRUDOnMultipleOK() {
		_operationExecResult = Sets.newHashSet();
	}
	CRUDOnMultipleOK(final Class<M> entityType,
					 final PersistenceRequestedOperation reqOp,final PersistencePerformedOperation performedOp) {
		super(Collection.class,
			  reqOp,performedOp);
		_modelObjectType = entityType;
		_operationExecResult = Sets.newHashSet();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Adds a {@link PersistenceOperationResultForSingleRecord} (a successful {@link PersistenceOperationResult})
	 * @param opOK
	 */
	public void addOperationOK(final CRUDOK<M> opOK) {
		_operationExecResult.add(opOK);
	}
	/**
	 * Adds a {@link PersistenceOperationNOK} (a failed {@link PersistenceOperationResult})
	 * @param opNOK
	 */
	public void addOperationNOK(final CRUDError<M> opNOK) {
		_operationExecResult.add(opNOK);
	}
	/**
	 * Adds a collection of performed operations
	 * @param okEntities
	 * @param reqOp
	 */
	@SuppressWarnings("unchecked")
	public void addOperationsOK(final Collection<M> okEntities,
								final PersistenceRequestedOperation reqOp) {
		if (CollectionUtils.hasData(okEntities)) {
			for (M okEntity : okEntities) {
				CRUDOK<M> ok = new CRUDOK<M>((Class<M>)_objectType,
										     reqOp,
										     okEntity);
				this.addOperationOK(ok);
			}
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the number of successful operations
	 */
	public int getNumberOfOperationsOK() {
		Collection<CRUDOK<M>> opsOK = this.getOperationsOK();
		return CollectionUtils.hasData(opsOK) ? opsOK.size() : 0;
	}
	/**
	 * Gets the {@link CRUDResult}s that were successful (ie the {@link CRUDOK} instances)
	 * @return the {@link CRUDOK} operations 
	 */
	public Collection<CRUDOK<M>> getOperationsOK() {
		Collection<CRUDOK<M>> outOps = null;
		if (CollectionUtils.hasData(_operationExecResult)) {
			outOps = FluentIterable.from(_operationExecResult)
										.filter(new Predicate<CRUDResult<M>>() {
															@Override
															public boolean apply(final CRUDResult<M> op) {
																return op.hasSucceeded();	// only successful operations
															}
												})	
									    .transform(new Function<CRUDResult<M>,CRUDOK<M>>() {
															@Override
															public CRUDOK<M> apply(final CRUDResult<M> op) {
																return op.asCRUDOK();	//as(CRUDOK.class);
															}
												   })
									    .toSet();
		}
		return outOps;
	}
	/**
	 * @return the number of unsuccessful operations
	 */
	public int getNumberOfOperationsNOK() {
		Collection<CRUDError<M>> opsNOK = this.getOperationsNOK();
		return CollectionUtils.hasData(opsNOK) ? opsNOK.size() : 0;
	}
	/**
	 * Gets the {@link CRUDResult}s that failed (ie the {@link CRUDError} instances)
	 * @return the {@link CRUDOK} operations 
	 */
	public Collection<CRUDError<M>> getOperationsNOK() {
		Collection<CRUDError<M>> outOps = null;
		if (CollectionUtils.hasData(_operationExecResult)) {
			outOps = FluentIterable.from(_operationExecResult)
										.filter(new Predicate<CRUDResult<M>>() {
															@Override
															public boolean apply(final CRUDResult<M> op) {
																return op.hasFailed();	// only failed operations
															}
												})	
									    .transform(new Function<CRUDResult<M>,CRUDError<M>>() {
															@Override 
															public CRUDError<M> apply(final CRUDResult<M> op) {
																return op.asCRUDError();	//as(CRUDError.class);
															}
												   })
									    .toSet();
		}
		return outOps;
	}
	/**
	 * @return true if all persistence operations failed
	 */
	public boolean haveAllFailed() {
		return CollectionUtils.hasData(_operationExecResult) ? this.getOperationsNOK().size() == _operationExecResult.size()	
															 : false;
	}
	/**
	 * @return true if all persistence operations succeeded
	 */
	public boolean haveAllSucceeded() {
		return CollectionUtils.hasData(_operationExecResult) ? this.getOperationsOK().size() == _operationExecResult.size()	
														  	 : false;		
	}
	/**
	 * @return true if there's any failed operation
	 */
	public boolean haveSomeFailed() {
		return CollectionUtils.hasData(_operationExecResult) ? this.getOperationsNOK().size() <= _operationExecResult.size()
														  	 : false;
	}
	/**
	 * @return true if there's any successful operation
	 */
	public boolean haveSomeSucceeded() {
		return CollectionUtils.hasData(_operationExecResult) ? this.getOperationsOK().size() <= _operationExecResult.size()
														  	 : false;		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public Collection<M> getSuccessfulOperationsOrThrow() throws PersistenceException {
		return this.getEntitiesOK();
	}
	/**
	 * Gets the {@link CRUDResult}s that were successful (ie the {@link CRUDOK} instances)
	 * <ul>
	 * <li>If some operations were successful and other failed, this method returns ONLY the successful ones
	 *     and does NOT throws an exception for the failed ones</li>
	 * <li>If all operations failed, this method throws a {@link PersistenceException} with the {@link CRUDError}
	 *     for the first failed operation</li>
	 * </ul>
	 * On the contrary, the {@link #getStrict()} method throws a {@link PersistenceException} if 
	 * there's any failed operation 
	 * @return a {@link Set} of the records after being processed
	 * @throws PersistenceException if there's a general error or ALL of the operations failed
	 */
	public Collection<M> get() {
		if (this.haveAllFailed()) {
			CRUDError<M> err = this.getFirstError();
			err.throwAsPersistenceException();
		}
		return this.getEntitiesOK();
	}
	/**
	 * Gets the multiple operation results or throws a {@link PersistenceException} if one of the
	 * operations has failed
	 * @return a {@link Set} of the records after being processed
	 * @throws PersistenceException if there's a general error or any of the operations failed
	 */
	public Collection<M> getStrict() throws PersistenceException {
		Collection<M> outResults = null;
		
		if (!this.haveSomeSucceeded()) {
			// everything ok
			outResults = this.getEntitiesOK();
		} else {		
			// Find the first error and throw it
			CRUDError<M> firstError = this.getFirstError();
			if (firstError != null) firstError.throwAsPersistenceException();
		}
		return outResults;
	}
	/**
	 * @return the entities that were successfully processed 
	 */
	public Collection<M> getEntitiesOK() {
		Set<M> outEntities = null;
		if (CollectionUtils.hasData(this.getOperationsOK())) {
			outEntities = FluentIterable.from(_operationExecResult)
									    .transform(new Function<CRUDResult<M>,M>() {
															@Override 
															public M apply(final CRUDResult<M> op) {
																M entity = op.asCRUDOK()		//.as(CRUDOK.class)
																			 .getOrThrow();	// sure it won't throw
																return entity;
															}
												   })
									    .toSet();
		}
		return outEntities;
	}
	/**
	 * @return the first failed operation
	 */
	public CRUDError<M> getFirstError() {
		Collection<CRUDError<M>> opError = this.getOperationsNOK();
		return CollectionUtils.hasData(opError) ? CollectionUtils.of(opError)
																 .pickOneElement()
												: null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CRUDOnMultipleOK<M> asCRUDOnMultipleOK() {
		return this;
	}
	@Override
	public CRUDOnMultipleError<M> asCRUDOnMultipleError() {
		throw new ClassCastException();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  DEBUG
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public CharSequence debugInfo() {
		Collection<CRUDOK<M>> opsOK = this.getOperationsOK();
		Collection<CRUDError<M>> opsNOK = this.getOperationsNOK();
		
		int size = (opsOK != null ? opsOK.size() * 200 : 0) +
				   (opsNOK != null ? opsNOK.size() * 200 : 0);
		StringBuilder sb = new StringBuilder(size);
		String debugErrors = this.haveSomeFailed() ? _debugErrors(opsNOK) : null;
		String debugNonErrors = this.haveSomeSucceeded()  ? _debugNonErrors(opsOK) : null;
		if (Strings.isNOTNullOrEmpty(debugNonErrors)) sb.append(debugNonErrors);
		if (Strings.isNOTNullOrEmpty(debugErrors)) {
			if (Strings.isNOTNullOrEmpty(debugErrors)) sb.append("\n");
			sb.append(debugErrors);
		}
		return sb;
	}
	private String _debugNonErrors(final Collection<CRUDOK<M>> opsOK) {
		StringBuilder sb = null;
		if (CollectionUtils.hasData(opsOK)) {
			sb = new StringBuilder(50);
			sb.append("Persistence Operations OK: ")
			  .append(opsOK.size());
		}
		return sb != null ? sb.toString() : null;
	}
	/**
	 * Returns a debug string with all the erroneous items
	 * @return
	 */
	private String _debugErrors(final Collection<CRUDError<M>> opsNOK) {
		StringBuilder sb = null;
		if (CollectionUtils.hasData(opsNOK)) {
			sb = new StringBuilder(opsNOK.size()*200);
			sb.append("Persistence Operations with ERROR: ")
			  .append(opsNOK.size()).append("\n");
			for (CRUDError<M> opErr : opsNOK) {
				sb.append("\t")
				  .append(opErr.getTargetEntityIdInfo())
				  .append(": ")
				  .append(opErr.getErrorMessage())
				  .append("\n");
			}
		}
		return sb != null ? sb.toString() : null;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unused")
	private static String _oksToCommaSeparatedOids(final Collection<CRUDOK<?>> ops) {
		Set<String> oids = null;
		if (CollectionUtils.hasData(ops)) {
			oids = FluentIterable.from(ops)
								 .transform(new Function<CRUDOK<?>,String>() {
									 				@Override 
													public String apply(final CRUDOK<?> op) {
									 					Object obj = op.getOrThrow();
									 					if (obj instanceof HasOID) return ((HasOID<?>)obj).getOid().asString();
									 					return "unknownOID";
													}
								 			})
								 .toSet();
		}
		return CollectionUtils.hasData(oids) ? CollectionUtils.of(oids).toStringCommaSeparated()
											 : null;
	}
	@SuppressWarnings("unused")
	private static String _noksToCommaSeparatedOids(final Collection<CRUDError<?>> ops) {
		Set<String> oids = null;
		if (CollectionUtils.hasData(ops)) {
			oids = FluentIterable.from(ops)
								 .transform(new Function<CRUDError<?>,String>() {
									 				@Override 
													public String apply(final CRUDError<?> op) {
									 					return op.getTargetEntityIdInfo();
													}
								 			})
								 .toSet();
		}
		return CollectionUtils.hasData(oids) ? CollectionUtils.of(oids).toStringCommaSeparated()
											 : null;
	}
}
