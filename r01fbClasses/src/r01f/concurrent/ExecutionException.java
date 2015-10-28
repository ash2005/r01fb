package r01f.concurrent;

import r01f.enums.EnumExtended;
import r01f.enums.EnumExtendedWrapper;



/**
 * Execution exception for {@link FutureResult}
 */
public class ExecutionException 
	 extends RuntimeException {

	private static final long serialVersionUID = 205593321852553374L;
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private enum ExecutionExceptionType 
	  implements EnumExtended<ExecutionExceptionType> {
		INCOMPLETE_RESULT,
		CANCELLED,
		EXECUTION;
		
		private static EnumExtendedWrapper<ExecutionExceptionType> _enum = EnumExtendedWrapper.create(ExecutionExceptionType.class);

		@Override
		public boolean isIn(ExecutionExceptionType... els) {
			return _enum.isIn(this,els);
		}
		@Override
		public boolean is(final ExecutionExceptionType el) {
			return _enum.is(this,el);
		}
		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private ExecutionExceptionType _subClass;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public ExecutionException() {
		super();
	}
	public ExecutionException(final String msg) {
		super(msg);
	}
	public ExecutionException(final Throwable other) {
		super(other);
	}
	/**
	 * Creates an {@link ExecutionException} due to the result not being available
	 * @return
	 */
	public static ExecutionException becauseOfIncompleteResult() {
		ExecutionException outEx = new ExecutionException();
		return outEx.subClass(ExecutionExceptionType.INCOMPLETE_RESULT);
	}
	/**
	 * Creates a {@link ExecutionException} due to the cancellation of the request
	 * @return
	 */
	public static ExecutionException becauseOfRequestCancellation() {
		ExecutionException outEx = new ExecutionException();
		return outEx.subClass(ExecutionExceptionType.CANCELLED);
	}
	/**
	 * Creates a {@link ExecutionException} due to some error while executing the request
	 * @param th
	 * @return
	 */
	public static ExecutionException becauseOfExecutionException(final Throwable th) {
		ExecutionException outEx = new ExecutionException(th);
		return outEx.subClass(ExecutionExceptionType.EXECUTION);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public ExecutionExceptionType getSubClass() {
		return _subClass;
	}
	public ExecutionException subClass(final ExecutionExceptionType theSubClass) {
		_subClass = theSubClass;
		return this;
	}
	public boolean isSubClassOf(final ExecutionExceptionType subClass) {		
		return subClass.is(_subClass);
	}
	public boolean isAnyOfSubClasses(final ExecutionExceptionType... subClasses) {
		return _subClass.isIn(subClasses);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return true if the {@link ExecutionException} was due to the result not being available
	 */
	public boolean wasBecauseOfIncompleteResult() {
		return this.isSubClassOf(ExecutionExceptionType.INCOMPLETE_RESULT);
	}
	/**
	 * @return true if the {@link ExecutionException} was due the cancellation of the request
	 */
	public boolean wasCancelled() {
		return this.isSubClassOf(ExecutionExceptionType.CANCELLED);
	}
	/**
	 * @return true if the {@link ExecutionException} was due an error while executing the request
	 */
	public boolean wasExecutionException() {
		return this.isSubClassOf(ExecutionExceptionType.EXECUTION);
	}
}
