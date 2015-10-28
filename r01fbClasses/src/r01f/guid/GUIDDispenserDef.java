package r01f.guid;

import java.util.Map;
import java.util.Properties;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import r01f.debug.Debuggable;
import r01f.guids.CommonOIDs.AppCode;
import r01f.patterns.IsBuilder;
import r01f.util.types.Strings;
import r01f.util.types.Strings.StringExtended;

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
 *               <!-- Cualquier conjunto de propiedades necesarias para la clase generadora de guids -->
 *               <highKeyBytes>9</highKeyBytes>              
 *               <lowKeyBytes>9</lowKeyBytes>
 *               <persistenceClass>com.ejie.r01f.guids.MemoryGUIDPersist</persistenceClass>                                                              
 *           </properties>
 *       </sequence>     
 *   </guidGenerator>
 */
@Accessors(prefix = "_")
public class GUIDDispenserDef 
  implements Debuggable {
	public static int GUID_DEFAULT_LENGTH = 36;
///////////////////////////////////////////////////////////////////////////////////////////
//  STATUS
///////////////////////////////////////////////////////////////////////////////////////////
    @Getter @Setter private AppCode _appCode;      		// AppCode
    @Getter @Setter private String _sequenceName;   	// Sequence name
    @Getter @Setter private String _uniqueID;       	// Unique id (is appended to the generated guids)
    @Getter @Setter private int _length = GUID_DEFAULT_LENGTH;	// GUID size
    @Getter @Setter private String _factoryBindingId; 	// Id of the GUID generator factory (is injected  by GUICE in the GUIDDispenserManager)
    @Getter @Setter private Properties _properties; 	// Dispenser properties

///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & FACTORY
///////////////////////////////////////////////////////////////////////////////////////////
    private GUIDDispenserDef() {
    	// nothing
    }
    /**
     * Constructor
     * @param newAppCode app code
     * @param newSequenceName name of the sequence
     * @param newLength GUIDs size 
     * @param newUniqueID some unique id
     * @param newFactoryBindingId GUID Generator factory class ID (it's injected by GUICE in the GUIDDispenserManager)
     * @param newProps GUIDDispenser properties
     */
    public GUIDDispenserDef(final AppCode newAppCode,final String newSequenceName,
    						final int newLength,final String newUniqueID,
    						final String newFactoryBindingId,
    						final Properties newProps) {
    	// Build base properties
    	GUIDDispenserDefBuilder builder = new GUIDDispenserDefBuilder();
    	builder.forAppSequence(newAppCode,newSequenceName)
    		   .withLength(newLength)
    		   .withUniqueId(newUniqueID)
        	   .build();
    	// other properties
        _factoryBindingId = newFactoryBindingId;
        _properties = newProps;
    }
    /**
     * Constructor using other dispenser 
     * @param other another dispenser
     */
    public GUIDDispenserDef(final GUIDDispenserDef other) {
    	this(other.getAppCode(),other.getSequenceName(),
    		 other.getLength(),
    		 other.getUniqueID(),
    		 other.getFactoryBindingId(),
    		 other.getProperties());
    }
    public static GUIDDispenserDefBuilder builder() {
    	GUIDDispenserDef outDef = new GUIDDispenserDef();
    	return outDef.new GUIDDispenserDefBuilder();
    }
    public class GUIDDispenserDefBuilder 
      implements IsBuilder {
    	
	    public GUIDDispenserDefBuilder forAppSequence(final AppCode appCode,final String seq) {
	    	_appCode = appCode;
	    	_sequenceName = seq;
	    	return this;
	    }
	    public GUIDDispenserDefBuilder withLength(final int length) {
	    	_length = length;
	    	return this;
	    }
	    public GUIDDispenserDefBuilder withUniqueId(final String id) {
	    	_uniqueID = id;
	    	return this;
	    }
	    public GUIDDispenserDef build() {
	        // guid size
	    	if (_uniqueID == null) _uniqueID = "un";   // unknown
	    	if (_uniqueID.length() < 2) _uniqueID = Strings.of(_uniqueID).rightPad('0',2).asString();
	    	return GUIDDispenserDef.this;
	    }
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
     * Sets a property
     * @param propName property name or id
     * @param propValue property value
     */
    public void putProperty(final String propName,final String propValue) {
    	if (_properties == null) _properties = new Properties();
    	_properties.put(propName,propValue);
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
        	} else {
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
