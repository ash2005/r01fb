package r01f.guid;

import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.AppCode;
import r01f.guids.CommonOIDs.AppComponent;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForAppComponent;

/**
 * Definition of a GUID
 * Normally, this definition is loaded by means of a properties component called
 * [appCode].guids.xml.
 * This XML definition has to be like:
 *   <guidGenerator>
 *       <sequence name='testGUIDDispenser'>
 *           <uniqueId>desa</uniqueId>
 *           <lenght>36</length>
 *           <factoryBindingId></factoryBindingId>
 *           <properties>
 *               <!-- Any property needed by the guid generator type -->
 *               <highKeyBytes>9</highKeyBytes>              
 *               <lowKeyBytes>9</lowKeyBytes>
 *               <persistenceClass>com.ejie.r01f.guids.MemoryGUIDPersist</persistenceClass>                                                              
 *           </properties>
 *       </sequence>     
 *   </guidGenerator>
 */
@Slf4j
@Accessors(prefix = "_")
public class GUIDDispenserDef 
  implements Debuggable {
/////////////////////////////////////////////////////////////////////////////////////////
//  CONSTANTS
/////////////////////////////////////////////////////////////////////////////////////////
	public static int GUID_DEFAULT_LENGTH = 36;
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
    @Getter private final AppCode _appCode;      		// AppCode
    @Getter private final String _sequenceName;   		// Sequence name
    @Getter private final String _uniqueID;       		// Unique id (is appended to the generated guids)
    @Getter private final int _length;					// GUID size
    @Getter private final String _factoryBindingId; 	// Id of the GUID generator factory (is injected  by GUICE in the GUIDDispenserManager)
    @Getter private final Properties _properties; 		// Dispenser properties

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & FACTORY
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor
     * @param newAppCode app code
     * @param newLength GUIDs size 
     */
    public GUIDDispenserDef(final AppCode newAppCode) {
    	this(newAppCode,"default","",
    		 36,
    		 "simpleGUIDDispenser",
    		 null);	// no properties	
    }
    /**
     * Constructor
     * @param newAppCode app code
     * @param newSequenceName name of the sequence
     * @param newUniqueID some unique id
     * @param newLength GUIDs size 
     * @param newProps GUIDDispenser properties
     */
    public GUIDDispenserDef(final AppCode newAppCode,final String newSequenceName,final String newUniqueID,
    						final int newLength,
    						final Properties newProps) {
    	this(newAppCode,newSequenceName,newUniqueID,
    		 newLength,
    		 "simpleGUIDDispenser",
    		 newProps);	
    }
    /**
     * Constructor
     * @param newAppCode app code
     * @param newSequenceName name of the sequence
     * @param newUniqueID some unique id
     * @param newLength GUIDs size 
     * @param newFactoryBindingId GUID Generator factory class ID (it's injected by GUICE in the GUIDDispenserManager)
     * @param newProps GUIDDispenser properties
     */
    public GUIDDispenserDef(final AppCode newAppCode,final String newSequenceName,final String newUniqueID,
    						final int newLength,
    						final String newFactoryBindingId,
    						final Properties newProps) {
    	_appCode = newAppCode;
    	_sequenceName = newSequenceName;
    	_length = newLength;
    	_uniqueID = newUniqueID;
        _factoryBindingId = newFactoryBindingId;	// simpleGUIDDispenser by default
        _properties = newProps;
    }
    public GUIDDispenserDef(final XMLProperties props,
    						final AppCode appCode) {
    	this(props,
    		 appCode,"default");
    }
    public GUIDDispenserDef(final XMLPropertiesForAppComponent props) {
    	this(props,
    		 "default");
    }
    public GUIDDispenserDef(final XMLProperties props,
    						final AppCode appCode,final String sequenceName) {
    	this(props.forAppComponent(appCode,AppComponent.forId("guids")),
    							   sequenceName);
    }
    public GUIDDispenserDef(final XMLPropertiesForAppComponent props,
    					    final String sequenceName) {    	
    	_appCode = props.getAppCode();
    	
        log.trace("Loading the config for dispenser {} in app {}",sequenceName,_appCode);
        if (sequenceName == null) {
        	log.error("NO sequence name set: the GUIDDispenser config for appCode={} will be loaded searching for a sequence named='default'!!!",
        			  _appCode);
        }
    	
        final String xPathBase = "guidGenerator/sequence[@name='" + sequenceName + "']/";
        if (!props.propertyAt(xPathBase).exist()) {
        	log.error("There does NOT exists the path \"guidGenerator/sequence[@name='{}']/\" in the {}.guids.properties.xml file! GUIDDispenser will be configured with default values!!",
        			  sequenceName,_appCode);
        }
        
 		// - generated guids length
 		int length = props.propertyAt(xPathBase + "length")
 						  .asInteger(GUIDDispenserDef.GUID_DEFAULT_LENGTH);
 		
        // - Sequence id
        String uniqueID = props.propertyAt(xPathBase + "uniqueId")
        					   .asString("");
        if (uniqueID == null) log.warn("The property {}/uniqueId is NOT defined at the guids properties file for appCode={}. '0-unknown' is assummed",
            		 					xPathBase,sequenceName,_appCode);
        // - Type that generates the guids
        String factoryBindingId = props.propertyAt(xPathBase + "factoryBindingId")
        							   .asString("simpleGUIDDispenser");
        // - Guid generator type properties
        Properties properties = props.propertyAt(xPathBase + "properties")
        							 .asProperties();
        
        // - Build the definition
        _sequenceName = sequenceName;
        _length = length;
        _uniqueID = uniqueID;
        _factoryBindingId = factoryBindingId;
        _properties = properties;
    }
    /**
     * Constructor using other dispenser 
     * @param other another dispenser
     */
    public GUIDDispenserDef(final GUIDDispenserDef other) {
    	this(other.getAppCode(),other.getSequenceName(),other.getUniqueID(),
    		 other.getLength(),
    		 other.getFactoryBindingId(),
    		 other.getProperties());
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  PUBLIC METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Returns the property with the provided name
     * @param propName name of the property
     * @return property value
     */
    public String getProperty(final String propName) {
    	return _properties != null ? _properties.getProperty(propName) : null;
    }
    /**
     * Gets a prefix for the GUID from the appCode
     * @return the prefix
     */
    public String guidPrefix() {
        // Get a 4 characters prefix
        String outPrefix = null;
        if (_appCode == null) {
            outPrefix = "UNKN";
        } else if (_appCode.asString().length() > 4) {
            outPrefix = _appCode.asString().substring(0, 4);
        } else if (_appCode.asString().length() < 4) {
            outPrefix = Strings.of(_appCode.asString())
            				   .rightPad('0',4)
            				   .asString();
        } else {
            outPrefix = _appCode.asString();
        }
        if (_uniqueID != null) {
        	// Get the two first letters from the identifier: environment (loc=lc,sb_des=sd,sb_pru=sp,des=ds,pru=pr,pro=pd)
        	if (_uniqueID.length() >= 2) {
        		outPrefix = outPrefix + _uniqueID.charAt(0) + _uniqueID.charAt(1);
        	} else if (_uniqueID.length() == 1) {
        		outPrefix = outPrefix + _uniqueID.charAt(0);
        	} 
        }
        return outPrefix;
    }
    @Override
    public String debugInfo() {
        StringExtended sw = Strings.create(200)
        			  			  .addCustomized("\t     appCode: {}\n",_appCode)
        			  			  .addCustomized("\tsequenceName: {}\n",_sequenceName)
        			  			  .addCustomized("\t    uniqueId: {}\n",_uniqueID)
        			  			  .addCustomized("\t dispenserId: {}\n",_factoryBindingId);
        if (_properties != null) {
            for (Map.Entry<Object,Object> me : _properties.entrySet()) {
                sw.addCustomized("\t\t{}:{}\r\n",me.getKey().toString(),me.getValue().toString());
            }
        }
        return sw.asString();
    }

}
