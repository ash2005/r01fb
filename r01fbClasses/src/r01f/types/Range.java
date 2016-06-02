package r01f.types;

import java.io.Serializable;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.collect.BoundType;

import lombok.AllArgsConstructor;
import lombok.Delegate;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.aspects.interfaces.dirtytrack.NotDirtyStateTrackable;
import r01f.marshalling.Marshaller;
import r01f.marshalling.annotations.XmlReadTransformer;
import r01f.marshalling.annotations.XmlWriteTransformer;
import r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlReadCustomTransformer;
import r01f.marshalling.simple.SimpleMarshallerCustomXmlTransformers.XmlWriteCustomTransformer;
import r01f.reflection.ReflectionUtils;
import r01f.types.Range.RangeXMLCustomMarshallers;
import r01f.types.annotations.Inmutable;
import r01f.util.types.Dates;
import r01f.util.types.Strings;

/**
 * Wraps a Guava {@link com.google.common.collect.Range} in order to be serializable by the {@link Marshaller}
 * If 
 * <ul>
 * 		<li>"[" or "]" represents a lower or upper bound where the bound itself is included in the range</li>
 * 		<li>"(" or ")" represents a lower or upper bound where the bound itself is EXCLUDED from the range</li>
 * <li>
 * <pre>
 * 		(a..b)		open(C, C)
 * 		[a..b]		closed(C, C)
 * 		[a..b)		closedOpen(C, C)
 * 		(a..b]		openClosed(C, C)
 * 		(a..+oo)	greaterThan(C)
 * 		[a..+oo)	atLeast(C)
 * 		(-oo..b)	lessThan(C)
 * 		(-oo..b]	atMost(C)
 * 		(-oo..+oo)	all() 
 * </pre>
 * 
 * Usage:
 * <pre class='brush:java'>
 *		// Create an integer range
 *		Range<Integer> intRange = Range.open(2,3);
 *
 *		// Serialize to string
 *		String intRangeStr = intRange.toString();
 *
 *		// Convert back to range from String representation
 *		intRange = Range.parse(intRangeStr,Integer.class);
 * </pre>
 * @param <T>
 */
