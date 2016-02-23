package r01f.xmlproperties;

import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Function;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.enums.EnumWithCode;
import r01f.enums.EnumWithCodeWrapper;
import r01f.enums.Enums;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.Environment;
import r01f.guids.CommonOIDs.Password;
import r01f.guids.CommonOIDs.UserCode;
import r01f.locale.Language;
import r01f.locale.Languages;
import r01f.marshalling.Marshaller;
import r01f.reflection.ReflectionUtils;
import r01f.resources.ResourcesLoader;
import r01f.resources.ResourcesLoaderDef;
import r01f.types.Path;
import r01f.types.TimeLapse;
import r01f.types.contact.EMail;
import r01f.types.url.Host;
import r01f.types.url.Url;
import r01f.types.url.UrlPath;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;
import r01f.xmlproperties.XMLProperties.OSType;
import r01f.xmlproperties.XMLPropertiesForApp.ComponentProperties;

/**
 * Wraps an xml property
 */
@Slf4j
@AllArgsConstructor
public final class XMLPropertyWrapper {
/////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
/////////////////////////////////////////////////////////////////////////////////////////	
	private ComponentProperties _props;
	private String _xPath;

/////////////////////////////////////////////////////////////////////////////////////////
//	RAW DOM'S NODE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * @return the xml node containing the property
	 */
	public Node node() {
		return _props.node(_xPath);
	}
	/**
 	 * @return the xml node list
	 */
	public NodeList nodeList() {
		return _props.nodeList(_xPath);
	}
	/**
	 * Checks if a property exists
	 * @return true if the property exists, false otherwise
	 */
	public boolean exist() {
		return _props.existProperty(_xPath);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  GENERIC GET
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property value trying to infer it's type
	 * @return the property
	 */
	public <T> T get() {
		return _props.<T>get(_xPath);
	}
	/**
	 * Gets a property value trying to infer it's type
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property
	 */
	public <T> T get(final T defaultValue) {
		return _props.<T>get(_xPath,defaultValue);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  OBJECT LIST
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Transforms the child nodes into a collection of objects
	 * @param transformFunction
	 * @return
	 */
	public <T> Collection<T> asObjectList(final Function<Node,T> transformFunction) {
		return _props.getObjectList(_xPath,
									transformFunction);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  STRING 
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link String}
	 * @return 
	 */
	public String asString() {
		return this.asString(null);
	}
	/**
	 * Gets a property as a {@link String}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public String asString(final String defaultVal) {
		return _props.getString(_xPath,defaultVal);
	}
	/**
	 * Gets a property as a {@link StringExtended}
	 * @return 
	 */
	public StringExtended asStringWrapped() {
		return this.asStringWrapped(null);
	}
	/**
	 * Gets a property as a {@link StringExtended}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public StringExtended asStringWrapped(final String defaultVal) {
		return _props.getStringWrapped(_xPath,defaultVal);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  NUMBERS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link Number}
	 * @return 
	 */
	public Number asNumber() {
		return this.asNumber(null);
	}
	/**
	 * Gets a property as a {@link Number}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Number asNumber(final Number defaultVal) {
		return _props.getNumber(_xPath,defaultVal);
	}
	/**
	 * Gets a property as an {@link Integer}
	 * @return 
	 */
	public int asInteger() {
		return _props.getInteger(_xPath);
	}
	/**
	 * Gets a property as an {@link Integer}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public int asInteger(final int defaultVal) {
		return _props.getInteger(_xPath,defaultVal);
	}
	/**
	 * Gets a property as a {@link Long}
	 * @return 
	 */
	public long asLong() {
		return _props.getLong(_xPath);
	}
	/**
	 * Gets a property as a {@link Long}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public long asLong(final long defaultVal) {
		return _props.getLong(_xPath,defaultVal);
	}
	/**
	 * Gets a property as a {@link Double}
	 * @return 
	 */
	public double asDouble() {
		return _props.getDouble(_xPath);
	}
	/**
	 * Gets a property as a {@link Double}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public double asDouble(final double defaultVal) {
		return _props.getDouble(_xPath,defaultVal);
	}
	/**
	 * Gets a property as a {@link Float}
	 * @return 
	 */
	public float asFloat() {
		return _props.getFloat(_xPath);
	}
	/**
	 * Gets a property as a {@link Float}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public float asFloat(final float defaultVal) {
		return _props.getFloat(_xPath,defaultVal);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  BOOLEAN
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link Boolean}
	 * @return 
	 */
	public boolean asBoolean() {
		return _props.getBoolean(_xPath);
	}
	/**
	 * Gets a property as a {@link Boolean}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public boolean asBoolean(boolean defaultVal) {
		return _props.getBoolean(_xPath,defaultVal);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PATH
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link PaUrlPathth}
	 * @return
	 */
	public UrlPath asUrlPath() {
		String path = _props.getString(_xPath);
		return path != null ? new UrlPath(_replacePathVars(path))
							: null;
	}
	/**
	 * Gets a property as a {@link UrlPath}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public UrlPath asUrlPath(final String defaultVal) {
		UrlPath defPath = UrlPath.from(_replacePathVars(defaultVal));
		return this.asUrlPath(defPath);
	}
	/**
	 * Gets a property as a {@link UrlPath}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public UrlPath asUrlPath(final UrlPath defaultVal) {
		UrlPath outPath = this.asUrlPath();
		return outPath != null ? outPath
							   : new UrlPath(_replacePathVars(defaultVal.asString()));
	}
	/**
	 * Gets a property as a {@link Path}
	 * @return
	 */
	public Path asPath() {
		String path = _props.getString(_xPath);
		return path != null ? new Path(_replacePathVars(path))
							: null;
	}
	/**
	 * Gets a property as a {@link Path}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Path asPath(final String defaultVal) {
		Path defPath = Path.from(_replacePathVars(defaultVal));
		return this.asPath(defPath);
	}
	/**
	 * Gets a property as a {@link Path}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Path asPath(final Path defaultVal) {
		Path outPath = this.asPath();
		return outPath != null ? outPath
							   : new Path(_replacePathVars(defaultVal.asString()));
	}
	private static String _replacePathVars(final String pathAsString) {
		if (Strings.isNullOrEmpty(pathAsString)) return null;
		
		String outPathAsString = pathAsString;
		OSType osType = XMLProperties.getOS();
		if (pathAsString.startsWith("%ROOT%")) {
			if (osType == OSType.Linux || osType == OSType.NIX) {
				outPathAsString = pathAsString.replace("%ROOT%","/");
			} else if (osType == OSType.WINDOWS) {
				outPathAsString = pathAsString.replace("%ROOT%","d:/");
			}
		}
		return outPathAsString;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  LANGUAGE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The property value as an {@link Language} object
	 * @return
	 */
	public Language asLanguageFromCode() {
		String langCode = this.asString();
		return Strings.isNOTNullOrEmpty(langCode) ? Languages.fromCountryCode(langCode) : null;
	}
	/**
	 * The property value as an {@link Language} object or the default value if the
	 * property is not found
	 * @param defaultVal 
	 * @return 
	 */
	public Language asLanguageFromCode(final Language defaultVal) {
		Language outLang = this.asLanguageFromCode();
		return outLang != null ? outLang
							   : defaultVal;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ENVIRONMENT & APPCODE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The property value as an {@link Environment} object
	 * @return
	 */
	public Environment asEnvironment() {
		String env = this.asString();
		return Strings.isNOTNullOrEmpty(env) ? Environment.forId(env) : null;
	}
	/**
	 * The property value as an {@link Environment} object or the default value if the
	 * property is not found
	 * @param defaultVal 
	 * @return 
	 */
	public Environment asEnvironment(final Environment defaultVal) {
		Environment outEnv = this.asEnvironment();
		return outEnv != null ? outEnv
							  : defaultVal;
	}
	/**
	 * The property value as an {@link Environment} object or the default value if the
	 * property is not found
	 * @param defaultVal 
	 * @return 
	 */
	public Environment asEnvironment(final String defaultVal) {
		Environment outEnv = this.asEnvironment();
		return outEnv != null ? outEnv
							  : Environment.forId(defaultVal);
	}
	/**
	 * The property value as an {@link AppCode} object
	 * @return
	 */
	public AppCode asAppCode() {
		String appCode = this.asString();
		return Strings.isNOTNullOrEmpty(appCode) ? AppCode.forId(appCode) : null;
	}
	/**
	 * The property value as an {@link AppCode} object or the default value if the
	 * property is not found
	 * @param defaultVal 
	 * @return 
	 */
	public AppCode asAppCode(final AppCode defaultVal) {
		String outAppCode = _props.getString(_xPath);
		return outAppCode != null ? AppCode.forId(outAppCode)
								  : defaultVal;
	}
	/**
	 * The property value as an {@link AppCode} object or the default value if the
	 * property is not found
	 * @param defaultVal 
	 * @return 
	 */
	public AppCode asAppCode(final String defaultVal) {
		return this.asAppCode(AppCode.forId(defaultVal));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PASSWORD
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link UserCode}
	 * @return 
	 */
	public UserCode asUserCode() {
		String outUserCode = _props.getString(_xPath);
		return outUserCode != null ? UserCode.forId(outUserCode) : null;
	}
	/**
	 * Gets a property as a {@link UserCode}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public UserCode asUserCode(final UserCode defaultVal) {
		String outUserCode = _props.getString(_xPath);
		return outUserCode != null ? UserCode.forId(outUserCode)
								   : defaultVal;
	}
	/**
	 * Gets a property as a {@link UserCode}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public UserCode asUserCode(final String defaultVal) {
		return this.asUserCode(UserCode.forId(defaultVal));
	}
	/**
	 * Gets a property as a {@link Password}
	 * @param defaultVal
	 * @return the property value or the default value if the property is NOT found
	 */
	public Password asPassword() {
		String outPwd = _props.getString(_xPath);
		return outPwd != null ? Password.forId(outPwd) : null;
	}
	/**
	 * Gets a property as a {@link Password}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Password asPassword(final Password defaultVal) {
		String outPwd = _props.getString(_xPath);
		return outPwd != null ? Password.forId(outPwd)
							  : defaultVal;
	}
	/**
	 * Gets a property as a {@link Password}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Password asPassword(final String defaultVal) {
		return this.asPassword(Password.forId(defaultVal));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  EMAIL
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The property value as an {@link EMail}
	 * @return
	 */
	public EMail asEMail() {
		String email = this.asString();
		return Strings.isNOTNullOrEmpty(email) ? EMail.create(email) : null;
	}
	/**
	 * The property value as an {@link EMail}
	 * @param defaultEMail
	 * @return
	 */
	public EMail asEMail(final String defaultEMail) {
		return EMail.create(this.asString(defaultEMail));
	}
	/**
	 * The property value as an {@link EMail}
	 * @param defaultEMail
	 * @return the property value or the default value if the property is NOT found
	 */
	public EMail asEMail(final EMail defaultEMail) {
		String email = this.asString();
		return Strings.isNOTNullOrEmpty(email) ? EMail.create(email)
											   : defaultEMail;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  TIMELAPSE
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * The property value as an {@link TimeLapse}
	 * @return
	 */
	public TimeLapse asTimeLapse() {
		String timeLapse = this.asString();
		return Strings.isNOTNullOrEmpty(timeLapse) ? TimeLapse.createFor(timeLapse) : null;
	}
	/**
	 * The property value as an {@link TimeLapse}
	 * @param defaultTimeLapse
	 * @return
	 */
	public TimeLapse asTimeLapse(final String defaultTimeLapse) {
		return TimeLapse.createFor(this.asString(defaultTimeLapse));
	}
	/**
	 * The property value as an {@link TimeLapse}
	 * @param defaultTimeLapse
	 * @return the property value or the default value if the property is NOT found
	 */
	public TimeLapse asTimeLapse(final TimeLapse defaultTimeLapse) {
		String timeLapse = this.asString();
		return Strings.isNOTNullOrEmpty(timeLapse) ? TimeLapse.createFor(timeLapse)
											   	   : defaultTimeLapse;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  HOST & URL
/////////////////////////////////////////////////////////////////////////////////////////	
	/**
	 * Gets the property as a {@link Host}
	 * @return
	 */
	public Host asHost() {
		String host = this.asString();
		return Host.of(host);
	}
	/**
	 * Gets the property as a {@link Host}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Host asHost(final Host defaultVal) {
		String host = this.asString();
		Host outHost = null;
		if (Strings.isNullOrEmpty(host)) {
			outHost = defaultVal;
		} else {
			outHost = Host.of(host);
		}
		return outHost;
	}
	/**
	 * Gets the property as a {@link Url}
	 * @return
	 */
	public Url asUrl() {
		String url = this.asString();
		return Url.from(url);
	}
	/**
	 * Gets the property as a {@link Url}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Url asUrl(final Url defaultVal) {
		String url = this.asString();
		Url outUrl = null;
		if (Strings.isNullOrEmpty(url)) {
			outUrl = defaultVal;
		} else {
			outUrl = Url.from(url);
		}
		return outUrl;
	}
	/**
	 * Gets the property as a {@link Url}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Url asUrl(final String defaultVal) {
		return this.asUrl(Url.from(defaultVal));
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  PROPERTIES & LIST OF STRINGS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link Properties}
	 * @return 
	 */
	public Properties asProperties() {
		return _props.getProperties(_xPath);
	}
	/**
	 * Gets a property as a {@link Properties}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Properties asProperties(final Properties defaultVal) {
		return _props.getProperties(_xPath,defaultVal);
	}
	/**
	 * Gets a property as a {@link List} of {@link String}s
	 * @return 
	 */
	public List<String> asListOfStrings() {
		return _props.getListOfStrings(_xPath);
	}
	/**
	 * Gets a property as a {@link List} of {@link String}s
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public List<String> asListOfStrings(final List<String> defaultVal) {
		return _props.getListOfStrings(_xPath,defaultVal);
	}
	/**
	 * Gets a property as a {@link List} of {@link String}s
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public List<String> asListOfStrings(final String... defaultStrings) {
		List<String> defaultVal = defaultStrings != null ? Arrays.asList(defaultStrings) : null;
		return _props.getListOfStrings(_xPath,defaultVal);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  CHARSET
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link Charset}
	 * @param defaultVal the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Charset asCharset(final Charset defaultVal) {
		String outCharset = _props.getString(_xPath);
		return outCharset != null ? Charset.forName(outCharset)
								  : defaultVal;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  JAVA CLASS TYPES
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a {@link Class}
	 * @return
	 */
	public Class<?> asType() {
		Class<?> outType = null;
		String typeName = this.asString();
		if (!Strings.isNullOrEmpty(typeName)) {
			outType = ReflectionUtils.typeFromClassName(typeName);
		}
		return outType;
	}
	/**
	 * Gets a property as a {@link Class}
	 * @param defaultType the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public Class<?> asType(final Class<?> defaultType) {
		Class<?> outType = this.asType();
		return outType != null ? outType : defaultType;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  COMPLEX OBJECTS
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Gets a property as a model object transforming from the object's XML to model object using the provided {@link Marshaller}
	 * @param objType the returned object's type
	 * @param marshaller the {@link Marshaller} used to transform the XML to a Java object
	 * @return the object
	 */
	public <T> T asObject(final Class<T> objType,
						  final Marshaller marshaller) {
		return _props.getObject(_xPath,objType,marshaller);
	}
	/**
	 * Returns the property as an object transforming the node to an object
	 * using a {@link Function} 
	 * @param transformFuncion
	 * @return
	 */
	public <T> T asObject(final Function<Node,T> transformFuncion) {
		return _props.getObject(_xPath,transformFuncion);
	}
	/**
	 * Returns the property as an object build from the property string value
	 * To do so, the object type MUST implement an static valueOf(String) method
	 * or an static fromString(String) method
	 * @param objType
	 * @return
	 */
	public <T> T asObjectFromString(final Class<T> objType) {
		String valueAsString = this.asString();
		if (valueAsString == null) valueAsString = "no_property_configured_at_" + _xPath;
		return ReflectionUtils.<T>createInstanceFromString(objType, 
														valueAsString);
	}
	/**
	 * Returns the property as an object build from the property string value
	 * To do so, the object type MUST implement an static valueOf(String) method
	 * or an static fromString(String) method
	 * @param objType
	 * @param defaultValue the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public <T> T asObjectFromString(final Class<T> objType,
									final T defaultValue) {
		String valueAsString = this.asString();
		if (valueAsString == null) return defaultValue;
		return ReflectionUtils.<T>createInstanceFromString(objType, 
														valueAsString);
	}
	/**
	 * Returns the property as an object build from the property string value
	 * To do so, the object type MUST implement an static valueOf(String) method
	 * or an static fromString(String) method
	 * @param objType
	 * @param defaultValue the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public <T> T asObjectFromString(final Class<T> objType,
									final String defaultValue) {
		String valueAsString = this.asString();
		if (valueAsString == null) valueAsString = defaultValue;
		return ReflectionUtils.<T>createInstanceFromString(objType, 
														valueAsString);
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  RESOURCES LOADER DEFINITION
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the property as a {@link ResourcesLoader} definition object (a {@link ResourcesLoaderDef} object)
	 * (obviously the definition xml MUST have the {@link ResourcesLoaderDef} xml structure).
	 * @return
	 * @see ResourcesLoaderDef
	 */
	public ResourcesLoaderDef asResourcesLoaderDef() {
		return _props.getResourcesLoaderDef(_xPath);
	}
	/**
	 * Returns the property as a {@link ResourcesLoader} definition object (a {@link ResourcesLoaderDef} object)
	 * (obviously the definition xml MUST have the {@link ResourcesLoaderDef} xml structure).
	 * @param defLoaderDef
	 * @return the property value or the default value if the property is NOT found
	 * @see ResourcesLoaderDef
	 */
	public ResourcesLoaderDef asResourcesLoaderDef(final ResourcesLoaderDef defLoaderDef) {
		ResourcesLoaderDef outDef = _props.getResourcesLoaderDef(_xPath);
		return outDef != null ? outDef : defLoaderDef;
	}
/////////////////////////////////////////////////////////////////////////////////////////
//  ENUM
/////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Returns the property as an {@link Enum} element
	 * @param enumType the {@link Enum} type
	 * @return  
	 */
	public <E extends Enum<E>> E asEnumElement(final Class<E> enumType) {
		return this.asEnumElement(enumType,null);
	}
	/**
	 * Returns the property as an {@link Enum} element
	 * @param enumType the {@link Enum} type
	 * @param devaultVelue the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found 
	 */
	public <E extends Enum<E>> E asEnumElement(final Class<E> enumType,
											   final E defaultValue) {
		String enumAsStr = this.asString();
		E outE = defaultValue;
		if (!Strings.isNullOrEmpty(enumAsStr)) {
			try {
				outE = Enum.valueOf(enumType,enumAsStr);
			} catch (IllegalArgumentException illArgEx) {
				outE = defaultValue;	// No hay un valor para la propiedad
			}
		}
		return outE;
	}
	/**
	 * Returns the property as an {@link Enum} element
	 * @param enumType the {@link Enum} type
	 * @return  
	 */
	public <E extends Enum<E>> E asEnumElementIgnoringCase(final Class<E> enumType) {
		return this.asEnumElementIgnoringCase(enumType,
											  null);
	}
	/**
	 * Returns the property as an {@link Enum} element
	 * @param enumType the {@link Enum} type
	 * @param defaultValue the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found 
	 */
	public <E extends Enum<E>> E asEnumElementIgnoringCase(final Class<E> enumType,
														   final E defaultValue) {
		String enumAsStr = this.asString();
		E outE = defaultValue;
		if (!Strings.isNullOrEmpty(enumAsStr)) {
			try {
				outE = Enums.of(enumType)
						   	.fromNameIgnoringCase(enumAsStr);
			} catch (IllegalArgumentException illArgEx) {
				outE = defaultValue;	// No hay un valor para la propiedad
			}
		}
		return outE;
	}
	/**
	 * Returns the property as an enum element using the xml value as the enum code
	 * (the enum MUST implement EnumWithCode<String,E>
	 * @param enumType
	 * @return
	 */
	public <E extends Enum<E> & EnumWithCode<String,E>> E asEnumFromCode(final Class<E> enumType) {
		return this.asEnumFromCode(enumType,
								   null);
	}
	/**
	 * Returns the property as an enum element using the xml value as the enum code
	 * (the enum MUST implement EnumWithCode<String,E>
	 * @param enumType
	 * @param defaultValue the default value if the property is not defined at the xml properties file
	 * @return the property value or the default value if the property is NOT found
	 */
	public <E extends Enum<E> & EnumWithCode<String,E>> E asEnumFromCode(final Class<E> enumType,
											    		 				 final E defaultValue) {
		String enumAsStr = this.asString();
		E outE = defaultValue;
		if (!Strings.isNullOrEmpty(enumAsStr)) {
			outE = EnumWithCodeWrapper.create(enumType)
									  .fromCode(enumAsStr);			
			if (outE == null) {
				log.error("{} is not a valid {} element code",enumAsStr,enumType);
				outE = defaultValue;
			}
		}
		return outE;
	}
}
