package r01f.persistence;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.model.SummarizedModelObject;
import r01f.patterns.IsBuilder;
import r01f.persistence.db.DBEntity;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Builder type for {@link FindSummariesResult}-implementing types:
 * <ul>
 * 		<li>A successful FIND operation result: {@link FindSummariesOK}</li>
 * 		<li>An error on a FIND operation execution: {@link FindSummariesError}</li>
 * </ul>
 * If the find operation execution was successful and entities are returned:
 * <pre class='brush:java'>
 * 		FindOK<MyEntity> opOK = FindSummariesResultBuilder.using(userContext)
 * 											     		  .on(MyEntity.class)
 * 												  	   	  .foundSummaries(myEntitySummaries);
 * </pre>
 * If an error is raised while executing an entity find operation:
 * <pre class='brush:java'>
 * 		FindError<MyEntity> opError = FindSummariesResultBuilder.using(userContext)
 * 													   		    .on(MyEntity.class)
 * 														   		.errorFindingSummaries()
 * 																.causedBy(error);
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class FindSummariesResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static FindSummariesResultBuilderEntityStep using(final UserContext userContext) {
		return new FindSummariesResultBuilder() {/* nothing */}
						.new FindSummariesResultBuilderEntityStep(userContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindSummariesResultBuilderEntityStep {
		private final UserContext _userContext;
		
		public <M extends PersistableModelObject<? extends OID>> 
			   FindSummariesResultBuilderOperationStep<M> on(final Class<M> entityType) {
			return new FindSummariesResultBuilderOperationStep<M>(_userContext,
														 		  entityType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindSummariesResultBuilderOperationStep<M extends PersistableModelObject<? extends OID>> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		
		//  --------- ERROR
		public FindSummariesResultBuilderForError<M> errorFindingSummaries() {
			return new FindSummariesResultBuilderForError<M>(_userContext,
														     _entityType);	
		}
		// ---------- SUCCESS FINDING 
		public <S extends SummarizedModelObject<M>>
			   FindSummariesOK<M> foundSummaries(final Collection<S> summaries) {
			return _modelObjectSummariesFrom(summaries,
											 _entityType);
		}
		public <DB extends DBEntity> FindSummariesResultBuilderDBEntityTransformStep<DB,M> foundDBEntities(final Collection<DB> dbEntities) {
			return new FindSummariesResultBuilderDBEntityTransformStep<DB,M>(_userContext,
																			 _entityType,
																			 dbEntities);
		}
		public FindOnModelObjectOK<M> noSummaryFound() {
			FindOnModelObjectOK<M> outFoundEntities = new FindOnModelObjectOK<M>();
			outFoundEntities.setModelObjectType(_entityType);
			outFoundEntities.setRequestedOperation(PersistenceRequestedOperation.FIND);
			outFoundEntities.setPerformedOperation(PersistencePerformedOperation.FOUND);
			outFoundEntities.setOperationExecResult(Lists.<M>newArrayList());	// no data found
			return outFoundEntities;
		}
	}	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindSummariesResultBuilderDBEntityTransformStep<DB extends DBEntity,
																 M extends PersistableModelObject<? extends OID>> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		protected final Collection<DB> _dbEntities;
		
		public <S extends SummarizedModelObject<M>>
			   FindSummariesOK<M> transformedToSummarizedModelObjectUsing(final Function<DB,S> transformer) {
			Collection<? extends SummarizedModelObject<M>> summaries = null;
			if (CollectionUtils.hasData(_dbEntities)) {
				summaries = FluentIterable.from(_dbEntities)
										  .transform(transformer)
										  .toList();
			} else {
				summaries = Sets.newHashSet();
			}
			return _modelObjectSummariesFrom(summaries,
											 _entityType);
		}
	}
	private static <M extends PersistableModelObject<? extends OID>,
					S extends SummarizedModelObject<M>>
		   FindSummariesOK<M> _modelObjectSummariesFrom(final Collection<S> summaries,
				   										final Class<M> entityType) {
		FindSummariesOK<M> outFoundSummaries = new FindSummariesOK<M>();
		outFoundSummaries.setModelObjectType(entityType);
		outFoundSummaries.setRequestedOperation(PersistenceRequestedOperation.FIND);
		outFoundSummaries.setPerformedOperation(PersistencePerformedOperation.FOUND);
		outFoundSummaries.setOperationExecResult(summaries);	
		return outFoundSummaries;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindSummariesResultBuilderForError<M extends PersistableModelObject<? extends OID>> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		
		public FindSummariesError<M> causedBy(final Throwable th) {
			return new FindSummariesError<M>(_entityType,
										     th);
		}
		public FindSummariesError<M> causedBy(final String cause) {
			return new FindSummariesError<M>(_entityType,
										     cause,
										     PersistenceErrorType.SERVER_ERROR);
		}
		public FindSummariesError<M> causedByClientBadRequest(final String msg,final Object... vars) {
			FindSummariesError<M> outError = new FindSummariesError<M>(_entityType,
											     	 			   	   Strings.customized(msg,vars),			// the error message
											     	 			   	   PersistenceErrorType.BAD_REQUEST_DATA);	// is a client error?
			return outError;
		}
	}
}
