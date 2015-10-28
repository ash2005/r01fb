package r01f.util.types;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.text.Normalizer;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.lang3.StringEscapeUtils;

import r01f.debug.Debuggable;
import r01f.encoding.TextEncoder;
import r01f.marshalling.Marshaller;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderBuilder;
import r01f.resources.ResourcesLoaderFromClassPath;
import r01f.resources.ResourcesLoaderFromFileSystem;
import r01f.util.types.collections.CollectionUtils;

import com.google.common.annotations.GwtIncompatible;
import com.google.common.base.CharMatcher;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;


/**
 * This type has two purposes:
 * <ul>
 * 		<li>Provide a fluent API for string operations:
 * 			<pre class='brush:java'>
 * 				// Customize a string
 * 				String myString = Strings.of("this is a {} string")
 * 										 .customizeWith("customized")
 * 										 .asString();
 * 				// Chain operations: encode as UTF8 and save in a file
 * 				Strings.of("this is a string").encodeUTF8().save("/usr/sampler/string.txt");
 * 			</pre>
 * 		</li>
 * 		<li>Provide some utillity static methods:
 * 			<pre class='brush:java'>
 * 				String myQuotedStr = Strings.quote("my unquoted str");
 * 				...
 * 				String customized = Strings.customized("{}:{}",
 * 													   "hello","Alex");
 * 				...
 * 				int anInt = Strings.asInteger("25");
 * 				...
 * 				Reader r = Strings.asReader("sadfasdfasd");
 * 			</pre>
 *		</li>
 *		<li>XML-specific functions like:
 * 			<pre class='brush:java'>
 * 				Strings.load("/usr/sampler/theXml.xml").asXml().encodeUTF8();
 * 			</pre>
 * 		</li>
 * </ul>
 */
