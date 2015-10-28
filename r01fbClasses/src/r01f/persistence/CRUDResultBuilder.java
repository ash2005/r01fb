package r01f.persistence;

import java.util.Date;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.guids.OID;
import r01f.guids.VersionIndependentOID;
import r01f.guids.VersionOID;
import r01f.model.OIDForVersionableModelObject;
import r01f.model.PersistableModelObject;
import r01f.model.facets.Versionable.HasVersionableFacet;
import r01f.patterns.IsBuilder;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.DBEntityToModelObjectTransformerBuilder;
import r01f.persistence.db.ModelObjectValidationResults.ModelObjectValidationResultNOK;
import r01f.types.weburl.SerializedURL;
import r01f.usercontext.UserContext;
import r01f.util.types.Dates;
import r01f.util.types.Strings;

import com.google.common.base.Function;

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
		
		public <O extends OID,M extends PersistableModelObject<O>> 
			   CRUDResultBuilderOperationStep<M> on(final Class<M> entityType) {
			return new CRUDResultBuilderOperationStep<M>(_userContext,
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
	public class CRUDResultBuilderOperationStep<M extends PersistableModelObject<? extends OID>> 
		 extends CRUDResultBuilderOperationStepBase<M> {
		public CRUDResultBuilderOperationStep(final UserContext userContext,
											  final Class<M> entityType) {
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
	private class CRUDResultBuilderOperationStepBase<M extends PersistableModelObject<? extends OID>> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		
		//  --------- ERROR
		public CRUDResultBuilderForErrorAboutStep<M> badClientRequestData(final PersistenceRequestedOperation reqOp,
																		  final String msg,final Object... vars) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
												reqOp,
												Strings.customized(msg,vars),PersistenceErrorType.BAD_REQUEST_DATA);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
																		err);
		}
		public CRUDResultBuilderForErrorStep<M> not(final PersistenceRequestedOperation reqOp) {
			return new CRUDResultBuilderForErrorStep<M>(_userContext,
														_entityType,
														reqOp);
		}
		public CRUDResultBuilderForErrorStep<M> notLoaded() {
			return new CRUDResultBuilderForErrorStep<M>(_userContext,
														_entityType,
														PersistenceRequestedOperation.LOAD);	
		}
		public CRUDResultBuilderForCreateError<M> notCreated() {
			return new CRUDResultBuilderForCreateError<M>(_userContext,
														  _entityType);	
		}
		public CRUDResultBuilderForUpdateError<M>  notUpdated() {
			return new CRUDResultBuilderForUpdateError<M>(_userContext,
														  _entityType);	
		}
		public CRUDResultBuilderForErrorStep<M> notDeleted() {
			return new CRUDResultBuilderForErrorStep<M>(_userContext,
														_entityType,
														PersistenceRequestedOperation.DELETE);	
		}
		// --------- SUCCESS
		public PersistenceOperationResultBuilderForOK<M> executed(final PersistenceRequestedOperation requestedOp,
																  final PersistencePerformedOperation performedOp) {
			return new PersistenceOperationResultBuilderForOK<M>(_userContext,
																 _entityType,
																 requestedOp,performedOp);
		}
		public PersistenceOperationResultBuilderForOK<M> loaded() {
			return new PersistenceOperationResultBuilderForOK<M>(_userContext,
																 _entityType,
																 PersistenceRequestedOperation.LOAD,PersistencePerformedOperation.LOADED);
		}
		public PersistenceOperationResultBuilderForOK<M> created() {
			return new PersistenceOperationResultBuilderForOK<M>(_userContext,
																 _entityType,
																 PersistenceRequestedOperation.CREATE,PersistencePerformedOperation.CREATED);
		}
		public PersistenceOperationResultBuilderForOK<M> updated() {
			return new PersistenceOperationResultBuilderForOK<M>(_userContext,
																 _entityType,
																 PersistenceRequestedOperation.UPDATE,PersistencePerformedOperation.UPDATED);
		}
		public PersistenceOperationResultBuilderForOK<M> deleted() {
			return new PersistenceOperationResultBuilderForOK<M>(_userContext,
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
	public class CRUDResultBuilderForErrorStep<M extends PersistableModelObject<? extends OID>> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		protected final PersistenceRequestedOperation _requestedOp;
		
		public CRUDResultBuilderForErrorAboutStep<M> because(final Throwable th) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
											    _requestedOp,
												th);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
												 			 err);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseClientCannotConnectToServer(final SerializedURL serverUrl) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
											    _requestedOp,
											    Strings.customized("Cannot connect to server at {}",serverUrl),PersistenceErrorType.CLIENT_CANNOT_CONNECT_SERVER);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseServerError(final String errData,final Object... vars) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
												_requestedOp,
												Strings.customized(errData,vars),PersistenceErrorType.SERVER_ERROR);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseClientError(final PersistenceErrorType errorType,
																		final String msg,final Object... vars) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),errorType);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseClientBadRequest(final String msg,final Object... vars) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),PersistenceErrorType.BAD_REQUEST_DATA);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseClientRequestedEntityWasNOTFound() {
			CRUDError<M> err = new CRUDError<M>(_entityType,
											   	_requestedOp,
											   	PersistenceErrorType.ENTITY_NOT_FOUND);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseRequiredRelatedEntityWasNOTFound(final String msg,final Object... vars) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),PersistenceErrorType.RELATED_REQUIRED_ENTITY_NOT_FOUND);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);		
		}
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class CRUDResultBuilderForErrorAboutStep<M extends PersistableModelObject<? extends OID>> { 
		protected final UserContext _userContext;
		protected final CRUDError<M> _err;
		
		public CRUDError<M> build() {
			return _err;
		}
		public CRUDError<M> about(final String meta,final String value) {
			_err.addTargetEntityIdInfo(meta,value);
			return _err;
		}
		public <O extends OID> CRUDError<M> about(final O entityOid) {
			_err.addTargetEntityIdInfo("oid",entityOid.asString());
			return _err;
		}
		public CRUDError<M> about(final M entity) {
			_err.setTargetEntity(entity);
			_err.addTargetEntityIdInfo("oid",entity.getOid().asString());
			return _err;
		}
		public CRUDError<M> about(final VersionIndependentOID oid) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			return _err;
		}
		public CRUDError<M> about(final VersionIndependentOID oid,final VersionOID version) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("version",version.asString());
			return _err;
		}
		public CRUDError<M> about(final VersionIndependentOID oid,final Date date) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("date",Dates.epochTimeStampAsString(date.getTime()));
			return _err;
		}
		public CRUDError<M> aboutWorkVersion(final VersionIndependentOID oid) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			_err.addTargetEntityIdInfo("version","workVersion");
			return _err;
		}
		public CRUDError<M> about(final VersionIndependentOID oid,final Object version) {
			_err.addTargetEntityIdInfo("versionIndependentOid",oid.asString());
			if (version instanceof Date) { 
				_err.addTargetEntityIdInfo("date",Dates.epochTimeStampAsString(((Date)version).getTime()));
			} else if (version instanceof VersionOID) {
				_err.addTargetEntityIdInfo("version",((VersionOID)version).asString());
			} else if (version instanceof String || version == null) {
				_err.addTargetEntityIdInfo("version","workVersion");	
			}
			return _err;
		}
	}
	private abstract class PersistenceCRUDResultBuilderForMutatorErrorBase<M extends PersistableModelObject<? extends OID>>
			       extends CRUDResultBuilderForErrorStep<M> {
		public PersistenceCRUDResultBuilderForMutatorErrorBase(final UserContext userContext,
										   	   				   final Class<M> entityType,
										   	   				   final PersistenceRequestedOperation reqOp) {
			super(userContext,
				  entityType,
				  reqOp);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseOptimisticLockingError() {
			CRUDError<M> err = new CRUDError<M>(_entityType,
											    _requestedOp,
												PersistenceErrorType.OPTIMISTIC_LOCKING_ERROR);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseClientSentEntityValidationErrors(final ModelObjectValidationResultNOK<M> validNOK) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
											    _requestedOp,
												validNOK.getReason(),PersistenceErrorType.ENTITY_NOT_VALID);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
	}
	public class CRUDResultBuilderForCreateError<M extends PersistableModelObject<? extends OID>>
		 extends PersistenceCRUDResultBuilderForMutatorErrorBase<M> {
		public CRUDResultBuilderForCreateError(final UserContext userContext,
										  	   final Class<M> entityType) {
			super(userContext,
				  entityType,
				  PersistenceRequestedOperation.CREATE);
		}
		public CRUDResultBuilderForCreateError(final UserContext userContext,
										  	   final Class<M> entityType,
										  	   final PersistenceRequestedOperation reqOp) {
			super(userContext,
				  entityType,
				  reqOp);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseClientRequestedEntityAlreadyExists() {
			CRUDError<M> err = new CRUDError<M>(_entityType,
												_requestedOp,
												PersistenceErrorType.ENTITY_ALREADY_EXISTS);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
	}
	public class CRUDResultBuilderForUpdateError<M extends PersistableModelObject<? extends OID>>
		 extends PersistenceCRUDResultBuilderForMutatorErrorBase<M> {

		public CRUDResultBuilderForUpdateError(final UserContext userContext,
										  	   final Class<M> entityType) {
			super(userContext,
				  entityType,
				  PersistenceRequestedOperation.UPDATE);
		}
		public CRUDResultBuilderForErrorAboutStep<M> becauseTargetEntityWasInAnIllegalStatus(final String msg,final Object... vars) {
			CRUDError<M> err = new CRUDError<M>(_entityType,
												_requestedOp,
												Strings.customized(msg,vars),PersistenceErrorType.ILLEGAL_STATUS);
			return new CRUDResultBuilderForErrorAboutStep<M>(_userContext,
															 err);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EXECUTED
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class PersistenceOperationResultBuilderForOK<M extends PersistableModelObject<? extends OID>> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		protected final PersistenceRequestedOperation _requestedOp;
		protected final PersistencePerformedOperation _performedOp;
		
		public CRUDOK<M> entity(final M entity) {
			CRUDOK<M> outPersistenceOpResult = new CRUDOK<M>(_entityType,
															 _requestedOp,_performedOp,
															 entity);
			return outPersistenceOpResult;			
		}
		public CRUDOK<M> dbEntity(final DBEntity dbEntity) {
			Function<DBEntity,M> defaultDBEntityToModelObjConverter = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																														_entityType);
			M modelObject = defaultDBEntityToModelObjConverter.apply(dbEntity);
			CRUDOK<M> outPersistenceOpResult = new CRUDOK<M>(_entityType,
															 _requestedOp,_performedOp,
															 modelObject);
			return outPersistenceOpResult;
		}
		public CRUDOK<M> dbEntity(final DBEntity dbEntity,
								  final Function<DBEntity,M> transformer) {
			Function<DBEntity,M> dbEntityToModelObjConverter = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																												  transformer);
			M modelObject = dbEntityToModelObjConverter.apply(dbEntity);
			CRUDOK<M> outPersistenceOpResult = new CRUDOK<M>(_entityType,
								 						     _requestedOp,_performedOp,
															 modelObject);
			return outPersistenceOpResult;
		}
	}
}
