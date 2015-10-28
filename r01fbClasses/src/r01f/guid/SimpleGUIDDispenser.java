package r01f.guid;

import java.security.SecureRandom;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Generador de OIDS
 * La configuración del generador de OIDs se hace en el fichero de propiedades de la aplicación en una sección
 * como la siguiente:
 *  <guidGenerator>
 *       <sequence name='default'>
 *           <uniqueId>desa</uniqueId>   <!-- loc=lc,sb_des=sd,sb_pru=sp,des=ds,pru=pr,pro=pd -->
 *           <bindingId></bindingId>
 *           <length>36</length>
 *       </sequence>
 *  </guidGenerator>
 *  Es necesario indicar:
 *          - El codigo de aplicacion para el que se quiere generar OIDs
 *          - La secuencia para la que se quieren generar OIDs
 *
 *  En caso en que falten parametros se toma la siguiente configuración:
 *          - uniqueID = unknown
 *          - length = 36
 *  El identificador generado para el entorno local de la aplicación x42t y length de 35 es de la forma:
 * 			Código de aplicación + entorno + guid(35caracteres)
 * 			x42tlc010e649a03f96408eeb78e532b216a3f94d
 */
public class SimpleGUIDDispenser 
  implements GUIDDispenser {
/////////////////////////////////////////////////////////////////////////////////////////
//  
/////////////////////////////////////////////////////////////////////////////////////////
	private static String LETTERS = "0123456789abcdefghijklmnopqrstuvxyz";
	
///////////////////////////////////////////////////////////////////////////////////////////
//  INJECT
///////////////////////////////////////////////////////////////////////////////////////////
	/** en este caso NO se inyecta NADA (no es necesario) */
	
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
    private GUIDDispenserDef _dispDef = null;		// Definicion del dispenser
    
///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ GUIDDispenserFactory utilizado en Guice AssistedInject para permitir 
//  crear objetos GUIDDispenser en base a una definición GUIDDispenserDef que solo 
//  se conoce en tiempo de ejecución (ver documentación de GUIDDispenserManagerGuiceModule)
//  Ver GUIDDispenserFlavourFactory!!!
///////////////////////////////////////////////////////////////////////////////////////////
    static interface SimpleGUIDDispenserFactory 
             extends GUIDDispenserFlavourFactory {
    	/* empty */
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTOR & FACTORY
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Constructor a partir de la definición del dispenser
     * Es utilizado por Guice AssistedInject para inyectar cualquier cosa (en este caso NO se inyecta nada -ver HighLowGUIDDispenser-)
     * a la vez que se pasan parámetros en tiempo de ejecución
     * @param def definición del dispenser
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
        
        // Obtener una secuencia aleatoria compuesta de tres partes: timeStamp, identificador único que depende de la máquina y un random
        long timeStampLong = new java.util.Date().getTime();		// TimeStamp	
        int objectHashCode = System.identityHashCode(this);			// HashCode que depende de la máquina
        long secureInt = randomGen.nextLong();						// Random
        String uniqueId = Long.toHexString(timeStampLong) + Integer.toHexString(objectHashCode) + Long.toHexString(secureInt);
        
        // crear un array de bytes del tamaño del guid relleno con caracteres aleatorios por la izq y con la secuencia aleatoria por la derecha
        char[] resultCharArray = new char[guidLength];
        // - caracteres aleatorios por la izq
        for (int i = 0; i < guidLength - uniqueId.length(); i++) resultCharArray[i] = LETTERS.charAt(randomGen.nextInt(LETTERS.length()));		
        // - secuencia aleatoria por la drcha en sentido inverso
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
