package r01f.guid;

import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

/**
 * Maneja un GUID en base a un valor HIGH, un valor LOW que se incrementa
 * localmente y un identificador unico de dominio: HIGH + LOW + UniqueID
 * El GUIDDispenser puede utilizarse en los siguientes casos:
 * CASO 1: Los GUIDs generados han de ser unicos SIEMPRE
 * -----------------------------------------------------
 *      En este caso hay que almacenar el valor HIGH en base de datos ya que
 *      si se reinicia el dispenser (reinicio de la maquina, etc) el siguiente
 *      guid ha de ser unico y para ello el valor de high ha de guardarse...
 *      Un ejemplo de este caso son los oid de los objetos que van a utilizarse
 *      como clave primaria de las tablas en bd
 * CASO 2: Los UIDs generados han de ser unicos EN LA SESION
 * ---------------------------------------------------------
 *      En este caso no importa que si se reinicia el dispense (reinicio de la maquina, etc)
 *      se repitan GUIDs.
 *      Un ejemplo de este caso son los identificadores de token para las
 *      peticiones de paginas html.
 */
@Slf4j
public class HighLowGUIDDispenser 
  implements GUIDDispenser {
///////////////////////////////////////////////////////////////////////////////////////////
//  INJECT
///////////////////////////////////////////////////////////////////////////////////////////
	/**
	 * Mapa de factorías de objetos HighLowGUIDPersist que relaciona el ID del HighLowGUIDPersist con
	 * su factoría
	 * IMPORTANTE!!	El mapa de factorías se "cablea" en el módulo GUIDDispenserGuiceModule,
	 * 				así que cuando aparece una nueva implementación de un HighLowGUIDPersist,
	 * 				hay que incluirlo en la clase GUIDDispenserGuiceModule
	 */
	private Map<String,HighLowGUIDPersist> _highLowGUIDPersistFactories;

///////////////////////////////////////////////////////////////////////////////////////////
//  ESTADO
///////////////////////////////////////////////////////////////////////////////////////////
	private GUIDDispenserDef _dispDef;
    private HighLowGUIDPersist _guidPersist = null;     // Capa de persistencia de GUIDs

    private HighLowKey _currHighKey = null;             // La parte high actual
    private HighLowKey _currLowKey  = null;             // La parte Low actual


///////////////////////////////////////////////////////////////////////////////////////////
//  INTERFAZ GUIDDispenserFactory utilizado en Guice AssistedInject para permitir
//  crear objetos GUIDDispenser en base a una definición GUIDDispenserDef que solo
//  se conoce en tiempo de ejecución (ver documentación de GUIDDispenserManagerGuiceModule)
//	Ver GUIDDispenserFlavourFactory!!!
///////////////////////////////////////////////////////////////////////////////////////////
    static interface HighLowGUIDDispenserFactory 
             extends GUIDDispenserFlavourFactory {
    	/* empty */
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  CONSTRUCTORES
///////////////////////////////////////////////////////////////////////////////////////////
    @Inject
    public HighLowGUIDDispenser(@Assisted final GUIDDispenserDef dispDef,
    									  final Map<String,HighLowGUIDPersist> highLowGUIDPersistFactories) {
    	_highLowGUIDPersistFactories = highLowGUIDPersistFactories;

        _dispDef = new GUIDDispenserDef(dispDef);

        // Inicializacion de la clase que controla la persistencia
        _guidPersist = _highLowGUIDPersistFactories.get(dispDef.getProperty("persistenceBindingId"));

        // Comprobar las propiedades definidas en el fichero properties
        // Tamaño del guid
        if (_dispDef.getProperty("highKeyBytes") == null) {
            log.warn("No se ha definido la propiedad {}properties/highKeyBytes en el fichero definición de guids de la aplicacion {}. Se toma un tamaño de {}",
            		 _xPathBase(_dispDef.getSequenceName()),_dispDef.getAppCode().asString(),Integer.toString(_dispDef.getLength() / 2));
            _dispDef.getProperties().put("highKeyBytes",Integer.toString(_dispDef.getLength() / 2));
        }
        if (_dispDef.getProperty("lowKeyBytes") == null) {
            log.warn("No se ha definido la propiedad {}properties/lowKeyBytes en el fichero de properties de la aplicacion {}. Se toma un tamaño de {}",
            		 _xPathBase(_dispDef.getSequenceName()),_dispDef.getAppCode().asString(),Integer.toString(_dispDef.getLength() / 2));
            _dispDef.getProperties().put("lowKeyBytes",Integer.toString(_dispDef.getLength() / 2));
        }
        // Persistencia del GUID
        if (_dispDef.getProperty("persistenceBindingId") == null) {
            log.warn("No se ha definido la propiedad {}properties/persistenceBindingId en el fichero definición de guids de la aplicacion {}. Se toma la persistencia en MEMORIA por defecto!!!!",
            		 _xPathBase(_dispDef.getSequenceName()),_dispDef.getAppCode());
            _dispDef.getProperties().put("persistenceBindingId","inMemoryHighKeyPersist");
        }


        // Inicializacion de las claves low y high
        _currLowKey = new HighLowKey(Integer.parseInt(_dispDef.getProperty("lowKeyBytes")));
        _moveToNextHighKey();          // Aumenta en uno el highKey
    }
    private static String _xPathBase(String sequenceName) {
    	return "guidGenerator/sequence[@name='" + sequenceName + "']/";
    }
///////////////////////////////////////////////////////////////////////////////////////////
//  METODOS PUBLICOS
///////////////////////////////////////////////////////////////////////////////////////////
    @Override
    public String generateGUID() {
        String theUIdString = null;

        // Incrementa el LOW (el HIGH se mantiene hasta que se alcanza el
        // valor máximo para el valor LOW, en cuyo caso se obtiene un nuevo
        // valor HIGH y se reinicializa el LOW)
        synchronized (_currLowKey) {	// OJO!!! el acceso ha de ser SINCRONIZADO ya que se cambia el estado
            try {
                _currLowKey.increment();
            } catch (HighLowMaxForKeyReachedException maxEx) {
            	// Se han terminado los valores LOW... incrementar el HIGH y volver a 0 el LOW
                _moveToNextHighKey();
                _currLowKey.setToZero();
            }
        }
        // Devuelve el GUID componiendo HIHG + LOW + Identificador Unico
        theUIdString = _currHighKey.toString() + _currLowKey.toString() + _dispDef.getUniqueID();
        return theUIdString;
    }

///////////////////////////////////////////////////////////////////////////////////////////
//  PRIVATE METHODS
///////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Incrementa en uno el valor actual de la parte high, y luego
     * al objeto de persistencia para actualizar el valor almacenado
     */
    private boolean _moveToNextHighKey() {
    	boolean outOK = false;
        try {
            // Incrementar el valor de la parte high
            if (_currHighKey != null) {
                // Aumentar el valor de HIGH y actualizarlo en la persistencia
                _currHighKey.increment();
	            outOK = _guidPersist.updateGUID(_dispDef,_currHighKey);		// true se se ha actualizado correctamente el highKey
            } else {
                // No habia HIGHKey para esta secuencia, hay que obtenerla de la persistencia o de cero si nunca se había creado
                _currHighKey = _guidPersist.getHighKeyValue(_dispDef);
                if (_currHighKey != null) {
                	_currHighKey.setToZero();      // Reinicializar un HIGH vacio (a cero)
                	outOK = _guidPersist.updateGUID(_dispDef,_currHighKey);
                } else {
                	outOK = true;
                }
            }
        } catch (HighLowMaxForKeyReachedException maxKeyEx) {
            log.error("Se han agotado los HIGH; hay un riesgo GRANDE de repetición de GUIDs... revisa el tamaño del guid (definición de guids)!!",maxKeyEx);
            _currHighKey.setToZero();		// se empieza de cero
        }
        if (!outOK) log.error("NO se ha conseguido actualizar el valor HIGH en la persistencia del GUID");
        return outOK;
    }


}
