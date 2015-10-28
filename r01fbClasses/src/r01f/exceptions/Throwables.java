package r01f.exceptions;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import r01f.reflection.ReflectionUtils;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.annotations.GwtIncompatible;

/**
 * Some exception-related utilities
 */
public class Throwables {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	static String composeMessage(final EnrichedThrowable th) {
		return Strings.create()
					  .add("[")
						  .addCustomizedIfParamNotNull(" group={}",th.getGroup())
						  .addCustomizedIfParamNotNull(" code={}",th.getCode())
						  .addCustomizedIfParamNotNull(" severity={}",th.getSeverity())
					  .add(" ]")
					  .addCustomizedIfParamNotNull(": {}",_message(th))
					  .asString();
	}
	static String composeXMLMessage(final EnrichedThrowable th) {
		return Strings.create()
					  .add("<exceptionData")
							  .addCustomizedIfParamNotNull(" group='{}'",th.getGroup())
							  .addCustomizedIfParamNotNull(" code='{}'",th.getCode())
							  .addCustomizedIfParamNotNull(" severity='{}'",th.getSeverity())
					  .add(">")
							  .addCustomizedIfParamNotNull("{}",_message(th))
					  .add("</exceptionData>")
					  .asString();
	}
	private static String _message(final EnrichedThrowable th) {
		String msg = Strings.isNOTNullOrEmpty(th.getRawMessage()) ? th.getRawMessage().replaceAll("[\n\r]"," ")
													   			  : th.getSubType() != null ? th.getSubType().toString() : "";
		return msg;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	static <S extends EnrichedThrowableSubType<?>> S getSubType(final EnrichedThrowable th,
																final Class<S> subTypeType) {
		S outSubType = null;
		if (th.getGroup() > 0 && th.getCode() > 0) {
			// Call the static factory
			outSubType = (S)ReflectionUtils.invokeStaticMethod(subTypeType,
															   "from",
															   new Class<?>[] {Integer.class,Integer.class},
															   new Object[] {th.getGroup(),th.getCode()});
		}
		return outSubType;
	}
	static <S extends EnrichedThrowableSubType<?>> S getSubType(final Class<S> subTypeType,
																final int group,final int code) {
		return ReflectionUtils.<S>invokeStaticMethod(subTypeType,
												  "from",new Class<?>[] {Integer.class,Integer.class},new Object[] {group,code});	
	}
	static boolean isMoreSerious(final EnrichedThrowable th,final EnrichedThrowable otherTh) {
		ExceptionSeverity thSeverity = th.getSeverity();
		ExceptionSeverity otherThSeverity = otherTh.getSeverity();
		
		if (thSeverity == null && otherThSeverity == null) {
			return false;
		} else if (thSeverity != null) { 
			return thSeverity.isMoreSeriousThan(otherThSeverity);
		}
		return false;
	}
	static <S extends EnrichedThrowableSubType<?>> boolean is(final EnrichedThrowable th,
															  final S subType) {
		return subType != null ? subType.is(th.getGroup(),
						  					th.getCode())
						  	   : false;
	}
	static <S extends EnrichedThrowableSubType<?>> boolean isAny(final EnrichedThrowable th,final S... subClasses) {
		if (CollectionUtils.isNullOrEmpty(subClasses)) {
			return false;
		}
		boolean found = false;
		for (S sub : subClasses) {
			if (sub.is(th.getGroup(),
					   th.getCode())) {
				found = true;
				break;
			}
		}
		return found;
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  UTILLITIES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Customizes an exception message replacing {}-like placeholder with vars
	 * @param msg the message
	 * @param vars the vars
	 * @return the customized message
	 */
	public static String message(final String msg,final Object... vars) {
		return Strings.of(msg)
					  .customizeWith(vars)
					  .asString();
	}
	/**
	 * Logs an exception
	 * ie:
	 * <pre class='brush:java'>
	 * 		public class MyType {
	 * 			public void myMethod(String param) {
	 * 				try {
	 * 					doSomething(param);
	 *				} catch (Exception ex) {
	 *					Throwables.log(MyType.class,
	 *								   ex,
	 *								   "Error {} calling doSomething() with parameter {}",ex.getClass().getName(),param); 
	 *				}
	 * </pre>
	 * @param throwingType the type where the exception is catched and logged
	 * @param th the throwed exception
	 * @param msg the message to log (can contain {}-like placeholder -see SL4FJ-)
	 * @param vars the params of the message to log
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static void log(final Class<?> throwingType,
						   final Throwable th,
						   final String msg,final Object... vars) {
		Logger logger = LoggerFactory.getLogger(throwingType);
		logger.error(msg,vars,th);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  WRAP OF com.google.common.base.Throwables
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @see com.google.common.base.Throwables.getCausalChain
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static List<Throwable> getCausalChain(final Throwable throwable) {
		 return com.google.common.base.Throwables.getCausalChain(throwable);
	}
	/**
	 * @see com.google.common.base.Throwables.getRootCause
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static Throwable getRootCause(final Throwable throwable) {
		return com.google.common.base.Throwables.getRootCause(throwable);
	}
	/**
	 * @see com.google.common.base.Throwables.getStackTraceAsString
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static String getStackTraceAsString(final Throwable throwable) {
		return  com.google.common.base.Throwables.getStackTraceAsString(throwable);
	}
	/**
	 * @see com.google.common.base.Throwables.propagate
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static RuntimeException propagate(final Throwable throwable) {
		return com.google.common.base.Throwables.propagate(throwable);
	}
	/**
	 * @see com.google.common.base.Throwables.propagateIfInstanceOf
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static <X extends Throwable> void propagateIfInstanceOf(final Throwable throwable,
																   final Class<X> declaredType) throws X {
		com.google.common.base.Throwables.propagateIfInstanceOf(throwable,
																declaredType);
	}
	/**
	 * @see com.google.common.base.Throwables.propagateIfPossible
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static void propagateIfPossible(Throwable throwable) {
		com.google.common.base.Throwables.propagateIfPossible(throwable);
	}
	/**
	 * @see com.google.common.base.Throwables.propagateIfPossible
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static <X extends Throwable> void propagateIfPossible(final Throwable throwable,
																 final Class<X> declaredType) throws X {
		com.google.common.base.Throwables.propagateIfPossible(throwable,
															  declaredType);
	}
	/**
	 * @see com.google.common.base.Throwables.propagateIfPossible
	 */
	@GwtIncompatible("Guava's Throwables NOT usable in GWT")
	public static <X1 extends Throwable,X2 extends Throwable> void propagateIfPossible(final Throwable throwable, 
																					   final Class<X1> declaredType1,final Class<X2> declaredType2) throws X1,X2 {
		com.google.common.base.Throwables.propagateIfPossible(throwable,
															  declaredType1,declaredType2);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  OTHER METHODS
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Transforms a checked exception into an unchecked exception
	 * @param ex the exception
	 * @return the unchecked-transformed exception
	 */
	public static RuntimeException throwUnchecked(final Exception ex) {
		Throwables.<RuntimeException>_throwUnchecked(ex);
		throw new AssertionError("This code is never executed");
	}
	@SuppressWarnings("unchecked")
	private static <T extends Exception> void _throwUnchecked(final Exception ex) throws T {
		throw (T)ex;
	}

}