public class Strings {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	public static final String EMPTY = "";
/////////////////////////////////////////////////////////////////////////////////////////
//  BUILDERS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 */
	public static StringExtended create() {
		return new StringExtended("");
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * @param length the wrapper's buffer length
	 */
	public static StringExtended create(final int length) {
		return new StringExtended(length);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * The wrapper's buffer is of a fixed length filled with the provided char 
	 * @param theChar
	 * @param size 
	 */
	public static StringExtended createFillingWithChar(final char theChar,final int size) {
		String theString = StringUtils.rightPad("",size,theChar);
		return new StringExtended(theString);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * @param string the {@link String} to initialize the wrapper's buffer
	 */
	public static StringExtended of(final CharSequence string) {
		return new StringExtended(string);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * @param strings the {@link String}s to initialize the wrapper's buffer
	 */
	public static StringExtended of(final CharSequence... strings) {
		return new StringExtended(strings);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * @param bytes the byte array to initialize the wrapper's buffer
	 */
	public static StringExtended of(final byte[] bytes) {
		return Strings.of(new String(bytes));
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * @param bytes the byte array to initialize the wrapper's buffer
	 * @param charset the charset fo the byte array
	 * @throws IOException
	 */
	@GwtIncompatible("The constructor String(byte[],charset) is not supported")
	public static StringExtended of(final byte[] bytes,
								    final Charset charset) throws IOException {
		String string = new String(bytes,charset);
		return Strings.of(string);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * the wrapper's buffer is loaded from a path that can be:
	 * <ul>
	 * 	<li>a classpath's relative path</li>
	 * 	<li>an absolute filesystem path</li>
	 * </ul>
	 * The {@link ResourcesLoader} used to load the file in the provided path is the one which is 
	 * going to use the path 
	 * @param loader {@link ResourcesLoader} implementation: {@link ResourcesLoaderFromClassPath}, {@link ResourcesLoaderFromFileSystem}, etc
	 * 				 if null, the default {@link ResourcesLoaderFromClassPath} is used
	 * @param filePath the file path
	 * @throws IOException 
	 */
	public static StringExtended load(final ResourcesLoader loader,
									  final String filePath) throws IOException {
		ResourcesLoader theLoader = loader == null ? ResourcesLoaderBuilder.DEFAULT_RESOURCES_LOADER
												   : loader;
		String string = StringPersistenceUtils.load(theLoader,filePath);
		return new StringExtended(string);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * The wrapper's buffer is loaded from a stream
	 * @param is the stream
	 */
	@GwtIncompatible("IO is not supported by GWT")
	public static StringExtended of(final InputStream is) throws IOException {
		return Strings.of(is,null);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * The wrapper's buffer is loaded from a stream
	 * @param is the stream
	 * @param charset the byte stream charset
	 */
	@GwtIncompatible("IO is not supported by GWT")
	public static StringExtended of(final InputStream is,
								    final Charset charset) throws IOException {
		String string = StringPersistenceUtils.load(is,charset);
		return new StringExtended(string);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * The wrapper's buffer is loaded from a stream
	 * @param r the stream reader
	 */
	@GwtIncompatible("IO is not supported by GWT")
	public static StringExtended of(final Reader r) throws IOException {
		return Strings.of(r,null);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * The wrapper's buffer is loaded from a stream
	 * @param r the stream reader
	 * @param charset the byte stream charset
	 */
	@GwtIncompatible("IO is not supported by GWT")
	public static StringExtended of(final Reader r,
								    final Charset charset) throws IOException {
		String string = StringPersistenceUtils.load(r,charset);
		return new StringExtended(string);
	}
	/**
	 * Creates a new {@link StringExtended} wrapper object to have access to fluent operations
	 * The wrapper's buffer is loaded from a file
	 * @param f the file
	 * @throws IOException 
	 */
	@GwtIncompatible("IO is not supported by GWT")
	public static StringExtended of(final File f) throws IOException {
		String string = StringPersistenceUtils.load(f);
		return new StringExtended(string);
	}
///////////////////////////////////////////////////////////////////////////////
// 	WRAPPER
///////////////////////////////////////////////////////////////////////////////
	public static class StringExtended 
	         implements CharSequence,
	                    Serializable {
		private static final long serialVersionUID = -7028698392502771655L;
		/**
		 * The buffer
		 */
		protected StringBuilder _buffer;

		StringExtended(final int size) {
			_createBuffer(size);
		}
		StringExtended(final int size,final CharSequence theString) {
			_createBuffer(size);
			_buffer.append(theString);
		}
		StringExtended(final CharSequence theString) {
			if (theString == null) {
				_createBuffer(0);
			} else {
				_createBuffer(theString.length());
				_buffer.append(theString);
			}
		}
		StringExtended(final CharSequence... theStrings) {
			if (theStrings != null && theStrings.length > 0) {
				int totalLength = 0;
				for (CharSequence cs : theStrings) totalLength = totalLength + (cs != null ? cs.length() : 0);
				_createBuffer(totalLength);
				for (CharSequence cs : theStrings) _buffer.append(cs);
			}
		}
		private void _createBuffer(int size) {
			_buffer  = size > 0 ? new StringBuilder(size)
							    : new StringBuilder();
		}
		@Override
		public char charAt(final int index) {
			return _buffer.charAt(index);
		}
		@Override
		public int length() {
			return _buffer.length();
		}
		@Override
		public CharSequence subSequence(final int start,final int end) {
			return _buffer.subSequence(start,end);
		}
		@Override
		public String toString() {
			return _buffer.toString();
		}
		/**
		 * Returns as a {@link String}
		 */
		public String asString() {
			return this.toString();
		}
		/**
		 * Returns as an {@link Integer}
		 */
		public int asInteger() {
			return Strings.asInteger(_buffer);
		}
		/** 
		 * Returns as a {@link Long}
		 */
		public long asLong() {
			return Strings.asLong(_buffer);
		}
		/**
		 * Returns as a {@link Double}
		 */
		public double asDouble() {
			return Strings.asDouble(_buffer);
		}
		/**
		 * Returns as a {@link Float}
		 */
		public Float asFloat() {
			return Strings.asFloat(_buffer);
		}
		/**
		 * Returns as a byte array encoded on the system's default charset
		 */
		@GwtIncompatible("Charset.defaultCharset() is NOT supported by GWT")
		public byte[] getBytes() {
			return Strings.getBytes(_buffer);
		}
		/**
		 * Returns as a byte array encoded on the provided charset
		 * @param charset the encoding
		 */
		@GwtIncompatible("String.getBytes(charset) is NOT suppported by GWT")
		public byte[] getBytes(final Charset charset) {
			return Strings.getBytes(_buffer,charset);
		}
		/**
		 * Returns as a {@link StringBuilder}
		 */
		public StringBuilder asStringBuilder() {
			return _buffer;
		}
		/**
		 * Returns as a {@link StringBuffer}
		 */
		public StringBuffer asStringBuffer() {
			return Strings.asStringBuffer(_buffer);
		}
		/**
		 * Returns as a char array
		 */
		public char[] asCharArray() {
			return Strings.asCharArray(_buffer);
		}
		/**
		 * Returns an {@link InputStream} to the buffer, that's the {@link String} as an {@link InputStream}
		 * (the stream's byte charset is the System's default charset)
		 */
		@GwtIncompatible("IO is NOT supported by GWT")
		public InputStream asInputStream() {
			return Strings.asInputStream(_buffer);
		}
		/**
		 * Returns an {@link InputStream} to the buffer, that's the {@link String} as an {@link InputStream}
		 * the stream's byte charset is provided 
		 * @param charset 
		 */
		@GwtIncompatible("IO is NOT supported by GWT")
		public InputStream asInputStream(final Charset charset) {
			return Strings.asInputStream(_buffer,charset);
		}
		/**
		 * Returns a {@link Reader} to the buffer, that's the {@link String} as a {@link Reader}
		 */
		@GwtIncompatible("IO is NOT supported by GWT")
		public Reader asReader() {
			return Strings.asReader(_buffer);
		}
		/**
		 * Returns an array of {@link String}s with each line of the original {@link String}
		 */
		public String[] getLines() {
			return Strings.getLines(_buffer);
		}	
		/**
		 * Provides access to the XML operations
		 */
		public XMLString asXml() {
			return new XMLString(_buffer);
		}
		/**
		 * Ensures that the {@link String} matches the provided regular expression, if NO match is found
		 * an {@link IllegalArgumentException} is thrown
		 * @param regExp regular expresion to match against
		 */
		@GwtIncompatible("regex is NOT supported by GWT")
		public StringExtended mustMatch(final String regExp) {
			return this.mustMatch(Pattern.compile(regExp));
		}
		/**
		 * Ensures that the wrapped string matches the provided regular expression.
		 * If no match if found an {@link IllegalArgumentException} is thrown
		 * @param regExp regular expresion to match against
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public StringExtended mustMatch(final Pattern regExp) {
			if (_buffer == null) throw new IllegalArgumentException("The string does NOT match the expression: " + regExp.pattern() + " because it's null");
			Matcher m = regExp.matcher(_buffer);
			if (!m.matches()) throw new IllegalArgumentException("The string " + _buffer + " does NOT match the expression: " + regExp.pattern());
			return this;
		}
		/**
		 * Formats the {@link String} using C-style formating (see http://download.oracle.com/javase/1.5.0/docs/api/index.html?java/util/Formatter.html)
		 * For example
		 * 		<pre class='brush:java'>
		 * 			Calendar cal = new GregorianCalendar(1995, MAY, 23)
		 * 			Strings.create("Duke's Birthday: %1$tm %1$te,%1$tY").format(Locale.US,cal).asString();
		 * 			// --> s == "Duke's Birthday: May 23, 1995"
		 * 		</pre>
		 * @param l
		 * @param args
		 */
		@GwtIncompatible("Locale is NOT supported by GWT")
		public StringExtended format(final Locale l,final Object args) {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(String.format(l,this.asString(),args));
			return this;
		}
		/**
		 * Formats the {@link String} using C-style formating (see http://download.oracle.com/javase/1.5.0/docs/api/index.html?java/util/Formatter.html)
		 * For example
		 * 		<pre class='brush:java'>
		 * 			Calendar cal = new GregorianCalendar(1995, MAY, 23)
		 * 			Strings.create("Duke's Birthday: %1$tm %1$te,%1$tY").format(cal).asString();
		 * 			// --> s == "Duke's Birthday: May 23, 1995"
		 * 		</pre>
		 * @param args
		 */
		@GwtIncompatible("format(String,Object) method of String type is NOT supported by GWT")
		public StringExtended format(final Object args) {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(String.format(this.asString(),args));
			return this;
		}
		/**
		 * Encodes the {@link String} using www-form-urlencodec encoding
		 * @return 
		 * @throws EncoderException 
		 */
		@GwtIncompatible("Encoding/Decoding is NOT supported by GWT")
		public StringExtended urlEncode() throws EncoderException {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.urlEncode(_buffer));
			return this;
		}
		/**
		 * Encodes the {@link String} using www-form-urlencodec encoding
		 * @return 
		 */
		@GwtIncompatible("Encoding/Decoding is NOT supported by GWT")
		public StringExtended urlEncodeNoThrow() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.urlEncodeNoThrow(_buffer));
			return this;
		}
		/**
		 * Decodes the underlying {@link String} asuming it's www-form-urlencodec encoded
		 * @return the decoded {@link String}
		 * @throws DecoderException 
		 */
		@GwtIncompatible("Encoding/Decoding is NOT supported by GWT")
		public StringExtended urlDecode() throws DecoderException {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.urlDecode(_buffer));
			return this;
		}
		/**
		 * Decodes the underlying {@link String} asuming it's www-form-urlencodec encoded
		 * @return the decoded {@link String}
		 */
		@GwtIncompatible("Encoding/Decoding is NOT supported by GWT")
		public StringExtended urlDecodeNoThrow() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.urlDecodeNoThrow(_buffer));
			return this;
		}
		/**
		 * Changes the underlying {@link String} {@link Charset} to the provided one
		 * To load the Charset from it's name use <pre class='brush:java'>Charset.forName(charset_name)</pre>
		 * @param encoding
		 */
		public StringExtended encode(final Charset encoding) {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.encode(_buffer,encoding));
			return this;
		}
		/**
		 * Changes the underlying {@link String}'s {@link Charset} to UTF-8
		 */
		public StringExtended encodeUTF8() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.encodeUTF(_buffer));
			return this;
		}
		/**
		 * Changes the underlying {@link String}'s {@link Charset} to UTF-8
		 */
		public StringExtended encodeISO8859() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.encodeISO8859(_buffer));
			return this;
		}
		/**
		 * Elimina algunos caracteres "especiales" del juego de caracteres utilizados en windows
		 * que es un super-conjunto del ISO8859
		 */
		public StringExtended windows1252ToIso8859() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.windows1252ToIso8859(_buffer));
			return this;
		}
		/**
		 * Filtra caracteres y los sustituye por otros
		 * @param charsToFilter caracteres a filtrar
		 * @param charsFiltered caracteres a sustituir por los filtrados
		 */
		public StringExtended filterAndReplaceChars(final char[] charsToFilter,final String[] charsFiltered) {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEncodeUtils.filterAndReplaceChars(_buffer,charsToFilter,charsFiltered));
			return this;
		}
		/**
		 * Escapes the characters in a JSon string
		 * According to the RFC. JSON is pretty liberal: The only characters that must be escaped are \, ", and control codes (anything less than U+0020).
		 * see http://stackoverflow.com/questions/3020094/how-should-i-escape-strings-in-json
		 * @return
		 */
		public StringExtended escapeJSON() {
			if (_buffer == null) return this;
	        char c = 0;
	        int i;
	        int len = _buffer.length();
	        StringBuilder sb = new StringBuilder(len + 4);
	        String t;
		    sb.append('"');
	        for (i = 0; i < len; i += 1) {
	        	c = _buffer.charAt(i);
	            switch (c) {
	            case '\\':
	            case '"':
	                sb.append('\\');
	                sb.append(c);
	                break;
	            case '/':
	            	sb.append('\\');
	                sb.append(c);
	                break;
	            case '\b':
	                sb.append("\\b");
	                break;
	            case '\t':
	                sb.append("\\t");
	                break;
	            case '\n':
	                sb.append("\\n");
	                break;
	            case '\f':
	                sb.append("\\f");
	                break;
	            case '\r':
	               sb.append("\\r");
	               break;
	            default:
	                if (c < ' ') {
	                    t = "000" + Integer.toHexString(c);
	                    sb.append("\\u" + t.substring(t.length() - 4));
	                } else {
	                    sb.append(c);
	                }
	            }
	        }
	        sb.append('"');
	        _buffer = sb;
	         
			return this;
		}
		/**
		 * Escapes the characters in a String using HTML entities.
		 * For example: "bread" & "butter" becomes &quot;bread&quot; &amp; &quot;butter&quot;.
		 * Supports all known HTML 4.0 entities, including funky accents. 
		 * Note that the commonly used apostrophe escape character (&apos;) is not a legal entity and so is not supported).
		 */
		@GwtIncompatible("apache commons StringEscaptUtils is NOT supported by GWT")
		public StringExtended escapeHTML() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEscapeUtils.escapeHtml4(_buffer.toString()));
			return this;
		}
		/**
		 * Escapes the characters in a String using XML entities.
		 * For example: "bread" & "butter" => &quot;bread&quot; &amp; &quot;butter&quot;.
		 * Supports only the five basic XML entities (gt, lt, quot, amp, apos). Does not support DTDs or external entities.
		 * Note that unicode characters greater than 0x7f are currently escaped to their numerical \\u equivalent. 
		 * This may change in future releases.
		 */
		@GwtIncompatible("apache commons StringEscaptUtils is NOT supported by GWT")
		public StringExtended escapeXML() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringEscapeUtils.escapeXml10(_buffer.toString()));
			return this;
		}
		/**
		 * Elimina todo lo que no es una palabra (espacios, _, /, etc) y lo
		 * sustituye por un _
		 * @param str la cadena de origen
		 * @return la cadena modificada
		 */
		@GwtIncompatible("regex is NOT supported by GWT")
		public StringExtended removeWhitespace() {
			if (_buffer == null) return this;
			// Sustituir todo lo que no es una palabra (espacios, _, /, etc) por un _
			StringBuffer sb = new StringBuffer();
			Pattern regex = Pattern.compile("[^\\w]");
			Matcher regexMatcher = regex.matcher(_buffer);
			while(regexMatcher.find()) regexMatcher.appendReplacement(sb,"");
			regexMatcher.appendTail(sb);
			_buffer = new StringBuilder(sb.toString());
			return this;
		}
		/**
		 * Removes all \n or \r characters
		 * @return
		 */
		public StringExtended removeNewlinesOrCarriageRetuns() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(_buffer.toString().replaceAll("[\n\r]",""));
			return this;
		}
		/**
		 * Ajusta el tamaño de la cadena añadiendo caracteres por la izquierda
		 * hasta completar el tamaño requerido
		 * @param size tamaño requerido
		 * @param character caracter
		 * @return
		 */
		public StringExtended leftPad(final int size,final char character) {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringUtils.leftPad(_buffer.toString(),size,character));
			return this;
		}
		/**
		 * Ajusta el tamaño de la cadena añadiendo caracteres por la derecha
		 * hasta completar el tamaño requerido
		 * @param size tamaño requerido
		 * @param character caracter
		 * @return
		 */
		public StringExtended rightPad(final int size,final char character) {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringUtils.rightPad(_buffer.toString(),size,character));
			return this;
		}
		/**
		 * Elimina los acentos de la cadena y los remplaza por su equivalente sin acento
		 */
		@GwtIncompatible("Normalized and regex is not supported by GWT")
		public StringExtended removeAccents() {
			if (_buffer == null ) return this;
			// ver http://www.v3rgu1.com/blog/231/2010/programacion/eliminar-acentos-y-caracteres-especiales-en-java/
			// se utiliza la técnica de descomposición canónica:
			//		Un caracter se puede representar de varias formas:
			//			- char c1 = 'í'							representacion nombral
			//			- char c2 = '\u00ed'					unicode
			//			- char[] c3 = {'\u0069', '\u0301'};		unicode en forma canonica
			//		La representación unicode canonica básicamente es dos caracteres: la letra base +  el acento
			//		de esta forma el caracter í se puede representar como la letra (\u0069) y el acento (\u0301)
		    String normalized = Normalizer.normalize(_buffer, Normalizer.Form.NFD);
		    // quedarse únicamente con los caracteres ASCII
		    Pattern pattern = Pattern.compile("[^\\p{ASCII}+]");
		    _buffer = new StringBuilder(pattern.matcher(normalized).replaceAll(""));
		    return this;
		}
		/**
		 * Pone la cadena en mayúsculas
		 */
		public StringExtended toUpperCase() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(_buffer.toString().toUpperCase());
			return this;
		}
		/**
		 * Pone la cadena en minúsculas
		 */
		public StringExtended toLowerCase() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(_buffer.toString().toLowerCase());
			return this;			
		}
		/**
		 * Pone la primera letra en mayúsculas
		 */
		public StringExtended capitalizeFirstLetter() {
			if (_buffer == null) return this;
			_buffer = new StringBuilder(StringUtils.capitalize(_buffer.toString()));
			return this;
		}
		/**
		 * Replaces a string 
		 * @param strToBeReplaced
		 * @param replacement
		 * @return
		 */
		public StringExtended replace(final String strToBeReplaced,final String replacement) {
			if (_buffer == null) return this;
			String newStr = _buffer.toString().replace(strToBeReplaced,replacement);
			_buffer = new StringBuilder(newStr);
			return this;
		}
		/**
		 * Realiza una sustitución de caracteres en la cadena utilizando una expresion regular
		 * @param regex la expresión regular con el patrón a remplazar
		 * @param replacement la cadena que remplaza los matches
		 */
		public StringExtended replaceAll(final String regex,final String replacement) {
			if (_buffer == null) return this;
			String newStr = _buffer.toString().replaceAll(regex,replacement);
			_buffer = new StringBuilder(newStr);
			return this;
		}
	    /**
	     * Sustituye una cadena que contiene variables por sus valores.
	     * Las variables estan delimitadas por un caracter marcador varDelim pej: $varName$
	     * @param inStr La cadena en la que se hacen las sustituciones
	     * @param varDelim La cadena que hace de delimitador de variables
	     * @param varValues Un mapa variable-valor para realizar las sustituciones
	     */
		public StringExtended replaceVariableValues(final String varDelim,final Map<String,String> varValues) {
			return this.replaceVariableValues(varDelim.charAt(0),varValues,false);
		}
		/**
		 * Sustituye una cadena que contiene variables por sus valores
		 * Las variables estan delimitadas por caracter marcador varDelim, pej: $varName$
		 * NOTA:
		 *      Ojo!!   La expresion utiliza expresiones regulares así que hay que
		 *              escapar la cadena a sustituir.
		 * @param inStr La cadena en la que se hacen las sustituciones
		 * @param varDelim El delimitador de variables
		 * @param varValues El valor de las variables variable-valor
		 */
		public StringExtended replaceVariableValues(final char varDelim,final Map<String,String> varValues) {
			return this.replaceVariableValues(varDelim,varValues,false);
		}
	    /**
	     * Sustituye una cadena que contiene variables por sus valores
	     * Las variables estan delimitadas por caracter marcador varDelim, pej: $varName$
	     * NOTA:
	     *      Ojo!!   La expresion utiliza expresiones regulares así que hay que
	     *              escapar la cadena a sustituir.
	     * @param inStr La cadena en la que se hacen las sustituciones
	     * @param varDelim El delimitador de variables
	     * @param varValues El valor de las variables variable-valor
	     * @param deep indica si hay que revisar si las variables a su vez contienen variables
	     */
		public StringExtended replaceVariableValues(final String varDelim,final Map<String,String> varValues,
    										   	   final boolean deep) {
			return this.replaceVariableValues(varDelim.charAt(0),varValues,deep);
		}
		/**
	     * Sustituye una cadena que contiene variables por sus valores
	     * Las variables estan delimitadas por caracter marcador varDelim, pej: $varName$
	     * NOTA:
	     *      Ojo!!   La expresion utiliza expresiones regulares así que hay que
	     *              escapar la cadena a sustituir.
	     * @param inStr La cadena en la que se hacen las sustituciones
	     * @param varDelim El delimitador de variables
	     * @param varValues El valor de las variables variable-valor
	     * @param deep indica si hay que revisar si las variables a su vez contienen variables
	     */
		public StringExtended replaceVariableValues(final char varDelim,final Map<String,String> varValues,
												   final boolean deep) {
			if (_buffer == null) return this;
			String newStr = StringCustomizeUtils.replaceVariableValues(_buffer.toString(),varDelim,varValues,deep);
			_buffer  = new StringBuilder(newStr);
			return this;
		}
		/**
		 * Guarda la cadena en un fichero
		 * @param f el fichero al que se guarda
		 * @throws IOException si no se puede guardar
		 */
		@GwtIncompatible("IO is not supported by GWT")
		public void save(final File f) throws IOException {
			StringPersistenceUtils.save(_buffer,f);
		}
		/**
		 * Guarda la cadena en un fichero
		 * @param filePath la ruta al fichero
		 * @throws IOException si no se puede guardar
		 */
		@GwtIncompatible("IO is not supported by GWT")
		public void save(final String filePath) throws IOException {
			StringPersistenceUtils.save(_buffer,filePath);
		}
		/**
		 * Prepends a character
		 * @param character
		 * @return
		 */
		public StringExtended prepend(final char character) {
			if (_buffer == null) return this;
			_buffer.insert(0,character);
			return this;
		}
		/**
		 * Prepends another {@link CharSequence}
		 * @param other
		 * @return
		 */
		public StringExtended prepend(final CharSequence other) {
			if (_buffer == null) return this;
			_buffer.insert(0,other);
			return this;
		}
		/**
		 * Añade un caracter
		 * @param character el caracter
		 * @return un wrapper de la cadena
		 */
		public StringExtended add(final char character) {
			if (_buffer == null) return this;
			_buffer.append(character);
			return this;
		}
		/**
		 * Añade una cadena
		 * @param other
		 */
		public StringExtended add(final CharSequence other) {
			if (_buffer == null) return this;
			if (other != null) _buffer.append(other);
			return this;
		}
		/**
		 * Añade el contenido que se lee de un inputStream
		 * @param is el inputStream del que leer el texto
		 */
		@GwtIncompatible("IO is not supported by GWT")
		public StringExtended add(final InputStream is) throws IOException {
			return this.add(is,null);
		}
		/**
		 * Añade el contenido que se lee de un inputStream
		 * @param is el inputStream del que leer el texto
		 * @param encoding la codificación del is
		 */
		@GwtIncompatible("IO is not supported by GWT")
		public StringExtended add(final InputStream is,final Charset encoding) throws IOException {
			String s = StringPersistenceUtils.load(is,encoding);
			return this.add(s);
		}
		/**
		 * Añade el contenido que se lee de un reader
		 * @param r el reader
		 */
		@GwtIncompatible("IO is not supported by GWT")
		public StringExtended add(final Reader r) throws IOException {
			return this.add(r,null);
		}
		/**
		 * Añade el contenido que se lee de un reader
		 * @param r el reader
		 * @param encoding la codificación de reader
		 */
		@GwtIncompatible("IO is not supported by GWT")
		public StringExtended add(final Reader r,final Charset encoding) throws IOException {
			String s = StringPersistenceUtils.load(r);
			return this.add(s);
		}
		/**
		 * Adds a customized String where variables are provided by the {@link StringCustomizerProvider} instance
		 * The {@link StringCustomizerProvider} instance can be in two flavours:
		 * <ul>
		 * 		<li>{@link StringCustomizerProvider} if the provided string is added without evaluating any condition</li>
		 * 		<li>{@link StringCustomizerProviderConditioned} if the provided string is added if the shouldAdd() method of the vars provider returns true</li>
		 * <ul>
		 * Sample code:
		 * <pre class="brush:java">
		 * 		String customized = Strings.create()
		 * 								   .add(new StringCustomizerProviderConditioned() {
		 * 												@Override
		 * 												public CharSequence provideStringToAdd() {
		 * 													return "the var value is {}";				
		 * 												}
		 * 												@Override
		 * 												public bolean shouldAdd() {
		 * 													return true;		// any condition could be evaluated to guess if the string should be added or not
		 * 												}
		 * 												@Override
		 * 												public Object[] provideVars() {	
		 * 													return new Object[] {"the value"};
		 * 												}
		 * 										})
		 * 								   .asString();
		 * </pre>
		 * @param other
		 * @param varsProvider
		 * @return
		 */
		public StringExtended add(final StringCustomizerProvider varsProvider) {
			boolean shouldAdd = (varsProvider instanceof StringCustomizerProviderConditioned) ? ((StringCustomizerProviderConditioned)varsProvider).shouldAdd()
																							  : true;
			if (shouldAdd) {
				Object[] vars = varsProvider.provideVars();
				return this.addCustomized(varsProvider.provideStringToAdd(),
										  vars);
			}
			return this;
		}		
		public interface StringCustomizerProvider {
			public CharSequence provideStringToAdd();
			public Object[] provideVars();
		}
		public interface StringCustomizerProviderConditioned 
  				 extends StringCustomizerProvider {
			public boolean shouldAdd();
		}
		/**
		 * Añade una cadena customizada con la variable que se pasa pero SOLO si var != null
		 * (la cadena NO se añade si var == null)
		 * @param other la cadena a añadir
 		 * @param var la variable con la que customizar la cadena
		 */
		public StringExtended addCustomizedIfParamNotNull(final CharSequence other,final Object var) {
			if (var == null || Strings.isNullOrEmpty(var.toString())) return this;
			StringBuffer customized = Strings.customize(other,var);
			return customized != null ? this.add(customized) : this;
		}
		/**
		 * Adds a string if the second parameter is NOT null
		 * @param other
		 * @param var
		 */
		public StringExtended addIfParamNotNull(final CharSequence other,final Object var) {
			if (var == null) return this;
			return this.add(other);
		}
		/**
		 * Adds the parameter if it's not null
		 * @param other
		 * @param var
		 */
		public StringExtended addIfNotNull(final Object var) {
			if (var == null) return this;
			return this.add(var.toString());
		}
		/**
		 * Adds a string if the second parameter is null
		 * @param other
		 * @param test
		 */
		public StringExtended addIf(final CharSequence other,final boolean test) {
			if (!test) return this;
			return this.add(other);
		}
		/**
		 * Adds a string if the provided {@link Predicate} applies on the also provided argument 
		 * @param other other string to be added
		 * @param pred the predicate
		 * @param predArg the target to apply the predicate
		 */
		public <T> StringExtended addIf(final CharSequence other,final Predicate<T> pred,final T predArg) {
			if (pred == null) return this;
			return pred.apply(predArg) ? this.add(other) : this;
		}
		/**
		 * Añade una cadena remplazando "placeholders" como {} por las variables que se pasan
		 * Ej:
		 * 		Strings.of("Hola {} hoy es {}","Alex","Sabado"}
		 * @param other la cadena a customizar
		 * @param vars las variables a sustituir
		 */
		public StringExtended addCustomized(final CharSequence other,final Object... vars) {
			StringBuffer customized = Strings.customize(other,vars);
			return customized != null ? this.add(customized) : this;
		}
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
		/**
		 * Customizes the underlyng string with variables are provided by the {@link StringCustomizerVarsProvider} instance
		 * The {@link StringCustomizerVarsProvider} instance can be in two flavours:
		 * <ul>
		 * 		<li>{@link StringCustomizerVarsProvider} if the provided string is added without evaluating any condition</li>
		 * 		<li>{@link StringCustomizerProviderVarsConditioned} if the provided string is added if the shouldAdd() method of the vars provider returns true</li>
		 * <ul>
		 * Sample code:
		 * <pre class="brush:java">
		 * 		String customized = Strings.of("the var value is {}")
		 * 								   .customizeWith(new StringCustomizerVarsProviderConditioned() {
		 * 														@Override
		 * 														public bolean shouldAdd() {
		 * 															return true;		// any condition could be evaluated to guess if the string should be added or not
		 * 														}
		 * 														@Override
		 * 														public Object[] provideVars() {	
		 * 															return new Object[] {"the value"};
		 * 														}
		 * 												  })
		 * 								   .asString();
		 * </pre>
		 * @param other
		 * @param varsProvider
		 * @return
		 */
		public StringExtended customizeWith(final StringCustomizerVarsProvider varsProvider) {
			boolean shouldAdd = (varsProvider instanceof StringCustomizerVarsProviderConditioned) ? ((StringCustomizerVarsProviderConditioned)varsProvider).shouldAdd()
																							      : true;
			if (shouldAdd) {
				Object[] vars = varsProvider.provideVars();
				return this.customizeWith(vars);
			}
			return this;
		}
		public interface StringCustomizerVarsProvider {
			public Object[] provideVars();
		}
		public interface StringCustomizerVarsProviderConditioned 
  				 extends StringCustomizerProvider {
			public boolean shouldAdd();
		}
		/**
		 * Customiza el buffer remplazando "placeholders" como {} por las variables que se pasan
		 * Ej:
		 * 		Strings.of("Hola {} hoy es {}","Alex","Sabado"}
		 * @param vars las variables a sustituir
		 */
		public StringExtended customizeWith(final CharSequence...vars) {
			StringBuffer customized = Strings.customize(_buffer,(Object[])vars);
			if (customized != null) _buffer = new StringBuilder(customized);
			return this;
		}
		/**
		 * Customiza el buffer remplazando "placeholders" como {} por las variables que se pasan
		 * Ej:
		 * 		Strings.of("Hola {} hoy es {}","Alex","Sabado"}
		 * @param vars las variables a sustituir
		 */
		public StringExtended customizeWith(final Object...vars) {
			if (vars == null || vars.length == 0) return this.customizeWith((CharSequence[])null);
			String[] varsAsString = new String[vars.length];
			for (int i=0; i < vars.length; i++) {
				varsAsString[i] = vars[i] != null ? _customizingVarToString(vars[i])
												  : null;
			}
			return this.customizeWith(varsAsString);
		}
		private static String _customizingVarToString(final Object var) {
			String outVarStr = null;
			if (var instanceof Collection) {
				StringBuffer sb = new StringBuffer();
				for (Iterator<?> it = ((Collection<?>)var).iterator(); it.hasNext(); ) {
					Object o = it.next();
					sb.append(o.toString());
					if (it.hasNext()) sb.append(",");
				}
				outVarStr = sb.toString();
				
			} else if (var.getClass().isArray()) {
				StringBuffer sb = new StringBuffer();
				Collection<?> col = CollectionUtils.of((Object[])var);
				for (Iterator<?> it = col.iterator(); it.hasNext(); ) {
					Object o = it.next();
					sb.append(o.toString());
					if (it.hasNext()) sb.append(",");
				}
				outVarStr = sb.toString();
				
			} else {
				outVarStr = var.toString();
			}
			return outVarStr;
		}
		/**
		 * Añade un numero
		 * @param num
		 * @return
		 */
		public StringExtended add(final Number num) {
			_buffer.append(num);
			return this;
		}
		/**
		 * Añade un array de chars
		 * @param other el array de chars
		 */
		public StringExtended add(final char[] other) {
			_buffer.append(other);
			return this;
		}
		/**
		 * añade la información de debug de un objeto que implementa {@link Debuggable}
		 * @param debuggable el objeto {@link Debuggable}
		 */
		public StringExtended add(final Debuggable debuggable) {
			if (debuggable == null) return this;
			_buffer.append(debuggable.debugInfo());
			return this;
		}
		/**
		 * Añade una subcadena de una cadena
		 * @param other
		 * @param start posicion inicial de la otra cadena a añadir
		 * @param end posicion final de la otra cadena a añadir
		 */
		public StringExtended add(final CharSequence other,final int start,final int end) {
			_buffer.append(other,start,end);
			return this;
		}
		/**
		 * Añade una porcion de un array de chars
		 * @param other el array de chars
		 * @param start la posicion inicial dentro del array
		 * @param end la posicion final dentro del array
		 */
		public StringExtended add(final char[] other,final int start,final int end) {
			_buffer.append(other,start,end);
			return this;
		}
		/**
		 * Añade una linea
		 * @param line
		 */
		public StringExtended addLine(final CharSequence line) {
			_buffer.append(line);
			_buffer.append("\r\n");
			return this;
		}
		/**
		 * Añade una cadena entrecomillada
		 * @param other cadena que se añade entrecomillada
		 */
		public StringExtended addQuoted(final CharSequence other) {
			_buffer.append("\"").append(other).append("\"");
			return this;
		}
		/**
		 * Añade varias cadenas
		 * @param others
		 */
		public StringExtended add(final CharSequence... others) {
			if (others != null && others.length > 0) {
				for (CharSequence cs : others) {
					_buffer.append(cs);
				}
			}
			return this;
		}
		/**
		 * Añade tantos caracteres c a la derecha de la cadena como sea necesario para llegar al tamaño length
		 * (si la cadena tiene un tamaño mayor o igual que length, NO rellena nada)
		 * @param c el caracter de relleno
		 * @param length el tamaño final de la cadena
		 */
		public StringExtended rightPad(final char c,final int length) {
			if (_buffer.length() <= length) {
				String paddedStr = StringUtils.rightPad(_buffer.toString(),length,c);
				_buffer.delete(0,_buffer.length());	// Borrar el contenido del buffer
				_buffer = _buffer.append(paddedStr);
			}
			return this;
		}
		/**
		 * Añade tantos caracteres c a la derecha de la cadena como sea necesario para llegar al tamaño length
		 * (si la cadena tiene un tamaño mayor o igual que length, NO rellena nada)
		 * @param c el caracter de relleno
		 * @param length el tamaño final de la cadena
		 */
		public StringExtended leftPad(final char c,final int length) {
			if (_buffer.length() <= length) {
				String paddedStr = StringUtils.leftPad(_buffer.toString(),length,c);
				_buffer.delete(0,_buffer.length());	// Borrar el contenido del buffer
				_buffer = _buffer.append(paddedStr);
			}
			return this;
		}
		/**
		 * Surrounds the string with single quotes (')
		 */
		public StringExtended quote() {
			_quote("'");
			return this;
		}
		/**
		 * Surrounds the string with double quotes (")
		 */
		public StringExtended doubleQuote() {
			_quote("\"");
			return this;
		}
		/**
		 * Rodea la cadena entre un caracter que se pasa
		 * @param q el caracter a rodear
		 */
		private StringExtended _quote(final String q) {
			_buffer.insert(0,q);
			_buffer.append(q);
			return this;
		}
		/**
		 * Elimina los espacios en blanco
		 */
		public StringExtended trim() {
			_buffer = new StringBuilder(_buffer.toString().trim());
			return this;
		}
		/**
		 * Codifica el texto utilizando el codificador que se pasa como parametro
		 * @param encoder el codificador
		 */
		public StringExtended encodeUsing(final TextEncoder encoder) {
			if (encoder != null) {
				_buffer = new StringBuilder(encoder.encode(_buffer));
			}
			return this;
		}
		/**
		 * Decodifica el texto utilizando el decodificador que se pasa como parametro
		 * @param decoder decodificador
		 */
		public StringExtended decodeUsing(final TextEncoder decoder) {
			if (decoder != null) {
				_buffer = new StringBuilder(decoder.decode(_buffer));
			}
			return this;
		}
		/**
		 * Devuelve un splitter que trocea la cadena en trozos iguales del tamaño que se pasa
		 * (el último trozo obviamente puede ser de menor tamaño)
		 * @param chunksLength el tamaño de los trozos
		 */
		public StringSplitter splitter(final int chunksLength) {
			return new StringSplitter(Splitter.fixedLength(chunksLength),
									  _buffer);
		}
		/**
		 * Devuelve un splitter sobre la cadena en base al caracter que se pasa
		 * @param separator el caracter separador de los trozos
		 */
		public StringSplitter splitter(final char separator) {
			return new StringSplitter(Splitter.on(separator),
									  _buffer);
		}
		/**
		 * Devuelve un splitter sobre la cadena en base a cualquier caracter que cumpla el matcher
		 * @param separatorMatcher un matcher del caracter separador de los trozos
		 */
		public StringSplitter splitter(final CharMatcher separatorMatcher) {
			return new StringSplitter(Splitter.on(separatorMatcher),
									  _buffer);
		}
		/**
		 * Devuelve un splitter sobre la cadena en base a la cadena que se pasaq
		 * @param separator la cadena separadora de los trozos
		 */
		public StringSplitter splitter(final String separator) {
			return new StringSplitter(Splitter.on(separator),
									  _buffer);
		}
		/**
		 * Devuelve un splitter sobre la cadena en base a cualquier cadena que cumpla el patron
		 * @param separatorPattern un patrón que ha de cumplir la cadena separadora de los trozos
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public StringSplitter splitter(final Pattern separatorPattern) {
			return new StringSplitter(Splitter.on(separatorPattern),
									  _buffer);
		}
		/**
		 * Devuelve un matcher sobre la cadena
		 * @param regEx la expresion regular
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public Matcher matcher(final String regEx) {
			Pattern p = Pattern.compile(regEx);
			Matcher m = p.matcher(_buffer);
			return m;
		}
		/**
		 * Devuelve un matcher sobre la cadena
		 * @param p el patrón sobre el que machear
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public Matcher matcher(final Pattern p) {
			Matcher m = p.matcher(_buffer);
			return m;
		}
		/**
		 * Devuelve un matcher sobre la cadena
		 * @param regEx la expresion regular
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public Matcher match(final String regEx) {
			Matcher m = this.matcher(regEx);
			m.find();
			return m;
		}
		/**
		 * Devuelve un matcher sobre la cadena
		 * @param p el patrón sobre el que machear
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public Matcher match(final Pattern p) {
			Matcher m = this.matcher(p);
			m.find();
			return m;
		}
		/**
		 * Comprueba si la cadena verifica el patrón
		 * @param p
		 * @return
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public boolean matches(final Pattern p) {
			Matcher m = this.matcher(p);
			return m.find();
		}
		/**
		 * Comprueba si la cadena verifica el patrón
		 * @param regEx
		 * @return
		 */
		@GwtIncompatible("regex is not supported by GWT")
		public boolean matches(final String regEx) {
			Matcher m = this.matcher(regEx);
			return m.find();
		}
		/**
		 * Checks if the wrapped {@link String} is in the provided {@link Set} of {@link String}s
		 * @param otherStrings a {@link Set} of {@link Strings}
		 * @return true if the wrapped {@link String} is contained in the otherStrings {@link Set}
		 */
		public boolean in(final Set<String> otherStrings) {
			return this.in(false,otherStrings);
		}
		/**
		 * Checks if the wrapped {@link String} is in the provided {@link Set} of {@link String}s
		 * @param otherStrings a {@link Set} of {@link Strings}
		 * @return true if the wrapped {@link String} is contained in the otherStrings {@link Set}
		 */
		public boolean in(final String... otherStrings) {
			return this.in(false,otherStrings);
		}
		/**
		 * Checks if the wrapped {@link String} is in the provided {@link Set} of {@link String}s
		 * ignorign the {@link String} case
		 * @param otherStrings a {@link Set} of {@link Strings}
		 * @return true if the wrapped {@link String} is contained in the otherStrings {@link Set}
		 */
		public boolean inIgnoringCase(final Set<String> otherStrings) {
			return this.in(true,otherStrings);
		}
		/**
		 * Checks if the wrapped {@link String} is in the provided {@link Set} of {@link String}s
		 * ignorign the {@link String} case
		 * @param otherStrings a {@link Set} of {@link Strings}
		 * @return true if the wrapped {@link String} is contained in the otherStrings {@link Set}
		 */
		public boolean inIgnoringCase(final String... otherStrings) {
			return this.in(true,otherStrings);
		}
		/**
		 * Checks if the wrapped {@link String} is in the provided {@link Set} of {@link String}s
		 * @param otherStrings a {@link Set} of {@link Strings}
		 * @param ignoreCase if the check should take case into account
		 * @return true if the wrapped {@link String} is contained in the otherStrings {@link Set}
		 */
		public boolean in(final boolean ignoreCase,final String... otherStrings) {
			return this.in(ignoreCase,Sets.newHashSet(otherStrings));
		}
		/**
		 * Checks if the wrapped {@link String} is in the provided {@link Set} of {@link String}s
		 * @param otherStrings a {@link Set} of {@link Strings}
		 * @param ignoreCase if the check should take case into account
		 * @return true if the wrapped {@link String} is contained in the otherStrings {@link Set}
		 */
		public boolean in(final boolean ignoreCase,final Set<String> otherStrings) {
			if (CollectionUtils.isNullOrEmpty(otherStrings)) return false;
			boolean outIsIn = false;
			String thisStr =  _buffer.toString();
			for (String s : otherStrings) {
				if (s != null) {
					outIsIn = ignoreCase ? thisStr.equalsIgnoreCase(s)
										 : thisStr.equals(s);
					if (outIsIn) break;
				}
			}
			return outIsIn;
		}
		/**
		 * Comprueba si la cadena contiene alguna de las que se pasan
		 * @param otherStrings otras cadenas
		 * @return true si la cadena contiene una de las que se pasan
		 */
		public boolean containsAny(final String... otherStrings) {
			return this.containsAny(false,otherStrings);
		}
		/**
		 * Comprueba si la cadena contiene alguna de las que se pasan
		 * @param ignoreCase si se ignoran las mayúsculas / minúsculas
		 * @param otherStrings otras cadenas
		 * @return true si la cadena contiene una de las que se pasan
		 */
		public boolean containsAny(final boolean ignoreCase,final String... otherStrings) {
			if (CollectionUtils.isNullOrEmpty(otherStrings)) return false;
			boolean outContains = false;
			String thisStr =  _buffer.toString();
			for (String s : otherStrings) {
				if (s != null) {
					outContains = ignoreCase ? thisStr.toLowerCase().contains(s.toLowerCase())
										     : thisStr.contains(s);
					if (outContains) break;
				}
			}
			return outContains;
		}
	}
