package r01f.guid;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import r01f.guids.CommonOIDs.AppCode;
import r01f.util.types.Strings;
import r01f.xmlproperties.XMLProperties;
import r01f.xmlproperties.XMLPropertiesForApp;

import com.google.inject.Inject;

/**
 * Acceso a las factorías de generación de GUIDs
 * El uso habitual es:
 * [OPCION 1]: Inyectar el GUIDDispenserManager como servicio utilizando GUICE:
 * 			   <pre class='brush:java'>
 * 					pubic class MyClass {
 * 						@Inject GUIDDispenserManager _guidDispenserManager;
 * 
 * 						...
 * 						public void someMethod(...) {
 * 							String guid = _guidDispenserManager.instanceFor("r01fb","default").generateGUID();
 * 						}
 * 					}
 * 			   </pre>
 * 
 * [OPCION 2]: (no recomendada) - Utilizar el inyector de Guice
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
	 * Acceso a XMLProperties
	 */
	@Inject private XMLProperties _xmlProperties;			// Acceso a XMLProperties
	/**
	 * Mapa de factorías de objetos GUIDDispenser que relaciona el ID del GUIDDispenser con
	 * su factoría
	 * IMPORTANTE!!	El mapa de factorías se "cablea" en el módulo GUIDDispenserGuiceModule, 
	 * 				así que cuando aparece una nueva implementación de un GUIDDispenser, 
	 * 				hay que incluirlo en la clase GUIDDispenserGuiceModule
	 */
	@Inject private Map<String,GUIDDispenserFlavourFactory> _dispensersFactories;	// Factorías de GUIDDispensers
///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
    /** 
     * CACHE que contiene los dispensers creados asociados por appCode.sequenceId
     * (por eso es importante que esta clase sea un Singleton en el módulo Guice)  
     */
    private Map<String,GUIDDispenser> _dispensers; 	// Tabla de _dispensers para cada secuencia
    
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS
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
    	// El GUIDDispenser de la cache...
    	// NOTA: aunqe la cache de dispensers se comprueba en el metodo instanceFor(GUIDDispenserDef),
    	//		 aqui también se comprueba para evitar tener que cargar la definición del GUIDDispenser
    	//		 simplemente para llamar al método instanceFor(GUIDDispenserDef)
    	GUIDDispenser outDispenser = _dispensers != null ? _dispensers.get(appCode + "." + sequenceId)
        											  	 : null;
        if (outDispenser == null) {	// El dispenser NO estaba creado... habrá que crearlo utilizando la factoría
        	GUIDDispenserDef def = _loadDispenserDefFor(_xmlProperties,
        												appCode,sequenceId);	
        	outDispenser = this.instanceFor(def);
        }
        return outDispenser;
    }
    public GUIDDispenser instanceFor(GUIDDispenserDef dispDef) {
    	GUIDDispenser outDispenser = null; 
    	
		// Obtener un descriptor de dispenser de GUIDs
		if (dispDef == null) {
			log.error("La definicion del dispenser de GUIDs es nula; NO se puede crear el dispenser");
		} else {
			// Obtener la clave del dispenser 
			String dispenserKey = dispDef.getAppCode() + "." + dispDef.getSequenceName();
			    
			// Verificar que no se ha creado anteriormente, si ya se había creado retornarlo
			outDispenser = _dispensers != null ? _dispensers.get(dispenserKey)
											   : null;
	
			// No existe dispenser con esa clave, por tanto crearlo
			if (outDispenser == null) {
	            log.trace("El GUIDDispenser {} de la aplicacion {} NO estaba creado...se crea ahora!",
	            		  dispDef.getSequenceName(),dispDef.getAppCode());
	            
	            // Utilizar el GUIDDispenserFlavourFactory que inyecta guice y que da acceso a cada
	            // una de las factorías de GUIDs en función del tipo
	            outDispenser = _dispensersFactories.get(dispDef.getFactoryBindingId())
	            								   .factoryFor(dispDef);
				
	            // Poner el dispenser en el mapa de dispenser disponibles...
		        if (outDispenser != null) {
		        	if (_dispensers == null) _dispensers = new HashMap<String,GUIDDispenser>(10,0.5F);
		            _dispensers.put(dispenserKey,outDispenser);
		            log.trace("GUIDDispenser created: >\r\n{}",dispDef.debugInfo());	// Resumen de la configuracion
		        } else {
		        	log.error("No se puede crear el dispenser de guids: {}/{}",
		        			  dispDef.getAppCode(),dispDef.getSequenceName());
		        }
			}
		}
		// Devolver el dispenser recien creado
		return outDispenser;
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CARGA DE LA DEFINICIÓN DEL DISPENSER
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Obtiene la ruta xPath base donde se encuentran las propiedades del guidGenerator
     * @return la ruta xPath (guidGenerator/sequence[@name='" + _sequenceName + "']/)
     */
    private static String _xPathBase(String sequenceName) {
    	return "guidGenerator/sequence[@name='" + sequenceName + "']/";
    }
    /**
     * Carga la configuracion del fichero de propiedades de la aplicacion
     * @param appCode Codigo de aplicacion
     * @param sequenceName Nombre de la secuencia
     */
    private static GUIDDispenserDef _loadDispenserDefFor(final XMLProperties xmlProperties,
    											  		 final AppCode appCode,final String sequenceName) {
        log.trace("Loading the config for dispenser {} in app {}",sequenceName,appCode);
        
        if (appCode == null || sequenceName == null) {
        	String err = Strings.customize("No se puede cargar la configuracion del GUIDDispenser ya que el codigo de aplicacion o el nombre de secuencia es null: appCode={} / sequenceId={}",appCode,sequenceName).toString();
            throw new IllegalArgumentException(err);
        }
        
        // La información de los dispensers se cargan de un fichero de propiedades de la aplicación
 		XMLPropertiesForApp props = xmlProperties.forApp(appCode);
 		
 		// - Tamaño de la secuencia
 		int length = props.of("guids").getInteger(_xPathBase(sequenceName) + "length",GUIDDispenserDef.GUID_DEFAULT_LENGTH);
 		
        // - Identificador unico de la secuencia
        String uniqueID = props.of("guids").getString(_xPathBase(sequenceName) + "uniqueId","0-unknown");
        if (uniqueID == null) log.warn("No se ha definido la propiedad {}/uniqueId en el fichero de properties de la aplicacion {}. Se toma un valor '0-unknown'",
            		 					_xPathBase(sequenceName),sequenceName,appCode.asString());
        // - Identificador de la clase que genera los guids (se inyecta utilizando GUICE en el GUIDDispenserManager)
        String factoryBindingId = props.of("guids").getString(_xPathBase(sequenceName) + "factoryBindingId","simpleGUIDDispenser");
        if (factoryBindingId == null) log.warn("No se ha definido la propiedad {}/factoryBindingId en el fichero de properties de definición de guids de la aplicacion {}. Se toma un valor 'simpleGUIDDispenser'",
            		 					 	   _xPathBase(sequenceName),sequenceName,appCode.asString());
        // - Propiedades de la clase generadora...
        Properties properties = props.of("guids").getProperties(_xPathBase(sequenceName) + "properties");
        
        // - Construir el guidDispenser
        GUIDDispenserDef outDispDef = new GUIDDispenserDef(appCode,sequenceName,
        												   length,
        												   uniqueID,
        												   factoryBindingId,
        												   properties);
        return outDispDef;
    }
}
