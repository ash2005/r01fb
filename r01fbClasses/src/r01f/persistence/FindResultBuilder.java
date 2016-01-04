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
import r01f.patterns.IsBuilder;
import r01f.persistence.db.DBEntity;
import r01f.persistence.db.DBEntityToModelObjectTransformerBuilder;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Builder type for {@link FindResult}-implementing types:
 * <ul>
 * 		<li>A successful FIND operation result: {@link FindOK}</li>
 * 		<li>An error on a FIND operation execution: {@link FindError}</li>
 * </ul>
 * If the find operation execution was successful and entities are returned:
 * <pre class='brush:java'>
 * 		FindOK<MyEntity> opOK = FindResultBuilder.using(userContext)
 * 											     .on(MyEntity.class)
 * 												  	   .foundEntities(myEntityInstances);
 * </pre>
 * If an error is raised while executing an entity find operation:
 * <pre class='brush:java'>
 * 		FindError<MyEntity> opError = FindResultBuilder.using(userContext)
 * 													   .on(MyEntity.class)
 * 														   	.errorFindingEntities()
 * 																.causedBy(error);
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class FindResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static FindResultBuilderEntityStep using(final UserContext userContext) {
		return new FindResultBuilder() {/* nothing */}
						.new FindResultBuilderEntityStep(userContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindResultBuilderEntityStep {
		private final UserContext _userContext;
		
		public <M extends PersistableModelObject<? extends OID>> 
			   FindResultBuilderOperationStep<M> on(final Class<M> entityType) {
			return new FindResultBuilderOperationStep<M>(_userContext,
														 entityType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindResultBuilderOperationStep<M> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		
		
		//  --------- ERROR
		public FindResultBuilderForError<M> errorFindingEntities() {
			return new FindResultBuilderForError<M>(_userContext,
													_entityType);	
		}
		// ---------- SUCCESS FINDING 
		public FindOK<M> foundEntities(final Collection<M> entities) {
			FindOK<M> outFoundEntities = new FindOK<M>();
			outFoundEntities.setFoundObjectType(_entityType);
			outFoundEntities.setRequestedOperation(PersistenceRequestedOperation.FIND);
			outFoundEntities.setPerformedOperation(PersistencePerformedOperation.FOUND);
			outFoundEntities.setOperationExecResult(entities);	
			return outFoundEntities;
		}
		public <DB extends DBEntity> FindOK<M> foundDBEntities(final Collection<DB> dbEntities) {
			return this.foundDBEntities(dbEntities,
							  			DBEntityToModelObjectTransformerBuilder.<DB,M>createFor(_userContext,
									  													  		_entityType));
		}
		public <DB extends DBEntity> FindOK<M> foundDBEntities(final Collection<DB> dbEntities,
							   			 final Function<DB,M> transformer) {
			Collection<M> entities = null;
			if (CollectionUtils.hasData(dbEntities)) {
				Function<DB,M> dbEntityToModelObjectTransformer = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																								  					transformer);
				entities = FluentIterable.from(dbEntities)
										 .transform(dbEntityToModelObjectTransformer)
										 .toList();
			} else {
				entities = Sets.newHashSet();
			}
			return this.foundEntities(entities);
		}
		public FindOK<M> noEntityFound() {
			FindOK<M> outFoundEntities = new FindOK<M>();
			outFoundEntities.setFoundObjectType(_entityType);
			outFoundEntities.setRequestedOperation(PersistenceRequestedOperation.FIND);
			outFoundEntities.setPerformedOperation(PersistencePerformedOperation.FOUND);
			outFoundEntities.setOperationExecResult(Lists.<M>newArrayList());	// no data found
			return outFoundEntities;
		}
		
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindResultBuilderForError<M> {
		protected final UserContext _userContext;
		protected final Class<M> _entityType;
		
		public FindError<M> causedBy(final Throwable th) {
			return new FindError<M>(_entityType,
									th);
		}
		public FindError<M> causedBy(final String cause) {
			return new FindError<M>(_entityType,
									cause,
									PersistenceErrorType.SERVER_ERROR);
		}
		public FindError<M> causedBy(final String cause,final Object... vars) {
			return this.causedBy(Strings.customized(cause,vars));
		}
		public FindError<M> causedByClientBadRequest(final String msg,final Object... vars) {
			FindError<M> outError = new FindError<M>(_entityType,
											     	 Strings.customized(msg,vars),			// the error message
											     	 PersistenceErrorType.BAD_REQUEST_DATA);	// is a client error?
			return outError;
		}
	}
}