///////////////////////////////////////////////////////////////////////////////
// 	WRAPPER PARA CADENAS XML
///////////////////////////////////////////////////////////////////////////////
	public static class XMLString 
	            extends StringExtended {
		private static final long serialVersionUID = -7905073194070461448L;
		
		XMLString(final CharSequence theString) {
			super(theString);
		}
		/**
		 * Elimina algunos caracteres que son invalidos según la especificacion XML
		 * (fundamentalmente caracteres ASCII de control)
		 */
		public XMLString filterInvalidChars() {
			_buffer = new StringBuilder(StringXMLEncodeUtils.filterInvalidChars(_buffer));
			return this;
		}
		/**
		 * Codifica los caracteres UTF de doble byte como una entidad XML (&#code;)
		 */
		public XMLString encodeUTFDoubleByteCharsAsEntities() {
			_buffer = new StringBuilder(StringXMLEncodeUtils.encodeUTFDoubleByteCharsAsEntities(_buffer));
			return this;
		}
		/**
		 * Igual que el metodo encodeUTFDoubleByteCharsAsEntities pero además codifica
		 * las comillas como entidades XML
		 */
		public XMLString encodeUTFDoubleByteCharsAndQuoutesAsEntities() {
			_buffer = new StringBuilder(StringXMLEncodeUtils.encodeUTFDoubleByteCharsAndQuoutesAsEntities(_buffer));
			return this;
		}
		/**
		 * Decodifica las entidades XML
		 */
		public XMLString decodeUTFDoubleByteCharsFromEntities() {
			_buffer = new StringBuilder(StringXMLEncodeUtils.decodeUTFDoubleByteCharsFromEntities(_buffer));
			return this;
		}
		/**
		 * Escapa caracteres NO válidos en xml como &, >, <, etc
		 */
		@GwtIncompatible("apache.commons.lang not supported by GWT")
		public XMLString escape() {
			String escapedXml = StringEscapeUtils.escapeXml10(_buffer.toString());
			_buffer = new StringBuilder(escapedXml);
			return this;
		}
		/**
		 * Returns the xml string as an object using an xml to objects marshaller 
		 * @param m
		 * @return
		 */
		@GwtIncompatible("marshaller not supported by GWT")
		public <T> T asObject(final Marshaller m) {
			return m.beanFromXml(_buffer);
		}
		
	}
