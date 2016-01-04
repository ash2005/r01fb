package r01f.persistence;

import java.util.Collection;
import java.util.Date;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.DBEntityToModelObjectTransformerBuilder;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;
import r01f.util.types.Dates;
import r01f.util.types.Strings;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * Used from {@link CRUDResultBuilder} when composing the {@link PersistenceOperationResult} for a
 * multiple entity operation (ie delete all versions)
 * @param <O>
 * @param <M>
 */
@RequiredArgsConstructor(access=AccessLevel.PACKAGE)
public class CRUDResultForVersionableBuilder<M extends PersistableModelObject<? extends OIDForVersionableModelObject> & HasVersionableFacet> {
	protected final UserContext _userContext;
	protected final Class<M> _entityType;
	
	public <DB extends DBEntity> CRUDOnMultipleOK<M> deletedDBEntities(final Collection<DB> okDBEntities) {
		Function<DBEntity,M> defaultDBEntityToModelObjConverter = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																													_entityType);
		return this.deletedDBEntities(okDBEntities,
									  defaultDBEntityToModelObjConverter);
	}
	public <DB extends DBEntity> CRUDOnMultipleOK<M> deletedDBEntities(final Collection<DB> okDBEntities,
																			   final Function<DBEntity,M> converter) {
		Collection<M> okEntities = Lists.newArrayListWithExpectedSize(okDBEntities.size());
		for (DBEntity dbEntity : okDBEntities) {
			okEntities.add(converter.apply(dbEntity));
		}
		return this.deleted(okEntities);
	}
	public CRUDOnMultipleOK<M> deleted(final Collection<M> delOKs) {
		CRUDOnMultipleOK<M> outMultipleCRUDOKs = new CRUDOnMultipleOK<M>(_entityType,
																						 PersistenceRequestedOperation.DELETE,PersistencePerformedOperation.DELETED);
		outMultipleCRUDOKs.addOperationsOK(delOKs,
										   PersistenceRequestedOperation.DELETE);
		return outMultipleCRUDOKs;
	}
	public CRUDResultOnMultipleBuilderErrorStep<M> notDeleted() {
		return new CRUDResultOnMultipleBuilderErrorStep<M>(_userContext,
														   _entityType,
														   PersistenceRequestedOperation.DELETE);
	}
	public CRUDResultOnMultipleBuilderErrorStep<M> not(final PersistenceRequestedOperation reqOp) {
		return new CRUDResultOnMultipleBuilderErrorStep<M>(_userContext,
														   _entityType,
														   reqOp);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class CRUDResultOnMultipleBuilderErrorStep<M extends PersistableModelObject<? extends OIDForVersionableModelObject> & HasVersionableFacet> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		protected final PersistenceRequestedOperation _requestedOp;
		
		public CRUDResultOnMultipleBuilderErrorAboutStep<M> because(final Throwable th) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 					_requestedOp,
												 				 					th);
			return new CRUDResultOnMultipleBuilderErrorAboutStep<M>(_userContext,
																	err);
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep<M> becauseClientBadRequest(final String msg,final Object... vars) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 					_requestedOp,
												 				 					Strings.customized(msg,vars),PersistenceErrorType.BAD_REQUEST_DATA);
			return new CRUDResultOnMultipleBuilderErrorAboutStep<M>(_userContext,
																	err);			
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep<M> becauseClientCannotConnectToServer(final SerializedURL serverUrl) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 					_requestedOp,
												 				 					Strings.customized("Cannot connect to server at {}",serverUrl),PersistenceErrorType.CLIENT_CANNOT_CONNECT_SERVER);
			return new CRUDResultOnMultipleBuilderErrorAboutStep<M>(_userContext,
																	err);
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep<M> becauseServerError(String errData,final Object... vars) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 					_requestedOp,
												 				 					Strings.customized(errData,vars),PersistenceErrorType.SERVER_ERROR);
			return new CRUDResultOnMultipleBuilderErrorAboutStep<M>(_userContext,
															 	    err);
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep<M> becauseClientRequestedVersionWasNOTFound() {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 					_requestedOp,
												 				 					PersistenceErrorType.ENTITY_NOT_FOUND);
			return new CRUDResultOnMultipleBuilderErrorAboutStep<M>(_userContext,
															 		err);			
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public static class CRUDResultOnMultipleBuilderErrorAboutStep<M extends PersistableModelObject<? extends OIDForVersionableModelObject> & HasVersionableFacet> { 
		protected final UserContext _userContext;
		protected final CRUDOnMultipleError<M> _err;
		
		public CRUDOnMultipleError<M> about(final VersionIndependentOID entityOid) {
			_err.addTargetEntityIdInfo("versionIndependentOid",entityOid.asString());
			return _err;
		}
		public CRUDOnMultipleError<M> about(final VersionIndependentOID oid,final Date date) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("date",Dates.format(date,Dates.formatAsEpochTimeStamp(date)));
			return _err;
		}
	}
}
