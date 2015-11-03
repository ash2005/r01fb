package r01f.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import lombok.Getter;
import lombok.experimental.Accessors;
import r01f.exceptions.EnrichedRuntimeException;
import r01f.exceptions.EnrichedThrowableSubType;
import r01f.exceptions.EnrichedThrowableSubTypeWrapper;
import r01f.exceptions.ExceptionSeverity;
import r01f.exceptions.Throwables;
import r01f.internal.R01F;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Exception thrown at {@link ReflectionUtils} utility type
 */
public class ReflectionException 
     extends EnrichedRuntimeException {
	
	private static final long serialVersionUID = -3758897550813211878L;
///////////////////////////////////////////////////////////////////////////////
// 	CONSTRUCTORS
///////////////////////////////////////////////////////////////////////////////
	private ReflectionException(final String msg,
							    final ReflectionExceptionType errorType) {
		super(ReflectionExceptionType.class,
			  msg,
			  errorType);	// all reflection exceptions are fatal
	}
	private ReflectionException(final Throwable th) {
		super(ReflectionExceptionType.class,
			  th,
			  ReflectionExceptionType.from(th));	// all reflection exceptions are fatal
	}
	private ReflectionException(final String msg,
								final Throwable th) {
		super(ReflectionExceptionType.class,
			  msg,
			  th,
			  ReflectionExceptionType.from(th));	// all reflection exceptions are fatal
	}
///////////////////////////////////////////////////////////////////////////////
//	METHODS
///////////////////////////////////////////////////////////////////////////////
	@Override @SuppressWarnings("null")
	public synchronized Throwable getCause() {
		// If it's and InvocationTagetException, return the cause
		Throwable cause = super.getCause();
		boolean isInvocationTargetEx = cause != null ? ReflectionUtils.isSameClassAs(cause.getClass(),
																					 InvocationTargetException.class)
												     : false;
		if (isInvocationTargetEx) {
			cause = ((InvocationTargetException)cause).getTargetException();
		}
		return super.getCause();
	}
///////////////////////////////////////////////////////////////////////////////
//	SUB_TYPE
///////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	      enum ReflectionExceptionType
	implements EnrichedThrowableSubType<ReflectionExceptionType> {
		UNKNOWN(R01F.CORE_GROUP),
		CLASS_NOT_FOUND(R01F.CORE_GROUP + 1),
		NO_CONSTRUCTOR(R01F.CORE_GROUP + 2),
		NO_METHOD(R01F.CORE_GROUP + 3),
		NO_FIELD(R01F.CORE_GROUP + 4),
		SECURITY(R01F.CORE_GROUP + 5),
		ILLEGAL_ARGUMENT(R01F.CORE_GROUP + 6),
		INSTANTIATION(R01F.CORE_GROUP + 7),
		INVOCATION_TARGET(R01F.CORE_GROUP + 8);
		
		@Getter private final int _group = R01F.CORE_GROUP;
		@Getter private int _code;
		
		private ReflectionExceptionType(final int code) {
			_code = code;
		}
		private static EnrichedThrowableSubTypeWrapper<ReflectionExceptionType> WRAPPER = EnrichedThrowableSubTypeWrapper.create(ReflectionExceptionType.class); 
		
		public static ReflectionExceptionType from(final int errorCode) {
			return WRAPPER.from(R01F.CORE_GROUP,errorCode);
		}
		public static ReflectionExceptionType from(final int groupCode,final int errorCode) {
			if (groupCode != R01F.CORE_GROUP) throw new IllegalArgumentException(Throwables.message("The group code for a {} MUST be {}",
																									ReflectionExceptionType.class,R01F.CORE_GROUP));
			return WRAPPER.from(R01F.CORE_GROUP,errorCode);
		}
		/**
		 * Gets the sub type of the exception
		 * @param th the exception
		 */
		public static ReflectionExceptionType from(final Throwable th) {
			ReflectionExceptionType outType = null;
			if (th instanceof ClassNotFoundException) {
				outType = ReflectionExceptionType.CLASS_NOT_FOUND;
			} else if (th instanceof NoSuchMethodException) {
				outType = ReflectionExceptionType.NO_METHOD;
			} else if (th instanceof NoSuchFieldException) {
				outType = ReflectionExceptionType.NO_FIELD;
			} else if (th instanceof SecurityException) {
				outType = ReflectionExceptionType.SECURITY;
			} else if (th instanceof InstantiationException) {
				outType = ReflectionExceptionType.INSTANTIATION;
			} else if (th instanceof IllegalAccessException) {
				outType = ReflectionExceptionType.SECURITY;
			} else if (th instanceof IllegalArgumentException) {
				outType = ReflectionExceptionType.ILLEGAL_ARGUMENT;
			} else if (th instanceof InvocationTargetException) {
				outType = ReflectionExceptionType.INVOCATION_TARGET;	// when invoking a method or constructor
			} else {
				outType = ReflectionExceptionType.UNKNOWN;
			}
			return outType;
		}
		@Override
		public ExceptionSeverity getSeverity() {
			return ExceptionSeverity.FATAL;		// All reflection exceptions are fatal
		}
		@Override
		public boolean is(final int group,final int code) {
			return WRAPPER.is(this,
							  group,code);
		}
		public boolean is(final int code) {
			return this.is(R01F.CORE_GROUP,code);
		}
		@Override
		public boolean isIn(final ReflectionExceptionType... els) {
			return WRAPPER.isIn(this,els);
		}
		@Override
		public boolean is(final ReflectionExceptionType el) {
			return WRAPPER.is(this,el);
		}
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public boolean isClassNotFoundException() {
		return this.is(ReflectionExceptionType.CLASS_NOT_FOUND);
	}
	public boolean isNoConstructorException() {
		return this.is(ReflectionExceptionType.NO_CONSTRUCTOR);
	}
	public boolean isNoMethodException() {
		return this.is(ReflectionExceptionType.NO_METHOD);
	}
	public boolean isNoFieldExcepton() {
		return this.is(ReflectionExceptionType.NO_FIELD);
	}
	public boolean isSecurityException() {
		return this.is(ReflectionExceptionType.SECURITY);
	}
	public boolean isIllegalArgumentException() {
		return this.is(ReflectionExceptionType.ILLEGAL_ARGUMENT);
	}
	public boolean isInstantiationException() {
		return this.is(ReflectionExceptionType.INSTANTIATION);
	}
	public boolean isInvocationTargetException() {
		return this.is(ReflectionExceptionType.INVOCATION_TARGET);
	}
	public boolean isunknownSubClassException() {
		return this.is(ReflectionExceptionType.UNKNOWN);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static ReflectionException of(final Throwable th) {
		return new ReflectionException(th);
	}
	public static ReflectionException classNotFoundException(final String typeName) {
		return new ReflectionException(Strings.customized("Could NOT load type with name: {}",typeName),
									   ReflectionExceptionType.CLASS_NOT_FOUND);
	}
	public static ReflectionException instantiationException(final String typeName) {
		return new ReflectionException(Strings.customized("Could NOT create an instance of the type: {}",typeName),
									   ReflectionExceptionType.INSTANTIATION);
	}
	public static ReflectionException instantiationException(final Class<?> type,final Class<?>[] constructorArgs) {
		if (CollectionUtils.isNullOrEmpty(constructorArgs)) {
			return new ReflectionException(Strings.customized("Could NOT create an instance of the type: {} using the no-args constructor",type),
										   ReflectionExceptionType.INSTANTIATION);			
		}
		return new ReflectionException(Strings.customized("Could NOT create an instance of the type: {} using the constructor with args",type,constructorArgs),
									   ReflectionExceptionType.INSTANTIATION);
	}
	public static ReflectionException securityException(final Class<?> type) {
		return new ReflectionException(Strings.customized("Security exception when creating an instance of type {}",type),
									   ReflectionExceptionType.SECURITY);
	}	
	public static ReflectionException securityException(final Class<?> type,final Method method) {
		return new ReflectionException(Strings.customized("Security exception when calling {} method in an instance of type {}",method,type),
									   ReflectionExceptionType.SECURITY);
	}
	public static ReflectionException noFieldException(final Class<?> type,final String fieldName) {
		return new ReflectionException(Strings.customized("Could NOT find field {} in type {}",fieldName,type),
									   ReflectionExceptionType.NO_FIELD);
	}
	public static ReflectionException noConstructorException(final Class<?> type,final Class<?>[] constructorArgs) {
		return new ReflectionException(Strings.customized("Could NOT find constructor with args {} in type {}",Arrays.asList(constructorArgs),type),
									   ReflectionExceptionType.NO_CONSTRUCTOR);
	}
	public static ReflectionException noMethodException(final Class<?> type,final String methodName) {
		return new ReflectionException(Strings.customized("Could NOT find method with name {} in type {}",methodName,type),
									   ReflectionExceptionType.NO_METHOD);
	}
	public static ReflectionException noMethodException(final Class<?> type,final String methodName,final Class<?>[] methodArgs) {
		return new ReflectionException(Strings.customized("Could NOT find method with name {} and arguments {} in type {}",methodName,Arrays.asList(methodArgs),type),
									   ReflectionExceptionType.NO_METHOD);
	}
	public static ReflectionException illegalArgumentException(final Class<?> expectedType,final Class<?> providedType) {
		return new ReflectionException(Strings.customized("The expected type was {} but the provided type was {}",expectedType,providedType),
									   ReflectionExceptionType.NO_METHOD);	}
	public static ReflectionException invocationTargetException(final Throwable th) {
		return new ReflectionException(th.getMessage(),
									   ReflectionExceptionType.INVOCATION_TARGET);
	}
}