///////////////////////////////////////////////////////////////////////////////
// 	SPLITTING WRAPPER
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Wrapper sobre el Splitter de Guava
	 */
	public static class StringSplitter {
		private CharSequence _str;
		private Splitter _splitter;

		public StringSplitter(final Splitter splitter,final CharSequence str) {
			_str = str;
			_splitter = splitter;
		}
		/**
		 * Busca un elemento en el splitter y devuelve su índice en caso de existir
		 * @param element el elemento a buscar
		 * @return el índice
		 */
		public int indexOf(final String element) {
			int outIndex = -1;
			Iterable<String> it = this.split();
			if (it != null) {
				int i = 0;
				for (String s : it) {
					if (s.equals(element)) {
						outIndex = i;
						break;
					}
					i++;
				}
			}
			return outIndex;
		}
		/**
		 * Ejecuta el troceado
		 * @return un iterador sobre las porciones
		 */
		public Iterable<String> split() {
			if (_str == null) return null;
			return _splitter.split(_str);
		}
		/**
		 * @return el iterador en forma de array
		 */
		public String[] toArray() {
			Iterable<String> it = this.split();
			// return Iterables.toArray(it,String.class);	// cannot be used since Iterables.toArray(it,String.class) is not supported by gwt
			Collection<String> out = Lists.newArrayList(it);
			return out.toArray(new String[out.size()]);
		}
		/**
		 * @return the iterator as a {@link Collection}
		 */
		public Collection<String> toCollection() {
			return Lists.newArrayList(this.toArray());
		}
		/**
		 * Devuelve uno de los elementos de la cadena partida
		 * @param groupNum numero de elemento
		 * @return el elemento
		 */
		public StringExtended group(final int groupNum) {
			String outGroup = null;
			Iterable<String> ib = this.split();
			if (ib != null && groupNum >= 0) {
				int i = 1;
				Iterator<String> it = ib.iterator();
				if (it.hasNext()) {
					do {
						outGroup = it.next();
						i++;
					} while(i <= groupNum && it.hasNext());
				}
			}
			return new StringExtended(outGroup);
		}
		/**
		 * Hace que el splitter deje de trocear tras N iteraciones
		 * @param limit el limite de iteraciones
		 */
		public StringSplitter limit(final int limit) {
			_splitter = _splitter.limit(limit);
			return this;
		}
		/**
		 * Hace que el splitter NO considere las cadenas vacias
		 */
		public StringSplitter omitEmptyStrings() {
			_splitter = _splitter.omitEmptyStrings();
			return this;
		}
		/**
		 * Hace que cuando se trocee la cadena, se haga un trim en cada procion
		 */
		public StringSplitter trimResults() {
			_splitter = _splitter.trimResults();
			return this;
		}
		/**
		 * Hace que cuando se trocee la cadena, se haga un trim en cada porcion utilizando el matcher
		 * @param trimmer
		 */
		public StringSplitter trimResults(final CharMatcher trimmer) {
			_splitter = _splitter.trimResults(trimmer);
			return this;
		}
	}
