package r01f.persistence;

import java.util.Date;

import com.google.common.base.Function;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.guids.OID;
import r01f.guids.VersionIndependentOID;
import r01f.guids.VersionOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.HasOID;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.patterns.IsBuilder;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.DBEntityToModelObjectTransformerBuilder;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;
import r01f.util.types.Dates;
import r01f.util.types.Strings;
import r01f.validation.ObjectValidationResults.ObjectValidationResultNOK;

/**
 * Builder type for {@link CRUDResult}-implementing types:
 * <ul>
 * 		<li>A successful CRUD operation result on a single entity: {@link CRUDOK}</li>
 * 		<li>An error on a CRUD operation execution on a single entity: {@link CRUDError}</li>
 * </ul>
 * If the operation execution was successful:
 * <pre class='brush:java'>
 * 		CRUDOK<MyEntity> opOK = CRUDResultBuilder.using(userContext)
 * 											     .on(MyEntity.class)
 * 												 .loaded()
 * 													.entity(myEntityInstance);
 * 		CRUDOK<MyEntity> opOK = CRUDResultBuilder.using(userContext)
 * 											     .on(MyEntity.class)
 * 												 .created()
 * 													.entity(myEntityInstance);
 * </pre>
 * If the client requested to load an entity BUT it was NOT found:
 * <pre class='brush:java'>
 * 		CRUDError<MyEntity> opError = CRUDResultBuilder.using(userContext)
 * 													   .on(MyEntity.class)
 * 													   .notLoaded()
 * 															.becauseClientRequestedEntityWasNOTFound()
 * 																.about(requestedEntityOid);
 * </pre>
 * If an error is raised while executing the persistence operation:
 * <pre class='brush:java'>
 * 		CRUDError<MyEntity> opError = CRUDResultBuilder.using(userContext)
 * 													   .on(MyEntity.class)
 * 													   .notLoaded()
 * 													   .because(error)
 * 														 	.about(myEntityOid);
 * </pre>
 * If multiple entities are affected by the operation (ie: the deletion of all entity versions)
 * <pre class='brush:java'>
 * 		CRUDResultOnMultipleEntities<MyEntity> opResult = CRUDResultBuilder.using(userContext)
 * 																		   .on(MyEntity.class)
 * 																		   .versions()
 * 																				.deleted(aDeletedEntitiesCol);
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class CRUDResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static CRUDResultBuilderEntityStep using(final UserContext userContext) {
		return new CRUDResultBuilder() {/* nothing */}
						.new CRUDResultBuilderEntityStep(userContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultBuilderEntityStep {
		private final UserContext _userContext;
		
		public <T> CRUDResultBuilderOperationStep<T> on(final Class<T> entityType) {
			return new CRUDResultBuilderOperationStep<T>(_userContext,
														 entityType);
		}
		public <M extends PersistableModelObject<? extends OIDForVersionableModelObject> & HasVersionableFacet> 
			   CRUDVersionableResultBuilderOperationStep<M> onVersionable(final Class<M> entityType) {
			return new CRUDVersionableResultBuilderOperationStep<M>(_userContext,
																	entityType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	public class CRUDResultBuilderOperationStep<T> 
		 extends CRUDResultBuilderOperationStepBase<T> {
		public CRUDResultBuilderOperationStep(final UserContext userContext,
											  final Class<T> entityType) {
			super(userContext,
				  entityType);
		}
	}
	public class CRUDVersionableResultBuilderOperationStep<M extends PersistableModelObject<? extends OIDForVersionableModelObject> & HasVersionableFacet> 
		 extends CRUDResultBuilderOperationStepBase<M> {
		public CRUDVersionableResultBuilderOperationStep(final UserContext userContext,
														 final Class<M> entityType) {
			super(userContext,
				  entityType);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	private class CRUDResultBuilderOperationStepBase<T> {
		protected final UserContext _userContext;
		protected final Class<T> _entityType;
		
		//  --------- ERROR
		public CRUDResultBuilderForErrorAboutStep<T> badClientRequestData(final PersistenceRequestedOperation reqOp,
																		  final String msg,final Object... vars) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
												reqOp,
												Strings.customized(msg,vars),PersistenceErrorType.BAD_REQUEST_DATA);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
														     err);
		}
		public CRUDResultBuilderForErrorStep<T> not(final PersistenceRequestedOperation reqOp) {
			return new CRUDResultBuilderForErrorStep<T>(_userContext,
														_entityType,
														reqOp);
		}
		public CRUDResultBuilderForErrorStep<T> notLoaded() {
			return new CRUDResultBuilderForErrorStep<T>(_userContext,
														_entityType,
														PersistenceRequestedOperation.LOAD);	
		}
		public CRUDResultBuilderForCreateError<T> notCreated() {
			return new CRUDResultBuilderForCreateError<T>(_userContext,
														  _entityType);	
		}
		public CRUDResultBuilderForUpdateError<T>  notUpdated() {
			return new CRUDResultBuilderForUpdateError<T>(_userContext,
														  _entityType);	
		}
		public CRUDResultBuilderForErrorStep<T> notDeleted() {
			return new CRUDResultBuilderForErrorStep<T>(_userContext,
														_entityType,
														PersistenceRequestedOperation.DELETE);	
		}
		// --------- SUCCESS
		public PersistenceOperationResultBuilderForOK<T> executed(final PersistenceRequestedOperation requestedOp,
																  				final PersistencePerformedOperation performedOp) {
			return new PersistenceOperationResultBuilderForOK<T>(_userContext,
														 		 _entityType,
														 		 requestedOp,performedOp);
		}
		public PersistenceOperationResultBuilderForOK<T> loaded() {
			return new PersistenceOperationResultBuilderForOK<T>(_userContext,
														 	     _entityType,
														 		 PersistenceRequestedOperation.LOAD,PersistencePerformedOperation.LOADED);
		}
		public PersistenceOperationResultBuilderForOK<T> created() {
			return new PersistenceOperationResultBuilderForOK<T>(_userContext,
														 		 _entityType,
														 		 PersistenceRequestedOperation.CREATE,PersistencePerformedOperation.CREATED);
		}
		public PersistenceOperationResultBuilderForOK<T> updated() {
			return new PersistenceOperationResultBuilderForOK<T>(_userContext,
														 		 _entityType,
														 		 PersistenceRequestedOperation.UPDATE,PersistencePerformedOperation.UPDATED);
		}
		public PersistenceOperationResultBuilderForOK<T> deleted() {
			return new PersistenceOperationResultBuilderForOK<T>(_userContext,
														 		 _entityType,
														 		 PersistenceRequestedOperation.DELETE,PersistencePerformedOperation.DELETED);
		}
		// --------- MULTIPLE SUCCESS
		@SuppressWarnings("unchecked")
		public <MV extends PersistableModelObject<? extends OIDForVersionableModelObject> & HasVersionableFacet>
			  CRUDResultForVersionableBuilder<MV> versionable() {
			return new CRUDResultForVersionableBuilder<MV>(_userContext,
							 							   (Class<MV>)_entityType);
		}
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultBuilderForErrorStep<T> {
		protected final UserContext _userContext;
		protected final Class<T> _entityType;
		protected final PersistenceRequestedOperation _requestedOp;
		
		public CRUDResultBuilderForErrorAboutStep<T> because(final Throwable th) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
							    				_requestedOp,
							    				th);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
												 			 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> because(final CRUDError<?> otherCRUDError) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
												otherCRUDError);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseClientCannotConnectToServer(final SerializedURL serverUrl) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
							    				_requestedOp,
							    				Strings.customized("Cannot connect to server at {}",serverUrl),PersistenceErrorType.CLIENT_CANNOT_CONNECT_SERVER);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseServerError(final String errData,final Object... vars) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
												_requestedOp,
												Strings.customized(errData,vars),PersistenceErrorType.SERVER_ERROR);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseClientError(final PersistenceErrorType errorType,
																		final String msg,final Object... vars) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),errorType);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseClientBadRequest(final String msg,final Object... vars) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),PersistenceErrorType.BAD_REQUEST_DATA);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseClientRequestedEntityWasNOTFound() {
			CRUDError<T> err = new CRUDError<T>(_entityType,
							   					_requestedOp,
							   					PersistenceErrorType.ENTITY_NOT_FOUND);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseRequiredRelatedEntityWasNOTFound(final String msg,final Object... vars) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),PersistenceErrorType.RELATED_REQUIRED_ENTITY_NOT_FOUND);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);		
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultBuilderForErrorAboutStep<T> { 
		protected final UserContext _userContext;
		protected final CRUDError<T> _err;
		
		public CRUDError<T> build() {
			return _err;
		}
		public CRUDResultBuilderForErrorExtErrorCodeStep<T> about(final String meta,final String value) {
			_err.addTargetEntityIdInfo(meta,value);
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																    _err);
		}
		public <O extends OID> CRUDResultBuilderForErrorExtErrorCodeStep<T> about(final O entityOid) {
			_err.addTargetEntityIdInfo("oid",entityOid.asString());
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																    _err);
		}
		public CRUDResultBuilderForErrorExtErrorCodeStep<T> about(final T entity) {
			_err.setTargetEntity(entity);
			if (entity instanceof HasOID) _err.addTargetEntityIdInfo("oid",((HasOID<?>)entity).getOid().asString());
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																	_err);
		}
		public CRUDResultBuilderForErrorExtErrorCodeStep<T> about(final VersionIndependentOID oid) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																	_err);
		}
		public CRUDResultBuilderForErrorExtErrorCodeStep<T> about(final VersionIndependentOID oid,final VersionOID version) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("version",version.asString());
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																	_err);
		}
		public CRUDResultBuilderForErrorExtErrorCodeStep<T> about(final VersionIndependentOID oid,final Date date) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("date",Dates.epochTimeStampAsString(date.getTime()));
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																	_err);
		}
		public CRUDResultBuilderForErrorExtErrorCodeStep<T> aboutWorkVersion(final VersionIndependentOID oid) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("version","workVersion");
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																    _err);
		}
		public CRUDResultBuilderForErrorExtErrorCodeStep<T> about(final VersionIndependentOID oid,final Object version) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			if (version instanceof Date) { 
				_err.addTargetEntityIdInfo("date",Dates.epochTimeStampAsString(((Date)version).getTime()));
			} else if (version instanceof VersionOID) {
				_err.addTargetEntityIdInfo("version",((VersionOID)version).asString());
			} else if (version instanceof String || version == null) {
				_err.addTargetEntityIdInfo("version","workVersion");	
			}
			return new CRUDResultBuilderForErrorExtErrorCodeStep<T>(_userContext,
																	_err);
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultBuilderForErrorExtErrorCodeStep<T> { 
		protected final UserContext _userContext;
		protected final CRUDError<T> _err;
		
		public CRUDError<T> buildWithExtendedErrorCode(final int extErrCode) {
			_err.setExtendedErrorCode(extErrCode);
			return _err;
		}
		public CRUDError<T> build() {
			return _err;
		}
	}
	private abstract class PersistenceCRUDResultBuilderForMutatorErrorBase<T>
			       extends CRUDResultBuilderForErrorStep<T> {
		public PersistenceCRUDResultBuilderForMutatorErrorBase(final UserContext userContext,
								   	   				   		   final Class<T> entityType,
								   	   				   		   final PersistenceRequestedOperation reqOp) {
			super(userContext,
				  entityType,
				  reqOp);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseOptimisticLockingError() {
			CRUDError<T> err = new CRUDError<T>(_entityType,
							    				_requestedOp,
							    				PersistenceErrorType.OPTIMISTIC_LOCKING_ERROR);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseClientSentEntityValidationErrors(final ObjectValidationResultNOK<T> validNOK) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
							    				_requestedOp,
							    				validNOK.getReason(),PersistenceErrorType.ENTITY_NOT_VALID);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
	}
	public class CRUDResultBuilderForCreateError<T>
		 extends PersistenceCRUDResultBuilderForMutatorErrorBase<T> {
		public CRUDResultBuilderForCreateError(final UserContext userContext,
										  	   final Class<T> entityType) {
			super(userContext,
				  entityType,
				  PersistenceRequestedOperation.CREATE);
		}
		public CRUDResultBuilderForCreateError(final UserContext userContext,
								  	   		   final Class<T> entityType,
								  	   		   final PersistenceRequestedOperation reqOp) {
			super(userContext,
				  entityType,
				  reqOp);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseClientRequestedEntityAlreadyExists() {
			CRUDError<T> err = new CRUDError<T>(_entityType,
											    _requestedOp,
											    PersistenceErrorType.ENTITY_ALREADY_EXISTS);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
	}
	public class CRUDResultBuilderForUpdateError<T>
		 extends PersistenceCRUDResultBuilderForMutatorErrorBase<T> {

		public CRUDResultBuilderForUpdateError(final UserContext userContext,
										  	   final Class<T> entityType) {
			super(userContext,
				  entityType,
				  PersistenceRequestedOperation.UPDATE);
		}
		public CRUDResultBuilderForErrorAboutStep<T> becauseTargetEntityWasInAnIllegalStatus(final String msg,final Object... vars) {
			CRUDError<T> err = new CRUDError<T>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),PersistenceErrorType.ILLEGAL_STATUS);
			return new CRUDResultBuilderForErrorAboutStep<T>(_userContext,
															 err);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EXECUTED
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class PersistenceOperationResultBuilderForOK<T> {
		protected final UserContext _userContext;
		protected final Class<T> _entityType;
		protected final PersistenceRequestedOperation _requestedOp;
		protected final PersistencePerformedOperation _performedOp;
		
		public CRUDOK<T> entity(final T entity) {
			CRUDOK<T> outPersistenceOpResult = new CRUDOK<T>(_entityType,
											 				 _requestedOp,_performedOp,
											 				 entity);
			return outPersistenceOpResult;			
		}
		public CRUDOK<T> dbEntity(final DBEntity dbEntity) {			
			Function<DBEntity,T> defaultDBEntityToModelObjConverter = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																														_entityType);
			T obj = defaultDBEntityToModelObjConverter.apply(dbEntity);
			CRUDOK<T> outPersistenceOpResult = new CRUDOK<T>(_entityType,
											 				 _requestedOp,_performedOp,
											 				 obj);
			return outPersistenceOpResult;
		}
		public CRUDOK<T> dbEntity(final DBEntity dbEntity,
								  		  final Function<DBEntity,T> transformer) {
			Function<DBEntity,T> dbEntityToModelObjConverter = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																												  transformer);
			T obj = dbEntityToModelObjConverter.apply(dbEntity);
			CRUDOK<T> outPersistenceOpResult = new CRUDOK<T>(_entityType,
				 						     				 _requestedOp,_performedOp,
				 						     				 obj);
			return outPersistenceOpResult;
		}
	}
}
