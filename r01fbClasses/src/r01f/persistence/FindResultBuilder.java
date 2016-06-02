package r01f.persistence;

import java.util.Collection;

import com.google.common.base.Function;
import com.google.common.base.Predicates;
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
import r01f.persistence.db.TransformsDBEntityIntoModelObject;
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
	public class FindResultBuilderOperationStep<T> {
		protected final UserContext _userContext;
		protected final Class<T> _entityType;
		
		
		//  --------- ERROR
		public FindResultBuilderForError<T> errorFindingEntities() {
			return new FindResultBuilderForError<T>(_userContext,
													_entityType);	
		}
		// ---------- SUCCESS FINDING 
		public FindOK<T> foundEntities(final Collection<T> entities) {
			return _buildFoundEntitiesCollection(entities,
												 _entityType);
		}
		public <DB extends DBEntity> FindResultBuilderDBEntityTransformerStep<DB,T> foundDBEntities(final Collection<DB> dbEntities) {
			return new FindResultBuilderDBEntityTransformerStep<DB,T>(_userContext,
																	  _entityType,
																	  dbEntities);
		}
		public FindOK<T> noEntityFound() {
			FindOK<T> outFoundEntities = new FindOK<T>();
			outFoundEntities.setFoundObjectType(_entityType);
			outFoundEntities.setRequestedOperation(PersistenceRequestedOperation.FIND);
			outFoundEntities.setPerformedOperation(PersistencePerformedOperation.FOUND);
			outFoundEntities.setOperationExecResult(Lists.<T>newArrayList());	// no data found
			return outFoundEntities;
		}
	}	
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindResultBuilderDBEntityTransformerStep<DB extends DBEntity,
														  T> {
		protected final UserContext _userContext;
		protected final Class<T> _entityType;
		protected final Collection<DB> _dbEntities;
		
		public <M extends PersistableModelObject<? extends OID>> FindOK<M> transformedIntoModelObjectsUsing(final TransformsDBEntityIntoModelObject<DB,M> dbEntityToModelObjectTransformer) {
			return this.transformedIntoModelObjectsUsing(DBEntityToModelObjectTransformerBuilder.<DB,M>createFor(_userContext,
													  													  		 dbEntityToModelObjectTransformer));
		}
		@SuppressWarnings("unchecked")
		public <M extends PersistableModelObject<? extends OID>> FindOK<M> transformedIntoModelObjectsUsing(final Function<DB,M> transformer) {
			Collection<M> entities = null;
			if (CollectionUtils.hasData(_dbEntities)) {
				Function<DB,M> dbEntityToModelObjectTransformer = DBEntityToModelObjectTransformerBuilder.createFor(_userContext,
																								  					transformer);
				entities = FluentIterable.from(_dbEntities)
										 .transform(dbEntityToModelObjectTransformer)
										  .filter(Predicates.notNull())
										  	.toList();
			} else {
				entities = Sets.newHashSet();
			}
			return _buildFoundEntitiesCollection(entities,
												 (Class<M>)_entityType);
		}
	}
	private static <T> FindOK<T> _buildFoundEntitiesCollection(final Collection<T> entities,
															   final Class<T> entityType) {
		FindOK<T> outFoundEntities = new FindOK<T>();
		outFoundEntities.setFoundObjectType(entityType);
		outFoundEntities.setRequestedOperation(PersistenceRequestedOperation.FIND);
		outFoundEntities.setPerformedOperation(PersistencePerformedOperation.FOUND);
		outFoundEntities.setOperationExecResult(entities);
		return outFoundEntities;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindResultBuilderForError<T> {
		protected final UserContext _userContext;
		protected final Class<T> _entityType;
		
		public FindError<T> causedBy(final Throwable th) {
			return new FindError<T>(_entityType,
									th);
		}
		public FindError<T> causedBy(final String cause) {
			return new FindError<T>(_entityType,
									cause,
									PersistenceErrorType.SERVER_ERROR);
		}
		public FindError<T> causedBy(final String cause,final Object... vars) {
			return this.causedBy(Strings.customized(cause,vars));
		}
		public FindError<T> causedByClientBadRequest(final String msg,final Object... vars) {
			FindError<T> outError = new FindError<T>(_entityType,
											     	 Strings.customized(msg,vars),			// the error message
											     	 PersistenceErrorType.BAD_REQUEST_DATA);	// is a client error?
			return outError;
		}
	}
}