///////////////////////////////////////////////////////////////////////////////
// 	STATIC UTIL METHODS
///////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns as an {@link Integer}
	 * @param str
	 */
	public static int asInteger(final CharSequence str) {
		return Integer.valueOf(str.toString());
	}
	/** 
	 * Returns as a {@link Long}
	 * @param str
	 */
	public static long asLong(final CharSequence str) {
		return Long.valueOf(str.toString());
	}
	/**
	 * Returns as a {@link Double}
	 * @param str
	 */
	public static double asDouble(final CharSequence str) {
		return Double.valueOf(str.toString());
	}
	/**
	 * Returns as a {@link Float}
	 * @param str
	 */
	public static Float asFloat(final CharSequence str) {
		if (str == null) return null;
		return Float.valueOf(str.toString());
	}
	/**
	 * Returns as a byte array encoded on the system's default charset
	 * @param str
	 */
	@GwtIncompatible("Charset.defaultCharset() is NOT supported by GWT")
	public static byte[] getBytes(final CharSequence str) {
		if (str == null) return null;
		return str.toString().getBytes(Charset.defaultCharset());
	}
	/**
	 * Returns as a byte array encoded on the provided charset
	 * @param str
	 * @param charset the encoding
	 */
	@GwtIncompatible("String.getBytes(charset) is NOT suppported by GWT")
	public static byte[] getBytes(final CharSequence str,
								  final Charset charset) {
		if (str == null) return null;
		return str.toString().getBytes(charset);
	}
	/**
	 * Returns as a {@link StringBuilder}
	 * @param str
	 */
	public static StringBuilder asStringBuilder(final CharSequence str) {
		if (str == null) return null;
		return new StringBuilder(str);
	}
	/**
	 * Returns as a {@link StringBuffer}
	 * @param str
	 */
	public static StringBuffer asStringBuffer(final CharSequence str) {
		if (str == null) return null;
		return new StringBuffer(str);
	}
	/**
	 * Returns as a char array
	 * @param str
	 */
	public static char[] asCharArray(final CharSequence str) {
		if (str == null) return null;
		return str.toString().toCharArray();
	}
	/**
	 * Returns an {@link InputStream} to the buffer, that's the {@link String} as an {@link InputStream}
	 * (the stream's byte charset is the System's default charset)
	 * @param str
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public static InputStream asInputStream(final CharSequence str) {
		return Strings.asInputStream(str,
									 Charset.defaultCharset());
	}
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
	 * Returns a {@link Reader} to the buffer, that's the {@link String} as a {@link Reader}
	 * @param str
	 */
	@GwtIncompatible("IO is NOT supported by GWT")
	public static Reader asReader(final CharSequence str) {
		if (str == null) return null;
		return new StringReader(str.toString());
	}
	/**
	 * Returns an array of {@link String}s with each line of the original {@link String}
	 * @param str
	 */
	public static String[] getLines(final CharSequence str) {
		if (str == null) return null;
		String[] outLines = str.toString().split("\\r?\\n");
		return outLines;
	}
	/**
	 * Customizes a {@link String} containing placeholders like {} for provided vars
	 * ie:
	 * <pre class='brush:java'>
	 * 		Strings.of("Hello {} today is {}","Alex","Saturday"}
	 * </pre>
	 * @param strToCustomize the {@link String} to be customized
	 * @param vars the placeholder's values
	 * @return an {@link String} composed from the strToCustomize param replacing the placeholders with the provided values
	 */
	public static String customized(final CharSequence strToCustomize,final Object... vars) {
		StringBuffer outCustomized = Strings.customize(strToCustomize,vars);
		return outCustomized != null ? outCustomized.toString() : null;
	}
	/**
	 * Customizes a {@link String} containing placeholders like {} for provided vars
	 * ie:
	 * <pre class='brush:java'>
	 * 		Strings.of("Hello {} today is {}","Alex","Saturday"}
	 * </pre>
	 * @param strToCustomize the {@link String} to be customized
	 * @param vars the placeholder's values
	 * @return an {@link StringBuffer} composed from the strToCustomize param replacing the placeholders with the provided values
	 */
	public static StringBuffer customize(final CharSequence strToCustomize,final Object... vars) {
		if (strToCustomize == null) return null;
		if (vars == null || vars.length == 0) return new StringBuffer(strToCustomize);
		// see MessageFormatter from SL4FJ
		// custom impl
		String workStr = strToCustomize.toString();
		for (Object var : vars) {
			workStr = workStr.replaceFirst("\\{\\}",(var != null ? _matcherQuoteReplacement(var.toString())	// should be _objectToString(var) but it's problematic in GWT 
																 : "null"));	
		}
		return new StringBuffer(workStr);
	}
    /**
     * Copy of {@link Matcher#quoteReplacement(String)} to make it possible to use
     * this with GWT
     * @param s
     * @return
     * @see Matcher#quoteReplacement(String)
     */
	private static String _matcherQuoteReplacement(String s) {
        if ((s.indexOf('\\') == -1) && (s.indexOf('$') == -1)) return s;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' || c == '$') sb.append('\\');
            sb.append(c);
        }
        return sb.toString();
    }
