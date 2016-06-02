package r01f.guid;

import java.util.HashMap;
import java.util.Map;

import com.google.inject.Inject;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.xmlproperties.XMLProperties;

/**
 * GUID generation type factories
 * The normal usage is:
 * [OPTION 1]: Use guice to inject the {@link GUIDDispenserManager}:
 * 			   <pre class='brush:java'>
 * 					pubic class MyClass {
 * 						@Inject 
 * 						private GUIDDispenserManager _guidDispenserManager;
 * 
 * 						...
 * 						public void someMethod(...) {
 * 							String guid = _guidDispenserManager.instanceFor("r01fb","default").generateGUID();
 * 						}
 * 					}
 * 			   </pre>
 * 
 * [OPTION 2]: (not recommended) - Use the guice injector directly
 * 			   <pre class='brush:java'>
 * 					GUIDDispenserManager guidDispenserManager = Guice.createInjector(new R01FBootstrapGuiceModule())
 *            												 		 .getInstance(GUIDDispenserManager.class);
 *            		GUIDDispenser disp = guidDispenserManager.instanceFor("r01fb","default");
 *            		String guid = uid = disp.generateGUID();
 * 			   </pre>
 */
@Slf4j
@NoArgsConstructor
public class GUIDDispenserManager {
///////////////////////////////////////////////////////////////////////////////////////////
//  INJECT
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * XMLProperties
	 */
	@Inject 
	private XMLProperties _xmlProperties;			
	/**
	 * Factories of GUIDDispenser types indexed by their id
	 * BEWARE!!		This Map is built at {@link GUIDDispenserGuiceModule} so when a new {@link GUIDDispenser} implementation 
	 * 				is available, it MUST be included at {@link GUIDDispenserGuiceModule}
	 */
	@Inject 
	private Map<String,GUIDDispenserFlavourFactory> _dispensersFactories;	
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * CACHE for the dispensers indexed by appCode.sequenceId
     * (this cache forces this factory to be a singleton managed by guice)  
     */
    private Map<String,GUIDDispenser> _dispensers; 	// Tabla de _dispensers para cada secuencia
    
///////////////////////////////////////////////////////////////////////////////////////////
//  METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    public GUIDDispenser instanceFor(final String appCode) {
    	return this.instanceFor(AppCode.forId(appCode),"default");
    }
    public GUIDDispenser instanceFor(final AppCode appCode) {
    	return this.instanceFor(appCode,"default");
    }
    public GUIDDispenser instanceFor(final String appCode,final String sequenceId) {
    	return this.instanceFor(AppCode.forId(appCode),sequenceId);
    }
    public GUIDDispenser instanceFor(final AppCode appCode,final String sequenceId) {
    	// Try to get a cached GUIDDispenser
    	// BEWARE:  The cache is checked at instanceFor(GUIDDispenserDef) method BUT it's also checked here,
    	//		 	just to avoid loading the GUIDDispenser definition to call instanceFor(GUIDDispenserDef)
    	GUIDDispenser outDispenser = _dispensers != null ? _dispensers.get(appCode + "." + sequenceId)
        											  	 : null;
        if (outDispenser == null) {	// The dispenser was NOT created...
        	GUIDDispenserDef def = new GUIDDispenserDef(_xmlProperties,
        												appCode,sequenceId);	
        	outDispenser = this.instanceFor(def);
        }
        return outDispenser;
    }
    public GUIDDispenser instanceFor(final GUIDDispenserDef dispDef) {
    	GUIDDispenser outDispenser = null; 
    	
		// Create a GUIDDispenser from the definition
		if (dispDef == null) {
			log.error("The GUIDs dispenser definition is null!");
		} else {
			// compose the dispenser key 
			String dispenserKey = dispDef.getAppCode() + "." + dispDef.getSequenceName();
			    
			// Check that the dispenser was NOT previously created
			outDispenser = _dispensers != null ? _dispensers.get(dispenserKey)
											   : null;
	
			// ... if the dispenser was NOT previously created, crete it
			if (outDispenser == null) {
	            log.trace("The GUIDDispenser {} for appCode={} was NOT previously created...create it now!",
	            		  dispDef.getSequenceName(),dispDef.getAppCode());
	            
	            // Use the injected GUIDDispenserFlavourFactory that provides access to the GUIDs factories by type
	            outDispenser = _dispensersFactories.get(dispDef.getFactoryBindingId())
	            								   .factoryFor(dispDef);
				
	            // Cache the dispenser
		        if (outDispenser != null) {
		        	if (_dispensers == null) _dispensers = new HashMap<String,GUIDDispenser>(10,0.5F);
		            _dispensers.put(dispenserKey,
		            				outDispenser);
		            log.trace("GUIDDispenser created: >\r\n{}",dispDef.debugInfo());	// config summary
		        } else {
		        	log.error("The guid dispenser for {}/{} could NOT be created!",
		        			  dispDef.getAppCode(),dispDef.getSequenceName());
		        }
			}
		}
		// return the dispenser
		return outDispenser;
    }
}
