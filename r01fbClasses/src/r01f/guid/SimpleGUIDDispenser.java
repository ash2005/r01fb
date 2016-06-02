package r01f.guid;

import java.security.SecureRandom;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * OID (guid) generator
 * The OIDs to be generated config is at a properties file (see {@link GUIDDispenserDef})
 */
public class SimpleGUIDDispenser 
  implements GUIDDispenser {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static String LETTERS = "0123456789abcdefghijklmnopqrstuvxyz";
	
///////////////////////////////////////////////////////////////////////////////////////////
//  FIELDS
///////////////////////////////////////////////////////////////////////////////////////////
    private GUIDDispenserDef _dispDef = null;		// Dispenser definition
    
///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFACE GUIDDispenserFactory used by Guice AssistedInject to create GUIDDispenser  
//  objects using a GUIDDispenserDef definition that's only known at runtime
//  (ver documentación de GUIDDispenserManagerGuiceModule)
///////////////////////////////////////////////////////////////////////////////////////////
    static interface SimpleGUIDDispenserFactory 
             extends GUIDDispenserFlavourFactory {
    	/* empty */
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & FACTORY
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor used by Guice AssistedInject to inject the definition
     * @param def
     */
    @Inject
    public SimpleGUIDDispenser(@Assisted final GUIDDispenserDef def) {
    	_dispDef = def;
    }
    public static SimpleGUIDDispenser create(final GUIDDispenserDef def) {
    	return new SimpleGUIDDispenser(def);
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ GUIDDispenser
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String generateGUID() {
        int guidLength = _dispDef.getLength();

        SecureRandom randomGen = new SecureRandom();
        
        // Get a random sequence built upon three parts: 
        //		- timeStamp
        //		- unique identifier (machine dependent)
        //		- a random
        long timeStampLong = new java.util.Date().getTime();		// TimeStamp	
        int objectHashCode = System.identityHashCode(this);			// HashCode
        long secureInt = randomGen.nextLong();						// Random
        String uniqueId = Long.toHexString(timeStampLong) + Integer.toHexString(objectHashCode) + Long.toHexString(secureInt);
        
        // Create an byte array with the size of the guid filled with 
        //		- random chars from the left 
        //		- the previous random sequence from the right
        char[] resultCharArray = new char[guidLength];
        // - left pad with random chars
        for (int i = 0; i < guidLength - uniqueId.length(); i++) resultCharArray[i] = LETTERS.charAt(randomGen.nextInt(LETTERS.length()));		
        // - the previously generated sequence inverted
        int cont = uniqueId.length() - 1;								
        for (int i = guidLength; i > 0; i--) {						
            if (cont >= 0) resultCharArray[i - 1] = uniqueId.charAt(cont);
            cont--;
        }
        return _dispDef.guidPrefix() + new String(resultCharArray);

        /*try{
        R01FEJBHomeFactory f = R01FEJBHomeFactory.getInstance();
        Q99FIDGeneratorHome generatorHome =
        ((Q99FIDGeneratorHome)f.lookupByRemoteEJBReference("com.ejie.Q99f.Q99FIDGenerator",com.ejie.q99f.Q99FIDGeneratorHome.class));

        Q99FIDGeneratorBean generator= generatorHome.create();
        guid=generator.generateId("r01e");

        //Timing
        stopwatch.stop();
        log.debug("*** R01ELabelManager.generateGUID executingTime: " + stopwatch);
        //Timing
        return guid;


        }catch(Exception e){
        	log.warn("Exception al GUID " + e);
        	return guid;
        }
        */
    }
}