@GwtIncompatible("Range NOT usable in GWT")
@Inmutable
@XmlRootElement(name="range") @XmlReadTransformer(using=RangeXMLCustomMarshallers.class) @XmlWriteTransformer(using=RangeXMLCustomMarshallers.class)
@Accessors(prefix="_")
@NoArgsConstructor
@SuppressWarnings("rawtypes")
public class Range<T extends Comparable<T>>
  implements Serializable {

	private static final long serialVersionUID = -5779835775897731838L;
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
////////////////////////////////////////////////////////////////////////////////////////
	private static final Pattern RANGE_PATTERN = Pattern.compile("(?:\\(|\\[)(.+)?\\.\\.(.+)?(?:\\)|\\])");

/////////////////////////////////////////////////////////////////////////////////////////
//  SERIALIZABLE FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The range lower bound
	 */
	@Getter private T _lowerBound;
	/**
	 * The range upper bound
	 */
	@Getter private T _upperBound;
	/**
	 * The lower bound type
	 */
	@Getter private BoundType _lowerBoundType;
	/**
	 * The upper bound type
	 */
	@Getter private BoundType _upperBoundType;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  NON-SERIALIZABLE FIELDS
/////////////////////////////////////////////////////////////////////////////////////////
	@Delegate
	@XmlTransient @NotDirtyStateTrackable
	private transient com.google.common.collect.Range<T> _range;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
	public Range(final com.google.common.collect.Range<T> range) {
		_range = range;
		_upperBound = range.hasUpperBound() ? range.upperEndpoint() : null;
		_lowerBound = range.hasLowerBound() ? range.lowerEndpoint() : null;
		
		_upperBoundType = range.hasUpperBound() ? range.upperBoundType() : BoundType.OPEN;
		_lowerBoundType = range.hasLowerBound() ? range.lowerBoundType() : BoundType.OPEN;
	}
	public Range(final T lower,final BoundType lowerBoundType,
				 final T upper,final BoundType upperBoundType) {
		// store the lower and upper bounds
		_lowerBound = lower;
		_upperBound = upper;
		
		_lowerBoundType = lowerBoundType;
		_upperBoundType = upperBoundType;
		
		// Create the delegate
		if (_lowerBound != null && _upperBound != null) {
			if (lowerBoundType == BoundType.OPEN && upperBoundType == BoundType.OPEN) { 
				_range = com.google.common.collect.Range.open(_lowerBound,_upperBound);
			} else if (lowerBoundType == BoundType.OPEN && upperBoundType == BoundType.CLOSED) {
				_range = com.google.common.collect.Range.openClosed(_lowerBound,_upperBound);
			} else if (lowerBoundType == BoundType.CLOSED && upperBoundType == BoundType.CLOSED) {
				_range = com.google.common.collect.Range.closed(_lowerBound,_upperBound);
			} else if (lowerBoundType == BoundType.CLOSED && upperBoundType == BoundType.OPEN) {
				_range = com.google.common.collect.Range.closedOpen(_lowerBound,_upperBound);
			} else {
				throw new IllegalArgumentException("Both lower and upper bound types MUST be provided!");
			}
		} else if (_lowerBound != null) {
			if (lowerBoundType == BoundType.OPEN) {
				_range = com.google.common.collect.Range.greaterThan(_lowerBound);
			} else {
				_range = com.google.common.collect.Range.atLeast(_lowerBound);
			}
		} else if (_upperBound != null) {
			if (upperBoundType == BoundType.OPEN) {
				_range = com.google.common.collect.Range.lessThan(_upperBound);
			} else {
				_range = com.google.common.collect.Range.atMost(_upperBound);
			}
		} else {
			throw new IllegalArgumentException("Cannot create range, at least lower or upper bound SHOULD be not null");
		}
	}
	public static <T extends Comparable<T>> Range<T> range(final T lower,final BoundType lowerBoundType,
				 									       final T upper,final BoundType upperBoundType) {
		return new Range<T>(lower,lowerBoundType,
						 	upper,upperBoundType);
	}
	/**
	 * Creates an OPEN range from its bounds
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> open(final T lower,final T upper) {
		if (lower == null || upper == null) throw new IllegalArgumentException("Both lower and upper bounds must be not null in an open Range");
		return new Range<T>(lower,BoundType.OPEN,
							upper,BoundType.OPEN);
	}
	/**
	 * Creates an OPEN on the lower end and CLOSED on the upper end range from its bounds
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> openClosed(final T lower,final T upper) {
		if (lower == null || upper == null) throw new IllegalArgumentException("Both lower and upper bounds must be not null in an open Range");
		return new Range<T>(lower,BoundType.OPEN,
							upper,BoundType.CLOSED);
	}
	/**
	 * Creates an CLOSED range from its bounds
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> closed(final T lower,final T upper) {
		if (lower == null || upper == null) throw new IllegalArgumentException("Both lower and upper bounds must be not null in an open Range");
		return new Range<T>(lower,BoundType.CLOSED,
							upper,BoundType.CLOSED);
	}
	/**
	 * Creates an CLOSED on the lower end and OPEN on the upper end range from its bounds
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> closedOpen(final T lower,final T upper) {
		if (lower == null || upper == null) throw new IllegalArgumentException("Both lower and upper bounds must be not null in an open Range");
		return new Range<T>(lower,BoundType.CLOSED,
							upper,BoundType.OPEN);
	}
	/**
	 * Creates a greaterThan (>) range from its lower bound
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> greaterThan(final T lower) {
		if (lower == null) throw new IllegalArgumentException("lower bound must be not null in an greaterThan Range");
		return new Range<T>(lower,BoundType.OPEN,
							null,null);
	}
	/**
	 * Creates a atLeast (>=) range from its lower bound
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> atLeast(final T lower) {
		if (lower == null) throw new IllegalArgumentException("lower bound must be not null in an greaterThan Range");
		return new Range<T>(lower,BoundType.CLOSED,
							null,null);
	}
	/**
	 * Creates a lessThan (<) range from the upper bound
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> lessThan(final T upper) {
		if (upper == null) throw new IllegalArgumentException("upper bound must be not null in an greaterThan Range");
		return new Range<T>(null,null,
							upper,BoundType.OPEN);
	}
	/**
	 * Creates a atMost (<=) range from the upper bound
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static <T extends Comparable<T>> Range<T> atMost(final T upper) {
		if (upper == null) throw new IllegalArgumentException("upper bound must be not null in an greaterThan Range");
		return new Range<T>(null,null,
							upper,BoundType.CLOSED);
	}
	/**
	 * Unsafe parse a range from its textual representation like lowerBound..upperBound
	 * It's unsafe since it can throw a {@link ClassCastException} if the dataType arg is not a {@link Comparable} type
	 * @param rangeStr
	 * @param dataType
	 * @return
	 */
	@SuppressWarnings({"unchecked"})
	public static Range<?> unsafeParse(final String rangeStr,
									   final Class<?> dataType) {
		Class<? extends Comparable> comparableDataType = (Class<? extends Comparable>)dataType;
		return Range.parse(rangeStr,
						   comparableDataType);
	}
	/**
	 * Parses a range from its textual representation like lowerBound..upperBound
	 * @param rangeStr
	 * @param dataType
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> Range<T> parse(final String rangeStr,
							 	 	  					   final Class<T> dataType) {
		Object outRange = null;
		
		// Errores de compilacion:  Incomparable Types
		//[javac] /softbase_ejie/aplic/r01fb/tmp/compileLib/r01fbClasses/src/r01f/types/Range.java:407: 
		//incomparable types: java.lang.Class<T> and java.lang.Class<java.util.Date>
	    // [javac] 		if (dataType == java.util.Date.class || dataType == java.sql.Date.class) {
		
		Class<?> java_util_Date_class = java.util.Date.class;
		Class<?> java_sql__Date_class = java.sql.Date.class;
		Class<?> java_lang_Byte_class = Byte.class;
		Class<?> java_lang_Integer_class = Integer.class;
		Class<?> java_lang_Short_class = Short.class;
		Class<?> java_lang_Long_class = Long.class;
		Class<?> java_lang_Double_class = Double.class;
		Class<?> java_lang_Float_class = Float.class;
		
		
		RangeDef bounds = _parseBounds(rangeStr);
		if (dataType == java_util_Date_class|| dataType == java_sql__Date_class) {
			outRange = _parseDateRange(bounds.getLowerBound(),bounds.getLowerBoundType(),
									   bounds.getUpperBound(),bounds.getUpperBoundType());
		} else if (dataType == java_lang_Byte_class) {
			outRange = _parseByteRange(bounds.getLowerBound(),bounds.getLowerBoundType(),
									   bounds.getUpperBound(),bounds.getUpperBoundType());
		} else if (dataType == java_lang_Integer_class) {
			outRange = _parseIntRange(bounds.getLowerBound(),bounds.getLowerBoundType(),
									  bounds.getUpperBound(),bounds.getUpperBoundType());
		} else if (dataType == java_lang_Short_class) {
			outRange = _parseShortRange(bounds.getLowerBound(),bounds.getLowerBoundType(),
										bounds.getUpperBound(),bounds.getUpperBoundType());
		} else if (dataType == java_lang_Long_class) {
			outRange = _parseLongRange(bounds.getLowerBound(),bounds.getLowerBoundType(),
									   bounds.getUpperBound(),bounds.getUpperBoundType());
		} else if (dataType ==java_lang_Double_class) {
			outRange = _parseDoubleRange(bounds.getLowerBound(),bounds.getLowerBoundType(),
										 bounds.getUpperBound(),bounds.getUpperBoundType());
		} else if (dataType == java_lang_Float_class) {
			outRange = _parseFloatRange(bounds.getLowerBound(),bounds.getLowerBoundType(),
										bounds.getUpperBound(),bounds.getUpperBoundType());
		} else {
			throw new IllegalArgumentException("Type " + dataType + " is NOT supported in Range");
		}
		return  (Range<T>)outRange;
	}
	
	
	private static RangeDef _parseBounds(final String rangeStr) {
		RangeDef outBounds = null;
		Matcher m = RANGE_PATTERN.matcher(rangeStr);
		if (m.matches()) {			
			outBounds = new RangeDef();
			if (m.groupCount() == 2) {
				if (rangeStr.startsWith("(") || rangeStr.startsWith("..")) {
					outBounds.setLowerBoundType(BoundType.OPEN); 
				} else if (rangeStr.startsWith("[")) {
					outBounds.setLowerBoundType(BoundType.CLOSED);
				} else {
					throw new IllegalArgumentException(rangeStr + " is NOT a valid range string representation!");
				}
				if (rangeStr.endsWith(")") || rangeStr.endsWith("..")) {
					outBounds.setUpperBoundType(BoundType.OPEN);
				} else if (rangeStr.endsWith("]")) {
					outBounds.setUpperBoundType(BoundType.CLOSED);
				} else {
					throw new IllegalArgumentException(rangeStr + " is NOT a valid range string representation!");
				}
				outBounds.setLowerBound(m.group(1));
				outBounds.setUpperBound(m.group(2));
			} else if (rangeStr.startsWith("(..") || rangeStr.startsWith("..")) {
				outBounds.setLowerBoundType(BoundType.OPEN);
				outBounds.setUpperBound(m.group(1));
			} else if (rangeStr.startsWith("[..")) {
				outBounds.setLowerBoundType(BoundType.CLOSED);
				outBounds.setUpperBound(m.group(1));
			} else if (rangeStr.endsWith("..)") || rangeStr.endsWith("..")) {
				outBounds.setUpperBoundType(BoundType.OPEN);
				outBounds.setLowerBound(m.group(1));
			} else if (rangeStr.endsWith("..]")) {
				outBounds.setUpperBoundType(BoundType.CLOSED);
				outBounds.setLowerBound(m.group(1));
			} else {
				throw new IllegalArgumentException(rangeStr + " is NOT a valid range!");
			}	
		} else {
			throw new IllegalArgumentException("The range string representation: " + rangeStr + " does NOT match the pattern " + RANGE_PATTERN.pattern());
		}
		return outBounds;
	}
	private static Range<Date> _parseDateRange(final String lowerBound,final BoundType lowerBoundType,
											   final String upperBound,final BoundType upperBoundType) {
		Date lowerBoundDate = lowerBound != null ? Dates.fromMillis(Long.parseLong(lowerBound)) : null;
		Date upperBoundDate = upperBound != null ? Dates.fromMillis(Long.parseLong(upperBound)) : null;
		return new Range<Date>(lowerBoundDate,lowerBoundType,
							   upperBoundDate,upperBoundType);
	}
	private static Range<Byte>  _parseByteRange(final String lowerBound,final BoundType lowerBoundType,
											    final String upperBound,final BoundType upperBoundType) {
		Byte lowerBoundByte = lowerBound != null ? Byte.valueOf(lowerBound) : null;
		Byte upperBoundByte = upperBound != null ? Byte.valueOf(upperBound) : null;
		return new Range<Byte>(lowerBoundByte,lowerBoundType,
							   upperBoundByte,upperBoundType);
	}
	private static Range<Integer>  _parseIntRange(final String lowerBound,final BoundType lowerBoundType,
											   	  final String upperBound,final BoundType upperBoundType) {
		Integer lowerBoundInt = lowerBound != null ? Integer.valueOf(lowerBound) : null;
		Integer upperBoundInt = upperBound != null ? Integer.valueOf(upperBound) : null;
		return new Range<Integer>(lowerBoundInt,lowerBoundType,
							      upperBoundInt,upperBoundType);
	}
	private static Range<Short>  _parseShortRange(final String lowerBound,final BoundType lowerBoundType,
											   	  final String upperBound,final BoundType upperBoundType) {
		Short lowerBoundShort = lowerBound != null ? Short.valueOf(lowerBound) : null;
		Short upperBoundShort = upperBound != null ? Short.valueOf(upperBound) : null;
		return new Range<Short>(lowerBoundShort,lowerBoundType,
							    upperBoundShort,upperBoundType);
	}
	private static Range<Long>  _parseLongRange(final String lowerBound,final BoundType lowerBoundType,
											    final String upperBound,final BoundType upperBoundType) {
		Long lowerBoundLong = lowerBound != null ? Long.valueOf(lowerBound) : null;
		Long upperBoundLong = upperBound != null ? Long.valueOf(upperBound) : null;
		return new Range<Long>(lowerBoundLong,lowerBoundType,
							   upperBoundLong,upperBoundType);
	}
	private static Range<Double>  _parseDoubleRange(final String lowerBound,final BoundType lowerBoundType,
											   		final String upperBound,final BoundType upperBoundType) {
		Double lowerBoundDouble = lowerBound != null ? Double.valueOf(lowerBound) : null;
		Double upperBoundDouble = upperBound != null ? Double.valueOf(upperBound) : null;
		return new Range<Double>(lowerBoundDouble,lowerBoundType,
							     upperBoundDouble,upperBoundType);
	}
	private static Range<Float>  _parseFloatRange(final String lowerBound,final BoundType lowerBoundType,
											   	  final String upperBound,final BoundType upperBoundType) {
		Float lowerBoundFloat = lowerBound != null ? Float.valueOf(lowerBound) : null;
		Float upperBoundFloat = upperBound != null ? Float.valueOf(upperBound) : null;
		return new Range<Float>(lowerBoundFloat,lowerBoundType,
							    upperBoundFloat,upperBoundType);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the data type of the range
	 */
	public Class<T> getDataType() {
		return Range.guessDataType(this);
	}
	/**
	 * @return the range as a Google Guava {@link com.google.common.collect.Range}
	 */
	public com.google.common.collect.Range<T> asGuavaRange() {
		return _range;
	}
	@Override
	public String toString() {
		return this.asString();
	}
	
	public String asString() {
		RangeDef rangeDef = null;
		Class<T> dataType = Range.guessDataType(this);
		/// Compilation errors:  Incomparable Types
		//[javac] /softbase_ejie/aplic/r01fb/tmp/compileLib/r01fbClasses/src/r01f/types/Range.java:407: 
		//incomparable types: java.lang.Class<T> and java.lang.Class<java.util.Date>
	    // [javac] 		if (dataType == java.util.Date.class || dataType == java.sql.Date.class) {
		
		Class<?> java_util_Date_class = java.util.Date.class;
		Class<?> java_sql_Date_class = java.sql.Date.class;
		Class<?> java_lang_Byte_class = Byte.class;
		Class<?> java_lang_Integer_class = Integer.class;
		Class<?> java_lang_Short_class = Short.class;
		Class<?> java_lang_Long_class = Long.class;
		Class<?> java_lang_Double_class = Double.class;
		Class<?> java_lang_Float_class = Float.class;
		
		if (dataType == java_util_Date_class || dataType == java_sql_Date_class) {
			rangeDef = _toDateBoundStrings(this);
		} else if (dataType == java_lang_Byte_class) {
			rangeDef = _toByteBoundStrings(this);
		} else if (dataType == java_lang_Integer_class) {
			rangeDef = _toIntegerBoundStrings(this);
		} else if (dataType == java_lang_Short_class) {
			rangeDef = _toShortBoundStrings(this);
		} else if (dataType == java_lang_Long_class ) {
			rangeDef = _toLongBoundStrings(this);
		} else if (dataType == java_lang_Double_class ) {
			rangeDef = _toDoubleBoundStrings(this);
		} else if (dataType == java_lang_Float_class) {
			rangeDef = _toFloatBoundStrings(this);
		} else {
			throw new IllegalArgumentException("Type " + dataType + " is NOT supported in Range");
		}
		String outStr = Strings.of("{}{}..{}{}")
							   .customizeWith((rangeDef.getLowerBoundType() == BoundType.CLOSED ? "[" : "("),(rangeDef.getLowerBound() != null ? rangeDef.getLowerBound() : ""),
									          (rangeDef.getUpperBound() != null ? rangeDef.getUpperBound() : ""),(rangeDef.getUpperBoundType() == BoundType.CLOSED ? "]" : ")"))
							   .asString();
		return outStr;
	}
	
	
	
	
	
	
	
	private static RangeDef _toDateBoundStrings(final Range<? extends Comparable> range) {
		String lower = range.getLowerBound() != null ? Long.toString(Dates.asMillis(((java.util.Date)range.getLowerBound()))) : null;
		String upper = range.getUpperBound() != null ? Long.toString(Dates.asMillis(((java.util.Date)range.getUpperBound()))) : null;
		return new RangeDef(lower,range.getLowerBoundType(),
							upper,range.getUpperBoundType());
	}
	private static RangeDef _toByteBoundStrings(final Range<? extends Comparable> range) {
		String lower = range.getLowerBound() != null ? Byte.toString((Byte)range.getLowerBound()) : null;
		String upper = range.getUpperBound() != null ? Byte.toString((Byte)range.getUpperBound()) : null;
		return new RangeDef(lower,range.getLowerBoundType(),
							upper,range.getUpperBoundType());
	}
	private static RangeDef _toIntegerBoundStrings(final Range<? extends Comparable> range) {
		String lower = range.getLowerBound() != null ? Integer.toString((Integer)range.getLowerBound()) : null;
		String upper = range.getUpperBound() != null ? Integer.toString((Integer)range.getUpperBound()) : null;
		return new RangeDef(lower,range.getLowerBoundType(),
							upper,range.getUpperBoundType());
	}
	private static RangeDef _toShortBoundStrings(final Range<? extends Comparable> range) {
		String lower = range.getLowerBound() != null ? Short.toString((Short)range.getLowerBound()) : null;
		String upper = range.getUpperBound() != null ? Short.toString((Short)range.getUpperBound()) : null;
		return new RangeDef(lower,range.getLowerBoundType(),
							upper,range.getUpperBoundType());
	}
	private static RangeDef _toLongBoundStrings(final Range<? extends Comparable> range) {
		String lower = range.getLowerBound() != null ? Long.toString((Long)range.getLowerBound()) : null;
		String upper = range.getUpperBound() != null ? Long.toString((Long)range.getUpperBound()) : null;
		return new RangeDef(lower,range.getLowerBoundType(),
							upper,range.getUpperBoundType());
	}
	private static RangeDef _toDoubleBoundStrings(final Range<? extends Comparable> range) {
		String lower = range.getLowerBound() != null ? Double.toString((Double)range.getLowerBound()) : null;
		String upper = range.getUpperBound() != null ? Double.toString((Double)range.getUpperBound()) : null;
		return new RangeDef(lower,range.getLowerBoundType(),
							upper,range.getUpperBoundType());
	}
	private static RangeDef _toFloatBoundStrings(final Range<? extends Comparable> range) {
		String lower = range.getLowerBound() != null ? Float.toString((Float)range.getLowerBound()) : null;
		String upper = range.getUpperBound() != null ? Float.toString((Float)range.getUpperBound()) : null;
		return new RangeDef(lower,range.getLowerBoundType(),
							upper,range.getUpperBoundType());
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> Class<T> guessDataType(final Range<T> range) {
		Class<T> outDataType = null;
		if (range.getLowerBound() != null) {
			outDataType = (Class<T>)range.getLowerBound().getClass();
		} else if (range.getUpperBound() != null) {
			outDataType = (Class<T>)range.getUpperBound().getClass();
		} else {
			throw new IllegalStateException("NO lower or upper bound set!");
		}
		return outDataType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	@Accessors(prefix="_")
	@NoArgsConstructor @AllArgsConstructor
	private static class RangeDef {
		@Getter @Setter private String _lowerBound;
		@Getter @Setter private BoundType _lowerBoundType;
		@Getter @Setter private String _upperBound;
		@Getter @Setter private BoundType _upperBoundType;
		
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  MARSHALLING
/////////////////////////////////////////////////////////////////////////////////////////
	public static class RangeXMLCustomMarshallers<T extends Comparable<T>>
  			 implements XmlReadCustomTransformer<Range<T>>,
  			  		    XmlWriteCustomTransformer {
		// <range type='{}'>{}</range> = <range type=['\"]([^'\"]+)['\"]>(.+)<\/range>
		private static final Pattern RANGE_XML_ELEMENT_PATTERN = Pattern.compile("<range type=['\\\"]([^'\\\"]+)['\\\"]>" +
																				 "(.+)" + 
																				 "<\\/range>");
		private static final Pattern RANGE_XML_ATTR_PATTERN = Pattern.compile("(Date|Integer|Long|Short|Double|Float):(.+)");
		
		@Override @SuppressWarnings("unchecked")
		public String xmlFromBean(final boolean isAttribute,
								  final Object object) {
			String outXml = null;
			Range<T> range = (Range<T>)object;
			Class<?> rangeDataType = Range.guessDataType(range);
			if (isAttribute) {
				outXml = Strings.of("{}:{}")
								.customizeWith(rangeDataType.getSimpleName(),range.asString())
								.asString();
			} else {
				outXml = Strings.of("<range type='{}'>{}</range>")
							  	.customizeWith(rangeDataType.getName(),range.toString())
							  	.asString();
			}
			return outXml;
		}
		@Override @SuppressWarnings("unchecked")
		public Range<T> beanFromXml(final boolean isAttribute,
									final CharSequence xml) {
			Range<T> outRange = null;
			if (isAttribute) {
				Matcher m = RANGE_XML_ATTR_PATTERN.matcher(xml);
				if (m.find()) {
					String rangeDataTypeStr = m.group(1);
					String range = m.group(2);
					Class<?> rangeDataType = null;
					if (rangeDataTypeStr.equals("Date")) {
						rangeDataType = Date.class;
					} else if (rangeDataTypeStr.equals("Integer")) {
						rangeDataType = Integer.class;
					} else if (rangeDataTypeStr.equals("Long")) {
						rangeDataType = Long.class;
					} else if (rangeDataTypeStr.equals("Short")) {
						rangeDataType = Short.class;
					} else if (rangeDataTypeStr.equals("Double")) {
						rangeDataType = Double.class;
					} else if (rangeDataTypeStr.equals("Float")) {
						rangeDataType = Float.class;
					}
					outRange = Range.parse(range,
										   (Class<T>)rangeDataType);
				} else {
					throw new IllegalArgumentException(xml + " is NOT a legal XML representation of the range as attribute: " + RANGE_XML_ATTR_PATTERN);
				}
			} else {
				Matcher m = RANGE_XML_ELEMENT_PATTERN.matcher(xml);
				if (m.find()) {
					Class<T> rangeDataType = ReflectionUtils.typeFromClassName(m.group(1));
					String range = m.group(2);
					outRange = Range.parse(range,
									   	   rangeDataType);
				} else {
					throw new IllegalArgumentException(xml + " is NOT a legal XML representation of the range: " + RANGE_XML_ELEMENT_PATTERN);
				}
			}
			return outRange;
		}
	}
}
