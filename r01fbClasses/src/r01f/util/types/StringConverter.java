package r01f.util.types;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.Date;

import com.google.common.annotations.GwtIncompatible;

import lombok.RequiredArgsConstructor;
import r01f.locale.Language;
import r01f.locale.Languages;
import r01f.reflection.ReflectionUtils;
import r01f.types.Path;
import r01f.util.types.Strings.XMLString;

/**
 * @author adminw7local
 *
 */
@RequiredArgsConstructor
public class StringConverter {
/////////////////////////////////////////////////////////////////////////////////////////
//  FINAL
/////////////////////////////////////////////////////////////////////////////////////////
	private final CharSequence _theString;
	
/////////////////////////////////////////////////////////////////////////////////////////
//  STRING
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link String}
	 * @return
	 */
	public String asString() {
		return this.asString(null);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link String} 
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param defValue
	 * @return
	 */
	public String asString(final String defValue) {
		return _theString != null ? _theString.toString()
							 	  : defValue;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BOOLEAN
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as a boolean 
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param val
	 * @param defValue
	 * @return
	 */
	public static boolean asBoolean(final CharSequence val,final boolean defValue) {
        if (val == null) return defValue;
        
        String value = val.toString();
        if ((value.equalsIgnoreCase("true")) ||
            (value.equalsIgnoreCase("on")) ||
            (value.equalsIgnoreCase("yes")) ||
            (value.equalsIgnoreCase("si")) ||
            (value.equalsIgnoreCase("bai"))) {
            return Boolean.TRUE;
        } else if ((value.equalsIgnoreCase("false")) ||
                   (value.equalsIgnoreCase("off")) ||
                   (value.equalsIgnoreCase("no")) ||
                   (value.equalsIgnoreCase("ez"))) {
            return Boolean.FALSE;
        } else {
            return defValue;
        }
	}	
	/**
	 * Returns the wrapped {@link CharSequence} as a boolean
	 * @return
	 */
	public boolean asBoolean() {
		return this.asBoolean(false);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a boolean
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param defValue
	 * @return
	 */
	public boolean asBoolean(final boolean defValue) {
    	return StringConverter.asBoolean(_theString,defValue);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  BYTE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as a byte 
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param value
	 * @param defValue
	 * @return
	 */
	public static byte asByte(final CharSequence value,final byte defValue) {
    	if (value == null) return defValue;
    	return Byte.parseByte(value.toString());
    }	
	/**
	 * Returns the wrapped {@link CharSequence} as a byte
	 * @return
	 */
	public byte asByte() {
		return this.asByte((byte)'\u0000');		// null char
	}
	/**
	 * Returns the given {@link CharSequence} as a byte
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param defValue
	 * @return
	 */
	public byte asByte(final byte defValue) {
		return StringConverter.asByte(_theString,defValue);
	}

/////////////////////////////////////////////////////////////////////////////////////////
//  CHAR
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the given {@link CharSequence} as a char 
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param value
     * @param defValue
     * @return
     */
    public static char asChar(final CharSequence value,final char defValue) {
        if (value == null) return defValue;
        return value.charAt(0);
    }    
    /**
     * Returns the wrapped {@link CharSequence} as a char
     * @return
     */
    public char asChar() {
    	return this.asChar('\u0000');	// null char
    }
    /**
     * Returns the wrapped {@link CharSequence} as a char
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param defValue
     * @return
     */
    public char asChar(final char defValue) {
    	return StringConverter.asChar(_theString,defValue);
    }

/////////////////////////////////////////////////////////////////////////////////////////
//  DOUBLE
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the given {@link CharSequence} as a double
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param value
     * @param defValue
     * @return
     */
    public static double asDouble(final CharSequence value,final double defValue) {
    	if (value == null) return defValue;
    	return Double.parseDouble(value.toString());
    }    
    /**
     * Returns the wrapped {@link CharSequence} as a double
     * @return
     */
    public double asDouble() {
    	return this.asDouble(0D);
    }
    /**
     * Returns the wrapped {@link CharSequence} as a double
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param defValue
     * @return
     */
    public double asDouble(final double defValue) {
    	return StringConverter.asDouble(_theString,defValue);
    }

/////////////////////////////////////////////////////////////////////////////////////////
//  FLOAT
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the given {@link CharSequence} as a float
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param value
     * @param defValue
     * @return
     */
    public static Float asFloat(final CharSequence value,final float defValue) {
    	if (value == null) return defValue;
    	return Float.parseFloat(value.toString());
    }
    /**
     * Returns the wrapped {@link CharSequence} as a float
     * @return
     */
    public float asFloat() {
    	return this.asFloat(0F);
    }
    /**
     * Returns the wrapped {@link CharSequence} as a float
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param defValue
     * @return
     */
    public float asFloat(final float defValue) {
    	return StringConverter.asFloat(_theString,defValue);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  INTEGER
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the given {@link CharSequence} as an integer
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param value
     * @param defValue
     * @return
     */
    public static int asInteger(final CharSequence value,final int defValue) {
    	if (value == null) return defValue;
    	return Integer.parseInt(value.toString());
    }
    /**
     * Returns the wrapped {@link CharSequence} as an integer
     * @return
     */
    public int asInteger() {
    	return this.asInteger(0);
    }
    /**
     * Returns the wrapped {@link CharSequence} as an integer
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param defValue
     * @return
     */
    public int asInteger(final int defValue) {
    	return StringConverter.asInteger(_theString,defValue);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  LONG
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the given {@link CharSequence} as a long
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param value
     * @param defValue
     * @return
     */
    public static long asLong(final CharSequence value,final long defValue) {
    	if (value == null) return defValue;
    	return Long.parseLong(value.toString());
    }
    /**
     * Returns the wrapped {@link CharSequence} as a long
     * @return
     */
    public long asLong() {
    	return this.asLong(0L);
    }
    /**
     * Returns the wrapped {@link CharSequence} as a long
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param defValue
     * @return
     */
    public long asLong(final long defValue) {
    	return StringConverter.asLong(_theString,defValue);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  SHORT
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the given {@link CharSequence} as a short
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param value
     * @param defValue
     * @return
     */
    public static short asShort(final CharSequence value,final short defValue) {
    	if (value == null) return defValue;
    	return Short.parseShort(value.toString());
    }
    /**
     * Returns the wrapped {@link CharSequence} as a short
     * @return
     */
    public short asShort() {
    	return this.asShort((short)0);
    }
    /**
     * Returns the wrapped {@link CharSequence} as a short
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param defValue
     * @return
     */
    public short asShort(final short defValue) {
    	return StringConverter.asShort(_theString,defValue);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  DATE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as a {@link Date} using the given format
	 * as a pattern to get the date
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param value
	 * @param format
	 * @param defValue
	 * @return
	 */
	public static Date asDate(final CharSequence value,final String format,
							  final Date defValue) {
		if (value == null) return defValue;
		return Dates.fromFormatedString(value.toString(),format);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link Date} using the given format
	 * as a pattern to get the date
	 * @param format
	 * @return
	 */
	public Date asDate(final String format) {
		return this.asDate(format,null);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link Date} using the given format
	 * as a pattern to get the date
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param format
	 * @param defValue
	 * @return
	 */
	public Date asDate(final String format,final Date defValue) {
		return StringConverter.asDate(_theString,format,
									  defValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PATH
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as a {@link Path}
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param value
	 * @param defValue
	 * @return
	 */
	public static Path asPath(final CharSequence value,final Path defValue) {
		if (value == null) return defValue;
		return Path.from(value.toString());
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link Path}
	 * @return
	 */
	public Path asPath() {
		return this.asPath(null);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link Path}
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param defValue
	 * @return
	 */
	public Path asPath(final Path defValue) {
		return StringConverter.asPath(_theString,defValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Language
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as a {@link Language}
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param value
	 * @param defValue
	 * @return
	 */
	public static Language asLanguageFromCountryCode(final CharSequence value,final Language defValue) {
		if (value == null) return defValue;
		return Languages.fromCountryCode(value.toString());
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link Language}
	 * @return
	 */
	public Language asLanguageFromCountryCode() {
		return this.asLanguageFromCountryCode(null);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link Language}
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param defValue
	 * @return
	 */
	public Language asLanguageFromCountryCode(final Language defValue) {
		return StringConverter.asLanguageFromCountryCode(_theString,defValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ENUM
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as an {@link Enum} of the given type
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param value
	 * @param enumType
	 * @param defValue
	 * @return
	 */
	public static <E extends Enum<E>> E asEnumElement(final CharSequence value,final Class<E> enumType,final E defValue) {
		E outE = defValue;
		if (!Strings.isNullOrEmpty(value)) {
			try {
				outE = Enum.valueOf(enumType,value.toString());
			} catch(IllegalArgumentException illArgEx) {
				outE = defValue;	// there's NO enum value 
			}
		}
		return outE;
	}
	/**
	 * Returns the wrapped {@link CharSequence} as an {@link Enum} of the given type
	 * @param enumType
	 * @return
	 */
	public <E extends Enum<E>> E asEnumElement(final Class<E> enumType) {
		return this.asEnumElement(enumType,null);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as an {@link Enum} of the given type
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param enumType
	 * @param defValue
	 * @return
	 */
	public <E extends Enum<E>> E asEnumElement(final Class<E> enumType,
											   final E defValue) {
		return StringConverter.asEnumElement(_theString,enumType,defValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TYPE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as an instance of the given type
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * The type MUST implement a valueOf(String) method or a constructor with a String param
	 * @param value
	 * @param type
	 * @param defValue
	 * @return
	 */
	public static <T> T asType(final CharSequence value,final Class<T> type,final T defValue) {
		if (value == null) return defValue;
		T outValue = ReflectionUtils.<T>createInstanceFromString(type,
																 value.toString());
		return outValue;
	}
	/**
	 * Returns the given {@link CharSequence} as an instance of the given type
	 * The type MUST implement a valueOf(String) method or a constructor with a String param
	 * @param type
	 * @return
	 */
	public <T> T asType(final Class<T> type) {
		return this.asType(type,null);
	}
	/**
	 * Returns the given {@link CharSequence} as an instance of the given type
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * The type MUST implement a valueOf(String) method or a constructor with a String param
	 * @param type
	 * @param defValue
	 * @return
	 */
	public <T> T asType(final Class<T> type,final T defValue) {
		return StringConverter.asType(_theString,type,defValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STRING BUILDER
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the given {@link CharSequence} as a {@link StringBuilder} 
	 * ...or the given default value if the wrapped {@link CharSequence} is null
     * @param value
     * @param defValue
     * @return
     */
    public static StringBuilder asStringBuilder(final CharSequence value,final StringBuilder defValue) {
    	if (value == null) return defValue;
    	return new StringBuilder(value);
    }
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link StringBuilder}
	 * @return
	 */
	public StringBuilder asStringBuilder() {
		return StringConverter.asStringBuilder(_theString,null);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link StringBuilder}
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param defValue
	 * @return
	 */
	public StringBuilder asStringBuilder(final StringBuilder defValue) {
		return StringConverter.asStringBuilder(_theString,defValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STRING BUFFER
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as a {@link StringBuffer}
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param value
	 * @param defValue
	 * @return
	 */
	public static StringBuffer asStringBuffer(final CharSequence value,final StringBuffer defValue) {
		if (value == null) return defValue;
		return new StringBuffer(value);
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a {@link StringBuffer}
	 * @return
	 */
	public StringBuffer asStringBuffer() {
		return StringConverter.asStringBuffer(_theString,null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CHAR ARRAY
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the given {@link CharSequence} as a char array
	 * ...or the given default value if the wrapped {@link CharSequence} is null
	 * @param value
	 * @param defValue
	 * @return
	 */
	public static char[] asCharArray(final CharSequence value,final char[] defValue) {
		if (value == null) return defValue;
		return value.toString().toCharArray(); 
	}
	/**
	 * Returns the wrapped {@link CharSequence} as a char array
	 * @return
	 */
	public char[] asCharArray() {
		return StringConverter.asCharArray(_theString,null);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  InputStream
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns an {@link InputStream} to the buffer, that's the {@link String} as an {@link InputStream}
	 * the stream's byte charset is provided 
	 * @param str
	 * @param charset 
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public static InputStream asInputStream(final CharSequence str,
											final Charset charset) {
		if (str == null) return null;
		return new ByteArrayInputStream(str.toString().getBytes(charset));
	}
	/**
	 * Returns an {@link InputStream} to the buffer, that's the {@link String} as an {@link InputStream}
	 * (the stream's byte charset is the System's default charset)
	 * @param str
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public static InputStream asInputStream(final CharSequence value) {
		return StringConverter.asInputStream(value,
									 		 Charset.defaultCharset());
	}
	/**
	 * Returns an {@link InputStream} to the buffer, that's the {@link String} as an {@link InputStream}
	 * (the stream's byte charset is the System's default charset)
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public InputStream asInputStream() {
		return StringConverter.asInputStream(_theString);
	}
	/**
	 * Returns an {@link InputStream} to the buffer, that's the {@link String} as an {@link InputStream}
	 * the stream's byte charset is provided 
	 * @param charset 
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public InputStream asInputStream(final Charset charset) {
		return StringConverter.asInputStream(_theString,charset);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns a {@link Reader} to the buffer, that's the {@link String} as a {@link Reader}
	 * @param str
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public static Reader asReader(final CharSequence str) {
		if (str == null) return null;
		return new StringReader(str.toString());
	}
	/**
	 * Returns a {@link Reader} to the buffer, that's the {@link String} as a {@link Reader}
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public Reader asReader() {
		return StringConverter.asReader(_theString);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  Xml
/////////////////////////////////////////////////////////////////////////////////////////
	public static XMLString asXml(final CharSequence str) {
		return new XMLString(str);
	}
	/**
	 * Provides access to the XML operations
	 */
	public XMLString asXml() {
		return StringConverter.asXml(_theString);
	}
}
