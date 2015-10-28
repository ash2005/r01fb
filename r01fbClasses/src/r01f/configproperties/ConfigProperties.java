package r01f.configproperties;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.BooleanUtils;

import r01f.bundles.ResourceBundleControl;
import r01f.bundles.ResourceBundleMissingKeyBehaviour;
import r01f.bundles.ResourceBundleMissingKeyException;
import r01f.types.CanBeRepresentedAsString;
import r01f.types.Path;
import r01f.util.types.Dates;
import r01f.util.types.Numbers;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Provides access to configuration properties
 * This type is a simple alternative to the more powerfull Apache Commons Config (http://commons.apache.org/proper/commons-configuration/index.html) project
 * 
 * <h2>OPTIONS TO GET AN INSTANCE OF {@link ConfigProperties}</h2>
 * <pre>---------------------------------------------------------</pre>
 * 
 * <h3>
 * [OPTION 1]: When the instance where the {@link ConfigProperties} is to be used is injected by guice 
 * </h3>
 * <pre>
 * 		Ej: If a type MyType, needs to use property of a properties file called myProject.properties
 * 		    the type will have a {@link ConfigProperties} member that is going to be injected:
 * </pre>
 * <pre class='brush:java'>
 * 		public class MyType {
 * 			@Inject
 * 			private ConfigProperties _config;
 * 			...
 * 		}
 * </pre>
 * If GUICE is used to get an instance of MyType, GUICE will also inject the config properties. 
 * <pre>
 * 1.- Create a GUICE {@link com.google.inject.Module} including a {@link com.google.inject.Provider} for EVERY BUNDLE annotated with <code>@Provides</code>
 *     and <code>@Named("myBundle")</code> (or a custom annotation)</pre>
 * 	<pre class='brush:java'>
 *		public class TestConfigModule
 *   		  implements Module {
 *				@Override
 *				public void configure(Binder binder) {		
 *				}
 *				@Provides @Named("myProjectProperties")
 *				public ConfigProperties provideMyConfigProperties(final ConfigPropertiesFactory configPropertiesFactory) {
 *					// The ConfigPropertiesFactory instance is injected to the provider
 *					return configPropertiesFactory.forBundle("myProject.properties")
 *				  		 	 			 		  .loadedUsingDefinitionAt(AppCode.forId("r01fb"),
 *										  								  				 AppComponent.forId("test"),
 *										  								  				 Path.of("/properties/resourcesLoader[@id='myClassPathResourcesLoader']"));
 *				}
 *			}
 * 	</pre><pre>
 * 2.- In the type where the properties are going to be used, the properties must be injected using the annotation @Named("myProjectProperties") -or a custom annotation-
 * 	(the @Named identifier must be the same as the one in the {@link com.google.inject.Provider} of the step1</pre>
 * 	<pre class='brush:java'>
 * 		public class MyType {
 * 			@Inject @Named("myProjectProperties")
 *			private ConfigProperties _config;
 * 			...
 * 		}
 * 	</pre><pre>
 * 	If the type MyType is NOT created by GUICE so no injection in MyType will be done, the {@link ConfigProperties} can be created as:</pre>
 * 	<pre class='brush:java'>
 * 		public class MyType {
 * 			public void someMethod() {
 * 				...
 * 				ConfigProperties properties = Guice.createInjector(new BootstrapGuiceModule(),
 * 																   new TestConfigPropertiesModule())
 *  	 											   .getInstance(Key.get(ConfigProperties.class,
 *  	 											   						Names.named("myProjectProperties")));
 * 				}
 * 			}
 * 		}
 * 	</pre>
 * 
 * 
 * <h3>
 * [OPTION 2]: Create the {@link ConfigProperties} by hand (not using GUICE at all)
 * </h3>
 * <pre>
 * 		1.- Create a ResourceBundleControlFactory that provides ResourceBundleControl object instances
 * 		    The factory needs an instance of an XMLProperties file where the resources loading/reloading is defined</pre>
 * 			<pre class='brush:java'>
 * 			XMLProperties props = XMLProperties.create(); 	// Normally you would put this XMLProperties in an static instance since it maintains a cache of properties
 * 			ResourceBundleControlFactory resBundleControlFactory = ResourceBundleControlFactory.create(props);
 * 			</pre><pre>
 * 		2.- Create the ConfigPropertiesFactory</pre>
 * 			<pre class='brush:java'>
 * 			ConfigPropertiesFactory cfgPropsFactory = ConfigPropertiesFactory.create(resBundleControlFactory);
 * 			</pre><pre>
 * 		3.- Create the ConfigProperties</pre>
 * 			<pre class='brush:java'>
 *	    	ConfigProperties props = cfgPropsFactory.forBundle("properties/myProject")
 *	    											.loadedUsingDefinitionAt(AppCode.forId("r01fb"),
 *	    																     AppComponent.forId("test"),
 *	    																     Path.of("/properties/resourcesLoader[@id='myClassPathResourcesLoader']"));
 * 			</pre>
 * <pre> 
 * If the resources loading/reloading is NOT defined in an XMLProperties file, the ResourceBundleControlFactory can be
 * created as:
 * 		1.- Create a ResourceBundleControlFactory that provides ResourceBundleControl object instances</pre>
 * 			<pre class='brush:java'>
 * 			ResourceBundleControlFactory resBundleControlFactory = ResourceBundleControlFactory.create();
 * 			</pre><pre>
 * 		2.- Create the ConfigPropertiesFactory</pre>
 * 			<pre class='brush:java'>
 * 			ConfigPropertiesFactory cfgPropsFactory = ConfigPropertiesFactory.create(resBundleControlFactory);
 * 			</pre><pre>
 * 		3.- Create the ConfigProperties</pre>
 * 			<pre class='brush:java'>
 *	    	ConfigProperties props = cfgPropsFactory.forBundle("properties/myProject")
 *	    											.loadedUsingDefinition(ResourceLoaderDef.DEFAULT);	// use the default resources loading/reloading definition
 *																										// obviously a custom one could be used
 * 			</pre>
 */
@Slf4j
@Accessors(prefix="_")
@RequiredArgsConstructor
public class ConfigProperties {
/////////////////////////////////////////////////////////////////////////////////////////
//	STATUS
/////////////////////////////////////////////////////////////////////////////////////////
	private final String _bundleSpec;				// Bundle Name
	private final ResourceBundleControl _control;	// Bundle load & reload control
    private ResourceBundleMissingKeyBehaviour _missingKeyBehaviour = ResourceBundleMissingKeyBehaviour.THROW_EXCEPTION;
    private boolean _devMode;

/////////////////////////////////////////////////////////////////////////////////////////
//	FLUENT-API
/////////////////////////////////////////////////////////////////////////////////////////
    public ConfigProperties withMissingKeyBehaviour(final ResourceBundleMissingKeyBehaviour behaviour) {
    	_missingKeyBehaviour = behaviour;
    	return this;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	INTERFAZ I18NBundle
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns every key from the properties file
     * @return todas las claves
     */
    public List<String> keys() {
        List<String> outKeys = new LinkedList<String>();
        Enumeration<String> en = _retrievePropertiesResource(_bundleSpec).getKeys();
        while (en.hasMoreElements()) outKeys.add(en.nextElement());
        return outKeys;
    }
    /**
     * Checks if exists a property for a given key 
     * @param key the key
     * @return the property
     */
    public boolean hasKey(final String key) {
        boolean containsKey = _retrievePropertiesResource(_bundleSpec).containsKey(key);
        return containsKey;
    }
    /**
     * Returns a wrapper of the property that offers some usefull methods to get the value in different formats 
     * @param key the key
     * @return the wrapper
     */
    public ConfigPropertyWrapper get(final String key) {
    	String keyValue = _retrieveProperty(key);
    	return new ConfigPropertyWrapper(keyValue);
    }
    /**
     * Returns every property whose keys starts with a given prefix
     * IE: If the properties file contains;
     * 			my.one = One
     * 			my.two = Two
     * 			yours.one = Your One
     * 		<pre class='brush:java'>
     * 			propertiesWithKeysStartingWith("my")
     *		</pre>
     *	   these messages would be returned as a Map
     * 			my.one = One
     * 			my.two = Two
     * @param keyPrefix el prefijo
     * @return
     */
    public final Map<String,ConfigPropertyWrapper> propertiesWithKeysStartingWith(final String keyPrefix) {
        if (keyPrefix == null) throw new IllegalArgumentException("Cannot load bundle key: Missing key!");  
        Map<String,ConfigPropertyWrapper> outMessages = new HashMap<String,ConfigPropertyWrapper>();
        try {
	        // Load the resourceBundle and iterate all the keys to find the ones that starts with the given prefix
	    	ResourceBundle bundle = _retrievePropertiesResource(_bundleSpec);
    		Enumeration<String> keys = bundle.getKeys();
    		if (keys != null && keys.hasMoreElements()) {
    			do {
    				String key = keys.nextElement();
    				String keyValue = key.startsWith(keyPrefix) ? bundle.getString(key)
    													   		: null;
    				if (keyValue != null) outMessages.put(key,new ConfigPropertyWrapper(keyValue));
    			} while(keys.hasMoreElements());
    		}
        } catch (MissingResourceException mrEx) {
        	log.error("The properties file could not be retrieved {}",_bundleSpec,mrEx);
        }
        return outMessages;
    }
    @Override
    public final String toString() {
        return _bundleSpec;
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  WRAPPER
/////////////////////////////////////////////////////////////////////////////////////////
    @RequiredArgsConstructor
    public class ConfigPropertyWrapper
      implements CanBeRepresentedAsString,
      			 Serializable {
		private static final long serialVersionUID = -1099917385505949571L;
		
		private final String _propertyValue;
    	
    	@Override
    	public String toString() {
    		return this.asString();
    	}
    	@Override
    	public String asString() {
    		return _propertyValue;
    	}
    	public String asString(final String defaultVal) {
    		return _propertyValue != null ? _propertyValue 
    									  : defaultVal;
    	}
    	public String asStringCustomized(final Object... params) {
    		String outValue = null;
    		if (CollectionUtils.hasData(params)) {
    			outValue = MessageFormat.format(_propertyValue,params);
    		} else {
    			outValue = _propertyValue;
    		}
    		return outValue;
	    }
    	public boolean asBoolean() {
    		return BooleanUtils.toBoolean(false);
    	}
    	public boolean asBoolean(final boolean defaultValue) {
			String bolStr = _propertyValue;
	    	Boolean outBool = null;
	    	if (bolStr != null) {
		    	outBool = BooleanUtils.toBooleanObject(bolStr);
		    	if (outBool == null) {
		    		log.debug("Property {} cannot be converted to a boolean!",bolStr);
		    		outBool = defaultValue;
		    	}
	    	} else {
	    		outBool = defaultValue;
	    	}
	    	return outBool;
    	}
    	public long asLong() {
    		return Numbers.isLong(_propertyValue) ? Long.parseLong(_propertyValue)
    											  : 0;
    	}
    	public long asLong(final long defaultValue) {
    		return Numbers.isLong(_propertyValue) ? Long.parseLong(_propertyValue)
    											  : defaultValue;
    	}
    	public int asInteger() {
    		return Numbers.isNumber(_propertyValue) ? Integer.parseInt(_propertyValue)
    												: 0;
    	}
    	public int asInteger(final int defaultValue) {
    		return Numbers.isInteger(_propertyValue) ? Integer.parseInt(_propertyValue)
    											  	 : defaultValue;
    	}
    	public double asDouble() {
    		return Numbers.isDouble(_propertyValue) ? Double.parseDouble(_propertyValue)
    												: 0;
    	}
    	public double asDouble(final double defaultValue) {
    		return Numbers.isDouble(_propertyValue) ? Double.parseDouble(_propertyValue)
    											  	: defaultValue;
    	}
    	public float asFloat() {
    		return Numbers.isFloat(_propertyValue) ? Float.parseFloat(_propertyValue)
    												: 0;
    	}
    	public float asFloat(final float defaultValue) {
    		return Numbers.isFloat(_propertyValue) ? Float.parseFloat(_propertyValue)
    											  	: defaultValue;
    	}
    	public Date asDate(final String format) {
    		return _propertyValue != null ? Dates.fromFormatedString(_propertyValue,format)
    									  : null;
    	}
    	public Date asDate(final String format,Date defaultValue) {
    		Date outDate = this.asDate(format);
    		return outDate != null ? outDate
    							   : defaultValue;
    	}
    	public Path asPath() {
    		return _propertyValue != null ? Path.of(_propertyValue)
    									  : null;
    	}
    	public Path asPath(final Path defaultValue) {
    		return _propertyValue != null ? Path.of(_propertyValue)
    									  : defaultValue;
    	}
		public <E extends Enum<E>> E asEnumElement(final Class<E> enumType) {
			return this.asEnumElement(enumType,null);
		}
		public <E extends Enum<E>> E asEnumElement(final Class<E> enumType,
												   final E defaultValue) {
			String enumAsStr = this.asString();
			E outE = defaultValue;
			if (!Strings.isNullOrEmpty(enumAsStr)) {
				try {
					outE = Enum.valueOf(enumType,enumAsStr);
				} catch(IllegalArgumentException illArgEx) {
					outE = defaultValue;	// No hay un valor para la propiedad
				}
			}
			return outE;
		}
    }
/////////////////////////////////////////////////////////////////////////////////////////
//	METODOS PRIVADOS
/////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Gets the given key value 
     * @param key the key to search for
     * @return
     * @throws MissingConfigPropertyException
     */
    private final String _retrieveProperty(final String key) throws ResourceBundleMissingKeyException {
        if (key == null) throw new IllegalArgumentException("Cannot load bundle key: Missing key!");
    	String outKeyValue = null;
        try {
        	ResourceBundle bundle = _retrievePropertiesResource(_bundleSpec);
        	try {
        		outKeyValue = bundle.getString(key);
        	} catch(MissingResourceException mkEx) {
    			log.warn("the requested key {} could not be found int the bundle {}",
    					 key,_bundleSpec);
        	}
        } catch (MissingResourceException mrEx) {
        	outKeyValue = Strings.of("The config file {} could NOT be loaded: {}",_bundleSpec,mrEx.getMessage())
        						 .asString();
        	log.error(outKeyValue,mrEx);
        }
        if (Strings.isNullOrEmpty(outKeyValue)) {
            switch (_missingKeyBehaviour) {
                case RETURN_KEY: {
                    outKeyValue = "[" + key + "]";
                    break;
                }
                case RETURN_NULL:
                    outKeyValue = null;
                    break;
                case THROW_EXCEPTION:
                    throw new ResourceBundleMissingKeyException(key,_bundleSpec);
                default:
            }
        }
        return outKeyValue;
    }
    private ResourceBundle _retrievePropertiesResource(final String bundleName) {
        if (_devMode) ResourceBundle.clearCache();	// IMPORTANT!! 	the cache is deleted in DEBUG mode so the bundle is reloaded everytime it's requested
        											//		 		This is NOT efficient; do not leave this way in production
        // Load the Properties file
        // IMPORTANT!!
        //		- DO NOT CACHE the ResourceBoundle because ResourceBundle itself implements a cache; also if ResourceBundel is cached the hot reload function is deactivated
        //		- Use ResourceBundle.Control to customize the hot reload 
        //		- CACHEE the ResourceBundle.Control instance because it could maintain some status about the hot reload (ie: timeStamp of the las reload)
    	ResourceBundle outBundle = ResourceBundle.getBundle(bundleName,
    									   	 				_control);
        return outBundle;
    }
}
