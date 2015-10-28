package r01f.persistence;

import java.util.Collection;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.patterns.IsBuilder;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;

import com.google.common.collect.Lists;

/**
 * Builder type for {@link FindOIDsResult}-implementing types:
 * <ul>
 * 		<li>A successful FIND operation result: {@link FindOIDsOK}</li>
 * 		<li>An error on a FIND operation execution: {@link FindOIDsError}</li>
 * </ul>
 * If the find operation execution was successful and oids are returned:
 * <pre class='brush:java'>
 * 		FindOIDsOK<MyEntityOID> opOK = FindOIDsResultBuilder.using(userContext)
 * 											    		    .on(MyEntity.class)
 * 												  	   			.foundEntitiesWithOids(myEntityOids);
 * </pre>
 * If an error is raised while executing an entity find operation:
 * <pre class='brush:java'>
 * 		FindError<MyEntityOID> opError = FindOIDsResultBuilder.using(userContext)
 * 													   		  .on(MyEntity.class)
 * 														   			.errorFindingOids()
 * 																		.causedBy(error);
 * </pre>
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public class FindOIDsResultBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static FindOIDsResultBuilderEntityStep using(final UserContext userContext) {
		return new FindOIDsResultBuilder() {/* ignore */}
						.new FindOIDsResultBuilderEntityStep(userContext);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindOIDsResultBuilderEntityStep {
		private final UserContext _userContext;
		
		public FindOIDsResultBuilderOperationStep on(final Class<? extends PersistableModelObject<? extends OID>> entityType) {
			return new FindOIDsResultBuilderOperationStep(_userContext,
														  entityType);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindOIDsResultBuilderOperationStep {
		protected final UserContext _userContext;
		protected final Class<? extends PersistableModelObject<? extends OID>> _entityType;

		
		//  --------- ERROR
		public FindOIDsResultBuilderForError errorFindingOids() {
			return new FindOIDsResultBuilderForError(_userContext,
													 _entityType);	
		}
		// ---------- SUCCESS FINDING 
		public <O extends OID> FindOIDsOK<O> foundEntitiesWithOids(final Collection<O> oids) {
			FindOIDsOK<O> outFoundOids = new FindOIDsOK<O>();
			outFoundOids.setModelObjectType(_entityType);
			outFoundOids.setRequestedOperation(PersistenceRequestedOperation.FIND);
			outFoundOids.setPerformedOperation(PersistencePerformedOperation.FOUND);
			outFoundOids.setOperationExecResult(oids);	
			return outFoundOids;
		}
		public <O extends OID> FindOIDsOK<O> noEntityFound() {
			FindOIDsOK<O> outFoundOids = new FindOIDsOK<O>();
			outFoundOids.setModelObjectType(_entityType);
			outFoundOids.setRequestedOperation(PersistenceRequestedOperation.FIND);
			outFoundOids.setPerformedOperation(PersistencePerformedOperation.FOUND);
			outFoundOids.setOperationExecResult(Lists.<O>newArrayList());	// no data found
			return outFoundOids;
		}
		
	}	
/////////////////////////////////////////////////////////////////////////////////////////
//  ERROR
/////////////////////////////////////////////////////////////////////////////////////////
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)
	public class FindOIDsResultBuilderForError {
		protected final UserContext _userContext;
		protected final Class<? extends PersistableModelObject<? extends OID>> _entityType;
		
		public <O extends OID> FindOIDsError<O> causedBy(final Throwable th) {
			return new FindOIDsError<O>(_entityType,
										th);
		}
		public <O extends OID> FindOIDsError<O> causedBy(final String cause) {
			return new FindOIDsError<O>(_entityType,
										cause,
										PersistenceErrorType.SERVER_ERROR);
		}
		public <O extends OID> FindOIDsError<O> causedByClientBadRequest(final String msg,final Object... vars) {
			FindOIDsError<O> outError = new FindOIDsError<O>(_entityType,
											     	 		 Strings.customized(msg,vars),			// the error message
											     	 		 PersistenceErrorType.BAD_REQUEST_DATA);	// is a client error?
			return outError;
		}
	}
}
