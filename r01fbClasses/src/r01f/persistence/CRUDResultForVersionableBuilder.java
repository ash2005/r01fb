package r01f.persistence;

import java.util.Collection;
import java.util.Date;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import r01f.guids.VersionIndependentOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.DBEntityToModelObjectTransformerBuilder;
import r01f.persistence.db.TransformsDBEntityIntoModelObject;
import r01f.types.url.Url;
import r01f.usercontext.UserContext;
import r01f.util.types.Dates;
import r01f.util.types.Strings;

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
	
	public CRUDOnMultipleOK<M> deleted(final Collection<M> delOKs) {
		return _buildCRUDOnMultiple(delOKs,
								    _entityType);
	}
	public <DB extends DBEntity> CRUDResultForVersionableBuilderTransformerStep<DB> deletedDBEntities(final Collection<DB> okDBEntities) {
		return new CRUDResultForVersionableBuilderTransformerStep<DB>(okDBEntities);
	}
	public CRUDResultOnMultipleBuilderErrorStep notDeleted() {
		return new CRUDResultOnMultipleBuilderErrorStep(PersistenceRequestedOperation.DELETE);
	}
	public CRUDResultOnMultipleBuilderErrorStep not(final PersistenceRequestedOperation reqOp) {
		return new CRUDResultOnMultipleBuilderErrorStep(reqOp);
	}
	private static <M extends PersistableModelObject<? extends OIDForVersionableModelObject> & HasVersionableFacet>
				   CRUDOnMultipleOK<M> _buildCRUDOnMultiple(final Collection<M> delOKs,
						   									final Class<M> entityType) {
		CRUDOnMultipleOK<M> outMultipleCRUDOKs = new CRUDOnMultipleOK<M>(entityType,
																		 PersistenceRequestedOperation.DELETE,PersistencePerformedOperation.DELETED);
		outMultipleCRUDOKs.addOperationsOK(delOKs,
										   PersistenceRequestedOperation.DELETE);
		return outMultipleCRUDOKs;
	}
/////////////////////////////////////////////////////////////////////////////////////////
// 	DBENTITY TO MODEL OBJECT TRANSFORM
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultForVersionableBuilderTransformerStep<DB extends DBEntity> {
		protected final Collection<DB> _dbEntities;
		
		public CRUDOnMultipleOK<M> transformedToModelObjectUsing(final TransformsDBEntityIntoModelObject<DB,M> dbEntityToModelObjectTransformer) {			
			return this.transformedToModelObjectUsing(DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																										dbEntityToModelObjectTransformer));
		}
		public CRUDOnMultipleOK<M> transformedToModelObjectUsing(final Function<DB,M> transformer) {
			Function<DB,M> dbEntityToModelObjConverter = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																										   transformer);
			
			Collection<M> okEntities = Lists.newArrayListWithExpectedSize(_dbEntities.size());
			for (DB dbEntity : _dbEntities) {
				okEntities.add(dbEntityToModelObjConverter.apply(dbEntity));
			}
			return _buildCRUDOnMultiple(okEntities,
									    _entityType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultOnMultipleBuilderErrorStep {
		protected final PersistenceRequestedOperation _requestedOp;
		
		public CRUDResultOnMultipleBuilderErrorAboutStep because(final Throwable th) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 	_requestedOp,
												 				 	th);
			return new CRUDResultOnMultipleBuilderErrorAboutStep(err);
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep becauseClientBadRequest(final String msg,final Object... vars) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 	_requestedOp,
												 				 	Strings.customized(msg,vars),PersistenceErrorType.BAD_REQUEST_DATA);
			return new CRUDResultOnMultipleBuilderErrorAboutStep(err);			
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep becauseClientCannotConnectToServer(final Url serverUrl) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
								 				 					_requestedOp,
								 				 					Strings.customized("Cannot connect to server at {}",serverUrl),PersistenceErrorType.CLIENT_CANNOT_CONNECT_SERVER);
			return new CRUDResultOnMultipleBuilderErrorAboutStep(err);
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep becauseServerError(String errData,final Object... vars) {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
												 				 	_requestedOp,
												 				 	Strings.customized(errData,vars),PersistenceErrorType.SERVER_ERROR);
			return new CRUDResultOnMultipleBuilderErrorAboutStep(err);
		}
		public CRUDResultOnMultipleBuilderErrorAboutStep becauseClientRequestedVersionWasNOTFound() {
			CRUDOnMultipleError<M> err = new CRUDOnMultipleError<M>(_entityType,
								 				 					_requestedOp,
								 				 					PersistenceErrorType.ENTITY_NOT_FOUND);
			return new CRUDResultOnMultipleBuilderErrorAboutStep(err);			
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultOnMultipleBuilderErrorAboutStep { 
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
