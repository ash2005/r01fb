package r01f.persistence;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.exceptions.EnrichedThrowable;
import r01f.exceptions.Throwables;
import r01f.marshalling.annotations.XmlCDATA;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended.StringCustomizerVarsProvider;

@Accessors(prefix="_")
public class PersistenceOperationExecError<T>
	 extends PersistenceOperationExecResult<T>
  implements PersistenceOperationError {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * If it's a bad client request this member stores an error number
	 * that should be used at the client side to present the user with
	 * some useful information about the action to be taken
	 */
	@XmlAttribute(name="errorCode")
	@Getter @Setter protected PersistenceErrorType _errorType;
	/**
	 * An application-specific extended code that provides additional information  
	 * to what _errorType gives 
	 */
	@XmlAttribute(name="extendedErrorCode")
	@Getter @Setter protected int _extendedErrorCode;
	/**
	 * Some message about the overall operation, usually used when there's an error
	 */
	@XmlElement(name="message") @XmlCDATA
	@Getter @Setter protected String _errorMessage;
	/**
	 * Contains details about the error, usually a java stack trace
	 */
	@XmlElement(name="errorDebug") @XmlCDATA
	@Getter @Setter protected String _errorDebug;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  NOT-SERIALIZABLE STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Contains the error in the case that there is a general error that prevents 
	 * the find operation to be executed
	 */
	@XmlTransient
	@Getter @Setter(AccessLevel.MODULE)
	protected transient Throwable _error;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public PersistenceOperationExecError() {
		// nothing
	}
	PersistenceOperationExecError(final Throwable th) {
		_error = th;		
		_errorMessage = th.getMessage();
		_errorDebug = Throwables.getStackTraceAsString(th);
		if (th instanceof EnrichedThrowable) {
			EnrichedThrowable enrichedTh = (EnrichedThrowable)th;
			_extendedErrorCode = enrichedTh.getExtendedCode();
		}
		if (th instanceof PersistenceException) {
			PersistenceException persistEx = (PersistenceException)th; 
			_errorType = persistEx.getPersistenceErrorType();
		} else {
			_errorType = PersistenceErrorType.SERVER_ERROR;		// a server error by default
			
		}
	}
	PersistenceOperationExecError(final String errMsg,
						 		  final PersistenceErrorType errorCode) {
		_errorMessage = errMsg;
		_errorDebug = null;
		_errorType = errorCode;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("unchecked")
	public <E extends Throwable> E getErrorAs(final Class<E> errorType) {
		return (E)_error;
	}
	@Override
	public void throwAsPersistenceException() throws PersistenceException {
		throw this.getPersistenceException();
	}
	@Override
	public PersistenceException getPersistenceException() {
		String errorMsg = Strings.isNOTNullOrEmpty(_errorMessage) ? _errorMessage
																  : this.getDetailedMessage();
		PersistenceErrorType errorType = _errorType != null ? _errorType : PersistenceErrorType.UNKNOWN;
		PersistenceRequestedOperation reqOp = PersistenceRequestedOperation.canBe(_requestedOperationName) ? PersistenceRequestedOperation.valueOf(_requestedOperationName)
																										   : PersistenceRequestedOperation.OTHER;
		return new PersistenceException(reqOp,_requestedOperationName,
										errorMsg,
									    errorType,_extendedErrorCode);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  REASON
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public boolean wasBecauseAServerError() {
		return !this.wasBecauseAClientError();
	}
	@Override
	public boolean wasBecauseAClientError() {
		if (_errorType == null) throw new IllegalStateException(Throwables.message("The {} object does NOT have error info!",
																				   PersistenceOperationError.class));
		return _errorType.isClientError();
	}
	@Override
	public boolean wasBecauseClientCouldNotConnectToServer() {
		if (_errorType == null) throw new IllegalStateException(Throwables.message("The {} object does NOT have error info!",
																				   PersistenceOperationError.class));
		return _errorType.is(PersistenceErrorType.CLIENT_CANNOT_CONNECT_SERVER);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public PersistenceOperationExecError<T> asOperationExecError() {
		return this;
	}
	@Override
	public PersistenceOperationExecOK<T> asOperationExecOK() {
		throw new ClassCastException();
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public String getDetailedMessage() {
		String outMsg = Strings.of("{} operation could NOT be performed because of a {} error{}: {}")
						       .customizeWith(new StringCustomizerVarsProvider() {
														@Override
														public Object[] provideVars() {
															PersistenceOperationExecError<T> err = PersistenceOperationExecError.this;
															Object[] outVars = new Object[4];
															outVars[0] = err.getRequestedOperationName();
															outVars[1] = err.wasBecauseAClientError() ? "CLIENT"
																									  : "SERVER";
															if (err.getErrorType() != null) {
																outVars[2] = " (code=" + err.getErrorType() + ")";
																
															} else {
																outVars[2] = "";
															}
															outVars[3] = Strings.isNOTNullOrEmpty(err.getErrorMessage()) ? err.getErrorMessage() 
																					 						     		 : err.getErrorType() != null ? err.getErrorType().toString() : "";
															return outVars;
														}
						       				  })
							   .asString();
		return outMsg;
	} 
	@Override
	public CharSequence debugInfo() {
		StringBuilder outDbgInfo = new StringBuilder();
		outDbgInfo.append(this.getDetailedMessage());
		if (_error != null) {
			outDbgInfo.append("\n")
					  .append(Throwables.getStackTraceAsString(_error));
		}
		return outDbgInfo.toString();
	}
}
