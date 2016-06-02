package r01f.guid;

import java.util.Map;
import java.util.Properties;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import r01f.guids.CommonOIDs.AppCode;
import r01f.patterns.IsBuilder;
import r01f.util.types.Strings;
import r01f.util.types.collections.CollectionUtils;

/**
 * Builder for {@link GUIDDispenserDef} objects
 */
@NoArgsConstructor(access=AccessLevel.PRIVATE)
public abstract class GUIDDispenserDefBuilder 
  implements IsBuilder {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    public static GUIDDispenserDefBuilderUniqueIDStep builderFor(final AppCode appCode,final String seq) {
    	return new GUIDDispenserDefBuilder() {/* nothing */}
    					.new GUIDDispenserDefBuilderUniqueIDStep(appCode,seq);
    }
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)    
    public class GUIDDispenserDefBuilderUniqueIDStep {
	    private final AppCode _appCode;      		
	    private final String _sequenceName;
	    
	    
	    public GUIDDispenserDefBuilderLengthStep withUniqueID(final String uniqueId) {
	    	String theUniqueId = uniqueId == null ? "un" : uniqueId;   // unknown
	    	if (theUniqueId.length() < 2) theUniqueId = Strings.of(theUniqueId)
	    													   .rightPad('0',2).asString();
	    	return new GUIDDispenserDefBuilderLengthStep(_appCode,_sequenceName,uniqueId);
	    }
    }
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)    
	public class GUIDDispenserDefBuilderLengthStep {
	    private final AppCode _appCode;      		
	    private final String _sequenceName;
	    private final String _uniqueID;
	    
	    public GUIDDispenserDefBuilderFactoryBindingIdStep withLength(final int length) {
	    	return new GUIDDispenserDefBuilderFactoryBindingIdStep(_appCode,_sequenceName,_uniqueID,
	    														   length);
	    }
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)    
	public class GUIDDispenserDefBuilderFactoryBindingIdStep {
	    private final AppCode _appCode;      		
	    private final String _sequenceName;
	    private final String _uniqueID;
	    private final int _length;
	    
	    public GUIDDispenserDefBuilderFactoryPropertiesStep usingGUIDDispenserFactoryIdentifiedBy(final String id) {
	    	return new GUIDDispenserDefBuilderFactoryPropertiesStep(_appCode,_sequenceName,_uniqueID,
	    															_length,
	    															id);
	    }
	    public GUIDDispenserDefBuilderFactoryPropertiesStep usingGUIDDispenserType(final Class<? extends GUIDDispenser> dispType) {
	    	String id = null;
	    	if (dispType == SimpleGUIDDispenser.class) {
	    		id = "simpleGUIDDispenser";
	    	} else if (dispType == HighLowGUIDDispenser.class) {
	    		id = "highLowGUIDDispenser";
	    	} else {
	    		throw new IllegalArgumentException(dispType + " is NOT a valid GUIDispenser type");
	    	}
	    	return this.usingGUIDDispenserFactoryIdentifiedBy(id);
	    }
	    public GUIDDispenserDefBuilderBuildStep usingDefaultGUIDDispenser() {
	    	return new GUIDDispenserDefBuilderBuildStep(_appCode,_sequenceName,_uniqueID,
	    												_length,
	    												"simpleGUIDDispenser",null);
	    }
	}
	@RequiredArgsConstructor(access=AccessLevel.PRIVATE)    
	public class GUIDDispenserDefBuilderFactoryPropertiesStep {
	    private final AppCode _appCode;      		
	    private final String _sequenceName;
	    private final String _uniqueID;
	    private final int _length;
	    private final String _factoryBingingId;
	    
	    public GUIDDispenserDefBuilderBuildStep using(final Properties props) {
	    	return new GUIDDispenserDefBuilderBuildStep(_appCode,_sequenceName,_uniqueID,
	    												_length,
	    												_factoryBingingId,props);
	    }
	    public GUIDDispenserDefBuilderBuildStep using(final Map<String,String> props) {
	    	return new GUIDDispenserDefBuilderBuildStep(_appCode,_sequenceName,_uniqueID,
	    												_length,
	    												_factoryBingingId,CollectionUtils.toProperties(props));
	    }
	     public GUIDDispenserDefBuilderBuildStep withoutProperties() {
	    	return new GUIDDispenserDefBuilderBuildStep(_appCode,_sequenceName,_uniqueID,
	    												_length,
	    												_factoryBingingId,null);
	     }
	}
    @RequiredArgsConstructor(access=AccessLevel.PRIVATE)
    public static class GUIDDispenserDefBuilderBuildStep {
	    private final AppCode _appCode;      		// AppCode
	    private final String _sequenceName;   		// Sequence name
	    private final String _uniqueID;       		// Unique id (is appended to the generated guids)
	    private final int _length;					// GUID size
	    private final String _factoryBindingId; 	// Id of the GUID generator factory (is injected  by GUICE in the GUIDDispenserManager)
	    private final Properties _properties; 		// Dispenser properties
	    
	    public GUIDDispenserDef build() {	    	
	    	return new GUIDDispenserDef(_appCode,_sequenceName,_uniqueID,
	    								_length,
	    								_factoryBindingId,_properties);
	    }
    }
}
