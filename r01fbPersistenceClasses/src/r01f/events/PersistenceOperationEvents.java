package r01f.events;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.guids.OID;
import r01f.model.PersistableModelObject;
import r01f.persistence.CRUDError;
import r01f.persistence.CRUDOK;
import r01f.persistence.PersistenceOperationError;
import r01f.persistence.PersistenceOperationOK;
import r01f.persistence.PersistenceOperationOnModelObjectResult;
import r01f.persistence.PersistenceOperationResult;
import r01f.usercontext.UserContext;
import r01f.util.types.Strings;

/**
 * CRUD operation events
 */
public class PersistenceOperationEvents {
/////////////////////////////////////////////////////////////////////////////////////////
//  Base type
/////////////////////////////////////////////////////////////////////////////////////////	
	@Accessors(prefix="_")
	@RequiredArgsConstructor
	static abstract class PersistenceOperationEventBase
			   implements Debuggable {
		/**
		 * The user context
		 */
		@Getter private final UserContext _userContext;
		/**
		 * The operation result 
		 */
		@Getter protected final PersistenceOperationResult _operationResult;
		
		@Override
		public CharSequence debugInfo() {
			String dbg = null;
			// [Success]
			if (_operationResult.hasSucceeded()) {
				if (_operationResult instanceof PersistenceOperationOnModelObjectResult) {
					PersistenceOperationOnModelObjectResult<?> opOK = (PersistenceOperationOnModelObjectResult<?>)_operationResult;
					dbg = Strings.of("Successful '{}' operation about {}")
								 .customizeWith(opOK.getRequestedOperation(),
										 		opOK.getModelObjectType())
								 .asString();
				} else {
					PersistenceOperationOK opOK = (PersistenceOperationOK)_operationResult;
					dbg = Strings.customized("Successful '{}' persistence operation",
									 		 opOK.getRequestedOperationName());
				}
			} 
			// [Error]
			else {
				if (_operationResult instanceof PersistenceOperationOnModelObjectResult) {
					PersistenceOperationError opError = (PersistenceOperationError)_operationResult;
					PersistenceOperationOnModelObjectResult<?> opErrOnModelObj = (PersistenceOperationOnModelObjectResult<?>)_operationResult;
					dbg = Strings.customized("Failed '{}' operation about {}: ({} error) --> {}" + 
										     "\t-Client Error: {}\n" + 
										     "\t-Message: {}",
										     opErrOnModelObj.getRequestedOperation(),
										     opErrOnModelObj.getModelObjectType(),
										     (opError.wasBecauseAClientError() ? "CLIENT" : "SERVER"),
										     opError.getErrorMessage());
				} else {
					PersistenceOperationError opError = (PersistenceOperationError)_operationResult;
					dbg = Strings.customized("Failed '{}' operation: ({} error) --> {}" + 
										     "\t-Message: {}",
										     opError.getRequestedOperationName(),
										     (opError.wasBecauseAClientError() ? "CLIENT" : "SERVER"),
										     opError.getErrorMessage());
				}
			}
			return dbg;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation OK
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	public static class PersistenceOperationOKEvent 
		        extends PersistenceOperationEventBase {
		public PersistenceOperationOKEvent(final UserContext userContext,
										   final PersistenceOperationOK opOK) {
			super(userContext,opOK);
		}
		public PersistenceOperationOK getResultAsOperationOK() {
			return (PersistenceOperationOK)_operationResult;
		}
		@SuppressWarnings("unchecked")
		public <M extends PersistableModelObject<? extends OID>> CRUDOK<M> getResultAsCRUDOperationOK() {
			return (CRUDOK<M>)_operationResult;
		}
		@SuppressWarnings({ "unchecked","unused" })
		public <M extends PersistableModelObject<? extends OID>> CRUDOK<M> getResultAsCRUDOperationOKOn(final Class<M> modelObjectType) {
			return (CRUDOK<M>)_operationResult;
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Operation NOK
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	public static class PersistenceOperationErrorEvent 
		 		extends PersistenceOperationEventBase {
		public PersistenceOperationErrorEvent(final UserContext userContext,
									 		  final PersistenceOperationError opNOK) {
			super(userContext,opNOK);
		}
		public PersistenceOperationError getResultAsOperationError() {
			return (PersistenceOperationError)_operationResult;
		}
		@SuppressWarnings("unchecked")
		public <M extends PersistableModelObject<? extends OID>> CRUDError<M> getResultAsCRUDOperationError() {
			return (CRUDError<M>)_operationResult;
		}
	}
}