/*
	private static String _objectToString(final Object object) {
		if (object == null) return null;
		String outStr = null;
		if (CollectionUtils.isArray(object.getClass())) {
			outStr = _objectArrayToString((Object[])object);
		} else if (CollectionUtils.isCollection(object.getClass())) {
			outStr = _objectCollectionToString((Collection<?>)object);
		} else if (CollectionUtils.isMap(object.getClass())) {
			outStr = _objectCollectionToString((Map<?,?>)object);
		} else {
			outStr = object.toString();
		}
		return outStr;
	}
	private static String _objectArrayToString(final Object[] objects) {
		StringBuffer outStr = new StringBuffer();
		outStr.append("[");
		for (int i=0; i<objects.length; i++) {
			outStr.append(objects[i]);
			if (i < objects.length-1) outStr.append(",");
		}
		outStr.append("]");
		return outStr.toString();
	}
	private static String _objectCollectionToString(final Collection<?> objects) {
		String outStr = null;
		if (CollectionUtils.hasData(objects)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Iterator<?> it = objects.iterator(); it.hasNext(); ) {
				Object o = it.next();
				sb.append(o != null ? o.toString() : "null");
				if (it.hasNext()) sb.append(",");
			}
			sb.append("]");
			outStr = sb.toString();
		}
		return outStr;
	}
	private static String _objectCollectionToString(final Map<?,?> objects) {
		String outStr = null;
		if (CollectionUtils.hasData(objects)) {
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			for (Iterator<?> it = objects.entrySet().iterator(); it.hasNext(); ) {
				Map.Entry<?,?> me = (Map.Entry<?,?>)it.next();
				sb.append(me.getKey())
				  .append(": ")
				  .append(me.getValue() != null ? me.getValue() : "null");
				if (it.hasNext()) sb.append(",");
			}
			sb.append("]");
			outStr = sb.toString();
		}
		return outStr;
	}
*/
    /**
     * <pre>
     * Método para validar si una cadena es nula o vacía, no se consideran espacios en blanco.
     * Este método debe ser utilizado en lugar de <code>sb.toString().trim().length()</code>,
     * debido a que es un modo ineficaz de comprobar si una cadena es vacía realmente,
     * ya que el código <code>String.trim().length()</code> crea internamente un objeto String
     * nuevo para comprobar su tamaño.
     * </pre>
     * @param str StringBuffer a validar
     * @return true si es un StringBuffer vacio o null
     */
    public static boolean isNullOrEmpty(final CharSequence str) {
        if (str == null || str.length() == 0) return true;        // true si null
        return CharMatcher.WHITESPACE.matchesAllOf(str);
        /*
        // GWT does NOT supports Character.isWhitespace method
        boolean isEmpty = true;
        for (int i=0;i<str.length();i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                isEmpty = false;
                break;
            }
        }
        return isEmpty;
        */
    }
    /**
     * <pre>
     * Método para validar si una cadena es nula o vacía, no se consideran espacios en blanco.
     * Este método debe ser utilizado en lugar de <code>sb.toString().trim().length()</code>,
     * debido a que es un modo ineficaz de comprobar si una cadena es vacía realmente,
     * ya que el código <code>String.trim().length()</code> crea internamente un objeto String
     * nuevo para comprobar su tamaño.
     * </pre>
     * @param str StringBuffer a validar
     * @return true si es un StringBuffer vacio o null
     */
    public static boolean isNullOrEmpty(final char[] chars) {
    	return Strings.isNullOrEmpty(new String(chars));
    }
    /**
     * The reverse method of {@link Strings}{@link #isNullOrEmpty(CharSequence)}
     * @param str the {@link CharSequence}
     * @return true is the CharSequence is NOT null or empty
     */
    public static boolean isNOTNullOrEmpty(final CharSequence str) {
    	return !Strings.isNullOrEmpty(str);
    }
    /**
     * The reverse method of {@link Strings}{@link #isNullOrEmpty(char[])}
     * @param str the char array
     * @return true is the char array NOT null or empty
     */
    public static boolean isNOTNullOrEmpty(final char[] chars) {
    	return !Strings.isNullOrEmpty(chars);
    }
	/**
	 * Concatena varias cadenas
	 * @param strs las cadenas a concatenar
	 * @return las cadenas concatenadas
	 */
	public static String concat(final CharSequence... strs) {
		if (strs == null || strs.length == 0) return null;
		StringBuilder sb = new StringBuilder();
		for (CharSequence currSeq : strs) sb.append(currSeq);
		return sb.toString();
	}
	/**
	 * Concatena varias cadenas
	 * @param strs cadenas a concatenar
	 * @return las cadenas concatenadas
	 */
	public static String concat(final String... strs) {
		if (strs == null || strs.length == 0) return null;
		StringBuilder sb = new StringBuilder();
		for (String s : strs) sb.append(s);
		return sb.toString();
	}
	/**
	 * Quotes the string (surrounds it with single quotes)
	 * @param s la cadena a entrecomillar
	 * @return la cadena entrecomillada
	 */
	public static String quote(final String s) {
		return s != null ? concat("'",s,"'") : null;
	}
	/**
	 * Double quoutes the string (surrounds it with double quotes)
	 * @param s
	 * @return
	 */
	public static String doubleQuoute(final String s) {
		return s != null ? concat("\"",s,"\"") : null;
	}
	/**
	 * Returns the value if the first parameter is not null or the default value if it is
	 * @param s the value
	 * @param defaultValue the defaultValue
	 * @return the value or the default value
	 */
	public static String valueOrDefault(final String s,
										final String defaultValue) {
		return s != null ? s 
						 : defaultValue;
	}
}
